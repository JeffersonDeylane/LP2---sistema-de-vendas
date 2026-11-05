from openpyxl import load_workbook
from openpyxl.styles import Font
from openpyxl.utils import get_column_letter
from openpyxl.utils.dataframe import dataframe_to_rows
import pandas as pd
import win32com.client as win32
import os
import re
from datetime import datetime

# --- CONFIGURAÇÃO DE PASTAS ---
# Cria as pastas automaticamente se elas não existirem
PASTA_ENTRADA = r"C:\Users\Usuario\Desktop\programas\arquivos_para_processar"
PASTA_SAIDA = r"C:\Users\Usuario\Desktop\programas\arquivos_para_processar\saida"
PASTA_SAIDA_DO_BOT = r"C:\Users\Usuario\Desktop\programas\bot-whatsapp-estrutura_NOVO\dados"
os.makedirs(PASTA_ENTRADA, exist_ok=True)
os.makedirs(PASTA_SAIDA, exist_ok=True)

# Obtém a data atual para o nome do arquivo (ex: 20.04)
data_atual = datetime.now().strftime("%d.%m")

def formatar_bordas(intervalo):
    for borda in range(7, 13):
        intervalo.Borders(borda).LineStyle = 1
        intervalo.Borders(borda).Weight = 2


def preparar_tabela_dinamica(wb, cache, nome_aba, nome_tabela, linha, ao_final=False):
    for aba in wb.Worksheets:
        if aba.Name == nome_aba:
            aba.Delete()
            break
    if ao_final:
        ws = wb.Worksheets.Add(After=wb.Worksheets(wb.Worksheets.Count))
    else:
        ws = wb.Worksheets.Add()
    ws.Name = nome_aba
    tabela = cache.CreatePivotTable(
        TableDestination=ws.Cells(linha, 1), TableName=nome_tabela
    )
    return ws, tabela


def criar_tabelas_dinamicas(caminho_arquivo):
    excel = win32.Dispatch("Excel.Application")
    wb = None
    try:
        excel.Visible = False
        excel.DisplayAlerts = False
        wb = excel.Workbooks.Open(caminho_arquivo)
        ws = wb.Worksheets("Analítico")
        ultima_linha = ws.Cells(ws.Rows.Count, 1).End(-4162).Row
        ultima_coluna = ws.Cells(1, ws.Columns.Count).End(-4159).Column
        origem = ws.Range(ws.Cells(1, 1), ws.Cells(ultima_linha, ultima_coluna))
        excel.CalculateFull()
        cache = wb.PivotCaches().Create(SourceType=1, SourceData=origem)
        criar_tabela_dinamica(wb, cache, ultima_linha)
        criar_tabela_dinamica_vendedor_data(wb, cache, excel)
        wb.Save()
        print("✅ Tabelas dinâmicas criadas com sucesso!")
    finally:
        try:
            if wb is not None:
                wb.Close(SaveChanges=False)
        finally:
            excel.Quit()


