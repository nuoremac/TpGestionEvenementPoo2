package com.GestionEvenement.event.model;

import com.GestionEvenement.event.designpatterns.ParticipantObserver;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;



public class Participant implements ParticipantObserver {
    private String id;
    private String nom;
    private String email;

    public Participant() {} // Default constructor for Jackson

    public Participant(String id, String nom, String email) {
        this.id = id;
        this.nom = nom;
        this.email = email;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override // Implement the update method from ParticipantObserver
    public void update(String message) {
        // Here, we call the existing receiveNotification to handle the asynchronous part
        receiveNotification(message).thenRun(() -> {
            System.out.println("Asynchronous notification for " + nom + " processed by Observer.");
        });
    }

    // Your existing receiveNotification method (now called by update)
    public CompletableFuture<Void> receiveNotification(String message) {

        System.out.println("Participant " + nom + " is about to receive notification (asynchronously): '" + message + "'");
        return CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                System.out.println("Participant " + nom + " received notification (after delay): '" + message + "'");
                return (Void) null ; //It is returned after a successful operation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Notification to " + nom + " was interrupted.");
                return (Void) null ; //It is returned after a successful operation
            }
        }).exceptionally(ex -> {
            System.err.println("Error sending notification to " + nom + ": " + ex.getMessage());
            return (Void) null;
        });
    }
}