# Especificação das Regras de Negócio

**Produto:** Sistema de Gestão Comercial Flexível.

**Versão documental:** 2 — visão comercial genérica, de 08/11/2026.

**Natureza:** especificação desejada; não comprova implementação.

Este é o documento oficial dos comportamentos comerciais esperados. Os [requisitos acadêmicos](../requisitos/REQUISITOS_ATIVIDADE_UNIDADE_03.md) definem os critérios do professor; o [status técnico](../STATUS_DO_PROJETO.md) registra o código existente. O [README](../../README.md) apresenta a utilização atual.

## 1. Escopo e vigência

O sistema deverá comercializar produtos e serviços de diferentes segmentos, permitir categorias, tipos de unidades e unidades configuráveis, registrar vendas com múltiplos itens e contratos opcionais, calcular comissões por políticas comerciais, controlar usuários, revisar alterações, preservar histórico e importar/reconciliar dados externos.

Exemplos de categorias: eletrônicos, alimentos, roupas, construção, serviços, assinaturas e móveis. São exemplos, não uma lista predefinida. Categorias, tipos de produto e unidades configuráveis **não serão enums**. Enums ficam reservados a conjuntos finitos controlados pela aplicação, como estados de venda, solicitação e comissão.

Ficam fora desta etapa: estoque, emissão fiscal, gateway de pagamento, API REST e microserviços. Registrar pagamento de comissão significa registrar uma decisão administrativa, sem transferir dinheiro por provedor externo. Conversão automática entre unidades também não é exigida na primeira versão.

### Relação com a documentação anterior

Esta versão substitui a especificação comercial anterior, centrada em telecomunicações. Os RN01–RN11 antigos não possuem equivalência automática com os identificadores desta versão. Os [diagramas](../diagramas/DIAGRAMAS_COMPLETOS.md) e arquivos em [planejamento](../planejamento/DECISOES_E_PENDENCIAS.md) são material legado a revisar. Em caso de conflito sobre o produto pretendido, prevalece esta especificação.

Prioridade VIRTUA > TV > CHIP > FONE, comissões fixas de telecomunicações, instalação obrigatória para toda comissão e cascatas por produto específico não são regras universais. VNA/RDT/PE, DCC e filtros por parceiro permanecem comportamentos da integração especializada. A mudança documental não altera o código.

| Conceito anterior | Tratamento na visão vigente |
|---|---|
| Contrato como centro e recipiente obrigatório dos itens | Venda passa a ser central; contrato é opcional e agrupa vendas do mesmo cliente |
| Aprovação, perfis e histórico | Generalizados em RN08, RN09 e RN18 |
| Instalação e prioridade fixa entre produtos | Substituídas por estados genéricos e critérios comerciais configurados |
| Comissão fixa por produto de telecomunicações | Substituída pela política percentual configurável da primeira versão |
| Cancelamento por VIRTUA/TV | Substituído pelo escopo de item/venda e grupo rastreável, RN14–RN15 |
| Mudança de tecnologia com valor fixo | Não é regra universal; poderá ser produto/serviço configurado sem tratamento fixo no núcleo |

## 2. Modelo conceitual pretendido

As entidades abaixo são propostas, salvo as implementações iniciais de Usuario e Vendedor. Nomes conceituais não afirmam existência de classes ou métodos.

| Entidade | Dados e responsabilidade |
|---|---|
| CategoriaProduto | Identificador, nome, descrição e situação; agrupamento dinâmico |
| TipoUnidadeMedida | Identificador, nome, descrição e situação; natureza configurável da medida |
| UnidadeMedida | Identificador, tipo, nome, símbolo, situação e permissão de quantidade fracionada |
| Produto | Identificador, nome, descrição, código, categoria, preço de venda, unidade opcional, situação e política/parâmetro de comissão opcional |
| Cliente | Pessoa ou organização compradora, identificação e situação |
| Venda | Cliente, vendedor, itens, contrato opcional, datas, totais e estado |
| ItemVenda | Referência ao produto, quantidade e fotografia das condições comerciais |
| Contrato | Vínculo de um cliente capaz de agrupar vendas desse mesmo cliente |
| Usuario | Abstração comum; especializações Vendedor, Apoio e Administrador |
| SolicitacaoAlteracao | Identificador, entidade/campo, valores anterior/proposto, justificativa, solicitante, decisor, datas da solicitação/decisão e estado |
| Comissao | Item original, beneficiário, base, percentual, valor, critério de liberação, estado e dados do pagamento |
| GrupoCancelamento | Operação de origem e itens efetivamente alterados |
| RegistroHistorico | Autor, momento, ação, entidade, valores anteriores/posteriores e justificativa |

