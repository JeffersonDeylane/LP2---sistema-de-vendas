/** Coordenará as regras de cancelamento em cascata e reativação autorizada. */
public final class ServicoCancelamento {
    private final ServicoAutorizacao autorizacao;

    public ServicoCancelamento(RepositorioUsuariosExcel usuarios) {
        this.autorizacao = new ServicoAutorizacao(usuarios);
    }

    public GrupoCancelamento cancelar(Contrato contrato, ItemVenda item, Usuario responsavel, String motivo) {
        autorizacao.exigirResponsavelPorCancelamentoOuReativacao(responsavel);
        throw new UnsupportedOperationException("Implementação pendente");
    }

    public void reativar(GrupoCancelamento grupo, Usuario responsavel) {
        autorizacao.exigirResponsavelPorCancelamentoOuReativacao(responsavel);
        throw new UnsupportedOperationException("Implementação pendente");
    }
}
