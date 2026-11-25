# Requisitos Acadêmicos — Atividade Unidade 03

**Avaliação documental atualizada em 24/11/2026.**

Este documento organiza os requisitos da atividade transcritos pela equipe nesta solicitação. O enunciado original do professor não está versionado no repositório; a equipe deve conferir a transcrição antes da entrega, sem interpretar decisões de produto como exigências acadêmicas adicionais.

As regras comerciais de telecomunicações RN01–RN11 são definidas na [especificação](../regras/REGRAS_DE_NEGOCIO.md). O [diagnóstico técnico](../STATUS_DO_PROJETO.md) detalha as evidências. O [README](../../README.md) contém apresentação, comandos de uso e situações de risco.

## 1. Como interpretar o checklist

| Estado | Significado |
|---|---|
| IMPLEMENTADO | Evidência identificável no código ou documento; execução comprovada é informada separadamente |
| PARCIALMENTE IMPLEMENTADO | Existe parte do requisito, com lacunas relevantes |
| NÃO IMPLEMENTADO | Implementação ou documento necessário não identificado |
| NÃO VERIFICÁVEL | Evidências insuficientes, ambiente indisponível ou fato externo ainda não comprovado |
| NÃO APLICÁVEL | Alternativa que não foi adotada ou critério sem aplicação no escopo; não dispensa exigência equivalente |

Os estados refletem o código reinspecionado e os quatro documentos oficiais desta revisão. Não representam funcionalidades futuras. A presença de um diagrama, nome de método ou descrição de regra não comprova execução. Não existe suíte versionada de testes de negócio; o ensaio isolado de funções Python em memória está descrito no diagnóstico e não equivale a essa suíte.

## 2. Requisitos de orientação a objetos e aplicação

| ID | Requisito | Critério de aceitação | Status atual | Evidência | Pendência |
|---|---|---|---|---|---|
| RA01 | Mínimo de 10 classes próprias contabilizáveis | Dez classes de domínio justificadas, excluídas as categorias da seção 3 | PARCIALMENTE IMPLEMENTADO | Hierarquia de usuários, Cliente, Contrato, Produto, Venda e ItemVenda são candidatos | Completar a matriz com classes de domínio efetivamente usadas e justificar cada contagem |
| RA02 | Encapsulamento de todas as entidades | Estado protegido e alterações pelos comportamentos da entidade | IMPLEMENTADO | Estado privado na hierarquia de usuários e nas entidades comerciais em uso | Preservar encapsulamento nas operações de telecomunicações |
| RA03 | Duas situações relevantes de polimorfismo | Tipos próprios com contrato comum, comportamentos distintos e uso efetivo em operações do domínio | PARCIALMENTE IMPLEMENTADO | Perfis especializam capacidades e RegraProduto despacha estratégias no processamento | Demonstrar dois casos relevantes e integrar as políticas de comissão/elegibilidade |
| RA04 | Pelo menos 10 regras de negócio relevantes | Regras implementadas, não apenas documentadas; sem contar validação exclusiva de formato | PARCIALMENTE IMPLEMENTADO | RN02 e RN03 têm implementação parcial. A integração aplica políticas específicas de telecomunicações | Implementar e demonstrar pelo menos dez regras, conforme RN01–RN11 e critérios de contagem |
| RA05 | Operações entre múltiplas classes e regras associadas | Fluxos completos demonstram colaboração de classes e condições do domínio | PARCIALMENTE IMPLEMENTADO | ServicoVendas coordena usuários, clientes, produtos, contratos e itens. ProcessadorPlanilhas coordena cruzamento e relatórios | Demonstrar revisão, estados, comissões e cascata |
| RA06 | Exceções personalizadas em situações reais | Exceção própria efetivamente lançada e tratada, não só declarada | PARCIALMENTE IMPLEMENTADO | ProcessamentoException em cabeçalhos/colunas obrigatórias e falhas de I/O; captura por arquivo | Tratar falhas finais em ExecutarProcessamento e demonstrar casos reais |
| RA07 | Validação com exceções | Entrada inválida lança exceção, preserva estado e recebe tratamento adequado | IMPLEMENTADO | Validador lança IllegalArgumentException; Main captura matrícula/login vazios | Criar verificações automatizadas e cobrir futuras operações |
| RA08 | UML completo | Todas as classes, relações e multiplicidades relevantes, correspondentes ao código | PARCIALMENTE IMPLEMENTADO | Nove diagramas retratam o código atual e o consolidado descreve o modelo-alvo de telecomunicações | Atualizar relações e multiplicidades conforme os novos fluxos forem integrados |
| RA09 | Persistência em arquivos e recuperação ao iniciar, ou banco | Salvar entidades, encerrar, reiniciar e recuperar estado e relacionamentos | PARCIALMENTE IMPLEMENTADO | Main recupera usuários e dados comerciais de BASE_GERAL.xlsx. Repositórios gravam com backup e substituição atômica | Incluir dados dos novos fluxos e demonstrar recuperação completa após reinício |
| RA10 | Estado dinâmico com transições controladas | Pelo menos uma classe bloqueia transições inválidas por comportamento | PARCIALMENTE IMPLEMENTADO | Venda passa de RASCUNHO para CONFIRMADA com validação de itens | Completar o ciclo por item de RN04 e demonstrar bloqueio de transições inválidas |
| RA11 | Autenticação e autorização | Verificar credenciais e permissões nas operações protegidas | PARCIALMENTE IMPLEMENTADO | Login, cadastro administrativo e controle de usuário ativo e escopo em operações de venda | Aplicar RN02 a sugestões, revisão, reativação e relatórios |
| RA12 | Dois perfis com permissões diferentes | Dois tipos concretos efetivos, com acesso permitido/negado demonstrado | IMPLEMENTADO | Vendedor, Apoio e ADM são concretos. Cadastro administrativo e escopo de vendas demonstram permissões distintas | Demonstrar permissões dos três perfis nos novos fluxos |
| RA13 | Pelo menos 15 funcionalidades | Operações reais, sem contar métodos ou etapas técnicas da mesma operação separadamente | PARCIALMENTE IMPLEMENTADO | Menu oferece autenticação, cadastros, confirmação, consulta e processamento | Inventariar e demonstrar pelo menos 15 operações distintas |
| RA14 | Pelo menos 7 funcionalidades além do CRUD | Operações adicionais com regras de negócio ou interação entre classes | PARCIALMENTE IMPLEMENTADO | Confirmação e processamento de planilhas colaboram entre classes | Evidenciar pelo menos sete operações além de CRUD com os fluxos de telecomunicações |

