# Status atual e diagnóstico técnico

Atualização de 24/11/2026. O projeto segue as [regras de telecomunicações RN01–RN11](regras/REGRAS_DE_NEGOCIO.md). Os [diagramas 01–09](diagramas/README.md) registram o estado do código e o [modelo consolidado](diagramas/DIAGRAMAS_COMPLETOS.md) detalha os fluxos planejados.

## Fluxos ativos

| Área | Evidência | Limite atual |
|---|---|---|
| Usuários e acesso | `Usuario`, `Vendedor`, `Apoio`, `Administrador`, `ServicoAutenticacao` e `Main` | Login, logout e cadastro administrativo ativos. Gestão completa de usuários e operações comerciais de Apoio pendentes. |
| Venda | `Cliente`, `Produto`, `Contrato`, `ItemVenda`, `Venda`, `EstadoVenda` e `ServicoVendas` | Rascunho e confirmação, contrato opcional validado pelo cliente, múltiplos itens e fotografia de preço, com total em `BigDecimal`. Vendedor acessa suas vendas e ADM tem consulta geral. |
| Persistência | `RepositorioUsuariosExcel`, `RepositorioVendasExcel` e `PlanilhasIO` | Carga a partir de `BASE_GERAL.xlsx` com abas de usuários e comerciais, backup e troca atômica. A serialização na JVM ainda requer uso por um processo escritor de cada vez. |
| Planilhas externas | `ProcessadorPlanilhas`, `ComplementadorClaro`, `CruzadorContratos`, `GeradorRelatorios` e `ConsolidadorVendas` | Processamento RDT/VNA, cruzamento quando a base Claro está disponível, relatórios e consolidação. A aprovação por campo e a gravação de decisões na base central estão pendentes. |

O [esquema de dados](planejamento/ESTRUTURA_DADOS.md) detalha as abas em uso. Os serviços de importação e cancelamento, a abstração de persistência de contratos e as políticas de comissão e elegibilidade foram declarados para o modelo-alvo e ainda precisam ser ligados aos fluxos ativos.

## Regras e lacunas

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

## Orientação a objetos e critérios acadêmicos

- Usuários compartilham a abstração `Usuario` e especializam capacidades por perfil. A integração de planilhas usa `RegraProduto` com estratégias de presença de produto e detecção de DCC.
- As políticas de comissão e elegibilidade são casos de polimorfismo previstos para o núcleo comercial. A demonstração exige políticas em uso com resultados observáveis.
- `Cliente`, `Contrato`, `Produto`, `Venda`, `ItemVenda` e a hierarquia de usuários são candidatos à matriz de classes de domínio. Conferir o mínimo de dez pela [matriz da Unidade 03](requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md), considerando responsabilidade e uso efetivo.
- A integração já classifica arquivos por empresa, aplica filtros por parceiro/código, exclui status específicos em abas complementares e calcula o indicador DCC. Registrar essas políticas com sua evidência para a avaliação acadêmica, evitando duplicar os casos Java/Python.

## Pontos de validação

- Usar dados fictícios em pasta isolada ao testar a consolidação. A execução limpa arquivos regulares da pasta de saída.
- Validar cabeçalhos e formato de valores de origem. `TabelaDados.numero` pode converter um valor inválido em zero, e o cruzamento pode operar com dados complementares incompletos.
- Testar gravação e reabertura da base em caso de erro, arquivo bloqueado ou conteúdo inválido. Ao integrar o histórico, salvar o evento junto com a mudança comercial.
- Acompanhar a ordem de implementação e os cenários de aceitação no [checklist da equipe](planejamento/CHECKLIST.md).

## Cadastro inicial de vendas — 22/11/2026

Implementados `Cliente`, `Produto`, `Contrato`, `ItemVenda`, `Venda` e o fluxo
`ServicoVendas`. Vendedor autenticado/ativo cadastra clientes e vendas próprias;
administrador cadastra produtos e consulta vendas gerais. Contrato é opcional e
validado contra o cliente. A venda aceita vários itens, permanece em rascunho
até confirmação explícita, calcula total com `BigDecimal` e preserva fotografia
de produto, unidade, quantidade e preço. Os dados ficam em abas comerciais no
mesmo Excel de usuários, com backup e substituição atômica.

O menu oferece cadastro de cliente/produto/contrato, registro em rascunho,
confirmação e consulta de vendas. Foram executados **92 testes, sem falhas ou
erros; 2 ignorados por plataforma**, em cópia isolada do projeto para não
sobrescrever artefatos `target/` já modificados no worktree.

A entrega documenta o cadastro inicial. A seção de regras acima acompanha as lacunas do modelo de telecomunicações.

## Atualização de usuários — 14/11/2026

A P0-01 implementou Vendedor, Apoio e Administrador e suas capacidades
polimórficas. A P0-03 acrescentou `RepositorioUsuariosExcel`, carga inicial pelo
`Main`, atualização por ID, validação estrita da aba `USUARIOS`, backup da versão
anterior e substituição exclusivamente atômica, reutilizando Apache POI.

A suíte da raiz executou **54 testes sem falhas, erros ou ignorados**: 14 de
usuários, 35 de persistência/inicialização e 5 de processamento. Os testes de
falha preservaram os bytes da base anterior, incluindo bloqueio e somente leitura
no Windows. Não houve uso de planilhas reais. Veja o
[relatório da P0-03](planejamento/P0-03_PERSISTENCIA_USUARIOS.md).
