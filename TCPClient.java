

import java.net.*;
import java.io.*;


/**
 * This is the TCP client class.
 * @author Muhammad Masum Miah
 * Version 1.0
 */
public class TCPClient {
    private String host;
    private int port;
    private String userName;
    private int protocol;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void execute() {
        try {
            Socket socket = new Socket(host, port);

            System.out.println("The client has successfully connected to the server.");

            new Reader(socket, this).start();
            new Writer(socket, this).start();

        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    void setProtocol(int version) {
        this.protocol = version;
    }

    String getUserName() {
        return this.userName;
    }

    public int getProtocol() {
        return protocol;
    }

    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        TCPClient client = new TCPClient(hostname,port);
        client.execute();
    }
}