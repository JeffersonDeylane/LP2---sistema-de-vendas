# Requisitos Acadêmicos — Atividade Unidade 03

**Referência documental:** 08/11/2026.

**Base de código examinada:** `b0355c29726ef2a00d1bd178103813e537cc9aab`.

Este documento organiza os requisitos da atividade transcritos pela equipe nesta solicitação. O enunciado original do professor não está versionado no repositório; a equipe deve conferir a transcrição antes da entrega, sem interpretar decisões de produto como exigências acadêmicas adicionais.

As regras comerciais são definidas na [especificação](../regras/REGRAS_DE_NEGOCIO.md). O [diagnóstico técnico](../STATUS_DO_PROJETO.md) detalha as evidências. O [README](../../README.md) contém apresentação, comandos de uso e situações de risco.

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
| RA01 | Mínimo de 10 classes próprias contabilizáveis | Dez classes de domínio justificadas, excluídas as categorias da seção 3 | PARCIALMENTE IMPLEMENTADO | Usuario e Vendedor; inventário de 19 classes e 1 interface no diagnóstico | Pelo menos 8 classes de domínio adicionais; Usuario abstrata incluída conforme ausência de exclusão expressa |
| RA02 | Encapsulamento de todas as entidades | Estado protegido e alterações pelos comportamentos da entidade | IMPLEMENTADO | Campos privados de Usuario/Vendedor; validação por Validador | Preservar no novo domínio; melhorar encapsulamento técnico de TabelaDados |
| RA03 | Duas situações relevantes de polimorfismo | Tipos próprios com contrato comum, comportamentos distintos e uso efetivo em operações do domínio | PARCIALMENTE IMPLEMENTADO | RegraProduto com RegraPresencaProduto/RegraDcc é utilizada; perfis têm uma única subclasse concreta | Completar dois casos comerciais demonstráveis; aceitação do caso de integração precisa ser confirmada |
| RA04 | Pelo menos 10 regras de negócio relevantes | Regras implementadas, não apenas documentadas; sem contar validação exclusiva de formato | PARCIALMENTE IMPLEMENTADO | 4 políticas comerciais na integração; núcleo genérico com 0 completas e RN08 parcial | Pelo menos mais 6 se integração for aceita; implementar regras centrais da especificação |
| RA05 | Operações entre múltiplas classes e regras associadas | Fluxos completos demonstram colaboração de classes e condições do domínio | PARCIALMENTE IMPLEMENTADO | ProcessadorPlanilhas coordena tratamento, filtros, cruzamento e relatórios | Integrar operações comerciais ao terminal e demonstrar execução |
| RA06 | Exceções personalizadas em situações reais | Exceção própria efetivamente lançada e tratada, não só declarada | PARCIALMENTE IMPLEMENTADO | ProcessamentoException em cabeçalhos/colunas obrigatórias e falhas de I/O; captura por arquivo | Tratar falhas finais em ExecutarProcessamento e demonstrar casos reais |
| RA07 | Validação com exceções | Entrada inválida lança exceção, preserva estado e recebe tratamento adequado | IMPLEMENTADO | Validador lança IllegalArgumentException; Main captura matrícula/login vazios | Criar verificações automatizadas e cobrir futuras operações |
| RA08 | UML completo | Todas as classes, relações e multiplicidades relevantes, correspondentes ao código | PARCIALMENTE IMPLEMENTADO | Nove diagramas individuais, predominantemente de planejamento | Atualizar domínio genérico e representar módulo existente; diagramas não alterados nesta revisão |
| RA09 | Persistência em arquivos e recuperação ao iniciar, ou banco | Salvar entidades, encerrar, reiniciar e recuperar estado e relacionamentos | NÃO IMPLEMENTADO | Há exportação do processamento, mas Main recria vendedor fixo e não carrega base | Persistir/carregar entidades; provar sobrevivência ao reinício |
| RA10 | Estado dinâmico com transições controladas | Pelo menos uma classe bloqueia transições inválidas por comportamento | PARCIALMENTE IMPLEMENTADO | Usuario.ativo muda consulta de permissão; ativar/desativar não impõem regras de transição | Implementar e demonstrar ciclo comercial válido/inválido |
| RA11 | Autenticação e autorização | Verificar credenciais e permissões nas operações protegidas | PARCIALMENTE IMPLEMENTADO | Consulta de permissões de Vendedor; login explicitamente pendente | Autenticação ausente; autorização sem serviços protegidos |
| RA12 | Dois perfis com permissões diferentes | Dois tipos concretos efetivos, com acesso permitido/negado demonstrado | PARCIALMENTE IMPLEMENTADO | Apenas Vendedor concreto; Usuario abstrata | Implementar Apoio/Administrador e verificar permissões |
| RA13 | Pelo menos 15 funcionalidades | Operações reais, sem contar métodos ou etapas técnicas da mesma operação separadamente | PARCIALMENTE IMPLEMENTADO | 5 operações identificadas no processamento separado; nenhum CRUD completo no menu | Pelo menos 10 outras operações, integrar e demonstrar |
| RA14 | Pelo menos 7 funcionalidades além do CRUD | Operações adicionais com regras de negócio ou interação entre classes | PARCIALMENTE IMPLEMENTADO | 5 operações de processamento colaboram entre classes | Pelo menos 2 outras adicionais válidas, sem dispensar RA13 |

