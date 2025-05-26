package com.GestionEvenement.event.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Conference extends Evenement {
    private String theme;
    private List<Intervenant> intervenants;

    public Conference() { // Default constructor for Jackson
        super();
        this.intervenants = new ArrayList<>();
    }

    public Conference(String id, String nom, LocalDateTime date, String lieu, int capaciteMax, String theme, List<Intervenant> intervenants) {
        super(id, nom, date, lieu, capaciteMax);
        this.theme = theme;
        this.intervenants = intervenants != null ? intervenants : new ArrayList<>();
    }

    @Override
    public void afficherDetails() {
        System.out.println("--- Conference Details ---");
        System.out.println("ID: " + id);
        System.out.println("Name: " + nom);
        System.out.println("Date: " + date);
        System.out.println("Location: " + lieu);
        System.out.println("Capacity: " + capaciteMax);
        System.out.println("Theme: " + theme);
        System.out.println("Intervenants:");
        if (intervenants != null && !intervenants.isEmpty()) {
            intervenants.forEach(i -> System.out.println("  - " + i.getNom() + " (" + i.getSpecialite() + ")"));
        } else {
            System.out.println("  No intervenants listed.");
        }
        System.out.println("Registered Participants: " + participants.size());
        System.out.println("--------------------------");
    }

    // Getters and Setters
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public List<Intervenant> getIntervenants() { return intervenants; }
    public void setIntervenants(List<Intervenant> intervenants) { this.intervenants = intervenants; }
}