/** Perfil com capacidades administrativas de gestão de usuários e do catálogo. */
public class Administrador extends Usuario {
    public void gerenciarUsuarios() {
        throw new UnsupportedOperationException("Implementação pendente");
    }

    public void gerarRelatorio() {
        throw new UnsupportedOperationException("Implementação pendente");
    }

    public Administrador(int id, String nome, String login,
                         String senhaHash, boolean ativo) {
        super(id, nome, login, senhaHash, ativo);
    }

    @Override
    public boolean podeAprovarAlteracoes() {
        return isAtivo();
    }

    @Override
    public boolean podeGerenciarUsuarios() {
        return isAtivo();
    }

    @Override
    public boolean podeGerenciarCatalogo() {
        return isAtivo();
    }
}
