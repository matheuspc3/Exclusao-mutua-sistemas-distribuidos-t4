import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    private static final int ADICIONA = 4000;
    private static final int INATIVO_PROCESSO = 8000;
    private static final int INATIVO_COORDENADOR = 30000;
    private static final int CONSOME_RECURSO_MIN = 5000;
    private static final int CONSOME_RECURSO_MAX = 10000;

    public static void main(String[] args) {
        BlockingQueue<String> filaPedidos = new LinkedBlockingQueue<>();
        Conexao conexao = new Conexao(filaPedidos);
        ThreadInterface threadInterface = new ThreadInterface(filaPedidos);

        // Iniciar a thread de interface
        threadInterface.start();

        criarProcessos(ControladorDeProcessos.getProcessosAtivos(), conexao);
        inativarCoordenador(ControladorDeProcessos.getProcessosAtivos());
        inativarProcesso(ControladorDeProcessos.getProcessosAtivos());
        acessarRecurso(ControladorDeProcessos.getProcessosAtivos());
    }

    public static void criarProcessos(ArrayList<Processo> processosAtivos, Conexao conexao) {
        new Thread(() -> {
            while (true) {
                synchronized (lock) {
                    Processo processo = new Processo(gerarIdUnico(processosAtivos), conexao);

                    if (processosAtivos.isEmpty())
                        processo.setEhCoordenador(true);

                    processosAtivos.add(processo);
                }

                esperar(ADICIONA);
            }
        }).start();
    }

    private static int gerarIdUnico(ArrayList<Processo> processosAtivos) {
        Random random = new Random();
        int idRandom = random.nextInt(1000);

        for (Processo p : processosAtivos) {
            if (p.getPid() == idRandom)
                return gerarIdUnico(processosAtivos);
        }

        return idRandom;
    }

    public static void inativarProcesso(ArrayList<Processo> processosAtivos) {
        new Thread(() -> {
            while (true) {
                esperar(INATIVO_PROCESSO);

                synchronized (lock) {
                    if (!processosAtivos.isEmpty()) {
                        int indexProcessoAleatorio = new Random().nextInt(processosAtivos.size());
                        Processo pRemover = processosAtivos.get(indexProcessoAleatorio);
                        if (pRemover != null && !pRemover.isCoordenador())
                            pRemover.destruir();
                    }
                }
            }
        }).start();
    }

    public static void inativarCoordenador(ArrayList<Processo> processosAtivos) {
        new Thread(() -> {
            while (true) {
                esperar(INATIVO_COORDENADOR);

                synchronized (lock) {
                    Processo coordenador = null;
                    for (Processo p : processosAtivos) {
                        if (p.isCoordenador())
                            coordenador = p;
                    }
                    if (coordenador != null) {
                        coordenador.destruir();
                        System.out.println("Processo coordenador " + coordenador + " destruido.");
                    }
                }
            }
        }).start();
    }

    public static void acessarRecurso(ArrayList<Processo> processosAtivos) {
        new Thread(() -> {
            Random random = new Random();
            int intervalo = 0;
            while (true) {
                intervalo = random.nextInt(CONSOME_RECURSO_MAX - CONSOME_RECURSO_MIN);
                esperar(CONSOME_RECURSO_MIN + intervalo);

                synchronized (lock) {
                    if (!processosAtivos.isEmpty()) {
                        int indexProcessoAleatorio = new Random().nextInt(processosAtivos.size());

                        Processo processoConsumidor = processosAtivos.get(indexProcessoAleatorio);
                        processoConsumidor.acessarRecursoCompartilhado();
                    }
                }
            }
        }).start();
    }

    private static void esperar(int segundos) {
        try {
            Thread.sleep(segundos);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
