package lp2.processamento;

import java.math.BigDecimal;

public final class NormalizadorContrato {
    private NormalizadorContrato() {}
    /** Remove somente o sufixo artificial .0, sem perder zeros à esquerda. */
    public static String normalizar(Object valor) {
        if (valor == null) return "";
        String s = valor instanceof Number ? new BigDecimal(valor.toString()).stripTrailingZeros().toPlainString() : valor.toString().trim();
        return s.trim().replaceFirst("^(\\d+)\\.0+$", "$1");
    }
    /** Repete o tratamento sequencial dos prefixos feito pelo Python. */
    public static String normalizarChip(Object valor) {
        String s = normalizar(valor);
        for (String p : new String[]{"9700", "136", "9500", "63700"}) {
            if (s.startsWith(p)) s = s.substring(p.length());
        }
        return s;
    }
}
