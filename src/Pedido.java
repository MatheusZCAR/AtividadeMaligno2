public class Pedido extends Comunicado {
    private static final long serialVersionUID = 1L;

    private byte[] numeros;

    public Pedido(byte[] numeros) {
        if (numeros == null) {
            throw new IllegalArgumentException("Vetor não pode ser nulo");
        }
        this.numeros = numeros;
    }

    public byte[] getNumeros() {
        return numeros;
    }

    public void ordenar() {
        if (numeros.length <= 1) {
            return;
        }
        byte[] aux = new byte[numeros.length];
        mergeSort(numeros, aux, 0, numeros.length - 1);
    }

    private static void mergeSort(byte[] v, byte[] aux, int inicio, int fim) {
        if (inicio >= fim) {
            return;
        }

        int meio = (inicio + fim) / 2;

        mergeSort(v, aux, inicio, meio);
        mergeSort(v, aux, meio + 1, fim);
        merge(v, aux, inicio, meio, fim);
    }

    private static void merge(byte[] v, byte[] aux, int inicio, int meio, int fim) {
        for (int i = inicio; i <= fim; i++) {
            aux[i] = v[i];
        }

        int i = inicio;
        int j = meio + 1;
        int k = inicio;

        while (i <= meio && j <= fim) {
            if (aux[i] <= aux[j]) {
                v[k++] = aux[i++];
            } else {
                v[k++] = aux[j++];
            }
        }

        while (i <= meio) {
            v[k++] = aux[i++];
        }
    }
}
