# Sistema de vendas · Plano da equipe

**Jefferson · Marcone · Faby**

Atualização: **14/11/2026** · Prazo: **a definir** · Responsáveis: **a escolher**

Marcação: `[ ]` pendente, `[~]` parcialmente feito, `[x]` concluído com evidência.

Referência: [Requisitos da atividade — Unidade 03](../requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md).

## 1. O que já existe

| Estado | Entrega | Evidência e limite atual |
|---|---|---|
| [x] | Estrutura do repositório e documentação versionada | Código em `src/`, documentos em `docs/` e histórico Git local; equipe definida; envio ao GitHub ainda precisa ser confirmado. |
| [x] | Configuração Java 17/Maven na raiz | `pom.xml` com Apache POI, Commons CSV e JUnit 4. |
| [x] | Menu de terminal com processamento de planilhas integrado | `Main.java`, opção 3 chama `ProcessadorPlanilhas` e mostra arquivos/avisos. Login continua pendente. |
| [x] | Base de usuários e validações | `Usuario` abstrata, `Vendedor`, `Apoio`, `Administrador` e `Validador`; matrícula, ativação/desativação e capacidades polimórficas. [Entrega P0-01](P0-01_USUARIOS.md), com 14 testes de usuários aprovados. |
| [x] | Leitura e tratamento especializado de XLS/XLSX/CSV | `PlanilhasIO`, `TratadorAnalitico`, `ComplementadorClaro` e `NormalizadorContrato`. Não cadastra vendas do domínio genérico. |
| [x] | Cruzamentos, consolidação e relatórios especializados | `CruzadorContratos`, `ConsolidadorVendas` e `GeradorRelatorios`; resumos estáticos, não pivôs nativos do Excel. |
| [x] | Estratégias polimórficas na integração | `RegraProduto`, `RegraPresencaProduto` e `RegraDcc`; não comprovam sozinhas os dois casos comerciais exigidos. |
| [x] | Testes automatizados de usuários e processamento | `UsuarioTest`: 14 testes; `ProcessamentoPlanilhasTest`: 5 testes. Maven executado em 14/11/2026: 19 testes, sem falhas, erros ou testes ignorados. |
| [x] | Especificação comercial e requisitos acadêmicos documentados | RN01–RN18, RF01–RF20 e critérios acadêmicos nos documentos vinculados acima; propostas ainda exigem decisões da equipe. |
| [~] | Catálogo de produtos | `CatalogoProdutos.java` lê `dados/catalogo_produtos.xlsx`, aba `CATALOGO`, e permite seleção em memória. Falta CRUD, entidades, persistência e integração ao `Main`. |
| [~] | Autenticação e autorização | Três perfis concretos e consultas de capacidade, com inatividade e impedimento de autoaprovação por ID. Autenticação e autorização por escopo nos serviços continuam pendentes. |
| [~] | UML e planejamento da persistência | Diagramas e `ESTRUTURA_DADOS.md` existem, mas seguem principalmente o modelo antigo. Não há carga/gravação de entidades em `BASE_GERAL.xlsx`. |
| [~] | Documentação de execução e status | README e diagnóstico ainda citam caminhos antigos, ausência de testes/Maven na raiz e processamento separado; precisam ser sincronizados. |
| [ ] | Núcleo comercial completo | Categorias/unidades dinâmicas, clientes, vendas/itens, contrato opcional, comissões, solicitações, histórico e recuperação ao iniciar ainda pendentes. |

## 2. Equipe e combinados

| Integrante | Matrícula | Parte assumida |
|---|---|---|
| **Jefferson** |  | A escolher |
| **Marcone** | | A escolher |
| **Faby** |  | A escolher |

- Cada tarefa tem um responsável principal e um revisor entre os outros integrantes.

- Consulte a coluna **Depende de** antes de começar.
- Ao concluir, marque `[x]` e registre commit/PR, teste ou demonstração no quadro.
- Cada integrante testa e documenta as próprias entregas.

