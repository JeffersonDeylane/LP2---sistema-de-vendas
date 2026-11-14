#rodar main:
 & ".\FERRAMENTAS\apache-maven-3.10.0\bin\mvn.cmd" exec:java "-Dexec.mainClass=Main"
usando o maven por causa do tratamento de planilha da transpiração.
# Sistema de Gestão Comercial Flexível

## Atualização P0-01 — 14/11/2026

`Usuario` agora possui as especializações `Vendedor`, `Apoio` e `Administrador`.
O `Main` demonstra suas capacidades distintas por referências `Usuario`, incluindo
o bloqueio de autoaprovação por ID. Essas consultas ainda dependem dos futuros
serviços de autenticação e autorização por escopo.

O build atual usa o `pom.xml` da raiz: `mvn test`. Foram executados **19 testes,
sem falhas ou erros** (14 de usuários e 5 de processamento), além da demonstração
do menu com as opções 2 e 0. Consulte as [decisões, comandos e limites da entrega](docs/planejamento/P0-01_USUARIOS.md).
As seções abaixo preservam a revisão histórica de 08/11/2026; suas referências a
build aninhado, ausência de testes e perfis incompletos estão desatualizadas.

Projeto de Linguagem de Programação II em Java, em evolução para uma plataforma de gestão comercial de produtos e serviços de diferentes segmentos.

A proposta é reunir catálogo configurável, vendas com múltiplos itens, contratos opcionais, comissões, usuários, solicitações e histórico. O público-alvo são vendedores, equipes de apoio e administradores que precisam preservar condições comerciais e revisar divergências de fontes externas.

**Estágio atual:** há um início de aplicação por terminal e um módulo separado de processamento de planilhas de telecomunicações. O núcleo comercial flexível ainda não foi implementado. As instruções Java abaixo foram conferidas contra os caminhos/classes, mas **não executadas nesta revisão**: JDK/Maven não foram encontrados no PATH consultado.

## Documentação oficial

| Documento | Finalidade |
|---|---|
| [Especificação das Regras de Negócio](docs/regras/REGRAS_DE_NEGOCIO.md) | Fonte oficial do comportamento comercial pretendido |
| [Requisitos Acadêmicos — Unidade 03](docs/requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md) | Critérios do professor, checklist e evidências |
| [Status Atual e Diagnóstico Técnico](docs/STATUS_DO_PROJETO.md) | Inventário real, lacunas, verificações e prioridades |
| [UML existente](docs/diagramas/DIAGRAMAS_COMPLETOS.md) | Planejamento legado, ainda não correspondente à visão flexível nem a todo o código |
| [Diagrama separado de usuários](docs/diagramas/09-usuários.mmd) | Complemento de planejamento; também requer atualização |

Os documentos em docs/planejamento e os diagramas foram preservados nesta revisão. Referências antigas a RN01–RN11, produto principal de telecomunicações e contrato obrigatório são históricas; não substituem a nova especificação. As regras genéricas atuais têm numeração própria na versão documental 2.

## Funcionalidades e disponibilidade

### Implementadas no código

- Menu por terminal com opções de descrição, tentativa de login e saída. A opção de login apenas informa que está pendente.
- Usuario abstrato, Vendedor e validação de atributos; exemplos fixos executados antes do menu.
- Processamento especializado de XLS/XLSX/CSV, com indicadores comerciais e identificação da empresa de origem.
- Conciliação de contratos com abas complementares da Claro.
- Consolidação dos analíticos em vendas.xlsx.
- Relatórios por supervisor/vendedor e por vendedor/data de instalação.

As cinco operações de processamento estão em um módulo independente, sem integração ao menu. “Implementado no código” não significa execução completa validada. Python e Java oferecem versões semelhantes dessas operações, sem duplicar a contagem.

### Parcialmente implementadas

- Permissões: Vendedor nega aprovação e gestão de usuários e consulta ativo para permitir solicitação; faltam os outros perfis e as operações protegidas.
- Situação de usuário: ativação/desativação em memória; não há transições comerciais controladas.
- Entrada/saída de planilhas: grava resultados, mas não persiste nem recupera entidades da aplicação.

