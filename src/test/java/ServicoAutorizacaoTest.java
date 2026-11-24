import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertThrows;

public class ServicoAutorizacaoTest {
    @Rule
    public TemporaryFolder pasta = new TemporaryFolder();

    @Test
    public void revisaoPermiteTerceirosEBloqueiaProprioPedidoNosDoisCaminhos() {
        var repositorio = repositorio();
        var apoio = new Apoio(1, "Apoio", "apoio", "hash", true);
        var admin = new Administrador(2, "Admin", "admin", "hash", true);
        var vendedor = new Vendedor(3, "Vendedor", "vendedor", "hash", true, "V01");
        repositorio.salvar(apoio);
        repositorio.salvar(admin);
        repositorio.salvar(vendedor);

        var importacao = new ServicoImportacao(repositorio);
        var pedidoApoio = new SolicitacaoAlteracao(new Apoio(1, "Apoio", "apoio", "hash", true));
        var pedidoVendedor = new SolicitacaoAlteracao(vendedor);

        assertThrows(UnsupportedOperationException.class, () -> importacao.aprovar(pedidoVendedor, apoio));
        assertThrows(UnsupportedOperationException.class, () -> importacao.recusar(pedidoApoio, admin, "Motivo"));
        assertThrows(IllegalArgumentException.class, () -> importacao.aprovar(pedidoApoio, apoio));
        assertThrows(IllegalArgumentException.class, () -> importacao.recusar(pedidoApoio, apoio, "Motivo"));
        assertThrows(IllegalArgumentException.class, () -> importacao.aprovar(new SolicitacaoAlteracao(admin), admin));
        assertThrows(IllegalArgumentException.class, () -> importacao.recusar(pedidoApoio, vendedor, "Motivo"));
    }

    @Test
    public void cancelamentoEReativacaoExigemApoioOuAdministradorAtivo() {
        var repositorio = repositorio();
        var apoio = new Apoio(1, "Apoio", "apoio", "hash", true);
        var admin = new Administrador(2, "Admin", "admin", "hash", true);
        var vendedor = new Vendedor(3, "Vendedor", "vendedor", "hash", true, "V01");
        repositorio.salvar(apoio);
        repositorio.salvar(admin);
        repositorio.salvar(vendedor);

        var cancelamento = new ServicoCancelamento(repositorio);
        var grupo = new GrupoCancelamento();
        assertThrows(UnsupportedOperationException.class,
            () -> cancelamento.cancelar(new Contrato(1, "C-1", 1, true), null, apoio, "Motivo"));
        assertThrows(UnsupportedOperationException.class,
            () -> cancelamento.cancelar(new Contrato(2, "C-2", 2, true), null, apoio, "Motivo"));
        assertThrows(UnsupportedOperationException.class, () -> cancelamento.reativar(grupo, apoio));
        assertThrows(UnsupportedOperationException.class, () -> cancelamento.reativar(grupo, admin));
        assertThrows(IllegalArgumentException.class, () -> cancelamento.reativar(grupo, vendedor));
        assertThrows(IllegalArgumentException.class, () -> cancelamento.reativar(grupo, null));

        repositorio.salvar(new Apoio(1, "Apoio", "apoio", "hash", false));
        assertThrows(IllegalArgumentException.class, () -> cancelamento.reativar(grupo, apoio));
    }

    @Test
    public void solicitacaoRespeitaDonoDaVendaEPermiteApoioParaTodaEquipe() {
        var repositorio = repositorio();
        var apoio = new Apoio(1, "Apoio", "apoio", "hash", true);
        var vendedor = new Vendedor(2, "Vendedor", "vendedor", "hash", true, "V01");
        repositorio.salvar(apoio);
        repositorio.salvar(vendedor);
        var autorizacao = new ServicoAutorizacao(repositorio);

        autorizacao.exigirSolicitante(vendedor, 2);
        autorizacao.exigirSolicitante(apoio, 2);
        autorizacao.exigirSolicitante(apoio, 3);
        assertThrows(IllegalArgumentException.class, () -> autorizacao.exigirSolicitante(vendedor, 3));
    }

    private RepositorioUsuariosExcel repositorio() {
        return new RepositorioUsuariosExcel(pasta.getRoot().toPath().resolve("base.xlsx"));
    }
}
