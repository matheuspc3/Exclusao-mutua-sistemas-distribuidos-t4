import java.util.concurrent.BlockingQueue;

public class ThreadInterface extends Thread {

    private BlockingQueue<String> filaPedidos;
    private volatile boolean encerrar;

    public ThreadInterface(BlockingQueue<String> filaPedidos) {
        this.filaPedidos = filaPedidos;
        this.encerrar = false;
    }

    @Override
    public void run() {
        while (!encerrar) {
            // Aguardar comandos do terminal
            // Processar os comandos conforme especificado (1, 2, 3)
        }
    }

    public void encerrarExecucao() {
        encerrar = true;
    }
}
