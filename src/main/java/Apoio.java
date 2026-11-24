/** Perfil que revisará solicitações de outros usuários e autorizará reativações. */
public class Apoio extends Usuario {
    public void aprovarSolicitacao(SolicitacaoAlteracao solicitacao) {
        throw new UnsupportedOperationException("Implementação pendente");
    }

    public void recusarSolicitacao(SolicitacaoAlteracao solicitacao, String motivo) {
        throw new UnsupportedOperationException("Implementação pendente");
    }

    public void reativarVenda(GrupoCancelamento grupo) {
        throw new UnsupportedOperationException("Implementação pendente");
    }

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
