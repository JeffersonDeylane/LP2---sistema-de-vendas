# Decisões e pendências

## Persistência dos usuários — P0-03, 14/11/2026

- Implementada em `RepositorioUsuariosExcel`, com esquema explícito documentado em
  [ESTRUTURA_DADOS.md](ESTRUTURA_DADOS.md), IDs preservados e reconstrução dos três perfis.
- Reutilizada a infraestrutura POI de `PlanilhasIO`; adicionada gravação com backup
  e troca exclusivamente atômica. A gravação anterior dos relatórios não foi alterada.
- Leitura estrita na base de usuários: a rotina genérica de importação pode usar
  outra aba e converter dados, comportamentos inadequados a identidades e perfis.
- Atualização por ID, validação completa antes de gravar e preservação das outras
  abas. Não existe recuperação automática a partir de arquivo inválido ou backup.
- Escritas serializadas dentro da JVM; uso por um processo escritor de cada vez.
  Coordenação entre processos e reserva concorrente de IDs permanecem pendentes.
- Cadastro autorizado, geração de hashes, login e sessão continuam nas próximas
  tarefas. Esta entrega não conclui RN08, RN09 ou histórico comercial.

As decisões comerciais abaixo são históricas; em caso de conflito, prevalece a
especificação vigente de regras de negócio.

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
- [ ] Concorrência entre processos na atualização da planilha central; backup e gravação atômica de usuários já implementados na P0-03.
