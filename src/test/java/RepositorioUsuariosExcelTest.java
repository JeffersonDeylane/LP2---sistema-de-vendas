import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Consumer;
import lp2.processamento.PlanilhasIO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class RepositorioUsuariosExcelTest {

    private static final String HASH_FICTICIO = "$pbkdf2-sha256$600000$c2FsdC10ZXN0ZQ==$aGFzaC10ZXN0ZQ==";

    @Rule
    public final TemporaryFolder pastaTemporaria = new TemporaryFolder();

    private Path arquivo;
    private RepositorioUsuariosExcel repositorio;

    @Before
    public void prepararRepositorio() throws Exception {
        arquivo = pastaTemporaria.newFolder().toPath().resolve("BASE_GERAL.xlsx");
        repositorio = new RepositorioUsuariosExcel(arquivo);
    }

    @Test
    public void ausenciaInicialRetornaListaVaziaSemCriarArquivo() {
        assertTrue(repositorio.listar().isEmpty());
        assertEquals(1, repositorio.proximoId());
        assertFalse(Files.exists(arquivo));
        assertFalse(Files.exists(backup()));
    }

    @Test
    public void persisteVendedorComIdMatriculaHashESituacao() {
        Usuario vendedor = vendedor(42);
        repositorio.salvar(vendedor);

        List<Usuario> recuperados = new RepositorioUsuariosExcel(arquivo).listar();
        assertEquals(1, recuperados.size());
        verificarUsuario(vendedor, recuperados.get(0));
        assertEquals("V042", ((Vendedor) recuperados.get(0)).getMatricula());
    }

    @Test
    public void persisteApoioAtivoComComportamentoPolimorfico() {
        Usuario apoio = new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true);
        repositorio.salvar(apoio);

        Usuario recuperado = new RepositorioUsuariosExcel(arquivo).listar().get(0);
        verificarUsuario(apoio, recuperado);
        assertTrue(recuperado.podeAprovarAlteracoes(vendedor(42)));
        assertFalse(recuperado.podeGerenciarUsuarios());
        assertFalse(recuperado.podeGerenciarCatalogo());
        assertFalse(recuperado.podeAprovarAlteracoes(apoio));
    }

    @Test
    public void persisteAdministradorInativoSemLiberarCapacidades() {
        Usuario administrador = new Administrador(105, "Admin fictício", "adm.teste", HASH_FICTICIO, false);
        repositorio.salvar(administrador);

        Usuario recuperado = new RepositorioUsuariosExcel(arquivo).listar().get(0);
        verificarUsuario(administrador, recuperado);
        assertFalse(recuperado.podeSolicitarAlteracao());
        assertFalse(recuperado.podeAprovarAlteracoes());
        assertFalse(recuperado.podeGerenciarUsuarios());
        assertFalse(recuperado.podeGerenciarCatalogo());
        recuperado.ativar();
        assertTrue(recuperado.podeGerenciarCatalogo());
    }

    @Test
    public void atualizacaoPreservaOutrosUsuariosSemDuplicarId() {
        Usuario vendedor = vendedor(42);
        Usuario apoio = new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true);
        Usuario administrador = new Administrador(105, "Admin fictício", "adm.teste", HASH_FICTICIO, false);
        repositorio.salvar(vendedor);
        repositorio.salvar(apoio);
        repositorio.salvar(administrador);

        vendedor.alterarNome("Vendedor alterado");
        vendedor.alterarLogin("vendedor.alterado");
        vendedor.definirSenhaHash(HASH_FICTICIO + "-alterado");
        vendedor.desativar();
        ((Vendedor) vendedor).setMatricula("V043");
        repositorio.salvar(vendedor);
        repositorio.salvar(vendedor);

        List<Usuario> recuperados = new RepositorioUsuariosExcel(arquivo).listar();
        assertEquals(3, recuperados.size());
        verificarUsuario(vendedor, buscar(recuperados, 42));
        assertEquals("V043", ((Vendedor) buscar(recuperados, 42)).getMatricula());
        verificarUsuario(apoio, buscar(recuperados, 73));
        verificarUsuario(administrador, buscar(recuperados, 105));
    }

    @Test
    public void repositoriosDiferentesReleemBaseAntesDeSalvar() {
        RepositorioUsuariosExcel outro = new RepositorioUsuariosExcel(arquivo);
        assertTrue(outro.listar().isEmpty());
        repositorio.salvar(vendedor(42));
        outro.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true));
        assertEquals(2, repositorio.listar().size());
    }

    @Test
    public void proximoIdConsideraMaiorIdInclusiveInativoDepoisDoReinicio() {
        repositorio.salvar(new Administrador(105, "Admin fictício", "adm.teste", HASH_FICTICIO, false));
        repositorio.salvar(vendedor(42));
        RepositorioUsuariosExcel reaberto = new RepositorioUsuariosExcel(arquivo);
        assertEquals(106, reaberto.proximoId());
        reaberto.salvar(new Apoio(reaberto.proximoId(), "Apoio fictício", "apoio.teste", HASH_FICTICIO, true));
        assertEquals(107, new RepositorioUsuariosExcel(arquivo).proximoId());
    }

    @Test
    public void esgotamentoDeIdsNaoProvocaRetornoAoInicio() {
        repositorio.salvar(vendedor(Integer.MAX_VALUE));
        assertThrows(PersistenciaUsuariosException.class, () -> repositorio.proximoId());
    }

    @Test
    public void primeiraGravacaoCriaPastasECabecalhoComTiposExplicitos() throws Exception {
        arquivo = arquivo.getParent().resolve("nova/pasta/BASE_GERAL.xlsx");
        repositorio = new RepositorioUsuariosExcel(arquivo);
        repositorio.salvar(vendedor(42));

        try (InputStream entrada = Files.newInputStream(arquivo);
             Workbook planilha = new XSSFWorkbook(entrada)) {
            var aba = planilha.getSheet("USUARIOS");
            String[] colunas = {"ID", "NOME", "LOGIN", "SENHA_HASH", "PERFIL", "ATIVO", "MATRICULA"};
            for (int indice = 0; indice < colunas.length; indice++) {
                assertEquals(colunas[indice], aba.getRow(0).getCell(indice).getStringCellValue());
            }
            assertEquals(CellType.NUMERIC, aba.getRow(1).getCell(0).getCellType());
            assertEquals("VENDEDOR", aba.getRow(1).getCell(4).getStringCellValue());
            assertEquals(CellType.BOOLEAN, aba.getRow(1).getCell(5).getCellType());
            assertEquals(HASH_FICTICIO, aba.getRow(1).getCell(3).getStringCellValue());
        }
        assertFalse(Files.exists(backup()));
    }

    @Test
    public void gravacaoPreservaOutrasAbasEFormulas() throws Exception {
        repositorio.salvar(vendedor(42));
        alterarPlanilha(planilha -> {
            var outraAba = planilha.createSheet("OUTRA_ABA");
            outraAba.createRow(0).createCell(0).setCellValue("conteúdo preservado");
            outraAba.getRow(0).createCell(1).setCellFormula("USUARIOS!A2+1");
        });
        repositorio.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true));

        try (InputStream entrada = Files.newInputStream(arquivo);
             Workbook planilha = new XSSFWorkbook(entrada)) {
            var outraAba = planilha.getSheet("OUTRA_ABA");
            assertNotNull(outraAba);
            assertEquals("conteúdo preservado", outraAba.getRow(0).getCell(0).getStringCellValue());
            assertEquals("USUARIOS!A2+1", outraAba.getRow(0).getCell(1).getCellFormula());
        }
    }

    @Test
    public void atualizacaoPreservaPosicaoEDefinicoesDaAbaUsuarios() throws Exception {
        Usuario vendedor = vendedor(42);
        repositorio.salvar(vendedor);
        alterarPlanilha(planilha -> {
            planilha.createSheet("OUTRA_ABA");
            planilha.setSheetOrder("OUTRA_ABA", 0);
            var nome = planilha.createName();
            nome.setSheetIndex(1);
            nome.setNameName("IDENTIFICADORES");
            nome.setRefersToFormula("USUARIOS!$A$2");
        });
        vendedor.alterarNome("Nome alterado");
        repositorio.salvar(vendedor);

        try (InputStream entrada = Files.newInputStream(arquivo);
             Workbook planilha = new XSSFWorkbook(entrada)) {
            assertEquals(1, planilha.getSheetIndex("USUARIOS"));
            assertNotNull(planilha.getName("IDENTIFICADORES"));
            assertEquals(1, planilha.getName("IDENTIFICADORES").getSheetIndex());
            assertEquals("USUARIOS!$A$2", planilha.getName("IDENTIFICADORES").getRefersToFormula());
        }
    }

    @Test
    public void backupMantemBytesDaVersaoAnteriorEPermiteReconstrucao() throws Exception {
        Usuario vendedor = vendedor(42);
        repositorio.salvar(vendedor);
        byte[] primeiraVersao = Files.readAllBytes(arquivo);
        repositorio.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true));
        assertArrayEquals(primeiraVersao, Files.readAllBytes(backup()));
        verificarUsuario(vendedor, new RepositorioUsuariosExcel(backup()).listar().get(0));

        byte[] segundaVersao = Files.readAllBytes(arquivo);
        vendedor.alterarNome("Nome alterado");
        repositorio.salvar(vendedor);
        assertArrayEquals(segundaVersao, Files.readAllBytes(backup()));
        assertEquals(2, new RepositorioUsuariosExcel(backup()).listar().size());
        verificarAusenciaDeTemporarios();
    }

    @Test
    public void arquivoCorrompidoNaoEVistoComoBaseVaziaNemSobrescrito() throws Exception {
        Files.writeString(arquivo, "arquivo inválido de teste");
        byte[] anterior = Files.readAllBytes(arquivo);
        PersistenciaUsuariosException erro = assertThrows(PersistenciaUsuariosException.class, () -> repositorio.listar());
        assertNotNull(erro.getCause());
        assertTrue(erro.getMessage().contains(arquivo.toString()));
        assertThrows(PersistenciaUsuariosException.class, () -> repositorio.salvar(vendedor(42)));
        assertArrayEquals(anterior, Files.readAllBytes(arquivo));
        assertFalse(Files.exists(backup()));
    }

    @Test
    public void arquivoVazioERecusado() throws Exception {
        Files.createFile(arquivo);
        assertThrows(PersistenciaUsuariosException.class, () -> repositorio.listar());
        assertEquals(0, Files.size(arquivo));
    }

    @Test
    public void ausenciaDaAbaNaoUsaOutraAbaComoUsuarios() throws Exception {
        try (Workbook planilha = new XSSFWorkbook()) {
            planilha.createSheet("OUTRA_ABA").createRow(0).createCell(0).setCellValue("ID");
            PlanilhasIO.salvar(planilha, arquivo);
        }
        verificarBaseInvalida("aba USUARIOS ausente");
    }

    @Test
    public void cabecalhoIncompativelImpedeLeituraEGravacao() throws Exception {
        repositorio.salvar(vendedor(42));
        alterarPlanilha(planilha -> planilha.getSheet("USUARIOS").getRow(0).getCell(3).setCellValue("SENHA"));
        verificarBaseInvalida("SENHA_HASH");
    }

    @Test
    public void perfilDesconhecidoIdentificaLinhaSemDescartarUsuario() throws Exception {
        repositorio.salvar(vendedor(42));
        alterarPlanilha(planilha -> planilha.getSheet("USUARIOS").getRow(1).getCell(4).setCellValue("OUTRO"));
        verificarBaseInvalida("linha 2: perfil desconhecido");
    }

    @Test
    public void registroSemNomeERecusadoSemExporHash() throws Exception {
        repositorio.salvar(vendedor(42));
        alterarPlanilha(planilha -> planilha.getSheet("USUARIOS").getRow(1).getCell(1).setBlank());
        verificarBaseInvalida("Nome");
        PersistenciaUsuariosException erro = assertThrows(PersistenciaUsuariosException.class, () -> repositorio.listar());
        assertFalse(erro.getMessage().contains(HASH_FICTICIO));
    }

    @Test
    public void matriculaObrigatoriaParaVendedor() throws Exception {
        repositorio.salvar(vendedor(42));
        alterarPlanilha(planilha -> planilha.getSheet("USUARIOS").getRow(1).getCell(6).setBlank());
        verificarBaseInvalida("Matrícula");
    }

    @Test
    public void matriculaDeOutroPerfilNaoEDescartadaSilenciosamente() throws Exception {
        repositorio.salvar(vendedor(42));
        alterarPlanilha(planilha -> planilha.getSheet("USUARIOS").getRow(1).getCell(4).setCellValue("APOIO"));
        verificarBaseInvalida("matrícula só se aplica");
    }

    @Test
    public void idFracionarioOuForaDoIntervaloERecusado() throws Exception {
        for (double id : new double[] {0, -1, 1.5, (double) Integer.MAX_VALUE + 1}) {
            prepararRepositorio();
            repositorio.salvar(vendedor(42));
            alterarPlanilha(planilha -> planilha.getSheet("USUARIOS").getRow(1).getCell(0).setCellValue(id));
            verificarBaseInvalida("ID fora do intervalo");
        }
    }

    @Test
    public void idDuplicadoNaBaseERecusadoSemPerderRegistros() throws Exception {
        repositorio.salvar(vendedor(42));
        repositorio.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true));
        alterarPlanilha(planilha -> planilha.getSheet("USUARIOS").getRow(2).getCell(0).setCellValue(42));
        verificarBaseInvalida("ID duplicado");
    }

    @Test
    public void ativoTextualNaoEConvertidoSilenciosamenteEmFalso() throws Exception {
        repositorio.salvar(vendedor(42));
        alterarPlanilha(planilha -> {
            Cell celula = planilha.getSheet("USUARIOS").getRow(1).getCell(5);
            celula.setBlank();
            celula.setCellValue("inválido");
        });
        verificarBaseInvalida("ATIVO deve ser booleano");
    }

    @Test
    public void formulasEmCamposDeUsuariosSaoRecusadas() throws Exception {
        repositorio.salvar(vendedor(42));
        alterarPlanilha(planilha -> planilha.getSheet("USUARIOS").getRow(1).getCell(2).setCellFormula("\"login\""));
        verificarBaseInvalida("LOGIN deve ser texto, sem fórmula");
    }

    @Test
    public void linhasVaziasSaoIgnoradasMasColunasExtrasSaoRecusadas() throws Exception {
        repositorio.salvar(vendedor(42));
        alterarPlanilha(planilha -> planilha.getSheet("USUARIOS").createRow(3));
        assertEquals(1, repositorio.listar().size());
        alterarPlanilha(planilha -> planilha.getSheet("USUARIOS").getRow(3).createCell(8).setCellValue("dado inesperado"));
        verificarBaseInvalida("linha 4: colunas extras");
    }

    @Test
    public void loginsDuplicadosSaoRecusadosSemModificarBase() throws Exception {
        repositorio.salvar(vendedor(42));
        byte[] anterior = Files.readAllBytes(arquivo);
        assertThrows(PersistenciaUsuariosException.class,
            () -> repositorio.salvar(new Apoio(73, "Apoio fictício", "VENDEDOR.42", HASH_FICTICIO, true)));
        assertArrayEquals(anterior, Files.readAllBytes(arquivo));
        assertFalse(Files.exists(backup()));

        repositorio.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true));
        alterarPlanilha(planilha -> planilha.getSheet("USUARIOS").getRow(2).getCell(2).setCellValue("VENDEDOR.42"));
        verificarBaseInvalida("Login duplicado");
    }

    @Test
    public void perfilNaoSuportadoENuloNaoCriamBase() {
        assertThrows(IllegalArgumentException.class, () -> repositorio.salvar(null));
        Usuario outroPerfil = new Apoio(73, "Teste", "teste", HASH_FICTICIO, true) { };
        assertThrows(IllegalArgumentException.class, () -> repositorio.salvar(outroPerfil));
        assertFalse(Files.exists(arquivo));
    }

    @Test
    public void falhaNoBackupPreservaBaseAnteriorELimpaTemporarios() throws Exception {
        repositorio.salvar(vendedor(42));
        byte[] anterior = Files.readAllBytes(arquivo);
        Files.createDirectory(backup());
        Files.writeString(backup().resolve("impedimento.txt"), "teste");

        PersistenciaUsuariosException erro = assertThrows(PersistenciaUsuariosException.class,
            () -> repositorio.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true)));

        assertNotNull(erro.getCause());
        assertArrayEquals(anterior, Files.readAllBytes(arquivo));
        assertEquals(1, repositorio.listar().size());
        verificarAusenciaDeTemporarios();
    }

    @Test
    public void caminhoPaiIncompativelGeraFalhaDeLeituraEGravacao() throws Exception {
        Path impedimento = pastaTemporaria.newFile().toPath();
        RepositorioUsuariosExcel invalido = new RepositorioUsuariosExcel(impedimento.resolve("BASE_GERAL.xlsx"));
        assertThrows(PersistenciaUsuariosException.class, () -> invalido.listar());
        assertThrows(PersistenciaUsuariosException.class, () -> invalido.salvar(vendedor(42)));
        assertTrue(Files.isRegularFile(impedimento));
    }

    @Test
    public void baseSomenteLeituraPreservaArquivoEBackupNoWindows() throws Exception {
        assumeTrue(System.getProperty("os.name").startsWith("Windows"));
        repositorio.salvar(vendedor(42));
        byte[] anterior = Files.readAllBytes(arquivo);
        Files.setAttribute(arquivo, "dos:readonly", true);
        try {
            assertThrows(PersistenciaUsuariosException.class,
                () -> repositorio.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true)));
            assertArrayEquals(anterior, Files.readAllBytes(arquivo));
            assertArrayEquals(anterior, Files.readAllBytes(backup()));
            assertEquals(1, repositorio.listar().size());
            verificarAusenciaDeTemporarios();
        } finally {
            Files.setAttribute(arquivo, "dos:readonly", false);
        }
    }

    @Test
    public void arquivoBloqueadoInformaFalhaEPreservaBytesNoWindows() throws Exception {
        assumeTrue(System.getProperty("os.name").startsWith("Windows"));
        repositorio.salvar(vendedor(42));
        byte[] anterior = Files.readAllBytes(arquivo);
        try (FileChannel canal = FileChannel.open(arquivo, StandardOpenOption.READ, StandardOpenOption.WRITE);
             var bloqueio = canal.lock()) {
            assertTrue(bloqueio.isValid());
            assertThrows(PersistenciaUsuariosException.class, () -> repositorio.listar());
            assertThrows(PersistenciaUsuariosException.class,
                () -> repositorio.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true)));
        }
        assertArrayEquals(anterior, Files.readAllBytes(arquivo));
        assertFalse(Files.exists(backup()));
        assertEquals(1, repositorio.listar().size());
    }

    @Test
    public void falhaDeSerializacaoNaoAlteraBaseNemBackupAnterior() throws Exception {
        repositorio.salvar(vendedor(42));
        repositorio.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true));
        byte[] baseAnterior = Files.readAllBytes(arquivo);
        byte[] backupAnterior = Files.readAllBytes(backup());
        Workbook planilhaFechada = new XSSFWorkbook();
        planilhaFechada.createSheet("USUARIOS");
        planilhaFechada.close();

        assertThrows(IOException.class, () -> PlanilhasIO.salvarComBackup(planilhaFechada, arquivo));

        assertArrayEquals(baseAnterior, Files.readAllBytes(arquivo));
        assertArrayEquals(backupAnterior, Files.readAllBytes(backup()));
        verificarAusenciaDeTemporarios();
    }

    @Test
    public void temporarioComAtributosDivergentesNaoSubstituiBaseOuBackup() throws Exception {
        repositorio.salvar(vendedor(42));
        byte[] anterior = Files.readAllBytes(arquivo);
        List<Usuario> esperados = repositorio.listar();
        for (int coluna = 0; coluna < 7; coluna++) {
            try (InputStream entrada = Files.newInputStream(arquivo);
                 Workbook candidato = new XSSFWorkbook(entrada)) {
                var linha = candidato.getSheet("USUARIOS").getRow(1);
                switch (coluna) {
                    case 0 -> linha.getCell(0).setCellValue(43);
                    case 1 -> linha.getCell(1).setCellValue("Outro nome");
                    case 2 -> linha.getCell(2).setCellValue("outro.login");
                    case 3 -> linha.getCell(3).setCellValue(HASH_FICTICIO + "-outro");
                    case 4 -> {
                        linha.getCell(4).setCellValue("APOIO");
                        linha.getCell(6).setBlank();
                    }
                    case 5 -> linha.getCell(5).setCellValue(false);
                    case 6 -> linha.getCell(6).setCellValue("V999");
                }
                PersistenciaUsuariosException erro = assertThrows(PersistenciaUsuariosException.class,
                    () -> PlanilhasIO.salvarComBackup(candidato, arquivo, anterior,
                        verificacao -> repositorio.validarPersistencia(verificacao, esperados)));
                assertTrue(erro.getMessage().contains("Dados de usuários divergentes"));
                assertFalse(erro.getMessage().contains(HASH_FICTICIO));
            }
            assertArrayEquals(anterior, Files.readAllBytes(arquivo));
            assertFalse(Files.exists(backup()));
            verificarAusenciaDeTemporarios();
        }
    }

    @Test
    public void temporarioSemTodosOsRegistrosNaoSubstituiBase() throws Exception {
        repositorio.salvar(vendedor(42));
        byte[] anterior = Files.readAllBytes(arquivo);
        List<Usuario> esperados = repositorio.listar();
        try (InputStream entrada = Files.newInputStream(arquivo);
             Workbook candidato = new XSSFWorkbook(entrada)) {
            var aba = candidato.getSheet("USUARIOS");
            aba.removeRow(aba.getRow(1));
            assertThrows(PersistenciaUsuariosException.class,
                () -> PlanilhasIO.salvarComBackup(candidato, arquivo, anterior,
                    verificacao -> repositorio.validarPersistencia(verificacao, esperados)));
        }
        assertArrayEquals(anterior, Files.readAllBytes(arquivo));
        assertFalse(Files.exists(backup()));
        verificarAusenciaDeTemporarios();
    }

    @Test
    public void temporarioLegivelSemAbaUsuariosNaoSubstituiBase() throws Exception {
        repositorio.salvar(vendedor(42));
        byte[] anterior = Files.readAllBytes(arquivo);
        List<Usuario> esperados = repositorio.listar();
        try (Workbook candidato = new XSSFWorkbook()) {
            candidato.createSheet("OUTRA_ABA");
            assertThrows(PersistenciaUsuariosException.class,
                () -> PlanilhasIO.salvarComBackup(candidato, arquivo, anterior,
                    verificacao -> repositorio.validarPersistencia(verificacao, esperados)));
        }
        assertArrayEquals(anterior, Files.readAllBytes(arquivo));
        assertFalse(Files.exists(backup()));
        verificarAusenciaDeTemporarios();
    }

    @Test
    public void baseExternaValidaModificadaDuranteValidacaoNaoESobrescrita() throws Exception {
        repositorio.salvar(vendedor(42));
        repositorio.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true));
        byte[] anterior = Files.readAllBytes(arquivo);
        byte[] backupAnterior = Files.readAllBytes(backup());
        List<Usuario> esperados = repositorio.listar();
        Path outraBase = pastaTemporaria.newFolder().toPath().resolve("externa.xlsx");
        new RepositorioUsuariosExcel(outraBase).salvar(vendedor(999));
        byte[] externa = Files.readAllBytes(outraBase);
        try (InputStream entrada = Files.newInputStream(arquivo);
             Workbook candidato = new XSSFWorkbook(entrada)) {
            IOException erro = assertThrows(IOException.class,
                () -> PlanilhasIO.salvarComBackup(candidato, arquivo, anterior, verificacao -> {
                    repositorio.validarPersistencia(verificacao, esperados);
                    try {
                        Files.write(arquivo, externa);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }));
            assertTrue(erro.getMessage().contains("alterado externamente"));
        }
        assertArrayEquals(externa, Files.readAllBytes(arquivo));
        assertArrayEquals(backupAnterior, Files.readAllBytes(backup()));
        assertEquals(999, repositorio.listar().get(0).getId());
        verificarAusenciaDeTemporarios();
    }

    @Test
    public void baseAusenteComBackupValidoExigeRecuperacaoManual() throws Exception {
        repositorio.salvar(vendedor(42));
        repositorio.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true));
        byte[] backupAnterior = Files.readAllBytes(backup());
        Files.delete(arquivo);
        RepositorioUsuariosExcel reaberto = new RepositorioUsuariosExcel(arquivo);
        PersistenciaUsuariosException erro = assertThrows(PersistenciaUsuariosException.class, () -> reaberto.listar());
        assertTrue(erro.getMessage().contains("Backup validado"));
        assertTrue(erro.getMessage().contains("recuperação manual"));
        assertThrows(PersistenciaUsuariosException.class, () -> reaberto.proximoId());
        assertThrows(PersistenciaUsuariosException.class, () -> reaberto.salvar(vendedor(105)));
        assertFalse(Files.exists(arquivo));
        assertArrayEquals(backupAnterior, Files.readAllBytes(backup()));
    }

    @Test
    public void baseCorrompidaComBackupValidoNaoRestauraAutomaticamenteVersaoAntiga() throws Exception {
        repositorio.salvar(vendedor(42));
        repositorio.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true));
        byte[] backupAnterior = Files.readAllBytes(backup());
        Files.writeString(arquivo, "base corrompida de teste");
        byte[] corrompida = Files.readAllBytes(arquivo);
        PersistenciaUsuariosException erro = assertThrows(PersistenciaUsuariosException.class, () -> repositorio.listar());
        assertTrue(erro.getMessage().contains("Backup validado"));
        assertThrows(PersistenciaUsuariosException.class, () -> repositorio.salvar(vendedor(105)));
        assertArrayEquals(corrompida, Files.readAllBytes(arquivo));
        assertArrayEquals(backupAnterior, Files.readAllBytes(backup()));
        verificarUsuario(vendedor(42), new RepositorioUsuariosExcel(backup()).listar().get(0));
    }

    @Test
    public void backupSemEsquemaDeUsuariosNaoESugeridoComoRecuperacaoValida() throws Exception {
        repositorio.salvar(vendedor(42));
        try (Workbook invalido = new XSSFWorkbook()) {
            invalido.createSheet("OUTRA_ABA");
            PlanilhasIO.salvar(invalido, backup());
        }
        byte[] backupInvalido = Files.readAllBytes(backup());
        Files.delete(arquivo);
        PersistenciaUsuariosException erro = assertThrows(PersistenciaUsuariosException.class, () -> repositorio.listar());
        assertTrue(erro.getMessage().contains("backup em"));
        assertTrue(erro.getMessage().contains("inválido ou indisponível"));
        assertFalse(erro.getMessage().contains("Backup validado"));
        assertEquals(1, erro.getSuppressed().length);
        assertFalse(Files.exists(arquivo));
        assertArrayEquals(backupInvalido, Files.readAllBytes(backup()));
    }

    @Test
    public void backupCorrompidoNuncaSubstituiBaseValida() throws Exception {
        repositorio.salvar(vendedor(42));
        byte[] anterior = Files.readAllBytes(arquivo);
        Files.writeString(backup(), "backup corrompido de teste");
        assertEquals(1, repositorio.listar().size());
        assertArrayEquals(anterior, Files.readAllBytes(arquivo));
        repositorio.salvar(new Apoio(73, "Apoio fictício", "apoio.teste", HASH_FICTICIO, true));
        assertArrayEquals(anterior, Files.readAllBytes(backup()));
        assertEquals(2, repositorio.listar().size());
    }

    @Test
    public void desaparecimentoDeBaseJaConhecidaNaoReiniciaUsuariosMesmoSemBackup() throws Exception {
        repositorio.salvar(vendedor(42));
        Files.delete(arquivo);
        assertThrows(PersistenciaUsuariosException.class, () -> repositorio.listar());
        assertThrows(PersistenciaUsuariosException.class, () -> repositorio.salvar(vendedor(105)));
        assertFalse(Files.exists(arquivo));
    }

    @Test
    public void temporarioDeGravacaoAnteriorImpedeInicializacaoVazia() throws Exception {
        Path temporario = arquivo.resolveSibling("." + arquivo.getFileName() + "-interrompido.tmp");
        Files.writeString(temporario, "gravação interrompida de teste");
        assertThrows(PersistenciaUsuariosException.class, () -> repositorio.listar());
        assertThrows(PersistenciaUsuariosException.class, () -> repositorio.proximoId());
        assertThrows(PersistenciaUsuariosException.class, () -> repositorio.salvar(vendedor(42)));
        assertFalse(Files.exists(arquivo));
        assertEquals("gravação interrompida de teste", Files.readString(temporario));
    }

    @Test
    public void inicializacaoCarregaBaseConfiguradaSemPersistirDemonstracao() throws Exception {
        repositorio.salvar(vendedor(42));
        byte[] anterior = Files.readAllBytes(arquivo);
        String saida = executarMain();
        assertTrue(saida.contains("Usuários persistidos carregados: 1"));
        assertTrue(saida.contains("Sistema de gestão de vendas"));
        assertTrue(saida.contains("Encerrando o sistema"));
        assertArrayEquals(anterior, Files.readAllBytes(arquivo));
        assertFalse(saida.contains(HASH_FICTICIO));
        assertFalse(Files.exists(backup()));
    }

    @Test
    public void inicializacaoSemBaseNaoCriaUsuariosAutomaticamente() throws Exception {
        String saida = executarMain();
        assertTrue(saida.contains("Usuários persistidos carregados: 0"));
        assertTrue(saida.contains("Encerrando o sistema"));
        assertFalse(saida.contains("Admin inicial criado"));
        assertFalse(Files.exists(arquivo));
    }

    @Test
    public void inicializacaoComBaseInvalidaInformaErroSemContinuarComExemplos() throws Exception {
        Files.writeString(arquivo, "base inválida de teste");
        byte[] anterior = Files.readAllBytes(arquivo);
        String saida = executarMain();
        assertTrue(saida.contains("Falha ao carregar usuários"));
        assertFalse(saida.contains("MENU PRINCIPAL"));
        assertFalse(saida.contains("Demonstração"));
        assertArrayEquals(anterior, Files.readAllBytes(arquivo));
    }

    private Usuario vendedor(int id) {
        return new Vendedor(id, "Vendedor fictício", "vendedor." + id, HASH_FICTICIO, true, "V042");
    }

    private Path backup() {
        return arquivo.resolveSibling(arquivo.getFileName() + ".bak");
    }

    private Usuario buscar(List<Usuario> usuarios, int id) {
        return usuarios.stream().filter(usuario -> usuario.getId() == id).findFirst().orElseThrow();
    }

    private void verificarUsuario(Usuario esperado, Usuario recuperado) {
        assertEquals(esperado.getClass(), recuperado.getClass());
        assertEquals(esperado.getId(), recuperado.getId());
        assertEquals(esperado.getNome(), recuperado.getNome());
        assertEquals(esperado.getLogin(), recuperado.getLogin());
        assertEquals(esperado.getSenhaHash(), recuperado.getSenhaHash());
        assertEquals(esperado.isAtivo(), recuperado.isAtivo());
    }

    private void alterarPlanilha(Consumer<Workbook> alteracao) throws Exception {
        Workbook planilha;
        try (InputStream entrada = Files.newInputStream(arquivo)) {
            planilha = new XSSFWorkbook(entrada);
        }
        try (planilha) {
            alteracao.accept(planilha);
            try (var saida = Files.newOutputStream(arquivo)) {
                planilha.write(saida);
            }
        }
    }

    private void verificarBaseInvalida(String mensagem) throws Exception {
        byte[] anterior = Files.readAllBytes(arquivo);
        PersistenciaUsuariosException erro = assertThrows(PersistenciaUsuariosException.class, () -> repositorio.listar());
        assertTrue(erro.getMessage(), erro.getMessage().contains(mensagem));
        assertThrows(PersistenciaUsuariosException.class, () -> repositorio.salvar(vendedor(42)));
        assertArrayEquals(anterior, Files.readAllBytes(arquivo));
    }

    private void verificarAusenciaDeTemporarios() throws Exception {
        try (var arquivos = Files.list(arquivo.getParent())) {
            assertFalse(arquivos.anyMatch(caminho -> caminho.getFileName().toString().endsWith(".tmp")));
        }
    }

    private String executarMain() throws Exception {
        InputStream entradaAnterior = System.in;
        PrintStream saidaAnterior = System.out;
        PrintStream erroAnterior = System.err;
        ByteArrayOutputStream captura = new ByteArrayOutputStream();
        try (PrintStream saida = new PrintStream(captura, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream("2\n0\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(saida);
            System.setErr(saida);
            Main.main(new String[] {arquivo.toString()});
        } finally {
            System.setIn(entradaAnterior);
            System.setOut(saidaAnterior);
            System.setErr(erroAnterior);
        }
        return captura.toString(StandardCharsets.UTF_8);
    }
}
