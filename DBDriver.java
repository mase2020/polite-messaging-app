

import java.sql.*;
import java.util.*;

/**
 * @author Muhammad Masum Miah
 * version 1.0
 * This class is the datasource  for the polite messaging app.
 * Everything which is linked to the MYSQL database is stored here.
 */

public class DBDriver {

    private static Connection conn;
    public static final String DB_NAME = "politeMessaging";
    private static final String url = "jdbc:mysql://localhost/" + DB_NAME;
    //TODO: Change username and password
    private static final String user = "root";
    private static final String password = "";

    public static Connection getConnection() {
        try {
            conn = DriverManager.getConnection(url, user, password);


        } catch (Exception e) {
            e.printStackTrace();
            e.getCause();
            System.out.println("database not available");
        }

        return conn;
    }

    //Establish DB Connection.


    //Create message info with headers table variables.
    public static final String TABLE_MESSAGES_INFO = "MESSAGES_INFO";
    public static final String COLUMN_MESSAGE_INFO_ID = "MESSAGE_INFO_ID";
    public static final String COLUMN_TIME_SENT = "TIME_SENT";
    public static final String COLUMN_FROM = "SENDER";
    public static final String COLUMN_TO = "RECIPIENT";
    public static final String COLUMN_TOPIC = "TOPIC";
    public static final String COLUMN_SUBJECT = "SUBJECT";
    public static final String COLUMN_CONTENTS = "CONTENTS";
    //create message table variables
    public static final String TABLE_MESSAGES = "MESSAGES";
    public static final String COLUMN_MESSAGE = "MESSAGE";
    public static final String COLUMN_MESSAGE_ID = "MESSAGE_ID";

    //Statements to insert into the database.
    public static final String INSERT_MESSAGE_INFO = "insert into " + TABLE_MESSAGES_INFO + "(" + COLUMN_MESSAGE_INFO_ID + ',' +
            COLUMN_TIME_SENT + ',' + COLUMN_FROM + ',' + COLUMN_TO + ',' + COLUMN_TOPIC + ',' +
            COLUMN_SUBJECT + ',' + COLUMN_CONTENTS +
            ")" + "values (?,?,?,?,?,?,?)";

    public static final String INSERT_MESSAGE = "insert into " + TABLE_MESSAGES + "(" + COLUMN_MESSAGE_INFO_ID + ',' +
            COLUMN_MESSAGE +
            ")" + "values (?,?)";

    //Statements to delete from database
    public static final String DELETE_TIME = "DELETE " + TABLE_MESSAGES + "," + TABLE_MESSAGES_INFO + " FROM " + TABLE_MESSAGES_INFO +
            " INNER JOIN " + TABLE_MESSAGES + " ON " + TABLE_MESSAGES_INFO + "." + COLUMN_MESSAGE_INFO_ID + " = " + TABLE_MESSAGES + "." + COLUMN_MESSAGE_INFO_ID +
            " WHERE " + TABLE_MESSAGES_INFO + "." + COLUMN_TIME_SENT + " < ?";

    public static final String DELETE_CONTENTS = "DELETE " + TABLE_MESSAGES + "," + TABLE_MESSAGES_INFO + " FROM " + TABLE_MESSAGES_INFO +
            " INNER JOIN " + TABLE_MESSAGES + " ON " + TABLE_MESSAGES_INFO + "." + COLUMN_MESSAGE_INFO_ID + " = " + TABLE_MESSAGES + "." + COLUMN_MESSAGE_INFO_ID +
            " WHERE " + TABLE_MESSAGES_INFO + "." + COLUMN_CONTENTS + " = ?";


