import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final AtomicInteger agentsReady = new AtomicInteger(0);
    private static final AtomicInteger listenersReady = new AtomicInteger(0);

    public static void main(String[] args) {
        final String[] HOSTS = new String[args.length];
        System.arraycopy(args, 0, HOSTS, 0, args.length);

        final int NUMBER_OF_AGENTS = HOSTS.length;
        for (int i = 0; i < NUMBER_OF_AGENTS; i++)
            new Thread(Main::handle).start();

        System.out.println("Awaiting for connections");
        while (agentsReady.get() < NUMBER_OF_AGENTS);

        System.out.println(agentsReady.get() + " agents ready");

        for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
            final int port = 8100 + i;
            // Use a try-with-resources statement to ensure the socket and output stream are closed automatically.
            try (Socket clientSocket = new Socket(HOSTS[i], port);
                 OutputStream outputStream = clientSocket.getOutputStream()
            ) {
                System.out.println("Successfully connected to the client.");

                // 4. Write the message bytes to the output stream.
                outputStream.write("go".getBytes());
                // Ensure all buffered data is sent immediately.
                outputStream.flush();

            } catch (Exception e) {
                System.err.println("Error: Server host unknown - " + e.getMessage());
            }
        }
    }

    private static void handle() {
        try (ServerSocket serverSocket = new ServerSocket(8000+listenersReady.getAndIncrement())) {

            try(Socket socket = serverSocket.accept()){

                try(BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String message = buffer.readLine();

                    if (message.equals("start"))
                        agentsReady.incrementAndGet();

                }
            } catch (IOException e) {
                        // network timeout
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
