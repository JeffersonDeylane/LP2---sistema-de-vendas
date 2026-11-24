# ╔══════════════════════════════════════════════════════════════════════╗
# ║ REGRAS ANTIGAS — ESCOPO DE TELECOMUNICAÇÕES (PARA DISCUSSÃO DA EQUIPE) ║
# ╚══════════════════════════════════════════════════════════════════════╝

Estas regras foram fornecidas pela equipe como a especificação anterior. Foram mantidas aqui para comparação; a versão atual do documento começa após o divisor. A presença nesta seção não significa que estejam implementadas nem que substituam a versão atual.

## RN01 — Aprovação de alterações em contratos

Contrato existente é identificado pelo número (escopo por empresa a confirmar). Divergências importadas não atualizam os dados automaticamente: Apoio/ADM decide, campo a campo; rejeição preserva o valor atual.

**Estado no código:** NÃO IMPLEMENTADA. `CruzadorContratos.cruzar()` em [CruzadorContratos.java](../../src/main/java/lp2/processamento/CruzadorContratos.java) compara contratos entre planilhas e marca diferenças, mas não abre solicitações nem aplica aprovação humana.

## RN02 — Perfis e permissões

Vendedor consulta seu escopo, sugere correções e pede reativação, mas não aprova nem reativa. Apoio revisa solicitações e pode reativar vendas. ADM tem acesso completo, gerencia usuários e gera relatórios gerais.

**Estado no código:** PARCIAL. `ServicoAutenticacao.autenticar()` e `cadastrar()` em [ServicoAutenticacao.java](../../src/main/java/ServicoAutenticacao.java) tratam login e cadastro; `Usuario.podeSolicitarAlteracao()`, `podeAprovarAlteracoes(Usuario)` e consultas de capacidade em [Usuario.java](../../src/main/java/Usuario.java), com implementações em [Vendedor.java](../../src/main/java/Vendedor.java), [Apoio.java](../../src/main/java/Apoio.java) e [Administrador.java](../../src/main/java/Administrador.java), definem algumas capacidades. `Main.main()` em [Main.java](../../src/main/java/Main.java) restringe o cadastro a administrador. Consultas por escopo, revisão e reativação comercial ainda não existem.

## RN03 — Contratos com múltiplos produtos

Um contrato reúne vários itens (por exemplo, VIRTUA, TV, CHIP, FONE ou Wi-Fi Mesh), cada qual com quantidade, estado e comissão próprios. Contagem de contratos não se confunde com contagem de itens.

**Estado no código:** NÃO IMPLEMENTADA. `CatalogoProdutos.listarProdutos()` e `adicionarProduto()` em [CatalogoProdutos.java](../../src/main/java/CatalogoProdutos.java) apenas leem opções e as mantêm em memória; não há entidades persistentes de contrato ou item de venda.

## RN04 — Ciclo de vida de cada item

Estados: CADASTRADA, AGENDADA, INSTALADA e CANCELADA. Fluxo normal: CADASTRADA → AGENDADA → INSTALADA. Pode cancelar a partir de qualquer um dos três estados não cancelados; reativar retorna a CADASTRADA. Apoio/ADM reativa; Vendedor solicita.

**Estado no código:** NÃO IMPLEMENTADA. Não há classes ou métodos de transição para estados de vendas/itens. `Usuario.ativar()` e `desativar()` em [Usuario.java](../../src/main/java/Usuario.java) alteram apenas a situação da conta.

## RN05 — Produto principal e status geral

O principal é escolhido por prioridade VIRTUA > TV > CHIP > FONE, e seu estado define o estado geral do contrato. Faltam decisões para contratos só com adicionais e para produtos de mesma prioridade.

**Estado no código:** NÃO IMPLEMENTADA. Não foi identificada função que escolha produto principal ou derive o estado do contrato.

## RN06 — Comissões dos vendedores

Os valores importados são comissões, não preços ao cliente, e devem ficar separados do cálculo. Regras fornecidas: VIRTUA 600MB = R$ 79,90 (comum) ou R$ 99,90 (SOLAR); CHIP tem valor fixo por plano/modalidade; FONE = R$ 35,00; TV = R$ 89,00 isolada ou com CHIP sem VIRTUA, R$ 119,00 com VIRTUA e R$ 139,00 com VIRTUA e outro produto; Wi-Fi Mesh = R$ 15,00 por unidade; Mudança de Tecnologia = R$ 5,00 por operação. Divergências passam por RN01. Tabela completa de planos, vigência e combos — incluindo TV com FONE — ainda depende de confirmação.

**Estado no código:** PARCIAL NO PROCESSAMENTO LEGADO; REGRAS DE COMISSÃO NÃO IMPLEMENTADAS. `RegraPresencaProduto.detectar()` em [RegraPresencaProduto.java](../../src/main/java/lp2/processamento/RegraPresencaProduto.java) identifica produtos por texto, `RegraDcc.detectar()` em [RegraDcc.java](../../src/main/java/lp2/processamento/RegraDcc.java) identifica DCC e `GeradorRelatorios.criar()` em [GeradorRelatorios.java](../../src/main/java/lp2/processamento/GeradorRelatorios.java) soma colunas importadas, inclusive `Valor`. Esses métodos não calculam as taxas/combos especificados nem revisam divergências.

## RN07 — Confirmação humana de conflitos comerciais

Conflito que permita exceção deve ser apresentado ao Apoio para confirmação explícita, registrando a decisão. A confirmação não ignora autenticação ou invariantes.

**Estado no código:** NÃO IMPLEMENTADA. `CruzadorContratos.cruzar()` marca diferenças visuais, mas não pede confirmação nem registra decisão.

## RN08 — Histórico obrigatório

Alterações em contratos/itens, cancelamentos em cascata e reativações registram responsável, perfil, data/hora, campo, valores anterior/novo, motivo e autorizações aplicáveis. O histórico é preservado.

**Estado no código:** NÃO IMPLEMENTADA. Não há serviço ou entidade de auditoria comercial. `PlanilhasIO.salvarComBackup()` em [PlanilhasIO.java](../../src/main/java/lp2/processamento/PlanilhasIO.java) cria cópia de segurança do arquivo; isso não registra eventos de negócio.

## RN09 — Mudança de tecnologia separada de venda

Mudança de Tecnologia gera comissão de R$ 5,00 por operação, não aumenta o número de vendas comerciais e aparece separadamente nos relatórios.

**Estado no código:** NÃO IMPLEMENTADA. `TratadorAnalitico.tratar()` em [TratadorAnalitico.java](../../src/main/java/lp2/processamento/TratadorAnalitico.java) cria a coluna `PORT` para portabilidade e a define como zero para revisão; não implementa a operação de Mudança de Tecnologia nem a comissão correspondente.

## RN10 — Elegibilidade para pagamento

Comissão comercial só fica elegível quando instalada; cadastrada, agendada ou cancelada não é elegível. Mudança de Tecnologia é elegível desde CADASTRADA e deixa de ser se cancelada. Comissão calculada não significa comissão elegível; históricos apurados são preservados. Permanecem pendentes os critérios para itens secundários e pagamentos já feitos.

**Estado no código:** NÃO IMPLEMENTADA. `GeradorRelatorios` gera resumos, mas não controla estados de elegibilidade ou pagamento de comissão.

## RN11 — reativação em cascata

Reativar a origem do produto restaura somente os itens daquele grupo para CADASTRADA. Apoio/ADM executa; alterações concorrentes posteriores não devem ser desfeitas.
Somente a raiz altera os outros produtos.



