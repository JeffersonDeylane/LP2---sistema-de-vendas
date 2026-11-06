# Portabilidade Java — processamento de planilhas LP2

Conversão modular do arquivo Python `processar_planilhas_minusculas(2).py` para Java 17 com **Apache POI** e **Apache Commons CSV**.

## Instalar no projeto existente

1. Copie **a pasta** `src/main/java/lp2/processamento/` para `src/main/java/lp2/processamento/` de seu repositório.
2. Copie `pom.xml` para a raiz do repositório **se você ainda não possui pom.xml**. Se já possui, apenas adicione as dependências indicadas abaixo (não substitua o seu).
3. Mantenha seus arquivos `Main.java`, `Usuario.java`, `Vendedor.java`, `Validador.java`: o ZIP **não os substitui**.
4. No `Main.java`, importe `lp2.processamento.ProcessadorPlanilhas` e `ResultadoProcessamento`, conforme `EXEMPLO_INTEGRACAO_MAIN.txt`.
5. Coloque os XLS/XLSX/CSV diretamente em `dados/importacao/`, e a base complementar em `dados/importacao/analitico_da_claro/claro.xlsx`.
6. Na raiz do repositório, execute `mvn compile` (requer Maven e acesso inicial para baixar dependências).
7. Para teste isolado: `mvn exec:java -Dexec.mainClass=lp2.processamento.ExecutarProcessamento`.

## Dependências Maven

- `org.apache.poi:poi-ooxml:5.4.1` (ler/escrever XLSX/XLS)
- `org.apache.commons:commons-csv:1.13.0` (ler CSV)

`javac -d out src/main/java/*.java` **não é suficiente** para compilar estas classes, pois elas dependem de bibliotecas externas.

## Correspondência com o Python

| Python | Java |
|---|---|
| `executar`, `processar_base` | `ProcessadorPlanilhas`, `TratadorAnalitico` |
| `normalizar_contrato`, `tratar_contrato_chip` | `NormalizadorContrato` |
| regras `VIRTUA/CHIP/TV/FONE/DCC` | `RegraProduto`, `RegraPresencaProduto`, `RegraDcc` |
| `adicionar_base_complementar` | `ComplementadorClaro` |
| `cruzar_analitico` | `CruzadorContratos` |
| `salvar_analitico` | `PlanilhasIO` |
| `criar_vendas_consolidado` | `ConsolidadorVendas` |
| `criar_tabelas_dinamicas` | `GeradorRelatorios` (resumos de dados) |

## Regras e limitações importantes

- Mantém identificação de `VIRTUA`, `CHIP`, `TV`, `FONE`; `DCC` exige VIRTUA e DÉBITO; `PORT` continua zero para análise manual; `SOMA` é fórmula `SUM(...)` nos arquivos.
- Mantém identificação de empresa VNA/RDT pelo nome do arquivo, e reclassificação de `ZAIRO JUNIOR`/`EDVALDO SANTOS` como `PE`.
- Mantém filtros específicos para CHIP, desistência/venda bruta e parceiros da Claro, e cruzamentos por contrato com divergências em vermelho/verde.
- As abas `Planilha1` e `Planilha2` são **resumos tabulares, não tabelas dinâmicas nativas do Excel**, pois `win32com.client` automatiza Excel/Windows e não tem substituição direta na API utilizada. Os campos agregados são os mesmos, mas formatação, agrupamento automático de pivot, cache e atualização interativa não são equivalentes.
- O script Python antigo apaga **todos** os arquivos da pasta de saída; a versão Java **não apaga arquivos antigos** por segurança. Apenas arquivos processados nesta execução são consolidados.
- Se houver dois arquivos de entrada da **mesma empresa**, o segundo será ignorado com aviso para evitar sobrescrever a saída. Não há mesclagem automática das entradas dessa mesma empresa.
- Não altera `BASE_GERAL.xlsx`, nem implementa aprovação de divergências, auditoria, comissão ou regras de cancelamento: esses são módulos de domínio que devem ficar separados do processo ETL de planilhas.
- Para evitar inconsistência numérica, confira os resultados contra o Python antes de usar dados reais. A leitura CSV usa UTF-8; outras codificações precisam de adaptação.
- O valor `Valor` da planilha é tratado como valor numérico para sumarização, sem inferir que se trata de receita de cliente (pode ser comissão, conforme base).
- A regra atual do Python coloca `Empresa=PE` para os dois supervisores listados. Se sua nova convenção comercial eliminar PE, altere conscientemente em `TratadorAnalitico` após confirmar a regra desejada.
- Arquivos XLSX são salvos com gravação temporária e substituição atômica quando suportada. Se o arquivo estiver aberto/bloqueado, a gravação poderá falhar com aviso.

## Organização e POO

`RegraProduto` + `RegraPresencaProduto`/`RegraDcc` mostram polimorfismo de regras de identificação, mas isso **não substitui** as situações de polimorfismo de negócio relevantes exigidas pela atividade (aprovação/gestão por perfil e políticas de comissão). Classes de serviço e utilidade não contam para as dez entidades exigidas.

## Verificação

O código foi organizado e submetido à verificação estática local, mas **não executado contra suas planilhas originais** neste ambiente, pois a planilha original não foi anexada junto a esta solicitação e as dependências Maven não estão disponíveis localmente. Portanto não representa garantia de saída idêntica ao Python em todos os tipos de célula/arquivo.
