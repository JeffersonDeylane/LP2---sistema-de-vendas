package lp2.processamento;

import java.util.*;

/** Tabela tabular em memória: cabeçalhos ordenados e linhas independentes. */
public final class TabelaDados {
    private final List<String> colunas = new ArrayList<>();
    private final List<Map<String, Object>> linhas = new ArrayList<>();

    public TabelaDados(List<String> cabecalhos) {
        for (String c : cabecalhos) adicionarColuna(c == null ? "" : c.trim(), null);
    }
    public List<String> colunas() { return Collections.unmodifiableList(colunas); }
    public List<Map<String,Object>> linhas() { return linhas; }
    public boolean temColuna(String coluna) { return colunas.contains(coluna); }
    public int indice(String coluna) { return colunas.indexOf(coluna); }
    public void adicionarLinha(Map<String,Object> linha) { linhas.add(new LinkedHashMap<>(linha)); }
    public void adicionarColuna(String coluna, Object valorPadrao) {
        if (colunas.contains(coluna)) return;
        colunas.add(coluna);
        linhas.forEach(l -> l.put(coluna, valorPadrao));
    }
    public void inserirColuna(int indice, String coluna, Object valorPadrao) {
        if (colunas.contains(coluna)) return;
        colunas.add(Math.max(0, Math.min(indice, colunas.size())), coluna);
        linhas.forEach(l -> l.put(coluna, valorPadrao));
    }
    public void removerColuna(String coluna) {
        colunas.remove(coluna);
        linhas.forEach(l -> l.remove(coluna));
    }
    public static String texto(Object valor) { return valor == null ? "" : valor.toString().trim(); }
    public static int inteiro(Object valor) {
        if (valor instanceof Number n) return n.intValue();
        try { return (int) Double.parseDouble(texto(valor)); } catch (NumberFormatException ex) { return 0; }
    }
    public static double numero(Object valor) {
        if (valor instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(texto(valor).replace(",", ".")); } catch (NumberFormatException ex) { return 0; }
    }
    public TabelaDados selecionarColunas(List<String> selecao) {
        TabelaDados t = new TabelaDados(selecao);
        for (Map<String,Object> linha : linhas) {
            Map<String,Object> nova = new LinkedHashMap<>();
            for (String c : selecao) nova.put(c, linha.get(c));
            t.adicionarLinha(nova);
        }
        return t;
    }
}
