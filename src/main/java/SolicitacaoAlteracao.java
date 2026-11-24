/** Registra uma alteração proposta em um campo do contrato para revisão humana. */
public class SolicitacaoAlteracao {
    private String id;
    private Contrato contrato;
    private Usuario solicitante;
    private String campo;
    private String valorProposto;
    private StatusSolicitacao status;
}
