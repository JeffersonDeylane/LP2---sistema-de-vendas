# Status Atual e Diagnóstico Técnico do Projeto

**Data da inspeção:** 08/11/2026, referência de calendário America/Sao_Paulo.

**Commit de código examinado:** `b0355c29726ef2a00d1bd178103813e537cc9aab`.

**Escopo:** código, configurações, estrutura, documentos, UML, dados e histórico Git local. Os quatro documentos oficiais são alterações documentais sobre essa base, ainda sem commit próprio nesta avaliação.

Este retrato deve ser atualizado quando o código evoluir. A [especificação comercial](regras/REGRAS_DE_NEGOCIO.md) descreve o produto pretendido; os [requisitos acadêmicos](requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md) organizam a atividade; o [README](../README.md) orienta uso. Requisitos documentados não são implementação.

## 1. Identidade e resumo do estágio atual

O projeto evolui para um **Sistema de Gestão Comercial Flexível**, voltado a vendedores, apoio comercial e administradores de negócios com produtos e serviços diversos. A proposta é reunir catálogo configurável, vendas, contratos opcionais, comissões, revisão de alterações e histórico, preservando condições comerciais e conciliando fontes externas.

Hoje existem duas partes independentes: início de aplicação Java por terminal e processamento especializado de planilhas de telecomunicações, disponível em Python e em um módulo Java separado. Não existem ainda categorias dinâmicas, unidades, produtos genéricos, clientes, vendas ou contratos como classes implementadas.

| Indicador | Situação por inspeção |
|---|---|
| Classes Java | 19, sendo 1 abstrata e 18 concretas |
| Interfaces/enums | 1 interface própria; nenhum enum |
| Entidades contabilizáveis | 2/10: Usuario e Vendedor; incluindo a abstração de domínio |
| Polimorfismo | 1 caso utilizado na integração, com ressalva de aceitação acadêmica; perfis incompletos |
| Regras genéricas vigentes | 0 completas, RN08 parcial, 17 não implementadas |
| Regras comerciais especializadas | 4 implementadas no código, sem execução Java comprovada |
| Funcionalidades comerciais identificadas | 5 no processamento separado; nenhum CRUD completo no menu |
| Autenticação | Não implementada |
| Autorização | Consultas parciais de permissão, sem operações protegidas |
| Persistência de entidades/recuperação ao iniciar | Não implementada |
| Testes versionados | Nenhum; src/test/java contém somente .gitkeep |
| Execução completa | Não comprovada; limitações na seção 10 |

## 2. Organização real

```text
/
├── README.md
├── .gitignore
├── dados/
│   ├── exemplos/
│   ├── importacao/
│   └── exportacao/
├── docs/
│   ├── STATUS_DO_PROJETO.md
│   ├── regras/REGRAS_DE_NEGOCIO.md
│   ├── requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md
│   ├── planejamento/
│   └── diagramas/
└── src/
    ├── main/
    │   ├── java/                 # Main, Usuario, Vendedor, Validador
    │   └── python/
    │       ├── processar_planilhas_minusculas.py
    │       └── java_transpilado_lp2/
    │           ├── pom.xml
    │           └── src/main/java/lp2/processamento/
    └── test/java/.gitkeep
```

Não há pom.xml na raiz nem mecanismo que compile as duas partes Java juntas. O build Maven aninhado inclui o processamento, não as quatro classes em src/main/java da raiz. As pastas de dados não contêm XLS/XLSX/CSV fictícios ou reais. dados/README.md e docs/diagramas/README.md estão vazios.

Nas referências abaixo, **J** significa src/main/java e **P** significa [src/main/python/java_transpilado_lp2/src/main/java/lp2/processamento](../src/main/python/java_transpilado_lp2/src/main/java/lp2/processamento/). Linhas citadas correspondem ao commit examinado.

## 3. Fluxos e funcionalidades atuais

### 3.1. Aplicação principal

[Main.java](../src/main/java/Main.java) cria Vendedor com dados fixos, imprime dados/permissões, provoca duas validações inválidas e captura IllegalArgumentException. Depois recebe opções via Scanner: 1 informa login pendente; 2 mostra descrição; 0 sai; outras opções são recusadas.

Não recebe cadastro comercial, não salva o usuário, não consulta contrato e não chama ProcessadorPlanilhas. As mensagens ainda usam o nome anterior do sistema, pois o código não foi alterado nesta revisão documental.

### 3.2. Operações encontradas

