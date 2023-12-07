package teste;

import java.net.*;
import java.util.Queue;
import java.util.LinkedList;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Coordinator {
    private static final int PORT = 12345;
    private static final int F = 256; // Tamanho fixo da mensagem
    private static final Queue<RequestMessage> requestQueue = new LinkedList<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public static void main(String[] args) {
        // Thread para receber conexões
        new Thread(() -> receiveConnections()).start();

        // Thread de interface
        new Thread(() -> processCommands()).start();
    }

    private static void receiveConnections() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(PORT);

            while (true) {
                byte[] receiveData = new byte[F];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String message = new String(receivePacket.getData()).trim();
                int processId = Integer.parseInt(message.split("#")[1]);

                // Adiciona à fila de pedidos
                synchronized (requestQueue) {
                    requestQueue.add(new RequestMessage("REQUEST", processId));
                }

                // Log
                log("Received REQUEST from Process " + processId);

                // Envia GRANT de volta para o processo
                sendGrant(processId, receivePacket.getAddress(), receivePacket.getPort());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendGrant(int processId, InetAddress address, int port) {
        try {
            DatagramSocket socket = new DatagramSocket();

            String grantMessage = "GRANT#" + processId;
            byte[] sendData = grantMessage.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            socket.send(sendPacket);

            // Log
            log("Sent GRANT to Process " + processId);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processCommands() {
        while (true) {
            // Aguarda comandos do terminal
            String command = System.console().readLine("Enter command (printQueue, printCount, exit): ");

            switch (command) {
                case "printQueue":
                    printQueue();
                    break;
                case "printCount":
                    printCount();
                    break;
                case "exit":
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid command. Try again.");
            }
        }
    }

    private static void printQueue() {
        // Imprime a fila de pedidos
        synchronized (requestQueue) {
            System.out.println("Current Request Queue:");
            for (RequestMessage request : requestQueue) {
                System.out.println("Type: " + request.getType() + ", ProcessID: " + request.getProcessId());
            }
        }
    }

    private static void printCount() {
        // Imprime quantas vezes cada processo foi atendido
        // (Implemente conforme necessário)
        System.out.println("Print Count: Not implemented yet.");
    }

    private static void log(String message) {
        // Gera um log com todas as mensagens recebidas e enviadas
        String logMessage = dateFormat.format(new Date()) + " - " + message;
        System.out.println(logMessage);
    }
}