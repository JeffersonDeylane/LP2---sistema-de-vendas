package lp2.processamento;

import java.util.Locale;
import java.util.Map;

/** Corresponde ao str.contains(... case=False) usado pelo Python. */
public final class RegraPresencaProduto implements RegraProduto {
    private final String produto;
    public RegraPresencaProduto(String produto) { this.produto = produto; }
    @Override public String coluna() { return produto; }
    @Override public int detectar(Map<String,Object> linha) {
        String texto = TabelaDados.texto(linha.get("Produtos"));
        return texto.toUpperCase(Locale.ROOT).contains(produto) ? 1 : 0;
    }
}
