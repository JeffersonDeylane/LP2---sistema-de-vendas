package lp2.processamento;

import org.apache.poi.ss.usermodel.*;
import java.util.*;

/** Compara os contratos das fontes e exporta a lista de contratos de TV do Analítico. */
public final class CruzadorContratos {
    public void listarTvAnalitico(Workbook wb, TabelaDados analitico) {
        Sheet aba = encontrarAba(wb, "tv");
        if (aba == null) return;

        int coluna = 0;
        for (Row linha : aba) coluna = Math.max(coluna, linha.getLastCellNum());
        Row cabecalho = aba.getRow(0);
        if (cabecalho == null) cabecalho = aba.createRow(0);
        cabecalho.createCell(coluna).setCellValue("tv_analitico");

        int indiceLinha = 1;
        for (Map<String, Object> dados : analitico.linhas()) {
            if (TabelaDados.numero(dados.get("TV")) != 1) continue;
            String contrato = NormalizadorContrato.normalizar(dados.get("Contrato"));
            if (contrato.isEmpty()) continue;

            Row linha = aba.getRow(indiceLinha);
            if (linha == null) linha = aba.createRow(indiceLinha);
            linha.createCell(coluna).setCellValue(contrato);
            indiceLinha++;
        }
    }

    public void cruzar(Workbook wb, TabelaDados analitico, String nomeAba, String flag, String colunaContrato, String colunaSaida) {
        Set<String> contratosAnalitico = new TreeSet<>();
        for (Map<String,Object> linha : analitico.linhas()) {
            if (TabelaDados.inteiro(linha.get(flag)) == 1) {
                String contrato = NormalizadorContrato.normalizar(linha.get("Contrato"));
                if (!contrato.isEmpty()) contratosAnalitico.add(contrato);
            }
        }
        Sheet aba = encontrarAba(wb, nomeAba);
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
        Map<Short,CellStyle> estilosVermelhos = new HashMap<>();
        Map<Short,CellStyle> estilosVerdes = new HashMap<>();
        for (int i = 1; i <= aba.getLastRowNum(); i++) {
            Row linha = aba.getRow(i); if (linha == null) continue;
            Cell origem = linha.getCell(contratoIndex);
            String contrato = normalizarCelulaContrato(origem, formatador);
            if (contrato.isEmpty()) continue;
            contratosBase.add(contrato);
            if (contratosAnalitico.contains(contrato)) linha.createCell(saidaIndex).setCellValue(contrato);
            else {
                origem.setCellStyle(estiloComFonte(wb, origem, IndexedColors.RED, estilosVermelhos));
                divergencias.put(contrato, false);
            }
        }
        int adicionados = 0;
        int prox = aba.getLastRowNum() + 1;
        for (String contrato : contratosAnalitico) {
            if (contratosBase.contains(contrato)) continue;
            Row linha = aba.createRow(prox++);
            Cell c = linha.createCell(contratoIndex);
            c.setCellValue(contrato);
            c.setCellStyle(estiloComFonte(wb, c, IndexedColors.GREEN, estilosVerdes));
            linha.createCell(saidaIndex).setCellValue(contrato);
            divergencias.put(contrato, true); adicionados++;
        }
        int i=1;
        for (Map.Entry<String,Boolean> divergencia : divergencias.entrySet()) {
            Row linha = aba.getRow(i);
            if (linha == null) linha = aba.createRow(i);
            Cell c = linha.createCell(saidaIndex+1);
            c.setCellValue(divergencia.getKey());
            c.setCellStyle(divergencia.getValue()
                    ? estiloComFonte(wb, c, IndexedColors.GREEN, estilosVerdes)
                    : estiloComFonte(wb, c, IndexedColors.RED, estilosVermelhos));
            i++;
        }
        aba.setColumnWidth(saidaIndex+1, 18*256);
        System.out.printf("Cruzamento %s: %d só no IMPERIUM, %d só no Analítico%n", nomeAba, contratosBase.stream().filter(c->!contratosAnalitico.contains(c)).count(), adicionados);
    }

    private Sheet encontrarAba(Workbook wb, String nomeAba) {
        for (Sheet aba : wb) {
            if (aba.getSheetName().trim().equalsIgnoreCase(nomeAba)) return aba;
        }
        return null;
    }

    private CellStyle estiloComFonte(Workbook wb, Cell celula, IndexedColors cor,
                                     Map<Short, CellStyle> estilos) {
        short estiloOriginal = celula.getCellStyle().getIndex();
        return estilos.computeIfAbsent(estiloOriginal, indice -> {
            CellStyle estilo = wb.createCellStyle();
            estilo.cloneStyleFrom(wb.getCellStyleAt(indice));
            Font fonteOriginal = wb.getFontAt(estilo.getFontIndexAsInt());
            Font fonteColorida = wb.createFont();
            fonteColorida.setFontName(fonteOriginal.getFontName());
            fonteColorida.setFontHeight(fonteOriginal.getFontHeight());
            fonteColorida.setBold(fonteOriginal.getBold());
            fonteColorida.setItalic(fonteOriginal.getItalic());
            fonteColorida.setStrikeout(fonteOriginal.getStrikeout());
            fonteColorida.setTypeOffset(fonteOriginal.getTypeOffset());
            fonteColorida.setUnderline(fonteOriginal.getUnderline());
            fonteColorida.setCharSet(fonteOriginal.getCharSet());
            fonteColorida.setColor(cor.getIndex());
            estilo.setFont(fonteColorida);
            return estilo;
        });
    }

    private String normalizarCelulaContrato(Cell celula, DataFormatter formatador) {
        if (celula == null) return "";
        CellType tipo = celula.getCellType() == CellType.FORMULA
                ? celula.getCachedFormulaResultType()
                : celula.getCellType();
        if (tipo == CellType.NUMERIC) {
            return NormalizadorContrato.normalizar(celula.getNumericCellValue());
        }
        if (tipo == CellType.STRING) {
            return NormalizadorContrato.normalizar(celula.getStringCellValue());
        }
        return NormalizadorContrato.normalizar(formatador.formatCellValue(celula));
    }
}
