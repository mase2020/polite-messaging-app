

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * TCP server class.
 *
 * @author Muhammad Masum Miah
 * Version 1.0
 */
public class TCPServer {
    private int port;
    private Map<String, Integer> userNames = new HashMap<>();
    //used to keep track of the number of client threads.
    private Set<ClientThread> userThreads = new HashSet<>();

    public TCPServer() {
        this.port = 20111;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("The Server is open on port  " + port);

            while (true) {

                System.out.println("The server is awaiting a client ...");
                Socket socket = serverSocket.accept();
                System.out.println("A Client has connected!");

                ClientThread newUser = new ClientThread(socket, this);
                userThreads.add(newUser);
                newUser.start();

            }

        } catch (IOException e) {
            System.out.println("oops, there is an error in the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        TCPServer server = new TCPServer();
        server.start();
    }

    /**
     * Used to broadcast the messages to different user. the users need to use the correct protocol as the sender or requester.
     *
     * @param message
     * @param excludeUser
     * @param version
     */
    void broadcast(String message, ClientThread excludeUser, int version) {
        for (ClientThread u : userThreads) {
            if (u != excludeUser) {
                if (u.getVersion() == version) {
                    u.sendMessage(message);
                }
            }
        }
    }


    /**
     * Username and protocol version are stored.
     *
     * @param userName
     * @param version
     */
    void addUser(String userName, int version) {
        userNames.put(userName, version);
    }

    /**
     * retrieve the time when requested by the client.
     *
     * @return
     */
    String getTime() {
        return String.valueOf(System.currentTimeMillis() / 1000L);
    }


    /**
     * once a user disconnects, then remove the user and let other clients know.
     *
     * @param userName
     * @param aUser
     */
    void removeUser(String userName, ClientThread aUser) {
        int version = userNames.get(userName);
        boolean removed = userNames.remove(userName, version);
        if (removed) {
            userThreads.remove(aUser);
            System.out.println("The user " + userName + " has quit");
        }
    }

    /**
     * @return
     */
    public Map<String, Integer> getUserNames() {
        return userNames;
    }


    /**
     * Checks to see if other clients are active.
     *
     * @return
     */
    boolean hasUsers() {
        return !this.userNames.isEmpty();
    }


}