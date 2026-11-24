import java.util.List;
import java.util.Objects;

/** Manutenção administrativa das contas persistidas. */
public final class ServicoUsuarios {
    private final RepositorioUsuariosExcel repositorio;
    private final ServicoAutorizacao autorizacao;

    public ServicoUsuarios(RepositorioUsuariosExcel repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.autorizacao = new ServicoAutorizacao(repositorio);
    }

    public List<Usuario> listar(Usuario responsavel) {
        exigirAdministrador(responsavel);
        return repositorio.listar();
    }

    public Usuario alterar(Usuario responsavel, int usuarioId, String nome, String login) {
        synchronized (RepositorioUsuariosExcel.BLOQUEIO_ESCRITA) {
            List<Usuario> usuarios = listar(responsavel);
            Usuario usuario = buscar(usuarios, usuarioId);
            String loginValidado = Validador.textoObrigatorio(login, "Login");
            if (usuarios.stream().anyMatch(outro -> outro.getId() != usuarioId
                    && outro.getLogin().equalsIgnoreCase(loginValidado))) {
                throw new IllegalArgumentException("Este login já está cadastrado.");
            }
            usuario.alterarNome(nome);
            usuario.alterarLogin(loginValidado);
            repositorio.salvar(usuario);
            return usuario;
        }
    }

    public Usuario alterarSituacao(Usuario responsavel, int usuarioId, boolean ativo) {
        synchronized (RepositorioUsuariosExcel.BLOQUEIO_ESCRITA) {
            List<Usuario> usuarios = listar(responsavel);
            Usuario usuario = buscar(usuarios, usuarioId);
            if (usuario.isAtivo() == ativo) return usuario;
            if (!ativo && usuario instanceof Administrador
                    && usuarios.stream().noneMatch(outro -> outro.getId() != usuarioId
                        && outro instanceof Administrador && outro.isAtivo())) {
                throw new IllegalArgumentException("O último administrador ativo não pode ser desativado.");
            }
            if (ativo) {
                usuario.ativar();
            } else {
                usuario.desativar();
            }
            repositorio.salvar(usuario);
            return usuario;
        }
    }

    void exigirAdministrador(Usuario responsavel) {
        if (!autorizacao.exigirContaAtiva(responsavel).podeGerenciarUsuarios()) {
            throw new IllegalArgumentException("Apenas um administrador ativo pode gerenciar usuários.");
        }
    }

    private Usuario buscar(List<Usuario> usuarios, int usuarioId) {
        return usuarios.stream().filter(usuario -> usuario.getId() == usuarioId)
            .findFirst().orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }
}
