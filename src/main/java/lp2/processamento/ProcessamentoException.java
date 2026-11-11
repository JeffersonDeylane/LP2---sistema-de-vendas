package lp2.processamento;

/** Exceção específica do processamento de planilhas. */
public final class ProcessamentoException extends RuntimeException {
    public ProcessamentoException(String mensagem) { super(mensagem); }
    public ProcessamentoException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
