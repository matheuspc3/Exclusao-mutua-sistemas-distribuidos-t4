package teste;

import java.net.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class Process {
    private static final int COORDINATOR_PORT = 12345;
    private static final int F = 256; // Tamanho fixo da mensagem
    private static final int k = 1; // Tempo de espera em segundos
    private static final int r = 10; // Número de repetições

    public static void main(String[] args) {
        try {
            // Conecta-se ao coordenador
            DatagramSocket socket = new DatagramSocket();
            InetAddress coordinatorAddress = InetAddress.getByName("localhost");

            for (int i = 0; i < r; i++) {
                // Envia REQUEST ao coordenador
                String requestMessage = "REQUEST#" + i;
                byte[] sendData = requestMessage.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, coordinatorAddress, COORDINATOR_PORT);
                socket.send(sendPacket);

                // Log
                System.out.println("Sent REQUEST to Coordinator");

                // Aguarda GRANT do coordenador
                byte[] receiveData = new byte[F];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                String grantMessage = new String(receivePacket.getData()).trim();
                System.out.println("Received " + grantMessage);

                // Acesso à região crítica
                writeToFile("resultado.txt", "Process ID: " + i + ", Timestamp: " + System.currentTimeMillis());

                // Envia RELEASE ao coordenador
                String releaseMessage = "RELEASE#" + i;
                sendData = releaseMessage.getBytes();
                sendPacket = new DatagramPacket(sendData, sendData.length, coordinatorAddress, COORDINATOR_PORT);
                socket.send(sendPacket);

                // Log
                System.out.println("Sent RELEASE to Coordinator");

                // Espera k segundos
                TimeUnit.SECONDS.sleep(k);
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(String filename, String content) {
        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(content + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
