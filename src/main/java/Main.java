import lp2.processamento.ProcessadorPlanilhas;
import lp2.processamento.ResultadoProcessamento;
import lp2.processamento.ProcessamentoException;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        RepositorioUsuariosExcel repositorio = args.length == 0
            ? new RepositorioUsuariosExcel()
            : new RepositorioUsuariosExcel(Path.of(args[0]));
        ServicoAutenticacao autenticacao = new ServicoAutenticacao(repositorio);
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
                    case "0":
                        System.out.println("Encerrando o sistema...");
                        executando = false;
                        break;
                    default:
                        System.out.println("Opção inválida!");
                }
            } catch (IllegalArgumentException | PersistenciaUsuariosException | ProcessamentoException e) {
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
}