### Relacionamentos

| Relação | Multiplicidade pretendida |
|---|---|
| Categoria → produtos | Zero ou mais produtos por categoria; uma categoria obrigatória por produto |
| Tipo de unidade → unidades | Zero ou mais unidades por tipo; um tipo obrigatório por unidade |
| Produto → unidade | Zero ou uma unidade por produto; uma unidade pode servir a vários produtos |
| Cliente → vendas | Zero ou mais vendas por cliente; um cliente obrigatório por venda |
| Cliente → contratos | Zero ou mais contratos por cliente; um cliente obrigatório por contrato |
| Venda → itens | Um ou mais itens para uma venda válida; cada item pertence a uma venda |
| Item → produto | Um produto por item; um produto pode aparecer em vários itens |
| Venda → contrato | Zero ou um contrato por venda; um contrato pode agrupar várias vendas |
| Item → comissão | Zero ou uma comissão original por item; ajustes não criam outro pagamento original |

TipoUnidadeMedida pode representar massa, comprimento, volume, tempo, área, quantidade ou natureza personalizada. UnidadeMedida pode representar kg, g, L, ml, m, cm, h, min, un e cx. Ambos permitem novos cadastros. Sem conversão automática, os valores sempre se referem à unidade do item.

### Perfis pretendidos

| Operação | Vendedor | Apoio | Administrador |
|---|---|---|---|
| Registrar e consultar vendas próprias | Permitido | Conforme atribuição comercial | Permitido |
| Solicitar correção | No próprio escopo | No próprio escopo | Permitido |
| Analisar importações e divergências | Sem aprovação | Permitido | Permitido |
| Decidir solicitação de outro usuário | Proibido | No seu escopo | Permitido |
| Cancelar/reativar com autorização | Solicita | Executa no seu escopo | Executa |
| Gerenciar usuários, produtos, categorias e unidades | Proibido | Proibido | Permitido |
| Registrar pagamento de comissão | Proibido | Proibido na proposta inicial | Permitido |

Nenhum perfil pode operar inativo, aprovar solicitação própria ou dispensar invariantes. A permissão será verificada nos serviços responsáveis, não apenas no menu.

## 3. Situação das regras e decisões documentais

- **IMPLEMENTADO:** comportamento identificável no código; comprovação de execução é informada separadamente.
- **PARCIALMENTE IMPLEMENTADO:** há parte do comportamento, com condições ou integração essenciais ausentes.
- **NÃO IMPLEMENTADO:** não há implementação identificável.
- **NÃO VERIFICÁVEL:** faltam evidências para classificar um comportamento existente.

As 18 regras seguintes estão definidas para o produto. Na inspeção de 08/11/2026: **0 implementadas, 1 parcial (RN08), 17 não implementadas e 0 classificadas como não verificáveis por inspeção**. A execução completa não foi comprovada. Os quatro comportamentos comerciais da integração legada são separados no status técnico.

Para tornar os cenários verificáveis, propõem-se unicidade por nome normalizado, precedência da política do produto, estados e arredondamento. Essas são decisões documentais a validar pela equipe, não exigências atribuídas ao professor nem funcionalidades existentes.

## 4. Regras comerciais

### RN01 — Categorias dinâmicas e preservação dos vínculos

