public final class Contrato {
    private final int id;
    private final String numero;
    private final int clienteId;
    private final boolean ativo;

    public Contrato(int id, String numero, int clienteId, boolean ativo) {
        this.id = Validador.inteiroPositivo(id, "ID do contrato");
        this.numero = Validador.textoObrigatorio(numero, "Número do contrato");
        this.clienteId = Validador.inteiroPositivo(clienteId, "ID do cliente do contrato");
        this.ativo = ativo;
    }

    public int getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public int getClienteId() {
        return clienteId;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
