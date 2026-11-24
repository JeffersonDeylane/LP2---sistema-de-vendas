import java.time.LocalDate;

/** Base abstrata para tratar vendas e mudanças de tecnologia pelo mesmo tipo. */
public abstract class OperacaoComercial {
    private Long id;
    private LocalDate dataCadastro;
    private Vendedor responsavel;
}
