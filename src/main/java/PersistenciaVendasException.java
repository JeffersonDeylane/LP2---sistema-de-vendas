public final class PersistenciaVendasException extends RuntimeException {
    public PersistenciaVendasException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaVendasException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
