
public class Vendedor extends Usuario {

    
    private String matricula; //só do vendedor

    // Construtor
    public Vendedor(int id, String nome, String login,
                    String senhaHash, boolean ativo,
                    String matricula) {

        // Chama o construtor da classe Usuario
        super(id, nome, login, senhaHash, ativo);

        setMatricula(matricula);
    }

    // Getter
    public String getMatricula() {
        return matricula;
    }

    // Setter com validação
    public void setMatricula(String matricula) {
        this.matricula = Validador.textoObrigatorio(
            matricula, "Matrícula"
        );
    }

    
    @Override
    public boolean podeAprovarAlteracoes() {
        return false;
    }
   
    @Override
    public boolean podeGerenciarUsuarios() {
        return false;
    }
}
