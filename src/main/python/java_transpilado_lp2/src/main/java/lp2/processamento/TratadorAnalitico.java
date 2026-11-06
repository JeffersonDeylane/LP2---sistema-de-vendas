package lp2.processamento;

import java.util.*;

/** Tradução das regras de processar_base, independentes do formato do arquivo. */
public final class TratadorAnalitico {
    private static final List<String> REMOVER = List.of("IdVenda", "IdCliente", "Proposta", "TipoCliente", "IdVendedor", "TipoPacote", "ComTV");
    private static final List<String> NOVAS = List.of("VIRTUA", "CHIP", "PORT", "TV", "FONE", "DCC", "SOMA");
    private final List<RegraProduto> regras = List.of(
            new RegraPresencaProduto("VIRTUA"), new RegraPresencaProduto("CHIP"),
            new RegraPresencaProduto("TV"), new RegraPresencaProduto("FONE"), new RegraDcc());

    public TabelaDados tratar(TabelaDados tabela, String nomeArquivo) {
        if (!tabela.temColuna("Produtos")) throw new ProcessamentoException("Coluna obrigatória Produtos ausente em " + nomeArquivo);
        if (!tabela.temColuna("Contrato")) throw new ProcessamentoException("Coluna obrigatória Contrato ausente em " + nomeArquivo);
        REMOVER.forEach(tabela::removerColuna);
        int aposSupervisor = tabela.temColuna("Supervisor") ? tabela.indice("Supervisor") + 1 : tabela.colunas().size();
        for (int i = 0; i < NOVAS.size(); i++) tabela.inserirColuna(aposSupervisor + i, NOVAS.get(i), 0);
        String empresa = nomeArquivo.toUpperCase(Locale.ROOT).contains("VNA") ? "VNA" : "RDT";
        if (!tabela.temColuna("Empresa")) tabela.inserirColuna(tabela.temColuna("Supervisor") ? tabela.indice("Supervisor")+1 : tabela.colunas().size(), "Empresa", empresa);
        for (Map<String,Object> linha : tabela.linhas()) {
            for (RegraProduto regra : regras) linha.put(regra.coluna(), regra.detectar(linha));
            linha.put("PORT", 0); // Requer revisão manual, conforme o script original.
            int soma = 0;
            for (String c : List.of("VIRTUA", "CHIP", "PORT", "TV", "FONE", "DCC")) soma += TabelaDados.inteiro(linha.get(c));
            linha.put("SOMA", soma);
            String supervisor = TabelaDados.texto(linha.get("Supervisor")).toUpperCase(Locale.ROOT);
            linha.put("Empresa", supervisor.equals("ZAIRO JUNIOR") || supervisor.equals("EDVALDO SANTOS") ? "PE" : empresa);
        }
        return tabela;
    }
}