- **Objetivo:** organizar o catálogo sem recompilar e preservar referências comerciais.
- **Condições de execução:** administrador ativo autenticado mantém categorias.
- **Comportamento esperado:** manter identificador, nome, descrição e situação; na proposta inicial, impedir nomes equivalentes após remover espaços externos e comparar sem distinguir maiúsculas/minúsculas, inclusive entre categorias ativas e inativas. Permitir desativar categoria associada, preservando referências.
- **Comportamentos proibidos:** categorias em enum; duplicidade pelo critério definido; excluir fisicamente categoria com produtos; usar categoria inativa em novo produto ou nova venda na proposta inicial.
- **Classes envolvidas:** CategoriaProduto, Produto, Administrador, Usuario e RegistroHistorico; todas conceituais, exceto Usuario.
- **Cenários de aceitação:** criar “Serviços” sem recompilar; rejeitar “ serviços ” já existente; desativar categoria e manter venda histórica consultável.
- **Status atual:** NÃO IMPLEMENTADO.

### RN02 — Tipos e unidades configuráveis com integridade de uso

- **Objetivo:** admitir novas medidas sem perder o significado das quantidades registradas.
- **Condições de execução:** administrador mantém tipos/unidades, inclusive com referências de produtos e vendas.
- **Comportamento esperado:** unidade pertence a um tipo e possui nome, símbolo, situação e permissão de fração. Desativação preserva referências. Mudança da natureza de unidade já usada exige novo cadastro, preservando o anterior.
- **Comportamentos proibidos:** excluir unidade utilizada por produto ou histórico; excluir tipo com unidades associadas; converter quantidades silenciosamente; depender apenas das unidades de exemplo.
- **Classes envolvidas:** TipoUnidadeMedida, UnidadeMedida, Produto, ItemVenda e Administrador; conceituais.
- **Cenários de aceitação:** criar tipo “Embalagem” e unidade “Pacote”; bloquear exclusão de unidade usada; manter item antigo em kg após cadastrar unidade g.
- **Status atual:** NÃO IMPLEMENTADO.

### RN03 — Disponibilidade comercial de produtos

- **Objetivo:** oferecer produtos/serviços genéricos sem novas vendas com cadastros indisponíveis.
- **Condições de execução:** manter produto ou incluí-lo em venda.
- **Comportamento esperado:** produto possui código, categoria obrigatória, preço e unidade opcional. Para nova inclusão, produto, categoria e unidade/tipo, quando existentes, devem estar ativos. Código identifica unicamente o produto no escopo definido pela equipe.
- **Comportamentos proibidos:** vender produto inativo; excluir produto com histórico; exigir unidade para todo produto; impor nomes de telecomunicações como tipos obrigatórios do domínio.
- **Classes envolvidas:** Produto, CategoriaProduto, UnidadeMedida, TipoUnidadeMedida, ItemVenda e Venda; conceituais.
- **Cenários de aceitação:** cadastrar serviço sem unidade; rejeitar produto inativo em nova venda; manter produto desativado visível em venda antiga.
- **Status atual:** NÃO IMPLEMENTADO.

### RN04 — Quantidade conforme a unidade comercial

- **Objetivo:** respeitar a forma de comercialização do produto.
- **Condições de execução:** incluir/revisar item conforme a unidade capturada para a transação.
- **Comportamento esperado:** sem unidade, quantidade exatamente 1 por item indivisível. Com unidade, quantidade positiva e fração apenas quando permitida. Várias ocorrências de produto sem unidade podem ser itens distintos, cada um com quantidade 1.
- **Comportamentos proibidos:** quantidade nula/negativa; quantidade 2 no mesmo item sem unidade; fração em unidade indivisível; alteração retroativa por reconfiguração da unidade.
- **Classes envolvidas:** Produto, UnidadeMedida, ItemVenda e Venda; conceituais.
- **Cenários de aceitação:** aceitar 1,5 kg com fração permitida; rejeitar 1,5 cx indivisível; aceitar duas linhas sem unidade, ambas com quantidade 1.
- **Status atual:** NÃO IMPLEMENTADO.

### RN05 — Cliente da venda e contrato opcional

