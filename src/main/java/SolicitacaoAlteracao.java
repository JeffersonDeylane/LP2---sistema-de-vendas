/** Registra uma alteração proposta em um campo do contrato para revisão humana. */
public class SolicitacaoAlteracao {
    private String id;
    private Contrato contrato;
    private final Usuario solicitante;
    private String campo;
    private String valorProposto;
    private StatusSolicitacao status;

    public SolicitacaoAlteracao(Usuario solicitante) {
        if (solicitante == null) {
            throw new IllegalArgumentException("Solicitante obrigatório.");
        }
        this.solicitante = solicitante;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }
}
