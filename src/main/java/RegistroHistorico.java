import java.time.LocalDateTime;

/** Registra quem alterou um campo do contrato, quando e quais eram os valores. */
public class RegistroHistorico {
    private String id;
    private Contrato contrato;
    private Usuario responsavel;
    private String campo;
    private String valorAnterior;
    private String valorNovo;
    private LocalDateTime dataHora;
}