---
## 3. P0 — Primeiro: usuários, acesso e planilha

**Primeira entrega:** cadastrar usuários, salvar na planilha, fechar/reabrir o sistema e fazer login com as permissões do perfil. Esta etapa pode ser concluída antes do catálogo e das vendas.

### Classes e responsabilidades

| Classe planejada | Responsabilidade | Situação atual |
|---|---|---|
| `Usuario` | Dados comuns: ID, nome, login, hash da senha e situação; contrato das capacidades e impedimento de autoaprovação. | P0-01 concluída; serviços de autorização ainda pendentes. |
| `Vendedor` | Matrícula; pode solicitar quando ativo e não pode aprovar ou gerenciar. | P0-01 concluída; autorização por escopo pendente. |
| `Apoio` | Capacidade de revisar alterações de outro usuário quando ativo, sem gestão administrativa. | Implementada e testada na P0-01; execução de operações e escopo pendentes. |
| `Administrador` | Capacidade de revisar alterações de outro usuário e gerenciar usuários/catálogo quando ativo. | Implementada e testada na P0-01; serviços administrativos pendentes. |
| `RepositorioUsuariosExcel` | Ler/gravar usuários e reconstruir a subclasse correta a partir do perfil salvo. | Criar. |
| `ServicoUsuarios` | Cadastro, login único, alterações e ativação/desativação com autorização. | Criar. |
| `ServicoAutenticacao` | Conferir senha, bloquear usuário inativo e manter a identidade da sessão. | Criar. |

Nomes novos são sugestões de implementação. Serviços e repositórios não entram na contagem de dez classes de domínio.

### Níveis de acesso

Os perfis representam permissões por operação; não basta comparar um número de nível ou esconder botões.

| Operação | Vendedor | Apoio | Administrador |
|---|---|---|---|
| Entrar no sistema | Se ativo | Se ativo | Se ativo |
| Consultar/registrar vendas | Próprias | Consultar no escopo autorizado | Acesso administrativo |
| Solicitar alteração | No próprio escopo | No escopo autorizado | No escopo autorizado |
| Aprovar/recusar solicitação | Não | Sim, de outro usuário e no seu escopo | Sim, de outro usuário |
| Cancelar/reativar | Não | Sim, no escopo autorizado | Sim |
| Gerenciar usuários e catálogo | Não | Não | Sim |

Aprovação, cancelamento e demais ações comerciais só serão habilitados quando implementados. Nenhum perfil pode autoaprovar ou ignorar regras comerciais. Validar permissões nos serviços, inclusive em chamadas fora da interface.

### Planilha de usuários

Planejar a aba **`USUARIOS` em `dados/BASE_GERAL.xlsx`**, reaproveitando o arquivo central previsto. A implementação inicial pode conter somente essa aba e deve preservar as demais quando forem acrescentadas.

| Coluna proposta | Conteúdo |
|---|---|
| `ID` | Identificador único e estável. |
| `NOME` | Nome do usuário. |
| `LOGIN` | Login único, normalizado para comparação. |
| `SENHA_HASH` | Hash de senha com salt e parâmetros do algoritmo; nunca senha em texto puro. |
| `PERFIL` | `VENDEDOR`, `APOIO` ou `ADMINISTRADOR`; usado para reconstruir o objeto. |
| `ATIVO` | Situação da conta. |
| `MATRICULA` | Matrícula para vendedor; vazia quando não aplicável. |

### Tarefas da primeira entrega

