import java.math.BigDecimal;

/** Calcula conforme o tipo de operação comercial. Estratégia final prevista para PoliticaComissao. */
public final class ComissaoPorOperacao implements PoliticaComissao {
    @Override
    public BigDecimal calcular(OperacaoComercial operacao) {
        throw new UnsupportedOperationException("Implementação pendente");
    }
}
