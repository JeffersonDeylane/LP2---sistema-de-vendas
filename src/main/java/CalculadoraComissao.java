import java.math.BigDecimal;

/** Delegará o cálculo à política de comissão selecionada para a operação. */
public final class CalculadoraComissao {
    private PoliticaComissao politica;

    public BigDecimal calcular(OperacaoComercial operacao) {
        throw new UnsupportedOperationException("Implementação pendente");
    }
}
