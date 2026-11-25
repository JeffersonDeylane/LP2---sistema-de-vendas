# Sistema de vendas de telecomunicações

Projeto de Linguagem de Programação II em Java para registrar vendas e contratos de telecomunicações, acompanhar a instalação dos produtos, revisar divergências de planilhas de parceiros e apurar comissões por vendedor. A estratégia segue as [regras RN01–RN11](docs/regras/REGRAS_DE_NEGOCIO.md) e o [modelo-alvo](docs/diagramas/DIAGRAMAS_COMPLETOS.md).

## Atualização P1 — Cadastro de vendas — 22/11/2026

O perfil `Vendedor` já existente agora pode cadastrar clientes e registrar suas
próprias vendas. Um administrador cadastra produtos; vendedor ou administrador
pode cadastrar contratos vinculados a um cliente. A venda aceita vários itens,
com contrato opcional, é salva como rascunho e só passa a confirmada após ação
explícita. O serviço revalida o usuário ativo persistido, o vínculo do contrato e
o escopo do vendedor; o menu não é a única barreira de autorização.

Para usar o fluxo, entre com uma conta existente e ativa:

1. Como administrador, use **7** para cadastrar ao menos um produto.
2. Como vendedor ou administrador, use **6** para cadastrar o cliente e, se
   necessário, **8** para associar um contrato ao cliente.
3. Use **9** para montar uma venda com um ou mais itens. Deixe o contrato em
   branco para uma venda avulsa.
4. Use **10** para confirmar a venda e **11** para consultar as vendas visíveis
   ao perfil.

As abas `CLIENTES`, `PRODUTOS`, `CONTRATOS`, `VENDAS` e `ITENS_VENDA` são
persistidas no mesmo `dados/BASE_GERAL.xlsx` da aba `USUARIOS`, com backup e
gravação atômica. Consulte o [esquema e as limitações](docs/planejamento/ESTRUTURA_DADOS.md).

**Limites desta entrega:** categoria e unidade são descrições de texto, sem
cadastro próprio; não há edição/inativação pela interface, cancelamento,
conclusão, auditoria completa, comissões ou solicitações de alteração. A
fotografia ao confirmar preserva código/nome/categoria/unidade, quantidade,
preço e subtotal, mas ainda não a permissão de fração ou uma política de
comissão. Subtotais são arredondados por item com `HALF_UP` para duas casas.
Apoio não opera vendas porque ainda não existe atribuição de escopo para esse
perfil.

## Login local simples — 16/11/2026

Execute na raiz pelo PowerShell:

```powershell
& ".\FERRAMENTAS\apache-maven-3.10.0\bin\mvn.cmd" compile exec:java "-Dexec.mainClass=Main"
```

Use **4** para cadastrar um vendedor (nome, login, senha e matrícula), **1**
para entrar, **5** para sair da conta e **0** para encerrar. A opção **3** exige
login. As contas são recuperadas da aba `USUARIOS` de `dados/BASE_GERAL.xlsx`.

Validação deste upgrade: compilação com Temurin 25.0.5.1 para Java 25 e **84
testes aprovados** com Maven. Os artefatos da validação ficam em `target/`,
ignorado pelo Git.

Veja [EXPLICACAO_LOGIN.txt](EXPLICACAO_LOGIN.txt) para detalhes das mudanças,
do código reaproveitado do Marcone e dos limites.



## Atualização P0-03 — 14/11/2026

`RepositorioUsuariosExcel` salva e atualiza os três perfis na aba `USUARIOS` de
`dados/BASE_GERAL.xlsx`, preservando IDs, demais usuários e outras abas. A base é
lida na inicialização. Arquivo ausente não cria contas; base inválida interrompe
a inicialização com uma mensagem de erro e não é sobrescrita.

Para executar com o Maven da raiz:

```powershell
mvn compile exec:java "-Dexec.mainClass=Main"
# Caminho alternativo da base, passado como primeiro argumento:
mvn exec:java "-Dexec.mainClass=Main" "-Dexec.args=dados/BASE_GERAL.xlsx"
mvn test
```

Cada atualização prepara e verifica um temporário, guarda a versão anterior em
`BASE_GERAL.xlsx.bak` e exige substituição atômica. A base, o backup e seus
temporários padrão estão no `.gitignore`. Não são gerados usuários administrativos
nem persistidos os exemplos da demonstração. Login e cadastro administrativo chegaram na entrega de 16/11/2026.

