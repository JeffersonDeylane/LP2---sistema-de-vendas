import java.math.BigDecimal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ServicoVendasTest {
    @Rule
    public final TemporaryFolder pasta = new TemporaryFolder();

    @Test
    public void vendedorRegistraRascunhoComContratoOpcionalEConfirmaFotografia() throws Exception {
        Fixture fixture = new Fixture(pasta.newFolder().toPath().resolve("base.xlsx"));
        Produto produto = fixture.servico.cadastrarProduto(fixture.admin, "P-01",
            "Consultoria", "Serviços", new BigDecimal("20.00"), "hora", true);
        Cliente cliente = fixture.servico.cadastrarCliente(fixture.vendedor, "CLI-01", "Cliente teste");
        Contrato contrato = fixture.servico.cadastrarContrato(fixture.vendedor, cliente.getId(), "C-01");

        Venda rascunho = fixture.servico.criarRascunho(fixture.vendedor, cliente.getId(),
            contrato.getNumero(), List.of(
                new ServicoVendas.ItemSolicitado(produto.getId(), new BigDecimal("1.5")),
                new ServicoVendas.ItemSolicitado(produto.getId(), BigDecimal.ONE)));
        assertEquals(EstadoVenda.RASCUNHO, rascunho.getEstado());
        assertEquals(BigDecimal.ZERO.setScale(2), rascunho.getTotal());
        assertEquals(2, rascunho.getItens().size());

        Venda confirmada = fixture.servico.confirmar(fixture.vendedor, rascunho.getId());

        assertEquals(EstadoVenda.CONFIRMADA, confirmada.getEstado());
        assertEquals(new BigDecimal("50.00"), confirmada.getTotal());
        assertEquals(new BigDecimal("20.00"), confirmada.getItens().get(0).getPrecoUnitario());
        assertEquals(contrato.getId(), confirmada.getContratoId().intValue());

        Produto produtoAtualizado = new Produto(produto.getId(), produto.getCodigo(),
            produto.getNome(), produto.getCategoria(), new BigDecimal("30.00"),
            produto.getUnidade(), true, true);
        fixture.repositorioVendas.salvar(produtoAtualizado);
        Venda recuperada = new RepositorioVendasExcel(fixture.arquivo)
            .listarVendas().get(0);
        assertEquals(EstadoVenda.CONFIRMADA, recuperada.getEstado());
        assertEquals(new BigDecimal("50.00"), recuperada.getTotal());
        assertEquals("Consultoria", recuperada.getItens().get(0).getNomeProduto());
        assertEquals(fixture.vendedor.getId(), recuperada.getVendedorId());
        assertTrue(fixture.servico.listarVendas(fixture.vendedor).stream()
            .anyMatch(venda -> venda.getId() == recuperada.getId()));
        assertThrows(IllegalArgumentException.class,
            () -> fixture.servico.confirmar(fixture.vendedor, confirmada.getId()));
    }

    @Test
    public void vendaAvulsaEConfirmadaSemExigirContrato() throws Exception {
        Fixture fixture = new Fixture(pasta.newFolder().toPath().resolve("base.xlsx"));
        Produto produto = fixture.servico.cadastrarProduto(fixture.admin, "P-02",
            "Peça", "Unidade", new BigDecimal("12.50"), null, false);
        Cliente cliente = fixture.servico.cadastrarCliente(fixture.vendedor, "CLI-02", "Outro cliente");

        Venda rascunho = fixture.servico.criarRascunho(fixture.vendedor, cliente.getId(),
            null, List.of(new ServicoVendas.ItemSolicitado(produto.getId(), BigDecimal.ONE)));
        Venda confirmada = fixture.servico.confirmar(fixture.vendedor, rascunho.getId());

        assertNull(confirmada.getContratoId());
        assertEquals(new BigDecimal("12.50"), confirmada.getTotal());
    }

    @Test
    public void arredondaCadaSubtotalParaCentavosComHalfUp() throws Exception {
        Fixture fixture = new Fixture(pasta.newFolder().toPath().resolve("base.xlsx"));
        Produto produto = fixture.servico.cadastrarProduto(fixture.admin, "P-09",
            "Medida", "Serviços", new BigDecimal("10.01"), "kg", true);
        Cliente cliente = fixture.servico.cadastrarCliente(fixture.vendedor, "CLI-09", "Cliente");
        Venda rascunho = fixture.servico.criarRascunho(fixture.vendedor, cliente.getId(),
            null, List.of(new ServicoVendas.ItemSolicitado(
                produto.getId(), new BigDecimal("0.5"))));

        assertEquals(new BigDecimal("5.01"),
            fixture.servico.confirmar(fixture.vendedor, rascunho.getId()).getTotal());
    }

    @Test
    public void rejeitaVendasVaziasContratosAlheiosEConfirmacaoPorOutroVendedor() throws Exception {
        Fixture fixture = new Fixture(pasta.newFolder().toPath().resolve("base.xlsx"));
        Produto produto = fixture.servico.cadastrarProduto(fixture.admin, "P-03",
            "Peça", "Unidade", new BigDecimal("5.00"), null, false);
        Cliente cliente = fixture.servico.cadastrarCliente(fixture.vendedor, "CLI-03", "Cliente A");
        Cliente outroCliente = fixture.servico.cadastrarCliente(fixture.vendedor, "CLI-04", "Cliente B");
        Contrato contrato = fixture.servico.cadastrarContrato(fixture.vendedor, outroCliente.getId(), "C-04");

        assertThrows(IllegalArgumentException.class, () -> fixture.servico.criarRascunho(
            fixture.vendedor, cliente.getId(), null, List.of()));
        assertThrows(IllegalArgumentException.class, () -> fixture.servico.criarRascunho(
            fixture.vendedor, cliente.getId(), contrato.getNumero(),
            List.of(new ServicoVendas.ItemSolicitado(produto.getId(), BigDecimal.ONE))));
        Venda rascunho = fixture.servico.criarRascunho(fixture.vendedor, cliente.getId(),
            null, List.of(new ServicoVendas.ItemSolicitado(produto.getId(), BigDecimal.ONE)));

        assertThrows(IllegalArgumentException.class,
            () -> fixture.servico.confirmar(fixture.outroVendedor, rascunho.getId()));
        assertEquals(EstadoVenda.RASCUNHO,
            fixture.servico.listarVendas(fixture.vendedor).get(0).getEstado());
    }

    @Test
    public void bloqueiaProdutoAUsuarioSemPermissaoEUsuarioInativo() throws Exception {
        Fixture fixture = new Fixture(pasta.newFolder().toPath().resolve("base.xlsx"));
        assertThrows(IllegalArgumentException.class, () -> fixture.servico.cadastrarProduto(
            fixture.vendedor, "P-04", "Produto", "Categoria",
            new BigDecimal("1.00"), null, false));
        fixture.vendedor.desativar();
        fixture.repositorioUsuarios.salvar(fixture.vendedor);
        assertThrows(IllegalArgumentException.class, () -> fixture.servico.cadastrarCliente(
            fixture.vendedor, "CLI-05", "Inativo"));
        assertTrue(fixture.repositorioVendas.listarClientes().isEmpty());
    }

    @Test
    public void validaUnidadeFracaoEPrecoAntesDeSalvarVenda() throws Exception {
        Fixture fixture = new Fixture(pasta.newFolder().toPath().resolve("base.xlsx"));
        Cliente cliente = fixture.servico.cadastrarCliente(fixture.vendedor, "CLI-06", "Cliente");
        Produto unitario = fixture.servico.cadastrarProduto(fixture.admin, "P-05",
            "Produto", "Unidade", new BigDecimal("1.00"), null, false);
        Produto indivisivel = fixture.servico.cadastrarProduto(fixture.admin, "P-06",
            "Caixa", "Unidade", new BigDecimal("2.00"), "caixa", false);

        assertThrows(IllegalArgumentException.class, () -> fixture.servico.criarRascunho(
            fixture.vendedor, cliente.getId(), null,
            List.of(new ServicoVendas.ItemSolicitado(unitario.getId(), new BigDecimal("2")))));
        assertThrows(IllegalArgumentException.class, () -> fixture.servico.criarRascunho(
            fixture.vendedor, cliente.getId(), null,
            List.of(new ServicoVendas.ItemSolicitado(indivisivel.getId(), new BigDecimal("1.5")))));
        assertThrows(IllegalArgumentException.class, () -> fixture.servico.cadastrarProduto(
            fixture.admin, "P-07", "Inválido", "Unidade",
            new BigDecimal("0.001"), "un", false));
        assertTrue(fixture.repositorioVendas.listarVendas().isEmpty());
    }

    @Test
    public void vendedorConsultaSomenteAsPropriasVendasEBasePreservaUsuarios() throws Exception {
        Fixture fixture = new Fixture(pasta.newFolder().toPath().resolve("base.xlsx"));
        Produto produto = fixture.servico.cadastrarProduto(fixture.admin, "P-08",
            "Produto", "Unidade", new BigDecimal("3.00"), null, false);
        Cliente cliente = fixture.servico.cadastrarCliente(fixture.vendedor, "CLI-08", "Cliente");
        Venda venda = fixture.servico.criarRascunho(fixture.vendedor, cliente.getId(),
            null, List.of(new ServicoVendas.ItemSolicitado(produto.getId(), BigDecimal.ONE)));
        fixture.servico.criarRascunho(fixture.outroVendedor, cliente.getId(),
            null, List.of(new ServicoVendas.ItemSolicitado(produto.getId(), BigDecimal.ONE)));

        assertEquals(1, fixture.servico.listarVendas(fixture.vendedor).size());
        assertEquals(2, fixture.servico.listarVendas(fixture.admin).size());
        assertEquals(venda.getId(), fixture.servico.listarVendas(fixture.vendedor).get(0).getId());
        assertNotNull(fixture.repositorioUsuarios.listar().stream()
            .filter(usuario -> usuario.getId() == fixture.vendedor.getId()).findFirst().orElse(null));
        assertFalse(fixture.servico.listarVendas(fixture.vendedor).isEmpty());
    }

    @Test
    public void apoioConsultaVendasDaEquipeComContaAtiva() throws Exception {
        Fixture fixture = new Fixture(pasta.newFolder().toPath().resolve("base.xlsx"));
        Apoio apoio = new Apoio(4, "Apoio", "apoio", "hash", true);
        fixture.repositorioUsuarios.salvar(apoio);
        Produto produto = fixture.servico.cadastrarProduto(fixture.admin, "P-10",
            "Produto", "Unidade", new BigDecimal("3.00"), null, false);
        Cliente cliente = fixture.servico.cadastrarCliente(fixture.vendedor, "CLI-10", "Cliente");
        var itens = List.of(new ServicoVendas.ItemSolicitado(produto.getId(), BigDecimal.ONE));
        Venda primeira = fixture.servico.criarRascunho(fixture.vendedor, cliente.getId(), null, itens);
        Venda segunda = fixture.servico.criarRascunho(fixture.outroVendedor, cliente.getId(), null, itens);

        assertEquals(List.of(primeira.getId(), segunda.getId()),
            fixture.servico.listarVendas(apoio).stream().map(Venda::getId).sorted().toList());
        assertThrows(IllegalArgumentException.class,
            () -> fixture.servico.criarRascunho(apoio, cliente.getId(), null, itens));
        assertThrows(IllegalArgumentException.class,
            () -> fixture.servico.confirmar(apoio, primeira.getId()));

        apoio.desativar();
        fixture.repositorioUsuarios.salvar(apoio);
        Apoio sessaoAnterior = new Apoio(apoio.getId(), "Apoio", "apoio", "hash", true);
        assertThrows(IllegalArgumentException.class, () -> fixture.servico.listarVendas(sessaoAnterior));
    }

    @Test
    public void menuPermiteAdministradorPrepararCatalogoEVendedorConfirmarVenda() throws Exception {
        Path arquivo = pasta.newFolder().toPath().resolve("base-menu.xlsx");
        RepositorioUsuariosExcel repositorioUsuarios = new RepositorioUsuariosExcel(arquivo);
        repositorioUsuarios.salvar(new Administrador(1, "Admin", "admin",
            Senhas.gerarHash("senha-admin"), true));
        repositorioUsuarios.salvar(new Vendedor(2, "Vendedor", "vendedor",
            Senhas.gerarHash("senha-vendedor"), true, "V01"));

        repositorioUsuarios.salvar(new Apoio(3, "Apoio", "apoio",
            Senhas.gerarHash("senha-apoio"), true));

        InputStream entradaAnterior = System.in;
        PrintStream saidaAnterior = System.out;
        ByteArrayOutputStream captura = new ByteArrayOutputStream();
        String entradas = "1\nadmin\nsenha-admin\n7\nP-01\nProduto\nCategoria\n10,00\n\nn\n"
            + "5\n1\nvendedor\nsenha-vendedor\n6\nCLI-01\nCliente\n"
            + "9\n1\n\n1\n1\n0\n10\n1\n11\n5\n1\napoio\nsenha-apoio\n11\n0\n";
        try (PrintStream saida = new PrintStream(captura, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(entradas.getBytes(StandardCharsets.UTF_8)));
            System.setOut(saida);
            Main.main(new String[] {arquivo.toString()});
        } finally {
            System.setIn(entradaAnterior);
            System.setOut(saidaAnterior);
        }

        String texto = captura.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("Produto cadastrado com ID 1."));
        assertTrue(texto.contains("Cliente cadastrado com ID 1."));
        assertTrue(texto.contains("salva em rascunho"));
        assertTrue(texto.contains("Venda 1 confirmada. Total: R$ 10.00"));
        String sessaoApoio = texto.substring(texto.indexOf("Bem-vindo, Apoio!"));
        assertTrue(sessaoApoio.contains("11 - Consultar vendas da equipe"));
        assertTrue(sessaoApoio.contains("Venda 1 - CONFIRMADA"));
        assertFalse(sessaoApoio.contains("9 - Registrar venda"));
        assertFalse(sessaoApoio.contains("10 - Confirmar venda"));
        assertEquals(EstadoVenda.CONFIRMADA,
            new RepositorioVendasExcel(arquivo).listarVendas().get(0).getEstado());
    }

    private static final class Fixture {
        private final Path arquivo;
        private final RepositorioUsuariosExcel repositorioUsuarios;
        private final RepositorioVendasExcel repositorioVendas;
        private final ServicoVendas servico;
        private final Usuario admin;
        private final Usuario vendedor;
        private final Usuario outroVendedor;

        private Fixture(Path arquivo) {
            this.arquivo = arquivo;
            repositorioUsuarios = new RepositorioUsuariosExcel(arquivo);
            repositorioVendas = new RepositorioVendasExcel(arquivo);
            admin = new Administrador(1, "Admin", "admin", "hash", true);
            vendedor = new Vendedor(2, "Vendedor", "vendedor", "hash", true, "V01");
            outroVendedor = new Vendedor(3, "Outro", "outro", "hash", true, "V02");
            repositorioUsuarios.salvar(admin);
            repositorioUsuarios.salvar(vendedor);
            repositorioUsuarios.salvar(outroVendedor);
            servico = new ServicoVendas(repositorioVendas, repositorioUsuarios);
        }
    }
}
