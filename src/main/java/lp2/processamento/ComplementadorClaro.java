package lp2.processamento;

import org.apache.poi.ss.usermodel.Workbook;
import java.io.IOException;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.*;

/** Filtra e copia as abas da base analitico_da_claro/claro.xlsx. */
public final class ComplementadorClaro {
    private static final Map<String,List<String>> COLUNAS = Map.of(
        "virtua", List.of("NM_INDICADOR_NEGOCIO", "NR_CONTRATO", "NM_PARCEIRO"),
        "tv", List.of("NM_INDICADOR_NEGOCIO", "NM_PARCEIRO", "DT"),
        "chip", List.of("DT_ATIVACAO", "CD_COMTA", "CONTRATO_NET"),
        "tel fixo", List.of("NM_INDICADOR_NEGOCIO", "NM_PARCEIRO", "NR_CONTRATO")
    );
    private static String semAcento(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
    public void adicionar(Workbook destino, Path claro, String tipo) throws IOException {
        Map<String,TabelaDados> abas = PlanilhasIO.lerTodasAbas(claro);
        for (Map.Entry<String,TabelaDados> entrada : abas.entrySet()) {
            String nome = entrada.getKey().trim();
            String chave = nome.toLowerCase(Locale.ROOT);
            if (!COLUNAS.containsKey(chave)) continue;
            TabelaDados filtrada = entrada.getValue().selecionarColunas(COLUNAS.get(chave));
            int antes = filtrada.linhas().size();
            if (chave.equals("chip")) {
                String codigo = tipo.equals("RDT") ? "4ZEJ" : "3Z67";
                for (Map<String,Object> linha : filtrada.linhas())
                    linha.put("CONTRATO_NET", NormalizadorContrato.normalizarChip(linha.get("CONTRATO_NET")));
                filtrada.linhas().removeIf(l -> TabelaDados.texto(l.get("CD_COMTA")).equals(codigo));
            } else {
                String parceiroExcluir = tipo.equals("RDT") ? "VNA TELECOM LTDA ME" : "RDT TELECOM LTDA";
                filtrada.linhas().removeIf(l -> {
                    String indicador = semAcento(TabelaDados.texto(l.get("NM_INDICADOR_NEGOCIO")));
                    String parceiro = TabelaDados.texto(l.get("NM_PARCEIRO"));
                    return indicador.equals("desistencia") || indicador.equals("venda bruta") || parceiro.equals(parceiroExcluir);
                });
            }
            System.out.printf("Base complementar %s: %d excluídas%n", nome, antes-filtrada.linhas().size());
            PlanilhasIO.escreverTabela(destino, nome, filtrada, false);
        }
    }
}
