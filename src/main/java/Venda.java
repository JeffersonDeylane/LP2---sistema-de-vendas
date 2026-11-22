import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Venda {
    private final int id;
    private final int clienteId;
    private final int vendedorId;
    private final Integer contratoId;
    private final LocalDateTime criadaEm;
    private final LocalDateTime confirmadaEm;
    private final EstadoVenda estado;
    private final List<ItemVenda> itens;
    private final BigDecimal total;

    private Venda(int id, int clienteId, int vendedorId, Integer contratoId,
                  LocalDateTime criadaEm, LocalDateTime confirmadaEm, EstadoVenda estado,
                  List<ItemVenda> itens, BigDecimal total) {
        this.id = Validador.inteiroPositivo(id, "ID da venda");
        this.clienteId = Validador.inteiroPositivo(clienteId, "ID do cliente");
        this.vendedorId = Validador.inteiroPositivo(vendedorId, "ID do vendedor");
        if (contratoId != null) {
            Validador.inteiroPositivo(contratoId, "ID do contrato");
        }
        if (criadaEm == null || estado == null || itens == null || total == null) {
            throw new IllegalArgumentException("Dados incompletos da venda.");
        }
        this.contratoId = contratoId;
        this.criadaEm = criadaEm;
        this.confirmadaEm = confirmadaEm;
        this.estado = estado;
        this.itens = List.copyOf(itens);
        this.total = total;
        Set<Integer> idsItens = new HashSet<>();
        for (ItemVenda item : this.itens) {
            if (item == null || !idsItens.add(item.getId())) {
                throw new IllegalArgumentException("A venda contém item nulo ou ID de item duplicado.");
            }
        }
        if (estado == EstadoVenda.CONFIRMADA
                && (confirmadaEm == null || this.itens.isEmpty()
                    || this.itens.stream().anyMatch(item -> !item.possuiFotografiaComercial())
                    || this.itens.stream().map(ItemVenda::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(total) != 0
                    || total.signum() <= 0)) {
            throw new IllegalArgumentException("Venda confirmada exige data e itens fotografados.");
        }
        if (estado == EstadoVenda.RASCUNHO && (confirmadaEm != null
                || total.signum() != 0
                || this.itens.stream().anyMatch(ItemVenda::possuiFotografiaComercial))) {
            throw new IllegalArgumentException("Rascunho não pode ter confirmação nem totais comerciais.");
        }
    }

    public static Venda rascunho(int id, int clienteId, int vendedorId, Integer contratoId,
                                LocalDateTime criadaEm, List<ItemVenda> itens) {
        return new Venda(id, clienteId, vendedorId, contratoId, criadaEm, null,
            EstadoVenda.RASCUNHO, itens, BigDecimal.ZERO.setScale(2));
    }

    static Venda restaurar(int id, int clienteId, int vendedorId, Integer contratoId,
                           LocalDateTime criadaEm, LocalDateTime confirmadaEm,
                           EstadoVenda estado, List<ItemVenda> itens, BigDecimal total) {
        return new Venda(id, clienteId, vendedorId, contratoId, criadaEm,
            confirmadaEm, estado, itens, total);
    }

    public Venda confirmar(Map<Integer, Produto> produtos, LocalDateTime momento) {
        if (estado != EstadoVenda.RASCUNHO) {
            throw new IllegalArgumentException("Somente uma venda em rascunho pode ser confirmada.");
        }
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("Não é possível confirmar uma venda sem itens.");
        }
        if (produtos == null || momento == null) {
            throw new IllegalArgumentException("Produtos e data de confirmação são obrigatórios.");
        }
        List<ItemVenda> itensConfirmados = new ArrayList<>();
        BigDecimal totalCalculado = BigDecimal.ZERO.setScale(2);
        for (ItemVenda item : itens) {
            ItemVenda confirmado = item.confirmar(produtos.get(item.getProdutoId()));
            itensConfirmados.add(confirmado);
            totalCalculado = totalCalculado.add(confirmado.getSubtotal());
        }
        return new Venda(id, clienteId, vendedorId, contratoId, criadaEm, momento,
            EstadoVenda.CONFIRMADA, itensConfirmados, totalCalculado);
    }

    public int getId() {
        return id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public int getVendedorId() {
        return vendedorId;
    }

    public Integer getContratoId() {
        return contratoId;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getConfirmadaEm() {
        return confirmadaEm;
    }

    public EstadoVenda getEstado() {
        return estado;
    }

    public List<ItemVenda> getItens() {
        return itens;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
