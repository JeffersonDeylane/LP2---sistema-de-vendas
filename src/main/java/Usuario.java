
public abstract class Usuario {

    // Atributos
    private int id;
    private String nome;
    private String login;
    private String senha;
    private boolean ativo;

    // Construtor
    public Usuario(int id, String nome, String login,
                   String senha, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.ativo = ativo;
    }

    // Getters
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

    // Setters
    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    // Métodos do usuário
    public boolean verificarSenha(String senha) {
        return this.senha.equals(senha);
    }

    public void alterarSenha(String senhaAtual, String novaSenha) {
        if (verificarSenha(senhaAtual)) {
            setSenha(novaSenha);
        }
    }

    // Métodos abstratos
    public abstract String getPerfil();
    public abstract void exibirMenu();
}
