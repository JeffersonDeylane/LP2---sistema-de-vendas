# Diagramas Mermaid — Sistema de Tratamento de Vendas e Comissões

**Projeto:** Linguagem de Programação II — Java  
**Objetivo:** documentação dos diagramas da versão inicial, com persistência EXCLUSIVAMENTE em planilhas Excel. SQLite fica para uma versão futura.  
**Como usar:** copie o conteúdo de cada bloco `mermaid` para https://mermaid.live/ ou visualize este Markdown em um editor compatível.

> **Atenção:** nesta primeira versão não haverá banco de dados. O arquivo `BASE_GERAL.xlsx` será a fonte persistente central. Estes são diagramas de **planejamento**. As classes, assinaturas, campos e relacionamentos ainda podem ser refinados na implementação. O fluxo de produtos secundários e estornos de comissões pagas requer detalhamento adicional.

## 1. Diagrama de classes — Estrutura principal

**O que representa:** as entidades centrais do negócio: usuários com perfis distintos, clientes, vendedores, contratos, produtos e itens de venda. Ilustra **herança, composição, associações e multiplicidade**. O produto principal é calculado pela prioridade **VIRTUA → TV → CHIP → FONE**.

```mermaid
classDiagram
    direction TB

    class Usuario {
        <<abstract>>
        -Long id
        -String nome
        -String login
        -String senhaHash
        +temPermissao(Permissao) boolean
    }
    class Vendedor {
        +solicitarAlteracao()
        +solicitarReativacao()
    }
    class Apoio {
        +aprovarSolicitacao()
        +recusarSolicitacao()
        +reativarVenda()
    }
    class Administrador {
        +gerenciarUsuarios()
        +gerarRelatorio()
    }
    Usuario <|-- Vendedor
    Usuario <|-- Apoio
    Usuario <|-- Administrador

    class Cliente {
        -Long id
        -String nome
        -String documento
    }
    class Contrato {
        -String numero
        -List~ItemVenda~ itens
        +obterProdutoPrincipal() ItemVenda
        +obterStatusGeral() StatusVenda
        +calcularComissaoTotal() BigDecimal
    }
    class Produto {
        -Long id
        -String descricao
        -TipoProduto tipo
        -String modalidade
    }
    class ItemVenda {
        -Long id
        -int quantidade
        -StatusVenda status
        -BigDecimal comissaoInformada
        +alterarStatus(StatusVenda)
    }
    class OperacaoComercial {
        <<abstract>>
        -Long id
        -LocalDate dataCadastro
    }
    class VendaComercial
    class MudancaTecnologia
    class SeletorProdutoPrincipal {
        +selecionar(Contrato) ItemVenda
    }

    OperacaoComercial <|-- VendaComercial
    OperacaoComercial <|-- MudancaTecnologia
    VendaComercial --> Contrato
    OperacaoComercial --> Vendedor : responsavel
    Cliente "1" --> "0..*" Contrato
    Contrato "1" *-- "1..*" ItemVenda
    ItemVenda "*" --> "1" Produto
    Contrato ..> SeletorProdutoPrincipal
```

## 2. Diagrama de classes — Políticas de comissão e elegibilidade

**O que representa:** dois comportamentos de **polimorfismo** por meio de interfaces: (a) cálculo de comissão por plano, combo, quantidade ou operação; (b) decisão de elegibilidade para pagamento. Venda comercial exige estado **Instalada**; Mudança de Tecnologia gera **R$ 5,00** desde **Cadastrada**, mas deixa de ser elegível ao cancelar.

