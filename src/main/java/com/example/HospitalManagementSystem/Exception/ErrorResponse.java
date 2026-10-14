package com.example.HospitalManagementSystem.Exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String messages;

    public ErrorResponse(){
    }

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String messages) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.messages = messages;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessage(String messages) {
        this.messages = messages;
    }
}
