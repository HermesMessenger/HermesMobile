package org.hermesmessenger.hermes;

import android.graphics.Bitmap;

public class Message {

    private String message;
    private String sender;
    private String time;
    private boolean isMine;
    private Bitmap avatar;
    private String color;

    public Message(String sender, String message, String time, boolean isMine, Bitmap avatar, String color) {
        this.message = message;
        this.sender = sender;
        this.time = time;
        this.isMine = isMine;
        this.avatar = avatar;
        this.color = color;
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

    public Bitmap getAvatar() {
        return avatar;
    }

    public String getColor() {
        return color;
    }
}