

import java.io.*;
import java.net.*;

/**
 * This class is responsible for reading the clients input and printing it onto the console.
 *
 * @author Muhammad Masum Miah
 * version 1.0
 * <p>
 * adapted from @author www.codejava.net
 */
public class Reader extends Thread {

    private Socket socket;
    private TCPClient client;
    private BufferedReader bufferedReader;

    public Reader(Socket socket, TCPClient client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream in = socket.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(in));
        } catch (IOException e) {
            System.out.println("Input error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {

            try {
                String response = bufferedReader.readLine();
                System.out.println(response);

                // prints the username after displaying the server's message
                if (client.getUserName() != null) {
//                    System.out.print("[" + client.getUserName() + "]: ");
                }
            } catch (IOException e) {
                System.out.println("Error reading from server: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
    }

}