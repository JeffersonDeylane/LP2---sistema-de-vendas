import java.util.List;

/** Implementação final prevista para persistir contratos na base Excel. */
public final class RepositorioContratoExcel implements RepositorioContrato {
    private String caminhoBase;
    private GerenciadorArquivoExcel gerenciador;

    @Override
    public Contrato buscarPorId(String id) {
        throw new UnsupportedOperationException("Implementação pendente");
    }

    @Override
    public List<Contrato> listar() {
        throw new UnsupportedOperationException("Implementação pendente");
    }

    @Override
    public void salvar(Contrato contrato) {
        throw new UnsupportedOperationException("Implementação pendente");
    }
}