### Planejadas

- Categorias, tipos de unidade e unidades cadastrados dinamicamente.
- Produtos/serviços com categoria e unidade opcional; item sem unidade tem quantidade 1.
- Clientes, vendas com múltiplos itens e contrato opcional do mesmo cliente.
- Fotografia histórica de preço, unidade, quantidade e condições comerciais.
- Políticas percentuais de comissão configuráveis, sem taxas universais por segmento.
- Autenticação, Vendedor/Apoio/Administrador e autorização nos serviços.
- Solicitações sem autoaprovação, revisão de divergências, cancelamento e reativação rastreáveis.
- Histórico preservado, conciliação idempotente e recuperação automática dos dados.

Não fazem parte desta etapa estoque, emissão fiscal, gateway de pagamento, API REST, microserviços ou conversão automática de unidades.

## Tecnologias e pré-requisitos

As versões declaradas pertencem ao [pom.xml do módulo](src/main/python/java_transpilado_lp2/pom.xml). O repositório não possui build Maven na raiz.

| Tecnologia | Versão/evidência | Necessidade |
|---|---|---|
| JDK | Java 17 declarado por maven.compiler.release | Compilar/executar Java; usar JDK, não apenas JRE |
| Maven | Versão não fixada; sem wrapper | Somente processamento Java |
| Apache POI poi-ooxml | 5.4.1 | Excel no módulo Java |
| Apache Commons CSV | 1.13.0 | CSV no módulo Java |
| Maven Compiler Plugin | 3.13.0 | Compilação do módulo |
| Exec Maven Plugin | 3.5.0 | Execução do ponto de entrada do módulo |
| Python | Versão mínima não declarada; interpretador observado na análise: 3.11.9 | Somente alternativa Python |
| pandas e openpyxl | Importados pelo script; versões não fixadas | Processamento Python |
| pywin32 / win32com.client | Importado pelo script; versão não fixada | Automação do Excel no Windows |
| Microsoft Excel desktop | Versão não declarada e instalação não verificada | Tabelas dinâmicas do Python; dispensável no Java |
| xlrd | Não importado diretamente nem fixado; mecanismo adicional para XLS no pandas | Quando utilizar XLS no Python |

Git é necessário para obter o projeto por clone. Maven precisa de acesso aos repositórios de dependências no primeiro build. Não há requirements.txt, ambiente Python fixado ou suíte de testes configurada.

Na análise de 08/11/2026, Python, pandas e openpyxl estavam disponíveis; win32com e xlrd não foram localizados. Java/javac/Maven não foram encontrados no PATH. Isso descreve o ambiente consultado, não uma ausência comprovada em toda a máquina.

## Instalação e execução

Os comandos são para **PowerShell**, a partir da raiz do repositório, salvo indicação contrária. São instruções para o usuário executar posteriormente; não foram instaladas dependências nem gerados artefatos nesta revisão documental.

### 1. Obter o projeto

Se já possui o repositório, abra um terminal na pasta existente. Para uma nova cópia, informe a URL verdadeira no prompt; o link oficial ainda deve ser preenchido pela equipe:

~~~powershell
$urlRepositorio = Read-Host "Informe a URL Git do repositorio"
git clone "$urlRepositorio" "LP2---sistema-de-vendas"
Set-Location "LP2---sistema-de-vendas"
~~~

O clone não foi executado na revisão. Não há URL fictícia apresentada como endereço oficial.

### 2. Conferir o ambiente

Instale/configure JDK 17 e, para o módulo de planilhas, Maven. Disponibilize seus executáveis no PATH e confira:

~~~powershell
java -version
javac -version
mvn -version
~~~

Para a alternativa Python:

~~~powershell
python --version
~~~

Python 3.11.9 foi observado, mas não é declarado como versão mínima ou única suportada. A equipe ainda deve fixar/validar as versões Python e Maven em uma máquina de apresentação.