IMPLEMENTADO nesta tabela significa implementação identificada por leitura, não execução completa confirmada.

| ID | Funcionalidade/descrição | Classes e localização | Evidência | Integrada ao Main? | Execução comprovada? |
|---|---|---|---|---|---|
| FA01 | Menu, descrição e encerramento | Main; J/Main.java:62 | Scanner e switch com opções reais | Sim | Não; Java fora do PATH |
| FA02 | Demonstração de usuário/permissões | Usuario, Vendedor; J/Main.java:14 | Instância fixa, getters e impressão de permissões | Sim, como demonstração | Não |
| FA03 | Validação de atributos | Validador, Usuario, Vendedor; J/Validador.java:10 | Texto obrigatório, inteiro positivo e captura de dois casos no Main | Sim, sem cadastro interativo | Não |
| FA04 | Alteração de dados/situação em memória | Usuario, Vendedor; J/Usuario.java:43 e :67 | Métodos de alteração, ativação/desativação e matrícula | Sem fluxo de edição no menu | Não |
| FA05 | Processar analíticos externos | ProcessadorPlanilhas, PlanilhasIO, TratadorAnalitico e regras; P/ProcessadorPlanilhas.java:23 | Leitura XLS/XLSX/CSV, transformação e arquivo por empresa | Não; ExecutarProcessamento separado | Não no Java; Python completo não executado |
| FA06 | Conciliar com base Claro | ComplementadorClaro, CruzadorContratos; P/ComplementadorClaro.java:20 e P/CruzadorContratos.java:8 | Filtra abas e marca contratos exclusivos por fonte | Não | Cruzamento Python verificado em memória; fluxo completo não |
| FA07 | Consolidar analíticos | ConsolidadorVendas, PlanilhasIO; P/ConsolidadorVendas.java:9 | Une arquivos da execução em vendas.xlsx | Não | Não |
| FA08 | Resumo supervisor/vendedor | GeradorRelatorios; P/GeradorRelatorios.java:17 | Agrega VIRTUA, CHIP, PORT, TV, SOMA e Valor | Não | Não |
| FA09 | Resumo vendedor/data | GeradorRelatorios; P/GeradorRelatorios.java:40 | Contagem por vendedor e DataInstalacao | Não | Não |

Para o mínimo acadêmico, somente FA05–FA09 foram contadas: **5/15 funcionalidades**, todas adicionais com interação entre classes. FA01–FA04 são interface, demonstração ou métodos auxiliares, não CRUD completo. Faltam pelo menos dez funcionalidades no total e duas adicionais para sete, caso o processamento seja aceito como parte do sistema entregue. Os RF da especificação são planejamento e não aumentam essa contagem.

### 3.3. Integração especializada preservada

ExecutarProcessamento chama a fachada, procura entradas diretamente em dados/importacao e opcionalmente usa dados/importacao/analitico_da_claro/claro.xlsx. Saídas: dados/exportacao/arquivos_para_processar/saida e consolidado dados/exportacao/vendas.xlsx. O Java cria Planilha1/Planilha2 como resumos tabulares, sem tabelas dinâmicas nativas.

O [script Python](../src/main/python/processar_planilhas_minusculas.py) tem 824 linhas e 14 funções: formatação/pivôs, limpeza, leitura/tratamento, normalização, cruzamento, consolidação e execução. Usa pandas, openpyxl e Excel via win32com. Seu fluxo completo exige a base complementar e o Excel. Antes de procurar entradas, remove os arquivos da pasta de saída. Python e Java implementam operações semelhantes e não são contados duas vezes.

## 4. Inventário completo dos tipos Java

