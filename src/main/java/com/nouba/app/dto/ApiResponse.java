package com.nouba.app.dto;

public class ApiResponse<T> {
    private T data;
    private String message;
    private int status;

    // Constructeur avec données et message
    public ApiResponse(T data, String message, int status) {
        this.data = data;
        this.message = message;
        this.status = status;
    }

    // Constructeur sans données (juste un message)
    public ApiResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }

    // Getters et Setters
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