def criar_tabela_dinamica_vendedor_data(wb, cache, excel):
    ws_pivot, tabela = preparar_tabela_dinamica(
        wb, cache, "Planilha2", "TabelaDinamicaVendedorData", 1, ao_final=True
    )

    campo_vendedor = tabela.PivotFields("Vendedor")

    campo_vendedor.Orientation = 1
    campo_vendedor.Position = 1

    # ============================================================
    # DATA INSTALAÇÃO = COLUNAS
    # ============================================================
    campo_data = tabela.PivotFields("DataInstalacao")

    campo_data.Orientation = 2
    campo_data.Position = 1

    # ============================================================
    # PRODUTOS = VALORES
    # CONTAGEM DE PRODUTOS
    # ============================================================
    tabela.AddDataField(
        tabela.PivotFields("Produtos"),
        "Contagem de Produtos",
        -4112
    )

    # ============================================================
    # ATUALIZA A TABELA
    # ============================================================
    tabela.RefreshTable()

    # ============================================================
    # ESTILO DA TABELA
    # ============================================================
    try:
        tabela.TableStyle2 = "PivotStyleMedium9"
    except:
        pass

    # ============================================================
    # FORMATAÇÃO DAS DATAS
    # ============================================================
    try:
        campo_data.NumberFormat = "dd/mm/yyyy"
    except:
        pass

    # ============================================================
    # ALINHAMENTO
    # ============================================================
    try:
        ws_pivot.Columns("A").HorizontalAlignment = -4131

        tabela.DataBodyRange.HorizontalAlignment = -4108
        tabela.DataBodyRange.VerticalAlignment = -4108

    except:
        pass

    # ============================================================
    # LARGURA DAS COLUNAS
    # ============================================================
    try:
        ws_pivot.Columns("A").ColumnWidth = 28

        for coluna in range(2, ws_pivot.UsedRange.Columns.Count + 1):
            ws_pivot.Columns(coluna).ColumnWidth = 13

    except:
        pass

    # ============================================================
    # DEIXA OS CAMPOS VAZIOS VERMELHOS
    # ============================================================
    try:

        corpo = tabela.DataBodyRange

        if corpo is not None:

            for linha in range(
                1,
                corpo.Rows.Count + 1
            ):

                for coluna in range(
                    1,
                    corpo.Columns.Count + 1
                ):

                    celula = corpo.Cells(
                        linha,
                        coluna
                    )

                    valor = celula.Value

                    # Campo vazio
                    if valor is None or str(valor).strip() == "":

                        # Vermelho
                        celula.Interior.Color = 255

                    else:

                        # Remove preenchimento
                        celula.Interior.ColorIndex = -4142

    except Exception as erro:

        print(
            f"⚠️ Erro ao pintar campos vazios: {erro}"
        )

    # ============================================================
    # DEIXA O CABEÇALHO EM NEGRITO
    # ============================================================
    try:

        tabela.RowRange.Font.Bold = True
        tabela.ColumnRange.Font.Bold = True

    except:
        pass

    # ============================================================
    # BORDAS
    # ============================================================
    try:

        formatar_bordas(tabela.TableRange1)

    except:
        pass

    # ============================================================
    # CONGELA LINHA E COLUNA
    # ============================================================
    try:

        ws_pivot.Activate()

        excel.ActiveWindow.SplitRow = 1
        excel.ActiveWindow.SplitColumn = 1

        excel.ActiveWindow.FreezePanes = True

    except:
        pass


def limpar_pasta_saida():
    """
    Remove todos os arquivos da pasta de saída.
    """

    if not os.path.exists(PASTA_SAIDA):
        return

    for arquivo in os.listdir(PASTA_SAIDA):
        caminho = os.path.join(PASTA_SAIDA, arquivo)

        if os.path.isfile(caminho):
            try:
                os.remove(caminho)
                print(f"🗑️ Removido: {arquivo}")
            except PermissionError:
                print(f"❌ Não foi possível remover '{arquivo}'. Feche o Excel e tente novamente.")
                raise


def salvar_analitico(df, caminho_saida):
    """Salva o Analítico com datas de instalação exibidas sem horário."""
    with pd.ExcelWriter(caminho_saida, engine="openpyxl") as writer:
        df.to_excel(writer, sheet_name="Analítico", index=False)
        ws = writer.sheets["Analítico"]
        if "SOMA" in df.columns:
            colunas = [df.columns.get_loc(nome) + 1 for nome in ("VIRTUA", "CHIP", "PORT", "TV", "FONE", "DCC")]
            coluna_soma = df.columns.get_loc("SOMA") + 1
            for linha in range(2, ws.max_row + 1):
                referencias = ",".join(f"{get_column_letter(coluna)}{linha}" for coluna in colunas)
                ws.cell(linha, coluna_soma, f"=SUM({referencias})")
        if "DataInstalacao" in df.columns:
            ws = writer.sheets["Analítico"]
            coluna = df.columns.get_loc("DataInstalacao") + 1
            for linha in range(2, ws.max_row + 1):
                ws.cell(linha, coluna).number_format = "dd/mm/yyyy"


