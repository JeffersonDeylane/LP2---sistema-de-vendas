package lp2.processamento;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PlanilhasIOSegurancaTest {

    @Rule
    public final TemporaryFolder pastaTemporaria = new TemporaryFolder();

    private Path arquivo;
    private Path backup;
    private byte[] baseAnterior;
    private byte[] backupAnterior;

    @Before
    public void prepararBase() throws Exception {
        arquivo = pastaTemporaria.getRoot().toPath().resolve("base.xlsx");
        backup = arquivo.resolveSibling("base.xlsx.bak");
        try (Workbook planilha = novaPlanilha()) {
            PlanilhasIO.salvar(planilha, arquivo);
        }
        Files.copy(arquivo, backup);
        baseAnterior = Files.readAllBytes(arquivo);
        backupAnterior = Files.readAllBytes(backup);
    }

    @Test
    public void falhaDuranteEscritaParcialPreservaOriginaisELimpaTemporario() throws Exception {
        IOException causa = new IOException("Falha de escrita simulada após produzir bytes.");
        AtomicBoolean validou = new AtomicBoolean();
        try (Workbook original = novaPlanilha()) {
            Workbook planilha = simularEscrita(original, new byte[] {1, 2, 3}, causa);
            IOException erro = assertThrows(IOException.class, () -> PlanilhasIO.salvarComBackup(
                planilha, arquivo, baseAnterior, verificacao -> validou.set(true)));
            assertSame(causa, erro);
        }
        assertFalse(validou.get());
        verificarOriginaisETemporarios();
    }

    @Test
    public void temporarioComBytesInvalidosNaoSubstituiBaseNemBackup() throws Exception {
        try (Workbook original = novaPlanilha()) {
            Workbook planilha = simularEscrita(original, new byte[] {1, 2, 3}, null);
            assertThrows(Exception.class, () -> PlanilhasIO.salvarComBackup(planilha, arquivo));
        }
        verificarOriginaisETemporarios();
    }

    @Test
    public void xlsxTruncadoNaoSubstituiBaseNemBackup() throws Exception {
        byte[] truncado = java.util.Arrays.copyOf(baseAnterior, baseAnterior.length / 2);
        try (Workbook original = novaPlanilha()) {
            Workbook planilha = simularEscrita(original, truncado, null);
            assertThrows(Exception.class, () -> PlanilhasIO.salvarComBackup(planilha, arquivo));
        }
        verificarOriginaisETemporarios();
    }

    @Test
    public void planilhaTemporariaSemAbasERecusada() throws Exception {
        try (Workbook planilha = new XSSFWorkbook()) {
            assertThrows(IOException.class, () -> PlanilhasIO.salvarComBackup(planilha, arquivo));
        }
        verificarOriginaisETemporarios();
    }

    @Test
    public void falhaNaValidacaoEspecificaOcorreAntesDeAtualizarBackup() throws Exception {
        IllegalStateException causa = new IllegalStateException("Conteúdo rejeitado pelo validador.");
        try (Workbook planilha = novaPlanilha()) {
            IllegalStateException erro = assertThrows(IllegalStateException.class, () -> PlanilhasIO.salvarComBackup(
                planilha, arquivo, baseAnterior, verificacao -> { throw causa; }));
            assertSame(causa, erro);
        }
        verificarOriginaisETemporarios();
    }

    @Test
    public void criacaoDoBackupFalhaAoAbrirDestinoIncompativel() throws Exception {
        Path destino = pastaTemporaria.newFolder().toPath();
        Files.writeString(destino.resolve("conteudo.txt"), "preservado");
        assertThrows(IOException.class, () -> PlanilhasIO.prepararBackup(arquivo, destino, baseAnterior));
        assertEquals("preservado", Files.readString(destino.resolve("conteudo.txt")));
        verificarOriginaisETemporarios();
    }

    @Test
    public void backupCopiadoDivergenteDaVersaoLidaERecusado() throws Exception {
        Path temporario = Files.createTempFile(pastaTemporaria.getRoot().toPath(), "backup-", ".tmp");
        byte[] outraVersao = baseAnterior.clone();
        outraVersao[outraVersao.length - 1] ^= 1;
        try {
            IOException erro = assertThrows(IOException.class,
                () -> PlanilhasIO.prepararBackup(arquivo, temporario, outraVersao));
            assertTrue(erro.getMessage().contains("alterado externamente"));
            assertArrayEquals(baseAnterior, Files.readAllBytes(temporario));
        } finally {
            Files.delete(temporario);
        }
        verificarOriginaisETemporarios();
    }

    @Test
    public void fonteCorrompidaNaoSobrescreveBackupValido() throws Exception {
        byte[] corrompido = new byte[] {1, 2, 3};
        Files.write(arquivo, corrompido);
        try (Workbook planilha = novaPlanilha()) {
            assertThrows(Exception.class, () -> PlanilhasIO.salvarComBackup(planilha, arquivo));
        }
        assertArrayEquals(corrompido, Files.readAllBytes(arquivo));
        assertArrayEquals(backupAnterior, Files.readAllBytes(backup));
        verificarTemporarios();
    }

    @Test
    public void falhaNaSubstituicaoDoBackupImpedeTrocaDaBase() throws Exception {
        Files.delete(backup);
        Files.createDirectory(backup);
        Files.writeString(backup.resolve("conteudo.txt"), "preservado");
        try (Workbook planilha = novaPlanilha()) {
            assertThrows(IOException.class, () -> PlanilhasIO.salvarComBackup(planilha, arquivo));
        }
        assertArrayEquals(baseAnterior, Files.readAllBytes(arquivo));
        assertEquals("preservado", Files.readString(backup.resolve("conteudo.txt")));
        verificarTemporarios();
    }

    @Test
    public void falhaNaTrocaAtomicaPreservaOrigemEDestino() throws Exception {
        Path destino = pastaTemporaria.newFolder().toPath();
        Files.writeString(destino.resolve("conteudo.txt"), "preservado");
        assertThrows(IOException.class, () -> PlanilhasIO.substituirAtomicamente(arquivo, destino));
        assertArrayEquals(baseAnterior, Files.readAllBytes(arquivo));
        assertEquals("preservado", Files.readString(destino.resolve("conteudo.txt")));
        assertArrayEquals(backupAnterior, Files.readAllBytes(backup));
    }

    @Test
    public void atomicMoveIndisponivelEntreProvedoresNaoUsaAlternativaNaoAtomica() throws Exception {
        Path zip = pastaTemporaria.getRoot().toPath().resolve("outro-sistema.zip");
        try (var sistema = FileSystems.newFileSystem(zip, Map.of("create", "true"))) {
            Path destino = sistema.getPath("/base.xlsx");
            Files.write(destino, backupAnterior);
            AtomicMoveNotSupportedException erro = assertThrows(AtomicMoveNotSupportedException.class,
                () -> PlanilhasIO.substituirAtomicamente(arquivo, destino));
            assertNotNull(erro.getCause());
            assertTrue(erro.getMessage().contains("Substituição atômica indisponível"));
            assertArrayEquals(baseAnterior, Files.readAllBytes(arquivo));
            assertArrayEquals(backupAnterior, Files.readAllBytes(destino));
        }
        verificarOriginaisETemporarios();
    }

    @Test
    public void alteracaoExternaComMesmoTamanhoEDataNaoEIgnorada() throws Exception {
        byte[] externa = baseAnterior.clone();
        externa[externa.length - 1] ^= 1;
        var dataAnterior = Files.getLastModifiedTime(arquivo);
        try (Workbook planilha = novaPlanilha()) {
            IOException erro = assertThrows(IOException.class, () -> PlanilhasIO.salvarComBackup(
                planilha, arquivo, baseAnterior, verificacao -> {
                    try {
                        Files.write(arquivo, externa);
                        Files.setLastModifiedTime(arquivo, dataAnterior);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }));
            assertTrue(erro.getMessage().contains("alterado externamente"));
        }
        assertEquals(dataAnterior, Files.getLastModifiedTime(arquivo));
        assertEquals(baseAnterior.length, Files.size(arquivo));
        assertArrayEquals(externa, Files.readAllBytes(arquivo));
        assertArrayEquals(backupAnterior, Files.readAllBytes(backup));
        verificarTemporarios();
    }

    @Test
    public void primeiraGravacaoNaoSobrescreveArquivoCriadoDepoisDaLeitura() throws Exception {
        try (Workbook planilha = novaPlanilha()) {
            assertThrows(IOException.class, () -> PlanilhasIO.salvarComBackup(
                planilha, arquivo, null, verificacao -> { }));
        }
        verificarOriginaisETemporarios();
    }

    @Test
    public void limpezaNaoOcultaFalhaOriginalEInformaTemporarioQueNaoPodeRemover() throws Exception {
        IllegalStateException causa = new IllegalStateException("Falha original de validação.");
        try (Workbook planilha = novaPlanilha()) {
            IllegalStateException erro = assertThrows(IllegalStateException.class, () -> PlanilhasIO.salvarComBackup(
                planilha, arquivo, baseAnterior, verificacao -> {
                    try (var arquivos = Files.list(arquivo.getParent())) {
                        Path temporario = arquivos.filter(caminho -> caminho.toString().endsWith(".tmp"))
                            .findFirst().orElseThrow();
                        Files.delete(temporario);
                        Files.createDirectory(temporario);
                        Files.writeString(temporario.resolve("bloqueio.txt"), "teste");
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    throw causa;
                }));
            assertSame(causa, erro);
            assertEquals(1, erro.getSuppressed().length);
            assertTrue(erro.getSuppressed()[0] instanceof IOException);
        }
        assertArrayEquals(baseAnterior, Files.readAllBytes(arquivo));
        assertArrayEquals(backupAnterior, Files.readAllBytes(backup));
    }

    private Workbook novaPlanilha() {
        Workbook planilha = new XSSFWorkbook();
        planilha.createSheet("DADOS").createRow(0).createCell(0).setCellValue("dados fictícios");
        return planilha;
    }

    private Workbook simularEscrita(Workbook original, byte[] conteudo, IOException falha) {
        return (Workbook) Proxy.newProxyInstance(Workbook.class.getClassLoader(), new Class<?>[] {Workbook.class},
            (objeto, metodo, argumentos) -> {
                if (metodo.getName().equals("write")) {
                    ((OutputStream) argumentos[0]).write(conteudo);
                    if (falha != null) throw falha;
                    return null;
                }
                try {
                    return metodo.invoke(original, argumentos);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            });
    }

    private void verificarOriginaisETemporarios() throws Exception {
        assertArrayEquals(baseAnterior, Files.readAllBytes(arquivo));
        assertArrayEquals(backupAnterior, Files.readAllBytes(backup));
        verificarTemporarios();
    }

    private void verificarTemporarios() throws Exception {
        try (var arquivos = Files.list(arquivo.getParent())) {
            assertFalse(arquivos.anyMatch(caminho -> caminho.toString().endsWith(".tmp")));
        }
    }
}
