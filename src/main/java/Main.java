
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

        System.out.println(
            "Pode aprovar: " + vendedor.podeAprovarAlteracoes()
        );

        System.out.println(
            "Pode solicitar: " + vendedor.podeSolicitarAlteracao()
        );

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
