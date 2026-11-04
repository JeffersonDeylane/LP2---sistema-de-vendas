# Estrutura proposta de persistência por planilhas

**Arquivo planejado:** `BASE_GERAL.xlsx` — ainda não criado.  
**Observação:** a estrutura é conceitual; nomes e colunas devem ser aprovados antes da implementação.

| Aba sugerida | Finalidade |
|---|---|
| `CONTRATOS` | Identificação do contrato, cliente e vendedor |
| `ITENS_VENDA` | Produtos, quantidades, estados e comissões por item |
| `USUARIOS` | Identidades e perfis (sem senhas em texto puro) |
| `SOLICITACOES` | Propostas de alteração e estado da solicitação |
| `DECISOES` | Aprovação ou recusa de campos |
| `HISTORICO` | Autor, data, campo, valor antigo, novo e motivo |
| `CANCELAMENTOS` | Grupos de cancelamento e referência aos itens afetados |
| `IMPORTACOES` | Lotes, arquivos de origem e registro de processamento |
| `TABELA_COMISSOES` | Políticas e valores por produto, plano e vigência |

## Cuidados
- Um contrato pode ter vários itens; não duplicar o contrato na aba `CONTRATOS` para representar vários produtos.
- Os valores originais importados devem ser preservados para auditoria e comparação.
- Um único arquivo Excel não oferece transações multiusuário como um banco: planejar cópia de segurança, bloqueio de edição concorrente e gravações seguras.
- Não versionar `BASE_GERAL.xlsx` com dados comerciais ou pessoais no GitHub.
- O banco SQLite poderá ser considerado numa versão futura, mantendo as regras de domínio independentes da camada de persistência.
