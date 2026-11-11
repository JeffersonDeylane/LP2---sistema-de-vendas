package lp2.processamento;

import java.nio.file.Path;
import java.util.Objects;

/** Caminhos relativos à raiz do repositório; não depende do computador. */
public final class ConfiguracaoProcessamento {
    private final Path raiz;
    private final Path entrada;
    private final Path saida;
    private final Path exportacao;
    private final Path baseClaro;

    public ConfiguracaoProcessamento(Path raizProjeto) {
        this.raiz = Objects.requireNonNull(raizProjeto).toAbsolutePath().normalize();
        this.entrada = raiz.resolve("dados/importacao");
        this.exportacao = raiz.resolve("dados/exportacao");
        this.saida = exportacao.resolve("arquivos_para_processar/saida");
        this.baseClaro = entrada.resolve("analitico_da_claro/claro.xlsx");
    }
    public Path raiz() { return raiz; }
    public Path entrada() { return entrada; }
    public Path saida() { return saida; }
    public Path exportacao() { return exportacao; }
    public Path baseClaro() { return baseClaro; }
}
