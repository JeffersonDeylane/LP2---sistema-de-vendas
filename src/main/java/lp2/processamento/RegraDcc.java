package lp2.processamento;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

/** DCC = produto VIRTUA e pagamento contendo DÉBITO/DEBITO. */
public final class RegraDcc implements RegraProduto {
    @Override public String coluna() { return "DCC"; }
    @Override public int detectar(Map<String,Object> linha) {
        if (TabelaDados.inteiro(linha.get("VIRTUA")) != 1) return 0;
        String pagamento = TabelaDados.texto(linha.get("FormaPagamento"));
        pagamento = Normalizer.normalize(pagamento, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
        return pagamento.contains("DEBITO") ? 1 : 0;
    }
}
