/** Contrato polimórfico para aplicar diferentes condições de pagamento. */
public interface PoliticaElegibilidade {
    boolean podePagar(OperacaoComercial operacao);
}