| ID | Estado | Entrega e critério de conclusão | Depende de |
|---|:---:|---|---|
| P0-01 | [x] | `Usuario`/`Vendedor` revisados; `Apoio`/`Administrador` implementados; despacho polimórfico demonstrado no `Main` e em testes. [Evidência e limites](P0-01_USUARIOS.md): 19 testes aprovados e menu verificado. | — |
| P0-02 | [ ] | Implementar autorização por perfil e escopo nos serviços, seguindo a matriz acima. Bloquear usuário inativo e operações não permitidas. | P0-01 |
| P0-03 | [ ] | Implementar `RepositorioUsuariosExcel` e a aba `USUARIOS`: carregar ao iniciar, cadastrar/alterar sem perder registros, manter IDs e reconstruir perfis. Tratar planilha inválida, backup e falha de escrita sem corromper o arquivo. | P0-01 |
| P0-04 | [ ] | Implementar cadastro e manutenção autorizados: validar campos/login único, gerar hash de senha e salvar ativação/desativação. Definir criação inicial do administrador apenas na primeira execução, sem senha fixa no código. | P0-02, P0-03 |
| P0-05 | [ ] | Implementar login/logout: verificar senha, bloquear conta inativa, manter usuário autenticado e limpar sessão ao sair. Remover o vendedor fixo como identidade de uso. | P0-04 |
| P0-06 | [ ] | Validar com usuários fictícios dos três perfis: cadastrar, fechar/reabrir, autenticar e confirmar bloqueios; testar login duplicado, senha incorreta e falha ao salvar. Conferir exclusão de dados reais/credenciais do versionamento. | P0-05 |
| P0-07 | [ ] | Registrar esquema da aba em `ESTRUTURA_DADOS.md`, decisões de acesso em `DECISOES_E_PENDENCIAS.md` e classes no UML. Evidenciar a primeira entrega funcionando. | P0-06 |
## 4. P1 — Base funcional: fazer em seguida

Depois da primeira entrega de usuários, avançar para o núcleo comercial. Definir IDs de venda/item, contrato opcional e esquema das novas abas antes de implementar a persistência dessas entidades. Confirmar as decisões comerciais pendentes e os critérios acadêmicos com a equipe.

| ID | Estado | Entrega e critério de conclusão | Depende de |
|---|:---:|---|---|
| P1-01 | [ ] | Integrar a sessão e as permissões da primeira entrega às operações comerciais. Exigir usuário ativo, perfil e vínculo com a venda em cada serviço. | P0-06 |
| P1-02 | [~] | Evoluir o leitor de catálogo para categorias, tipos/unidades e produtos configuráveis (RN01–RN04). CRUD, inativação, quantidade/fração e validações; integrar seleção ao fluxo comercial. | P0-02, P0-03 |
| P1-03 | [ ] | Implementar clientes e contratos opcionais (RN05). Rejeitar contrato de outro cliente e preservar vínculos históricos após inativação. | P0-02, P0-03 |
| P1-04 | [ ] | Ampliar a base Excel e seus repositórios para as entidades comerciais, preservando a aba de usuários, com leitura inicial, gravação segura, backup e bloqueio de escrita concorrente. Usar IDs acordados; cada bloco acrescenta seus mapeamentos. Validar salvamento e recuperação com amostra fictícia. | P0-03 |
| P1-05 | [ ] | Implementar histórico persistente e sua integração às operações (RN18). Registrar autor, data, entidade, antes/depois e motivo; mudança e evento devem ser consistentes. | P1-01, P1-04 |
| P1-06 | [ ] | Implementar vendas/itens, totais, fotografia histórica e transições controladas (RN06, RN07, RN10). Demonstrar venda avulsa, múltiplos itens, rejeição de venda vazia e transição inválida sem alteração parcial. | P1-01, P1-02, P1-03, P1-05 |
| P1-07 | [~] | Integrar os fluxos concluídos ao terminal. Tratar entradas e exceções reais sem encerrar indevidamente a aplicação; remover a dependência dos exemplos fixos como fluxo de uso. | P1-01, P1-06 |

## 5. P2 — Regras comerciais e integração

Essas entregas fazem parte do objetivo do projeto; a prioridade indica dependências, não dispensa sua implementação.