| Tipo | Categoria / responsabilidade | Localização | Conta nas dez? |
|---|---|---|---|
| Usuario | Entidade abstrata; identidade, dados e situação | J/Usuario.java | Sim |
| Vendedor | Entidade concreta; matrícula e permissões | J/Vendedor.java | Sim |
| Main | Apresentação/ponto de entrada | J/Main.java | Não |
| Validador | Utilitário de validação | J/Validador.java | Não |
| ComplementadorClaro | Serviço de filtros e cópia de abas | P/ComplementadorClaro.java | Não |
| ConfiguracaoProcessamento | Configuração de caminhos | P/ConfiguracaoProcessamento.java | Não |
| ConsolidadorVendas | Serviço de consolidação | P/ConsolidadorVendas.java | Não |
| CruzadorContratos | Serviço de comparação de contratos | P/CruzadorContratos.java | Não |
| ExecutarProcessamento | Ponto de entrada independente | P/ExecutarProcessamento.java | Não |
| GeradorRelatorios | Serviço de agregações | P/GeradorRelatorios.java | Não |
| NormalizadorContrato | Utilitário de identificadores | P/NormalizadorContrato.java | Não |
| PlanilhasIO | Infraestrutura de arquivos | P/PlanilhasIO.java | Não |
| ProcessadorPlanilhas | Serviço/fachada de processamento | P/ProcessadorPlanilhas.java | Não |
| ProcessamentoException | Exceção personalizada | P/ProcessamentoException.java | Não |
| RegraDcc | Política de detecção comercial | P/RegraDcc.java | Não |
| RegraPresencaProduto | Política de identificação textual | P/RegraPresencaProduto.java | Não |
| ResultadoProcessamento | Objeto de transporte de resultado | P/ResultadoProcessamento.java | Não |
| TabelaDados | Estrutura tabular auxiliar | P/TabelaDados.java | Não |
| TratadorAnalitico | Serviço de transformação | P/TratadorAnalitico.java | Não |
| RegraProduto | Interface de detecção | P/RegraProduto.java | Não |

Totais sem dupla contagem: **19 classes, 1 interface, 0 enums**. Classes abstratas/concretas são qualificações, não categorias adicionais somadas ao total. Não existem controllers ou repositórios dedicados. TabelaDados e ResultadoProcessamento são estruturas técnicas, não entidades comerciais. O mínimo permanece **2/10**, ou uma concreta se o professor desconsiderar abstrações — interpretação a confirmar.

CategoriaProduto, TipoUnidadeMedida, UnidadeMedida, Produto, Cliente, Venda, ItemVenda, Contrato, Apoio, Administrador, Comissao, SolicitacaoAlteracao, GrupoCancelamento e RegistroHistorico são conceitos documentados, sem classe correspondente no código atual.

## 5. Regras implementadas e lacunas

### 5.1. Domínio comercial genérico vigente

| Regra | Estado atual | Evidência/lacuna |
|---|---|---|
| RN01 Categorias | NÃO IMPLEMENTADO | CategoriaProduto inexistente |
| RN02 Tipos/unidades | NÃO IMPLEMENTADO | Entidades e manutenção inexistentes |
| RN03 Produtos disponíveis | NÃO IMPLEMENTADO | Não há Produto ou catálogo genérico |
| RN04 Quantidade/unidade | NÃO IMPLEMENTADO | Não há ItemVenda com unidade ou quantidade de domínio |
| RN05 Cliente/contrato opcional | NÃO IMPLEMENTADO | Não há Cliente, Venda ou Contrato |
| RN06 Confirmação da venda | NÃO IMPLEMENTADO | Não há transação comercial de domínio |
| RN07 Fotografia histórica | NÃO IMPLEMENTADO | Não há item ou versionamento comercial |
| RN08 Autenticação/permissões | PARCIALMENTE IMPLEMENTADO | Vendedor.java:31–41 nega aprovação/gestão e consulta ativo; autenticação e proteção real ausentes |
| RN09 Solicitações/revisão | NÃO IMPLEMENTADO | Sem entidade/fila/decisão de campos |
| RN10 Transições comerciais | NÃO IMPLEMENTADO | Sem estados ou métodos de transição comercial |
| RN11 Comissão percentual | NÃO IMPLEMENTADO | Relatório soma Valor; não calcula por política |
| RN12 Unicidade financeira | NÃO IMPLEMENTADO | Sem comissão/pagamento identificados |
| RN13 Elegibilidade/pagamento | NÃO IMPLEMENTADO | Sem critério ou registro de pagamento |
| RN14 Cancelamento | NÃO IMPLEMENTADO | Sem cancelamento de item/venda |
| RN15 Reativação seletiva | NÃO IMPLEMENTADO | Sem grupos e proteção de alterações posteriores |
| RN16 Adaptação para domínio genérico | NÃO IMPLEMENTADO | Processamento não cria entidades genéricas nem aplica suas invariantes |
| RN17 Conciliação idempotente | NÃO IMPLEMENTADO | Cruzamento não mantém base ou solicitações |
| RN18 Histórico | NÃO IMPLEMENTADO | Console não é histórico persistente |

Núcleo: **0 implementadas, 1 parcial, 17 não implementadas, 0 não verificáveis por inspeção**. A ausência de implementação é verificável pelo inventário; isso é diferente da impossibilidade de executar código existente.

### 5.2. Políticas comerciais específicas da integração

