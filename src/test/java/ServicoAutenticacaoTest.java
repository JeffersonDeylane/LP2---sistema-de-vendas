import static org.junit.Assert.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ServicoAutenticacaoTest {
    @Rule public TemporaryFolder pasta = new TemporaryFolder();

    @Test public void autenticaAdministradorPersistidoNaBase() throws Exception {
        Path arquivo = pasta.getRoot().toPath().resolve("base.xlsx");
        RepositorioUsuariosExcel repositorio = new RepositorioUsuariosExcel(arquivo);
        guardarAdminPadrao(repositorio);
        Usuario recuperado = new ServicoAutenticacao(new RepositorioUsuariosExcel(arquivo))
            .autenticar(" admin ", "admin");
        assertTrue(recuperado instanceof Administrador);
        assertNotEquals("admin", recuperado.getSenhaHash());
    }

    @Test public void adminCadastraPerfisERecuperaDadosAposReabrir() throws Exception {
        Path arquivo = pasta.getRoot().toPath().resolve("base.xlsx");
        RepositorioUsuariosExcel repositorio = new RepositorioUsuariosExcel(arquivo);
        ServicoAutenticacao servico = new ServicoAutenticacao(repositorio);
        Usuario admin = guardarAdminPadrao(repositorio);

        Usuario vendedor = servico.cadastrar(admin, "VENDEDOR",
            "Vendedor teste", " vendedor ", " minha senha ", "V01");
        Usuario apoio = servico.cadastrar(admin, "APOIO",
            "Apoio teste", "apoio", "senha apoio", "");
        Usuario outroAdmin = servico.cadastrar(admin, "ADMINISTRADOR",
            "Admin teste", "admin.teste", "senha admin", "");
        ServicoAutenticacao reaberto = new ServicoAutenticacao(new RepositorioUsuariosExcel(arquivo));

        Usuario vendedorRecuperado = reaberto.autenticar(" VENDEDOR ", " minha senha ");
        assertEquals(vendedor.getId(), vendedorRecuperado.getId());
        assertTrue(vendedorRecuperado instanceof Vendedor);
        assertEquals("V01", ((Vendedor) vendedorRecuperado).getMatricula());
        assertTrue(reaberto.autenticar("apoio", "senha apoio") instanceof Apoio);
        assertEquals(outroAdmin.getId(), reaberto.autenticar("admin.teste", "senha admin").getId());
        assertTrue(apoio.getSenhaHash().startsWith("pbkdf2-sha256$"));
        assertNotEquals(" minha senha ", vendedorRecuperado.getSenhaHash());
        assertFalse(Senhas.verificar("minha senha", vendedorRecuperado.getSenhaHash()));
    }

    @Test public void somenteAdminAtivoPodeCadastrarEPerfilSemMatricula() throws Exception {
        Path arquivo = pasta.getRoot().toPath().resolve("base.xlsx");
        RepositorioUsuariosExcel repositorio = new RepositorioUsuariosExcel(arquivo);
        ServicoAutenticacao servico = new ServicoAutenticacao(repositorio);
        Usuario admin = guardarAdminPadrao(repositorio);
        Usuario vendedor = new Vendedor(2, "Vendedor", "vendedor", "hash", true, "V02");
        Usuario apoio = new Apoio(3, "Apoio", "apoio", "hash", true);
        Usuario adminInativo = new Administrador(4, "Admin", "inativo", "hash", false);

        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(
            vendedor, "APOIO", "Novo apoio", "novo.apoio", "senha", ""));
        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(
            apoio, "APOIO", "Novo apoio", "novo.apoio", "senha", ""));
        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(
            adminInativo, "APOIO", "Novo apoio", "novo.apoio", "senha", ""));
        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(
            null, "APOIO", "Novo apoio", "novo.apoio", "senha", ""));
        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(
            admin, "APOIO", "Apoio teste", "apoio.teste", "senha", "V03"));
        assertEquals(1, repositorio.listar().size());
    }

    @Test public void rejeitaDuplicataSenhaVaziaContaInativaECredenciaisErradas() throws Exception {
        Path arquivo = pasta.getRoot().toPath().resolve("base.xlsx");
        RepositorioUsuariosExcel repositorio = new RepositorioUsuariosExcel(arquivo);
        ServicoAutenticacao servico = new ServicoAutenticacao(repositorio);
        Usuario admin = guardarAdminPadrao(repositorio);
        Vendedor usuario = (Vendedor) servico.cadastrar(admin,
            "VENDEDOR", "Teste", "teste", "senha", "V01");
        byte[] antes = Files.readAllBytes(arquivo);
        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(
            admin, "VENDEDOR", "Outro", " TESTE ", "senha", "V02"));
        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(
            admin, "VENDEDOR", "Outro", "outro", "  ", "V02"));
        assertArrayEquals(antes, Files.readAllBytes(arquivo));
        assertThrows(IllegalArgumentException.class, () -> servico.autenticar("teste", "errada"));
        assertThrows(IllegalArgumentException.class, () -> servico.autenticar("ausente", "senha"));
        usuario.desativar();
        repositorio.salvar(usuario);
        assertThrows(IllegalArgumentException.class, () -> servico.autenticar("teste", "senha"));
    }

    @Test public void menuExibeCadastroSomenteParaAdminEAcessoProtegido() throws Exception {
        Path arquivo = pasta.getRoot().toPath().resolve("base.xlsx");
        RepositorioUsuariosExcel repositorio = new RepositorioUsuariosExcel(arquivo);
        guardarAdminPadrao(repositorio);
        InputStream entradaAnterior = System.in;
        PrintStream saidaAnterior = System.out;
        ByteArrayOutputStream captura = new ByteArrayOutputStream();
        try (PrintStream saida = new PrintStream(captura, true, StandardCharsets.UTF_8)) {
            String entradas = "1\nadmin\nadmin\n4\n1\nTeste\nteste\nsenha\nV01\n5\n"
                + "1\nteste\nsenha\n4\n5\n1\nteste\nerrada\n5\n3\n0\n";
            System.setIn(new ByteArrayInputStream(entradas.getBytes(StandardCharsets.UTF_8)));
            System.setOut(saida);
            Main.main(new String[] {arquivo.toString()});
        } finally {
            System.setIn(entradaAnterior);
            System.setOut(saidaAnterior);
        }
        String texto = captura.toString(StandardCharsets.UTF_8);
        assertFalse(texto.contains("Admin inicial criado"));
        assertTrue(texto.contains("Cadastro salvo!"));
        assertTrue(texto.contains("Bem-vindo, Teste!"));
        assertTrue(texto.contains("Apenas um administrador pode cadastrar usuários."));
        assertTrue(texto.contains("Login ou senha inválidos."));
        assertTrue(texto.contains("Você saiu da conta."));
        int conectadoVendedor = texto.indexOf("Bem-vindo, Teste!");
        int menuVendedor = texto.indexOf("===== MENU PRINCIPAL =====", conectadoVendedor);
        int escolhaVendedor = texto.indexOf("Escolha uma opção:", menuVendedor);
        assertFalse(texto.substring(menuVendedor, escolhaVendedor).contains("4 - Cadastrar usuário"));
        assertFalse(texto.contains("Iniciando tratamento"));
    }

    private Usuario guardarAdminPadrao(RepositorioUsuariosExcel repositorio) {
        Usuario admin = new Administrador(repositorio.proximoId(), "Administrador",
            "admin", Senhas.gerarHash("admin"), true);
        repositorio.salvar(admin);
        return admin;
    }

    @Test public void hashesUsamSaltERecusamValoresInvalidos() {
        String primeiro = Senhas.gerarHash("senha");
        assertNotEquals(primeiro, Senhas.gerarHash("senha"));
        assertTrue(Senhas.verificar("senha", primeiro));
        assertFalse(Senhas.verificar("errada", primeiro));
        assertFalse(Senhas.verificar("senha", "hash123"));
        assertFalse(Senhas.verificar("senha", "pbkdf2-sha256$600000$???$???"));
    }

}