- **Objetivo:** permitir venda avulsa e impedir vínculo contratual com outro cliente.
- **Condições de execução:** criar venda, vincular contrato ou inativar cliente.
- **Comportamento esperado:** cada venda tem cliente; contrato é opcional e, quando informado, pertence ao mesmo cliente. Cliente pode ter várias vendas/contratos. Com histórico, será inativado; na proposta inicial, cliente inativo não inicia nova venda.
- **Comportamentos proibidos:** exigir contrato para toda venda; associar contrato de outro cliente; excluir fisicamente cliente com vendas ou contratos.
- **Classes envolvidas:** Cliente, Venda, Contrato e RegistroHistorico; conceituais.
- **Cenários de aceitação:** confirmar venda avulsa; rejeitar contrato alheio; consultar venda de cliente posteriormente inativado.
- **Status atual:** NÃO IMPLEMENTADO.

### RN06 — Confirmação e composição válida da venda

- **Objetivo:** confirmar uma transação comercial consistente.
- **Condições de execução:** vendedor responsável confirma venda em rascunho.
- **Comportamento esperado:** rascunho pode estar em preparação; confirmação exige pelo menos um item comercial válido, quantidade, preço unitário e valor comercial positivos. Subtotal = preço unitário × quantidade; total = soma dos itens considerados, respeitando RN03–RN05. Cancelados/rejeitados não compõem o total ativo; valores históricos permanecem.
- **Comportamentos proibidos:** confirmar venda vazia; contar item rejeitado como vendido; misturar comissão com preço do cliente; confundir número de itens com número de vendas.
- **Classes envolvidas:** Venda, ItemVenda, Cliente, Produto e Vendedor; conceituais, exceto Vendedor.
- **Cenários de aceitação:** recusar confirmação vazia; confirmar dois produtos e um serviço; apresentar total comercial separado das comissões.
- **Status atual:** NÃO IMPLEMENTADO.

### RN07 — Fotografia histórica das condições comerciais

- **Objetivo:** impedir atualização retroativa pelo catálogo.
- **Condições de execução:** confirmar venda ou decidir correção de venda confirmada.
- **Comportamento esperado:** capturar identificação/descrição comercial do produto, categoria relevante, unidade/símbolo, permissão de fração, quantidade, preço, política/percentual e critério de comissão aplicados. Consultas e cálculos históricos usam essa fotografia. Correção autorizada produz versão/evento rastreável e respeita pagamentos realizados.
- **Comportamentos proibidos:** recalcular venda antiga pelo preço atual; substituir unidade histórica; editar condição confirmada sem revisão quando exigida.
- **Classes envolvidas:** Produto, CategoriaProduto, UnidadeMedida, ItemVenda, SolicitacaoAlteracao e RegistroHistorico; conceituais.
- **Cenários de aceitação:** mudar preço e manter venda anterior; alterar taxa e preservar comissão original; consultar antes/depois de correção aprovada.
- **Status atual:** NÃO IMPLEMENTADO.

### RN08 — Autenticação, escopo e permissões por perfil

- **Objetivo:** limitar operações ao usuário identificado, ativo e autorizado.
- **Condições de execução:** qualquer operação comercial ou administrativa.
- **Comportamento esperado:** autenticar e verificar situação, permissão e vínculo com a venda no serviço. Vendedor registra vendas próprias e solicita; Apoio revisa importações/divergências e atua no seu escopo; Administrador mantém usuários e catálogo.
- **Comportamentos proibidos:** usuário inativo operar; vendedor aprovar/gerenciar usuários; acessar venda alheia sem permissão; confiar somente no menu; ignorar invariantes por privilégio administrativo.
- **Classes envolvidas:** Usuario e Vendedor existentes; Apoio, Administrador e serviços de autorização conceituais.
- **Cenários de aceitação:** negar vendedor inativo; negar manutenção de unidades ao vendedor; permitir categoria ao administrador ativo; bloquear chamada direta não autorizada ao serviço.
- **Status atual:** PARCIALMENTE IMPLEMENTADO. Vendedor nega aprovação/gestão e consulta isAtivo() para solicitação; faltam autenticação, outros perfis e operações protegidas.

