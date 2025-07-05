import application.Agent;
import application.producers.network.RangesProducer;
import configuration.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new RuntimeException("Usage: java -jar atlas {agent_id} {host_home}");
        }

        final int agentId = Integer.parseInt(args[0]);
        final String host = args[1];

        Agent agent = new Agent(agentId);
        agent.prepareConsumers();

        sendStartConfirmation(agentId, host);

        try (ServerSocket serverSocket = new ServerSocket(8100+agentId)) {
            try(Socket socket = serverSocket.accept()){
                try(BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String go = buffer.readLine();
                    if (go.equals("go"))
                        agent.mapAndShuffleFiles(2);
                }
            }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

    }

        private static void sendStartConfirmation(int agentId, String host) {
        final int port = 8000 + agentId;

        // Use a try-with-resources statement to ensure the socket and output stream are closed automatically.
        try (Socket clientSocket = new Socket(host, port);
             OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            System.out.println("Successfully connected to the server.");

            // 4. Write the message bytes to the output stream.
            outputStream.write("start".getBytes());
            // Ensure all buffered data is sent immediately.
            outputStream.flush();

            System.out.println("Message sent to server");
            System.out.println("Client finished sending data.");

        } catch (UnknownHostException e) {
            // This exception occurs if the hostname cannot be resolved.
            System.err.println("Error: Server host unknown - " + host);
            System.err.println("Please check the server hostname or IP address.");
            e.printStackTrace();
        } catch (IOException e) {
            // This exception covers various network-related issues (e.g., connection refused,
            // server not reachable, problems with reading/writing streams).
            System.err.println("Error: Could not connect to or communicate with server.");
            System.err.println("Details: " + e.getMessage());
            System.err.println("Please ensure the server is running and the host/port are correct.");
            e.printStackTrace();
        } catch (SecurityException e) {
            // This can occur if a security manager exists and denies the connection.
            System.err.println("Error: Security exception during socket operation.");
            e.printStackTrace();
        }
    }
}