Foram executados **54 testes, sem falhas, erros ou ignorados**. Veja o
[esquema implementado](docs/planejamento/ESTRUTURA_DADOS.md) e o
[relatório da P0-03](docs/planejamento/P0-03_PERSISTENCIA_USUARIOS.md), incluindo
API, comandos reais de validação e limitações de concorrência.

## Atualização P0-01 — 14/11/2026

`Usuario` agora possui as especializações `Vendedor`, `Apoio` e `Administrador`.
O `Main` demonstra suas capacidades distintas por referências `Usuario`, incluindo
o bloqueio de autoaprovação por ID. Essas consultas ainda dependem dos futuros
serviços de autenticação e autorização por escopo.

O build atual usa o `pom.xml` da raiz: `mvn test`. Foram executados **19 testes,
sem falhas ou erros** (14 de usuários e 5 de processamento), além da demonstração
do menu com as opções 2 e 0. Consulte as [decisões, comandos e limites da entrega](docs/planejamento/P0-01_USUARIOS.md).
## Documentação do projeto

| Documento | Finalidade |
|---|---|
| [Regras de negócio](docs/regras/REGRAS_DE_NEGOCIO.md) | Especificação RN01–RN11 de telecomunicações |
| [Checklist](docs/planejamento/CHECKLIST.md) | Entregas, dependências e critérios de conclusão |
| [Diagramas 01–09](docs/diagramas/README.md) | Estruturas do código atual e seus estados de implementação |
| [Modelo-alvo](docs/diagramas/DIAGRAMAS_COMPLETOS.md) | Contratos, estados, comissões, revisão e cascata |
| [Diagnóstico técnico](docs/STATUS_DO_PROJETO.md) | Fluxos ativos e lacunas |
| [Requisitos da Unidade 03](docs/requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md) | Critérios acadêmicos e evidências |

## Estratégia e funcionalidades

- **Operação atual.** O terminal oferece login, cadastro de usuários e cadastro inicial de clientes, produtos, contratos e vendas. A persistência usa `dados/BASE_GERAL.xlsx`.
- **Integração atual.** O menu executa o processamento RDT/VNA, cruza dados com a base Claro quando disponível e gera analíticos e consolidados.
- **Próxima etapa.** Estruturar contrato com múltiplos produtos e estado por item, integrar a revisão por campo e o histórico e implementar comissão, elegibilidade e cancelamento/reativação em cascata. A Mudança de Tecnologia terá classificação própria nos relatórios.

## Tecnologias e pré-requisitos

As dependências Java estão no [pom.xml da raiz](pom.xml).

| Tecnologia | Versão/evidência | Necessidade |
|---|---|---|
| JDK | Java 21 declarado por maven.compiler.release | Compilar/executar Java; usar JDK, não apenas JRE |
| Maven | Versão não fixada; sem wrapper | Aplicação e testes Java |
| Apache POI poi-ooxml | 5.4.1 | Excel no módulo Java |
| Apache Commons CSV | 1.13.0 | CSV no módulo Java |
| Maven Compiler Plugin | 3.13.0 | Compilação do módulo |
| Exec Maven Plugin | 3.5.0 | Execução do ponto de entrada do módulo |
| Python | Versão mínima não declarada; interpretador observado na análise: 3.11.9 | Somente alternativa Python |
| pandas e openpyxl | Importados pelo script; versões não fixadas | Processamento Python |
| pywin32 / win32com.client | Importado pelo script; versão não fixada | Automação do Excel no Windows |
| Microsoft Excel desktop | Versão não declarada e instalação não verificada | Tabelas dinâmicas do Python; dispensável no Java |
| xlrd | Não importado diretamente nem fixado; mecanismo adicional para XLS no pandas | Quando utilizar XLS no Python |

Git é necessário para obter o projeto por clone. Maven precisa de acesso aos repositórios de dependências no primeiro build. As dependências Python são instaladas separadamente.

Na análise de 08/11/2026, Python, pandas e openpyxl estavam disponíveis; win32com e xlrd não foram localizados. Java/javac/Maven não foram encontrados no PATH. Isso descreve o ambiente consultado, não uma ausência comprovada em toda a máquina.

## Instalação e execução

Os comandos são para **PowerShell**, a partir da raiz do repositório, salvo indicação contrária. São instruções para o usuário executar posteriormente; não foram instaladas dependências nem gerados artefatos nesta revisão documental.

### 1. Obter o projeto

Se já possui o repositório, abra um terminal na pasta existente. Para obter uma cópia, use a URL do repositório na seção de entrega.