### RN09 — Solicitações, revisão e decisão independente

- **Objetivo:** revisar alterações relevantes preservando o dado confirmado.
- **Condições de execução:** propor alteração comercial de venda/item confirmado ou conciliar divergência externa.
- **Comportamento esperado:** registrar identificador, entidade/campo, valores anterior/proposto, justificativa, solicitante, decisor, datas e estado. Permitir no máximo uma pendência por entidade/campo. Apoio/Administrador diferente do solicitante decide. Aprovar verifica se o valor atual ainda corresponde ao anterior, aplica e registra histórico; rejeitar mantém o original. Dado alterado depois exige nova revisão.
- **Comportamentos proibidos:** autoaprovação inclusive administrativa; pendência duplicada por reimportação; decisão repetida; sobrescrever alteração posterior; concluir alteração sem seu histórico obrigatório.
- **Classes envolvidas:** SolicitacaoAlteracao, Usuario, Apoio, Administrador, Venda, ItemVenda e RegistroHistorico; conceituais, exceto Usuario.
- **Cenários de aceitação:** manter preço até aprovação; rejeição preserva original; outro usuário aprova e gera evento; reimportação mantém uma pendência; decisão obsoleta é recusada.
- **Status atual:** NÃO IMPLEMENTADO.

### RN10 — Transições de estado controladas

- **Objetivo:** impedir operações incompatíveis com o ciclo comercial.
- **Condições de execução:** confirmar, concluir, cancelar, reativar, decidir ou pagar.
- **Comportamento esperado:** métodos de domínio executam somente transições da seção 5, com condições e autorização verificadas previamente; falha gera exceção apropriada sem alteração parcial.
- **Comportamentos proibidos:** setter irrestrito de estado; concluir rascunho; pagar comissão não elegível; reabrir solicitação decidida; usar enum sem comportamento.
- **Classes envolvidas:** Venda, ItemVenda, SolicitacaoAlteracao, Comissao e GrupoCancelamento; conceituais.
- **Cenários de aceitação:** recusar RASCUNHO → CONCLUIDA; aceitar confirmação válida; preservar dados após tentativa inválida.
- **Status atual:** NÃO IMPLEMENTADO. Usuario.ativo não representa o ciclo comercial.

### RN11 — Comissão percentual configurável e precisão monetária

- **Objetivo:** calcular comissão independente do segmento.
- **Condições de execução:** item confirmado com política aplicável.
- **Comportamento esperado:** base = preço unitário histórico × quantidade. Proposta inicial: percentual do produto prevalece sobre categoria; sem ambos, registrar ausência de política e não gerar automaticamente. Registrar percentual/origem. Usar BigDecimal; proposta de arredondamento: duas casas monetárias, HALF_UP, ao final de cada comissão. Percentual entre 0 e 100; taxa zero pode gerar comissão zero.
- **Comportamentos proibidos:** incluir comissão no preço do cliente; usar double no cálculo monetário do domínio; fixar taxa por segmento; interpretar Valor da integração legada como preço sem mapeamento explícito.
- **Classes envolvidas:** Produto, CategoriaProduto, ItemVenda, Comissao e políticas de comissão; conceituais.
- **Cenários de aceitação:** exemplo conceitual, taxa demonstrativa: 80,00 × 2 × 5% = 8,00; política do produto prevalece; taxa nova não altera comissão histórica.
- **Status atual:** NÃO IMPLEMENTADO. Somar Valor em relatório não calcula comissão por política.

### RN12 — Unicidade da comissão e do pagamento original

