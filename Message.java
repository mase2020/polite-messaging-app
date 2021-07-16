

/**
 * @author Muhammad Masum Miah
 * version 1.0
 * This class is for the messages which the polite messaging app can generate and retrieve.
 */
public class Message {
    private String id, from, to, topic, subject, message;
    private long time;
    private int contents;


    public Message(String id, String from, String to, String topic, String subject, String message, Long time, int contents) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.topic = topic;
        this.subject = subject;
        this.message = message;
        this.time = time;
        this.contents = contents;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getContents() {
        return contents;
    }

    public void setContents(int contents) {
        this.contents = contents;
    }
}