| ID | Regra encontrada | Evidência Java | Situação |
|---|---|---|---|
| EL01 | Classificação empresarial por arquivo, com exceção de supervisores para PE | TratadorAnalitico.tratar, P/TratadorAnalitico.java:19 e :27 | IMPLEMENTADO no código; execução Java não comprovada |
| EL02 | DCC exige VIRTUA e pagamento contendo débito | RegraDcc.detectar, P/RegraDcc.java:10 | IMPLEMENTADO no código; execução Java não comprovada |
| EL03 | Excluir desistência e venda bruta das abas complementares pertinentes | ComplementadorClaro.adicionar, P/ComplementadorClaro.java:35 | IMPLEMENTADO no código; execução completa não comprovada |
| EL04 | Separar escopo empresarial por parceiro e código CHIP | ComplementadorClaro.adicionar, P/ComplementadorClaro.java:29 e :34 | IMPLEMENTADO no código; execução completa não comprovada |

As mesmas políticas aparecem no Python; não foram duplicadas na contagem. Identificação textual de produto, remoção de sufixos, formatação, campos obrigatórios e soma genérica não foram contados como regras comerciais adicionais.

Universo combinado atual: **22 regras identificadas**, sendo 4 implementadas, 1 parcial, 17 não implementadas e 0 não verificáveis por leitura. Somente as 18 RN são especificação universal; as quatro EL são legado especializado. Para o requisito de dez, há quatro candidatas completas por inspeção se o professor aceitar o módulo; faltam ao menos seis. Não é prudente substituir as regras centrais por filtros de integração.

## 6. POO, arquitetura e padrões

### Relações e responsabilidades

- Main cria Vendedor; Vendedor herda Usuario; ambos usam Validador.
- ExecutarProcessamento cria ProcessadorPlanilhas, que compõe configuração, tratador, complementador, cruzador e gerador; chama ConsolidadorVendas e retorna ResultadoProcessamento.
- TratadorAnalitico percorre List<RegraProduto>; RegraPresencaProduto e RegraDcc implementam detectar/coluna.
- Serviços manipulam TabelaDados e dependem de PlanilhasIO/Apache POI. A lista de linhas fica diretamente exposta.

Há separação de apresentação, domínio inicial, serviços de processamento e entrada/saída. Não há arquitetura MVC completa, repositório de entidades nem recuperação de estado. A persistência Excel substituível dos diagramas é planejamento, não código.

### Polimorfismo

1. **Regras de integração:** interface própria RegraProduto, duas implementações, chamada real em TratadorAnalitico.java:22. Uma procura texto; outra exige condição DCC. Existe despacho polimórfico e significado comercial, mas parte do comportamento é parsing; aceitação como caso acadêmico deve ser confirmada.
2. **Perfis:** Usuario declara podeAprovarAlteracoes/podeGerenciarUsuarios; somente Vendedor implementa. Não há dois tipos concretos com diferenças demonstradas. podeSolicitarAlteracao não sobrescreve método da base; seu comentário sobre polimorfismo não comprova outro caso.

Políticas genéricas de comissão/elegibilidade ainda não existem. Não contar o mesmo conjunto de regras duas vezes nem herança de exceção da biblioteca como polimorfismo próprio do domínio.

### Convenções e qualidade

Predomina português: Usuario, Vendedor, alterarNome, podeAprovarAlteracoes. get/set/is seguem convenção Java; variáveis sheet/header/formatter e abreviações wb/c/r coexistem com português. Recomendação: português consistente nos conceitos e ações do domínio, preservando convenções Java e nomes das APIs/campos externos. Não é necessário traduzir tudo nem refatorar só por preferência.

Classes/interface seguem PascalCase; atributos/métodos, camelCase; constantes, maiúsculas. Núcleo sem package contrasta com lp2.processamento. Há imports curinga, métodos compactos e comentários didáticos redundantes. Vendedor não usa @Override nos métodos sobrescritos. São pontos de padronização, secundários às funcionalidades ausentes.

Campos privados de Usuario/Vendedor, ID final, construtores com validação, utilitários static e try-with-resources são práticas presentes. ResultadoProcessamento faz cópias imutáveis das listas; TabelaDados.linhas expõe coleção/mapas mutáveis. final na referência não torna conteúdo imutável.

Fachada e estratégia são identificáveis no processamento. Há indícios de responsabilidade única e separação de interesses; não é possível afirmar adoção completa de SOLID. DRY é parcial: listas de indicadores reaparecem em transformação, soma e fórmula. Python/Java mantêm políticas paralelas que podem divergir. O desenho é simples, compatível com KISS; não requer frameworks, microserviços ou API para a atividade.

