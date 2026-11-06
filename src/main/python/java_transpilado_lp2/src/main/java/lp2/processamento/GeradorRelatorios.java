package lp2.processamento;

import org.apache.poi.ss.usermodel.Workbook;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Relatórios agregados equivalentes em dados aos dois pivôs Python.
 *  Atenção: são abas de RESUMO, não objetos de tabela dinâmica do Excel. */
public final class GeradorRelatorios {
    private static final List<String> CAMPOS = List.of("VIRTUA", "CHIP", "PORT", "TV", "SOMA", "Valor");

    public void criar(Workbook wb, TabelaDados analitico) {
        resumoSupervisorVendedor(wb, analitico);
        resumoVendedorData(wb, analitico);
    }
    private void resumoSupervisorVendedor(Workbook wb, TabelaDados dados) {
        Map<String,double[]> acumulado = new LinkedHashMap<>();
        for (Map<String,Object> linha : dados.linhas()) {
            String supervisor = TabelaDados.texto(linha.get("Supervisor"));
            String vendedor = TabelaDados.texto(linha.get("Vendedor"));
            String chave = supervisor + "\u0001" + vendedor;
            double[] valores = acumulado.computeIfAbsent(chave, k -> new double[CAMPOS.size()]);
            for (int i=0;i<CAMPOS.size();i++) valores[i] += TabelaDados.numero(linha.get(CAMPOS.get(i)));
        }
        List<Map.Entry<String,double[]>> ordem = new ArrayList<>(acumulado.entrySet());
        ordem.sort(Comparator.<Map.Entry<String,double[]>>comparingDouble(e -> e.getValue()[5]).reversed());
        List<String> colunas = new ArrayList<>(List.of("Supervisor", "Vendedor"));
        CAMPOS.forEach(c -> colunas.add("Soma de " + c));
        TabelaDados saida = new TabelaDados(colunas);
        for (Map.Entry<String,double[]> item : ordem) {
            String[] chave = item.getKey().split("\u0001", -1);
            Map<String,Object> linha = new LinkedHashMap<>();
            linha.put("Supervisor", chave[0]); linha.put("Vendedor", chave[1]);
            for (int i=0;i<CAMPOS.size();i++) linha.put("Soma de " + CAMPOS.get(i), item.getValue()[i]);
            saida.adicionarLinha(linha);
        }
        PlanilhasIO.escreverTabela(wb, "Planilha1", saida, false);
    }
    private void resumoVendedorData(Workbook wb, TabelaDados dados) {
        Map<String,Map<String,Integer>> porVendedor = new TreeMap<>();
        Set<String> datas = new TreeSet<>();
        for (Map<String,Object> linha : dados.linhas()) {
            String vendedor = TabelaDados.texto(linha.get("Vendedor"));
            String data = dataChave(linha.get("DataInstalacao"));
            if (vendedor.isEmpty() || data.isEmpty()) continue;
            datas.add(data);
            porVendedor.computeIfAbsent(vendedor, k -> new TreeMap<>()).merge(data, 1, Integer::sum);
        }
        List<String> colunas = new ArrayList<>(); colunas.add("Vendedor"); colunas.addAll(datas); colunas.add("Total");
        TabelaDados saida = new TabelaDados(colunas);
        for (Map.Entry<String,Map<String,Integer>> entrada : porVendedor.entrySet()) {
            Map<String,Object> linha = new LinkedHashMap<>(); linha.put("Vendedor", entrada.getKey());
            int total=0;
            for (String d : datas) { int n = entrada.getValue().getOrDefault(d,0); linha.put(d,n); total+=n; }
            linha.put("Total", total); saida.adicionarLinha(linha);
        }
        PlanilhasIO.escreverTabela(wb, "Planilha2", saida, false);
    }
    private String dataChave(Object valor) {
        if (valor == null) return "";
        if (valor instanceof LocalDateTime dt) return dt.toLocalDate().toString();
        if (valor instanceof LocalDate d) return d.toString();
        if (valor instanceof java.util.Date d) return new java.text.SimpleDateFormat("yyyy-MM-dd").format(d);
        String s = TabelaDados.texto(valor);
        if (s.isEmpty()) return "";
        for (DateTimeFormatter f : List.of(DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ISO_LOCAL_DATE)) {
            try { return LocalDate.parse(s.substring(0, Math.min(10, s.length())), f).toString(); }
            catch (Exception ignored) {}
        }
        return s;
    }
}
