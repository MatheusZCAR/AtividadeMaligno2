public class Resposta extends Comunicado {
    private static final long serialVersionUID = 1L;

    private final byte[] numerosOrdenados;

    public Resposta(byte[] numerosOrdenados) {
        if (numerosOrdenados == null) {
            throw new IllegalArgumentException("Vetor ordenado não pode ser nulo");
        }
        this.numerosOrdenados = numerosOrdenados;
    }

    public byte[] getVetor() {
        return numerosOrdenados;
    }
}