```mermaid
classDiagram
    direction TB
    class PoliticaComissao {
        <<interface>>
        +calcular(OperacaoComercial) BigDecimal
    }
    class ComissaoPorPlano {
        +calcular(OperacaoComercial) BigDecimal
    }
    class ComissaoPorCombo {
        +calcular(OperacaoComercial) BigDecimal
    }
    class ComissaoPorQuantidade {
        +calcular(OperacaoComercial) BigDecimal
    }
    class ComissaoPorOperacao {
        +calcular(OperacaoComercial) BigDecimal
    }
    PoliticaComissao <|.. ComissaoPorPlano
    PoliticaComissao <|.. ComissaoPorCombo
    PoliticaComissao <|.. ComissaoPorQuantidade
    PoliticaComissao <|.. ComissaoPorOperacao

    class PoliticaElegibilidade {
        <<interface>>
        +podePagar(OperacaoComercial) boolean
    }
    class ElegibilidadeVenda {
        +podePagar(OperacaoComercial) boolean
    }
    class ElegibilidadeMudanca {
        +podePagar(OperacaoComercial) boolean
    }
    PoliticaElegibilidade <|.. ElegibilidadeVenda
    PoliticaElegibilidade <|.. ElegibilidadeMudanca

    class CalculadoraComissao {
        +calcular(OperacaoComercial) BigDecimal
    }
    class ApuradorComissoes {
        +apurar(List~OperacaoComercial~)
    }
    CalculadoraComissao --> PoliticaComissao
    ApuradorComissoes --> CalculadoraComissao
    ApuradorComissoes --> PoliticaElegibilidade
```

**Exemplos comerciais:** VIRTUA e CHIP por plano/modalidade; FONE **R$ 35,00**; TV **R$ 89,00** isolada ou com CHIP, **R$ 119,00** com VIRTUA, **R$ 139,00** com VIRTUA e mais um produto; Wi-Fi Mesh **R$ 15,00 por unidade**. Algumas combinações de TV ainda precisam ser verificadas.

## 3. Diagrama de estados — Ciclo de vida da venda

**O que representa:** as transições da **RN04**. A sequência normal é **Cadastrada → Agendada → Instalada**. Qualquer etapa pode ir para **Cancelada**; uma reativação autorizada por Apoio/ADM retorna para **Cadastrada**.

```mermaid
stateDiagram-v2
    [*] --> Cadastrada
    Cadastrada --> Agendada: Agendar
    Agendada --> Instalada: Instalar
    Cadastrada --> Cancelada: Cancelar
    Agendada --> Cancelada: Cancelar
    Instalada --> Cancelada: Cancelar
    Cancelada --> Cadastrada: Reativar
    note right of Cancelada
        Reativacao exige autorizacao
        do Apoio ou Administrador
    end note
```

## 4. Fluxograma — Cancelamento e reativação em cascata

**O que representa:** a **RN11**. Cancelar VIRTUA afeta todos os produtos; cancelar TV afeta todos, exceto VIRTUA; cancelar CHIP, FONE ou outro item é individual. Um **grupo de cancelamento** registra apenas os itens efetivamente alterados; na reativação, itens já cancelados antes permanecem cancelados.

```mermaid
flowchart TD
    A["Solicitar cancelamento"] --> B{"Tipo do produto"}
    B -->|VIRTUA| C["Selecionar todos os produtos"]
    B -->|TV| D["Selecionar todos exceto VIRTUA"]
    B -->|CHIP, FONE ou outros| E["Selecionar somente o item"]
    C --> F["Excluir itens ja cancelados"]
    D --> F
    E --> F
    F --> G["Registrar grupo de cancelamento"]
    G --> H["Cancelar itens efetivamente afetados"]
    H --> I["Registrar usuario, data e motivo"]
    I --> J{"Reativacao solicitada?"}
    J -->|Nao| K["Manter cancelamento"]
    J -->|Sim| L{"Apoio ou ADM autoriza?"}
    L -->|Nao| M["Manter situacao atual"]
    L -->|Sim| N["Consultar grupo original"]
    N --> O["Identificar itens cancelados pelo grupo"]
    O --> P["Verificar alteracoes posteriores"]
    P --> Q["Reativar somente itens permitidos para Cadastrada"]
    Q --> R["Registrar historico da reativacao"]
    R --> S["Recalcular estado e comissoes"]
```

## 5. Fluxograma — Importação e revisão de divergências

