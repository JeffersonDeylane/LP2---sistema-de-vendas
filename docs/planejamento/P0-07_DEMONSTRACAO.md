# P0-07 — Demonstração da primeira entrega

Em 24/11/2026, o menu foi executado com três contas fictícias em uma planilha
temporária. O caminho dessa planilha foi passado como primeiro argumento de
`Main`. A execução usou o compilador Java 17 disponível no ambiente.

| Passo | Resultado observado |
|---|---|
| Entrar como Vendedor e tentar a opção 13 (editar usuário) | Login aceito e edição bloqueada. |
| Entrar como Apoio e tentar a opção 13 | Login aceito e edição bloqueada. |
| Entrar como Administrador e editar o usuário de ID 2 | Login aceito e novo nome e login salvos. |
| Encerrar e iniciar outro processo com a mesma planilha | Login aceito com o login atualizado. A opção 12 exibiu a conta com o ID 2, o novo nome e login, o perfil Vendedor e a situação ativa. |

As duas tentativas de edição por perfis sem acesso receberam a mensagem
“Apenas um administrador ativo pode gerenciar usuários.” A edição autorizada
recebeu a mensagem “Usuário atualizado.”

- [Esquema da aba USUARIOS](ESTRUTURA_DADOS.md#usuarios--implementado-na-p0-03)
- [Decisões de acesso](DECISOES_E_PENDENCIAS.md#autorização-da-primeira-entrega)
- [UML de usuários](../diagramas/09-usuários.mmd)
