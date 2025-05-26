package com.GestionEvenement.event.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.GestionEvenement.event.designpatterns.EvenementObservable;
import com.GestionEvenement.event.designpatterns.ParticipantObserver;
import com.GestionEvenement.event.exception.CapaciteMaxAtteinteException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Conference.class, name = "conference"),
        @JsonSubTypes.Type(value = Concert.class, name = "concert")
})
public abstract class Evenement implements EvenementObservable { // Implements EvenementObservable
    protected String id;
    protected String nom;
    protected LocalDateTime date;
    protected String lieu;
    protected int capaciteMax;
    protected List<Participant> participants; // To track registered participants
    protected List<ParticipantObserver> observers; // For Observer Pattern

    public Evenement() { // Default constructor for Jackson
        this.participants = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public Evenement(String id, String nom, LocalDateTime date, String lieu, int capaciteMax) {
        this.id = id;
        this.nom = nom;
        this.date = date;
        this.lieu = lieu;
        this.capaciteMax = capaciteMax;
        this.participants = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    // Abstract methods to be implemented by subclasses
    public abstract void afficherDetails();

    // Concrete methods
    public void ajouterParticipant(Participant participant) throws CapaciteMaxAtteinteException {
        if (this.participants.size() >= this.capaciteMax) {
            throw new CapaciteMaxAtteinteException("Capacity reached for event: " + nom);
        }
        this.participants.add(participant);
        System.out.println(participant.getNom() + " registered for " + nom);
        // Optionally, notify observers of participant registration
    }

    public void annuler() {
        System.out.println("Event " + nom + " has been cancelled.");
        // Logic to notify participants via Observer pattern
        notifyObservers("Event '" + nom + "' has been cancelled.");
    }

    // --- Observer Pattern Implementation ---
    @Override
    public void registerObserver(ParticipantObserver observer) {
        this.observers.add(observer);
        System.out.println(observer.getNom() + " is now observing " + this.getNom());
    }

    @Override
    public void unregisterObserver(ParticipantObserver observer) {
        this.observers.remove(observer);
        System.out.println(observer.getNom() + " stopped observing " + this.getNom());
    }

    @Override
    public void notifyObservers(String message) {
        System.out.println("\nNotifying observers of event: " + this.getNom());
        for (ParticipantObserver observer : observers) {
            observer.update(message); // Call the update method of each observer
        }
    }

    // Getters and Setters (Generate using Alt+Insert or Cmd+N in IntelliJ)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public int getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(int capaciteMax) { this.capaciteMax = capaciteMax; }
    public List<Participant> getParticipants() { return participants; }
    public void setParticipants(List<Participant> participants) { this.participants = participants; }
    public List<ParticipantObserver> getObservers() { return observers; } // For Jackson, though not strictly needed for logic
    public void setObservers(List<ParticipantObserver> observers) { this.observers = observers; }
}