**O que representa:** como o sistema lê Excel/CSV, identifica contratos pelo número, compara com dados existentes e envia diferenças para **aprovação manual por campo**. Vendedor sugere; Apoio/ADM decide. Conflitos comerciais podem gerar aviso e confirmação explícita, sem dispensar validações obrigatórias.

```mermaid
flowchart TD
    A["Importar Excel ou CSV"] --> B["Criar lote de importacao"]
    B --> C["Ler e normalizar dados"]
    C --> D["Identificar numero do contrato"]
    D --> E{"Contrato existente?"}
    E -->|Nao| F["Validar novo registro"]
    E -->|Sim| G["Comparar campos"]
    G --> H{"Existem divergencias?"}
    H -->|Nao| I["Manter dados atuais"]
    H -->|Sim| J["Criar solicitacao de alteracao"]
    J --> K["Apoio ou ADM analisa campos"]
    K --> L{"Aprovar alteracao?"}
    L -->|Nao| M["Recusar e registrar motivo"]
    L -->|Sim| N["Verificar conflitos e validacoes"]
    N --> O{"Conflito comercial passivel de confirmacao?"}
    O -->|Nao| P["Aplicar alteracao valida"]
    O -->|Sim| Q["Exibir aviso e solicitar confirmacao"]
    Q --> R{"Usuario confirma?"}
    R -->|Sim| P
    R -->|Nao| S["Manter pendente"]
    P --> T["Registrar historico"]
    M --> T
    F --> U["Cadastrar se valido"]
    T --> V["Atualizar relatorios"]
    U --> V
    I --> V
```

## 6. Diagrama de persistência — Planilha principal (substitui SQLite)

**O que representa:** o sistema Java lê arquivos externos, compara com os registros da base central e salva somente alterações autorizadas em `BASE_GERAL.xlsx`. A base possui abas relacionadas por identificadores; não há banco de dados nesta versão.

```mermaid
flowchart TD
    A["Relatorios Excel e CSV externos"] --> B["Importador Java Apache POI"]
    B --> C["Normalizacao e comparacao"]
    D[("BASE_GERAL.xlsx")] --> C
    C --> E["Solicitacoes e divergencias"]
    E --> F["Apoio ou ADM revisa"]
    F --> G["Aplicar somente decisoes aprovadas"]
    G --> H["Gravar copia temporaria e validar"]
    H --> I["Backup e substituicao segura da base"]
    I --> D
    D --> J["Relatorios Excel e CSV"]
```

## 7. Estrutura lógica das abas da BASE_GERAL.xlsx

**O que representa:** um mapa de chaves entre as abas, semelhante a um diagrama de entidades, mas implementado com planilhas e não com tabelas SQL.

```mermaid
erDiagram
    USUARIOS {
        string usuario_id PK
        string nome
        string login
        string perfil
    }
    CONTRATOS {
        string contrato_id PK
        string numero_contrato
        string empresa
        string vendedor_id FK
        string cliente_id FK
    }
    CLIENTES {
        string cliente_id PK
        string nome
    }
    ITENS_VENDA {
        string item_id PK
        string contrato_id FK
        string produto_id FK
        int quantidade
        string status
        decimal comissao_informada
    }
    PRODUTOS {
        string produto_id PK
        string tipo
        string plano
        string modalidade
    }
    SOLICITACOES {
        string solicitacao_id PK
        string contrato_id FK
        string solicitante_id FK
        string status
    }
    DECISOES {
        string decisao_id PK
        string solicitacao_id FK
        string usuario_id FK
        string resultado
    }
    HISTORICO {
        string historico_id PK
        string contrato_id FK
        string usuario_id FK
        string campo
        string valor_anterior
        string valor_novo
        datetime data_hora
    }
    GRUPOS_CANCELAMENTO {
        string grupo_id PK
        string contrato_id FK
        string responsavel_id FK
    }
    ITENS_CANCELADOS {
        string registro_id PK
        string grupo_id FK
        string item_id FK
        boolean reativado
    }
    IMPORTACOES {
        string lote_id PK
        string nome_arquivo
        datetime data_importacao
    }
    TABELA_COMISSOES {
        string politica_id PK
        string produto_tipo
        string modalidade
        decimal valor
        string vigencia
    }

    CLIENTES ||--o{ CONTRATOS : possui
    USUARIOS ||--o{ CONTRATOS : vende
    CONTRATOS ||--|{ ITENS_VENDA : contem
    PRODUTOS ||--o{ ITENS_VENDA : identifica
    CONTRATOS ||--o{ SOLICITACOES : recebe
    USUARIOS ||--o{ SOLICITACOES : solicita
    SOLICITACOES ||--o{ DECISOES : recebe
    USUARIOS ||--o{ DECISOES : analisa
    CONTRATOS ||--o{ HISTORICO : registra
    USUARIOS ||--o{ HISTORICO : executa
    CONTRATOS ||--o{ GRUPOS_CANCELAMENTO : possui
    GRUPOS_CANCELAMENTO ||--|{ ITENS_CANCELADOS : agrupa
    ITENS_VENDA ||--o{ ITENS_CANCELADOS : afetado
```

