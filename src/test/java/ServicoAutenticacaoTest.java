import static org.junit.Assert.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ServicoAutenticacaoTest {
    @Rule public TemporaryFolder pasta = new TemporaryFolder();

    @Test public void cadastroSobreviveAReaberturaSemSalvarSenhaEmTexto() throws Exception {
        Path arquivo = pasta.getRoot().toPath().resolve("base.xlsx");
        ServicoAutenticacao servico = new ServicoAutenticacao(new RepositorioUsuariosExcel(arquivo));
        Vendedor cadastrado = servico.cadastrar("Teste", " vendedor ", " minha senha ", "V01");
        Usuario recuperado = new ServicoAutenticacao(new RepositorioUsuariosExcel(arquivo))
            .autenticar(" VENDEDOR ", " minha senha ");
        assertEquals(cadastrado.getId(), recuperado.getId());
        assertTrue(recuperado instanceof Vendedor);
        assertNotEquals(" minha senha ", recuperado.getSenhaHash());
        assertFalse(Senhas.verificar("minha senha", recuperado.getSenhaHash()));
    }

    @Test public void hashesUsamSaltERecusamValoresInvalidos() {
        String primeiro = Senhas.gerarHash("senha");
        assertNotEquals(primeiro, Senhas.gerarHash("senha"));
        assertTrue(Senhas.verificar("senha", primeiro));
        assertFalse(Senhas.verificar("errada", primeiro));
        assertFalse(Senhas.verificar("senha", "hash123"));
        assertFalse(Senhas.verificar("senha", "pbkdf2-sha256$600000$???$???"));
    }

    @Test public void rejeitaDuplicataSenhaVaziaContaInativaECredenciaisErradas() throws Exception {
        Path arquivo = pasta.getRoot().toPath().resolve("base.xlsx");
        RepositorioUsuariosExcel repositorio = new RepositorioUsuariosExcel(arquivo);
        ServicoAutenticacao servico = new ServicoAutenticacao(repositorio);
        Vendedor usuario = servico.cadastrar("Teste", "teste", "senha", "V01");
        byte[] antes = Files.readAllBytes(arquivo);
        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar("Outro", " TESTE ", "senha", "V02"));
        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar("Outro", "outro", "  ", "V02"));
        assertArrayEquals(antes, Files.readAllBytes(arquivo));
        assertThrows(IllegalArgumentException.class, () -> servico.autenticar("teste", "errada"));
        assertThrows(IllegalArgumentException.class, () -> servico.autenticar("ausente", "senha"));
        usuario.desativar();
        repositorio.salvar(usuario);
        assertThrows(IllegalArgumentException.class, () -> servico.autenticar("teste", "senha"));
    }

    @Test public void menuCadastraAutenticaEFazLogoutSemProcessarPlanilhas() throws Exception {
        Path arquivo = pasta.getRoot().toPath().resolve("base.xlsx");
        InputStream entradaAnterior = System.in;
        PrintStream saidaAnterior = System.out;
        ByteArrayOutputStream captura = new ByteArrayOutputStream();
        try (PrintStream saida = new PrintStream(captura, true, StandardCharsets.UTF_8)) {
            String entradas = "3\n4\nTeste\nteste\nsenha\nV01\n1\nteste\nerrada\n1\nteste\nsenha\n5\n3\n0\n";
            System.setIn(new ByteArrayInputStream(entradas.getBytes(StandardCharsets.UTF_8)));
            System.setOut(saida);
            Main.main(new String[] {arquivo.toString()});
        } finally {
            System.setIn(entradaAnterior);
            System.setOut(saidaAnterior);
        }
        String texto = captura.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("Cadastro salvo!"));
        assertTrue(texto.contains("Login ou senha inválidos."));
        assertTrue(texto.contains("Bem-vindo, Teste!"));
        assertTrue(texto.contains("Você saiu da conta."));
        assertTrue(texto.indexOf("Faça login") != texto.lastIndexOf("Faça login"));
        assertFalse(texto.contains("Iniciando tratamento"));
    }
}