### 3. Compilar e iniciar o menu Java

A aplicação inicial usa apenas a biblioteca padrão Java. Compile os quatro arquivos explicitamente:

~~~powershell
javac -encoding UTF-8 -d out src/main/java/Main.java src/main/java/Usuario.java src/main/java/Vendedor.java src/main/java/Validador.java
java -cp out Main
~~~

Esses comandos se referem ao menu atual, sem o processamento de planilhas. Não foram verificados por execução nesta revisão. A pasta out é artefato local ignorado pelo Git.

**Uso atual:** aparecem os dados fictícios e mensagens de validação da demonstração; depois, digite 2 para ler a descrição, 1 para ver o aviso de login não implementado e 0 para sair. Sequência demonstrativa de entradas: 2, depois 0. O resultado é esperado pela leitura do código, sem execução Java comprovada.

Não há credencial de acesso funcional, cadastro/consulta comercial ou importação pelo menu. O nome antigo ainda aparece no terminal.

### 4. Preparar dados da integração especializada

Use somente amostras fictícias ou arquivos de trabalho mantidos fora do versionamento. Os padrões XLS/XLSX/CSV e as saídas são ignorados pelo .gitignore.

~~~powershell
New-Item -ItemType Directory -Force -Path "dados/importacao/analitico_da_claro", "dados/exportacao/arquivos_para_processar/saida"
~~~

- Entradas: arquivos diretamente em dados/importacao, sem busca recursiva.
- Base complementar: dados/importacao/analitico_da_claro/claro.xlsx.
- Aba preferida: Analítico; se ausente, é lida a primeira.
- No Java, Produtos e Contrato são colunas obrigatórias.
- Supervisor, Vendedor, FormaPagamento, DataInstalacao e Valor são usados na classificação e nos relatórios; a ausência pode tornar o resultado incompleto.
- CSV Java: UTF-8, vírgula ou ponto e vírgula por detecção simples. O Python usa a leitura padrão de CSV, sem a mesma detecção.
- O nome do arquivo define VNA quando contém esse texto; os demais nomes são tratados como RDT. Não use arquivos genéricos de outros segmentos esperando adaptação automática.
- Valor é campo numérico da origem, tratado historicamente como comissão nesse contexto; não é convertido automaticamente em preço ao cliente.

| Aba complementar | Colunas esperadas, com grafia do código |
|---|---|
| virtua | NM_INDICADOR_NEGOCIO, NR_CONTRATO, NM_PARCEIRO |
| tv | NM_INDICADOR_NEGOCIO, NM_PARCEIRO, DT |
| chip | DT_ATIVACAO, CD_COMTA, CONTRATO_NET |
| tel fixo | NM_INDICADOR_NEGOCIO, NM_PARCEIRO, NR_CONTRATO |

Não foi confirmado um arquivo real com esses cabeçalhos; o esquema foi extraído do código. Não renomeie CD_COMTA ou DT supondo erro sem conferir a fonte.

### 5. Compilar e executar o processamento Java

Permaneça na raiz. A opção -f seleciona o pom.xml existente e evita a instrução incorreta de executar mvn compile na raiz sem configuração:

~~~powershell
mvn -f src/main/python/java_transpilado_lp2/pom.xml compile
mvn -f src/main/python/java_transpilado_lp2/pom.xml exec:java "-Dexec.mainClass=lp2.processamento.ExecutarProcessamento" "-Dexec.args=."
~~~

Comandos conferidos contra o pom.xml e a classe principal, **não executados nesta revisão**. O argumento ponto representa a raiz a partir do diretório de trabalho; execute os comandos nessa raiz.

O Maven resolve as dependências declaradas no build. Não é necessário copiar classes ou substituir configurações para executar o módulo isolado. O README interno do módulo descreve uma integração manual anterior, ainda não realizada no projeto.

Comportamentos a considerar:

