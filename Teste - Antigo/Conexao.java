import java.io.BufferedReader;
//import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Conexao {

    private boolean conectado = true;
    public static final String PERMITIR_ACESSO = "PERMITIR";
    public static final String NEGAR_ACESSO = "NAO_PERMITIR";
    private static final int PORTA = 8000;
    private static final int TAMANHO_MENSAGEM = 20; // Tamanho fixo da mensagem
    private Socket sock;
    private ServerSocket listenSocket;
	private BlockingQueue<String> filaPedidos;

	public Conexao(BlockingQueue<String> filaPedidos2) {
        this.filaPedidos = new LinkedBlockingQueue<>();
    }

	public Conexao(BlockingQueue<String> filaPedidos2) {
    }

    public BlockingQueue<String> getFilaPedidos() {
        return filaPedidos;
    }

    public void conectar(Processo coordenador) {
        System.out.println("Coordenador " + coordenador + " pronto para receber requisicoes.");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // cria um socket TCP para pedidos de conexão
                    listenSocket = new ServerSocket(PORTA);

                    // fica conectado enquanto o coordenador estiver vivo
                    while (conectado) {
                        // aguarda ate um cliente pedir por uma conexao
                        sock = listenSocket.accept();

                        // prepara um buffer para receber dados do cliente
                        InputStreamReader s = new InputStreamReader(sock.getInputStream());
                        BufferedReader rec = new BufferedReader(s);

                        // le os dados enviados pelo cliente
                        char[] buffer = new char[TAMANHO_MENSAGEM];
                        rec.read(buffer);
                        String rBuf = new String(buffer);
                        System.out.println(rBuf);

                        // coloca a resposta em um buffer e envia para o cliente
                        DataOutputStream d = new DataOutputStream(sock.getOutputStream());
                        String sBuf = "Error!\n";

                        if (coordenador.isRecursoEmUso())
                            sBuf = NEGAR_ACESSO + "\n";
                        else
                            sBuf = PERMITIR_ACESSO + "\n";

                        d.write(sBuf.getBytes("UTF-8"));
                    }
                    System.out.println("Conexao encerrada.");
                } catch (IOException e) {
                    System.out.println("Conexao encerrada.");
                }
            }
        }).start();
    }

    public String realizarRequisicao(String mensagem) {
        String rBuf = "ERROR!";
        try {
            // cria um socket TCP para conexao com localhost:PORTA
            Socket sock = new Socket("localhost", PORTA);

            // coloca os dados em um buffer e envia para o servidor
            DataOutputStream d = new DataOutputStream(sock.getOutputStream());
            d.write(mensagem.getBytes("UTF-8"));

            // prepara um buffer para receber a resposta do servidor
            InputStreamReader s = new InputStreamReader(sock.getInputStream());
            BufferedReader rec = new BufferedReader(s);

            // Crie um DataInputStream para ler mensagens recebidas do servidor
            //DataInputStream dis = new DataInputStream(sock.getInputStream());
            // Le os dados enviados pela aplicacao servidora
            char[] buffer = new char[TAMANHO_MENSAGEM];
            rec.read(buffer);
            rBuf = new String(buffer);

            sock.close();
        } catch (Exception e) {
            System.out.println("A requisicao nao foi finalizada corretamente.");
        }
        return rBuf;
    }

    public void encerrarConexao() {
        conectado = false;
        try {
            sock.close();
        } catch (IOException | NullPointerException e) {
            System.out.println("Erro ao encerrar a conexao: ");
            e.printStackTrace();
        }
        try {
            listenSocket.close();
        } catch (IOException | NullPointerException e) {
            System.out.println("Erro ao encerrar a conexao: ");
            e.printStackTrace();
        }
    }
}