def criar_vendas_consolidado():
    """
    Junta as abas 'Analítico' de todos os arquivos gerados
    e cria o arquivo vendas.xlsx.
    """

    arquivos = [
        os.path.join(PASTA_SAIDA, f)
        for f in os.listdir(PASTA_SAIDA)
        if f.lower().endswith(".xlsx")
        and "ANALITICO" in f.upper()
    ]

    if not arquivos:
        print("⚠️ Nenhum arquivo encontrado para consolidar.")
        return

    bases = []

    for arquivo in arquivos:
        try:
            df = pd.read_excel(arquivo, sheet_name="Analítico")
            bases.append(df)
            print(f"✔ Lido: {os.path.basename(arquivo)}")
        except Exception as e:
            print(f"Erro ao ler {arquivo}: {e}")

    if not bases:
        print("Nenhuma base foi carregada.")
        return

    df_final = pd.concat(bases, ignore_index=True)

    caminho_saida = os.path.join(PASTA_SAIDA_DO_BOT, "vendas.xlsx")

    try:
        salvar_analitico(df_final, caminho_saida)
    except PermissionError:
        print(f"\n❌ O arquivo '{caminho_saida}' está aberto ou sendo utilizado por outro programa.")
        print("Feche o Excel ou o bot do WhatsApp e tente novamente.")
        return

    print(f"\n📦 Arquivo consolidado criado:")
    print(caminho_saida)


def normalizar_contrato(valor):
    """Compara contratos sem o decimal artificial criado por células vazias."""
    if pd.isna(valor):
        return ""
    texto = str(valor).strip()
    return re.sub(r"^(\d+)\.0+$", r"\1", texto)


def tratar_contrato_chip(df_saida):
    """
    Remove os prefixos dos contratos da aba CHIP.

    Prefixos removidos:
    - 9700
    - 136
    - 9500
    - 63700
    """

    prefixos = ("9700", "136", "9500", "63700")

    # Garante que a coluna seja texto
    df_saida["CONTRATO_NET"] = (
        df_saida["CONTRATO_NET"]
        .map(normalizar_contrato)
    )

    # Remove apenas o prefixo encontrado
    for prefixo in prefixos:
        df_saida["CONTRATO_NET"] = df_saida["CONTRATO_NET"].str.replace(
            f"^{prefixo}",
            "",
            regex=True
        )

    return df_saida

def cruzar_analitico(
    wb,
    df,
    nome_aba,
    coluna_flag,
    coluna_contrato_aba,
    nome_coluna_saida
):
    """
    Faz o cruzamento entre a aba Analítico e outra aba do arquivo.

    Parâmetros
    ----------
    nome_aba:
        Nome da aba que será comparada.
        Ex.: "virtua", "chip", "tel fixo"

    coluna_flag:
        Coluna da aba Analítico que indica se deve participar do cruzamento.
        Ex.:
            VIRTUA
            CHIP
            FONE

    coluna_contrato_aba:
        Nome da coluna que contém o contrato na aba.
        Ex.:
            NR_CONTRATO
            CONTRATO_NET

    nome_coluna_saida:
        Nome da nova coluna criada.
        Ex.:
            virtua_analitico
            chip_analitico
            fone_analitico
    """

    # Seleciona somente os registros desejados
    contratos_analitico = (
        df[df[coluna_flag] == 1]["Contrato"]
        .map(normalizar_contrato)
    )

    contratos_analitico = set(contratos_analitico) - {""}

    nome_real = None

    for aba in wb.sheetnames:
        if aba.strip().lower() == nome_aba.strip().lower():
            nome_real = aba
            break

    if nome_real is None:
        print(f"Aba '{nome_aba}' não encontrada.")
        print("Abas existentes:", wb.sheetnames)
        return

    ws = wb[nome_real]

    # =====================================================
    # LOCALIZA A COLUNA DO CONTRATO
    # =====================================================

    coluna_contrato = None

    for c in range(1, ws.max_column + 1):

        if ws.cell(1, c).value == coluna_contrato_aba:
            coluna_contrato = c
            break

    if coluna_contrato is None:

        print(
            f"❌ Coluna {coluna_contrato_aba} não encontrada "
            f"na aba {nome_aba}."
        )

        return

    # =====================================================
    # CRIA A NOVA COLUNA
    # =====================================================

    nova_coluna = ws.max_column + 1

    ws.cell(
        1,
        nova_coluna
    ).value = nome_coluna_saida

    fonte_vermelha = Font(color="FF0000")
    fonte_verde = Font(color="008000")

    contratos_base = set()
    sobrando = {}

    # =====================================================
    # COMPARA A BASE
    # =====================================================

    for linha in range(2, ws.max_row + 1):

        contrato = ws.cell(
            linha,
            coluna_contrato
        ).value

        contrato = normalizar_contrato(contrato)
        if not contrato:
            continue

        contratos_base.add(contrato)

        if contrato in contratos_analitico:

            ws.cell(
                linha,
                nova_coluna
            ).value = contrato

        else:

            ws.cell(
                linha,
                coluna_contrato
            ).font = fonte_vermelha
            sobrando[contrato] = fonte_vermelha

    # =====================================================
    # ADICIONA CONTRATOS QUE EXISTEM APENAS
    # NO ANALÍTICO
    # =====================================================

    linha = ws.max_row + 1

    qtd = 0

    for contrato in sorted(contratos_analitico):

        if contrato not in contratos_base:

            ws.cell(
                linha,
                coluna_contrato
            ).value = contrato

            ws.cell(
                linha,
                coluna_contrato
            ).font = fonte_verde

            ws.cell(
                linha,
                nova_coluna
            ).value = contrato

            sobrando[contrato] = fonte_verde
            linha += 1
            qtd += 1


    coluna_olhar = nova_coluna + 1
    ws.cell(1, coluna_olhar, "olhar")
    for linha_olhar, (contrato, fonte) in enumerate(sobrando.items(), start=2):
        celula = ws.cell(linha_olhar, coluna_olhar, contrato)
        celula.font = fonte
    ws.column_dimensions[get_column_letter(coluna_olhar)].width = 18

    print(f"\n📄 Aba: {nome_aba}")
    print(f"🔴 Somente no IMPERIUM: {len(contratos_base - contratos_analitico)}")
    print(f"🟢 Somente no Analítico da claro: {qtd}")


