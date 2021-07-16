

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;


/**
 * This class handles multiple clients at the same time.
 * In order to communicate with the other clients, they need to be on the same version.
 * If the clients are on different versions, then they will not be able to interact.
 * The thread consists of a bunch of recognised commands which the peer responds to by using a database.
 * Create, delete, retrieve messages from a database.
 *
 * @author Muhammad Masum Miah
 * Version 1.0
 */
public class ClientThread extends Thread {
    private Socket socket;
    private TCPServer server;
    private PrintWriter writer;
    private int version;
    List<String> headers = getHeaders();

    public ClientThread(Socket socket, TCPServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            OutputStream out = socket.getOutputStream();
            writer = new PrintWriter(out, true);

            printUsers();
            // Collects the username and protocol number from the client.
            String protocolCommand = reader.readLine();
            String protocolResponses[] = protocolCommand.split(" ");
            version = Integer.parseInt(protocolResponses[1]);
            String userName = protocolResponses[2];

            //Server adds the client to it's records.
            server.addUser(userName, version);
            //Let all current clients who are on the same protocol number  know about the new client joining
            String serverMessage = "New user connected: " + userName;
            server.broadcast(serverMessage, this, version);

            String clientMessage;

            do {
                clientMessage = reader.readLine();
                serverMessage = "[" + userName + "]: " ;
                server.broadcast(serverMessage, this, version);

                /**
                 * The client types delete? followed by 'time' and then a unix time.
                 * 3 words in total with a space in between.
                 * This will delete all messages in the database from that time and before '<='.
                 */
                if (clientMessage.toLowerCase().startsWith("delete?")) {
                    String[] listSplit = clientMessage.split(" ");
                    String header = listSplit[1];
                    String data = listSplit[2];
                    if (header.equalsIgnoreCase("time")) {
                        long time = Long.parseLong(data);
                        DBDriver.deleteTime(time);
                        server.broadcast("deleted successfully", this, version);
                        /**
                         * The client types delete? followed by 'contents' and then a number.
                         * 3 words in total with a space in between.
                         * This will delete all messages from the database which have amount of contents or more'>='.
                         */
                    } else if (header.equalsIgnoreCase("contents")) {
                        int contents = Integer.parseInt(data);
                        DBDriver.deleteContents(contents);
                        server.broadcast("deleted successfully", this, version);
                        /**
                         * The client types delete? followed by 'header' and then the header to search for followed by the value.
                         * 4 words in total with a space in between.
                         * This will delete all messages from the database which have the stipulated value in the stipulated header column.
                         * header can refer to 'message-id','from','to','topic','subject','contents',time-sent'.
                         * example 'delete? header topic: #announcements'
                         */
                    } else if (header.equalsIgnoreCase("header")) {
                        data = deleteColon(data);
                        if (data.equalsIgnoreCase("from")) {
                            data = "sender";
                        } else if (data.equalsIgnoreCase("to")) {
                            data = "recipient";
                        } else if (data.equalsIgnoreCase("message-id")) {
                            data = "message_info_id";
                        } else if (data.equalsIgnoreCase("time-sent")) {
                            data = "time_sent";
                        }
                        String value = listSplit[3];
                        DBDriver.deleteByHeader(data, value);
                        server.broadcast("deleted successfully", this, version);
                    }
                    /**
                     * The client types 'create?' followed by enter.
                     * The console will print out the relevant requested data.
                     * After following the prompts from the console, the message will save into the database.
                     * SHA-256 is used to create an id for the message.
                     */
                } else if (clientMessage.equalsIgnoreCase("create?")) {

                    Long time_sent = System.currentTimeMillis() / 1000L;
                    String from = reader.readLine();
                    String to = reader.readLine();
                    String topic = reader.readLine();
                    String subject = reader.readLine();
                    int content = Integer.parseInt(reader.readLine());
                    List<String> messages = new LinkedList<>();
                    int end = 0;
                    while (end < content) {
                        String msg = reader.readLine();
                        messages.add(msg);
                        end++;
                    }
                    createMessage(from, to, topic, subject, time_sent, content, messages);
                    server.broadcast("Message created", this, version);
                }
                /**
                 * The client types 'time?' followed by enter.
                 * The console will print out the relevant requested data.
                 */
                else if (clientMessage.equalsIgnoreCase("time?")) {
                    server.broadcast("NOW " + server.getTime(), this, version);
                }
                /**
                 * The client types 'list' followed by a unix time and a number for the number of headers they want to check.
                 * the maximum headers to check is 4.
                 * each header will be followed by the value.
                 * The headers will be validated.
                 * If any corresponding data is found, then this will be displayed with number of messages followed by their id's.
                 * Otherwise nothing will be displayed
                 * The console will print out the relevant requested data.
                 */
                else if (clientMessage.toLowerCase().startsWith("list?")) {
                    String[] listSplit = clientMessage.split(" ");
                    String since = listSplit[1];
                    int headers = Integer.parseInt(listSplit[2]);
                    long time = Long.parseLong(since);
                    long current = System.currentTimeMillis() / 1000L;
                    if (time > current) {
                        server.broadcast("You are not allowed to go into the future, please try again with a date before: " + current, this, version);

                    } else {
//                    //convert seconds to milliseconds
//                    Date date = new Date(time * 1000L);
//                    // format of the date
//                    SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    d1.setTimeZone(TimeZone.getTimeZone("GMT-00"));
//                    String time_sent_string = d1.format(date);
//                    Timestamp time_sent = Timestamp.valueOf(time_sent_string);
                        int i = 0;
                        List<String> header = new LinkedList<>();
                        List<String> m = new LinkedList<>();
                        String msg;
                        /**
                         * header can refer to 'message-id','from','to','topic','subject','contents',time-sent'.
                         * example 'topic: #announcements'
                         */
                        while (i < headers) {
                            msg = reader.readLine();
                            String[] msgSplit = msg.split(" ");
                            String msgHeader = msgSplit[0];
                            msgHeader = deleteColon(msgHeader);
                            if (msgHeader.equalsIgnoreCase("from")) {
                                msgHeader = "sender";
                            } else if (msgHeader.equalsIgnoreCase("to")) {
                                msgHeader = "recipient";
                            } else if (msgHeader.equalsIgnoreCase("message-id")) {
                                msgHeader = "message_info_id";
                            } else if (msgHeader.equalsIgnoreCase("time-sent")) {
                                msgHeader = "time_sent";
                            }
                            if (validateHeader(msgHeader)) {
                                header.add(msgHeader);
                                m.add(msgSplit[1]);
                                i++;
                            } else {
                                i++;
                            }

                        }
                        if (header.size() == 0) {

                            List<String> retrieved = DBDriver.queryTime(time);
                            if (retrieved.size() > 0) {
                                StringBuilder reply = new StringBuilder("Messages " + retrieved.size() + "\n");
                                for (String r : retrieved) {
                                    reply.append(r + "\n");

                                }
                                server.broadcast(reply.toString(), this, version);
                            }
                        } else if (header.size() == 1) {

                            List<String> retrieved = DBDriver.queryheader_1(time, header.get(0), m.get(0));
                            if (retrieved.size() > 0) {
                                StringBuilder reply = new StringBuilder("Messages " + retrieved.size() + "\n");
                                for (String r : retrieved) {
                                    reply.append(r + "\n");

                                }
                                server.broadcast(reply.toString(), this, version);
                            }
                        } else if (header.size() == 2) {
                            List<String> retrieved = DBDriver.queryheader_2(time, header.get(0), m.get(0), header.get(1), m.get(1));
                            if (retrieved.size() > 0) {
                                StringBuilder reply = new StringBuilder("Messages " + retrieved.size() + "\n");
                                for (String r : retrieved) {
                                    reply.append(r + "\n");

                                }
                                server.broadcast(reply.toString(), this, version);
                            }
                        } else if (header.size() == 3) {
                            List<String> retrieved = DBDriver.queryheader_3(time, header.get(0), m.get(0), header.get(1), m.get(1), header.get(2), m.get(2));
                            if (retrieved.size() > 0) {
                                StringBuilder reply = new StringBuilder("Messages " + retrieved.size() + "\n");
                                for (String r : retrieved) {
                                    reply.append(r + "\n");

                                }
                                server.broadcast(reply.toString(), this, version);
                            }
                        } else if (header.size() == 4) {
                            List<String> retrieved = DBDriver.queryheader_4(time, header.get(0), m.get(0), header.get(1), m.get(1), header.get(2), m.get(2), header.get(3), m.get(3));
                            if (retrieved.size() > 0) {
                                StringBuilder reply = new StringBuilder("Messages " + retrieved.size() + "\n");
                                for (String r : retrieved) {
                                    reply.append(r + "\n");

                                }
                                server.broadcast(reply.toString(), this, version);
                            }


                        } else {
                            server.broadcast("There are no messages!", this, version);

                        }
                    }
                }
                /**
                 * The 'get? SHA-256' command is followed by a message-id.
                 * This will retrieve that message with all headers and relevant messages related to that id.
                 */
                else if (clientMessage.toLowerCase().startsWith("get?")) {
                    String[] listSplit = clientMessage.split(" ");
                    String id = listSplit[2];
                    String reply = retrieveMessage(id);

                    server.broadcast(reply, this, version);

                }

                //Will terminate the clients connection by typing 'bye'
            } while (!clientMessage.equalsIgnoreCase("bye!"));

            server.removeUser(userName, this);
            socket.close();

            serverMessage = userName + " has quit.";
            server.broadcast(serverMessage, this, version);

        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Sends a list of online users to the newly connected user.
     */
    void printUsers() {
        if (server.hasUsers()) {
            writer.println("Connected users: " + server.getUserNames());
        } else {
            writer.println("No other users connected");
        }
    }

    /**
     * Sends a message to the client.
     */
    void sendMessage(String message) {
        writer.println(message);
    }

    public int getVersion() {
        return version;
    }

    /**
     * Add headers for future versions of the implementation
     *
     * @param header
     */
    public void addHeader(String header) {
        headers.add(header);

    }

    /**
     * Returns all the headers which are available for the polite messaging app.
     */
    public List<String> getHeaders() {
        List<String> headers = new LinkedList<>();
        String from = "Sender";
        String Message_ID = "Message_info_id";
        String Time = "Time_sent";
        String Topic = "Topic";
        String Subject = "Subject";
        String Contents = "Contents";
        String recipient = "Recipient";
        headers.add(from);
        headers.add(Message_ID);
        headers.add(Time);
        headers.add(Topic);
        headers.add(Subject);
        headers.add(Contents);
        headers.add(recipient);
        return headers;
    }

    /**
     * validates a header in the 'list?' command to see if the client typed a valid header to retrieve from the database.
     * Undocumented headers will be ignored.
     *
     * @param header
     */
    public boolean validateHeader(String header) {
        List<String> getHeaders = getHeaders();
        for (String h : getHeaders) {
            if (header.equalsIgnoreCase(h)) {
                return true;
            }

        }
        return false;
    }

    /**
     * Retrieve a message from the database by using id.
     * if found, the message will display else it will display'sorry'.
     *
     * @param id
     */

    public String retrieveMessage(String id) {
        List<Message> messages = DBDriver.queryMessages(id);
        if (messages.size() > 0) {
            StringBuilder sb = new StringBuilder("Found!\nMessage-id: SHA-256 " + messages.get(0).getId() + "\n");
            sb.append("Time-sent: " + messages.get(0).getTime() + "\n");
            sb.append("From: " + messages.get(0).getFrom() + "\n");
            sb.append("Topic: " + messages.get(0).getTopic() + "\n");
            sb.append("Subject: " + messages.get(0).getSubject() + "\n");
            sb.append("Contents: " + messages.get(0).getContents() + "\n");

            for (Message m : messages) {
                sb.append(m.getMessage() + "\n");
            }
            return sb.toString();
        } else {
            return "Sorry";
        }
    }


    /**
     * Create a message and store it into the database using the SHA-256 algorithm for initialising an id.
     */
    public void createMessage(String from, String to, String topic, String subject, long time, int contents, List<String> messages) throws NoSuchAlgorithmException, SQLException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        StringBuilder sb = new StringBuilder(String.valueOf(time));
        sb.append(from);
        sb.append(to);
        sb.append(topic);
        sb.append(subject);
        sb.append(contents);
        for (String m : messages) {
            sb.append(m);
        }
        String text = sb.toString();

        // Change this to UTF-16 if needed
        md.update(text.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        String id = String.format("%064x", new BigInteger(1, digest));
        DBDriver.insertMessage_Info(id, time, from, to, topic, subject, contents);
        for (String m : messages) {
            DBDriver.insertMessage(id, m);
        }

    }

    public String deleteColon(String str) {
        if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == ':') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }
}