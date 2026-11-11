package lp2.processamento;

import java.nio.file.Path;
import java.util.List;

public final class ResultadoProcessamento {
    private final List<Path> arquivosGerados;
    private final Path consolidado;
    private final List<String> avisos;
    public ResultadoProcessamento(List<Path> arquivosGerados, Path consolidado, List<String> avisos) {
        this.arquivosGerados = List.copyOf(arquivosGerados);
        this.consolidado = consolidado;
        this.avisos = List.copyOf(avisos);
    }
    public List<Path> getArquivosGerados() { return arquivosGerados; }
    public Path getConsolidado() { return consolidado; }
    public List<String> getAvisos() { return avisos; }
}
