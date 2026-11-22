import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import lp2.processamento.PlanilhasIO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

public final class RepositorioVendasExcel {
    private static final String CLIENTES = "CLIENTES";
    private static final String PRODUTOS = "PRODUTOS";
    private static final String CONTRATOS = "CONTRATOS";
    private static final String VENDAS = "VENDAS";
    private static final String ITENS = "ITENS_VENDA";
    private static final List<String> COLUNAS_CLIENTES =
        List.of("ID", "IDENTIFICACAO", "NOME", "ATIVO");
    private static final List<String> COLUNAS_PRODUTOS =
        List.of("ID", "CODIGO", "NOME", "CATEGORIA", "PRECO", "UNIDADE", "PERMITE_FRACAO", "ATIVO");
    private static final List<String> COLUNAS_CONTRATOS =
        List.of("ID", "NUMERO", "CLIENTE_ID", "ATIVO");
    private static final List<String> COLUNAS_VENDAS =
        List.of("ID", "CLIENTE_ID", "VENDEDOR_ID", "CONTRATO_ID", "CRIADA_EM",
            "CONFIRMADA_EM", "ESTADO", "TOTAL");
    private static final List<String> COLUNAS_ITENS =
        List.of("ID", "VENDA_ID", "PRODUTO_ID", "QUANTIDADE", "CODIGO_PRODUTO",
            "NOME_PRODUTO", "CATEGORIA", "UNIDADE", "PRECO_UNITARIO", "SUBTOTAL");

    private final Path arquivo;

    public RepositorioVendasExcel(Path arquivo) {
        if (arquivo == null) {
            throw new IllegalArgumentException("O caminho da base comercial é obrigatório.");
        }
        this.arquivo = arquivo.toAbsolutePath().normalize();
    }

    public List<Cliente> listarClientes() {
        return ler("clientes", workbook -> {
            validarAbasComerciais(workbook);
            return lerClientes(workbook);
        });
    }

    public List<Produto> listarProdutos() {
        return ler("produtos", workbook -> {
            validarAbasComerciais(workbook);
            return lerProdutos(workbook);
        });
    }

    public List<Contrato> listarContratos() {
        return ler("contratos", workbook -> {
            validarAbasComerciais(workbook);
            return lerContratos(workbook);
        });
    }

    public List<Venda> listarVendas() {
        return ler("vendas", workbook -> {
            validarAbasComerciais(workbook);
            return lerVendas(workbook);
        });
    }

    public int proximoIdCliente() {
        return proximoId(listarClientes().stream().mapToInt(Cliente::getId).max().orElse(0));
    }

    public int proximoIdProduto() {
        return proximoId(listarProdutos().stream().mapToInt(Produto::getId).max().orElse(0));
    }

    public int proximoIdContrato() {
        return proximoId(listarContratos().stream().mapToInt(Contrato::getId).max().orElse(0));
    }

    public int proximoIdVenda() {
        return proximoId(listarVendas().stream().mapToInt(Venda::getId).max().orElse(0));
    }

    public int proximoIdItem() {
        return ler("itens", workbook -> {
            validarAbasComerciais(workbook);
            Sheet sheet = workbook.getSheet(ITENS);
            if (sheet == null) return 1;
            validarCabecalho(sheet, COLUNAS_ITENS);
            int maior = 0;
            for (Row row : sheet) {
                if (row.getRowNum() > 0 && !linhaVazia(row)) {
                    maior = Math.max(maior, lerInteiro(row, 0, "ID do item"));
                }
            }
            return proximoId(maior);
        });
    }

    public void salvar(Cliente cliente) {
        if (cliente == null) throw new IllegalArgumentException("O cliente é obrigatório.");
        alterar("salvar cliente", workbook -> {
            List<Cliente> existentes = lerClientes(workbook);
            for (Cliente existente : existentes) {
                if (existente.getId() != cliente.getId()
                        && existente.getIdentificacao().equalsIgnoreCase(cliente.getIdentificacao())) {
                    throw new IllegalArgumentException("A identificação do cliente já está cadastrada.");
                }
            }
            Sheet sheet = obterAba(workbook, CLIENTES, COLUNAS_CLIENTES);
            escreverRegistro(sheet, cliente.getId(), new Object[] {
                cliente.getId(), cliente.getIdentificacao(), cliente.getNome(), cliente.isAtivo()
            });
        });
    }

