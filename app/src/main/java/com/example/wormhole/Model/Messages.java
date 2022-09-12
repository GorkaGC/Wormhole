package com.example.wormhole.Model;

public class Messages {

    String message;
    String sernderId;
    long timeStamp;


    public Messages() {
    }

    public Messages(String message, String sernderId, long timeStamp) {
        this.message = message;
        this.sernderId = sernderId;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSernderId() {
        return sernderId;
    }

    public void setSernderId(String sernderId) {
        this.sernderId = sernderId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
