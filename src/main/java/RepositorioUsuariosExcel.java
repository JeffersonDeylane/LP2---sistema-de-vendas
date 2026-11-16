import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lp2.processamento.PlanilhasIO;
import lp2.processamento.TabelaDados;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

public final class RepositorioUsuariosExcel {

    private static final String ABA = "USUARIOS";
    private static final List<String> COLUNAS = List.of(
        "ID", "NOME", "LOGIN", "SENHA_HASH", "PERFIL", "ATIVO", "MATRICULA"
    );
    private static final Object BLOQUEIO_ESCRITA = new Object();

    private final Path arquivo;
    private volatile boolean baseConhecida;

    public RepositorioUsuariosExcel() {
        this(Path.of("dados", "BASE_GERAL.xlsx"));
    }

    public RepositorioUsuariosExcel(Path arquivo) {
        if (arquivo == null) {
            throw new IllegalArgumentException("O caminho da base de usuários é obrigatório.");
        }
        this.arquivo = arquivo.toAbsolutePath().normalize();
    }

    public List<Usuario> listar() {
        try (Workbook planilha = abrir(lerConteudo())) {
            return List.copyOf(lerUsuarios(planilha));
        } catch (IOException | RuntimeException e) {
            throw falhaDeLeitura(e);
        }
    }

    public int proximoId() {
        int maiorId = listar().stream().mapToInt(Usuario::getId).max().orElse(0);
        if (maiorId == Integer.MAX_VALUE) {
            throw new PersistenciaUsuariosException("Não há IDs de usuário disponíveis em " + arquivo);
        }
        return maiorId + 1;
    }