## 7. Exceções, segurança e persistência

Validador lança IllegalArgumentException para texto obrigatório e ID positivo; Main captura matrícula/login vazios. ProcessamentoException é lançada por colunas ausentes, aba sem cabeçalho e falhas de I/O. ProcessadorPlanilhas captura Exception por arquivo e transforma mensagens em avisos, mas falha de consolidação pode propagar até ExecutarProcessamento sem tratamento amigável. A captura ampla reduz distinção entre erro previsto e defeito de programação.

Usuario possui nome/login/senhaHash, mas não há geração/verificação de hash, busca de credencial ou sessão. Nenhum segredo real foi necessário à análise; dados de demonstração não são autenticação. Autorizações são consultas sem operações protegidas e sem validação de propriedade da venda.

Usuario.ativo muda em memória por ativar/desativar e influencia podeSolicitarAlteracao; esses métodos apenas atribuem booleano. Isso não cumpre ciclo comercial com transições inválidas bloqueadas.

PlanilhasIO salva saídas por temporário e tenta substituição atômica, com alternativa não atômica. Não há backup nem bloqueio entre processos. Exportar resultados não equivale a persistir entidades. Main não lê/grava base central; não há recuperação automática ou demonstração de sobrevivência dos dados após reinício.

## 8. Problemas técnicos priorizados

| Prioridade | Problema e evidência | Impacto | Direção recomendada, sem implementação nesta revisão |
|---|---|---|---|
| Alta | Build fragmentado; pom.xml só no módulo dentro de python | Instrução mvn compile na raiz não funciona sem -f/integração | Definir build unificado quando autorizado |
| Alta | Python executar:800 limpa saída antes de procurar entradas; exclusão em :244 | Perda de saídas mesmo sem novas entradas | Preservar resultados e definir política explícita de limpeza |
| Alta | TabelaDados.numero:37 retorna zero se parsing falha; 1.234,56 não é tratado como valor brasileiro completo | Total pode ocultar valor inválido | Parsing monetário explícito e erro por registro |
| Alta | Falta de entidades, persistência e autorização operacional | Novo núcleo comercial não funciona | Implementar domínio e serviços protegidos |
| Alta | Nenhuma suíte ou amostra fictícia | Regressões e diferenças Python/Java sem detecção | Testar regras e integração com dados fictícios |
| Média | Nomes empresa + dd.MM e REPLACE_EXISTING | Reexecução substitui saída anterior sem backup | Identificar execução e preservar versões |
| Média | ProcessadorPlanilhas.java:49 ignora outra entrada da mesma empresa e reserva nome antes de concluir | Lote parcial, inclusive após falha da primeira entrada | Mesclar ou rejeitar lote claramente; reservar sucesso após gravar |
| Média | ComplementadorClaro seleciona colunas ausentes como null | Filtros podem deixar registros sem classificação | Validar esquema por aba antes de filtrar |
| Média | Fórmulas escritas sem avaliação explícita; leitura usa cache, PlanilhasIO.java:63/:117 | Risco de valores não atualizados na leitura/consolidação | Testar cálculo e leitores antes da consolidação; não reproduzido nesta análise |
| Média | double nas agregações; TabelaDados.linhas mutável | Arredondamento e consistência vulneráveis | BigDecimal no domínio e mutação controlada |
| Média | Cabeçalhos duplicados não rejeitados e mapas por nome | Possível sobrescrita de colunas homônimas | Validar unicidade/cabeçalhos obrigatórios |
| Média | Alguns avisos do cruzamento só vão ao console | Resultado pode parecer completo com cruzamento omitido | Propagar aviso/estado ao resultado |
| Baixa | Main.nextLine sem verificar fim da entrada | Encerramento inesperado em entrada terminada | Tratar fim da entrada |
| Documental | UML/planejamento antigos descrevem domínio diferente e código ainda traz nome anterior | Confusão entre intenção, implementação e entrega | Prevalência dos quatro documentos oficiais; revisão futura dos demais arquivos |

## 9. Pendências por área e ordem recomendada