- **Objetivo:** impedir duplicação por repetição de operação.
- **Condições de execução:** recalcular, importar, corrigir, liberar ou registrar pagamento.
- **Comportamento esperado:** identificar item original por chave estável; manter uma comissão original por item e uma confirmação de pagamento por comissão. Repetição retorna resultado existente ou recusa justificada. Ajuste preserva a origem, sem outra comissão original.
- **Comportamentos proibidos:** comissão duplicada por reimportação; pagar novamente comissão PAGA; apagar pagamento para permitir outro; identificar item só pela descrição.
- **Classes envolvidas:** ItemVenda, Comissao, SolicitacaoAlteracao, RegistroHistorico e integração; conceituais.
- **Cenários de aceitação:** calcular duas vezes e manter um registro; reimportar e manter comissão; repetir pagamento sem novo evento financeiro.
- **Status atual:** NÃO IMPLEMENTADO.

### RN13 — Elegibilidade e registro administrativo de pagamento

- **Objetivo:** separar comissão calculada, liberada e paga.
- **Condições de execução:** avaliar elegibilidade ou registrar pagamento por administrador ativo.
- **Comportamento esperado:** aplicar critério registrado na comissão; proposta inicial: confirmação ou conclusão do item, configurável. Sem critério, não liberar. Cancelado/rejeitado não é elegível; pendência relevante bloqueia liberação. Registrar responsável, data, valor e histórico do pagamento.
- **Comportamentos proibidos:** inferir pagamento do cálculo; usar estado de outro item; pagar sem autor; recalcular pagamento histórico. Na proposta inicial, bloquear correção/cancelamento incompatível de item com comissão paga até definição e registro do tratamento administrativo de estorno; não há estorno automático.
- **Classes envolvidas:** Comissao, ItemVenda, SolicitacaoAlteracao, Administrador e RegistroHistorico; conceituais.
- **Cenários de aceitação:** confirmação não libera critério de conclusão; conclusão torna elegível; cancelamento retira elegibilidade não paga; pagamento preserva autoria e unicidade.
- **Status atual:** NÃO IMPLEMENTADO.

### RN14 — Cancelamento rastreável de itens e vendas

- **Objetivo:** cancelar somente o escopo autorizado.
- **Condições de execução:** Apoio/Administrador autorizado informa justificativa e respeita RN13.
- **Comportamento esperado:** cancelar item afeta esse item; cancelar venda afeta itens ainda não cancelados/rejeitados. Registrar grupo com itens efetivamente alterados, estados anteriores, responsável e motivo. Reavaliar total ativo, venda e comissões não pagas sem apagar valores históricos.
- **Comportamentos proibidos:** cascata por nome fixo de produto; inserir item já cancelado/rejeitado no novo grupo; excluir histórico; alterar item pago sem tratamento permitido.
- **Classes envolvidas:** Venda, ItemVenda, GrupoCancelamento, Comissao e RegistroHistorico; conceituais.
- **Cenários de aceitação:** cancelar A sem afetar B; cancelar venda com B previamente cancelado e registrar apenas novos afetados; consultar motivo e evento.
- **Status atual:** NÃO IMPLEMENTADO.

### RN15 — Reativação seletiva e proteção de alterações posteriores

- **Objetivo:** recuperar somente itens elegíveis do cancelamento de origem.
- **Condições de execução:** Apoio/Administrador autorizado identifica grupo e motivo.
- **Comportamento esperado:** conferir origem, última alteração/versão, cliente, cadastros ativos, contrato e bloqueios financeiros. Reativar para PENDENTE apenas itens cancelados por aquele grupo e ainda elegíveis; preservar fotografia e exigir confirmação. Permitir resultado parcial, explicando cada recusa. Recalcular estado agregado conforme seção 5.
- **Comportamentos proibidos:** reativar item cancelado fora do grupo ou rejeitado; desfazer mudança posterior; restaurar diretamente concluído; apagar histórico; gerar outra comissão original.
- **Classes envolvidas:** GrupoCancelamento, Venda, ItemVenda, Cliente, Contrato, Comissao e RegistroHistorico; conceituais.
- **Cenários de aceitação:** A/B afetados e C antes cancelado: recuperar A/B elegíveis, manter C; se B mudou depois, recuperar somente A e justificar resultado parcial.
- **Status atual:** NÃO IMPLEMENTADO.

### RN16 — Adaptação e validação de dados externos

