import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Cadastro local de usuários e autenticação. */
public final class ServicoAutenticacao {
    private final RepositorioUsuariosExcel repositorio;
    private final ServicoAutorizacao autorizacao;

    public ServicoAutenticacao(RepositorioUsuariosExcel repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.autorizacao = new ServicoAutorizacao(repositorio);
    }

    public Usuario cadastrar(Usuario solicitante, String perfil, String nome,
                            String login, String senha, String matricula) {
        if (!autorizacao.exigirContaAtiva(solicitante).podeGerenciarUsuarios()) {
            throw new IllegalArgumentException("Apenas um administrador ativo pode cadastrar usuários.");
        }
        String loginValidado = Validador.textoObrigatorio(login, "Login");
        List<Usuario> usuarios = repositorio.listar();
        if (usuarios.stream().anyMatch(u -> u.getLogin().equalsIgnoreCase(loginValidado))) {
            throw new IllegalArgumentException("Este login já está cadastrado.");
        }
        String perfilValidado = Validador.textoObrigatorio(perfil, "Perfil").toUpperCase(Locale.ROOT);
        String hash = Senhas.gerarHash(senha);
        Usuario novoUsuario = switch (perfilValidado) {
            case "VENDEDOR" -> new Vendedor(repositorio.proximoId(), nome, loginValidado,
                hash, true, matricula);
            case "APOIO" -> {
                exigirMatriculaVazia(matricula);
                yield new Apoio(repositorio.proximoId(), nome, loginValidado, hash, true);
            }
            case "ADMINISTRADOR" -> {
                exigirMatriculaVazia(matricula);
                yield new Administrador(repositorio.proximoId(), nome, loginValidado, hash, true);
            }
            default -> throw new IllegalArgumentException("Perfil inválido.");
        };
        repositorio.salvar(novoUsuario);
        return novoUsuario;
    }

    public Usuario autenticar(String login, String senha) {
        String procurado = login == null ? "" : login.trim();
        Usuario usuario = repositorio.listar().stream()
            .filter(u -> u.getLogin().equalsIgnoreCase(procurado))
            .findFirst().orElse(null);
        if (usuario == null || !usuario.isAtivo()
                || !Senhas.verificar(senha, usuario.getSenhaHash())) {
            throw new IllegalArgumentException("Login ou senha inválidos.");
        }
        return usuario;
    }

    private void exigirMatriculaVazia(String matricula) {
        if (matricula != null && !matricula.isBlank()) {
            throw new IllegalArgumentException("Matrícula só se aplica ao perfil vendedor.");
        }
    }
}
