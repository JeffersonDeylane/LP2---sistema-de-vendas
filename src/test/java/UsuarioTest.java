import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class UsuarioTest {

    @Test
    public void tresPerfisPreservamDadosComunsValidados() {
        for (Usuario usuario : criarUsuarios(true)) {
            assertEquals(1, usuario.getId());
            assertEquals("Ana Souza", usuario.getNome());
            assertEquals("ana.souza", usuario.getLogin());
            assertEquals("hash-ficticio", usuario.getSenhaHash());
            assertTrue(usuario.isAtivo());
        }
    }

    @Test
    public void vendedorPreservaMatriculaERecusaRevisaoEGestao() {
        Vendedor vendedor = new Vendedor(1, "Ana", "ana", "hash-ficticio", true, " V001 ");
        assertEquals("V001", vendedor.getMatricula());
        vendedor.setMatricula(" V002 ");
        assertEquals("V002", vendedor.getMatricula());

        Usuario usuario = vendedor;
        assertTrue(usuario.podeSolicitarAlteracao());
        assertFalse(usuario.podeAprovarAlteracoes());
        assertFalse(usuario.podeAprovarAlteracoes(outroSolicitante()));
        assertFalse(usuario.podeGerenciarUsuarios());
        assertFalse(usuario.podeGerenciarCatalogo());
    }

    @Test
    public void apoioPodeRevisarOutroUsuarioSemGerenciarUsuariosOuCatalogo() {
        Usuario usuario = new Apoio(1, "Ana", "ana", "hash-ficticio", true);

        assertTrue(usuario.podeSolicitarAlteracao());
        assertTrue(usuario.podeAprovarAlteracoes());
        assertTrue(usuario.podeAprovarAlteracoes(outroSolicitante()));
        assertFalse(usuario.podeGerenciarUsuarios());
        assertFalse(usuario.podeGerenciarCatalogo());
    }

    @Test
    public void administradorPodeRevisarEGerenciarUsuariosECatalogo() {
        Usuario usuario = new Administrador(1, "Ana", "ana", "hash-ficticio", true);

        assertTrue(usuario.podeSolicitarAlteracao());
        assertTrue(usuario.podeAprovarAlteracoes());
        assertTrue(usuario.podeAprovarAlteracoes(outroSolicitante()));
        assertTrue(usuario.podeGerenciarUsuarios());
        assertTrue(usuario.podeGerenciarCatalogo());
    }

    @Test
    public void perfisCriadosInativosNaoPossuemCapacidadesOperacionais() {
        for (Usuario usuario : criarUsuarios(false)) {
            assertFalse(usuario.isAtivo());
            verificarCapacidadesBloqueadas(usuario);
        }
    }

    @Test
    public void desativacaoEReativacaoReavaliamCapacidadesSemAlterarIdentidade() {
        for (Usuario usuario : criarUsuarios(true)) {
            boolean podeAprovar = usuario.podeAprovarAlteracoes();
            boolean podeGerenciarUsuarios = usuario.podeGerenciarUsuarios();
            boolean podeGerenciarCatalogo = usuario.podeGerenciarCatalogo();

            usuario.desativar();
            verificarCapacidadesBloqueadas(usuario);
            assertEquals(1, usuario.getId());
            assertEquals("Ana Souza", usuario.getNome());
            assertEquals("ana.souza", usuario.getLogin());
            assertEquals("hash-ficticio", usuario.getSenhaHash());

            usuario.ativar();
            assertTrue(usuario.isAtivo());
            assertTrue(usuario.podeSolicitarAlteracao());
            assertEquals(podeAprovar, usuario.podeAprovarAlteracoes());
            assertEquals(podeAprovar, usuario.podeAprovarAlteracoes(outroSolicitante()));
            assertEquals(podeGerenciarUsuarios, usuario.podeGerenciarUsuarios());
            assertEquals(podeGerenciarCatalogo, usuario.podeGerenciarCatalogo());
        }
    }

    @Test
    public void nenhumPerfilPodeAprovarAlteracaoPropria() {
        for (Usuario usuario : criarUsuarios(true)) {
            assertFalse(usuario.podeAprovarAlteracoes(usuario));
        }
    }

    @Test
    public void autoaprovacaoUsaIdMesmoEmOutraInstanciaComDadosOuPerfilDiferentes() {
        Usuario solicitante = new Vendedor(1, "Nome anterior", "login.anterior",
            "outro-hash-ficticio", false, "V010");

        for (Usuario usuario : criarUsuarios(true)) {
            assertFalse(usuario.podeAprovarAlteracoes(solicitante));
        }
    }

    @Test
    public void aprovacaoSemSolicitanteERecusada() {
        for (Usuario usuario : criarUsuarios(true)) {
            assertFalse(usuario.podeAprovarAlteracoes(null));
        }
    }

    @Test
    public void inatividadeDoSolicitanteNaoImpedeRevisaoPorOutroUsuarioAtivo() {
        Usuario solicitante = outroSolicitante();
        solicitante.desativar();

        Usuario apoio = new Apoio(1, "Ana", "ana", "hash-ficticio", true);
        Usuario administrador = new Administrador(3, "Maria", "maria", "hash-ficticio", true);
        assertTrue(apoio.podeAprovarAlteracoes(solicitante));
        assertTrue(administrador.podeAprovarAlteracoes(solicitante));
    }

    @Test
    public void construtoresDosTresPerfisRecusamDadosComunsInvalidos() {
        for (int id : new int[] {0, -1}) {
            verificarConstrutoresInvalidos(id, "Ana", "ana", "hash-ficticio");
        }
        for (String texto : new String[] {null, "", "   "}) {
            verificarConstrutoresInvalidos(1, texto, "ana", "hash-ficticio");
            verificarConstrutoresInvalidos(1, "Ana", texto, "hash-ficticio");
            verificarConstrutoresInvalidos(1, "Ana", "ana", texto);
        }
    }

    @Test
    public void alteracoesDosDadosComunsPreservamValidacaoEId() {
        for (Usuario usuario : criarUsuarios(true)) {
            usuario.alterarNome(" Maria Lima ");
            usuario.alterarLogin(" maria.lima ");
            usuario.definirSenhaHash(" novo-hash-ficticio ");

            assertEquals(1, usuario.getId());
            assertEquals("Maria Lima", usuario.getNome());
            assertEquals("maria.lima", usuario.getLogin());
            assertEquals("novo-hash-ficticio", usuario.getSenhaHash());
        }
    }

    @Test
    public void alteracoesInvalidasPreservamDadosAnteriores() {
        for (Usuario usuario : criarUsuarios(true)) {
            for (String texto : new String[] {null, "", "   "}) {
                assertThrows(IllegalArgumentException.class, () -> usuario.alterarNome(texto));
                assertThrows(IllegalArgumentException.class, () -> usuario.alterarLogin(texto));
                assertThrows(IllegalArgumentException.class, () -> usuario.definirSenhaHash(texto));
                assertEquals("Ana Souza", usuario.getNome());
                assertEquals("ana.souza", usuario.getLogin());
                assertEquals("hash-ficticio", usuario.getSenhaHash());
            }
        }
    }

    @Test
    public void matriculaObrigatoriaNaCriacaoENaAlteracaoSemPerdaDoValorAnterior() {
        Vendedor vendedor = new Vendedor(1, "Ana", "ana", "hash-ficticio", true, "V001");

        for (String matricula : new String[] {null, "", "   "}) {
            assertThrows(IllegalArgumentException.class,
                () -> new Vendedor(1, "Ana", "ana", "hash-ficticio", true, matricula));
            assertThrows(IllegalArgumentException.class, () -> vendedor.setMatricula(matricula));
            assertEquals("V001", vendedor.getMatricula());
        }
    }

    private List<Usuario> criarUsuarios(boolean ativo) {
        return List.of(
            new Vendedor(1, " Ana Souza ", " ana.souza ", " hash-ficticio ", ativo, "V001"),
            new Apoio(1, " Ana Souza ", " ana.souza ", " hash-ficticio ", ativo),
            new Administrador(1, " Ana Souza ", " ana.souza ", " hash-ficticio ", ativo)
        );
    }

    private Usuario outroSolicitante() {
        return new Vendedor(2, "João", "joao", "hash-ficticio", true, "V002");
    }

    private void verificarCapacidadesBloqueadas(Usuario usuario) {
        assertFalse(usuario.podeSolicitarAlteracao());
        assertFalse(usuario.podeAprovarAlteracoes());
        assertFalse(usuario.podeAprovarAlteracoes(outroSolicitante()));
        assertFalse(usuario.podeGerenciarUsuarios());
        assertFalse(usuario.podeGerenciarCatalogo());
    }

    private void verificarConstrutoresInvalidos(int id, String nome, String login, String senhaHash) {
        assertThrows(IllegalArgumentException.class,
            () -> new Vendedor(id, nome, login, senhaHash, true, "V001"));
        assertThrows(IllegalArgumentException.class,
            () -> new Apoio(id, nome, login, senhaHash, true));
        assertThrows(IllegalArgumentException.class,
            () -> new Administrador(id, nome, login, senhaHash, true));
    }
}