#cria a tabela dinâmica
def criar_tabela_dinamica(wb, cache, ultima_linha):
    ws_pivot, tabela = preparar_tabela_dinamica(
        wb, cache, "Planilha1", "TabelaDinamica", 3
    )

    # Primeiro nível: Supervisor
    campo_supervisor = tabela.PivotFields("Supervisor")
    campo_supervisor.Orientation = 1      # Campo de Linha
    campo_supervisor.Position = 1         # Primeiro nível

    # Segundo nível: Vendedor
    campo_vendedor = tabela.PivotFields("Vendedor")
    campo_vendedor.Orientation = 1        # Campo de Linha
    campo_vendedor.Position = 2           # Segundo nível
    # ============================================================
    # CAMPOS DE VALORES
    # ============================================================

    for coluna in ("VIRTUA", "CHIP", "PORT", "TV", "SOMA", "Valor"):
        tabela.AddDataField(tabela.PivotFields(coluna), f"Soma de {coluna}", -4157)

    # ============================================================
    # ORDENA OS VENDEDORES PELA SOMA DE VALOR (MAIOR → MENOR)
    # ============================================================

    campo_vendedor.AutoSort(
        2,                  # 2 = ordem decrescente
        "Soma de Valor"     # Nome do campo de valores
    )


    # ============================================================
    # FORMATAÇÃO DA TABELA DINÂMICA deixas as coisas em negrito e o resto.
    # ============================================================


    # Estilo da Tabela Dinâmica
    tabela.TableStyle2 = "PivotStyleMedium9"

    # Atualiza
    tabela.RefreshTable()

    # ============================================================
    # Destaca VIRTUA e SOMA nas linhas dos vendedores.
    # ============================================================
    corpo = tabela.DataBodyRange
    if corpo is not None:
        for linha in range(1, corpo.Rows.Count + 1):
            for coluna in (1, 5):
                celula = corpo.Cells(linha, coluna)
                # xlPivotCellValue: mantém os subtotais e o total no estilo azul.
                if celula.PivotCell.PivotCellType == 0:
                    celula.Interior.Color = 65535
            celula_valor = corpo.Cells(linha, 6)
            celula_valor.Font.Bold = True
            celula_valor.NumberFormat = "#.##0,00"

    # Cabeçalho da tabela
    cabecalho = ws_pivot.Range("A4:G4")
    #deixa o cabeçalho em negrito e centralizado
    cabecalho.Font.Bold = True
    cabecalho.HorizontalAlignment = -4108   # Centro
    cabecalho.VerticalAlignment = -4108

        # Coluna dos rótulos
    coluna_a = ws_pivot.Columns("A")

    coluna_a.HorizontalAlignment = -4131   # Esquerda

    #números centralizados
    ws_pivot.Range("B:G").HorizontalAlignment = -4108

    #ajeita a largura das colunas
    ws_pivot.Columns.AutoFit()  

    # ============================================================
    # FORMATAÇÃO DAS CÉLULAS
    # ============================================================

    usada = ws_pivot.UsedRange


    formatar_bordas(usada)

    # Alinhamento vertical
    usada.VerticalAlignment = -4108   # Centro


