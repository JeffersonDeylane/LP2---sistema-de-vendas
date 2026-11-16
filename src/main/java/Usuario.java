
public abstract class Usuario {

    // Atributos
    private final int id; 
    //id não muda
    private String nome;
    private String login;
    private String senhaHash;
    private boolean ativo;

    // Construtor
    public Usuario(int id, String nome, String login,
                   String senhaHash, boolean ativo) {

        this.id = Validador.inteiroPositivo(id, "ID");
        alterarNome(nome);
        alterarLogin(login);
        definirSenhaHash(senhaHash);
        this.ativo = ativo;
    }

    // PEGADORES
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLogin() {
        return login;
    }

    public boolean isAtivo() {
        return ativo;
    }

    

    // Métodos para alterar os dados
    public void alterarNome(String novoNome) {
        this.nome = Validador.textoObrigatorio(
            novoNome, "Nome"
        );
    }

    public void alterarLogin(String novoLogin) {
        this.login = Validador.textoObrigatorio(
            novoLogin, "Login"
        );
    }

    // Métodos relacionados à senha
    public String getSenhaHash() {
        return senhaHash;
    }

    public void definirSenhaHash(String novoHash) {
        this.senhaHash = Validador.textoObrigatorio(
            novoHash, "Hash da senha"
        );
    }

    // Métodos relacionados ao estado da conta
    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    // Consultas de capacidade; os serviços ainda devem validar autenticação e escopo.
    public boolean podeSolicitarAlteracao() {
        return isAtivo();
    }

    public abstract boolean podeAprovarAlteracoes();

    public final boolean podeAprovarAlteracoes(Usuario solicitante) {
        return solicitante != null
            && id != solicitante.getId()
            && podeAprovarAlteracoes();
    }

    public abstract boolean podeGerenciarUsuarios();

    public boolean podeGerenciarCatalogo() {
        return false;
    }
}