- **Objetivo:** manter o domínio independente da origem e formato.
- **Condições de execução:** processar lote externo.
- **Comportamento esperado:** camada especializada adapta campos/códigos e fornece dados ao domínio. Novos registros passam pelas mesmas validações locais. Avisos/rejeições identificam fonte, linha e motivo. VNA/RDT, DCC e similares permanecem na integração especializada.
- **Comportamentos proibidos:** presumir segmento único; criar produto/categoria desconhecido sem decisão; converter comissão em preço sem confirmação; persistir registro inválido.
- **Classes envolvidas:** integração conceitual, Produto, Venda e ItemVenda; ProcessadorPlanilhas, TratadorAnalitico e PlanilhasIO existentes são ponto de partida sem adaptação genérica implementada.
- **Cenários de aceitação:** rejeitar unidade desconhecida com origem; adaptar outra fonte sem alterar Venda; preservar relatórios legados separados.
- **Status atual:** NÃO IMPLEMENTADO como regra genérica; o processamento existente não cria entidades comerciais genéricas.

### RN17 — Conciliação idempotente e dados confirmados

- **Objetivo:** reimportar sem duplicar vendas, itens ou solicitações.
- **Condições de execução:** importar identidade de origem conhecida ou comparar dados confirmados.
- **Comportamento esperado:** chave estável identifica fonte/registro; registro idêntico não altera a base. Divergência gera solicitação RN09; manter dado confirmado até decisão. Identidade ambígua gera aviso/rejeição sem associação automática.
- **Comportamentos proibidos:** substituir preço confirmado automaticamente; usar contrato como identidade universal de venda/item; criar pendência repetida; deduzir cliente só pela descrição.
- **Classes envolvidas:** integração, Venda, ItemVenda, Contrato, SolicitacaoAlteracao e RegistroHistorico; conceituais.
- **Cenários de aceitação:** importar duas vezes sem aumentar contagens; diferença gera uma pendência; reimportar a diferença preserva uma só pendência.
- **Status atual:** NÃO IMPLEMENTADO. Cruzamento atual compara contratos, sem base de entidades ou fila de revisão.

### RN18 — Histórico obrigatório e preservado

- **Objetivo:** explicar operações comerciais e administrativas.
- **Condições de execução:** confirmação, correção, decisão, transição, cancelamento, reativação, liberação/pagamento e manutenção relevante de cadastro.
- **Comportamento esperado:** registrar usuário, data/hora, ação, entidade/identificador, valores anteriores/posteriores quando aplicáveis e justificativa exigida. Vincular lote, solicitação ou grupo correspondente. Alteração e evento devem ser gravados consistentemente e recuperados ao iniciar.
- **Comportamentos proibidos:** editar/apagar evento em operação normal; remover histórico por inativação; efetivar alteração sem evento obrigatório; tratar console como auditoria persistente.
- **Classes envolvidas:** RegistroHistorico, Usuario e entidades/serviços comerciais conceituais.
- **Cenários de aceitação:** consultar eventos distintos de cancelamento/reativação; reiniciar mantendo eventos; recusar edição normal do histórico.
- **Status atual:** NÃO IMPLEMENTADO.

## 5. Estados e transições propostos

Todos os estados abaixo são propostos, não implementados. Podem ser enums por representarem conjuntos fechados.

| Objeto | Transições permitidas e condições |
|---|---|
| Venda | RASCUNHO → CONFIRMADA após validações; CONFIRMADA → CONCLUIDA se todos os itens ativos estiverem concluídos; RASCUNHO/CONFIRMADA/CONCLUIDA → CANCELADA por RN14 quando todos os itens comerciais estiverem cancelados/rejeitados |
| ItemVenda | PENDENTE → CONFIRMADO na confirmação; CONFIRMADO → CONCLUIDO por conclusão autorizada; PENDENTE/CONFIRMADO/CONCLUIDO → CANCELADO por RN14; PENDENTE → REJEITADO por validação com decisão justificada |
| Reativação | CANCELADO → PENDENTE somente por RN15; REJEITADO não reativa nessa operação. Venda retorna a RASCUNHO enquanto houver item pendente, inclusive se antes concluída, exclusivamente por reativação autorizada |
| Solicitação | PENDENTE → APROVADA ou REJEITADA por RN09; decisão terminal; conflito impede transição |
| Comissão | CALCULADA → ELEGIVEL por RN13; ELEGIVEL → PAGA por registro autorizado; CALCULADA/ELEGIVEL → CANCELADA com cancelamento do item; ELEGIVEL → CALCULADA se perder critério antes de pagar; CANCELADA → CALCULADA somente na reativação válida do mesmo item/comissão original |

