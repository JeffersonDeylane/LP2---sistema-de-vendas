import java.util.List;
import java.util.Objects;

/** Cadastro local de vendedores e autenticação, sem gestão de permissões. */
public final class ServicoAutenticacao {
    private final RepositorioUsuariosExcel repositorio;

    public ServicoAutenticacao(RepositorioUsuariosExcel repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    public Vendedor cadastrar(String nome, String login, String senha, String matricula) {
        String loginValidado = Validador.textoObrigatorio(login, "Login");
        List<Usuario> usuarios = repositorio.listar();
        if (usuarios.stream().anyMatch(u -> u.getLogin().equalsIgnoreCase(loginValidado))) {
            throw new IllegalArgumentException("Este login já está cadastrado.");
        }
        Vendedor vendedor = new Vendedor(repositorio.proximoId(), nome, loginValidado,
            Senhas.gerarHash(senha), true, matricula);
        repositorio.salvar(vendedor);
        return vendedor;
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
}