    public void salvar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("O usuário é obrigatório.");
        }
        String perfil = perfilDoUsuario(usuario);

        // Evita perda de atualizações entre instâncias deste repositório na mesma JVM.
        synchronized (BLOQUEIO_ESCRITA) {
            try {
                byte[] anterior = lerConteudo();
                try (Workbook planilha = abrir(anterior)) {
                    List<Usuario> usuarios = lerUsuarios(planilha);
                    int indice = 0;
                    while (indice < usuarios.size() && usuarios.get(indice).getId() != usuario.getId()) {
                        indice++;
                    }
                    if (indice < usuarios.size()) {
                        usuarios.set(indice, usuario);
                    } else {
                        usuarios.add(usuario);
                    }
                    validarLogins(usuarios);

                    escreverUsuario(planilha, usuario, perfil);
                    List<Usuario> esperados = validarPersistencia(planilha, usuarios);
                    PlanilhasIO.salvarComBackup(planilha, arquivo, anterior,
                        verificacao -> validarPersistencia(verificacao, esperados));
                    baseConhecida = true;
                }
            } catch (IOException | RuntimeException e) {
                throw falha("salvar", e);
            }
        }
    }

    private void escreverUsuario(Workbook planilha, Usuario usuario, String perfil) {
        Sheet aba = planilha.getSheet(ABA);
        Row registro = null;
        for (Row linha : aba) {
            if (linha.getRowNum() > 0 && !linhaVazia(linha) && lerId(linha) == usuario.getId()) {
                registro = linha;
                break;
            }
        }
        if (registro == null) {
            registro = aba.createRow(aba.getLastRowNum() + 1);
        }
        registro.getCell(0, CREATE_NULL_AS_BLANK).setCellValue(usuario.getId());
        registro.getCell(1, CREATE_NULL_AS_BLANK).setCellValue(usuario.getNome());
        registro.getCell(2, CREATE_NULL_AS_BLANK).setCellValue(usuario.getLogin());
        registro.getCell(3, CREATE_NULL_AS_BLANK).setCellValue(usuario.getSenhaHash());
        registro.getCell(4, CREATE_NULL_AS_BLANK).setCellValue(perfil);
        registro.getCell(5, CREATE_NULL_AS_BLANK).setCellValue(usuario.isAtivo());
        registro.getCell(6, CREATE_NULL_AS_BLANK).setCellValue(usuario instanceof Vendedor vendedor
            ? vendedor.getMatricula() : "");
    }

    private byte[] lerConteudo() throws IOException {
        if (Files.notExists(arquivo, LinkOption.NOFOLLOW_LINKS)) {
            Path pasta = arquivo.getParent();
            while (pasta != null && Files.notExists(pasta)) {
                pasta = pasta.getParent();
            }
            if (pasta == null || !Files.isDirectory(pasta)) {
                throw new IOException("Diretório da base de usuários indisponível: " + arquivo.getParent());
            }
            if (baseConhecida || !Files.notExists(caminhoBackup(), LinkOption.NOFOLLOW_LINKS)) {
                throw new PersistenciaUsuariosException("Base de usuários ausente em " + arquivo
                    + ". Há indícios de uma base anterior; é necessária recuperação manual.");
            }
            if (Files.isDirectory(arquivo.getParent())) {
                String prefixo = "." + arquivo.getFileName() + "-";
                try (var arquivos = Files.newDirectoryStream(arquivo.getParent(), caminho -> {
                    String nome = caminho.getFileName().toString();
                    return nome.startsWith(prefixo) && nome.endsWith(".tmp");
                })) {
                    if (arquivos.iterator().hasNext()) {
                        throw new PersistenciaUsuariosException("Base de usuários ausente em " + arquivo
                            + ". Há temporários de uma gravação anterior; verifique a recuperação manual.");
                    }
                }
            }
            return null;
        }
        byte[] conteudo = Files.readAllBytes(arquivo);
        baseConhecida = true;
        return conteudo;
    }

    private Workbook abrir(byte[] conteudo) throws IOException {
        if (conteudo == null) {
            Workbook planilha = new XSSFWorkbook();
            PlanilhasIO.escreverTabela(planilha, ABA, new TabelaDados(COLUNAS), false);
            return planilha;
        }
        try (InputStream entrada = new ByteArrayInputStream(conteudo)) {
            return new XSSFWorkbook(entrada);
        }
    }

    List<Usuario> validarPersistencia(Workbook planilha, List<Usuario> esperados) {
        List<Usuario> recuperados = lerUsuarios(planilha);
        if (recuperados.size() != esperados.size()) {
            throw new PersistenciaUsuariosException("Quantidade de usuários divergente no temporário de " + arquivo);
        }
        for (int indice = 0; indice < esperados.size(); indice++) {
            Usuario esperado = esperados.get(indice);
            Usuario recuperado = recuperados.get(indice);
            if (esperado.getClass() != recuperado.getClass()
                    || esperado.getId() != recuperado.getId()
                    || !esperado.getNome().equals(recuperado.getNome())
                    || !esperado.getLogin().equals(recuperado.getLogin())
                    || !esperado.getSenhaHash().equals(recuperado.getSenhaHash())
                    || esperado.isAtivo() != recuperado.isAtivo()
                    || (esperado instanceof Vendedor vendedor
                        && !vendedor.getMatricula().equals(((Vendedor) recuperado).getMatricula()))) {
                throw new PersistenciaUsuariosException("Dados de usuários divergentes no temporário de " + arquivo);
            }
        }
        return List.copyOf(recuperados);
    }

    private Path caminhoBackup() {
        return arquivo.resolveSibling(arquivo.getFileName() + ".bak");
    }

    private PersistenciaUsuariosException falhaDeLeitura(Exception causa) {
        PersistenciaUsuariosException original = falha("ler", causa);
        Path backup = caminhoBackup();
        if (Files.notExists(backup, LinkOption.NOFOLLOW_LINKS)) {
            return original;
        }
        try (InputStream entrada = Files.newInputStream(backup);
             Workbook planilha = new XSSFWorkbook(entrada)) {
            lerUsuarios(planilha);
            return new PersistenciaUsuariosException(original.getMessage()
                + " Backup validado em " + backup
                + "; avalie a recuperação manual após diagnosticar a falha. Nenhuma restauração automática foi realizada.", original);
        } catch (IOException | RuntimeException e) {
            PersistenciaUsuariosException falha = new PersistenciaUsuariosException(original.getMessage()
                + " O backup em " + backup + " também está inválido ou indisponível.", original);
            falha.addSuppressed(e);
            return falha;
        }
    }

    private List<Usuario> lerUsuarios(Workbook planilha) {
        Sheet aba = planilha.getSheet(ABA);
        if (aba == null) {
            throw registroInvalido(1, "aba USUARIOS ausente");
        }
        Row cabecalho = aba.getRow(0);
        if (cabecalho == null || cabecalho.getLastCellNum() != COLUNAS.size()) {
            throw registroInvalido(1, "cabeçalho incompatível");
        }
        for (int coluna = 0; coluna < COLUNAS.size(); coluna++) {
            if (!COLUNAS.get(coluna).equals(lerTexto(cabecalho, coluna))) {
                throw registroInvalido(1, "coluna esperada: " + COLUNAS.get(coluna));
            }
        }

        List<Usuario> usuarios = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        for (int numero = 1; numero <= aba.getLastRowNum(); numero++) {
            Row linha = aba.getRow(numero);
            if (linha == null || linhaVazia(linha)) {
                continue;
            }
            if (linha.getLastCellNum() > COLUNAS.size()) {
                throw registroInvalido(numero + 1, "colunas extras no registro");
            }
            try {
                int id = lerId(linha);
                String nome = lerTexto(linha, 1);
                String login = lerTexto(linha, 2);
                String senhaHash = lerTexto(linha, 3);
                String perfil = lerTexto(linha, 4);
                boolean ativo = lerAtivo(linha);
                String matricula = lerTexto(linha, 6);
                if (!ids.add(id)) {
                    throw registroInvalido(numero + 1, "ID duplicado");
                }
                Usuario usuario = switch (perfil) {
                    case "VENDEDOR" -> new Vendedor(id, nome, login, senhaHash, ativo, matricula);
                    case "APOIO" -> new Apoio(id, nome, login, senhaHash, ativo);
                    case "ADMINISTRADOR" -> new Administrador(id, nome, login, senhaHash, ativo);
                    default -> throw registroInvalido(numero + 1, "perfil desconhecido");
                };
                if (!"VENDEDOR".equals(perfil) && !matricula.isEmpty()) {
                    throw registroInvalido(numero + 1, "matrícula só se aplica ao vendedor");
                }
                usuarios.add(usuario);
            } catch (IllegalArgumentException e) {
                throw new PersistenciaUsuariosException(
                    "Registro inválido em " + arquivo + ", aba USUARIOS, linha " + (numero + 1)
                        + ": " + e.getMessage(), e);
            }
        }
        validarLogins(usuarios);
        return usuarios;
    }

    private String lerTexto(Row linha, int coluna) {
        Cell celula = linha.getCell(coluna);
        if (celula == null || celula.getCellType() == CellType.BLANK) {
            return "";
        }
        if (celula.getCellType() != CellType.STRING) {
            throw registroInvalido(linha.getRowNum() + 1,
                COLUNAS.get(coluna) + " deve ser texto, sem fórmula");
        }
        String texto = celula.getStringCellValue();
        if (!texto.equals(texto.trim())) {
            throw registroInvalido(linha.getRowNum() + 1,
                COLUNAS.get(coluna) + " contém espaços externos");
        }
        return texto;
    }

    private int lerId(Row linha) {
        Cell celula = linha.getCell(0);
        if (celula == null || celula.getCellType() != CellType.NUMERIC) {
            throw registroInvalido(linha.getRowNum() + 1, "ID deve ser inteiro positivo numérico");
        }
        double valor = celula.getNumericCellValue();
        if (!Double.isFinite(valor) || valor < 1 || valor > Integer.MAX_VALUE || valor != Math.rint(valor)) {
            throw registroInvalido(linha.getRowNum() + 1, "ID fora do intervalo de inteiros positivos");
        }
        return (int) valor;
    }

    private boolean lerAtivo(Row linha) {
        Cell celula = linha.getCell(5);
        if (celula == null || celula.getCellType() != CellType.BOOLEAN) {
            throw registroInvalido(linha.getRowNum() + 1, "ATIVO deve ser booleano");
        }
        return celula.getBooleanCellValue();
    }

    private boolean linhaVazia(Row linha) {
        for (Cell celula : linha) {
            if (celula.getCellType() != CellType.BLANK
                    && !(celula.getCellType() == CellType.STRING && celula.getStringCellValue().isEmpty())) {
                return false;
            }
        }
        return true;
    }

    private String perfilDoUsuario(Usuario usuario) {
        if (usuario.getClass() == Vendedor.class) {
            return "VENDEDOR";
        }
        if (usuario.getClass() == Apoio.class) {
            return "APOIO";
        }
        if (usuario.getClass() == Administrador.class) {
            return "ADMINISTRADOR";
        }
        throw new IllegalArgumentException("Perfil de usuário não suportado pela persistência.");
    }

    private void validarLogins(List<Usuario> usuarios) {
        Set<String> logins = new HashSet<>();
        for (Usuario usuario : usuarios) {
            if (!logins.add(usuario.getLogin().toLowerCase(Locale.ROOT))) {
                throw new PersistenciaUsuariosException("Login duplicado na aba USUARIOS de " + arquivo
                    + ", registro com ID " + usuario.getId());
            }
        }
    }

    private PersistenciaUsuariosException registroInvalido(int linha, String motivo) {
        return new PersistenciaUsuariosException(
            "Registro inválido em " + arquivo + ", aba USUARIOS, linha " + linha + ": " + motivo);
    }

    private PersistenciaUsuariosException falha(String operacao, Exception causa) {
        if (causa instanceof PersistenciaUsuariosException falha) {
            return falha;
        }
        return new PersistenciaUsuariosException(
            "Não foi possível " + operacao + " a base de usuários em " + arquivo + ".", causa);
    }
}
