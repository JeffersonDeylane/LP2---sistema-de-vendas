package lp2.processamento;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

/** Fachada para o Main: executar processamento completo em uma chamada. */
public final class ProcessadorPlanilhas {
    private final ConfiguracaoProcessamento config;
    private final TratadorAnalitico tratador = new TratadorAnalitico();
    private final ComplementadorClaro complementador = new ComplementadorClaro();
    private final CruzadorContratos cruzador = new CruzadorContratos();
    private final GeradorRelatorios relatorios = new GeradorRelatorios();

    public ProcessadorPlanilhas(Path raizProjeto) { this.config = new ConfiguracaoProcessamento(raizProjeto); }
    public ConfiguracaoProcessamento getConfiguracao() { return config; }

    public ResultadoProcessamento executar() {
        List<Path> gerados = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        try {
            Files.createDirectories(config.entrada());
            Files.createDirectories(config.saida());
            Files.createDirectories(config.exportacao());
            List<Path> entradas;
            try (Stream<Path> arquivos = Files.list(config.entrada())) {
                entradas = arquivos.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).matches(".*\\.(xlsx|xls|csv)"))
                    .sorted().toList();
            }
            if (entradas.isEmpty()) {
                avisos.add("Nenhum CSV/XLS/XLSX em " + config.entrada());
                return new ResultadoProcessamento(gerados, null, avisos);
            }
            // Não apagamos arquivos existentes: segurança melhor que a limpeza indiscriminada do script original.
            String hoje = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM"));
            Set<String> nomesCriados = new HashSet<>();
            for (Path entrada : entradas) {
                try {
                    String nome = entrada.getFileName().toString();
                    TabelaDados tabela = tratador.tratar(PlanilhasIO.ler(entrada,"Analítico"), nome);
                    String empresa = nome.toUpperCase(Locale.ROOT).contains("VNA") ? "VNA" : "RDT";
                    String nomeSaida = empresa + " - ANALITICO " + hoje + ".xlsx";
                    if (!nomesCriados.add(nomeSaida)) {
                        avisos.add("Mais de uma entrada da empresa " + empresa + "; evite sobrescrita e renomeie/una os arquivos de origem.");
                        continue;
                    }
                    Path saida = config.saida().resolve(nomeSaida);
                    try (Workbook wb = new XSSFWorkbook()) {
                        PlanilhasIO.escreverTabela(wb, "Analítico", tabela, true);
                        if (Files.exists(config.baseClaro())) {
                            complementador.adicionar(wb, config.baseClaro(), empresa);
                            cruzador.cruzar(wb, tabela, "virtua", "VIRTUA", "NR_CONTRATO", "virtua_analitico");
                            cruzador.cruzar(wb, tabela, "tv", "TV", "DT", "tv_analitico");
                            cruzador.cruzar(wb, tabela, "chip", "CHIP", "CONTRATO_NET", "chip_analitico");
                            cruzador.cruzar(wb, tabela, "tel fixo", "FONE", "NR_CONTRATO", "fone_analitico");
                        } else avisos.add("Base complementar ausente: " + config.baseClaro() + "; cruzamentos omitidos.");
                        relatorios.criar(wb, tabela);
                        PlanilhasIO.salvar(wb, saida);
                    }
                    gerados.add(saida);
                    System.out.printf("Processado %s (%d linhas)%n", saida.getFileName(), tabela.linhas().size());
                } catch (Exception e) {
                    avisos.add("Falha ao processar " + entrada.getFileName() + ": " + e.getMessage());
                }
            }
            Path consolidado = new ConsolidadorVendas().consolidar(gerados, config.exportacao().resolve("vendas.xlsx"));
            return new ResultadoProcessamento(gerados, consolidado, avisos);
        } catch (IOException e) {
            throw new ProcessamentoException("Falha de entrada/saída do processamento", e);
        }
    }
}