| ID | Estado | Entrega e critério de conclusão | Depende de |
|---|:---:|---|---|
| P2-01 | [ ] | Implementar comissão configurável com BigDecimal, fotografia da política, unicidade, elegibilidade e registro de pagamento (RN11–RN13). Provar repetição sem duplicar comissão/pagamento e preservar valores históricos. | P1-06, P0-02 |
| P2-02 | [ ] | Implementar solicitações e aprovação/recusa por campo (RN09). Bloquear autoaprovação, pendência duplicada, decisão repetida e aplicação sobre dado alterado posteriormente. | P1-06 |
| P2-03 | [ ] | Implementar cancelamento e reativação seletiva por grupo (RN14–RN15). Preservar itens antes cancelados, bloquear reversão de mudanças posteriores e respeitar comissão paga. | P2-01, P2-02 |
| P2-04 | [~] | Adaptar a entrada especializada ao domínio e conciliar sem duplicar (RN16–RN17). Reimportar dois arquivos fictícios, manter dados confirmados e gerar uma única pendência por divergência. Cruzamento atual não equivale à aprovação manual. | P1-06, P2-02 |
| P2-05 | [~] | Integrar consultas e relatórios comerciais ao terminal: vendas por escopo, totais separados de comissão, elegibilidade/pagamento e histórico. Preservar relatórios especializados existentes. | P1-07, P2-01, P2-02, P2-03, P2-04 |

## 6. Interface gráfica pequena — primeira versão

Criar uma interface desktop simples depois da primeira entrega de usuários, sem esperar todas as vendas ficarem prontas. **Sugestão: Java Swing**, disponível no JDK. A escolha é proposta; manter regras e leitura/gravação fora das telas.

| ID | Estado | Entrega e critério de conclusão | Depende de |
|---|:---:|---|---|
| GUI-01 | [ ] | Criar tela de login com usuário, senha oculta, botão Entrar e mensagem de erro. Usar o serviço de autenticação e abrir a tela principal apenas após sucesso. | P0-05 |
| GUI-02 | [ ] | Criar janela principal com nome/perfil do usuário, navegação permitida e botão Sair da conta. Logout retorna ao login e elimina a sessão anterior. | GUI-01 |
| GUI-03 | [ ] | Criar tela de usuários para administrador: listar, cadastrar, editar e ativar/desativar. Selecionar perfil, mostrar matrícula quando aplicável e salvar pela camada de serviços na planilha. | GUI-02, P0-04 |
| GUI-04 | [ ] | Tratar campos inválidos e falhas de leitura/gravação com mensagens claras; manter operações de arquivo fora da thread de eventos para não travar a janela. Confirmar sucesso somente após salvar. | GUI-03 |
| GUI-05 | [ ] | Demonstrar login dos três perfis, acesso administrativo bloqueado aos demais, cadastro pela tela e recuperação após fechar/reabrir. Acrescentar telas comerciais aos poucos, conforme P1/P2 forem concluídos. | GUI-04, P0-06 |

**Limite inicial:** login, janela principal e gestão de usuários. Não é necessário criar todas as telas comerciais nesta primeira versão.
## 7. P3 — Validação e entrega

Executar testes de cada bloco durante o desenvolvimento; esta etapa reúne a validação integrada e a preparação final.

