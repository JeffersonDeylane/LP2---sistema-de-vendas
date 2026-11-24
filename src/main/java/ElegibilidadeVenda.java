/** Verificará a instalação da venda antes de liberar o pagamento. Estratégia final prevista para PoliticaElegibilidade. */
public final class ElegibilidadeVenda implements PoliticaElegibilidade {
    @Override
    public boolean podePagar(OperacaoComercial operacao) {
        throw new UnsupportedOperationException("Implementação pendente");
    }
}