    public void salvar(Produto produto) {
        if (produto == null) throw new IllegalArgumentException("O produto é obrigatório.");
        alterar("salvar produto", workbook -> {
            for (Produto existente : lerProdutos(workbook)) {
                if (existente.getId() != produto.getId()
                        && existente.getCodigo().equalsIgnoreCase(produto.getCodigo())) {
                    throw new IllegalArgumentException("O código do produto já está cadastrado.");
                }
            }
            Sheet sheet = obterAba(workbook, PRODUTOS, COLUNAS_PRODUTOS);
            escreverRegistro(sheet, produto.getId(), new Object[] {
                produto.getId(), produto.getCodigo(), produto.getNome(), produto.getCategoria(),
                produto.getPreco().toPlainString(), textoOuVazio(produto.getUnidade()),
                produto.isPermiteFracao(), produto.isAtivo()
            });
        });
    }

    public void salvar(Contrato contrato) {
        if (contrato == null) throw new IllegalArgumentException("O contrato é obrigatório.");
        alterar("salvar contrato", workbook -> {
            for (Contrato existente : lerContratos(workbook)) {
                if (existente.getId() != contrato.getId()
                        && existente.getNumero().equalsIgnoreCase(contrato.getNumero())) {
                    throw new IllegalArgumentException("O número do contrato já está cadastrado.");
                }
            }
            Sheet sheet = obterAba(workbook, CONTRATOS, COLUNAS_CONTRATOS);
            escreverRegistro(sheet, contrato.getId(), new Object[] {
                contrato.getId(), contrato.getNumero(), contrato.getClienteId(), contrato.isAtivo()
            });
        });
    }

    public void salvar(Venda venda) {
        if (venda == null) throw new IllegalArgumentException("A venda é obrigatória.");
        alterar("salvar venda", workbook -> {
            List<Cliente> clientes = lerClientes(workbook);
            Cliente cliente = localizar(clientes, venda.getClienteId(), Cliente::getId);
            if (cliente == null) {
                throw new IllegalArgumentException("O cliente da venda não está cadastrado.");
            }
            List<Contrato> contratos = lerContratos(workbook);
            if (venda.getContratoId() != null) {
                Contrato contrato = localizar(contratos, venda.getContratoId(), Contrato::getId);
                if (contrato == null) throw new IllegalArgumentException("O contrato da venda não está cadastrado.");
                if (contrato.getClienteId() != cliente.getId()) {
                    throw new IllegalArgumentException("O contrato pertence a outro cliente.");
                }
            }
            Map<Integer, Produto> produtos = new LinkedHashMap<>();
            for (Produto produto : lerProdutos(workbook)) produtos.put(produto.getId(), produto);
            for (ItemVenda item : venda.getItens()) {
                if (!produtos.containsKey(item.getProdutoId())) {
                    throw new IllegalArgumentException("Produto do item não está cadastrado: "
                        + item.getProdutoId());
                }
            }
            Venda anterior = localizar(lerVendas(workbook), venda.getId(), Venda::getId);
            if (anterior != null) {
                if (anterior.getEstado() == EstadoVenda.CONFIRMADA) {
                    throw new IllegalArgumentException("Venda confirmada não pode ser sobrescrita.");
                }
                if (anterior.getClienteId() != venda.getClienteId()
                        || anterior.getVendedorId() != venda.getVendedorId()
                        || !Objects.equals(anterior.getContratoId(), venda.getContratoId())
                        || !anterior.getCriadaEm().equals(venda.getCriadaEm())) {
                    throw new IllegalArgumentException("A identidade do rascunho não pode ser alterada.");
                }
            }
            Sheet vendas = obterAba(workbook, VENDAS, COLUNAS_VENDAS);
            escreverRegistro(vendas, venda.getId(), new Object[] {
                venda.getId(), venda.getClienteId(), venda.getVendedorId(),
                venda.getContratoId() == null ? "" : venda.getContratoId(),
                venda.getCriadaEm().toString(),
                venda.getConfirmadaEm() == null ? "" : venda.getConfirmadaEm().toString(),
                venda.getEstado().name(), venda.getTotal().toPlainString()
            });

            Sheet itens = obterAba(workbook, ITENS, COLUNAS_ITENS);
            removerItensDaVenda(itens, venda.getId());
            for (ItemVenda item : venda.getItens()) {
                escreverRegistro(itens, item.getId(), new Object[] {
                    item.getId(), venda.getId(), item.getProdutoId(),
                    item.getQuantidade().toPlainString(), textoOuVazio(item.getCodigoProduto()),
                    textoOuVazio(item.getNomeProduto()), textoOuVazio(item.getCategoria()),
                    textoOuVazio(item.getUnidade()), decimalOuVazio(item.getPrecoUnitario()),
                    decimalOuVazio(item.getSubtotal())
                });
            }
        });
    }

