# Estrutura de persistência por planilhas

## USUARIOS — implementado na P0-03

**Atualização:** 14/11/2026. A API usa `dados/BASE_GERAL.xlsx` por padrão,
relativo ao diretório de execução. `RepositorioUsuariosExcel(Path arquivo)`
permite escolher outro caminho; o primeiro argumento do `Main` configura essa
mesma localização. Não há arquivo de usuários ou credenciais distribuído no Git.

A primeira linha deve conter exatamente estas sete colunas, nesta ordem:

| Coluna | Tipo da célula Excel | Contrato |
|---|---|---|
| `ID` | Numérico | Inteiro de 1 a 2147483647, único e imutável na entidade. |
| `NOME` | Texto | Obrigatório, conforme `Usuario`. |
| `LOGIN` | Texto | Obrigatório; unicidade sem distinguir maiúsculas/minúsculas. A grafia original é preservada. |
| `SENHA_HASH` | Texto | Conteúdo de `Usuario.getSenhaHash()`, obrigatório e preservado. Não existe campo de senha em texto puro. |
| `PERFIL` | Texto | Exatamente `VENDEDOR`, `APOIO` ou `ADMINISTRADOR`. |
| `ATIVO` | Booleano | Valor lógico nativo do Excel, sem conversão de texto para booleano. |
| `MATRICULA` | Texto ou célula vazia | Obrigatória para Vendedor; vazia para Apoio e Administrador. |

Não são aceitas fórmulas nos registros, colunas extras ou espaços externos em
textos. Os construtores do domínio continuam validando os campos. Linhas
inteiramente vazias são ignoradas; qualquer outro registro inválido impede a
carga completa. O erro identifica arquivo, aba e linha ou ID, sem imprimir hashes.

`PERFIL` determina a subclasse construída pelos construtores existentes, com o ID
original. Perfil desconhecido, ID repetido, login repetido, campos ou tipos
inválidos são recusados. Não há conversão para um perfil padrão nem descarte
silencioso de usuários.

`listar()` relê a base e devolve novas instâncias. `salvar(Usuario)` relê e valida
todos os usuários antes de inserir ou substituir pelo ID; altera apenas a linha
correspondente, mantendo posições, definições da aba e demais abas. Alterações
somente em memória não são persistidas até chamar `salvar`. `proximoId()` calcula
o maior ID persistido mais um, incluindo
inativos; não usa tamanho da lista e não reinicia ao recriar o repositório.
Esgotamento do intervalo é um erro. Não há exclusão de usuários nesta API.

Arquivo inexistente corresponde a uma base inicial vazia, criada apenas no
primeiro salvamento. Arquivo existente vazio/corrompido, aba ausente, cabeçalho
incompatível ou falha de acesso geram `PersistenciaUsuariosException`, preservando
a causa técnica. Uma base inválida também impede novas gravações.

### Backup e gravação segura

`PlanilhasIO.salvarComBackup` grava um temporário na mesma pasta e o reabre com
POI para conferir sua legibilidade. Se a base já existir, copia seus bytes para
outro temporário e substitui atomicamente o backup `<nome-do-arquivo>.bak`.
Somente então substitui atomicamente a base. Não há alternativa de substituição
não atômica: se o sistema de arquivos não a suportar, a operação falha.

O backup é a última versão anterior, não uma coleção de versões. A primeira
gravação não tem backup. Se a troca final falhar depois do backup, base e backup
podem conter a mesma versão anterior válida. Falha no backup impede a troca da
base; temporários são removidos ao encerrar a operação. A restauração é manual,
após corrigir o bloqueio e validar o arquivo; uma base inválida nunca é substituída
automaticamente pelo backup.

As gravações dos repositórios são serializadas na mesma JVM e cada uma relê a
base. **Usar um processo escritor por vez**: não há bloqueio entre JVMs ou contra
edição externa no Excel. `proximoId()` informa um candidato, sem reservá-lo;
cadastros simultâneos precisam de coordenação na futura gestão de usuários.
Uma leitura concorrente pode observar a versão válida anterior à troca atômica.
Não há garantia de recuperação de queda de energia nem migração de esquema.

Os campos de hash são tratados como dados já preparados pelo futuro serviço de
cadastro. A P0-03 não gera ou verifica hashes, não reconhece algoritmos e não
oferece criptografia do arquivo. Geração de hash com salt e parâmetros fica na
P0-04; acesso ao arquivo e backup deve ser restrito pela instalação.

## Demais abas — planejamento legado

As abas abaixo, exceto `USUARIOS`, continuam conceituais e não foram criadas por
esta entrega. Prevalece a especificação comercial vigente sobre o esquema legado.

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