Para o estado agregado da venda: desconsiderar cancelados/rejeitados; se houver item pendente, permanecer em RASCUNHO; sem pendentes e com ativo confirmado, CONFIRMADA; todos os ativos concluídos, CONCLUIDA; sem ativos após cancelamento, CANCELADA. Rascunho vazio só é permitido na preparação. Cancelamento/reativação parcial é informação derivada dos itens/grupos, sem exigir outro estado.

Todas as demais transições diretas são proibidas: por exemplo, RASCUNHO → CONCLUIDA, PENDENTE → CONCLUIDO, CANCELADO → CONCLUIDO, PAGA → CALCULADA e solicitação decidida → PENDENTE. Comissão paga é preservada; ajustes/estornos exigem política posterior. Reativação não altera a taxa histórica nem cria outra comissão original.

## 6. Catálogo funcional planejado

Os RF orientam desenvolvimento; não representam quinze funcionalidades já concluídas. Operações CRUD precisam ser detalhadas e demonstradas segundo os critérios acadêmicos.

| ID | Capacidade planejada | Regras |
|---|---|---|
| RF01 | Manter categorias | RN01, RN08, RN18 |
| RF02 | Manter tipos de unidades | RN02, RN08 |
| RF03 | Manter unidades | RN02, RN04 |
| RF04 | Manter produtos/serviços | RN03, RN07 |
| RF05 | Manter clientes | RN05 |
| RF06 | Manter contratos opcionais | RN05 |
| RF07 | Registrar/confirmar vendas com itens | RN03–RN07, RN10 |
| RF08 | Consultar vendas conforme usuário | RN08 |
| RF09 | Solicitar correção | RN09 |
| RF10 | Decidir solicitação de outro usuário | RN08, RN09 |
| RF11 | Cancelar item/venda | RN10, RN14 |
| RF12 | Reativar itens elegíveis por grupo | RN10, RN15 |
| RF13 | Calcular comissão | RN11, RN12 |
| RF14 | Avaliar elegibilidade/registrar pagamento | RN12, RN13 |
| RF15 | Consultar histórico | RN18 |
| RF16 | Importar por adaptação especializada | RN16 |
| RF17 | Conciliar sem duplicidade e com revisão | RN09, RN17 |
| RF18 | Gerar relatórios do domínio comercial | RN06, RN11–RN13, RN18 |
| RF19 | Autenticar/autorizar | RN08 |
| RF20 | Gerenciar usuários e situações | RN08, RN18 |

## 7. Decisões propostas e confirmações da equipe

1. Confirmar unicidade normalizada de categorias e escopo do código de produto; definir unicidade de nomes/símbolos de tipos e unidades.
2. Aprovar estados, campos cuja alteração exige revisão e matriz de acesso, sobretudo atribuições do Apoio.
3. Confirmar precedência produto → categoria, intervalo percentual, critério de liberação e arredondamento. Taxas exemplificadas não são tabela oficial.
4. Definir ajuste/estorno de comissão paga; até existir política, a proposta bloqueia alterações incompatíveis e não integra pagamentos externos.
5. Definir identidade por fonte e IDs de venda/item independentes do contrato.
6. Rever futuramente UML e esquema da base Excel central, direção anterior ainda não implementada. O esquema antigo não atende ao novo domínio sem atualização.

As pendências não tornam funcionalidades disponíveis e não autorizam alterações de código nesta revisão documental.
