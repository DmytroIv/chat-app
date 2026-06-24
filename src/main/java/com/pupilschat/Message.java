package com.pupilschat;

public class Message {
    private String room;
    private String sender;
    private String content;
    private String timestamp;

    public Message() {
    }

    // NEW Constructor
    public Message(String room, String sender, String content, String timestamp) {
        this.room = room;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