    public static void main(String[] args) throws SQLException {


        //By putting statement  in the parenthesis, there is no need to close statement at the end.
        try (Statement statement = getConnection().createStatement()) {

            // For testing purposes, delete all tables before running code.
            statement.execute("DROP TABLE IF EXISTS  " + TABLE_MESSAGES);
            statement.execute("DROP TABLE IF EXISTS  " + TABLE_MESSAGES_INFO);

            statement.execute("CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGES_INFO + "(\n" +
                    COLUMN_MESSAGE_INFO_ID + "  varchar(255) NOT NULL,\n" +
                    COLUMN_TIME_SENT + " int ,\n" +
                    COLUMN_FROM + " varchar(255),\n" +
                    COLUMN_TO + " varchar(255),\n" +
                    COLUMN_TOPIC + " varchar(255),\n" +
                    COLUMN_SUBJECT + " varchar(255),\n" +
                    COLUMN_CONTENTS + "  int,\n" +
                    "PRIMARY KEY (" + COLUMN_MESSAGE_INFO_ID + ")\n" +
                    ")");

            statement.execute("CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGES + "(\n" +
                    COLUMN_MESSAGE_ID + "  int   AUTO_INCREMENT NOT NULL,\n" +
                    COLUMN_MESSAGE_INFO_ID + " varchar(255) NOT NULL,\n" +
                    COLUMN_MESSAGE + " varchar(255),\n" +
                    "PRIMARY KEY (" + COLUMN_MESSAGE_ID + "),\n" +
                    "FOREIGN KEY(" + COLUMN_MESSAGE_INFO_ID + ") REFERENCES\n" +
                    TABLE_MESSAGES_INFO + "(" + COLUMN_MESSAGE_INFO_ID + ")\n" +
                    "  ON DELETE CASCADE" +
                    ")");

            //Insert the default message in to the database.
            insertMessage_Info("bc18ecb5316e029af586fdec9fd533f413b16652bafe079b23e021a6d8ed69aa", 1614686400L, "martin.brain@city.ac.uk", "null", "#announcements", "Hello!", 2);
            insertMessage("bc18ecb5316e029af586fdec9fd533f413b16652bafe079b23e021a6d8ed69aa", "Hello everyone!");
            insertMessage("bc18ecb5316e029af586fdec9fd533f413b16652bafe079b23e021a6d8ed69aa", "This is the first message sent using PM.");

            System.out.println("Connected to Database!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Insert statement to enter data into message_INFO_ID table into the database.
     *
     * @param id
     * @param unix_time
     * @param from
     * @param to
     * @param topic
     * @param subject
     * @param contents
     * @throws SQLException
     */
    public static void insertMessage_Info(String id, long unix_time, String from, String to, String topic, String subject, int contents) throws SQLException {
        try (PreparedStatement insertIntoMessages = getConnection().prepareStatement(INSERT_MESSAGE_INFO)) {

            insertIntoMessages.setString(1, id);
            insertIntoMessages.setLong(2, unix_time);
            insertIntoMessages.setString(3, from);
            insertIntoMessages.setString(4, to);
            insertIntoMessages.setString(5, topic);
            insertIntoMessages.setString(6, subject);
            insertIntoMessages.setInt(7, contents);

            int affectedRows = insertIntoMessages.executeUpdate();
            if (affectedRows != 1) {
                throw new SQLException("Couldn't insert message-info!");

            }
        }
    }

    /**
     * Insert statement to enter data into message_INFO_ID table into the database.
     *
     * @param id
     * @param message
     */

    public static void insertMessage(String id, String message) {
        try (Connection connection = getConnection();
             PreparedStatement insertIntoMessage = connection.prepareStatement(INSERT_MESSAGE, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);

            // Insert message
            insertIntoMessage.setString(1, id);
            insertIntoMessage.setString(2, message);

            int affectedRows = insertIntoMessage.executeUpdate();
            if (affectedRows == 1) {
                connection.commit();
            } else {
                throw new SQLException("The message insertion failed.");

            }
        } catch (Exception e) {
            System.out.println("Insert message exception: " + e.getMessage());
            try {
                System.out.println("Performing rollback");
                getConnection().rollback();
            } catch (SQLException e2) {
                System.out.println("exception " + e2.getMessage());
            }
        } finally {
            try {
                System.out.println("Resetting default commit behaviour");
                getConnection().setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Couldn't reset auto-commit! " + e.getMessage());
            }

        }
    }

    /**
     * retrieve messages from the database with relevant id
     *
     * @param id
     * @return List of messages which have the specified id
     */
    public static List<Message> queryMessages(String id) {
        try (Statement statement = getConnection().createStatement();
             ResultSet results = statement.executeQuery("SELECT " + TABLE_MESSAGES_INFO + "." + COLUMN_MESSAGE_INFO_ID + "," +
                     TABLE_MESSAGES_INFO + "." + COLUMN_TIME_SENT + "," + TABLE_MESSAGES_INFO + "." + COLUMN_FROM + "," + TABLE_MESSAGES_INFO + "." + COLUMN_TO + "," +
                     TABLE_MESSAGES_INFO + "." + COLUMN_TOPIC + "," + TABLE_MESSAGES_INFO + "." + COLUMN_SUBJECT + "," +
                     TABLE_MESSAGES_INFO + "." + COLUMN_CONTENTS + "," + TABLE_MESSAGES + "." + COLUMN_MESSAGE +
                     " FROM " + TABLE_MESSAGES_INFO +
                     " LEFT JOIN " + TABLE_MESSAGES + " ON " + TABLE_MESSAGES_INFO + "." + COLUMN_MESSAGE_INFO_ID + " = " + TABLE_MESSAGES + "." + COLUMN_MESSAGE_INFO_ID +
                     " WHERE " + TABLE_MESSAGES_INFO + "." + COLUMN_MESSAGE_INFO_ID + " =  '" + id +
                     "' GROUP BY " + TABLE_MESSAGES + "." + COLUMN_MESSAGE)) {

            List<Message> messages = new LinkedList<>();
            while (results.next()) {
                String id1 = results.getString(COLUMN_MESSAGE_INFO_ID);
                Long time_sent = results.getLong(COLUMN_TIME_SENT);
                String from = results.getString(COLUMN_FROM);
                String to = results.getString(COLUMN_TO);
                String topic = results.getString(COLUMN_TOPIC);
                String subject = results.getString(COLUMN_SUBJECT);
                int contents = results.getInt(COLUMN_CONTENTS);
                String message = results.getString(COLUMN_MESSAGE);

                Message msg = new Message(id1, from, to, topic, subject, message, time_sent, contents);
                messages.add(msg);
            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Used when the client queries with no headers
     * @param time
     * @return
     */
    public static List<String> queryTime(Long time) {
        try (Statement statement = getConnection().createStatement();
             ResultSet results = statement.executeQuery("SELECT " + TABLE_MESSAGES_INFO + "." + COLUMN_MESSAGE_INFO_ID +
                     " FROM " + TABLE_MESSAGES_INFO +
                     " WHERE (" + TABLE_MESSAGES_INFO + "." + COLUMN_TIME_SENT + " >= " + time  + ")")) {

            List<String> messages = new LinkedList<>();
            while (results.next()) {
                String id = results.getString(COLUMN_MESSAGE_INFO_ID);
                messages.add(id);

            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Used when the client queries one header
     *
     * @param time
     * @param header
     * @param headerDetail
     * @return id of found messages
     */

    public static List<String> queryheader_1(Long time, String header, String headerDetail) {
        try (Statement statement = getConnection().createStatement();
             ResultSet results = statement.executeQuery("SELECT " + TABLE_MESSAGES_INFO + "." + COLUMN_MESSAGE_INFO_ID +
                     " FROM " + TABLE_MESSAGES_INFO +
                     " WHERE (" + TABLE_MESSAGES_INFO + "." + COLUMN_TIME_SENT + " >= " + time + " AND  " + TABLE_MESSAGES_INFO + "." + header + " = '" + headerDetail + "')")) {

            List<String> messages = new LinkedList<>();
            while (results.next()) {
                String id = results.getString(COLUMN_MESSAGE_INFO_ID);
                messages.add(id);

            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Used when the client queries two headers.
     *
     * @param time
     * @param header
     * @param headerDetail
     * @param header1
     * @param header1Detail
     * @return id of found messages
     */

    public static List<String> queryheader_2(Long time, String header, String headerDetail, String header1, String header1Detail) {
        try (Statement statement = getConnection().createStatement();
             ResultSet results = statement.executeQuery("SELECT " + TABLE_MESSAGES_INFO + "." + COLUMN_MESSAGE_INFO_ID +
                     " FROM " + TABLE_MESSAGES_INFO +
                     " WHERE " + TABLE_MESSAGES_INFO + "." + COLUMN_TIME_SENT + " >= " + time + " AND  " + TABLE_MESSAGES_INFO + "." + header + " = '" + headerDetail
                     + "' AND  " + TABLE_MESSAGES_INFO + "." + header1 + " = '" + header1Detail + "'")
        ) {
            List<String> messages = new LinkedList<>();
            while (results.next()) {
                String id = results.getString(COLUMN_MESSAGE_INFO_ID);

                messages.add(id);

            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Used when the client queries three headers.
     *
     * @param time
     * @param header
     * @param headerDetail
     * @param header1
     * @param header1Detail
     * @param header2
     * @param header2Detail
     * @return id of found messages
     */
    public static List<String> queryheader_3(Long time, String header, String headerDetail, String header1, String header1Detail, String header2, String header2Detail) {
        try (Statement statement = getConnection().createStatement();
             ResultSet results = statement.executeQuery("SELECT " + TABLE_MESSAGES_INFO + "." + COLUMN_MESSAGE_INFO_ID +
                     " FROM " + TABLE_MESSAGES_INFO +
                     " WHERE " + TABLE_MESSAGES_INFO + "." + COLUMN_TIME_SENT + " >= " + time + " AND  " + TABLE_MESSAGES_INFO + "." + header + " = '" + headerDetail
                     + "' AND  " + TABLE_MESSAGES_INFO + "." + header1 + " = '" + header1Detail
                     + "' AND  " + TABLE_MESSAGES_INFO + "." + header2 + " = '" + header2Detail +
                     "'")
        ) {
            List<String> messages = new LinkedList<>();
            while (results.next()) {
                String id = results.getString(COLUMN_MESSAGE_INFO_ID);

                messages.add(id);

            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Used when the client queries 4 headers.
     *
     * @param time
     * @param header
     * @param headerDetail
     * @param header1
     * @param header1Detail
     * @param header2
     * @param header2Detail
     * @param header3
     * @param header3Detail
     * @return id of found messages
     */
    public static List<String> queryheader_4(Long time, String header, String headerDetail, String header1, String header1Detail, String header2, String header2Detail, String header3, String header3Detail) {
        try (Statement statement = getConnection().createStatement();
             ResultSet results = statement.executeQuery("SELECT " + TABLE_MESSAGES_INFO + "." + COLUMN_MESSAGE_INFO_ID + ", COUNT(" + TABLE_MESSAGES_INFO + "." + COLUMN_MESSAGE_INFO_ID + ") AS TOTAL" +
                     " FROM " + TABLE_MESSAGES_INFO +
                     " WHERE " + TABLE_MESSAGES_INFO + "." + COLUMN_TIME_SENT + " >= " + time + " AND  " + TABLE_MESSAGES_INFO + "." + header + " = '" + headerDetail
                     + "' AND  " + TABLE_MESSAGES_INFO + "." + header1 + " = '" + header1Detail
                     + "' AND  " + TABLE_MESSAGES_INFO + "." + header2 + " = '" + header2Detail
                     + "' AND  " + TABLE_MESSAGES_INFO + "." + header3 + " = '" + header3Detail +
                     "' ")
        ) {
            List<String> messages = new LinkedList<>();
            while (results.next()) {
                String id = results.getString(COLUMN_MESSAGE_INFO_ID);

                messages.add(id);

            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * delete message based on time
     *
     * @param time
     */
    public static void deleteTime(Long time) {
        try (Connection connection = getConnection();
             PreparedStatement deletebyTime = connection.prepareStatement(DELETE_TIME, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);

            // delete
            deletebyTime.setLong(1, time);


            int affectedRows = deletebyTime.executeUpdate();
            if (affectedRows == 1) {
                connection.commit();
            } else {
                throw new SQLException("The message deletion failed.");

            }
        } catch (Exception e) {
            System.out.println("delete message exception: " + e.getMessage());
            try {
                System.out.println("Performing rollback");
                getConnection().rollback();
            } catch (SQLException e2) {
                System.out.println("exception " + e2.getMessage());
            }
        } finally {
            try {
                System.out.println("Resetting default commit behaviour");
                getConnection().setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Couldn't reset auto-commit! " + e.getMessage());
            }

        }
    }

    /**
     * delete based on content number.
     *
     * @param contents
     */
    public static void deleteContents(int contents) {
        try (Connection connection = getConnection();
             PreparedStatement deletebyTime = connection.prepareStatement(DELETE_CONTENTS, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);

            // Insert message
            deletebyTime.setLong(1, contents);


            int affectedRows = deletebyTime.executeUpdate();
            if (affectedRows == 1) {
                connection.commit();
            } else {
                throw new SQLException("The message deletion failed.");

            }
        } catch (Exception e) {
            System.out.println("delete message exception: " + e.getMessage());
            try {
                System.out.println("Performing rollback");
                getConnection().rollback();
            } catch (SQLException e2) {
                System.out.println("exception " + e2.getMessage());
            }
        } finally {
            try {
                System.out.println("Resetting default commit behaviour");
                getConnection().setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Couldn't reset auto-commit! " + e.getMessage());
            }

        }
    }


    /**
     * delete based on header and it's value
     *
     * @param header
     * @param headerdetails
     */
    public static void deleteByHeader(String header, String headerdetails) {
        try (Connection connection = getConnection();
             PreparedStatement deletebyHeader = connection.prepareStatement("DELETE " + TABLE_MESSAGES + "," + TABLE_MESSAGES_INFO + " FROM " + TABLE_MESSAGES_INFO +
                     " INNER JOIN " + TABLE_MESSAGES + " ON " + TABLE_MESSAGES_INFO + "." + COLUMN_MESSAGE_INFO_ID + " = " + TABLE_MESSAGES + "." + COLUMN_MESSAGE_INFO_ID +
                     " WHERE " + TABLE_MESSAGES_INFO + "." + header + " = '" + headerdetails + "'")) {
            connection.setAutoCommit(false);


            int affectedRows = deletebyHeader.executeUpdate();
            if (affectedRows == 1) {
                connection.commit();
            } else {
                throw new SQLException("The message deletion failed.");

            }
        } catch (Exception e) {
            System.out.println("delete message exception: " + e.getMessage());
            try {
                System.out.println("Performing rollback");
                getConnection().rollback();
            } catch (SQLException e2) {
                System.out.println("exception " + e2.getMessage());
            }
        } finally {
            try {
                System.out.println("Resetting default commit behaviour");
                getConnection().setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Couldn't reset auto-commit! " + e.getMessage());
            }

        }
    }




}


