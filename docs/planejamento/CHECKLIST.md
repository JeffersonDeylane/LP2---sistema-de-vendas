# Sistema de vendas · Plano da equipe

**Jefferson · Marcone · Faby**

Atualização: **22/11/2026** · Prazo: **a definir** · Responsáveis: **a escolher**

Marcação: `[ ]` pendente, `[~]` parcialmente feito, `[x]` concluído com evidência.

Referência: [Requisitos da atividade — Unidade 03](../requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md).

## 1. O que já existe

| Estado | Entrega | Evidência e limite atual |
|---|---|---|
| [x] | Estrutura do repositório e documentação versionada | Código em `src/`, documentos em `docs/` e histórico Git local; equipe definida; envio ao GitHub ainda precisa ser confirmado. |
| [x] | Configuração Java 21/Maven na raiz | `pom.xml` com Apache POI, Commons CSV e JUnit 4. |
| [x] | Menu de terminal com processamento de planilhas integrado | `Main.java`, opção 3 chama `ProcessadorPlanilhas`; login/logout estão implementados. |
| [x] | Base de usuários e validações | `Usuario` abstrata, `Vendedor`, `Apoio`, `Administrador` e `Validador`; matrícula, ativação/desativação e capacidades polimórficas. [Entrega P0-01](P0-01_USUARIOS.md), com 14 testes de usuários aprovados. |
| [x] | Leitura e tratamento especializado de XLS/XLSX/CSV | `PlanilhasIO`, `TratadorAnalitico`, `ComplementadorClaro` e `NormalizadorContrato`. Não cadastra vendas do domínio genérico. |
| [x] | Cruzamentos, consolidação e relatórios especializados | `CruzadorContratos`, `ConsolidadorVendas` e `GeradorRelatorios`; resumos estáticos, não pivôs nativos do Excel. |
| [x] | Estratégias polimórficas na integração | `RegraProduto`, `RegraPresencaProduto` e `RegraDcc`; não comprovam sozinhas os dois casos comerciais exigidos. |
| [x] | Testes automatizados de usuários, persistência e processamento | Maven executado em cópia isolada em 22/11/2026: 92 testes, sem falhas/erros; 2 ignorados por plataforma. Inclui 8 testes de vendas. |
| [x] | Especificação comercial e requisitos acadêmicos documentados | RN01–RN18, RF01–RF20 e critérios acadêmicos nos documentos vinculados acima; propostas ainda exigem decisões da equipe. |
| [~] | Catálogo de produtos | `Produto` e aba `PRODUTOS` com código, preço, categoria/unidade em texto e restrição de cadastro a administrador. Categorias/unidades configuráveis e edição/inativação pela interface continuam pendentes. |
| [~] | Autenticação e autorização | Login/logout e cadastro protegido de usuários existentes. `ServicoVendas` valida usuário persistido ativo, restringe produtos a administrador e vendas a vendedor/administrador, limitando consulta do vendedor às próprias vendas. Escopo de Apoio e outras operações ainda pendentes. |
| [x] | Persistência dos usuários | `RepositorioUsuariosExcel`, aba `USUARIOS`, carga inicial, atualização por ID, backup e troca atômica. [Entrega P0-03](P0-03_PERSISTENCIA_USUARIOS.md). |
| [~] | Persistência comercial e cadastro de vendas | Abas `CLIENTES`, `PRODUTOS`, `CONTRATOS`, `VENDAS` e `ITENS_VENDA` na mesma base, com backup atômico, rascunho, múltiplos itens, contrato opcional e fotografia de preço. Histórico completo, cancelamento e comissão pendentes. |
| [~] | UML e documentação de execução | Esquema de usuários e vendas atualizado; UML e partes históricas do README/diagnóstico ainda precisam de sincronização. |
| [~] | Núcleo comercial completo | Cadastro inicial de clientes, produtos, contratos e vendas implementado. Categorias/unidades dinâmicas, auditoria, comissões, solicitações, cancelamento e outras transições ainda pendentes. |

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
| `RepositorioUsuariosExcel` | Ler/gravar usuários e reconstruir a subclasse correta a partir do perfil salvo. | P0-03 concluída: IDs preservados, atualização sem perda dos demais registros, backup e falhas cobertos por testes. |
| `ServicoUsuarios` | Cadastro, login único, alterações e ativação/desativação com autorização. | Criar. |
| `ServicoAutenticacao` | Conferir senha, bloquear usuário inativo e manter a identidade da sessão. | Criar. |

Nomes novos são sugestões de implementação. Serviços e repositórios não entram na contagem de dez classes de domínio.

### Níveis de acesso

Os perfis representam permissões por operação; não basta comparar um número de nível ou esconder botões.

| Operação | Vendedor | Apoio | Administrador |
|---|---|---|---|
| Entrar no sistema | Se ativo | Se ativo | Se ativo |
| Consultar/registrar vendas | Próprias | Consultar no escopo autorizado (atualmente, toda a equipe) | Acesso administrativo |
| Solicitar alteração | No próprio escopo | No escopo autorizado | No escopo autorizado |
| Aprovar/recusar solicitação | Não | Sim, solcitações de terceiros de toda a equipe | Sim, de outro usuário |
| Cancelar/reativar | Não | Sim, no escopo autorizado (atualmente, toda a equipe) | Sim |
| Gerenciar usuários e catálogo | Não | Não | Sim |