As cinco funcionalidades atuais são: processar analítico, conciliar contratos com base complementar, consolidar analíticos, gerar resumo supervisor/vendedor e gerar resumo vendedor/data. Reimplementação Python/Java não duplica a contagem. Execução completa permanece NÃO VERIFICÁVEL.

## 3. Restrições de contagem e validade

| ID | Requisito | Critério de aceitação | Status atual | Evidência | Pendência |
|---|---|---|---|---|---|
| RC01 | Enums não contam no mínimo | Não usar enum para completar RA01 | IMPLEMENTADO | Nenhum enum no código; inventário não contabiliza enums | Manter critério quando estados forem implementados |
| RC02 | Serviços, controllers e repositórios não contam | Excluir essas classes do mínimo de dez | IMPLEMENTADO | Inventário exclui serviços e infraestrutura; não há controllers/repositórios implementados | Manter classificação por responsabilidade |
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
| RD04 | README com instruções de execução | Pré-requisitos, compilação, execução principal e módulos | PARCIALMENTE IMPLEMENTADO | Comandos ajustados à estrutura real e identificados como não verificados | Validar em máquina com Java/Maven e dependências Python |
| RD05 | Identificação da equipe e repositório | Nomes, matrículas e link do repositório preenchidos | PARCIALMENTE IMPLEMENTADO | Campos reservados no README | Preenchimento e confirmação pela equipe |

## 5. Apresentação e entrega

| ID | Requisito | Critério de aceitação | Status atual | Evidência | Pendência |
|---|---|---|---|---|---|
| RE01 | Sistema funcionando | Aplicação compila e executa na apresentação | NÃO VERIFICÁVEL | Java/javac/Maven não localizados no PATH da análise | Compilar e executar em ambiente preparado |
| RE02 | Demonstração interativa por terminal ou GUI | Inserir informações e selecionar operações reais | PARCIALMENTE IMPLEMENTADO | Main recebe opção textual; sem entrada de dados comerciais | Demonstrar fluxo comercial completo |
| RE03 | Apresentar o diagrama | Explicar classes, relações e multiplicidades correspondentes | NÃO VERIFICÁVEL | UML existe; apresentação não comprovada | Atualizar e apresentar |
| RE04 | Apresentar situações de risco | Explicar os cinco cenários e mitigação | NÃO VERIFICÁVEL | Cenários documentados; apresentação não comprovada | Preparar demonstração/explicação |
| RE05 | Explicar código e decisões de modelagem | Integrantes demonstram compreensão das escolhas | NÃO VERIFICÁVEL | Documentação técnica disponível | Preparar divisão de falas e exemplos |
| RE06 | Grupo de três ou quatro componentes | Identificar integrantes e matrículas | NÃO VERIFICÁVEL | Histórico não comprova composição; campos pendentes | Confirmar 3 ou 4 integrantes |
| RE07 | Uso colaborativo de Git | Histórico de contribuições identificáveis dos integrantes | NÃO VERIFICÁVEL | 5 commits; nomes Jefferson Deylane, Jefferson Deylane Costa dos Santos e unknown | Confirmar identidades e contribuições; variantes de nome não provam pessoas diferentes |
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