| ID | Estado | Entrega e critério de conclusão | Depende de |
|---|:---:|---|---|
| P3-01 | [ ] | Validar ambiente e executar `mvn test` na raiz; registrar resultado dos cinco testes existentes. Testar o menu com amostras fictícias em diretório isolado: o processamento atual limpa arquivos regulares da pasta de saída. | P0-05 |
| P3-02 | [~] | Acrescentar testes de negócio e demonstrar os cenários das RN implementadas: permissões, fotografia, transições, revisão, comissões, cancelamento e reativação. Registrar falhas e resultados, além dos testes da integração já existentes. | P2-01, P2-02, P2-03, P2-04 |
| P3-03 | [ ] | Salvar, encerrar e reabrir a aplicação; recuperar automaticamente usuários, cadastros, vendas, comissões, solicitações e histórico, com vínculos preservados. Testar falha de gravação/arquivo bloqueado sem corromper a base. | P2-05 |
| P3-04 | [~] | Atualizar UML com classes/relações/multiplicidades reais; alinhar README, status, planejamento e requisitos ao código atual, caminhos Maven na raiz e resultados dos testes. | P2-05, P3-02, P3-03 |
| P3-05 | [ ] | Montar matriz requisito → operação → classe/regra → evidência. Conferir 10 classes contabilizáveis, 2 polimorfismos usados, 10 regras, 15 funcionalidades e 7 além de CRUD, sem duplicar Java/Python ou contar utilitários como entidades. | P3-04 |
| P3-06 | [ ] | Preparar roteiro e dividir falas: demonstração interativa, UML, decisões, dois polimorfismos, regras e cinco situações de risco. Ensaiar na máquina da apresentação com dados fictícios. | P3-05, GUI-05 |
| P3-07 | [ ] | Conferir nomes/matrículas, link do repositório e contribuições; gerar ZIP sem dados reais/segredos, testar execução a partir do pacote e definir um único integrante para envio no SIGAA. Registrar envio e apresentação. | P3-06 |

## 8. Quadro de distribuição

 **Jefferson**, **Marcone** ou **Faby** 

| Tarefa | Responsável | Revisor | Prazo | Evidência / observações |
|---|---|---|---|---|
| P0-01 | — | — | A definir | — |
| P0-02 | — | — | A definir | — |
| P0-03 | — | — | A definir | — |
| P0-04 | — | — | A definir | — |
| P0-05 | — | — | A definir | — |
| P0-06 | — | — | A definir | — |
| P0-07 | — | — | A definir | — |
| P1-01 | [ ] | Integrar a sessão e as permissões da primeira entrega às operações comerciais. Exigir usuário ativo, perfil e vínculo com a venda em cada serviço. | P0-06 |
| P1-02 | — | — | A definir | — |
| P1-03 | — | — | A definir | — |
| P1-04 | — | — | A definir | — |
| P1-05 | — | — | A definir | — |
| P1-06 | — | — | A definir | — |
| P1-07 | — | — | A definir | — |
| P2-01 | — | — | A definir | — |
| P2-02 | — | — | A definir | — |
| P2-03 | — | — | A definir | — |
| P2-04 | — | — | A definir | — |
| P2-05 | — | — | A definir | — |
| P3-01 | — | — | A definir | — |
| P3-02 | — | — | A definir | — |
| P3-03 | — | — | A definir | — |
| P3-04 | — | — | A definir | — |
| P3-05 | — | — | A definir | — |
| P3-06 | — | — | A definir | — |
| P3-07 | — | — | A definir | — |
| GUI-01 | — | — | A definir | — |
| GUI-02 | — | — | A definir | — |
| GUI-03 | — | — | A definir | — |
| GUI-04 | — | — | A definir | — |
| GUI-05 | — | — | A definir | — |

### Frentes para orientar a escolha

Agrupamento sugerido, sem nomes atribuídos. Redistribua tarefas individuais para equilibrar o volume.

| Frente | Nome escolhido | Tarefas principais |
|---|---|---|
| **Usuários e níveis de acesso** | A escolher | P0-01, P0-02, P0-04, P0-05 e P1-01 |
| **Catálogo e operação comercial** | A escolher | P1-02, P1-03, P1-06, P2-01, P2-03 |
| **Persistência e integração** | A escolher | P0-03, P0-06, P0-07, P1-04, P1-05, P2-04 |
| **Interface gráfica e revisão** | A escolher | GUI-01 a GUI-05, P2-02 e integração com P1-07/P2-05 |

> A frente comercial tem maior volume e pode ser repartida. As tarefas da primeira entrega (P0) e de validação/entrega (P3) também precisam de responsáveis no quadro acima.
