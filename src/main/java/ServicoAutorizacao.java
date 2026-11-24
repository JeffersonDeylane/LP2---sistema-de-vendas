import java.util.Objects;

/** Valida a conta persistida e as permissões das operações comerciais. */
public final class ServicoAutorizacao {
    private final RepositorioUsuariosExcel usuarios;

    public ServicoAutorizacao(RepositorioUsuariosExcel usuarios) {
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    public Usuario exigirContaAtiva(Usuario solicitante) {
        if (solicitante == null) throw new IllegalArgumentException("Usuário autenticado obrigatório.");
        Usuario persistido = usuarios.listar().stream()
            .filter(usuario -> usuario.getId() == solicitante.getId())
            .findFirst().orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (persistido.getClass() != solicitante.getClass()
                || !persistido.getLogin().equals(solicitante.getLogin())
                || !persistido.isAtivo()) {
            throw new IllegalArgumentException("Usuário inativo ou sessão não corresponde à conta persistida.");
        }
        return persistido;
    }

    public void exigirSolicitante(Usuario solicitante, int vendedorId) {
        Usuario usuario = exigirContaAtiva(solicitante);
        Validador.inteiroPositivo(vendedorId, "ID do vendedor");
        if (!usuario.podeSolicitarAlteracao()
                || (usuario instanceof Vendedor && usuario.getId() != vendedorId)) {
            throw new IllegalArgumentException("Usuário sem permissão para solicitar alteração nessa venda.");
        }
    }

    public void exigirRevisor(Usuario revisor, Usuario solicitante) {
        Usuario usuario = exigirContaAtiva(revisor);
        if (!usuario.podeAprovarAlteracoes(solicitante)) {
            throw new IllegalArgumentException("Apenas Apoio ou Administrador pode revisar solicitações de terceiros.");
        }
    }

    public void exigirResponsavelPorCancelamentoOuReativacao(Usuario responsavel) {
        Usuario usuario = exigirContaAtiva(responsavel);
        if (!(usuario instanceof Apoio) && !(usuario instanceof Administrador)) {
            throw new IllegalArgumentException("Apenas Apoio ou Administrador pode cancelar ou reativar vendas.");
        }
    }
}
