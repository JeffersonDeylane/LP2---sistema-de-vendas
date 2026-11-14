# P0-03 — Persistência dos usuários

Entrega validada em **14/11/2026**, sobre os perfis da P0-01, sem novas dependências.

## Arquivos e responsabilidades

- `src/main/java/RepositorioUsuariosExcel.java`: leitura estrita, reconstrução dos
  perfis, próximo ID, inserção/atualização e validação da base completa.
- `src/main/java/PersistenciaUsuariosException.java`: falhas de formato e acesso,
  com contexto e causa técnica quando disponível, sem incluir hashes nas mensagens.
- `src/main/java/lp2/processamento/PlanilhasIO.java`: novo `salvarComBackup` com
  temporários, verificação de legibilidade e substituições atômicas. Os métodos
  já utilizados pelo processamento mantiveram seu comportamento.
- `src/main/java/Main.java`: carga inicial pelo repositório, caminho configurável
  e interrupção com mensagem quando a base é inválida/inacessível.
- `src/test/java/RepositorioUsuariosExcelTest.java`: 35 testes em diretórios
  temporários, incluindo inicialização, dados inválidos e falhas de gravação.
- `.gitignore`: exclusão da base padrão, backup e temporários de usuários.
- `README.md`, `docs/STATUS_DO_PROJETO.md`, `CHECKLIST.md`, `ESTRUTURA_DADOS.md` e
  `DECISOES_E_PENDENCIAS.md`: esquema, uso, critérios atendidos e limites.

## API e decisões

```java
RepositorioUsuariosExcel repositorio = new RepositorioUsuariosExcel();
List<Usuario> usuarios = repositorio.listar();
int idDisponivel = repositorio.proximoId();
// O futuro serviço de cadastro constrói o usuário com hash já preparado.
// repositorio.salvar(usuario);
```

O construtor com `Path` permite escolher outra base. O padrão é
`dados/BASE_GERAL.xlsx`, relativo ao diretório de execução; o primeiro argumento
do `Main` substitui esse caminho. A API não recebe senha em texto puro: preserva
o campo `senhaHash` existente. Geração e validação criptográfica ficam na P0-04.

A aba tem as colunas fixas `ID`, `NOME`, `LOGIN`, `SENHA_HASH`, `PERFIL`, `ATIVO`,
`MATRICULA`. ID é numérico inteiro positivo, ATIVO é booleano e os demais campos
são textuais. Vendedor exige matrícula; outros perfis exigem esse campo vazio.
Veja o [esquema completo](ESTRUTURA_DADOS.md).

Cada leitura constrói instâncias reais de Vendedor, Apoio ou Administrador com
os IDs originais. Cada salvamento relê a base e substitui pelo ID ou acrescenta
um registro, sem apagar outros usuários. IDs e logins duplicados na base são
recusados; o próximo ID considera também contas inativas e detecta esgotamento.
Essas garantias preservam identidade e capacidades relacionadas a RN08/RN09,
sem implementar autorização ou decisões de solicitações.

A leitura genérica de `PlanilhasIO` não foi reutilizada porque aceita outra aba,
converte valores e não valida o esquema de identidades. A escrita de tabelas foi
reutilizada para criar o cabeçalho inicial; atualizações alteram apenas a linha
do usuário, mantendo a aba e seus nomes definidos. A nova rotina de salvamento
exige troca atômica, pois a alternativa não atômica do salvamento dos relatórios
não atende à preservação da base anterior.

O salvamento prepara e reabre um temporário, grava um backup dos bytes da versão
anterior em `<nome>.bak`, também por temporário e troca atômica, e então troca a
base atomicamente. Falha no backup impede a alteração da base. Falha na troca
final deixa a base anterior, que pode também ser a versão contida no backup.
Não há restauração automática nem substituição silenciosa de dados inválidos.

## Verificações executadas

A suíte anterior passou com 19 testes antes das alterações. Na primeira execução
com persistência, dois testes falharam: diagnóstico de perfil desconhecido e
tratamento de caminho cujo ancestral era arquivo. A implementação foi corrigida,
sem remover ou enfraquecer os testes. A revisão identificou também a perda de
nomes definidos ao recriar a aba. Um teste adicional confirmou a falha, corrigida
pela atualização da linha sem recriar a aba. Na execução final, **54 testes
passaram, sem erros, falhas ou ignorados**:

| Suíte | Testes |
|---|---:|
| `UsuarioTest` | 14 |
| `RepositorioUsuariosExcelTest` | 35 |
| `lp2.processamento.ProcessamentoPlanilhasTest` | 5 |

Os testes conferem os três perfis, IDs, matrícula, hash, situação, comportamento
polimórfico após reconstrução, atualização e reabertura, próximo ID, outras abas
e fórmulas, posição da aba e nomes definidos, backup por bytes, estrutura inválida,
campos/perfis/IDs/logins inválidos, falhas de backup e serialização, arquivo
bloqueado e somente leitura. Os dois casos
específicos do Windows são ignorados em outros sistemas; nesta execução ambos
foram executados e aprovados. Os testes de inicialização exercitaram o `Main` com
base existente, ausente e corrompida e confirmaram que os exemplos não são salvos.

Comando efetivamente executado na raiz, com JDK Azul 25.0.2 e Maven 3.9.11,
compilando para Java 17 conforme o `pom.xml`:

```powershell
$env:JAVA_HOME = 'C:\Users\Marcone\.jdks\azul-25.0.2'
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd' -B -o '-Dmaven.repo.local=target/repositorio-maven' '-Dmaven.test.failure.ignore=false' test
```

Permanecem avisos preexistentes de APIs obsoletas do Maven/JDK e processamento,
além da mensagem de ausência de provedor Log4j na integração. Não impediram o build.

## Limitações e continuidade

- Um processo escritor por vez. A serialização das gravações protege instâncias
  na mesma JVM; não coordena outras JVMs ou edição externa no Excel.
- `proximoId()` não reserva IDs. A futura gestão deve coordenar cadastros
  simultâneos; `salvar` trata ID existente como atualização.
- Necessário sistema de arquivos que suporte movimento atômico na pasta da base;
  não há alternativa não atômica nem garantia contra queda de energia.
- Um backup anterior, restauração manual e ausência de migração de esquema.
- P0-02, P0-04 e P0-05 continuam pendentes. Não há autorização, autenticação,
  serviço administrativo, geração de hashes ou sessão nesta entrega.
- A P0-07 ainda precisa do UML e evidência integrada da primeira entrega;
  esquema e decisões de persistência de usuários foram documentados nesta tarefa.
- Não foram alteradas as entidades da P0-01, processadas bases reais ou criadas
  contas administrativas persistidas. Não foram realizados commit ou push.
