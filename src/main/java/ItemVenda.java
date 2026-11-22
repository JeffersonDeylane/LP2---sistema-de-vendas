import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ItemVenda {
    private final int id;
    private final int produtoId;
    private final BigDecimal quantidade;
    private final String codigoProduto;
    private final String nomeProduto;
    private final String categoria;
    private final String unidade;
    private final BigDecimal precoUnitario;
    private final BigDecimal subtotal;

    private ItemVenda(int id, int produtoId, BigDecimal quantidade, String codigoProduto,
                      String nomeProduto, String categoria, String unidade,
                      BigDecimal precoUnitario, BigDecimal subtotal) {
        this.id = Validador.inteiroPositivo(id, "ID do item");
        this.produtoId = Validador.inteiroPositivo(produtoId, "ID do produto");
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new IllegalArgumentException("A quantidade do item deve ser positiva.");
        }
        this.quantidade = quantidade.stripTrailingZeros();
        boolean fotografado = precoUnitario != null;
        if (fotografado != (codigoProduto != null)
                || fotografado != (nomeProduto != null)
                || fotografado != (categoria != null)
                || fotografado != (subtotal != null)) {
            throw new IllegalArgumentException("Fotografia comercial do item incompleta.");
        }
        if (fotografado) {
            if (codigoProduto.isBlank() || nomeProduto.isBlank() || categoria.isBlank()
                    || precoUnitario.signum() <= 0 || subtotal.signum() <= 0) {
                throw new IllegalArgumentException("Fotografia comercial do item inválida.");
            }
            BigDecimal subtotalEsperado = precoUnitario.multiply(this.quantidade)
                .setScale(2, RoundingMode.HALF_UP);
            if (subtotal.compareTo(subtotalEsperado) != 0) {
                throw new IllegalArgumentException("Subtotal do item não corresponde ao preço e à quantidade.");
            }
        } else if (unidade != null) {
            throw new IllegalArgumentException("Rascunho não pode ter fotografia parcial.");
        }
        this.codigoProduto = codigoProduto;
        this.nomeProduto = nomeProduto;
        this.categoria = categoria;
        this.unidade = unidade == null || unidade.isEmpty() ? null : unidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = subtotal;
    }

    public static ItemVenda rascunho(int id, int produtoId, BigDecimal quantidade) {
        return new ItemVenda(id, produtoId, quantidade, null, null, null, null, null, null);
    }

    static ItemVenda restaurar(int id, int produtoId, BigDecimal quantidade, String codigoProduto,
                               String nomeProduto, String categoria, String unidade,
                               BigDecimal precoUnitario, BigDecimal subtotal) {
        return new ItemVenda(id, produtoId, quantidade, codigoProduto, nomeProduto,
            categoria, unidade, precoUnitario, subtotal);
    }

    ItemVenda confirmar(Produto produto) {
        if (produto == null || !produto.isAtivo()) {
            throw new IllegalArgumentException("Produto inexistente ou inativo: " + produtoId);
        }
        if (produto.getUnidade() == null && quantidade.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("Produto sem unidade deve ser vendido em quantidade 1 por item.");
        }
        if (!produto.isPermiteFracao() && quantidade.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("A unidade do produto não permite quantidade fracionada.");
        }
        BigDecimal subtotalCalculado = produto.getPreco().multiply(quantidade)
            .setScale(2, RoundingMode.HALF_UP);
        return restaurar(id, produtoId, quantidade, produto.getCodigo(), produto.getNome(),
            produto.getCategoria(), produto.getUnidade(), produto.getPreco(), subtotalCalculado);
    }

    public int getId() {
        return id;
    }

    public int getProdutoId() {
        return produtoId;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public String getCodigoProduto() {
        return codigoProduto;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getUnidade() {
        return unidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public boolean possuiFotografiaComercial() {
        return precoUnitario != null;
    }
}
