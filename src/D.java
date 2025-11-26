import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class D {
    private static final String TAG = "[D]";

    private static final String[] IPS_SERVIDORES = {
            "127.0.0.1",
            "127.0.0.1",
            "127.0.0.1"
    };

    private static final int PORTA_SERVIDORES = 12345;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        try {
            System.out.print(TAG + " Informe o tamanho do vetor: ");
            int tamanho = teclado.nextInt();

            if (tamanho <= 0) {
                System.out.println(TAG + " Tamanho inválido.");
                return;
            }

            byte[] vetorGrande = gerarVetorAleatorio(tamanho);

            long inicio = System.currentTimeMillis();

            List<byte[]> partesOrdenadas = ordenarDistribuido(vetorGrande);

            byte[] vetorFinalOrdenado = mergeTodosParalelo(partesOrdenadas);

            long fim = System.currentTimeMillis();
            long tempo = fim - inicio;

            System.out.println(TAG + " Ordenação distribuída concluída em " + tempo + " ms.");

            System.out.print(TAG + " Nome do arquivo texto para salvar o vetor ordenado: ");
            String nomeArquivo = teclado.next();

            salvarEmArquivo(vetorFinalOrdenado, nomeArquivo);
            System.out.println(TAG + " Vetor ordenado salvo em " + nomeArquivo);

            enviarEncerramentoParaTodos();

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

    private static List<byte[]> ordenarDistribuido(byte[] vetorGrande) {
        int numServidores = IPS_SERVIDORES.length;
        List<byte[]> partesOrdenadas = new ArrayList<>();

        Thread[] threads = new Thread[numServidores];
        byte[][] resultados = new byte[numServidores][];

        int tamanhoBase = vetorGrande.length / numServidores;
        int resto = vetorGrande.length % numServidores;

        int inicio = 0;
        for (int i = 0; i < numServidores; i++) {
            int tamanhoParte = tamanhoBase + (i < resto ? 1 : 0);
            int fim = inicio + tamanhoParte;

            byte[] subvetor = new byte[tamanhoParte];
            System.arraycopy(vetorGrande, inicio, subvetor, 0, tamanhoParte);

            final int indice = i;
            final String ip = IPS_SERVIDORES[i];

            threads[i] = new Thread(() -> {
                resultados[indice] = enviarPedidoEReceberResposta(ip, subvetor);
            });

            threads[i].start();
            inicio = fim;
        }

        for (int i = 0; i < numServidores; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                System.err.println(TAG + " Thread cliente interrompida: " + e.getMessage());
            }
        }

        for (byte[] r : resultados) {
            if (r != null) {
                partesOrdenadas.add(r);
            }
        }

        return partesOrdenadas;
    }

    private static byte[] enviarPedidoEReceberResposta(String ip, byte[] subvetor) {
        String tagLocal = TAG + " [" + ip + "]";
        try (Socket conexao = new Socket(ip, PORTA_SERVIDORES);
             ObjectOutputStream transmissor =
                     new ObjectOutputStream(conexao.getOutputStream());
             ObjectInputStream receptor =
                     new ObjectInputStream(conexao.getInputStream())) {

            System.out.println(tagLocal + " Enviando Pedido. Tamanho: " + subvetor.length);
            Pedido pedido = new Pedido(subvetor);
            transmissor.writeObject(pedido);
            transmissor.flush();

            Object obj = receptor.readObject();
            if (!(obj instanceof Resposta)) {
                System.err.println(tagLocal + " Objeto inesperado recebido: " + obj.getClass().getName());
                return null;
            }

            Resposta resposta = (Resposta) obj;
            byte[] ordenado = resposta.getVetor();
            System.out.println(tagLocal + " Resposta recebida. Tamanho: " + ordenado.length);

            return ordenado;

        } catch (Exception e) {
            System.err.println(tagLocal + " Erro ao comunicar com servidor: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] mergeTodosParalelo(List<byte[]> partesOrdenadas) {
        if (partesOrdenadas.isEmpty()) {
            return new byte[0];
        }
        if (partesOrdenadas.size() == 1) {
            return partesOrdenadas.get(0);
        }

        List<byte[]> atual = new ArrayList<>(partesOrdenadas);

        while (atual.size() > 1) {
            int pares = atual.size() / 2;
            byte[][] resultados = new byte[pares][];
            Thread[] threads = new Thread[pares];

            for (int p = 0; p < pares; p++) {
                final int indice = p;
                final byte[] a = atual.get(2 * p);
                final byte[] b = atual.get(2 * p + 1);

                threads[p] = new Thread(() -> {
                    resultados[indice] = mergeDois(a, b);
                });
                threads[p].start();
            }

            for (int p = 0; p < pares; p++) {
                try {
                    threads[p].join();
                } catch (InterruptedException e) {
                    System.err.println(TAG + " Thread juntadora interrompida: " + e.getMessage());
                }
            }

            List<byte[]> proximo = new ArrayList<>();
            for (int p = 0; p < pares; p++) {
                proximo.add(resultados[p]);
            }

            if (atual.size() % 2 == 1) {
                proximo.add(atual.get(atual.size() - 1));
            }

            atual = proximo;
        }

        return atual.get(0);
    }

    private static byte[] mergeDois(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];

        int i = 0;
        int j = 0;
        int k = 0;

        while (i < a.length && j < b.length) {
            if (a[i] <= b[j]) {
                c[k++] = a[i++];
            } else {
                c[k++] = b[j++];
            }
        }

        while (i < a.length) {
            c[k++] = a[i++];
        }

        while (j < b.length) {
            c[k++] = b[j++];
        }

        return c;
    }

    private static void salvarEmArquivo(byte[] vetor, String nomeArquivo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) {
            for (byte b : vetor) {
                writer.write(Byte.toString(b));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println(TAG + " Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    private static void enviarEncerramentoParaTodos() {
        for (String ip : IPS_SERVIDORES) {
            String tagLocal = TAG + " [" + ip + "]";
            try (Socket conexao = new Socket(ip, PORTA_SERVIDORES);
                 ObjectOutputStream transmissor =
                         new ObjectOutputStream(conexao.getOutputStream())) {

                transmissor.writeObject(new ComunicadoEncerramento());
                transmissor.flush();
                System.out.println(tagLocal + " ComunicadoEncerramento enviado.");
            } catch (IOException e) {
                System.err.println(tagLocal + " Erro ao enviar ComunicadoEncerramento: " + e.getMessage());
            }
        }
    }
}
