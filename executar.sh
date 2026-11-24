#!/usr/bin/env bash

# Execute com: bash executar.sh
set -u

if ! cd -- "$(dirname -- "${BASH_SOURCE[0]}")"; then
    printf '%s\n' 'Nao foi possivel acessar a pasta do projeto.' >&2
    exit 1
fi

if ! command -v java >/dev/null 2>&1; then
    printf '%s\n' 'Java nao encontrado. Configure um JDK 21 ou superior no PATH.' >&2
    exit 1
fi

if [[ -f './FERRAMENTAS/apache-maven-3.10.0/bin/mvn' ]]; then
    maven_executar=(bash './FERRAMENTAS/apache-maven-3.10.0/bin/mvn')
elif command -v mvn >/dev/null 2>&1; then
    maven_executar=(mvn)
else
    printf '%s\n' 'Maven nao encontrado na pasta FERRAMENTAS nem no PATH.' >&2
    printf '%s\n' 'Instale o Maven e execute este arquivo novamente.' >&2
    exit 1
fi

printf '%s\n' 'Compilando e iniciando o sistema...'
printf '%s\n\n' 'Na primeira execucao, o Maven pode precisar baixar dependencias.'
"${maven_executar[@]}" -q compile exec:java '-Dexec.mainClass=Main'
resultado_executar=$?

if [[ "$resultado_executar" -ne 0 ]]; then
    printf '\n%s\n' 'Nao foi possivel concluir a execucao. Confira as mensagens acima.' >&2
fi

exit "$resultado_executar"