1. **Execução:** preparar JDK 17/Maven, validar comandos do README e tornar o build reproduzível; manter proteção contra perda das saídas existentes.
2. **Modelagem:** criar categorias, tipos/unidades, Produto, Cliente, Venda, ItemVenda e Contrato opcional; completar perfis, solicitações, comissões e histórico. Faltam ao menos oito classes elegíveis, não oito utilitários.
3. **Regras:** validar decisões propostas da especificação; implementar RN01–RN18 com métodos de domínio e casos de aceitação, sem transformar telecomunicações em regra universal.
4. **Funcionalidades:** decompor e integrar CRUD/fluxos completos para quinze operações, sendo sete adicionais; aproveitar relatórios e processamento sem contar etapas internas separadamente.
5. **Segurança/persistência:** autenticar, autorizar nos serviços, impedir autoaprovação e operação inativa; definir base com recuperação, backup e histórico consistente.
6. **Documentação:** atualizar futuramente UML e planejamento legado; preencher integrantes/matrículas/link; confirmar requisitos e decisões com a equipe.
7. **Testes/apresentação:** criar suíte de domínio e integração, amostras fictícias, teste de reinício, comparação de versões e roteiro de demonstração; preparar ZIP e envio conforme a atividade.

Não são necessários nesta etapa estoque, fiscal, gateway, API REST, microserviços ou conversão automática de unidades. Cada evolução de comportamento deve atualizar este diagnóstico e seu checklist acadêmico.

## 10. Histórico da avaliação e verificações

### Evidências de 08/11/2026

| Verificação | Resultado e limite |
|---|---|
| Reinspeção de fontes, estrutura e configuração | 19 classes/1 interface/0 enums; nenhum arquivo de teste além de .gitkeep; nenhuma planilha/CSV em dados |
| Sintaxe do script Python por ast.parse | Válida; 14 funções. Não importa nem executa o script |
| Estrutura XML do pom.xml | Bem formada; não comprova build ou resolução das dependências |
| Validação dos quatro documentos oficiais | 22 links relativos existentes; blocos Markdown e tabelas consistentes; 18 RN com os sete campos e cenários; cinco riscos com os cinco campos; 36 itens de checklist acadêmico |
| Integridade do escopo documental | Comparação de hashes confirmou mudanças somente nos quatro caminhos autorizados, sem exclusões; código, configurações, diagramas e demais documentos preservados |
| Ambiente | Python 3.11.9 localizado; pandas/openpyxl disponíveis; win32com e xlrd não localizados. Versões mínimas Python/Maven não fixadas no repositório |
| Ferramentas Java | java, javac e mvn não encontrados no PATH consultado; não é prova de ausência em toda a máquina |
| Ensaio isolado Python em memória | 18 assertivas aprovadas: seis de normalizar_contrato, seis de remoção de prefixos em tratar_contrato_chip, seis do cruzamento e cores em cruzar_analitico; funções extraídas da AST, sem executar inicialização/limpeza do script |
| Primeira tentativa desse ensaio | Falhou na impressão de emoji por encoding cp1252 do terminal; repetida com stdout UTF-8, conforme configuração existente no script, e aprovada. Não classificada como defeito do script |
| Fluxo completo Python/Excel | Não executado: falta win32com; Excel não verificado; fluxo grava/exclui dados |
| Compilação e execução Java | Não executadas; ferramentas fora do PATH e auditoria restrita a documentação |
| Testes versionados de domínio e persistência | Não existentes; os ensaios em memória não equivalem a uma suíte nem comprovam equivalência integral Python/Java |

### Git e documentação

Cinco commits locais, entre 03/11/2026 e 06/11/2026. Autoria: Jefferson Deylane (2), Jefferson Deylane Costa dos Santos (2), unknown (1). As variantes podem pertencer à mesma pessoa; não comprovam grupo de três/quatro integrantes ou colaboração completa. Uso de Git é comprovado; composição da equipe, apresentação e envio não são verificáveis.

Existem nove arquivos Mermaid individuais. O documento combinado repete os oito primeiros; o diagrama de usuários está separado. Mostram classes ainda ausentes, assinatura temPermissao inexistente e ID Long no principal, contra int no código. O módulo de processamento não está integralmente representado. Não foram alterados nesta tarefa.

Nesta revisão, somente README.md, regras/REGRAS_DE_NEGOCIO.md, requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md e este status são criados/atualizados. A documentação anterior fica preservada como legado; seus IDs RN antigos não devem ser usados como referência à especificação nova sem conferir o significado.

Próxima atualização: registrar novo commit examinado, repetir inventário, executar validações disponíveis, atualizar contagens e evidências, e distinguir novamente especificação de implementação. Nenhum percentual global é tratado como nota acadêmica.
