public final class PersistenciaUsuariosException extends RuntimeException {

    public PersistenciaUsuariosException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaUsuariosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
