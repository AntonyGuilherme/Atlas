package application.producers.network;

import configuration.Parameters;
import configuration.Connection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MessageProducer {
   public static void flood(String message, Integer agentId) {
       for (Connection connection : Parameters.EMIT_TO.get(agentId)) {
            try (Socket socket = new Socket(connection.HOST, connection.MESSAGE_PORT);
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                out.write(message);
                out.newLine();
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
