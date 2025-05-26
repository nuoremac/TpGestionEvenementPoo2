package com.GestionEvenement.event.app;

import com.GestionEvenement.event.exception.CapaciteMaxAtteinteException;
import com.GestionEvenement.event.exception.EvenementDejaExistantException;
import com.GestionEvenement.event.model.Conference;
import com.GestionEvenement.event.model.Concert;
import com.GestionEvenement.event.model.Evenement;
import com.GestionEvenement.event.model.Intervenant;
import com.GestionEvenement.event.model.Participant;
import com.GestionEvenement.event.service.GestionEvenements;
import com.GestionEvenement.event.persistence.JsonDataManager;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture; // For managing async tasks



public class MainApp {
    public static void main(String[] args) {
        System.out.println("Starting Event Management System...");

        GestionEvenements gestionEvenements = GestionEvenements.getInstance();
        String jsonFilePath = "events.json";

        // 1. Try to load existing events from JSON
        List<Evenement> loadedEvents = JsonDataManager.loadEvents(jsonFilePath);
        System.out.println("\nLoaded " + loadedEvents.size() + " events from " + jsonFilePath);
        loadedEvents.forEach(e -> {
            try {
                gestionEvenements.ajouterEvenement(e); // Re-add to the singleton instance
            } catch (EvenementDejaExistantException ex) {
                System.out.println("Skipping adding duplicate event: " + ex.getMessage());
            }
        });

        // 2. Create and add some new events
        Intervenant speaker1 = new Intervenant("S001", "Dr. Chana", "AI Danger");
        Intervenant speaker2 = new Intervenant("S002", "Mr. Onana", "Quantum Computing");

        Evenement conference1 = new Conference(
                "C001", "AI Future Summit", LocalDateTime.of(2025, 7, 10, 9, 0), "Online", 5, // Small capacity for testing
                "Future of AI", Arrays.asList(speaker1, speaker2)
        );
        Evenement concert1 = new Concert(
                "P001", "Summer Beats", LocalDateTime.of(2025, 8, 15, 20, 0), "OLembe Stadium", 5000,
                "Beyonce ", "POP"
        );

        try {
            gestionEvenements.ajouterEvenement(conference1);
            gestionEvenements.ajouterEvenement(concert1);
            // Attempt to add a duplicate event to test exception
            gestionEvenements.ajouterEvenement(conference1); // This should throw EvenementDejaExistantException
        } catch (EvenementDejaExistantException e) {
            System.out.println("Handled Exception: " + e.getMessage());
        }

        // 3. Register participants and test capacity exception
        Participant p1 = new Participant("P001", "Raoul", "raoul@example.com");
        Participant p2 = new Participant("P002", "Pierre", "pierre@example.com");
        Participant p3 = new Participant("P003", "Rene", "rene@example.com");
        Participant p4 = new Participant("P004", "Ossombe", "ossombe@example.com");
        Participant p5 = new Participant("P005", "Tony", "tony@example.com");
        Participant p6 = new Participant("P006", "Frank", "frank@example.com");


        try {
            conference1.ajouterParticipant(p1);
            conference1.ajouterParticipant(p2);
            conference1.ajouterParticipant(p3);
            conference1.ajouterParticipant(p4);
            conference1.ajouterParticipant(p5);
            conference1.ajouterParticipant(p6); // This should throw CapaciteMaxAtteinteException
        } catch (CapaciteMaxAtteinteException e) {
            System.out.println("Handled Exception: " + e.getMessage());
        }

        // Register participants as observers (Observer Pattern)
        conference1.registerObserver(p1);
        conference1.registerObserver(p2);
        concert1.registerObserver(p3); // P3 observes concert1

        // 4. Trigger event actions and demonstrate notifications (now asynchronous)
        System.out.println("\n--- Triggering Event Cancellation and Asynchronous Notifications ---");
        conference1.annuler(); // This will trigger asynchronous notifications
        concert1.annuler();    // This will trigger asynchronous notifications

        // Wait for asynchronous notifications to complete (important in main() to see output)
        // In a real application, we might use a more sophisticated way to manage these futures
        // or let a framework handle it.
        System.out.println("\nWaiting for asynchronous notifications to complete...");
        try {
            // Collect all CompletableFuture objects and wait for them all to complete
            // This assumes all notifications are triggered before this point.
            // In a production system, you'd manage a pool of these futures.
            CompletableFuture.allOf(
                    // Example: if you had a list of all futures triggered:
                    // List<CompletableFuture<Void>> notificationFutures = ...
                    // notificationFutures.toArray(new CompletableFuture[0])
            ).join(); // Blocks until all completes
        } catch (Exception e) {
            System.err.println("An error occurred while waiting for notifications: " + e.getMessage());
        }


        // 5. Display details of all events
        System.out.println("\n--- Displaying All Events in System ---");
        gestionEvenements.getEvenements().values().forEach(Evenement::afficherDetails);


        // 6. Save current events to JSON
        JsonDataManager.saveEvents(new ArrayList<>(gestionEvenements.getEvenements().values()), jsonFilePath);

        System.out.println("\n--- Application Finished ---");
    }
}