# Sistema de vendas · Checklist da equipe

**Jefferson · Marcone · Faby**
**Atualizado em 24/11/2026** · Prazo e responsáveis a definir

Marcação `[x]` concluído, `[~]` parcial e `[ ]` pendente. Ao concluir uma tarefa, registre o commit, teste ou demonstração correspondente.

- [Regras de negócio de telecomunicações (RN01–RN11)](../regras/REGRAS_DE_NEGOCIO.md)
- [Diagramas 01–09 do código atual](../diagramas/README.md)
- [Diagramas consolidados do modelo-alvo](../diagramas/DIAGRAMAS_COMPLETOS.md)
- [Requisitos da Unidade 03](../requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md)

## P0 — Usuários, acesso e base atual

| ID | Estado | Entrega e critério de conclusão | Referência |
|---|:---:|---|---|
| P0-01 | [x] Jefferson | Perfis `Vendedor`, `Apoio` e `Administrador` derivados de `Usuario`, com capacidades polimórficas. [Evidência](P0-01_USUARIOS.md) | RN02 · 01, 09 |
| P0-02 | [x] Jefferson | marcone | Login, logout e cadastro de usuários pelo terminal com `ServicoAutenticacao`. A autenticação bloqueia contas inativas e o cadastro exige administrador ativo. | RN02 · 09 |
| P0-03 | [x] marcone | jefferson| Persistir usuários com IDs, perfis e hash de senha na aba `USUARIOS` de `BASE_GERAL.xlsx`. Carga inicial, preservação de outras abas, backup e troca atômica disponíveis. [Evidência](P0-03_PERSISTENCIA_USUARIOS.md) | 06–09 |
| P0-04 | [x] faby | Registrar clientes, produtos, contratos e vendas pelo terminal. O fluxo atual aceita contrato opcional, grava múltiplos itens, exige itens para confirmar e guarda preços e totais da venda. A consulta do vendedor retorna apenas suas vendas. | 01, 03, 06, 07 |
| P0-05 | [x] jefferson | Processar RDT/VNA em Excel/CSV, cruzar com a base Claro quando disponível e gerar analíticos e o consolidado `dados/exportacao/vendas.xlsx`. | 05, 06 |
| P0-06 | [~] | Completar a autorização nos serviços. Vendedor consulta o próprio escopo, sugere correções e solicita reativação. Apoio revisa solicitações e pode reativar. ADM tem acesso completo, gerencia usuários e gera relatórios gerais. Validar perfil, conta ativa e escopo em cada operação aplicável. | RN02 · 09 |
| P0-07 | [~] Marcone | Completar a gestão de usuários no `ServicoUsuarios` e no terminal. O cadastro está ativo. Faltam edição, ativação e desativação persistidas com autorização administrativa. | RN02 · 09 |

## P1 — Contratos, itens, estados e persistência

| ID | Estado | Entrega e critério de conclusão | Depende de |
|---|:---:|---|---|
| P1-01 | [ ] | Definir se o número identifica o contrato globalmente ou por empresa e como identificar itens repetidos no mesmo contrato. Preservar zeros à esquerda na leitura e gravação. **RN01, RN03 · 05, 07, 08** | — |
| P1-02 | [~] | Integrar `OperacaoComercial` e `VendaComercial` ao fluxo persistido, preservando os vínculos existentes de cliente, vendedor e produto. Contrato deve reunir múltiplos itens com quantidade, estado e comissão próprios. Demonstrar a contagem de um contrato com vários itens. **RN03 · 01, 07** | P0-04, P1-01 |
| P1-03 | [~] | Ligar `StatusVenda` ao ciclo de cada item: `CADASTRADA → AGENDADA → INSTALADA`, com cancelamento a partir desses três estados e reativação autorizada para `CADASTRADA`. Validar transições, salvar o estado por item e definir como o ciclo se integra à confirmação atual de `Venda` (`RASCUNHO → CONFIRMADA`). **RN04 · 03, 04** | P0-06, P1-02 |
| P1-04 | [ ] | Completar `SeletorProdutoPrincipal` com a prioridade VIRTUA > TV > CHIP > FONE e derivar o estado geral do contrato do estado do principal. Confirmar a regra para contratos só com adicionais e empates de prioridade. **RN05 · 01, 03** | P1-02, P1-03 |
| P1-05 | [~] | Integrar `ServicoContrato`, `RepositorioContrato`, `RepositorioContratoExcel` e `GerenciadorArquivoExcel` à base central. A leitura/gravação atual passa por `RepositorioVendasExcel`. Validar IDs e referências, preservar as abas existentes (`USUARIOS`, `CLIENTES`, `PRODUTOS`, `CONTRATOS`, `VENDAS`, `ITENS_VENDA`) e reaproveitar gravação temporária, backup e troca atômica. Garantir uma gravação por vez. **06–08** | P0-03, P1-01 |
| P1-06 | [ ] | Persistir `RegistroHistorico` com responsável, perfil, data/hora, campo, valores anterior e novo, motivo e autorizações aplicáveis. Salvar o evento junto à alteração de contrato/item, à decisão de revisão, ao cancelamento ou à reativação correspondente. **RN08 · 04–07** | P0-06, P1-05 |

