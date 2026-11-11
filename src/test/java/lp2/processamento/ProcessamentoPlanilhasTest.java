package lp2.processamento;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProcessamentoPlanilhasTest {
    @Rule
    public final TemporaryFolder pastaTemporaria = new TemporaryFolder();

    @Test
    public void resumoContaApenasProdutosNaoVaziosEIncluiCategoriasEmBranco() throws Exception {
        TabelaDados dados = new TabelaDados(List.of(
                "Supervisor", "Vendedor", "DataInstalacao", "Produtos",
                "VIRTUA", "CHIP", "PORT", "TV", "SOMA", "Valor"));
        dados.adicionarLinha(linha("Ana", LocalDate.of(2026, 11, 10), "VIRTUA"));
        dados.adicionarLinha(linha("Ana", LocalDate.of(2026, 11, 10), ""));
        dados.adicionarLinha(linha("", null, "TV"));

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            new GeradorRelatorios().criar(wb, dados);

            var resumo = wb.getSheet("Planilha2");
            int colunaData = coluna(resumo.getRow(0), "10/11/2026");
            int colunaVazia = coluna(resumo.getRow(0), "(blank)");
            int linhaAna = linha(resumo, "Ana");
            int linhaVazia = linha(resumo, "(blank)");
            assertEquals(1, (int) resumo.getRow(linhaAna).getCell(colunaData).getNumericCellValue());
            assertEquals(0, (int) resumo.getRow(linhaAna).getCell(colunaVazia).getNumericCellValue());
            assertEquals(0, (int) resumo.getRow(linhaVazia).getCell(colunaData).getNumericCellValue());
            assertEquals(1, (int) resumo.getRow(linhaVazia).getCell(colunaVazia).getNumericCellValue());
        }
    }

    @Test
    public void entradasDaMesmaEmpresaSobrescrevemComoNoPythonEConsolidamUmaVez() throws Exception {
        Path raiz = pastaTemporaria.newFolder().toPath();
        Path entrada = raiz.resolve("dados/importacao");
        Path saida = raiz.resolve("dados/exportacao/arquivos_para_processar/saida");
        Files.createDirectories(entrada);
        Files.createDirectories(saida);
        Files.writeString(saida.resolve("antigo.tmp"), "obsoleto");
        criarEntrada(entrada.resolve("RDT_A.xlsx"), "Primeiro");
        criarEntrada(entrada.resolve("RDT_B.xlsx"), "Segundo");

        ResultadoProcessamento resultado = new ProcessadorPlanilhas(raiz).executar();

        assertEquals(1, resultado.getArquivosGerados().size());
        assertTrue(Files.exists(resultado.getConsolidado()));
        assertFalse(Files.exists(saida.resolve("antigo.tmp")));
        TabelaDados finalGerado = PlanilhasIO.ler(resultado.getArquivosGerados().get(0), "Analítico");
        assertEquals(1, finalGerado.linhas().size());
        assertEquals("Segundo", finalGerado.linhas().get(0).get("Vendedor"));
    }

    @Test
    public void cruzamentoMantemFormatacaoExistenteNasDivergencias() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            var sheet = wb.createSheet("virtua");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("NR_CONTRATO");
            Row linha = sheet.createRow(1);
            var contrato = linha.createCell(0);
            contrato.setCellValue("100");
            var estiloOriginal = wb.createCellStyle();
            estiloOriginal.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            estiloOriginal.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            contrato.setCellStyle(estiloOriginal);

            TabelaDados analitico = new TabelaDados(List.of("VIRTUA", "Contrato"));
            analitico.adicionarLinha(Map.of("VIRTUA", 1, "Contrato", "200"));
            new CruzadorContratos().cruzar(
                    wb, analitico, "virtua", "VIRTUA", "NR_CONTRATO", "virtua_analitico");

            assertEquals(IndexedColors.YELLOW.getIndex(),
                    linha.getCell(0).getCellStyle().getFillForegroundColor());
            assertEquals(IndexedColors.RED.getIndex(),
                    wb.getFontAt(linha.getCell(0).getCellStyle().getFontIndexAsInt()).getColor());
            assertEquals("200", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals(IndexedColors.GREEN.getIndex(),
                    wb.getFontAt(sheet.getRow(2).getCell(0).getCellStyle().getFontIndexAsInt()).getColor());
        }
    }

    @Test
    public void numerosBrasileirosSaoInterpretadosCorretamente() {
        assertEquals(1234.56, TabelaDados.numero("1.234,56"), 0.0001);
        assertEquals(1234.56, TabelaDados.numero("1,234.56"), 0.0001);
        assertEquals(-1234.56, TabelaDados.numero("(R$ 1.234,56)"), 0.0001);
    }

    @Test
    public void contratoNumericoIgnoraZerosDeFormatacaoDoExcel() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            var sheet = wb.createSheet("virtua");
            sheet.createRow(0).createCell(0).setCellValue("NR_CONTRATO");
            var contrato = sheet.createRow(1).createCell(0);
            contrato.setCellValue(123);
            var estilo = wb.createCellStyle();
            estilo.setDataFormat(wb.createDataFormat().getFormat("000000"));
            contrato.setCellStyle(estilo);

            TabelaDados analitico = new TabelaDados(List.of("VIRTUA", "Contrato"));
            analitico.adicionarLinha(Map.of("VIRTUA", 1, "Contrato", "123"));
            new CruzadorContratos().cruzar(
                    wb, analitico, "virtua", "VIRTUA", "NR_CONTRATO", "virtua_analitico");

            assertEquals("123", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals(1, sheet.getLastRowNum());
        }
    }

    private static int coluna(Row linha, String nome) {
        for (var celula : linha) {
            if (nome.equals(celula.getStringCellValue())) return celula.getColumnIndex();
        }
        throw new AssertionError("Coluna não encontrada: " + nome);
    }

    private static int linha(org.apache.poi.ss.usermodel.Sheet sheet, String vendedor) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            if (vendedor.equals(sheet.getRow(i).getCell(0).getStringCellValue())) return i;
        }
        throw new AssertionError("Vendedor não encontrado: " + vendedor);
    }

    private static Map<String, Object> linha(String vendedor, LocalDate data, String produtos) {
        Map<String, Object> linha = new LinkedHashMap<>();
        linha.put("Supervisor", "Supervisora");
        linha.put("Vendedor", vendedor);
        linha.put("DataInstalacao", data);
        linha.put("Produtos", produtos);
        for (String campo : List.of("VIRTUA", "CHIP", "PORT", "TV", "SOMA", "Valor")) {
            linha.put(campo, 0);
        }
        return linha;
    }

    private static void criarEntrada(Path arquivo, String vendedor) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            var sheet = wb.createSheet("Analítico");
            Row cabecalho = sheet.createRow(0);
            List<String> colunas = List.of(
                    "Supervisor", "Vendedor", "DataInstalacao", "Produtos",
                    "Contrato", "FormaPagamento", "Valor");
            for (int i = 0; i < colunas.size(); i++) {
                cabecalho.createCell(i).setCellValue(colunas.get(i));
            }
            Row linha = sheet.createRow(1);
            linha.createCell(0).setCellValue("Supervisora");
            linha.createCell(1).setCellValue(vendedor);
            linha.createCell(2).setCellValue("11/11/2026");
            linha.createCell(3).setCellValue("VIRTUA");
            linha.createCell(4).setCellValue("123");
            linha.createCell(5).setCellValue("DÉBITO");
            linha.createCell(6).setCellValue(10);
            try (var out = Files.newOutputStream(arquivo)) {
                wb.write(out);
            }
        }
    }
}
