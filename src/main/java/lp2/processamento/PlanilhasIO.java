package lp2.processamento;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

/** Operações Excel e CSV, encapsulando a biblioteca Apache POI. */
public final class PlanilhasIO {
    private PlanilhasIO() {}

    public static TabelaDados ler(Path caminho, String abaPreferida) throws IOException {
        String nome = caminho.getFileName().toString().toLowerCase(Locale.ROOT);
        if (nome.endsWith(".csv")) return lerCsv(caminho);
        try (InputStream in = Files.newInputStream(caminho); Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = abaPreferida == null ? null : wb.getSheet(abaPreferida);
            if (sheet == null) sheet = wb.getSheetAt(0);
            return lerAba(sheet);
        }
    }

    public static Map<String,TabelaDados> lerTodasAbas(Path caminho) throws IOException {
        Map<String,TabelaDados> resultado = new LinkedHashMap<>();
        try (InputStream in = Files.newInputStream(caminho); Workbook wb = WorkbookFactory.create(in)) {
            for (Sheet aba : wb) resultado.put(aba.getSheetName(), lerAba(aba));
        }
        return resultado;
    }

    static TabelaDados lerAba(Sheet sheet) {
        Row cab = sheet.getRow(0);
        if (cab == null) throw new ProcessamentoException("Aba sem cabeçalho: " + sheet.getSheetName());
        DataFormatter formatador = new DataFormatter(new Locale("pt", "BR"));
        List<String> colunas = new ArrayList<>();
        for (int i = 0; i < cab.getLastCellNum(); i++) colunas.add(formatador.formatCellValue(cab.getCell(i)).trim());
        TabelaDados tabela = new TabelaDados(colunas);
        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Map<String,Object> valores = new LinkedHashMap<>();
            boolean naoVazia = false;
            for (int i = 0; i < colunas.size(); i++) {
                Cell cell = row.getCell(i);
                Object valor = valorCelula(cell, formatador);
                valores.put(colunas.get(i), valor);
                if (valor != null && !TabelaDados.texto(valor).isEmpty()) naoVazia = true;
            }
            if (naoVazia) tabela.adicionarLinha(valores);
        }
        return tabela;
    }

    private static Object valorCelula(Cell c, DataFormatter formatter) {
        if (c == null || c.getCellType() == CellType.BLANK) return null;
        CellType tipo = c.getCellType() == CellType.FORMULA ? c.getCachedFormulaResultType() : c.getCellType();
        return switch (tipo) {
            case STRING -> c.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(c) ? c.getLocalDateTimeCellValue() : c.getNumericCellValue();
            case BOOLEAN -> c.getBooleanCellValue();
            default -> formatter.formatCellValue(c);
        };
    }

    private static TabelaDados lerCsv(Path caminho) throws IOException {
        // Suporta delimitador vírgula ou ponto e vírgula, e BOM UTF-8.
        String primeira = Files.readAllLines(caminho, StandardCharsets.UTF_8).stream().findFirst().orElse("");
        char sep = primeira.chars().filter(ch -> ch == ';').count() > primeira.chars().filter(ch -> ch == ',').count() ? ';' : ',';
        CSVFormat formato = CSVFormat.DEFAULT.builder().setDelimiter(sep).setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).build();
        try (Reader reader = Files.newBufferedReader(caminho, StandardCharsets.UTF_8);
             CSVParser parser = formato.parse(reader)) {
            List<String> colunas = new ArrayList<>(parser.getHeaderNames());
            if (!colunas.isEmpty()) colunas.set(0, colunas.get(0).replace("\uFEFF", ""));
            TabelaDados tabela = new TabelaDados(colunas);
            for (CSVRecord record : parser) {
                Map<String,Object> linha = new LinkedHashMap<>();
                for (int i = 0; i < colunas.size(); i++) linha.put(colunas.get(i).trim(), i < record.size() ? record.get(i) : null);
                tabela.adicionarLinha(linha);
            }
            return tabela;
        }
    }

    public static void escreverTabela(Workbook wb, String nomeAba, TabelaDados tabela, boolean formulaSoma) {
        Sheet antiga = wb.getSheet(nomeAba);
        if (antiga != null) wb.removeSheetAt(wb.getSheetIndex(antiga));
        Sheet aba = wb.createSheet(nomeAba);
        CellStyle cab = wb.createCellStyle();
        Font fonte = wb.createFont(); fonte.setBold(true); cab.setFont(fonte);
        cab.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        cab.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle data = wb.createCellStyle();
        data.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
        Row hr = aba.createRow(0);
        for (int j = 0; j < tabela.colunas().size(); j++) {
            Cell c = hr.createCell(j); c.setCellValue(tabela.colunas().get(j)); c.setCellStyle(cab);
        }
        for (int i = 0; i < tabela.linhas().size(); i++) {
            Row r = aba.createRow(i+1);
            Map<String,Object> linha = tabela.linhas().get(i);
            for (int j = 0; j < tabela.colunas().size(); j++) {
                String nome = tabela.colunas().get(j);
                Cell c = r.createCell(j);
                if (formulaSoma && nome.equals("SOMA")) {
                    List<String> referencias = new ArrayList<>();
                    for (String f : List.of("VIRTUA", "CHIP", "PORT", "TV", "FONE", "DCC")) {
                        int pos = tabela.indice(f);
                        if (pos >= 0) referencias.add(org.apache.poi.ss.util.CellReference.convertNumToColString(pos) + (i+2));
                    }
                    c.setCellFormula("SUM(" + String.join(",", referencias) + ")");
                } else {
                    Object valor = linha.get(nome);
                    if (valor instanceof Number n) c.setCellValue(n.doubleValue());
                    else if (valor instanceof Boolean b) c.setCellValue(b);
                    else if (valor instanceof java.time.LocalDateTime dt) { c.setCellValue(dt); c.setCellStyle(data); }
                    else if (valor instanceof LocalDate d) { c.setCellValue(d); c.setCellStyle(data); }
                    else if (valor != null) c.setCellValue(valor.toString());
                }
                if (nome.equals("DataInstalacao") && c.getCellType() == CellType.NUMERIC) c.setCellStyle(data);
            }
        }
        aba.createFreezePane(0, 1);
        if (!tabela.linhas().isEmpty() && !tabela.colunas().isEmpty())
            aba.setAutoFilter(new CellRangeAddress(0, tabela.linhas().size(), 0, tabela.colunas().size()-1));
        for (int j=0; j<tabela.colunas().size();j++) aba.setColumnWidth(j, Math.min(28, Math.max(12,tabela.colunas().get(j).length()+3))*256);
    }

    public static void salvar(Workbook wb, Path destino) throws IOException {
        Files.createDirectories(destino.getParent());
        Path temp = Files.createTempFile(destino.getParent(), "salvando_", ".xlsx");
        try {
            try (OutputStream out = Files.newOutputStream(temp)) { wb.write(out); }
            try { Files.move(temp, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, destino, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }

    public static void salvarComBackup(Workbook planilha, Path destino) throws IOException {
        byte[] anterior = Files.notExists(destino, LinkOption.NOFOLLOW_LINKS)
            ? null : Files.readAllBytes(destino);
        salvarComBackup(planilha, destino, anterior, verificacao -> { });
    }

    public static void salvarComBackup(Workbook planilha, Path destino, byte[] versaoAnterior,
                                       Consumer<Workbook> validarConteudo) throws IOException {
        Objects.requireNonNull(validarConteudo, "A validação do conteúdo é obrigatória.");
        byte[] anterior = versaoAnterior == null ? null : versaoAnterior.clone();
        Path arquivo = destino.toAbsolutePath().normalize();
        Path pasta = arquivo.getParent();
        Files.createDirectories(pasta);
        Path temporario = Files.createTempFile(pasta, "." + arquivo.getFileName() + "-", ".tmp");
        Path backupTemporario = null;
        Throwable falha = null;
        try {
            try (FileChannel canal = FileChannel.open(temporario, StandardOpenOption.WRITE);
                 OutputStream saida = Channels.newOutputStream(canal)) {
                planilha.write(saida);
                saida.flush();
                canal.force(true);
            }
            validarPlanilha(temporario, validarConteudo);
            verificarVersao(arquivo, anterior);
            if (anterior != null) {
                Path backup = arquivo.resolveSibling(arquivo.getFileName() + ".bak");
                backupTemporario = Files.createTempFile(pasta, "." + arquivo.getFileName() + "-", ".tmp");
                prepararBackup(arquivo, backupTemporario, anterior);
                verificarVersao(arquivo, anterior);
                substituirAtomicamente(backupTemporario, backup);
                verificarVersao(backup, anterior);
            }
            verificarVersao(arquivo, anterior);
            substituirAtomicamente(temporario, arquivo);
        } catch (IOException | RuntimeException | Error e) {
            falha = e;
            throw e;
        } finally {
            limparTemporarios(falha, temporario, backupTemporario);
        }
    }

    private static void validarPlanilha(Path arquivo, Consumer<Workbook> validarConteudo) throws IOException {
        try (InputStream entrada = Files.newInputStream(arquivo);
             Workbook verificacao = WorkbookFactory.create(entrada)) {
            if (verificacao.getNumberOfSheets() == 0) {
                throw new IOException("Planilha sem abas: " + arquivo);
            }
            validarConteudo.accept(verificacao);
        }
    }

    private static void verificarVersao(Path arquivo, byte[] esperada) throws IOException {
        boolean ausente = Files.notExists(arquivo, LinkOption.NOFOLLOW_LINKS);
        if (esperada == null ? !ausente : ausente || !Arrays.equals(esperada, Files.readAllBytes(arquivo))) {
            throw new IOException("Arquivo alterado externamente durante a gravação: " + arquivo
                + ". Recarregue a base antes de tentar novamente.");
        }
    }

    static void prepararBackup(Path arquivo, Path temporario, byte[] anterior) throws IOException {
        try (InputStream entrada = Files.newInputStream(arquivo);
             FileChannel canal = FileChannel.open(temporario, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
             OutputStream saida = Channels.newOutputStream(canal)) {
            entrada.transferTo(saida);
            saida.flush();
            canal.force(true);
        }
        verificarVersao(temporario, anterior);
        validarPlanilha(temporario, verificacao -> { });
    }

    static void substituirAtomicamente(Path origem, Path destino) throws IOException {
        try {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            AtomicMoveNotSupportedException falha = new AtomicMoveNotSupportedException(
                origem.toString(), destino.toString(), "Substituição atômica indisponível; operação cancelada.");
            falha.initCause(e);
            throw falha;
        }
    }

    private static void limparTemporarios(Throwable falha, Path... arquivos) throws IOException {
        IOException falhaLimpeza = null;
        for (Path arquivo : arquivos) {
            if (arquivo == null) continue;
            try {
                Files.deleteIfExists(arquivo);
            } catch (IOException e) {
                if (falha != null) {
                    falha.addSuppressed(e);
                } else if (falhaLimpeza == null) {
                    falhaLimpeza = e;
                } else {
                    falhaLimpeza.addSuppressed(e);
                }
            }
        }
        if (falhaLimpeza != null) throw falhaLimpeza;
    }

    public static void salvarAnalitico(TabelaDados dados, Path destino) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            escreverTabela(wb, "Analítico", dados, true);
            salvar(wb, destino);
        }
    }
}
