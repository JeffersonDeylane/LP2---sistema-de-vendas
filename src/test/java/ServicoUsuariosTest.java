import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

public class ServicoUsuariosTest {
    @Rule
    public TemporaryFolder pasta = new TemporaryFolder();

    @Test
    public void editaPorIdERejeitaLoginDuplicadoSemAlterarDados() {
        var repositorio = repositorio();
        var admin = admin(1);
        var vendedor = new Vendedor(2, "Vendedor", "venda", "hash", true, "V01");
        repositorio.salvar(admin);
        repositorio.salvar(vendedor);
        var servico = new ServicoUsuarios(repositorio);

        assertThrows(IllegalArgumentException.class,
            () -> servico.alterar(admin, 2, "Outro nome", " ADMIN1 "));
        assertEquals("Vendedor", servico.listar(admin).get(1).getNome());
        assertThrows(IllegalArgumentException.class,
            () -> servico.alterar(vendedor, 2, "Outro nome", "novo"));

        Usuario editado = servico.alterar(admin, 2, "Outro nome", "novo");
        assertEquals(2, editado.getId());
        Vendedor persistido = (Vendedor) servico.listar(admin).get(1);
        assertEquals("Outro nome", persistido.getNome());
        assertEquals("novo", persistido.getLogin());
        assertEquals("hash", persistido.getSenhaHash());
        assertEquals("V01", persistido.getMatricula());
        assertTrue(persistido.isAtivo());
    }

    @Test
    public void protegeUltimoAdministradorEPermiteDesativarAPropriaContaComOutroAtivo() {
        var repositorio = repositorio();
        var primeiro = admin(1);
        var segundo = admin(2);
        repositorio.salvar(primeiro);
        var servico = new ServicoUsuarios(repositorio);

        assertThrows(IllegalArgumentException.class, () -> servico.alterarSituacao(primeiro, 1, false));
        assertTrue(servico.listar(primeiro).get(0).isAtivo());

        repositorio.salvar(segundo);
        assertFalse(servico.alterarSituacao(primeiro, 1, false).isAtivo());
        assertThrows(IllegalArgumentException.class, () -> servico.listar(primeiro));
        assertThrows(IllegalArgumentException.class, () -> servico.alterarSituacao(segundo, 2, false));
        assertTrue(servico.alterarSituacao(segundo, 1, true).isAtivo());
        assertEquals(2, servico.listar(primeiro).size());
    }

    @Test
    public void menuAtualizaSessaoAoEditarLoginEEncerraAoDesativarPropriaConta() {
        Path arquivo = pasta.getRoot().toPath().resolve("base.xlsx");
        var repositorio = new RepositorioUsuariosExcel(arquivo);
        repositorio.salvar(new Administrador(1, "Admin 1", "admin1", Senhas.gerarHash("senha"), true));
        repositorio.salvar(admin(2));

        InputStream entradaAnterior = System.in;
        PrintStream saidaAnterior = System.out;
        var captura = new ByteArrayOutputStream();
        String comandos = "1\nadmin1\nsenha\n13\n1\nNome editado\nnovo\n12\n14\n1\nn\n0\n";
        try (var saida = new PrintStream(captura, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(comandos.getBytes(StandardCharsets.UTF_8)));
            System.setOut(saida);
            Main.main(new String[] {arquivo.toString()});
        } finally {
            System.setIn(entradaAnterior);
            System.setOut(saidaAnterior);
        }

        String texto = captura.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("Usuário atualizado."));
        assertTrue(texto.contains("Nome editado (novo) - Administrador - Ativo"));
        assertTrue(texto.contains("Sessão encerrada."));
        assertFalse(repositorio.listar().get(0).isAtivo());
    }

    private RepositorioUsuariosExcel repositorio() {
        return new RepositorioUsuariosExcel(pasta.getRoot().toPath().resolve("base.xlsx"));
    }

    private Administrador admin(int id) {
        return new Administrador(id, "Admin " + id, "admin" + id, "hash", true);
    }
}
