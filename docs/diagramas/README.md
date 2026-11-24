# Diagramas e estado da implementação

Os arquivos `.mmd` abaixo acompanham o código atual. `DIAGRAMAS_COMPLETOS.md` preserva o modelo-alvo e as regras de negócio; ele não representa que todos esses fluxos já estejam implementados. Consulte os comentários de status antes de tratar uma relação como funcional.

| Diagrama | Estado refletido |
|---|---|
| [01 — Classes principais](01-classes-principais.mmd) | Cadastro e vendas ativos; classes do modelo ampliado marcadas como esqueleto. |
| [02 — Comissão e elegibilidade](02-comissoes-e-elegibilidade.mmd) | Declarações presentes, ainda não integradas à venda. |
| [03 — Estados](03-estados.mmd) | Venda ativa em rascunho ou confirmada; ciclo de instalação/cancelamento pendente. |
| [04 — Cancelamento](04-cancelamento-reativacao.mmd) | Fluxo especificado, serviço ainda pendente. |
| [05 — Importação e revisão](05-importacao-revisao.mmd) | Processamento RDT/VNA ativo; revisão comercial genérica pendente. |
| [06 — Persistência](06-persistencia-planilhas.mmd) | Repositórios de usuários e vendas usam `BASE_GERAL.xlsx`; processamento gera planilhas separadas. |
| [07 — Abas da base](07-abas-base-geral.mmd) | Abas comerciais e de usuários persistidas atualmente. |
| [08 — Repositórios](08-armazenamento-substituivel.mmd) | Repositórios ativos e declaração de repositório de contrato pendente. |
| [09 — Usuários](09-usuários.mmd) | Autenticação e perfis ativos; gestão/revisão ampliada ainda parcial. |

O fluxo ativo de vendas usa `Venda`, `EstadoVenda`, `ServicoVendas` e `RepositorioVendasExcel`. Ele calcula subtotal e total de venda a partir do preço do produto; não calcula comissão.

As classes do modelo ampliado foram adicionadas como estruturas iniciais. Serviços pendentes lançam `UnsupportedOperationException`; suas presenças nos diagramas indicam o desenho previsto, não comportamento disponível.

Os arquivos Java estão em [src/main/java](../../src/main/java). O [diagrama consolidado](DIAGRAMAS_COMPLETOS.md) contém as regras pretendidas para cancelamento, comissões e revisão de importação.
