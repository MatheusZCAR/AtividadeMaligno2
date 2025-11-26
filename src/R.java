import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class R {
    private static final int PORTA = 12345;
    private static final String TAG = "[R]";

    private static final int NUM_PROCESSADORES =
            Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        System.out.println(TAG + " Servidor iniciado na porta " + PORTA);
        System.out.println(TAG + " Processadores disponíveis: " + NUM_PROCESSADORES);

        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            while (true) {
                Socket conexao = servidor.accept();
                System.out.println(TAG + " Nova conexão de " + conexao.getInetAddress());

                Thread t = new Thread(() -> atenderCliente(conexao));
                t.start();
            }
        } catch (IOException e) {
            System.err.println(TAG + " Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void atenderCliente(Socket conexao) {
        String tagLocal = TAG + " [" + conexao.getInetAddress() + "]";

        try (Socket socket = conexao;
             ObjectOutputStream transmissor =
                     new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream receptor =
                     new ObjectInputStream(socket.getInputStream())) {

            while (true) {
                Object obj = receptor.readObject();

                if (obj instanceof ComunicadoEncerramento) {
                    System.out.println(tagLocal + " ComunicadoEncerramento recebido. Encerrando conexão.");
                    break;
                }

                if (!(obj instanceof Pedido)) {
                    System.out.println(tagLocal + " Objeto inesperado: " + obj.getClass().getName());
                    continue;
                }

                Pedido pedido = (Pedido) obj;
                byte[] numeros = pedido.getNumeros();
                System.out.println(tagLocal + " Pedido recebido. Tamanho do vetor: " + numeros.length);

                long inicio = System.currentTimeMillis();

                byte[] ordenado = ordenarParalelo(numeros);

                long fim = System.currentTimeMillis();
                long tempo = fim - inicio;

                System.out.println(tagLocal + " Ordenação concluída em " + tempo + " ms.");

                Resposta resposta = new Resposta(ordenado);
                transmissor.writeObject(resposta);
                transmissor.flush();

                System.out.println(tagLocal + " Resposta enviada ao cliente.");
            }

        } catch (IOException e) {
            System.err.println(tagLocal + " Erro de E/S: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println(tagLocal + " Classe desconhecida recebida: " + e.getMessage());
        }
    }

    private static byte[] ordenarParalelo(byte[] numeros) {
        int n = numeros.length;

        if (n <= 1 || NUM_PROCESSADORES <= 1) {
            Pedido p = new Pedido(numeros.clone());
            p.ordenar();
            return p.getNumeros();
        }

        int partes = Math.min(NUM_PROCESSADORES, n);
        byte[][] segmentos = new byte[partes][];

        int tamanhoBase = n / partes;
        int resto = n % partes;

        int inicio = 0;
        for (int i = 0; i < partes; i++) {
            int tamanhoParte = tamanhoBase + (i < resto ? 1 : 0);
            int fim = inicio + tamanhoParte;

            segmentos[i] = new byte[tamanhoParte];
            System.arraycopy(numeros, inicio, segmentos[i], 0, tamanhoParte);

            inicio = fim;
        }

        Thread[] threads = new Thread[partes];

        for (int i = 0; i < partes; i++) {
            final int indice = i;
            threads[i] = new Thread(() -> {
                Pedido p = new Pedido(segmentos[indice]);
                p.ordenar();
            });
            threads[i].start();
        }

        for (int i = 0; i < partes; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                System.err.println(TAG + " Thread ordenadora interrompida: " + e.getMessage());
            }
        }

        byte[] resultado = mergeTodos(segmentos);

        return resultado;
    }

    private static byte[] mergeTodos(byte[][] partes) {
        if (partes.length == 0) {
            return new byte[0];
        }
        if (partes.length == 1) {
            return partes[0];
        }

        byte[] atual = partes[0];
        for (int i = 1; i < partes.length; i++) {
            atual = mergeDois(atual, partes[i]);
        }
        return atual;
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
}
