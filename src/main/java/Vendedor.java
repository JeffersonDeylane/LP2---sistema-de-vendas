
/** Perfil responsável por registrar vendas e solicitar alterações e reativações. */
public class Vendedor extends Usuario {
    public void solicitarAlteracao(SolicitacaoAlteracao solicitacao) {
        throw new UnsupportedOperationException("Implementação pendente");
    }

    public void solicitarReativacao(GrupoCancelamento grupo) {
        throw new UnsupportedOperationException("Implementação pendente");
    }

    
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
