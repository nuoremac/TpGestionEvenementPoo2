package com.GestionEvenement.event.service;

import com.GestionEvenement.event.exception.EvenementDejaExistantException;
import com.GestionEvenement.event.model.Evenement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Import for Stream API

public class GestionEvenements {
    private static GestionEvenements instance; // The single instance
    private Map<String, Evenement> evenements; // Map to store events

    // Private constructor to prevent direct instantiation
    private GestionEvenements() {
        this.evenements = new HashMap<>();
        // Optionally load data at startup
        // This is handled in MainApp for clearer demonstration of loading
    }

    // Public static method to get the single instance (Singleton pattern)
    public static synchronized GestionEvenements getInstance() {
        if (instance == null) {
            instance = new GestionEvenements();
        }
        return instance;
    }

    public void ajouterEvenement(Evenement evenement) throws EvenementDejaExistantException {
        if (evenements.containsKey(evenement.getId())) {
            throw new EvenementDejaExistantException("Event with ID " + evenement.getId() + " already exists.");
        }
        evenements.put(evenement.getId(), evenement);
        System.out.println("Event added: " + evenement.getNom());
    }

    public Evenement rechercherEvenement(String id) {
        return evenements.get(id);
    }

    public void supprimerEvenement(String id) {
        evenements.remove(id);
        System.out.println("Event removed: " + id);
    }

    public Map<String, Evenement> getEvenements() {
        return evenements;
    }

    // Method using Streams and Lambdas (for filtering events by location)
    public List<Evenement> getEventsByLocation(String location) {
        return evenements.values().stream() // Get a stream of all events
                .filter(event -> event.getLieu().equalsIgnoreCase(location)) // Filter by location (lambda)
                .collect(Collectors.toList()); // Collect results into a new List
    }

    // For testing purposes, you might want a way to reset the instance or its state
    public static void resetInstanceForTesting() {
        instance = null; // Forces a new instance on next getInstance() call
    }
}