- Sem entradas, retorna aviso.
- Sem claro.xlsx, o Java gera analítico/relatórios com aviso e omite cruzamentos.
- Outra entrada da mesma empresa pode ser ignorada com aviso; não há mesclagem automática.
- Nomes de saída repetidos podem substituir arquivos anteriores; preserve cópias antes de reexecutar.
- Feche as planilhas de destino se estiverem bloqueadas por outro programa.

### 6. Alternativa Python com Excel no Windows

O caminho Java é independente desta alternativa. Para preparar um ambiente Python separado, execute manualmente:

~~~powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install pandas openpyxl pywin32
~~~

Para entradas XLS, também pode ser necessário o mecanismo xlrd:

~~~powershell
.\.venv\Scripts\python.exe -m pip install xlrd
~~~

Os pacotes não têm versões fixadas pelo projeto; instalação e fluxo completo não foram validados nesta revisão. A pasta .venv não está explicitamente ignorada na configuração atual; mantenha o ambiente local fora dos commits e confira git status antes de adicionar arquivos.

**Efeito importante do script:** ao iniciar, ele remove todos os arquivos diretamente em dados/exportacao/arquivos_para_processar/saida, antes de verificar se há entradas. Faça cópia dos resultados necessários. O fluxo Python requer a base complementar e Excel desktop funcionando.

Após preparar esses recursos:

~~~powershell
.\.venv\Scripts\python.exe src/main/python/processar_planilhas_minusculas.py
~~~

O comando não foi executado na revisão. O módulo win32com estava ausente e não houve automação do Excel.

### 7. Localizar os resultados

| Caminho | Conteúdo |
|---|---|
| dados/exportacao/arquivos_para_processar/saida/ | Arquivos por empresa, como VNA - ANALITICO dd.MM.xlsx e RDT - ANALITICO dd.MM.xlsx |
| dados/exportacao/vendas.xlsx | Consolidado dos analíticos |
| Abas Planilha1 e Planilha2 | Resumos no Java; tabelas dinâmicas nativas no Python |

Esses arquivos são saídas da integração, não a base persistente do domínio genérico. BASE_GERAL.xlsx continua planejada e não é carregada pelo Main.

### 8. Testes e limites da validação

Não existe suíte automatizada no repositório. A pasta src/test/java contém apenas .gitkeep. Quando o ambiente Maven estiver preparado, o ciclo pode ser solicitado assim:

~~~powershell
mvn -f src/main/python/java_transpilado_lp2/pom.xml test
~~~

Esse comando não foi executado. Mesmo se retornar sucesso sem testes, isso **não comprova regras, persistência ou funcionalidades**. Também não testa o Main externo ao módulo.

Nesta revisão foram aprovadas 18 assertivas em três funções Python isoladas, usando objetos em memória, além da sintaxe Python e estrutura XML do pom.xml. Não foram criados arquivos de teste; esse ensaio não substitui suíte versionada ou execução ponta a ponta. Detalhes e a limitação de encoding da primeira tentativa estão no diagnóstico técnico.

## Exemplo conceitual da visão futura

**Não executável no sistema atual:** cadastrar categoria “Serviços”, tipo “Tempo”, unidade “Hora” com frações e um produto de consultoria; vender 1,5 hora sem contrato; preservar preço/unidade da venda e calcular comissão pelo percentual configurado. Um serviço sem unidade seria outro item com quantidade exatamente 1.

O exemplo orienta a modelagem e não é uma funcionalidade disponível. Taxas, estados e arredondamento propostos precisam ser confirmados pela equipe na especificação.

## Arquitetura e estrutura

O núcleo inicial de usuários não depende do módulo de planilhas. ProcessadorPlanilhas funciona como fachada de leitura, tratamento, filtros, cruzamento, relatórios e consolidação. RegraProduto define uma estratégia executada por implementações próprias. PlanilhasIO concentra entrada/saída Excel/CSV. Ainda não há MVC completo nem repositórios de entidades.

