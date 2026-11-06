
import java.util.Scanner;

public class Main {

    public static void Main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        boolean executando = true;

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
