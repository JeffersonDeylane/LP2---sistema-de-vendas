package lp2.processamento;

import java.util.Map;
/** Contrato de detecção de produto: permite diferentes regras por produto. */
public interface RegraProduto {
    String coluna();
    int detectar(Map<String,Object> linha);
}
