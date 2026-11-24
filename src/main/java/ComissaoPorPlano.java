import java.math.BigDecimal;

/** Calcula conforme o plano ou a modalidade do produto. Estratégia final prevista para PoliticaComissao. */
public final class ComissaoPorPlano implements PoliticaComissao {
    @Override
    public BigDecimal calcular(OperacaoComercial operacao) {
        throw new UnsupportedOperationException("Implementação pendente");
    }
}