    private List<Cliente> lerClientes(Workbook workbook) {
        Sheet sheet = workbook.getSheet(CLIENTES);
        if (sheet == null) return List.of();
        validarCabecalho(sheet, COLUNAS_CLIENTES);
        List<Cliente> resultado = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        Set<String> identificacoes = new HashSet<>();
        for (Row row : registros(sheet)) {
            int id = lerInteiro(row, 0, "ID do cliente");
            String identificacao = lerTexto(row, 1, "IDENTIFICACAO");
            Cliente cliente = new Cliente(id, identificacao, lerTexto(row, 2, "NOME"),
                lerBooleano(row, 3, "ATIVO"));
            if (!ids.add(id) || !identificacoes.add(identificacao.toLowerCase(Locale.ROOT))) {
                throw registroInvalido(sheet, row, "ID ou identificação de cliente duplicados");
            }
            resultado.add(cliente);
        }
        return resultado;
    }

    private List<Produto> lerProdutos(Workbook workbook) {
        Sheet sheet = workbook.getSheet(PRODUTOS);
        if (sheet == null) return List.of();
        validarCabecalho(sheet, COLUNAS_PRODUTOS);
        List<Produto> resultado = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        Set<String> codigos = new HashSet<>();
        for (Row row : registros(sheet)) {
            int id = lerInteiro(row, 0, "ID do produto");
            String codigo = lerTexto(row, 1, "CODIGO");
            Produto produto = new Produto(id, codigo, lerTexto(row, 2, "NOME"),
                lerTexto(row, 3, "CATEGORIA"), lerDecimal(row, 4, "PRECO"),
                lerTexto(row, 5, "UNIDADE"), lerBooleano(row, 6, "PERMITE_FRACAO"),
                lerBooleano(row, 7, "ATIVO"));
            if (!ids.add(id) || !codigos.add(codigo.toLowerCase(Locale.ROOT))) {
                throw registroInvalido(sheet, row, "ID ou código de produto duplicados");
            }
            resultado.add(produto);
        }
        return resultado;
    }

    private List<Contrato> lerContratos(Workbook workbook) {
        Sheet sheet = workbook.getSheet(CONTRATOS);
        if (sheet == null) return List.of();
        validarCabecalho(sheet, COLUNAS_CONTRATOS);
        List<Contrato> resultado = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        Set<String> numeros = new HashSet<>();
        for (Row row : registros(sheet)) {
            int id = lerInteiro(row, 0, "ID do contrato");
            String numero = lerTexto(row, 1, "NUMERO");
            Contrato contrato = new Contrato(id, numero, lerInteiro(row, 2, "CLIENTE_ID"),
                lerBooleano(row, 3, "ATIVO"));
            if (!ids.add(id) || !numeros.add(numero.toLowerCase(Locale.ROOT))) {
                throw registroInvalido(sheet, row, "ID ou número de contrato duplicados");
            }
            resultado.add(contrato);
        }
        return resultado;
    }

    private List<Venda> lerVendas(Workbook workbook) {
        Sheet salesSheet = workbook.getSheet(VENDAS);
        if (salesSheet == null) {
            if (!lerItens(workbook).isEmpty()) {
                throw new PersistenciaVendasException("Há itens vinculados a vendas inexistentes na aba "
                    + ITENS + ".");
            }
            return List.of();
        }
        validarCabecalho(salesSheet, COLUNAS_VENDAS);
        Map<Integer, List<ItemVenda>> itensPorVenda = lerItens(workbook);
        List<Venda> resultado = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        for (Row row : registros(salesSheet)) {
            int id = lerInteiro(row, 0, "ID da venda");
            int clienteId = lerInteiro(row, 1, "CLIENTE_ID");
            int vendedorId = lerInteiro(row, 2, "VENDEDOR_ID");
            Integer contratoId = lerInteiroOpcional(row, 3, "CONTRATO_ID");
            java.time.LocalDateTime criadaEm = data(row, 4, "CRIADA_EM");
            String confirmadaTexto = lerTexto(row, 5, "CONFIRMADA_EM");
            java.time.LocalDateTime confirmadaEm = confirmadaTexto.isEmpty()
                ? null : data(row, 5, "CONFIRMADA_EM");
            EstadoVenda estado;
            try {
                estado = EstadoVenda.valueOf(lerTexto(row, 6, "ESTADO"));
            } catch (IllegalArgumentException e) {
                throw registroInvalido(salesSheet, row, "estado de venda desconhecido");
            }
            BigDecimal total = lerDecimal(row, 7, "TOTAL");
            List<ItemVenda> itens = itensPorVenda.remove(id);
            if (itens == null) itens = List.of();
            if (!ids.add(id)) throw registroInvalido(salesSheet, row, "ID de venda duplicado");
            resultado.add(Venda.restaurar(id, clienteId, vendedorId, contratoId, criadaEm,
                confirmadaEm, estado, itens, total));
        }
        if (!itensPorVenda.isEmpty()) {
            throw new PersistenciaVendasException("Há itens vinculados a vendas inexistentes na aba " + ITENS + ".");
        }
        return resultado;
    }

