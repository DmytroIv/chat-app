package com.pupilschat;

public class Message {
    private String room;
    private String sender;
    private String content;

    public Message() {
    }

    public Message(String room, String sender, String content) {
        this.room = room;
        this.sender = sender;
        this.content = content;
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
}
