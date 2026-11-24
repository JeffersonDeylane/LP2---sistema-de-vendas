import java.util.List;

/** Agrupa somente os itens efetivamente cancelados por uma ação. */
public class GrupoCancelamento {
    private String id;
    private Contrato contrato;
    private Usuario responsavel;
    private String motivo;
    private List<ItemCancelado> itens;
}
