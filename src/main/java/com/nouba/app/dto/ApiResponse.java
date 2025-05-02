package com.nouba.app.dto;

/**
 * Classe générique pour les réponses API
 * @param <T> Type des données à retourner
 */
public class ApiResponse<T> {
    private T data;         // Données de la réponse
    private String message; // Message descriptif
    private int status;     // Code statut HTTP

    /**
     * Constructeur complet
     * @param data Données à retourner
     * @param message Message descriptif
     * @param status Code statut HTTP
     */
    public ApiResponse(T data, String message, int status) {
        this.data = data;
        this.message = message;
        this.status = status;
    }

    /**
     * Constructeur sans données
     * @param message Message descriptif
     * @param status Code statut HTTP
     */
    public ApiResponse(String message, int status) {
        this(null, message, status);
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