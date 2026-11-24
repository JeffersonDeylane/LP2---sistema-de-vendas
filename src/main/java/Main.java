import lp2.processamento.ProcessadorPlanilhas;
import lp2.processamento.ResultadoProcessamento;
import lp2.processamento.ProcessamentoException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Path arquivoBase = args.length == 0 ? Path.of("dados", "BASE_GERAL.xlsx") : Path.of(args[0]);
        RepositorioUsuariosExcel repositorio = new RepositorioUsuariosExcel(arquivoBase);
        ServicoUsuarios servicoUsuarios = new ServicoUsuarios(repositorio);
        ServicoAutenticacao autenticacao = new ServicoAutenticacao(repositorio);
        ServicoVendas servicoVendas = new ServicoVendas(
            new RepositorioVendasExcel(arquivoBase), repositorio);
        try {
            System.out.println("Usuários persistidos carregados: " + repositorio.listar().size());
        } catch (PersistenciaUsuariosException e) {
            System.err.println("Falha ao carregar usuários: " + e.getMessage());
            return;
        }
        Usuario usuarioAutenticado = null;
        Scanner scanner = new Scanner(System.in);
        boolean executando = true;
        System.out.println("==================================");
        System.out.println("    SISTEMA DE VENDAS - LP2");
        System.out.println("==================================");
        while (executando) {
            System.out.println("\n===== MENU PRINCIPAL =====");
            System.out.println("1 - Login");
            System.out.println("2 - Sobre o sistema");
            System.out.println("3 - Tratamento de planilha");
            if (usuarioAutenticado != null && usuarioAutenticado.podeGerenciarUsuarios()) {
                System.out.println("4 - Cadastrar usuário");
            }
            System.out.println("5 - Sair da conta");
            if (usuarioAutenticado instanceof Vendedor || usuarioAutenticado instanceof Administrador) {
                System.out.println("6 - Cadastrar cliente");
                if (usuarioAutenticado instanceof Administrador) {
                    System.out.println("7 - Cadastrar produto");
                }
                System.out.println("8 - Cadastrar contrato");
                System.out.println("9 - Registrar venda em rascunho");
                System.out.println("10 - Confirmar venda");
                System.out.println(usuarioAutenticado instanceof Administrador
                    ? "11 - Consultar vendas" : "11 - Consultar minhas vendas");
            } else if (usuarioAutenticado instanceof Apoio) {
                System.out.println("11 - Consultar vendas da equipe");
            }
            if (usuarioAutenticado != null && usuarioAutenticado.podeGerenciarUsuarios()) {
                System.out.println("12 - Listar usuários");
                System.out.println("13 - Editar usuário");
                System.out.println("14 - Ativar ou desativar usuário");
            }
            System.out.println("0 - Sair");
            if (usuarioAutenticado != null) {
                System.out.println("Conectado: " + usuarioAutenticado.getNome());
            }
            System.out.print("Escolha uma opção: ");
            if (!scanner.hasNextLine()) break;
            String opcao = scanner.nextLine().trim();
            try {
                switch (opcao) {
                    case "1":
                        if (usuarioAutenticado != null) {
                            System.out.println("Saia da conta antes de entrar com outro usuário.");
                            break;
                        }
                        String login = lerCampo(scanner, "Login: ");
                        String senha = lerCampo(scanner, "Senha: ");
                        usuarioAutenticado = autenticacao.autenticar(login, senha);
                        System.out.println("Bem-vindo, " + usuarioAutenticado.getNome() + "!");
                        break;
                    case "2":
                        System.out.println("Sistema de gestão de vendas");
                        System.out.println("e comissões de vendedores.");
                        break;
                    case "3":
                        if (usuarioAutenticado == null) {
                            System.out.println("Faça login para tratar as planilhas.");
                            break;
                        }
                        System.out.println("Iniciando tratamento das planilhas...");
                        ResultadoProcessamento resultado = new ProcessadorPlanilhas(Path.of(".")).executar();
                        if (resultado.getArquivosGerados().isEmpty()) {
                            System.out.println("Nenhum arquivo foi processado.");
                        } else {
                            System.out.println("Processamento finalizado!");
                            System.out.println("Arquivo consolidado:");
                            System.out.println(resultado.getConsolidado());
                        }
                        System.out.println("Arquivos gerados:");
                        resultado.getArquivosGerados().forEach(System.out::println);
                        resultado.getAvisos().forEach(aviso -> System.out.println("Aviso: " + aviso));
                        break;
                    case "4":
                        if (usuarioAutenticado == null || !usuarioAutenticado.podeGerenciarUsuarios()) {
                            System.out.println("Apenas um administrador pode cadastrar usuários.");
                            break;
                        }
                        System.out.println("Perfil: 1 - Vendedor, 2 - Apoio, 3 - Administrador");
                        String opcaoPerfil = lerCampo(scanner, "Escolha o perfil: ").trim();
                        String perfil = switch (opcaoPerfil) {
                            case "1" -> "VENDEDOR";
                            case "2" -> "APOIO";
                            case "3" -> "ADMINISTRADOR";
                            default -> throw new IllegalArgumentException("Perfil inválido.");
                        };
                        String nome = lerCampo(scanner, "Nome: ");
                        String novoLogin = lerCampo(scanner, "Login: ");
                        String novaSenha = lerCampo(scanner, "Senha: ");
                        String matricula = perfil.equals("VENDEDOR")
                            ? lerCampo(scanner, "Matrícula: ") : "";
                        autenticacao.cadastrar(usuarioAutenticado, perfil,
                            nome, novoLogin, novaSenha, matricula);
                        System.out.println("Cadastro salvo! Use a opção 1 para entrar.");
                        break;
                    case "5":
                        usuarioAutenticado = null;
                        System.out.println("Você saiu da conta.");
                        break;
                    case "6":
                        exigirAcessoVendas(usuarioAutenticado);
                        Cliente cliente = servicoVendas.cadastrarCliente(usuarioAutenticado,
                            lerCampo(scanner, "Identificação do cliente: "),
                            lerCampo(scanner, "Nome do cliente: "));
                        System.out.println("Cliente cadastrado com ID " + cliente.getId() + ".");
                        break;
                    case "7":
                        exigirAdministrador(usuarioAutenticado);
                        Produto produto = servicoVendas.cadastrarProduto(usuarioAutenticado,
                            lerCampo(scanner, "Código: "),
                            lerCampo(scanner, "Nome: "),
                            lerCampo(scanner, "Categoria: "),
                            lerDecimal(scanner, "Preço unitário (ex.: 10,50): "),
                            lerCampo(scanner, "Unidade (vazio para item indivisível): "),
                            lerSimNao(scanner, "Permite quantidade fracionada? (s/n): "));
                        System.out.println("Produto cadastrado com ID " + produto.getId() + ".");
                        break;
                    case "8":
                        exigirAcessoVendas(usuarioAutenticado);
                        List<Cliente> clientesParaContrato =
                            servicoVendas.listarClientesAtivos(usuarioAutenticado);
                        if (clientesParaContrato.isEmpty()) {
                            System.out.println("Cadastre um cliente antes de cadastrar um contrato.");
                            break;
                        }
                        listarClientes(clientesParaContrato);
                        Contrato contrato = servicoVendas.cadastrarContrato(usuarioAutenticado,
                            lerInteiro(scanner, "ID do cliente: "),
                            lerCampo(scanner, "Número do contrato: "));
                        System.out.println("Contrato cadastrado com ID " + contrato.getId() + ".");
                        break;
                    case "9":
                        exigirAcessoVendas(usuarioAutenticado);
                        registrarVenda(scanner, servicoVendas, usuarioAutenticado);
                        break;
                    case "10":
                        exigirAcessoVendas(usuarioAutenticado);
                        List<Venda> rascunhos = servicoVendas.listarVendas(usuarioAutenticado).stream()
                            .filter(venda -> venda.getEstado() == EstadoVenda.RASCUNHO)
                            .toList();
                        if (rascunhos.isEmpty()) {
                            System.out.println("Não há vendas em rascunho para confirmar.");
                            break;
                        }
                        listarVendas(rascunhos);
                        Venda confirmada = servicoVendas.confirmar(usuarioAutenticado,
                            lerInteiro(scanner, "ID da venda a confirmar: "));
                        System.out.println("Venda " + confirmada.getId() + " confirmada. Total: R$ "
                            + confirmada.getTotal().toPlainString());
                        break;
                    case "11":
                        listarVendas(servicoVendas.listarVendas(usuarioAutenticado));
                        break;
                    case "12":
                        for (Usuario usuario : servicoUsuarios.listar(usuarioAutenticado)) {
                            System.out.println(usuario.getId() + " - " + usuario.getNome()
                                + " (" + usuario.getLogin() + ") - "
                                + usuario.getClass().getSimpleName() + " - "
                                + (usuario.isAtivo() ? "Ativo" : "Inativo"));
                        }
                        break;
                    case "13":
                        servicoUsuarios.exigirAdministrador(usuarioAutenticado);
                        Usuario editado = servicoUsuarios.alterar(usuarioAutenticado,
                            lerInteiro(scanner, "ID do usuário: "),
                            lerCampo(scanner, "Novo nome: "), lerCampo(scanner, "Novo login: "));
                        if (editado.getId() == usuarioAutenticado.getId()) usuarioAutenticado = editado;
                        System.out.println("Usuário atualizado.");
                        break;
                    case "14":
                        servicoUsuarios.exigirAdministrador(usuarioAutenticado);
                        Usuario atualizado = servicoUsuarios.alterarSituacao(usuarioAutenticado,
                            lerInteiro(scanner, "ID do usuário: "),
                            lerSimNao(scanner, "Manter a conta ativa? (s/n): "));
                        System.out.println(atualizado.isAtivo() ? "Usuário ativo." : "Usuário inativo.");
                        if (atualizado.getId() == usuarioAutenticado.getId() && !atualizado.isAtivo()) {
                            usuarioAutenticado = null;
                            System.out.println("Sessão encerrada.");
                        }
                        break;
                    case "0":
                        System.out.println("Encerrando o sistema...");
                        executando = false;
                        break;
                    default:
                        System.out.println("Opção inválida!");
                }
            } catch (IllegalArgumentException | PersistenciaUsuariosException
                     | PersistenciaVendasException | ProcessamentoException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (java.util.NoSuchElementException e) {
                executando = false;
            }
        }
        scanner.close();
    }
    private static String lerCampo(Scanner scanner, String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine();
    }

    private static void registrarVenda(Scanner scanner, ServicoVendas servico, Usuario usuario) {
        List<Cliente> clientes = servico.listarClientesAtivos(usuario);
        if (clientes.isEmpty()) {
            throw new IllegalArgumentException("Cadastre um cliente antes de registrar uma venda.");
        }
        List<Produto> produtos = servico.listarProdutosAtivos(usuario);
        if (produtos.isEmpty()) {
            throw new IllegalArgumentException("Não há produtos ativos cadastrados.");
        }
        listarClientes(clientes);
        int clienteId = lerInteiro(scanner, "ID do cliente: ");
        String numeroContrato = lerCampo(scanner, "Número do contrato (vazio para venda avulsa): ");
        System.out.println("Produtos disponíveis:");
        for (Produto produto : produtos) {
            System.out.println(produto.getId() + " - " + produto.getNome()
                + " (" + produto.getCodigo() + ") R$ " + produto.getPreco().toPlainString()
                + (produto.getUnidade() == null ? "" : " / " + produto.getUnidade()));
        }
        List<ServicoVendas.ItemSolicitado> itens = new ArrayList<>();
        while (true) {
            int produtoId = lerInteiro(scanner, "ID do produto (0 para finalizar itens): ", true);
            if (produtoId == 0) break;
            itens.add(new ServicoVendas.ItemSolicitado(produtoId,
                lerDecimal(scanner, "Quantidade: ")));
        }
        Venda venda = servico.criarRascunho(usuario, clienteId, numeroContrato, itens);
        System.out.println("Venda " + venda.getId() + " salva em rascunho. Confirme pela opção 10.");
    }

    private static void listarClientes(List<Cliente> clientes) {
        System.out.println("Clientes disponíveis:");
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }
        for (Cliente cliente : clientes) {
            System.out.println(cliente.getId() + " - " + cliente.getNome()
                + " (" + cliente.getIdentificacao() + ")");
        }
    }

    private static void listarVendas(List<Venda> vendas) {
        if (vendas.isEmpty()) {
            System.out.println("Nenhuma venda encontrada.");
            return;
        }
        for (Venda venda : vendas) {
            System.out.println("Venda " + venda.getId() + " - " + venda.getEstado()
                + " - cliente " + venda.getClienteId() + " - " + venda.getItens().size()
                + " item(ns) - total R$ " + venda.getTotal().toPlainString());
        }
    }

    private static int lerInteiro(Scanner scanner, String mensagem) {
        return lerInteiro(scanner, mensagem, false);
    }

    private static int lerInteiro(Scanner scanner, String mensagem, boolean permitirZero) {
        String valor = lerCampo(scanner, mensagem).trim();
        try {
            int inteiro = Integer.parseInt(valor);
            if (inteiro < (permitirZero ? 0 : 1)) {
                throw new NumberFormatException();
            }
            return inteiro;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(permitirZero
                ? "Informe um ID inteiro não negativo." : "Informe um ID inteiro positivo.");
        }
    }

    private static BigDecimal lerDecimal(Scanner scanner, String mensagem) {
        String valor = lerCampo(scanner, mensagem).trim().replace(',', '.');
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe um valor numérico válido.");
        }
    }

    private static boolean lerSimNao(Scanner scanner, String mensagem) {
        String resposta = lerCampo(scanner, mensagem).trim();
        if (resposta.equalsIgnoreCase("s") || resposta.equalsIgnoreCase("sim")) return true;
        if (resposta.equalsIgnoreCase("n") || resposta.equalsIgnoreCase("nao")
                || resposta.equalsIgnoreCase("não")) return false;
        throw new IllegalArgumentException("Responda s ou n.");
    }

    private static void exigirAcessoVendas(Usuario usuario) {
        if (!(usuario instanceof Vendedor) && !(usuario instanceof Administrador)) {
            throw new IllegalArgumentException("Faça login como vendedor ou administrador.");
        }
    }

    private static void exigirAdministrador(Usuario usuario) {
        if (!(usuario instanceof Administrador)) {
            throw new IllegalArgumentException("Apenas um administrador pode cadastrar produtos.");
        }
    }
}