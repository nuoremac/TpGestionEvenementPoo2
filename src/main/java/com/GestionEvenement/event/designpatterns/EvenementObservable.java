package com.GestionEvenement.event.designpatterns;

import com.GestionEvenement.event.model.Participant; // Participant is an observer

public interface EvenementObservable {
    void registerObserver(ParticipantObserver observer);
    void unregisterObserver(ParticipantObserver observer);
    void notifyObservers(String message);
}