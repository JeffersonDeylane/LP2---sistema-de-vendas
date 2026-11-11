# Checklist de desenvolvimento — equipe de três integrantes

Marcação: `[ ]` não iniciado, `[~]` em andamento, `[x]` concluído.  
Em cada etapa, preencher: **Responsável:** _____ | **Prazo:** _____ | **Evidência:** _____.

## Etapa 0 — Repositório e organização
- [x] Criar repositório no GitHub e subir esta estrutura.
- [ ] Definir três responsáveis e regras de revisão de alterações.
- [ ] Criar quadro de tarefas (GitHub Issues ou Projects, opcional).
- [~] Conferir que nenhuma planilha real ou credencial será publicada.

## Etapa 1 — Refinar decisões
- [~] Revisar e aprovar RN01–RN11 no grupo.
- [X] Definir identificação de contrato por número ou empresa+número.
- [ ] Confirmar tabela completa de comissões e validade dos valores.
- [ ] Definir elegibilidade de itens secundários (RN10).
- [ ] Definir regra para contratos só com serviços adicionais.
- [ ] Definir cancelamentos posteriores, pagamento já realizado e estornos.
- [ ] Definir o formato de exportação esperado pelos scripts Python.

## Etapa 2 — Modelagem
- [ ] Revisar UML de classes principais.
- [ ] Revisar políticas polimórficas de comissão e elegibilidade.
- [ ] Revisar estados e cancelamento/reativação em cascata.
- [ ] Revisar fluxo de importação e aprovação manual.
- [ ] Aprovar o modelo da base geral em abas Excel.
- [ ] Conferir mínimo de 10 classes e 2 polimorfismos.

## Etapa 3 — Base Excel (a preparar)
- [ ] Definir cabeçalhos e tipos de cada aba de `BASE_GERAL.xlsx`.
- [ ] Definir relacionamentos por IDs entre abas.
- [ ] Definir armazenamento de histórico e decisões por campo.
- [ ] Planejar backup, escrita segura e recuperação de falhas.
- [ ] Planejar acesso concorrente ao arquivo central.
- [ ] Criar amostras fictícias para teste, sem dados de clientes reais.

## Etapa 4 — Projeto Java (futuro; não implementado neste pacote)
- [ ] Criar configuração Java/Maven ou Gradle.
- [ ] Implementar classes do domínio, enums e interfaces.
- [ ] Implementar importação/exportação Excel e CSV.
- [ ] Implementar leitura/gravação da base central.
- [ ] Implementar RN01–RN11 e testes unitários.
- [ ] Desenvolver interface por perfil, fila de revisão e relatórios.

## Etapa 5 — Validação e entrega
- [ ] Demonstrar importação com dois arquivos fictícios.
- [ ] Demonstrar divergência e aprovação por campo.
- [ ] Demonstrar cancelamento em cascata e reativação seletiva.
- [ ] Demonstrar cálculo e elegibilidade das comissões.
- [ ] Testar fechar/reabrir a aplicação e recuperar dados pela planilha.
- [ ] Demonstrar dois polimorfismos e dez regras de negócio.
- [ ] Preparar apresentação e dividir as falas do grupo.
