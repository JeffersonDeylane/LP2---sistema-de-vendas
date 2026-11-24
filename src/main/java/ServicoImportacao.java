import java.nio.file.Path;

/** Coordenará a leitura de Excel ou CSV e a revisão por campo das divergências. */
public final class ServicoImportacao {
    public LoteImportacao importar(Path arquivo, Usuario responsavel) {
        throw new UnsupportedOperationException("Implementação pendente");
    }

    public DecisaoAlteracao aprovar(SolicitacaoAlteracao solicitacao, Usuario revisor) {
        throw new UnsupportedOperationException("Implementação pendente");
    }

    public DecisaoAlteracao recusar(SolicitacaoAlteracao solicitacao, Usuario revisor, String motivo) {
        throw new UnsupportedOperationException("Implementação pendente");
    }
}