    private Map<Integer, List<ItemVenda>> lerItens(Workbook workbook) {
        Sheet sheet = workbook.getSheet(ITENS);
        if (sheet == null) return new LinkedHashMap<>();
        validarCabecalho(sheet, COLUNAS_ITENS);
        Map<Integer, List<ItemVenda>> resultado = new LinkedHashMap<>();
        Set<Integer> ids = new HashSet<>();
        for (Row row : registros(sheet)) {
            int itemId = lerInteiro(row, 0, "ID do item");
            int vendaId = lerInteiro(row, 1, "VENDA_ID");
            int produtoId = lerInteiro(row, 2, "PRODUTO_ID");
            BigDecimal quantidade = lerDecimal(row, 3, "QUANTIDADE");
            String codigo = lerTexto(row, 4, "CODIGO_PRODUTO");
            String nome = lerTexto(row, 5, "NOME_PRODUTO");
            String categoria = lerTexto(row, 6, "CATEGORIA");
            String unidade = lerTexto(row, 7, "UNIDADE");
            String precoTexto = lerTexto(row, 8, "PRECO_UNITARIO");
            String subtotalTexto = lerTexto(row, 9, "SUBTOTAL");
            BigDecimal preco = precoTexto.isEmpty() ? null : decimal(precoTexto, row, "PRECO_UNITARIO");
            BigDecimal subtotal = subtotalTexto.isEmpty() ? null : decimal(subtotalTexto, row, "SUBTOTAL");
            if ((preco == null) != (subtotal == null)
                    || (preco == null) != codigo.isEmpty()
                    || (preco == null) != nome.isEmpty()
                    || (preco == null) != categoria.isEmpty()) {
                throw registroInvalido(sheet, row, "fotografia comercial incompleta");
            }
            if (!ids.add(itemId)) throw registroInvalido(sheet, row, "ID de item duplicado");
            resultado.computeIfAbsent(vendaId, ignorado -> new ArrayList<>()).add(
                ItemVenda.restaurar(itemId, produtoId, quantidade,
                    preco == null ? null : codigo, preco == null ? null : nome,
                    preco == null ? null : categoria, preco == null ? null : unidade,
                    preco, subtotal));
        }
        return resultado;
    }

    private void alterar(String operacao, Consumer<Workbook> alteracao) {
        synchronized (RepositorioUsuariosExcel.BLOQUEIO_ESCRITA) {
            byte[] anterior = null;
            try {
                anterior = Files.notExists(arquivo, LinkOption.NOFOLLOW_LINKS)
                    ? null : Files.readAllBytes(arquivo);
                try (Workbook workbook = abrir(anterior)) {
                    alteracao.accept(workbook);
                    byte[] versaoAnterior = anterior;
                    PlanilhasIO.salvarComBackup(workbook, arquivo, versaoAnterior,
                        this::validarAbasComerciais);
                }
            } catch (IOException | RuntimeException e) {
                if (e instanceof PersistenciaVendasException falha) throw falha;
                throw new PersistenciaVendasException(
                    "Não foi possível " + operacao + " na base comercial " + arquivo + ".", e);
            }
        }
    }

