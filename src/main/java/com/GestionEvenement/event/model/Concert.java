package com.GestionEvenement.event.model;

import java.time.LocalDateTime;

public class Concert extends Evenement {
    private String artiste;
    private String genreMusical;

    public Concert() { // Default constructor for Jackson
        super();
    }

    public Concert(String id, String nom, LocalDateTime date, String lieu, int capaciteMax, String artiste, String genreMusical) {
        super(id, nom, date, lieu, capaciteMax);
        this.artiste = artiste;
        this.genreMusical = genreMusical;
    }

    @Override
    public void afficherDetails() {
        System.out.println("--- Concert Details ---");
        System.out.println("ID: " + id);
        System.out.println("Name: " + nom);
        System.out.println("Date: " + date);
        System.out.println("Location: " + lieu);
        System.out.println("Capacity: " + capaciteMax);
        System.out.println("Artist: " + artiste);
        System.out.println("Genre: " + genreMusical);
        System.out.println("Registered Participants: " + participants.size());
        System.out.println("-----------------------");
    }

    // Getters and Setters
    public String getArtiste() { return artiste; }
    public void setArtiste(String artiste) { this.artiste = artiste; }
    public String getGenreMusical() { return genreMusical; }
    public void setGenreMusical(String genreMusical) { this.genreMusical = genreMusical; }
}