## P2 — Revisão de importação, comissões e cancelamento

| ID | Estado | Entrega e critério de conclusão | Depende de |
|---|:---:|---|---|
| P2-01 | [ ] jefferson | Integrar `ServicoImportacao` e `LoteImportacao` à leitura Excel/CSV. Preservar arquivos de origem, normalizar números de contrato, cadastrar novos registros válidos e comparar contratos existentes por campo. Criar `SolicitacaoAlteracao` apenas para diferenças, preservando dados atuais enquanto a revisão estiver pendente. Reimportar a mesma entrada sem duplicar contratos, itens ou pendências. **RN01 · 05–08** | P0-05, P1-02, P1-05 |
| P2-02 | [ ] | Implementar sugestões do vendedor e revisão por campo com `SolicitacaoAlteracao` e `DecisaoAlteracao`. Apoio/ADM aprova ou recusa com registro no histórico. Aplicar apenas campos aprovados e válidos, preservar valores recusados e revalidar os dados antes de gravar. Conflito passível de exceção exige confirmação explícita do Apoio e permanece pendente sem confirmação. **RN01, RN02, RN07, RN08 · 05–07** | P0-06, P1-06, P2-01 |
| P2-03 | [ ] | Completar `PoliticaComissao`, `CalculadoraComissao` e `ParametrizacaoComissao` para cálculo por plano, combo, quantidade e operação. Persistir separadamente preço ao cliente, comissão informada e comissão calculada. Encaminhar divergências de comissão para revisão. Validar a tabela abaixo e preservar os valores já apurados. **RN01, RN06, RN09 · 02, 07** | P1-02, P1-05, P2-02 |
| P2-04 | [ ] | Integrar `MudancaTecnologia` como operação com comissão de R$ 5,00. Apresentar essa operação separadamente no relatório e excluí-la da contagem de vendas comerciais. **RN09 · 01, 02** | P1-05, P2-03 |
| P2-05 | [ ] | Integrar `PoliticaElegibilidade`, `ElegibilidadeVenda`, `ElegibilidadeMudanca` e `ApuradorComissoes`. Venda comercial é elegível quando instalada. Mudança de Tecnologia é elegível desde o cadastro e perde elegibilidade no cancelamento. Exibir cálculo e elegibilidade em campos distintos, preservar apurações anteriores e confirmar o tratamento de itens secundários e pagamentos já realizados. **RN10 · 02–04** | P1-03, P1-04, P2-03, P2-04 |
| P2-06 | [ ] | Implementar cancelamento no `ServicoCancelamento`. VIRTUA cancela todos os itens ativos do contrato, TV cancela os ativos exceto VIRTUA e CHIP, FONE ou outro produto cancela somente o item selecionado. Vincular a origem a um `GrupoCancelamento`, registrar apenas os itens efetivamente alterados em `ItemCancelado` e recalcular estado e elegibilidade. **RN04, RN05, RN08, RN10, RN11 · 03, 04, 07** | P1-03, P1-04, P1-06, P2-05 |
| P2-07 | [ ] | Implementar solicitação do vendedor e execução da reativação por Apoio/ADM. Somente a raiz aciona a cascata. Restaurar para `CADASTRADA` apenas itens do grupo original ainda aptos à reversão, preservando cancelamentos anteriores e mudanças posteriores. Registrar histórico e recalcular estado e elegibilidade. **RN02, RN04, RN05, RN08, RN10, RN11 · 03, 04, 07** | P0-06, P2-06 |
| P2-08 | [ ] | Integrar ao terminal revisão, histórico, transições, cancelamento, reativação e relatórios comerciais. Exibir contagens distintas de contratos, itens e mudanças de tecnologia, além de comissões informadas, calculadas e elegíveis. Aplicar o escopo de cada perfil e manter os relatórios especializados RDT/VNA disponíveis. **RN02, RN03, RN06, RN08–RN10 · 02, 05, 06, 09** | P2-02, P2-04, P2-05, P2-07 |

### Valores para validar na RN06 e na RN09

| Produto ou operação | Comissão prevista |
|---|---|
| VIRTUA 600 MB | R$ 79,90 comum ou R$ 99,90 SOLAR. Confirmar demais planos e vigência. |
| CHIP | Valor fixo por plano e modalidade. Confirmar tabela e vigência. |
| FONE | R$ 35,00. |
| TV | R$ 89,00 isolada ou com CHIP sem VIRTUA, R$ 119,00 com VIRTUA, R$ 139,00 com VIRTUA e outro produto. Confirmar TV com FONE e demais combos sem VIRTUA. |
| Wi-Fi Mesh | R$ 15,00 por unidade. |
| Mudança de Tecnologia | R$ 5,00 por operação. |

