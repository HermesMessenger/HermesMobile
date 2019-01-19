package org.hermesmessenger.hermes;

public class Message {

    private String message;
    private String sender;
    private String time;
    private boolean isMine;

    public Message(String sender, String message, String time, boolean isMine) {
        this.message = message;
        this.sender = sender;
        this.time = time;
        this.isMine = isMine;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public String getTime() {
        return time;
    }

    public boolean belongsToCurrentUser() {
        return isMine;
    }

}