~~~text
/
├── README.md
├── dados/{exemplos,importacao,exportacao}/
├── docs/
│   ├── STATUS_DO_PROJETO.md
│   ├── regras/REGRAS_DE_NEGOCIO.md
│   ├── requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md
│   ├── planejamento/
│   └── diagramas/
└── src/
    ├── main/
    │   ├── java/{Main,Usuario,Vendedor,Validador}.java
    │   └── python/
    │       ├── processar_planilhas_minusculas.py
    │       └── java_transpilado_lp2/
    │           ├── pom.xml
    │           └── src/main/java/lp2/processamento/
    └── test/java/.gitkeep
~~~

A direção futura separa apresentação, domínio comercial, serviços autorizados, persistência e adaptação de fontes. Categorias e unidades são entidades dinâmicas; regras de telecomunicações ficam na integração. Isso é objetivo arquitetural, não estrutura já concluída.

## Cinco situações de risco

As mitigações abaixo são propostas; não estão todas implementadas. Os cenários usam classes reais, pois o catálogo comercial flexível ainda não existe em código.

### Risco 1 — Operação sem proteção ao integrar usuários

- **Situação:** um futuro fluxo comercial usa o Vendedor criado pelo Main como se houvesse sessão autenticada.
- **Problema:** Usuario apenas armazena dados; Vendedor consulta permissões, mas não há autenticação ou serviço que proteja operações. O menu atual ainda não oferece essas operações.
- **Impacto:** ao integrar funcionalidades sem essa proteção, seria possível atribuir ação a usuário não autenticado, inativo ou fora do seu escopo.
- **Mitigação:** implementar autenticação e exigir identidade, situação e permissão nos serviços antes de disponibilizar operações.
- **Classes relacionadas:** Main, Usuario, Vendedor.

### Risco 2 — Valor importado convertido silenciosamente em zero

- **Situação:** uma planilha traz Valor como 1.234,56 ou texto inválido.
- **Problema:** TabelaDados.numero substitui vírgula por ponto e retorna zero se a conversão falhar; não distingue erro de valor zero.
- **Impacto:** GeradorRelatorios pode apresentar total menor sem indicar qual registro foi descartado.
- **Mitigação:** validar formato monetário por fonte, rejeitar/avisar por registro e usar BigDecimal no futuro domínio comercial.
- **Classes relacionadas:** TabelaDados, GeradorRelatorios, PlanilhasIO.

### Risco 3 — Origem genérica classificada como empresa de telecomunicações

- **Situação:** arquivo de outro segmento, sem VNA no nome, é colocado em dados/importacao.
- **Problema:** a classificação atual assume RDT para nomes sem VNA e aplica filtros de parceiros/códigos específicos.
- **Impacto:** dados podem ser atribuídos à empresa errada ou excluídos inadequadamente; o processamento não é importador genérico.
- **Mitigação:** validar origem explicitamente e desenvolver adaptação especializada antes de fornecer dados ao domínio flexível.
- **Classes relacionadas:** ProcessadorPlanilhas, TratadorAnalitico, ComplementadorClaro.

### Risco 4 — Perda de resultados ao reexecutar

- **Situação:** processamento da mesma empresa é repetido no mesmo dia, ou a alternativa Python é iniciada.
- **Problema:** Java usa nomes por empresa e dd.MM com substituição do destino; o Python limpa previamente todos os arquivos da pasta de saída.
- **Impacto:** resultados anteriores podem ser perdidos; a gravação temporária do Java não é backup ou histórico de versões.
- **Mitigação:** preservar cópias, identificar execuções, definir política de substituição e revisar futuramente a limpeza do script.
- **Classes relacionadas:** ProcessadorPlanilhas, PlanilhasIO; no Python, funções executar e limpar_pasta_saida.

### Risco 5 — Conciliação com esquema complementar incompleto