## Interface gráfica — primeira versão proposta

| ID | Estado | Entrega e critério de conclusão | Depende de |
|---|:---:|---|---|
| GUI-01 | [ ] | Criar tela de login com senha oculta usando `ServicoAutenticacao`. | P0-02 |
| GUI-02 | [ ] | Criar janela principal com nome, perfil, navegação autorizada e logout com retorno ao login. | GUI-01 |
| GUI-03 | [ ] | Integrar listagem, cadastro, edição e ativação/desativação à gestão administrativa. Exibir matrícula para vendedor e gravar por meio dos serviços. | GUI-02, P0-07 |
| GUI-04 | [ ] | Tratar campos inválidos e erros de arquivo em tela. Executar leitura/gravação fora da thread de eventos e confirmar sucesso após salvar. | GUI-03 |
| GUI-05 | [ ] | Demonstrar login dos três perfis, bloqueio de gestão para os demais perfis, cadastro e recuperação após reabertura. Swing segue como opção a confirmar. | GUI-04 |

## P3 — Validação e entrega da Unidade 03

| ID | Estado | Entrega e critério de conclusão | Depende de |
|---|:---:|---|---|
| P3-01 | [ ] | Executar `mvn test` com Java 21, registrar testes executados, falhas e ignorados. Testar o menu com dados fictícios em diretório isolado, pois o processamento limpa arquivos regulares da pasta de saída. | P0-05 |
| P3-02 | [ ] | Validar cenários das RN01–RN11, incluindo perfis, decisão por campo, reimportação, estados inválidos, produto principal, combos, elegibilidade e reativação pela raiz com mudança posterior. Demonstrar polimorfismo em execução. | P2-08 |
| P3-03 | [ ] | Encerrar e reabrir a aplicação, conferir usuários, contratos, itens, lotes, solicitações, decisões, histórico, grupos de cancelamento, comissões e vínculos. Testar arquivo bloqueado, corrompido, com cabeçalhos ausentes ou dados inválidos e verificar a integridade da base após falha. | P1-05, P2-08 |
| P3-04 | [~] | Atualizar UML, esquema das abas, README e diagnóstico com a implementação entregue, as decisões da equipe e as evidências. Os nove diagramas atuais já registram o fluxo em uso e as estruturas pendentes. | P3-02, P3-03 |
| P3-05 | [ ] | Montar matriz requisito → operação → classe/regra → evidência. Conferir 10 classes contabilizáveis, 2 casos de polimorfismo em uso, 10 regras implementadas, 15 funcionalidades e 7 além de CRUD. Aplicar os critérios de contagem da Unidade 03. | P3-04 |
| P3-06 | [ ] | Dividir falas e ensaiar na máquina da apresentação com dados fictícios. Demonstrar operações, UML, regras, dois casos de polimorfismo e cinco situações de risco. Incluir a interface gráfica se a equipe a adotar na entrega. | P3-05 |
| P3-07 | [ ] | Conferir nomes, matrículas, link do repositório e contribuições. Gerar ZIP sem dados reais ou segredos, testar a execução a partir do pacote e designar um integrante para o envio no SIGAA. Registrar envio e apresentação. | P3-06 |

## Distribuição

| Frente | Tarefas | Responsável | Revisor | Prazo |
|---|---|---|---|---|
| Acesso e gestão de usuários | P0-06 a P0-07 | A escolher | A escolher | A definir |
| Contratos, estados e persistência | P1-01 a P1-06 | A escolher | A escolher | A definir |
| Importação e revisão | P2-01 a P2-02 | A escolher | A escolher | A definir |
| Comissões, cancelamento e relatórios | P2-03 a P2-08 | A escolher | A escolher | A definir |
| Interface gráfica, se adotada | GUI-01 a GUI-05 | A escolher | A escolher | A definir |
| Validação e entrega | P3-01 a P3-07 | A escolher | A escolher | A definir |


## Pré-código

| Estado | Ação | Responsável | Prazo |
|:---:|---|---|---|
| [x] | Definir a arquitetura da aplicação e as responsabilidades do terminal, do domínio, dos serviços e dos repositórios Excel. | | |
| [x] | Revisar e aprovar com a equipe os diagramas do modelo-alvo que orientarão a implementação. | | |
| [~] | Validar as regras de telecomunicações (RN01–RN11), priorizar as da primeira entrega e registrar decisões pendentes. | | |
| [x] | Definir os fluxos prioritários e os cenários de aceitação da primeira entrega. | | |
| [x] | Criar as tabelas nas abas necessárias de `BASE_GERAL.xlsx`, com cabeçalhos, IDs e vínculos conforme os diagramas. | | |
| [x] | Preencher uma cópia da base com dados fictícios de usuários, clientes, produtos e contratos com múltiplos itens. | | |
| [x] | Preparar planilhas de importação de exemplo, com um contrato novo e outro com divergência para revisão. | | |
