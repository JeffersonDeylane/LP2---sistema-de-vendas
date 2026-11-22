import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class ServicoVendas {
    private final RepositorioVendasExcel vendas;
    private final RepositorioUsuariosExcel usuarios;

    public ServicoVendas(RepositorioVendasExcel vendas, RepositorioUsuariosExcel usuarios) {
        this.vendas = Objects.requireNonNull(vendas);
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    public Cliente cadastrarCliente(Usuario solicitante, String identificacao, String nome) {
        Usuario autorizado = exigirRegistrador(solicitante);
        synchronized (RepositorioUsuariosExcel.BLOQUEIO_ESCRITA) {
            String identificacaoValidada = Validador.textoObrigatorio(identificacao, "Identificação do cliente");
            if (vendas.listarClientes().stream().anyMatch(cliente ->
                    cliente.getIdentificacao().equalsIgnoreCase(identificacaoValidada))) {
                throw new IllegalArgumentException("A identificação do cliente já está cadastrada.");
            }
            Cliente cliente = new Cliente(vendas.proximoIdCliente(), identificacaoValidada, nome, true);
            vendas.salvar(cliente);
            return cliente;
        }
    }

    public Produto cadastrarProduto(Usuario solicitante, String codigo, String nome, String categoria,
                                    BigDecimal preco, String unidade, boolean permiteFracao) {
        exigirAdministradorCatalogo(solicitante);
        synchronized (RepositorioUsuariosExcel.BLOQUEIO_ESCRITA) {
            String codigoValidado = Validador.textoObrigatorio(codigo, "Código do produto");
            if (vendas.listarProdutos().stream().anyMatch(produto ->
                    produto.getCodigo().equalsIgnoreCase(codigoValidado))) {
                throw new IllegalArgumentException("O código do produto já está cadastrado.");
            }
            Produto produto = new Produto(vendas.proximoIdProduto(), codigoValidado, nome, categoria,
                preco, unidade, permiteFracao, true);
            vendas.salvar(produto);
            return produto;
        }
    }

    public Contrato cadastrarContrato(Usuario solicitante, int clienteId, String numero) {
        exigirRegistrador(solicitante);
        synchronized (RepositorioUsuariosExcel.BLOQUEIO_ESCRITA) {
            Cliente cliente = vendas.listarClientes().stream()
                .filter(c -> c.getId() == clienteId && c.isAtivo())
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                    "Cliente inexistente ou inativo."));
            String numeroValidado = Validador.textoObrigatorio(numero, "Número do contrato");
            if (vendas.listarContratos().stream().anyMatch(contrato ->
                    contrato.getNumero().equalsIgnoreCase(numeroValidado))) {
                throw new IllegalArgumentException("O número do contrato já está cadastrado.");
            }
            Contrato contrato = new Contrato(vendas.proximoIdContrato(),
                numeroValidado, cliente.getId(), true);
            vendas.salvar(contrato);
            return contrato;
        }
    }

    public Venda criarRascunho(Usuario solicitante, int clienteId, String numeroContrato,
                               List<ItemSolicitado> itensSolicitados) {
        Usuario autorizado = exigirRegistrador(solicitante);
        synchronized (RepositorioUsuariosExcel.BLOQUEIO_ESCRITA) {
            Cliente cliente = vendas.listarClientes().stream()
                .filter(c -> c.getId() == clienteId && c.isAtivo())
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                    "Cliente inexistente ou inativo."));
            Integer contratoId = resolverContrato(numeroContrato, cliente.getId());
            if (itensSolicitados == null || itensSolicitados.isEmpty()) {
                throw new IllegalArgumentException("A venda deve conter ao menos um item.");
            }

            Map<Integer, Produto> produtosAtivos = new LinkedHashMap<>();
            for (Produto produto : vendas.listarProdutos()) {
                if (produto.isAtivo()) produtosAtivos.put(produto.getId(), produto);
            }
            List<ItemVenda> itens = new ArrayList<>();
            int proximoIdItem = vendas.proximoIdItem();
            for (ItemSolicitado solicitado : itensSolicitados) {
                if (solicitado == null) throw new IllegalArgumentException("Item da venda inválido.");
                Produto produto = produtosAtivos.get(solicitado.produtoId());
                ItemVenda item = ItemVenda.rascunho(proximoIdItem++, solicitado.produtoId(),
                    solicitado.quantidade());
                item.confirmar(produto);
                itens.add(item);
            }

            Venda venda = Venda.rascunho(vendas.proximoIdVenda(), cliente.getId(),
                autorizado.getId(), contratoId, LocalDateTime.now(), itens);
            vendas.salvar(venda);
            return venda;
        }
    }

    public Venda confirmar(Usuario solicitante, int vendaId) {
        Usuario autorizado = exigirRegistrador(solicitante);
        synchronized (RepositorioUsuariosExcel.BLOQUEIO_ESCRITA) {
            Venda rascunho = vendas.listarVendas().stream()
                .filter(venda -> venda.getId() == vendaId)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Venda não encontrada."));
            if (autorizado instanceof Vendedor && rascunho.getVendedorId() != autorizado.getId()) {
                throw new IllegalArgumentException("Vendedor só pode confirmar as próprias vendas.");
            }
            validarClienteContratoAtivos(rascunho);
            Map<Integer, Produto> produtosAtivos = new LinkedHashMap<>();
            for (Produto produto : vendas.listarProdutos()) {
                if (produto.isAtivo()) produtosAtivos.put(produto.getId(), produto);
            }
            Venda confirmada = rascunho.confirmar(produtosAtivos, LocalDateTime.now());
            vendas.salvar(confirmada);
            return confirmada;
        }
    }

    public List<Cliente> listarClientesAtivos(Usuario solicitante) {
        exigirRegistrador(solicitante);
        return vendas.listarClientes().stream().filter(Cliente::isAtivo).toList();
    }

    public List<Produto> listarProdutosAtivos(Usuario solicitante) {
        exigirRegistrador(solicitante);
        return vendas.listarProdutos().stream().filter(Produto::isAtivo).toList();
    }

    public List<Venda> listarVendas(Usuario solicitante) {
        Usuario autorizado = exigirRegistrador(solicitante);
        List<Venda> existentes = vendas.listarVendas();
        if (autorizado instanceof Administrador) return existentes;
        return existentes.stream().filter(venda -> venda.getVendedorId() == autorizado.getId()).toList();
    }

    private Integer resolverContrato(String numero, int clienteId) {
        if (numero == null || numero.isBlank()) return null;
        String procurado = numero.trim().toLowerCase(Locale.ROOT);
        Contrato contrato = vendas.listarContratos().stream()
            .filter(c -> c.getNumero().toLowerCase(Locale.ROOT).equals(procurado))
            .findFirst().orElseThrow(() -> new IllegalArgumentException("Contrato não cadastrado."));
        if (!contrato.isAtivo()) throw new IllegalArgumentException("Contrato inativo.");
        if (contrato.getClienteId() != clienteId) {
            throw new IllegalArgumentException("O contrato pertence a outro cliente.");
        }
        return contrato.getId();
    }

    private void validarClienteContratoAtivos(Venda venda) {
        Cliente cliente = vendas.listarClientes().stream()
            .filter(c -> c.getId() == venda.getClienteId())
            .findFirst().orElseThrow(() -> new IllegalArgumentException("Cliente da venda não encontrado."));
        if (!cliente.isAtivo()) throw new IllegalArgumentException("Cliente da venda está inativo.");
        if (venda.getContratoId() != null) {
            Contrato contrato = vendas.listarContratos().stream()
                .filter(c -> c.getId() == venda.getContratoId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Contrato da venda não encontrado."));
            if (!contrato.isAtivo() || contrato.getClienteId() != cliente.getId()) {
                throw new IllegalArgumentException("Contrato inativo ou vinculado a outro cliente.");
            }
        }
    }

    private Usuario exigirRegistrador(Usuario solicitante) {
        Usuario usuario = usuarioAtivoPersistido(solicitante);
        if (!(usuario instanceof Vendedor) && !(usuario instanceof Administrador)) {
            throw new IllegalArgumentException("Somente vendedor ou administrador pode operar vendas.");
        }
        return usuario;
    }

    private Usuario exigirAdministradorCatalogo(Usuario solicitante) {
        Usuario usuario = usuarioAtivoPersistido(solicitante);
        if (!(usuario instanceof Administrador) || !usuario.podeGerenciarCatalogo()) {
            throw new IllegalArgumentException("Apenas um administrador ativo pode cadastrar produtos.");
        }
        return usuario;
    }

    private Usuario usuarioAtivoPersistido(Usuario solicitante) {
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

    public record ItemSolicitado(int produtoId, BigDecimal quantidade) {
        public ItemSolicitado {
            Validador.inteiroPositivo(produtoId, "ID do produto");
            if (quantidade == null || quantidade.signum() <= 0) {
                throw new IllegalArgumentException("A quantidade deve ser positiva.");
            }
        }
    }
}
