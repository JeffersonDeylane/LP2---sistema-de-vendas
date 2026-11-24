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
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ProcessamentoPlanilhasTest {
    @Rule
    public final TemporaryFolder pastaTemporaria = new TemporaryFolder();

    @Test
    public void resumoOrdenaVendedoresDentroDoSupervisorEGeraSoPlanilha1() throws Exception {
        TabelaDados dados = new TabelaDados(List.of(
                "Supervisor", "Vendedor", "VIRTUA", "CHIP", "PORT", "TV", "SOMA", "Valor"));
        dados.adicionarLinha(Map.of("Supervisor", "Bruno", "Vendedor", "Ana", "Valor", 200));
        dados.adicionarLinha(Map.of("Supervisor", "Amanda", "Vendedor", "Zélia", "Valor", 100));
        dados.adicionarLinha(Map.of("Supervisor", "Amanda", "Vendedor", "Álvaro", "Valor", 10));

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            new GeradorRelatorios().criar(wb, dados);

            var resumo = wb.getSheet("Planilha1");
            assertEquals("Álvaro", resumo.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Zélia", resumo.getRow(2).getCell(1).getStringCellValue());
            assertEquals("Bruno", resumo.getRow(3).getCell(0).getStringCellValue());
            assertEquals(10, resumo.getRow(1).getCell(7).getNumericCellValue(), 0.001);
            assertNull(wb.getSheet("Planilha2"));
        }
    }

    @Test
    public void listaTvPreservaOrdemDuplicatasEContratosDaBase() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            var tv = wb.createSheet(" TV ");
            tv.createRow(0).createCell(0).setCellValue("DT");
            tv.createRow(1).createCell(0).setCellValue("100");
            tv.createRow(2).createCell(2).setCellValue("outra coluna");

            TabelaDados analitico = new TabelaDados(List.of("TV", "Contrato"));
            analitico.adicionarLinha(Map.of("TV", 1, "Contrato", "002.0"));
            analitico.adicionarLinha(Map.of("TV", 0, "Contrato", "999"));
            analitico.adicionarLinha(Map.of("TV", 1, "Contrato", " "));
            analitico.adicionarLinha(Map.of("TV", 1, "Contrato", 123));
            analitico.adicionarLinha(Map.of("TV", 1, "Contrato", "002"));

            new CruzadorContratos().listarTvAnalitico(wb, analitico);

            assertEquals("tv_analitico", tv.getRow(0).getCell(3).getStringCellValue());
            assertEquals(4, tv.getRow(0).getLastCellNum());
            assertEquals("100", tv.getRow(1).getCell(0).getStringCellValue());
            assertEquals((short) 0, tv.getRow(1).getCell(0).getCellStyle().getIndex());
            assertEquals("002", tv.getRow(1).getCell(3).getStringCellValue());
            assertEquals("123", tv.getRow(2).getCell(3).getStringCellValue());
            assertEquals("002", tv.getRow(3).getCell(3).getStringCellValue());
            assertNull(tv.getRow(3).getCell(0));
        }
    }

    @Test
    public void reclassificaEmpresasPeloSupervisorComPrecedenciaDoPython() {
        TabelaDados dados = new TabelaDados(List.of("Supervisor", "Produtos", "Contrato", "Empresa"));
        List<String> supervisores = List.of(
                " Regional Pedro Melo ", "raissa silva", "Sr. Anderson", "ZAIRO JUNIOR",
                "EDVALDO SANTOS", "Pedro Melo e Raissa e Anderson", "Outro");
        for (String supervisor : supervisores) {
            dados.adicionarLinha(Map.of("Supervisor", supervisor, "Produtos", "TV",
                    "Contrato", "01", "Empresa", "ORIGINAL"));
        }
        new TratadorAnalitico().tratar(dados, "VNA.xlsx");

        assertEquals(List.of("AL", "PB", "PE", "PE", "PE", "PE", "VNA"),
                dados.linhas().stream().map(linha -> linha.get("Empresa")).toList());
    }

    @Test
    public void removeOsNovosPrefixosChipNaOrdemDoPython() {
        assertEquals("001", NormalizadorContrato.normalizarChip("9070682002914480094700001"));
        assertEquals("005", NormalizadorContrato.normalizarChip("94700005.0"));
        assertEquals("005", NormalizadorContrato.normalizarChip("005"));
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
