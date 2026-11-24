import java.util.List;

/** Contrato de armazenamento para substituir o Excel por outro meio de persistência. */
public interface RepositorioContrato {
    Contrato buscarPorId(String id);

    List<Contrato> listar();

    void salvar(Contrato contrato);
}