    private void validarAbasComerciais(Workbook workbook) {
        Map<Integer, Cliente> clientes = new LinkedHashMap<>();
        for (Cliente cliente : lerClientes(workbook)) clientes.put(cliente.getId(), cliente);
        Map<Integer, Produto> produtos = new LinkedHashMap<>();
        for (Produto produto : lerProdutos(workbook)) produtos.put(produto.getId(), produto);
        Map<Integer, Contrato> contratos = new LinkedHashMap<>();
        for (Contrato contrato : lerContratos(workbook)) {
            if (!clientes.containsKey(contrato.getClienteId())) {
                throw new PersistenciaVendasException("Contrato " + contrato.getId()
                    + " referencia cliente inexistente.");
            }
            contratos.put(contrato.getId(), contrato);
        }
        for (Venda venda : lerVendas(workbook)) {
            if (!clientes.containsKey(venda.getClienteId())) {
                throw new PersistenciaVendasException("Venda " + venda.getId()
                    + " referencia cliente inexistente.");
            }
            if (venda.getContratoId() != null) {
                Contrato contrato = contratos.get(venda.getContratoId());
                if (contrato == null || contrato.getClienteId() != venda.getClienteId()) {
                    throw new PersistenciaVendasException("Venda " + venda.getId()
                        + " possui contrato inexistente ou de outro cliente.");
                }
            }
            for (ItemVenda item : venda.getItens()) {
                if (!produtos.containsKey(item.getProdutoId())) {
                    throw new PersistenciaVendasException("Item " + item.getId()
                        + " referencia produto inexistente.");
                }
            }
        }
    }

    private <T> T ler(String operacao, Function<Workbook, T> leitura) {
        if (Files.notExists(arquivo, LinkOption.NOFOLLOW_LINKS)) {
            try (Workbook workbook = new XSSFWorkbook()) {
                return leitura.apply(workbook);
            } catch (IOException | RuntimeException e) {
                throw new PersistenciaVendasException("Não foi possível ler " + operacao + ".", e);
            }
        }
        try (InputStream entrada = Files.newInputStream(arquivo);
             Workbook workbook = WorkbookFactory.create(entrada)) {
            return leitura.apply(workbook);
        } catch (IOException | RuntimeException e) {
            if (e instanceof PersistenciaVendasException falha) throw falha;
            throw new PersistenciaVendasException(
                "Não foi possível ler " + operacao + " na base comercial " + arquivo + ".", e);
        }
    }

    private Workbook abrir(byte[] conteudo) throws IOException {
        if (conteudo == null) return new XSSFWorkbook();
        try (InputStream entrada = new ByteArrayInputStream(conteudo)) {
            return WorkbookFactory.create(entrada);
        }
    }

    private static int proximoId(int maiorId) {
        if (maiorId == Integer.MAX_VALUE) {
            throw new PersistenciaVendasException("Não há IDs disponíveis para a entidade.");
        }
        return maiorId + 1;
    }

    private static <T> T localizar(List<T> valores, int id, ToIntFunction<T> identificador) {
        return valores.stream().filter(valor -> identificador.applyAsInt(valor) == id)
            .findFirst().orElse(null);
    }

    private static Sheet obterAba(Workbook workbook, String nome, List<String> colunas) {
        Sheet sheet = workbook.getSheet(nome);
        if (sheet == null) {
            sheet = workbook.createSheet(nome);
            Row header = sheet.createRow(0);
            for (int i = 0; i < colunas.size(); i++) header.createCell(i).setCellValue(colunas.get(i));
        } else {
            validarCabecalho(sheet, colunas);
        }
        return sheet;
    }

    private static void validarCabecalho(Sheet sheet, List<String> esperado) {
        Row header = sheet.getRow(0);
        if (header == null || header.getLastCellNum() != esperado.size()) {
            throw new PersistenciaVendasException("Cabeçalho incompatível na aba " + sheet.getSheetName() + ".");
        }
        for (int i = 0; i < esperado.size(); i++) {
            if (!esperado.get(i).equals(lerTexto(header, i, esperado.get(i)))) {
                throw new PersistenciaVendasException("Coluna esperada " + esperado.get(i)
                    + " na aba " + sheet.getSheetName() + ".");
            }
        }
    }

