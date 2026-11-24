import java.nio.file.Path;

/** Coordenará a leitura de Excel ou CSV e a revisão por campo das divergências. */
public final class ServicoImportacao {
    private final ServicoAutorizacao autorizacao;

    public ServicoImportacao(RepositorioUsuariosExcel usuarios) {
        this.autorizacao = new ServicoAutorizacao(usuarios);
    }

    public LoteImportacao importar(Path arquivo, Usuario responsavel) {
        autorizacao.exigirContaAtiva(responsavel);
        throw new UnsupportedOperationException("Implementação pendente");
    }

    public DecisaoAlteracao aprovar(SolicitacaoAlteracao solicitacao, Usuario revisor) {
        exigirRevisaoAutorizada(solicitacao, revisor);
        throw new UnsupportedOperationException("Implementação pendente");
    }

    public DecisaoAlteracao recusar(SolicitacaoAlteracao solicitacao, Usuario revisor, String motivo) {
        exigirRevisaoAutorizada(solicitacao, revisor);
        throw new UnsupportedOperationException("Implementação pendente");
    }

    private void exigirRevisaoAutorizada(SolicitacaoAlteracao solicitacao, Usuario revisor) {
        if (solicitacao == null) {
            throw new IllegalArgumentException("Solicitação obrigatória.");
        }
        autorizacao.exigirRevisor(revisor, solicitacao.getSolicitante());
    }
}
