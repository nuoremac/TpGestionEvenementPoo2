package com.GestionEvenement.event.model;

public class Intervenant {
    private String id;
    private String nom;
    private String specialite;

    public Intervenant() {} // Default constructor for Jackson

    public Intervenant(String id, String nom, String specialite) {
        this.id = id;
        this.nom = nom;
        this.specialite = specialite;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
}