**Observação:** a notação PK/FK serve apenas para documentar identificadores lógicos; o Excel não impõe essas restrições. O Java precisará validá-las. Recomenda-se guardar o número do contrato como texto, para preservar zeros iniciais, e separar `contrato_id` do número original quando a mesma numeração puder aparecer em mais de uma empresa.

## 8. Diagrama de classes — Armazenamento substituível

**O que representa:** interface de persistência e uma implementação Excel. No futuro, será possível adicionar SQLite sem reescrever as regras de negócio.

```mermaid
classDiagram
    class RepositorioContrato {
        <<interface>>
        +buscarPorId(String) Contrato
        +listar() List~Contrato~
        +salvar(Contrato) void
    }
    class RepositorioContratoExcel {
        -String caminhoBase
        +buscarPorId(String) Contrato
        +listar() List~Contrato~
        +salvar(Contrato) void
    }
    class ServicoContrato {
        -RepositorioContrato repositorio
        +consultar(String) Contrato
        +aplicarAlteracaoAprovada(Contrato) void
    }
    class GerenciadorArquivoExcel {
        +abrirBase() void
        +salvarComBackup() void
    }
    RepositorioContrato <|.. RepositorioContratoExcel
    ServicoContrato --> RepositorioContrato
    RepositorioContratoExcel --> GerenciadorArquivoExcel
```

## Checklist para o grupo — Versão somente com planilhas

- [ ] Definir o formato de `BASE_GERAL.xlsx` e os cabeçalhos de cada aba.
- [ ] Definir IDs estáveis para contratos, itens, usuários e solicitações.
- [ ] Validar número de contrato e empresa sem perder zeros à esquerda.
- [ ] Criar leitura e gravação de Excel usando Apache POI.
- [ ] Criar rotina de cópia de segurança antes de salvar alterações.
- [ ] Gravar em arquivo temporário; conferir integridade antes de substituir a base.
- [ ] Impedir edições concorrentes: apenas uma gravação por vez.
- [ ] Importar Excel/CSV sem apagar os arquivos originais.
- [ ] Comparar registros e encaminhar diferenças para aprovação individual.
- [ ] Registrar histórico, decisões, cancelamentos e reativações em abas próprias.
- [ ] Testar importação repetida para evitar duplicação.
- [ ] Testar planilha bloqueada, corrompida, sem cabeçalhos e com linhas inválidas.
- [ ] Gerar relatórios e arquivo consolidado a partir da base principal.
- [ ] Manter possibilidade de futura implementação `RepositorioContratoSQLite`.
- [ ] Confirmar tratamento de produtos secundários e serviços adicionais isolados.
- [ ] Definir segurança dos dados e credenciais de usuários antes do uso real.

**Nota de escala:** uma aba `.xlsx` suporta até 1.048.576 linhas. Para bases muito grandes, particionar históricos por período ou migrar para banco de dados posteriormente. Não tratar o Excel como armazenamento multiusuário simultâneo: centralizar as escritas no aplicativo e utilizar bloqueio/serialização e backups.
