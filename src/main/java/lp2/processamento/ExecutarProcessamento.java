package lp2.processamento;

import java.nio.file.Path;
/** Execução independente opcional: não precisa alterar o Main.java existente. */
public final class ExecutarProcessamento {
    private ExecutarProcessamento() {}
    public static void main(String[] args) {
        Path raiz = args.length > 0 ? Path.of(args[0]) : Path.of(".");
        ResultadoProcessamento r = new ProcessadorPlanilhas(raiz).executar();
        System.out.println("Arquivos gerados: " + r.getArquivosGerados().size());
        System.out.println("Consolidado: " + r.getConsolidado());
        r.getAvisos().forEach(a -> System.out.println("Aviso: " + a));
    }
}
