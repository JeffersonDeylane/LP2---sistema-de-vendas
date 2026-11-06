
public final class Validador {
    //podemos utilizar os métodos de Validador, mas não podemos criar uma classe filha dela usando extends.

    // Impede a criação de objetos Validador
    private Validador() {
    }

    // Validação de textos obrigatórios
    public static String textoObrigatorio(
            String valor, String campo) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                "O campo " + campo + " é obrigatório."
            );
        }

        return valor.trim();
    }

    // Validação de números inteiros positivos
    public static int inteiroPositivo(
            int valor, String campo) {

        if (valor <= 0) {
            throw new IllegalArgumentException(
                "O campo " + campo + " deve ser positivo."
            );
        }

        return valor;
    }
}