As cinco funcionalidades atuais são: processar analítico, conciliar contratos com base complementar, consolidar analíticos, gerar resumo supervisor/vendedor e gerar resumo vendedor/data. Reimplementação Python/Java não duplica a contagem. Execução completa permanece NÃO VERIFICÁVEL.

## 3. Restrições de contagem e validade

| ID | Requisito | Critério de aceitação | Status atual | Evidência | Pendência |
|---|---|---|---|---|---|
| RC01 | Enums não contam no mínimo | Não usar enum para completar RA01 | IMPLEMENTADO | EstadoVenda e StatusVenda são enums excluídos da contagem | Manter o critério |
| RC02 | Serviços, controllers e repositórios não contam | Excluir essas classes do mínimo de dez | IMPLEMENTADO | Serviços e repositórios Excel ficam fora do mínimo de classes de domínio | Manter classificação por responsabilidade |
| RC03 | Subclasses possuem atributos ou comportamentos próprios | Especialização não pode ser vazia | IMPLEMENTADO | Vendedor tem matrícula e podeSolicitarAlteracao | Aplicar aos próximos tipos |
| RC04 | Herança, implementação e polimorfismo demonstrados com tipos próprios | Contratos e especializações pertinentes desenvolvidos pelo grupo | IMPLEMENTADO | Vendedor/Usuario e RegraProduto/implementações são próprios | Herança de RuntimeException não é contada como caso de polimorfismo comercial |
| RC05 | Polimorfismo relevante ao domínio | Não contar sobrescrita/interface artificial ou sem utilização | PARCIALMENTE IMPLEMENTADO | DCC tem significado comercial; detecção textual tem alcance limitado | Demonstrar casos de perfil e comissão/elegibilidade com regras distintas |
| RC06 | Formato de dado não conta como regra comercial | Não contar texto obrigatório, tipo, tamanho ou formato isoladamente | IMPLEMENTADO | Contagem exclui Validador e normalização de contratos | Manter contagem independente de validações técnicas |
| RC07 | Classes de constantes não contam | Não criar classe só de valores para completar mínimo | IMPLEMENTADO | Nenhuma classe de constantes foi contada; auxiliares também foram excluídas | Preservar critério |

RC01–RC07 registram conformidade do critério de contagem e dos casos presentes; não significam que os mínimos RA01/RA03/RA04 tenham sido alcançados.

## 4. Documentação exigida

