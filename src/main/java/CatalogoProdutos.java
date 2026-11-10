import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Lê o catálogo Excel; nunca modifica nem salva a planilha. */
public final class CatalogoProdutos {
    private static final Path ARQUIVO = Path.of("dados", "catalogo_produtos.xlsx");

    private CatalogoProdutos() { }

    public static Map<String, List<String>> listarProdutos() throws IOException {
        Map<String, List<String>> catalogo = new LinkedHashMap<>();
        DataFormatter formatador = new DataFormatter();

        try (InputStream entrada = Files.newInputStream(ARQUIVO);
             Workbook arquivo = new XSSFWorkbook(entrada)) {
            Sheet aba = arquivo.getSheet("CATALOGO");
            if (aba == null) {
                throw new IOException("Aba CATALOGO não encontrada em " + ARQUIVO);
            }
            for (int i = 1; i <= aba.getLastRowNum(); i++) {
                Row linha = aba.getRow(i);
                if (linha == null) continue;
                String tipo = formatador.formatCellValue(linha.getCell(0)).trim().toUpperCase();
                String produto = formatador.formatCellValue(linha.getCell(1)).trim();
                if (!tipo.isEmpty() && !produto.isEmpty()) {
                    catalogo.computeIfAbsent(tipo, k -> new ArrayList<>()).add(produto);
                }
            }
        }
        return catalogo;
    }

    /** Exibe opções e devolve escolhas em memória. Nada é gravado no Excel. */
    public static List<String> adicionarProduto(Scanner scanner) throws IOException {
        Map<String, List<String>> catalogo = listarProdutos();
        List<String> escolhas = new ArrayList<>();
        if (catalogo.isEmpty()) {
            System.out.println("Nenhum produto cadastrado na planilha.");
            return escolhas;
        }

        List<String> tipos = new ArrayList<>(catalogo.keySet());
        while (true) {
            System.out.println("\n===== TIPOS DE PRODUTO =====");
            for (int i = 0; i < tipos.size(); i++) {
                System.out.println((i + 1) + " - " + tipos.get(i));
            }
            System.out.println("0 - Finalizar");
            System.out.print("Escolha o tipo: ");
            int escolhaTipo = lerNumero(scanner);
            if (escolhaTipo == 0) break;
            if (escolhaTipo < 1 || escolhaTipo > tipos.size()) {
                System.out.println("Tipo inválido.");
                continue;
            }

            String tipo = tipos.get(escolhaTipo - 1);
            List<String> produtos = catalogo.get(tipo);
            System.out.println("\n===== " + tipo + " =====");
            for (int i = 0; i < produtos.size(); i++) {
                System.out.println((i + 1) + " - " + produtos.get(i));
            }
            System.out.println("0 - Voltar");
            System.out.print("Escolha o produto: ");
            int escolhaProduto = lerNumero(scanner);
            if (escolhaProduto == 0) continue;
            if (escolhaProduto < 1 || escolhaProduto > produtos.size()) {
                System.out.println("Produto inválido.");
                continue;
            }
            String selecionado = tipo + " -> " + produtos.get(escolhaProduto - 1);
            escolhas.add(selecionado);
            System.out.println("Selecionado: " + selecionado);
        }
        return escolhas;
    }

    private static int lerNumero(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
