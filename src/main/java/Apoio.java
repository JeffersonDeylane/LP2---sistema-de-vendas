public class Apoio extends Usuario {

    public Apoio(int id, String nome, String login,
                 String senhaHash, boolean ativo) {
        super(id, nome, login, senhaHash, ativo);
    }

    @Override
    public boolean podeAprovarAlteracoes() {
        return isAtivo();
    }

    @Override
    public boolean podeGerenciarUsuarios() {
        return false;
    }
}