    private static List<Row> registros(Sheet sheet) {
        List<Row> resultado = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || linhaVazia(row)) continue;
            if (row.getLastCellNum() > sheet.getRow(0).getLastCellNum()) {
                throw registroInvalido(sheet, row, "colunas extras");
            }
            resultado.add(row);
        }
        return resultado;
    }

    private static void escreverRegistro(Sheet sheet, int id, Object[] valores) {
        Row registro = null;
        for (Row row : registros(sheet)) {
            if (lerInteiro(row, 0, "ID") == id) {
                registro = row;
                break;
            }
        }
        if (registro == null) registro = sheet.createRow(sheet.getLastRowNum() + 1);
        for (int i = 0; i < valores.length; i++) {
            Cell cell = registro.getCell(i, CREATE_NULL_AS_BLANK);
            Object valor = valores[i];
            if (valor instanceof Number numero) cell.setCellValue(numero.doubleValue());
            else if (valor instanceof Boolean booleano) cell.setCellValue(booleano);
            else if (valor != null) cell.setCellValue(valor.toString());
            else cell.setBlank();
        }
    }

    private static void removerItensDaVenda(Sheet sheet, int vendaId) {
        List<Integer> indices = new ArrayList<>();
        for (Row row : registros(sheet)) {
            if (lerInteiro(row, 1, "VENDA_ID") == vendaId) indices.add(row.getRowNum());
        }
        for (int i = indices.size() - 1; i >= 0; i--) {
            int indice = indices.get(i);
            Row linha = sheet.getRow(indice);
            if (linha != null) sheet.removeRow(linha);
            if (indice < sheet.getLastRowNum()) sheet.shiftRows(indice + 1, sheet.getLastRowNum(), -1);
        }
    }

    private static int lerInteiro(Row row, int coluna, String campo) {
        Cell cell = row.getCell(coluna);
        if (cell == null || cell.getCellType() != CellType.NUMERIC) {
            throw registroInvalido(row.getSheet(), row, campo + " deve ser numérico");
        }
        double valor = cell.getNumericCellValue();
        if (!Double.isFinite(valor) || valor < 1 || valor > Integer.MAX_VALUE || valor != Math.rint(valor)) {
            throw registroInvalido(row.getSheet(), row, campo + " deve ser inteiro positivo");
        }
        return (int) valor;
    }

    private static Integer lerInteiroOpcional(Row row, int coluna, String campo) {
        Cell cell = row.getCell(coluna);
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().isEmpty()) return null;
        return lerInteiro(row, coluna, campo);
    }

    private static String lerTexto(Row row, int coluna, String campo) {
        Cell cell = row.getCell(coluna);
        if (cell == null || cell.getCellType() == CellType.BLANK) return "";
        if (cell.getCellType() != CellType.STRING) {
            throw registroInvalido(row.getSheet(), row, campo + " deve ser texto");
        }
        String valor = cell.getStringCellValue();
        if (!valor.equals(valor.trim())) {
            throw registroInvalido(row.getSheet(), row, campo + " contém espaços externos");
        }
        return valor;
    }

    private static boolean lerBooleano(Row row, int coluna, String campo) {
        Cell cell = row.getCell(coluna);
        if (cell == null || cell.getCellType() != CellType.BOOLEAN) {
            throw registroInvalido(row.getSheet(), row, campo + " deve ser booleano");
        }
        return cell.getBooleanCellValue();
    }

    private static BigDecimal lerDecimal(Row row, int coluna, String campo) {
        return decimal(lerTexto(row, coluna, campo), row, campo);
    }

    private static BigDecimal decimal(String valor, Row row, String campo) {
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            throw registroInvalido(row.getSheet(), row, campo + " deve ser decimal", e);
        }
    }

    private static java.time.LocalDateTime data(Row row, int coluna, String campo) {
        try {
            return java.time.LocalDateTime.parse(lerTexto(row, coluna, campo));
        } catch (java.time.format.DateTimeParseException e) {
            throw registroInvalido(row.getSheet(), row, campo + " deve usar data ISO", e);
        }
    }

    private static boolean linhaVazia(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK
                    && !(cell.getCellType() == CellType.STRING && cell.getStringCellValue().isEmpty())) {
                return false;
            }
        }
        return true;
    }

    private static PersistenciaVendasException registroInvalido(Sheet sheet, Row row, String motivo) {
        return new PersistenciaVendasException("Registro inválido na aba " + sheet.getSheetName()
            + ", linha " + (row.getRowNum() + 1) + ": " + motivo + ".");
    }

    private static PersistenciaVendasException registroInvalido(
            Sheet sheet, Row row, String motivo, Throwable causa) {
        return new PersistenciaVendasException("Registro inválido na aba " + sheet.getSheetName()
            + ", linha " + (row.getRowNum() + 1) + ": " + motivo + ".", causa);
    }

    private static String textoOuVazio(String texto) {
        return texto == null ? "" : texto;
    }

    private static String decimalOuVazio(BigDecimal valor) {
        return valor == null ? "" : valor.toPlainString();
    }
}
