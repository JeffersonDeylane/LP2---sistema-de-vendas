package lp2.processamento;

import org.apache.poi.ss.usermodel.*;
import java.util.*;

/** Marca contratos exclusivos de cada fonte com fonte vermelha ou verde. */
public final class CruzadorContratos {
    public void cruzar(Workbook wb, TabelaDados analitico, String nomeAba, String flag, String colunaContrato, String colunaSaida) {
        Set<String> contratosAnalitico = new TreeSet<>();
        for (Map<String,Object> linha : analitico.linhas()) {
            if (TabelaDados.inteiro(linha.get(flag)) == 1) {
                String contrato = NormalizadorContrato.normalizar(linha.get("Contrato"));
                if (!contrato.isEmpty()) contratosAnalitico.add(contrato);
            }
        }
        Sheet aba = null;
        for (Sheet candidata : wb) if (candidata.getSheetName().trim().equalsIgnoreCase(nomeAba)) aba = candidata;
        if (aba == null) { System.out.println("Aviso: aba não encontrada: " + nomeAba); return; }
        Row header = aba.getRow(0);
        if (header == null) return;
        int contratoIndex = -1;
        for (Cell celula : header) if (celula.getStringCellValue().equals(colunaContrato)) contratoIndex = celula.getColumnIndex();
        if (contratoIndex < 0) { System.out.println("Aviso: coluna " + colunaContrato + " ausente em " + nomeAba); return; }
        int saidaIndex = header.getLastCellNum();
        header.createCell(saidaIndex).setCellValue(colunaSaida);
        header.createCell(saidaIndex+1).setCellValue("olhar");
        DataFormatter formatador = new DataFormatter();
        Set<String> contratosBase = new HashSet<>();
        LinkedHashMap<String,Boolean> divergencias = new LinkedHashMap<>();
        CellStyle fonteVermelha = wb.createCellStyle();
        Font vermelho = wb.createFont(); vermelho.setColor(IndexedColors.RED.getIndex()); fonteVermelha.setFont(vermelho);
        CellStyle fonteVerde = wb.createCellStyle();
        Font verde = wb.createFont(); verde.setColor(IndexedColors.GREEN.getIndex()); fonteVerde.setFont(verde);
        for (int i = 1; i <= aba.getLastRowNum(); i++) {
            Row linha = aba.getRow(i); if (linha == null) continue;
            Cell origem = linha.getCell(contratoIndex);
            String contrato = NormalizadorContrato.normalizar(origem == null ? "" : formatador.formatCellValue(origem));
            if (contrato.isEmpty()) continue;
            contratosBase.add(contrato);
            if (contratosAnalitico.contains(contrato)) linha.createCell(saidaIndex).setCellValue(contrato);
            else { origem.setCellStyle(fonteVermelha); divergencias.put(contrato, false); }
        }
        int adicionados = 0;
        int prox = aba.getLastRowNum() + 1;
        for (String contrato : contratosAnalitico) {
            if (contratosBase.contains(contrato)) continue;
            Row linha = aba.createRow(prox++);
            Cell c = linha.createCell(contratoIndex); c.setCellValue(contrato); c.setCellStyle(fonteVerde);
            linha.createCell(saidaIndex).setCellValue(contrato);
            divergencias.put(contrato, true); adicionados++;
        }
        int i=1;
        for (Map.Entry<String,Boolean> divergencia : divergencias.entrySet()) {
            Row linha = aba.getRow(i);
            if (linha == null) linha = aba.createRow(i);
            Cell c = linha.createCell(saidaIndex+1);
            c.setCellValue(divergencia.getKey()); c.setCellStyle(divergencia.getValue() ? fonteVerde : fonteVermelha);
            i++;
        }
        aba.setColumnWidth(saidaIndex+1, 18*256);
        System.out.printf("Cruzamento %s: %d só no IMPERIUM, %d só no Analítico%n", nomeAba, contratosBase.stream().filter(c->!contratosAnalitico.contains(c)).count(), adicionados);
    }
}
