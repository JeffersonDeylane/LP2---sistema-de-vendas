import java.math.BigDecimal;

/** Contrato polimórfico que permite trocar o cálculo de comissão conforme a regra comercial. */
public interface PoliticaComissao {
    BigDecimal calcular(OperacaoComercial operacao);
}