| ID | Requisito | Critério de aceitação | Status atual | Evidência | Pendência |
|---|---|---|---|---|---|
| RD01 | Regras identificadas mas não implementadas no README | Listagem explícita no documento principal | IMPLEMENTADO | Seção de regras pendentes no README com referência às RN vigentes | Atualizar após cada implementação |
| RD02 | Cinco situações de risco no README | Cinco situações específicas ligadas a classes reais | IMPLEMENTADO | Cinco cenários contextualizados no README | Rever quando o núcleo comercial for ampliado |
| RD03 | Estrutura obrigatória de cada risco | Situação, Problema, Impacto, Mitigação e Classes relacionadas | IMPLEMENTADO | Cinco campos presentes em cada cenário | Não substituir por explicações genéricas |
| RD04 | README com instruções de execução | Pré-requisitos, compilação, execução principal e módulos | PARCIALMENTE IMPLEMENTADO | README documenta o build Maven da raiz, o menu e o processamento | Validar em máquina com Java/Maven e dependências Python |
| RD05 | Identificação da equipe e repositório | Nomes, matrículas e link do repositório preenchidos | PARCIALMENTE IMPLEMENTADO | Equipe e link do repositório registrados no README | Confirmar nomes completos e matrículas |

## 5. Apresentação e entrega

| ID | Requisito | Critério de aceitação | Status atual | Evidência | Pendência |
|---|---|---|---|---|---|
| RE01 | Sistema funcionando | Aplicação compila e executa na apresentação | NÃO VERIFICÁVEL | Há registros de testes anteriores. A execução na máquina de apresentação ainda precisa ser confirmada | Compilar, executar e ensaiar no ambiente da apresentação |
| RE02 | Demonstração interativa por terminal ou GUI | Inserir informações e selecionar operações reais | PARCIALMENTE IMPLEMENTADO | Main recebe credenciais e entradas para cadastros, vendas e processamento | Demonstrar fluxo comercial completo |
| RE03 | Apresentar o diagrama | Explicar classes, relações e multiplicidades correspondentes | NÃO VERIFICÁVEL | UML existe; apresentação não comprovada | Atualizar e apresentar |
| RE04 | Apresentar situações de risco | Explicar os cinco cenários e mitigação | NÃO VERIFICÁVEL | Cenários documentados; apresentação não comprovada | Preparar demonstração/explicação |
| RE05 | Explicar código e decisões de modelagem | Integrantes demonstram compreensão das escolhas | NÃO VERIFICÁVEL | Documentação técnica disponível | Preparar divisão de falas e exemplos |
| RE06 | Grupo de três ou quatro componentes | Identificar integrantes e matrículas | NÃO VERIFICÁVEL | Jefferson, Marcone e Faby constam no checklist | Confirmar nomes completos e matrículas |
| RE07 | Uso colaborativo de Git | Histórico de contribuições identificáveis dos integrantes | NÃO VERIFICÁVEL | O repositório tem histórico de commits | Confirmar identidades e contribuições dos integrantes |
| RE08 | Código-fonte compactado em ZIP | Pacote reproduzível com código/documentação necessários, sem segredos ou dados reais | NÃO VERIFICÁVEL | Não há pacote de entrega identificado | Gerar e conferir ZIP na etapa de entrega |
| RE09 | Envio por apenas um integrante no SIGAA | Responsável definido e envio conforme instrução | NÃO VERIFICÁVEL | Sem evidência de envio | Definir responsável e efetuar envio |
| RE10 | Apresentação obrigatória | Demonstrar o sistema; ausência de demonstração ou sistema que não execute implica nota zero, conforme os requisitos fornecidos | NÃO VERIFICÁVEL | Não há apresentação realizada comprovada | Validar execução antes da apresentação e conferir enunciado original |

## 6. Critérios de encerramento da atividade

- [ ] Confirmar os requisitos acima contra o enunciado original, inclusive prazo e formato de entrega.
- [ ] Demonstrar dez classes contabilizáveis, dois polimorfismos e dez regras relevantes.
- [ ] Demonstrar quinze funcionalidades, sendo sete adicionais ao CRUD, sem duplicar etapas técnicas.
- [ ] Provar autenticação, permissões diferentes e bloqueio de transição inválida.
- [ ] Salvar dados, encerrar e recuperar automaticamente na inicialização.
- [ ] Executar testes/cenários e registrar resultados sem presumir sucesso.
- [ ] Atualizar UML, status, regras pendentes e riscos conforme a implementação final.
- [ ] Preencher equipe/repositório e demonstrar participação colaborativa.
- [ ] Preparar ZIP, conferir execução e realizar apresentação/envio por um integrante.

Checklist documentado não equivale a checklist cumprido. A avaliação deve ser atualizada com evidências de código, testes, execução e entrega.