def processar_base(caminho_arquivo, nome_arquivo):
    # Verifica se é CSV ou Excel
    try:
        if nome_arquivo.endswith('.csv'):
            df = pd.read_csv(caminho_arquivo)
        else:
            # Tenta ler a aba 'Analítico', se não existir, lê a primeira
            xl = pd.ExcelFile(caminho_arquivo)
            aba_leitura = 'Analítico' if 'Analítico' in xl.sheet_names else 0
            df = pd.read_excel(xl, sheet_name=aba_leitura)
    except Exception as e:
        print(f"❌ Erro ao ler {nome_arquivo}: {e}")
        return

    # Limpeza de colunas
    df.columns = df.columns.str.strip()

    # Remove colunas desnecessárias
    colunas_remover = ["IdVenda", "IdCliente", "Proposta", "TipoCliente", "IdVendedor","TipoPacote","ComTV"]
    df.drop(columns=colunas_remover, inplace=True, errors="ignore")

    # Criar colunas após 'Supervisor'
    if "Supervisor" in df.columns:
        idx_sup = df.columns.get_loc("Supervisor") + 1
        novas_cols = ["VIRTUA", "CHIP", "PORT", "TV", "FONE", "DCC", "SOMA"]

        # Insere as colunas se elas ainda não existirem
        for i, col in enumerate(novas_cols):
            if col not in df.columns:
                df.insert(idx_sup + i, col, 0)

    # Lógica de Identificação de Produtos
    for produto in ("VIRTUA", "CHIP", "TV", "FONE"):
        df[produto] = df['Produtos'].str.contains(produto, case=False, na=False).astype(int)

    # Lógica DCC: VIRTUA + DEBITO
    if 'FormaPagamento' in df.columns:
        condicao_dcc = (df['VIRTUA'] == 1) & (df['FormaPagamento'].str.contains('DÉBITO|DEBITO', case=False, na=False))
        df['DCC'] = condicao_dcc.astype(int)
    else:
        df['DCC'] = 0

    # PORT fica zerada para revisão manual; SOMA é gravada como fórmula no Excel.
    df["PORT"] = 0
    df["SOMA"] = df[["VIRTUA", "CHIP", "PORT", "TV", "FONE", "DCC"]].sum(axis=1)

    # Define o novo nome do arquivo baseado na origem
    tipo = "VNA" if "VNA" in nome_arquivo.upper() else "RDT"
    # Cria a coluna Empresa
    if "Empresa" not in df.columns:
        idx_sup = df.columns.get_loc("Supervisor") + 1 if "Supervisor" in df.columns else len(df.columns)
        df.insert(idx_sup, "Empresa", tipo)
    else:
        df["Empresa"] = tipo

    if "Supervisor" in df.columns:
        supervisores_pe = (
            df["Supervisor"].fillna("").astype(str).str.strip().str.upper()
            .isin(["ZAIRO JUNIOR", "EDVALDO SANTOS"])
        )
        df.loc[supervisores_pe, "Empresa"] = "PE"

    novo_nome = f"{tipo} - ANALITICO {data_atual}.xlsx"

    # Caminho final onde o arquivo será salvo no seu PC
    caminho_saida = os.path.join(PASTA_SAIDA, novo_nome)

    # Salva com o nome da aba como 'Analítico'
    salvar_analitico(df, caminho_saida)


    caminho_base = r"C:\Users\Usuario\Desktop\programas\arquivos_para_processar\analitico_da_claro\claro.xlsx"
    # Relê uma única vez para preservar a inferência de tipos usada nos cruzamentos.
    df_analitico = pd.read_excel(caminho_saida, sheet_name="Analítico")
    wb = load_workbook(caminho_saida)
    try:
        adicionar_base_complementar(wb, caminho_base, tipo)
        cruzamentos = (
            ("virtua", "VIRTUA", "NR_CONTRATO", "virtua_analitico"),
            ("tv", "TV", "DT", "tv_analitico"),
            ("chip", "CHIP", "CONTRATO_NET", "chip_analitico"),
            ("tel fixo", "FONE", "NR_CONTRATO", "fone_analitico"),
        )
        for parametros in cruzamentos:
            cruzar_analitico(wb, df_analitico, *parametros)
        wb.save(caminho_saida)
    finally:
        wb.close()

    criar_tabelas_dinamicas(caminho_saida)

    print(f"✅ Processado: {novo_nome} (Aba: Analítico | {len(df)} linhas)")


