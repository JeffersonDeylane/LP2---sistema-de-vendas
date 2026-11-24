package lp2.processamento;

import org.apache.poi.ss.usermodel.Workbook;
import java.text.Collator;
import java.util.*;

/** Gera o resumo estático por supervisor e vendedor equivalente à Planilha1 do Python. */
public final class GeradorRelatorios {
    private static final List<String> CAMPOS = List.of("VIRTUA", "CHIP", "PORT", "TV", "SOMA", "Valor");

    public void criar(Workbook wb, TabelaDados analitico) {
        resumoSupervisorVendedor(wb, analitico);
    }
    private void resumoSupervisorVendedor(Workbook wb, TabelaDados dados) {
        Map<Grupo,double[]> acumulado = new LinkedHashMap<>();
        for (Map<String,Object> linha : dados.linhas()) {
            String supervisor = TabelaDados.texto(linha.get("Supervisor"));
            String vendedor = TabelaDados.texto(linha.get("Vendedor"));
            Grupo chave = new Grupo(supervisor, vendedor);
            double[] valores = acumulado.computeIfAbsent(chave, k -> new double[CAMPOS.size()]);
            for (int i=0;i<CAMPOS.size();i++) valores[i] += TabelaDados.numero(linha.get(CAMPOS.get(i)));
        }
        List<Map.Entry<Grupo,double[]>> ordem = new ArrayList<>(acumulado.entrySet());
        Collator ordemAlfabetica = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
        ordem.sort(Map.Entry.comparingByKey(
                Comparator.comparing(Grupo::supervisor, ordemAlfabetica)
                        .thenComparing(Grupo::vendedor, ordemAlfabetica)));
        List<String> colunas = new ArrayList<>(List.of("Supervisor", "Vendedor"));
        CAMPOS.forEach(c -> colunas.add("Soma de " + c));
        TabelaDados saida = new TabelaDados(colunas);
        for (Map.Entry<Grupo,double[]> item : ordem) {
            Grupo chave = item.getKey();
            Map<String,Object> linha = new LinkedHashMap<>();
            linha.put("Supervisor", chave.supervisor());
            linha.put("Vendedor", chave.vendedor());
            for (int i=0;i<CAMPOS.size();i++) linha.put("Soma de " + CAMPOS.get(i), item.getValue()[i]);
            saida.adicionarLinha(linha);
        }
        PlanilhasIO.escreverTabela(wb, "Planilha1", saida, false);
    }

    private record Grupo(String supervisor, String vendedor) {}
}
