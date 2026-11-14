
import lp2.processamento.ProcessadorPlanilhas;
import lp2.processamento.ResultadoProcessamento;


import java.nio.file.Path;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        boolean executando = true;
        

        // Criação de um objeto Vendedor para testes, que herda de Usuário
                        //TESTE
        Vendedor vendedor = new Vendedor(
            1,
            "João Silva",
            "joao.silva",
            "hash123",
            true,
            "V001"
        );

        System.out.println("Nome: " + vendedor.getNome());
        System.out.println("Login: " + vendedor.getLogin());
        System.out.println("Matrícula: " + vendedor.getMatricula());

        Usuario[] usuarios = {
            vendedor,
            new Apoio(2, "Ana Souza", "ana.souza", "hash-ficticio-apoio", true),
            new Administrador(3, "Maria Lima", "maria.lima", "hash-ficticio-adm", true)
        };

        System.out.println("\nDemonstração de capacidades dos perfis (sem autenticação):");
        for (Usuario usuario : usuarios) {
            System.out.println("Usuário: " + usuario.getNome());
            System.out.println("Pode solicitar: " + usuario.podeSolicitarAlteracao());
            System.out.println("Pode aprovar alteração de João: "
                + usuario.podeAprovarAlteracoes(vendedor));
            System.out.println("Pode aprovar alteração própria: "
                + usuario.podeAprovarAlteracoes(usuario));
            System.out.println("Pode gerenciar usuários: " + usuario.podeGerenciarUsuarios());
            System.out.println("Pode gerenciar catálogo: " + usuario.podeGerenciarCatalogo());
        }

        // Testar a validação de matrícula
        try {
            vendedor.setMatricula("   ");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        // Testar a validação de login
        try {
            vendedor.alterarLogin("");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }










        System.out.println("==================================");
        System.out.println("    SISTEMA DE VENDAS - LP2");
        System.out.println("==================================");

        while (executando) {

            System.out.println("\n===== MENU PRINCIPAL =====");
            System.out.println("1 - Login");
            System.out.println("2 - Sobre o sistema");
            System.out.println("3 - Tratamento de planilha");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");

            String opcao = scanner.nextLine();

            switch (opcao) {

                case "1":
                    System.out.println("Login ainda não implementado.");
                    break;

                case "2":
                    System.out.println("Sistema de gestão de vendas");
                    System.out.println("e comissões de vendedores.");
                    break;

                case "3":
                    System.out.println("Iniciando tratamento das planilhas...");

                    ResultadoProcessamento resultado =
                        new ProcessadorPlanilhas(Path.of(".")).executar();

                    if (resultado.getArquivosGerados().isEmpty()) {
                        System.out.println("Nenhum arquivo foi processado.");
                    } else {
                        System.out.println("Processamento finalizado!");
                        System.out.println("Arquivo consolidado:");
                        System.out.println(resultado.getConsolidado());
                    }

                    System.out.println("Arquivos gerados:");
                    resultado.getArquivosGerados().forEach(System.out::println);
                    resultado.getAvisos().forEach(aviso ->
                        System.out.println("Aviso: " + aviso));

                    break;
                case "0":
                    System.out.println("Encerrando o sistema...");
                    executando = false;
                    break;

                default:
                    System.out.println("Opção inválida!");
            }
        }

        scanner.close();
    }
}
