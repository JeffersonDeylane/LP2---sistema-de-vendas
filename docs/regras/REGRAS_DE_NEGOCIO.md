# Regras de negócio — RN01 a RN11

**Status:** regras definidas em conversa; pendências estão assinaladas.  
**Primeira versão:** persistência somente em planilhas Excel (sem SQLite).

As marcações `[ ]` indicam **ainda não implementada/testada**, e não que a regra esteja rejeitada.

[ ] RN01 — APROVAÇÃO DE ALTERAÇÕES EM CONTRATOS
Contratos existentes são identificados pelo número de contrato (verificar escopo por empresa). Quando a importação ou sugestão apresenta valor divergente, a atualização não é aplicada automaticamente: depende da decisão de Apoio ou ADM. A revisão pode ocorrer campo a campo. Rejeição preserva o valor atual.
Teste: importar contrato existente com valor diferente; confirmar que nada mudou antes de aprovar.

[ ] RN02 — PERFIS E PERMISSÕES
Vendedor: consulta dados a que tem acesso, sugere correções e solicita reativação; não aprova nem reativa diretamente.
Apoio: analisa, aprova ou recusa solicitações e pode reativar vendas.
ADM: tem acesso completo, gerencia usuários e gera relatórios gerais.
Teste: tentar realizar a mesma reativação com cada perfil; Vendedor deve ser bloqueado.

[ ] RN03 — CONTRATOS COM MÚLTIPLOS PRODUTOS
Um contrato de venda pode conter vários produtos/itens (VIRTUA, TV, CHIP, FONE, Wi-Fi Mesh etc.), mantendo quantidade, status e comissão de cada item, sem confundir a contagem de contratos com a de produtos.
Teste: contrato VIRTUA + TV + CHIP deve permanecer um contrato com três itens.

[ ] RN04 — CICLO DE VIDA DE CADA ITEM DE VENDA
Estados: CADASTRADA, AGENDADA, INSTALADA, CANCELADA.
Sequência normal obrigatória: CADASTRADA -> AGENDADA -> INSTALADA.
Cancelamento permitido a partir de qualquer um dos três estados.
Reativação: CANCELADA -> CADASTRADA (não retorna diretamente a AGENDADA ou INSTALADA).
Apoio ou ADM autoriza reativação; Vendedor apenas solicita.
Teste: bloquear CADASTRADA -> INSTALADA; permitir INSTALADA -> CANCELADA -> CADASTRADA.

[ ] RN05 — PRODUTO PRINCIPAL E STATUS GERAL
A escolha do produto principal é automática pela prioridade VIRTUA > TV > CHIP > FONE. O status geral do contrato acompanha o status desse produto, independentemente do status dos demais itens.
Teste: TV instalada + VIRTUA agendada => status geral AGENDADA; somente CHIP + FONE => CHIP é principal.
PENDÊNCIA: como determinar o estado geral de contrato contendo somente Wi-Fi Mesh ou outros adicionais; como tratar dois itens de mesma prioridade.

[ ] RN06 — COMISSÕES DOS VENDEDORES
Os valores da base são comissões dos vendedores, NÃO preços cobrados ao cliente. O valor calculado deve permanecer separado do valor importado. Divergências exigem revisão (RN01).
- VIRTUA: comissão fixa conforme plano e modalidade (SOLAR/comum), independente dos outros produtos. Na base foram observados VIRTUA 600MB R$ 79,90 e SOLAR VIRTUA 600MB R$ 99,90; confirmar tabela oficial e demais planos.
- CHIP: comissão fixa conforme plano e modalidade, independente de combos; valores dos demais planos ainda a parametrizar.
- FONE: comissão atual de R$ 35,00, independente dos outros produtos.
- TV: TV isolada = R$ 89,00; TV + CHIP sem VIRTUA = R$ 89,00; TV + VIRTUA = R$ 119,00; TV + VIRTUA + pelo menos mais um produto = R$ 139,00. Demais combos sem VIRTUA, especialmente com FONE, ainda a confirmar.
- Wi-Fi Mesh: R$ 15,00 por unidade, independente de outros produtos.
- Mudança de Tecnologia: comissão fixa de R$ 5,00 por operação.
Teste: TV+VIRTUA+CHIP muda comissão da TV para R$ 139,00, mas não altera a comissão da VIRTUA; 3 Mesh = R$ 45,00.
PENDÊNCIA: tabela completa de planos, vigência de valores e exceções de combos.

[ ] RN07 — CONFIRMAÇÃO HUMANA DE CONFLITOS COMERCIAIS
Se alteração aprovada gerar alerta/conflito comercial que admita exceção, apresentar aviso ao Apoio e permitir confirmação explícita, registrando a decisão. Essa confirmação não dispensa autenticação nem autoriza violar invariantes técnicas ou regras de transição obrigatórias.
Teste: conflito comercial com aviso => Apoio confirma e sistema registra; sem confirmação => não aplica.

[ ] RN08 — HISTÓRICO OBRIGATÓRIO
Toda alteração efetivada em contrato/item registra: usuário responsável, perfil, data/hora, campo, valor anterior, valor novo, motivo e, quando aplicável, autorização/alerta. Cancelamentos em cascata e reativações também geram eventos ligados à operação original. O histórico é preservado após reativação.
Teste: cancelar e reativar um item; histórico mantém os dois eventos e seus responsáveis.

[ ] RN09 — MUDANÇA DE TECNOLOGIA NÃO É VENDA COMERCIAL
Mudança de Tecnologia gera R$ 5,00 de comissão por operação, não aumenta o contador de vendas comerciais e deve ser exibida separadamente em relatórios de produção e comissões.
Teste: 10 vendas + 5 mudanças = 10 vendas comerciais e R$ 25,00 em comissões de mudança.

[ ] RN10 — ELEGIBILIDADE PARA PAGAMENTO
Venda comercial: comissão elegível somente se estiver INSTALADA. Venda CADASTRADA, AGENDADA ou CANCELADA não é elegível.
Mudança de Tecnologia: elegível a partir de CADASTRADA; se CANCELADA, deixa de ser elegível.
Comissão calculada não equivale necessariamente a comissão elegível. Não excluir históricos de comissões já apuradas.
Teste: venda agendada não entra no total elegível; instalada entra; cancelada sai. Mudança cadastrada entra, cancelada sai.
PENDÊNCIA: decidir explicitamente como apurar itens secundários não instalados/cancelados quando o principal está instalado e como tratar pagamentos já realizados.

[ ] RN11 — CANCELAMENTO E REATIVAÇÃO EM CASCATA
Cancelar VIRTUA => cancelar todos os itens ainda não cancelados do contrato.
Cancelar TV => cancelar TV e todos os outros itens ainda não cancelados, EXCETO VIRTUA.
Cancelar CHIP, FONE ou Wi-Fi Mesh => cancelar somente o próprio item (para Mesh, confirmar se há alguma exceção).
Cada ação cria um grupo de cancelamento com os itens efetivamente alterados. Item que JÁ estava cancelado NÃO entra no grupo novo.
Ao reativar o produto que originou um cancelamento em cascata, reativar para CADASTRADA todos e somente os itens efetivamente afetados por aquele mesmo grupo. A reativação exige Apoio ou ADM. Preservar o histórico original.
Teste: CHIP previamente cancelado; cancelar TV; reativar TV => CHIP permanece cancelado. Outro teste: cancelar VIRTUA e reativar => todos os itens atingidos por essa operação retornam a CADASTRADA.
PENDÊNCIA: definir solução para alterações posteriores concorrentes no mesmo item (não reverter alterações posteriores indevidamente).