~~~powershell
$urlRepositorio = Read-Host "Informe a URL Git do repositorio"
git clone "$urlRepositorio" "LP2---sistema-de-vendas"
Set-Location "LP2---sistema-de-vendas"
~~~

### 2. Conferir o ambiente

Configure JDK 21 ou superior e Maven. Disponibilize seus executáveis no PATH e confira:

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

~~~powershell
mvn compile exec:java "-Dexec.mainClass=Main"
~~~

Entre com uma conta ativa em **1**. Use **3** para processar as planilhas, **5** para sair da conta e **0** para encerrar. As opções de cadastro, confirmação e consulta estão descritas na atualização P1 acima.

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

Use a opção **3** do menu após autenticar ou execute o processamento diretamente da raiz:

~~~powershell
mvn compile exec:java "-Dexec.mainClass=lp2.processamento.ExecutarProcessamento" "-Dexec.args=."
~~~

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

O cadastro da aplicação fica em `dados/BASE_GERAL.xlsx`. As saídas acima são geradas pelo processamento RDT/VNA.

### 8. Testes

~~~powershell
mvn test
~~~

A suíte em `src/test/java/` cobre usuários, autenticação, persistência, vendas e processamento de planilhas. Consulte o [diagnóstico](docs/STATUS_DO_PROJETO.md) para o registro das execuções anteriores.

## Exemplo do fluxo comercial planejado

Em um contrato com VIRTUA, TV e CHIP, cada item terá seu próprio estado e comissão. VIRTUA é o produto principal e seu estado determina o estado geral do contrato. Se uma importação trouxer comissão diferente da registrada, Apoio ou ADM revisa o campo antes de aplicar a mudança. O cancelamento da TV agrupa os itens ativos afetados, preservando VIRTUA. A reativação pela raiz restaura para CADASTRADA os itens aptos daquele grupo.

## Arquitetura

`Main` integra `ServicoAutenticacao`, `ServicoVendas` e `ProcessadorPlanilhas`. Os repositórios Excel de usuários e vendas compartilham a base central. O processamento usa `PlanilhasIO` para ler arquivos de entrada e gravar relatórios. Os [diagramas](docs/diagramas/README.md) registram a integração prevista dos serviços de contrato, importação e cancelamento.

## Cinco situações de risco

As mitigações abaixo orientam a evolução dos serviços atuais.

### Risco 1 — Revisão ou reativação fora da permissão do perfil

- **Situação:** um vendedor solicita correção ou reativação de uma venda.
- **Problema:** se o novo serviço confiar apenas na opção exibida pelo menu, uma chamada direta pode contornar a permissão.
- **Impacto:** uma alteração pode ser aplicada por usuário fora do próprio escopo ou sem autorização de Apoio/ADM.
- **Mitigação:** aplicar no serviço a validação de sessão, perfil e escopo e registrar o autor da decisão.
- **Classes relacionadas:** `ServicoAutenticacao`, `ServicoVendas`, `ServicoImportacao`, `ServicoCancelamento` e `Usuario`.

### Risco 2 — Valor importado convertido silenciosamente em zero

- **Situação:** uma planilha traz Valor como 1.234,56 ou texto inválido.
- **Problema:** TabelaDados.numero substitui vírgula por ponto e retorna zero se a conversão falhar; não distingue erro de valor zero.
- **Impacto:** GeradorRelatorios pode apresentar total menor sem indicar qual registro foi descartado.
- **Mitigação:** validar formato monetário por fonte, rejeitar/avisar por registro e usar BigDecimal no futuro domínio comercial.
- **Classes relacionadas:** TabelaDados, GeradorRelatorios, PlanilhasIO.

### Risco 3 — Arquivo classificado no parceiro incorreto

- **Situação:** arquivo da VNA renomeado sem VNA no nome, é colocado em dados/importacao.
- **Problema:** a classificação atual assume RDT para nomes sem VNA e aplica filtros de parceiros/códigos específicos.
- **Impacto:** dados podem ser atribuídos à empresa errada ou excluídos inadequadamente.
- **Mitigação:** validar a origem do lote antes de comparar os contratos da base.
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

## Regras de telecomunicações pendentes

