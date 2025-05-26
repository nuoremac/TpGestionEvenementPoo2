package com.GestionEvenement.event.designpatterns;

// This interface makes Participant an observer
// You'll need to modify Participant.java to implement this
public interface ParticipantObserver {
    void update(String message);
    String getNom(); // Adding this for easier logging in observers
}