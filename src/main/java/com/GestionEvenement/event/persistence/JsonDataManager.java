package com.GestionEvenement.event.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.GestionEvenement.event.model.Evenement;
import com.GestionEvenement.event.model.Conference; // Needed for deserialization if no @JsonTypeInfo
import com.GestionEvenement.event.model.Concert;    // Needed for deserialization if no @JsonTypeInfo

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonDataManager {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Register JavaTimeModule for LocalDateTime support
        objectMapper.registerModule(new JavaTimeModule());
        // Disable writing dates as timestamps for human-readable dates
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // Enable pretty printing JSON
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // With @JsonTypeInfo on Evenement, Jackson can handle subclasses automatically.
        // No need for SimpleModule for registering subtypes explicitly here,
        // as @JsonSubTypes handles it.
    }

    public static void saveEvents(List<Evenement> events, String filePath) {
        try {
            objectMapper.writeValue(new File(filePath), events);
            System.out.println("Events saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving events: " + e.getMessage());
        }
    }

    public static List<Evenement> loadEvents(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) { // Check for existence and if file is empty
            System.out.println("File not found or empty, returning empty list: " + filePath);
            return new ArrayList<>();
        }
        try {
            // Reads as List<Evenement>. @JsonTypeInfo on Evenement handles polymorphic deserialization.
            return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Evenement.class));
        } catch (IOException e) {
            System.err.println("Error loading events: " + e.getMessage());
            // Consider logging the full stack trace for debugging: e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