# copia a base complementar para o arquivo final

def adicionar_base_complementar(wb, caminho_base, tipo):

    abas = pd.read_excel(caminho_base, sheet_name=None)

    colunas_por_aba = {
        "virtua": ["NM_INDICADOR_NEGOCIO", "NR_CONTRATO", "NM_PARCEIRO"],
        "tv": ["NM_INDICADOR_NEGOCIO", "NM_PARCEIRO", "DT"],
        "chip": ["DT_ATIVACAO", "CD_COMTA", "CONTRATO_NET"],
        "tel fixo": ["NM_INDICADOR_NEGOCIO", "NM_PARCEIRO", "NR_CONTRATO"],
    }


    for nome_aba, df in abas.items():

        chave = nome_aba.strip().lower()

        if chave not in colunas_por_aba:
            continue

        df_saida = df.reindex(columns=colunas_por_aba[chave])

        # ======================
        # CHIP
        # ======================
        if chave == "chip":
            df_saida = tratar_contrato_chip(df_saida)

            codigo_excluir = "4ZEJ" if tipo == "RDT" else "3Z67"

            excluir = (
                df_saida["CD_COMTA"]
                .fillna("")
                .astype(str)
                .str.strip()
                .eq(codigo_excluir)
            )
            qtd = excluir.sum()
            df_saida = df_saida.loc[~excluir]

            print(f"🗑️ Removidas {qtd} linhas da aba 'chip'.")

        # ======================
        # Virtua / TV / Tel Fixo
        # ======================
        if chave in ("virtua", "tv", "tel fixo"):

            df_saida = df_saida[
                ~df_saida["NM_INDICADOR_NEGOCIO"]
                .fillna("")
                .str.strip()
                .str.casefold()
                .isin(["desistência", "venda bruta"])
            ]

            parceiro = (
                "VNA TELECOM LTDA ME"
                if tipo == "RDT"
                else "RDT TELECOM LTDA"
            )

            excluir = (
                df_saida["NM_PARCEIRO"]
                .fillna("")
                .str.strip()
                .eq(parceiro)
            )
            qtd = excluir.sum()
            df_saida = df_saida.loc[~excluir]

            print(f"🗑️ Removidas {qtd} linhas da aba '{nome_aba}'.")

        # Remove a aba antiga, se existir
        nome_planilha = nome_aba.strip()

        if nome_planilha in wb.sheetnames:
            del wb[nome_planilha]

        # Cria a nova aba
        ws = wb.create_sheet(title=nome_planilha)

        # Escreve o DataFrame
        for linha in dataframe_to_rows(df_saida, index=False, header=True):
            ws.append(linha)


def executar():

    # Limpa os arquivos antigos
    limpar_pasta_saida()

    # Lista todos os arquivos da pasta de entrada
    arquivos = [
        f for f in os.listdir(PASTA_ENTRADA)
        if f.endswith(('.csv', '.xlsx', '.xls'))
    ]

    if not arquivos:
        print(f"⚠️ Nenhum arquivo encontrado na pasta '{PASTA_ENTRADA}'.")
        return
    

    print(f"🔍 Encontrado(s) {len(arquivos)} arquivo(s) para processar...")

    for nome_arquivo in arquivos:
        caminho_completo = os.path.join(PASTA_ENTRADA, nome_arquivo)
        processar_base(caminho_completo, nome_arquivo)

    criar_vendas_consolidado()

    print(f"\n🎉 Tudo pronto! Os arquivos gerados estão na pasta: '{PASTA_SAIDA}'")

if __name__ == "__main__":
    executar()