| Regra | Situação atual e entrega pendente |
|---|---|
| RN01 — Revisão de contratos | O cruzamento de planilhas identifica diferenças. Falta registrar divergências e aplicar aprovação/recusa por campo pelo Apoio/ADM, preservando o valor recusado. Confirmar se o número do contrato é único por empresa. |
| RN02 — Perfis | Login, cadastro administrativo e escopo das próprias vendas já funcionam. Faltam sugestões e pedidos de reativação pelo Vendedor, revisão e reativação por Apoio/ADM, manutenção de usuários e relatórios gerais para ADM. |
| RN03 — Contratos e itens | A venda pode ter vários itens e vínculo com contrato. Falta completar o modelo de telecomunicações com quantidade, estado e comissão por item e contagens distintas de contratos e itens. |
| RN04 — Estados | O fluxo ativo confirma vendas em rascunho. Falta integrar por item o ciclo CADASTRADA → AGENDADA → INSTALADA, o cancelamento nos três estados e o retorno autorizado para CADASTRADA. |
| RN05 — Produto principal | Falta aplicar VIRTUA > TV > CHIP > FONE e derivar o estado geral do contrato. Confirmar empates e contratos só com adicionais. |
| RN06 — Comissões | Falta calcular por plano, combo, quantidade ou operação, guardar separadamente preço, comissão informada e calculada e encaminhar divergências para revisão. A tabela completa de planos, vigências e combos ainda precisa de confirmação. |
| RN07 — Conflitos | Falta pedir confirmação explícita ao Apoio quando houver exceção comercial permitida, respeitar as validações obrigatórias e registrar a decisão. |
| RN08 — Histórico | Falta persistir responsável, perfil, data/hora, campo, antes/depois, motivo e autorizações para alterações, cancelamentos e reativações. |
| RN09 — Mudança de Tecnologia | Falta registrar a operação separadamente, aplicar R$ 5,00 por operação e apresentá-la fora da contagem de vendas comerciais. |
| RN10 — Elegibilidade | Falta apurar elegibilidade de venda instalada e de Mudança de Tecnologia cadastrada, cuja elegibilidade termina no cancelamento. Preservar apurações e confirmar critérios para itens secundários e pagamentos já realizados. |
| RN11 — Cascata | Falta cancelar os itens ativos conforme a origem (VIRTUA atinge todos, TV exclui VIRTUA, demais cancelam apenas o próprio item) e permitir que Apoio/ADM reative pela raiz apenas os itens afetados pelo grupo original, preservando cancelamentos anteriores e mudanças posteriores. |

Consulte o [checklist](docs/planejamento/CHECKLIST.md) para dependências, valores comerciais e critérios de conclusão.

## Desenvolvimento colaborativo

O repositório possui histórico Git, mas não comprova uma convenção histórica de branches/commits nem a participação de todos os integrantes. Para os próximos trabalhos, adotam-se como diretrizes documentais, sem alterar configuração:

- Branches por tarefa: docs/descricao, feat/descricao ou fix/descricao.
- Commits com objetivo claro e mudanças de escopo limitado; não misturar implementação e documentação sem necessidade.
- Tarefas com responsável, critério de aceitação e evidência; Issues ou quadro da equipe são possibilidades, não ferramentas já implantadas.
- Revisar impactos nas RN, atualizar status/UML quando pertinente e executar testes disponíveis antes de integrar.
- Identificar corretamente autoria e contribuição de cada integrante; não usar variantes de nome como prova de pessoas diferentes.
- Preservar português nos identificadores do domínio, convenções Java e nomes próprios de APIs externas.
- Não versionar credenciais, dados comerciais reais ou ambientes locais. Conferir o conteúdo antes de adicionar arquivos.

Cada integrante deve registrar suas contribuições e evidências de teste.

## Integrantes

Equipe registrada no checklist. Confirmar nomes completos e matrículas.

| Integrante | Nome completo | Matrícula |
|---|---|---|
| 1 | Jefferson | A preencher |
| 2 | Marcone | A preencher |
| 3 | Faby | A preencher |

## Repositório e apresentação acadêmica

- **Repositório:** [LP2 — Sistema de vendas](https://github.com/JeffersonDeylane/LP2---sistema-de-vendas).
- **Prazo e turma:** conferir no enunciado/SIGAA.
- **Responsável único pelo envio:** a definir.
- **Pacote de código-fonte ZIP:** preparar e conferir na entrega; ainda não gerado.
- **Apresentação:** demonstrar sistema interativo funcionando, diagrama, cinco riscos, código e decisões de modelagem.
- **Envio:** por apenas um integrante no SIGAA, conforme requisitos fornecidos.

A apresentação é obrigatória. Conforme a atividade transcrita, ausência de demonstração ou sistema que não execute implica nota zero. Confirmar o texto original e validar a máquina de apresentação antes da entrega. O [checklist acadêmico](docs/requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md) mantém as pendências e evidências.
