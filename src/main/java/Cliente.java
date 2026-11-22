public final class Cliente {
    private final int id;
    private final String identificacao;
    private final String nome;
    private final boolean ativo;

    public Cliente(int id, String identificacao, String nome, boolean ativo) {
        this.id = Validador.inteiroPositivo(id, "ID do cliente");
        this.identificacao = Validador.textoObrigatorio(identificacao, "Identificação do cliente");
        this.nome = Validador.textoObrigatorio(nome, "Nome do cliente");
        this.ativo = ativo;
    }

    public int getId() {
        return id;
    }

    public String getIdentificacao() {
        return identificacao;
    }

    public String getNome() {
        return nome;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