- **Situação:** claro.xlsx não possui coluna de parceiro ou código esperada em uma aba.
- **Problema:** TabelaDados.selecionarColunas preenche campos ausentes com null; filtros podem deixar de excluir registros e alguns avisos só aparecem no console.
- **Impacto:** relatório de contratos divergentes pode parecer completo sem respeitar o escopo empresarial.
- **Mitigação:** validar cabeçalhos obrigatórios por aba, interromper ou sinalizar lote incompleto e propagar avisos ao resultado.
- **Classes relacionadas:** ComplementadorClaro, TabelaDados, CruzadorContratos, ResultadoProcessamento.

## Regras identificadas, mas não implementadas

A [especificação completa](docs/regras/REGRAS_DE_NEGOCIO.md) contém condições, proibições e cenários de aceitação. A numeração desta tabela corresponde à visão genérica vigente.

| Regras | Pendência atual |
|---|---|
| RN01–RN03 | Categorias, tipos/unidades e produtos dinâmicos; disponibilidade e preservação de vínculos |
| RN04 | Quantidade indivisível sem unidade e frações conforme configuração |
| RN05–RN07 | Cliente, contrato opcional, venda válida e fotografia histórica |
| RN08 | **Parcial:** completar autenticação, perfis, escopo e autorização efetiva |
| RN09–RN10 | Solicitações sem autoaprovação/duplicação e transições controladas |
| RN11–RN13 | Comissão percentual, unicidade, elegibilidade e pagamento rastreado |
| RN14–RN15 | Cancelamento e reativação seletiva sem apagar histórico |
| RN16–RN17 | Adaptação ao domínio genérico e conciliação idempotente |
| RN18 | Histórico persistente e preservado |

Nenhuma das 18 regras genéricas está completa. As quatro políticas comerciais do processamento legado são registradas separadamente no diagnóstico; não substituem o desenvolvimento do núcleo genérico.

## Desenvolvimento colaborativo

O repositório possui histórico Git, mas não comprova uma convenção histórica de branches/commits nem a participação de todos os integrantes. Para os próximos trabalhos, adotam-se como diretrizes documentais, sem alterar configuração:

- Branches por tarefa: docs/descricao, feat/descricao ou fix/descricao.
- Commits com objetivo claro e mudanças de escopo limitado; não misturar implementação e documentação sem necessidade.
- Tarefas com responsável, critério de aceitação e evidência; Issues ou quadro da equipe são possibilidades, não ferramentas já implantadas.
- Revisar impactos nas RN, atualizar status/UML quando pertinente e executar testes disponíveis antes de integrar.
- Identificar corretamente autoria e contribuição de cada integrante; não usar variantes de nome como prova de pessoas diferentes.
- Preservar português nos identificadores do domínio, convenções Java e nomes próprios de APIs externas.
- Não versionar credenciais, dados comerciais reais ou ambientes locais. Conferir o conteúdo antes de adicionar arquivos.

A suíte de testes ainda precisa ser criada. Os cinco commits existentes não demonstram, por si, colaboração de três/quatro pessoas.

## Integrantes

Preencher com a composição real, de três ou quatro integrantes, sem inventar dados:

| Integrante | Nome completo | Matrícula |
|---|---|---|
| 1 | A preencher | A preencher |
| 2 | A preencher | A preencher |
| 3 | A preencher | A preencher |
| 4, se aplicável | A preencher ou remover linha | A preencher |

## Repositório e apresentação acadêmica

- **Link oficial do repositório:** a preencher pela equipe.
- **Prazo e turma:** conferir no enunciado/SIGAA.
- **Responsável único pelo envio:** a definir.
- **Pacote de código-fonte ZIP:** preparar e conferir na entrega; ainda não gerado.
- **Apresentação:** demonstrar sistema interativo funcionando, diagrama, cinco riscos, código e decisões de modelagem.
- **Envio:** por apenas um integrante no SIGAA, conforme requisitos fornecidos.

A apresentação é obrigatória. Conforme a atividade transcrita, ausência de demonstração ou sistema que não execute implica nota zero. Confirmar o texto original e validar a máquina de apresentação antes da entrega. O [checklist acadêmico](docs/requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md) mantém as pendências e evidências.