Aprovação, cancelamento e demais ações comerciais só serão habilitados quando implementados. Nenhum perfil pode autoaprovar ou ignorar regras comerciais. Validar permissões nos serviços, inclusive em chamadas fora da interface.

### Planilha de usuários

A aba **`USUARIOS` em `dados/BASE_GERAL.xlsx`** foi implementada na P0-03. A base só é criada no primeiro salvamento e preserva outras abas existentes. Tipos, validações e configuração estão em [ESTRUTURA_DADOS.md](ESTRUTURA_DADOS.md).

| Coluna implementada | Conteúdo |
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
| P0-02 | [~] | Autorização por conta ativa, perfil e escopo implementada e testada. Vendedor consulta e confirma apenas suas próprias vendas. Apoio e Administrador consultam, cancelam e reativam vendas de toda a equipe e podem aprovar ou recusar solicitações de terceiros. A autorização para solicitar alteração foi testada e aguarda integração ao fluxo de criação. A execução da aprovação, recusa, cancelamento e reativação segue nas tarefas P2-02 e P2-03. | P0-01 |
| P0-03 | [x] | `RepositorioUsuariosExcel` implementado e integrado à carga inicial. IDs e perfis preservados, atualização por ID, validação estrita, backup e troca atômica. [Evidência e limites](P0-03_PERSISTENCIA_USUARIOS.md): 35 testes novos e suíte completa com 54 aprovados. | P0-01 |
| P0-04 | [x] |Administrador ativo pode cadastrar os três perfis, listar usuários, editar nome e login e ativar ou desativar contas pelo menu. O sistema valida login único, salva as alterações no Excel pelo ID do usuário e impede a desativação do último administrador ativo. Se um administrador desativar a própria conta, a sessão é encerrada. | P0-02, P0-03 |
| P0-05 | [x] | Login/logout implementados: verificar senha, bloquear conta inativa, manter usuário autenticado e limpar sessão ao sair. A opção de cadastro só aparece para administrador; o serviço também valida a permissão. | P0-04 |
| P0-06 | [~] | Suíte completa: 84 testes aprovados. Testes cobrem os três perfis, persistência/reabertura, login, senha incorreta, conta inativa, login duplicado e bloqueio de cadastro sem administrador. Conferir dados/credenciais versionados na revisão final. | P0-05 |
| P0-07 | [x] | Esquema da aba `USUARIOS` conferido, decisões de acesso documentadas e UML de usuários atualizado. [Demonstração em base temporária](P0-07_DEMONSTRACAO.md) confirmou o login dos três perfis, o bloqueio de edição para Vendedor e Apoio e a persistência da edição feita pelo Administrador após reabrir o sistema. | P0-06 |
## 4. P1 — Base funcional: fazer em seguida

O primeiro fluxo de cadastro de vendas foi integrado. A próxima etapa é completar
o domínio: categorias/unidades configuráveis, histórico e estados comerciais,
mantendo as decisões de persistência e autorização já aplicadas.

| ID | Estado | Entrega e critério de conclusão | Depende de |
|---|:---:|---|---|
| P1-01 | [~] | Serviço de vendas exige usuário ativo persistido e perfil; vendedores só confirmam/consultam vendas próprias, administradores têm acesso geral. Escopo de Apoio continua pendente. | P0-06 |
| P1-02 | [~] | `Produto` com preço, categoria/unidade textual, situação e regra de quantidade; administrador cadastra e venda seleciona produto ativo. Faltam entidades e CRUD dinâmico de categorias/unidades e manutenção de produtos. | P0-02, P0-03 |
| P1-03 | [~] | Cliente obrigatório, contrato opcional e validação de contrato do mesmo cliente implementados. Inativação/manutenção de cadastro e preservação por histórico completo pendentes. | P0-02, P0-03 |
| P1-04 | [~] | Novas abas comerciais usam `BASE_GERAL.xlsx`, preservam `USUARIOS` e demais abas e gravam com backup atômico; sem coordenação entre processos ou migração de esquema. | P0-03 |
| P1-05 | [ ] | Implementar histórico persistente e sua integração às operações (RN18). Registrar autor, data, entidade, antes/depois e motivo; mudança e evento devem ser consistentes. | P1-01, P1-04 |
| P1-06 | [~] | Cadastro em rascunho, venda avulsa/contrato opcional, múltiplos itens, rejeição de venda vazia, confirmação, total e fotografia de preço/unidade implementados e testados. Histórico de eventos, transições de item, cancelamento e conclusão continuam pendentes. | P1-01, P1-02, P1-03, P1-05 |
| P1-07 | [~] | Cadastro de clientes/produtos/contratos, registro, confirmação e consulta integrados ao terminal com tratamento de erros. Edição dos cadastros e fluxos comerciais adicionais pendentes. | P1-01, P1-06 |

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
