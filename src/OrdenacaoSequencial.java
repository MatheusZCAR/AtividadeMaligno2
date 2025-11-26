import java.util.Random;
import java.util.Scanner;

public class OrdenacaoSequencial {
    private static final String TAG = "[SEQ]";

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        try {
            System.out.print(TAG + " Informe o tamanho do vetor: ");
            int tamanho = teclado.nextInt();

            if (tamanho <= 0) {
                System.out.println(TAG + " Tamanho inválido.");
                return;
            }

            byte[] vetor = gerarVetorAleatorio(tamanho);

            long inicio = System.currentTimeMillis();

            Pedido pedido = new Pedido(vetor);
            pedido.ordenar();
            byte[] ordenado = pedido.getNumeros();

            long fim = System.currentTimeMillis();
            long tempo = fim - inicio;

            System.out.println(TAG + " Ordenação sequencial concluída em " + tempo + " ms.");
            System.out.println(TAG + " Primeiro elemento: " + ordenado[0]);
            System.out.println(TAG + " Último elemento: " + ordenado[ordenado.length - 1]);

        } finally {
            teclado.close();
        }
    }

    private static byte[] gerarVetorAleatorio(int tamanho) {
        Random random = new Random();
        byte[] v = new byte[tamanho];
        for (int i = 0; i < tamanho; i++) {
            v[i] = (byte) (random.nextInt(256) - 128);
        }
        return v;
    }
}
