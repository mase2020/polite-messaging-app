

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

/**
 * This thread will read the clients input and relay it to the server
 * The loop will terminate once the client types "bye!".
 *
 * @author Muhammad Masum Miah
 * <p>
 * Adapted from author www.codejava.net
 */

public class Writer extends Thread {

    private Socket socket;
    private TCPClient client;
    private PrintWriter printWriter;

    public Writer(Socket socket, TCPClient client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream out = socket.getOutputStream();
            printWriter = new PrintWriter(out, true);
        } catch (IOException e) {
            System.out.println("Output stream error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run() {
        Console console = System.console();

        String protocolCommand = console.readLine("\nEnter 'Protocol?' version username \n");
        String protocolResponses[] = protocolCommand.split(" ");
        int protocol = Integer.parseInt(protocolResponses[1]);
        String userName = protocolResponses[2];


        client.setUserName(userName);
        client.setProtocol(protocol);
        printWriter.println(protocolCommand);


        String str;


        do {

            str = console.readLine();
            printWriter.println(str);
            if (str.equalsIgnoreCase("create?")) {
                List<String> messages = new LinkedList<>();
                String from = console.readLine("Enter sender: \n");
                String to = console.readLine("Enter recipient: \n");
                String topic = console.readLine("Enter topic: \n");
                String subject = console.readLine("Enter subject: \n");
                int contents = Integer.parseInt(console.readLine("Enter contents: \n"));
                printWriter.println(from);
                printWriter.println(to);
                printWriter.println(topic);
                printWriter.println(subject);
                printWriter.println(contents);
                int end = 0;
                while (end < contents) {
                    String msg = console.readLine("Enter message: \n");
                    messages.add(msg);
                    printWriter.println(msg);
                    end++;
                }
            }
        }

        //close the socket if message is "bye".
        while (!str.equalsIgnoreCase("bye!"));

        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Could not write to the server: " + e.getMessage());
        }

    }
}
