import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash e salt ficam juntos no campo SENHA_HASH, sem alterar o Excel. */
public final class Senhas {
    private static final int ITERACOES = 600_000;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Senhas() { }

    public static String gerarHash(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }
        byte[] salt = new byte[16];
        ALEATORIO.nextBytes(salt);
        return "pbkdf2-sha256$" + ITERACOES + "$"
            + Base64.getEncoder().encodeToString(salt) + "$"
            + Base64.getEncoder().encodeToString(derivar(senha, salt));
    }

    public static boolean verificar(String senha, String hashSalvo) {
        if (senha == null || senha.isBlank() || hashSalvo == null) return false;
        try {
            String[] partes = hashSalvo.split("\\$", -1);
            if (partes.length != 4 || !partes[0].equals("pbkdf2-sha256")
                    || !partes[1].equals(Integer.toString(ITERACOES))) return false;
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return salt.length == 16 && esperado.length == 32
                && MessageDigest.isEqual(esperado, derivar(senha, salt));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(String senha, byte[] salt) {
        char[] caracteres = senha.toCharArray();
        PBEKeySpec especificacao = new PBEKeySpec(caracteres, salt, ITERACOES, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(especificacao).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível processar a senha.", e);
        } finally {
            especificacao.clearPassword();
            Arrays.fill(caracteres, '\0');
        }
    }
}
