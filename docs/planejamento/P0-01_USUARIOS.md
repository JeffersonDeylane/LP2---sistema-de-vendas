# P0-01 — Estrutura dos usuários

Entrega verificada em **14/11/2026**, conforme a especificação vigente de
[regras de negócio](../regras/REGRAS_DE_NEGOCIO.md), especialmente RN08 e RN09.

## Modelo implementado

| Classe | Responsabilidade |
|---|---|
| `Usuario` | Abstração com ID imutável, nome, login, hash informado e situação encapsulados; validação e contrato das capacidades. |
| `Vendedor` | Preserva matrícula validada e os contratos existentes. Pode solicitar se ativo; não aprova nem gerencia usuários ou catálogo. |
| `Apoio` | Pode solicitar e revisar alterações de outro usuário se ativo; não gerencia usuários nem catálogo. |
| `Administrador` | Pode solicitar, revisar alterações de outro usuário e gerenciar usuários/catálogo se ativo. |

As classes permanecem sem `package`, seguindo o núcleo existente, separado de
`lp2.processamento`. Não foram criadas dependências, interfaces ou atributos sem
necessidade de negócio. Apoio e Administrador se especializam por comportamento;
somente Vendedor precisa da matrícula já prevista.

`podeSolicitarAlteracao()` foi movido para `Usuario`, mantendo o comportamento e
a chamada existente em `Vendedor`. `podeGerenciarCatalogo()` nega por padrão e é
sobrescrito por Administrador. Os métodos existentes e construtores de Usuario
e Vendedor foram preservados.

## Polimorfismo e limites das consultas

O `Main` percorre um vetor de `Usuario` com os três perfis. As implementações de
`podeAprovarAlteracoes()` e `podeGerenciarUsuarios()`, e a sobrescrita de
`podeGerenciarCatalogo()`, retornam capacidades diferentes conforme o objeto real,
sem comparação de classes ou conversão de tipo. Os testes também usam referências
`Usuario` para conferir os resultados distintos.

`podeAprovarAlteracoes()` sem argumento informa apenas a capacidade do perfil
ativo. A sobrecarga `podeAprovarAlteracoes(Usuario solicitante)` é final e combina
essa capacidade polimórfica com solicitante obrigatório e ID diferente do decisor.
Isso impede autoaprovação inclusive administrativa, mesmo quando outra instância
representa o mesmo ID. O ID é a identidade estável já existente; sua unicidade
deverá ser garantida pela futura gestão/persistência.

Nenhuma consulta aprova ou modifica uma solicitação. RN08 continua parcial:
autenticação, vínculo com a venda, escopo do Apoio e proteção das operações nos
serviços pertencem à P0-02 e etapas seguintes. RN09 também continua parcial no
modelo: entidade, decisão, conflitos, unicidade de pendências e histórico ainda
não existem. A consulta contextual não substitui essas verificações.

## Validação executada

Antes da implementação, os 5 testes existentes de processamento passaram.
Depois da implementação, a suíte Maven da raiz executou **19 testes: 14 de
UsuarioTest e 5 de ProcessamentoPlanilhasTest; zero falhas, erros ou ignorados**.

Os novos testes cobrem construção e campos comuns dos três perfis, matrícula,
validação sem perda dos dados anteriores, capacidades distintas por `Usuario`,
contas inicialmente inativas, desativação/reativação e impedimento de autoaprovação
por instância e ID, além de solicitante ausente.

Java e Maven não estavam no PATH. Foi utilizado o JDK Azul 25.0.2 instalado,
compilando com `release 17` do `pom.xml`, e o Maven 3.9.11 do IntelliJ.
O sandbox bloqueou a leitura do `java.security` do JDK; a execução ocorreu com
permissão ampliada. A primeira tentativa offline encontrou componentes de teste
ausentes; eles foram baixados para o cache dentro de `target/repositorio-maven`.
Comando da execução final:

```powershell
$env:JAVA_HOME = 'C:\Users\Marcone\.jdks\azul-25.0.2'
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd' -B -o '-Dmaven.repo.local=target/repositorio-maven' '-Dmaven.test.failure.ignore=false' test
```

O `Main` também foi executado com as entradas 2 e 0:

```powershell
@('2', '0') | & 'C:\Users\Marcone\.jdks\azul-25.0.2\bin\java.exe' '-Dfile.encoding=UTF-8' -cp target/classes Main
```

A demonstração apresentou aprovação negada ao Vendedor, permitida a Apoio e
Administrador para outro solicitante, gestão exclusiva do Administrador e
autoaprovação negada aos três. As validações antigas, a descrição e a saída do
menu continuaram funcionando; o processo terminou com código 0. As planilhas
reais não foram processadas; os testes da integração usam diretórios temporários.

O build emitiu avisos de API obsoleta no processamento e no Maven/JDK, além de
mensagem de ausência de provedor Log4j na integração. Não impediram os testes;
não foram adicionadas dependências para tratar avisos fora da P0-01.

## Continuidade

- P0-02: autorização nos serviços, autenticação exigida e escopo por operação.
- P0-03: persistência e reconstrução dos três perfis com IDs estáveis.
- P0-04/P0-05: gestão de contas, geração/verificação de hashes e login/logout.
- P0-07 e etapas comerciais: atualizar o UML completo e implementar as entidades
  de solicitação e histórico com todas as invariantes da RN09.

Os usuários fixos do `Main` continuam sendo exemplos fictícios, sem sessão
autenticada. Não foram realizados commit ou push.
