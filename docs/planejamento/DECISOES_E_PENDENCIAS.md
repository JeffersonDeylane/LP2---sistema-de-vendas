# Decisões e pendências

## Decisões confirmadas
- Aplicação Java com primeira versão persistida em **uma planilha Excel central** (`BASE_GERAL.xlsx`, múltiplas abas); sem banco de dados nesta etapa.
- Fontes: planilhas Excel/CSV exportadas de outros sistemas. Python existente continua independente; integração por arquivos.
- Três perfis: Vendedor (sugere), Apoio (aprova/recusa e reativa), ADM (acesso completo e relatórios).
- Número de contrato será identificador principal; possível necessidade de incluir empresa na chave.
- Divergências passam por decisão manual, com histórico de usuário, data e motivo.
- Produto principal: VIRTUA > TV > CHIP > FONE; seu estado determina o estado geral.
- Estados: Cadastrada → Agendada → Instalada; cancelamento permitido em qualquer etapa; reativação Cancelada → Cadastrada.
- VIRTUA cancelada cancela todos os itens ativos; TV cancelada cancela todos os ativos exceto VIRTUA; outros produtos cancelam apenas a si.
- Reativação de cascata recupera apenas os itens efetivamente cancelados por aquela mesma ação; itens já cancelados ficam assim.
- Valores importados são comissões de vendedores, não preços ao cliente.
- VIRTUA e CHIP: comissões por plano/modalidade, independentes do combo; FONE: R$ 35; Mesh: R$ 15/unidade.
- TV: sozinha R$ 89; com CHIP sem VIRTUA R$ 89; com VIRTUA R$ 119; com VIRTUA e ao menos outro produto R$ 139.
- Mudança de Tecnologia: R$ 5 por operação; não conta como venda comercial; elegível desde cadastro, mas cancelamento retira elegibilidade.
- Venda comercial: comissão elegível se instalada.

## Pontos a decidir antes da implementação
- [ ] Número de contrato é único globalmente ou por empresa?
- [ ] Identificação de itens repetidos do mesmo produto em um contrato.
- [ ] Contratos com somente Wi-Fi Mesh (sem VIRTUA/TV/CHIP/FONE): qual principal?
- [ ] Comissões de TV + FONE sem VIRTUA e combos sem VIRTUA com três ou mais produtos.
- [ ] Tabela oficial de todos os planos VIRTUA/CHIP e sua vigência.
- [ ] Comissão de produto secundário não instalado quando o principal está instalado.
- [ ] Políticas para pagamentos já realizados e posterior cancelamento/estorno.
- [ ] Efeito de alterações posteriores sobre reativação de grupos antigos.
- [ ] Modelo de autenticação sem expor senhas no Excel; preferir credenciais com hash e acesso restrito.
- [ ] Concorrência na atualização da planilha central; backup e gravação atômica.
