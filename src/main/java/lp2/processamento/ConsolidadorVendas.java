package lp2.processamento;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/** Une os analíticos recém-gerados em dados/exportacao/vendas.xlsx. */
public final class ConsolidadorVendas {
    public Path consolidar(List<Path> arquivos, Path destino) throws IOException {
        if (arquivos.isEmpty()) return null;
        TabelaDados consolidada = null;
        for (Path arquivo : arquivos) {
            TabelaDados origem = PlanilhasIO.ler(arquivo, "Analítico");
            if (consolidada == null) consolidada = new TabelaDados(origem.colunas());
            for (String coluna : origem.colunas()) consolidada.adicionarColuna(coluna,null);
            for (Map<String,Object> linha : origem.linhas()) consolidada.adicionarLinha(linha);
        }
        PlanilhasIO.salvarAnalitico(consolidada, destino);
        return destino;
    }
}
