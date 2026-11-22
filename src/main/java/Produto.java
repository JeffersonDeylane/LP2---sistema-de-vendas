import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Produto {
    private final int id;
    private final String codigo;
    private final String nome;
    private final String categoria;
    private final BigDecimal preco;
    private final String unidade;
    private final boolean permiteFracao;
    private final boolean ativo;

    public Produto(int id, String codigo, String nome, String categoria, BigDecimal preco,
                   String unidade, boolean permiteFracao, boolean ativo) {
        this.id = Validador.inteiroPositivo(id, "ID do produto");
        this.codigo = Validador.textoObrigatorio(codigo, "Código do produto");
        this.nome = Validador.textoObrigatorio(nome, "Nome do produto");
        this.categoria = Validador.textoObrigatorio(categoria, "Categoria do produto");
        this.preco = validarPreco(preco);
        this.unidade = unidade == null || unidade.isBlank() ? null : unidade.trim();
        if (permiteFracao && this.unidade == null) {
            throw new IllegalArgumentException("Produto sem unidade deve ser indivisível.");
        }
        this.permiteFracao = permiteFracao;
        this.ativo = ativo;
    }

    private static BigDecimal validarPreco(BigDecimal preco) {
        if (preco == null || preco.signum() <= 0) {
            throw new IllegalArgumentException("O preço do produto deve ser positivo.");
        }
        try {
            return preco.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("O preço deve ter no máximo duas casas decimais.", e);
        }
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public String getUnidade() {
        return unidade;
    }

    public boolean isPermiteFracao() {
        return permiteFracao;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
