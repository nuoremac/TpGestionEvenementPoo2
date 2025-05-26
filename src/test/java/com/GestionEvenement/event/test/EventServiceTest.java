package com.GestionEvenement.event.test;

import com.GestionEvenement.event.exception.CapaciteMaxAtteinteException;
import com.GestionEvenement.event.exception.EvenementDejaExistantException;
import com.GestionEvenement.event.model.*;
import com.GestionEvenement.event.service.GestionEvenements;
import com.GestionEvenement.event.persistence.JsonDataManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir; // For temporary file/directory creation

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections; // For Collections.emptyList()
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletionException; // For unwrapping CompletableFuture exceptions
import java.util.concurrent.CompletableFuture;

public class EventServiceTest {

    private GestionEvenements gestionEvenements;
    private static final String TEST_JSON_FILE_NAME = "test_events.json";

    // Use JUnit's @TempDir to create a temporary directory for test files
    @TempDir
    Path tempDir;

    private String testJsonFilePath;

    @BeforeEach
    void setUp() {
        GestionEvenements.resetInstanceForTesting();
        gestionEvenements = GestionEvenements.getInstance();
        gestionEvenements.getEvenements().clear();

        // Ensure the test file path points to the temporary directory
        testJsonFilePath = tempDir.resolve(TEST_JSON_FILE_NAME).toString();

        // Clean up the test file if it somehow exists from a previous run
        File testFile = new File(testJsonFilePath);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    // --- Core GestionEvenements Tests ---
    @Test
    void testAddEvent() throws EvenementDejaExistantException {
        Conference conf = new Conference("CONF001", "Dev Summit", LocalDateTime.now(), "Online", 100, "Coding Best Practices", new ArrayList<>());
        gestionEvenements.ajouterEvenement(conf);
        assertNotNull(gestionEvenements.rechercherEvenement("CONF001"));
        assertEquals(1, gestionEvenements.getEvenements().size());
    }

    @Test
    void testAddDuplicateEventThrowsException() {
        Conference conf = new Conference("CONF002", "Data Science Meetup", LocalDateTime.now(), "Offline", 50, "ML Trends", new ArrayList<>());
        try {
            gestionEvenements.ajouterEvenement(conf);
        } catch (EvenementDejaExistantException e) {
            fail("Should not throw exception on first add");
        }

        assertThrows(EvenementDejaExistantException.class, () -> {
            gestionEvenements.ajouterEvenement(conf); // Attempt to add again
        });
        assertEquals(1, gestionEvenements.getEvenements().size()); // Still only one event
    }

    @Test
    void testSearchExistingEvent() throws EvenementDejaExistantException {
        Conference conf = new Conference("CONF003", "Search Test", LocalDateTime.now(), "Online", 100, "Search Theme", new ArrayList<>());
        gestionEvenements.ajouterEvenement(conf);
        Evenement foundEvent = gestionEvenements.rechercherEvenement("CONF003");
        assertNotNull(foundEvent);
        assertEquals("Search Test", foundEvent.getNom());
    }

    @Test
    void testSearchNonExistentEvent() {
        assertNull(gestionEvenements.rechercherEvenement("NON_EXISTENT_ID"));
    }

    @Test
    void testRemoveEventFromGestionEvenements() throws EvenementDejaExistantException {
        Conference conf = new Conference("CONF007", "Removal Test", LocalDateTime.now(), "Hall B", 20, "Remove Theme", new ArrayList<>());
        gestionEvenements.ajouterEvenement(conf);
        assertEquals(1, gestionEvenements.getEvenements().size());

        gestionEvenements.supprimerEvenement("CONF007");
        assertEquals(0, gestionEvenements.getEvenements().size());
        assertNull(gestionEvenements.rechercherEvenement("CONF007"));
    }

    @Test
    void testRemoveNonExistentEventFromGestionEvenements() {
        // Removing a non-existent event should not cause an error and map size should remain 0
        gestionEvenements.supprimerEvenement("NON_EXISTENT_ID");
        assertEquals(0, gestionEvenements.getEvenements().size());
    }

    @Test
    void testGestionEvenementsGetEvenements() throws EvenementDejaExistantException {
        Conference conf = new Conference("GEC001", "Get Events Test", LocalDateTime.now(), "Venue", 10, "Topic", null);
        gestionEvenements.ajouterEvenement(conf);
        assertEquals(1, gestionEvenements.getEvenements().size());
        assertTrue(gestionEvenements.getEvenements().containsKey("GEC001"));
    }

    // --- Evenement and Participant Management Tests ---
    @Test
    void testAddParticipantSuccessfully() throws CapaciteMaxAtteinteException {
        Conference conf = new Conference("CONF008", "Capacity Test", LocalDateTime.now(), "Room B", 2, "Topic", null);
        Participant p1 = new Participant("P001", "Alice", "alice@test.com");
        conf.ajouterParticipant(p1);
        assertEquals(1, conf.getParticipants().size());
        assertTrue(conf.getParticipants().contains(p1));
    }

    @Test
    void testRegisterParticipantThrowsCapacityException() {
        Conference conf = new Conference("CONF003", "Small Workshop", LocalDateTime.now(), "Room A", 1, "Tiny Topics", new ArrayList<>());
        Participant p1 = new Participant("P001", "Alice", "alice@test.com");
        Participant p2 = new Participant("P002", "Bob", "bob@test.com");

        assertDoesNotThrow(() -> conf.ajouterParticipant(p1));
        assertEquals(1, conf.getParticipants().size());

        assertThrows(CapaciteMaxAtteinteException.class, () -> {
            conf.ajouterParticipant(p2); // This should fail
        });
        assertEquals(1, conf.getParticipants().size()); // Still only one participant
    }

    @Test
    void testUnregisterParticipant() throws CapaciteMaxAtteinteException {
        Conference conf = new Conference("CONF006", "Unregister Test", LocalDateTime.now(), "Test Loc", 5, "Unregister Theme", new ArrayList<>());
        Participant p1 = new Participant("UP1", "Unreg P1", "up1@test.com");
        Participant p2 = new Participant("UP2", "Unreg P2", "up2@test.com");

        conf.ajouterParticipant(p1);
        conf.ajouterParticipant(p2);
        assertEquals(2, conf.getParticipants().size());

        conf.getParticipants().remove(p1); // Directly manipulate the list for this test
        assertEquals(1, conf.getParticipants().size());
        assertFalse(conf.getParticipants().contains(p1));
        assertTrue(conf.getParticipants().contains(p2));
    }

    // --- Observer Pattern Tests ---
    @Test
    void testObserverRegistrationAndUnregistration() {
        Conference conf = new Conference("CONF008", "Observer Reg/Unreg", LocalDateTime.now(), "Studio A", 10, "Observer Pattern", new ArrayList<>());
        Participant p1 = new Participant("O1", "Observer P1", "o1@test.com");
        Participant p2 = new Participant("O2", "Observer P2", "o2@test.com");

        // Initial state: no observers
        assertEquals(0, conf.getObservers().size());

        // Register observer
        conf.registerObserver(p1);
        assertEquals(1, conf.getObservers().size());
        assertTrue(conf.getObservers().contains(p1));

        conf.registerObserver(p2);
        assertEquals(2, conf.getObservers().size());
        assertTrue(conf.getObservers().contains(p2));

        // Unregister observer
        conf.unregisterObserver(p1);
        assertEquals(1, conf.getObservers().size());
        assertFalse(conf.getObservers().contains(p1));
        assertTrue(conf.getObservers().contains(p2));

        conf.unregisterObserver(p2);
        assertEquals(0, conf.getObservers().size());
        assertFalse(conf.getObservers().contains(p2));
    }

    @Test
    void testAnnulerEventWithObserversTriggersNotifications() throws InterruptedException {
        Conference conf = new Conference("CONF009", "Annul Test", LocalDateTime.now(), "Virtual", 10, "Test Theme", new ArrayList<>());
        Participant p1 = new Participant("OP1", "Observer P1", "op1@test.com");
        Participant p2 = new Participant("OP2", "Observer P2", "op2@test.com");

        conf.registerObserver(p1);
        conf.registerObserver(p2);

        // This will trigger asynchronous notifications
        conf.annuler();

        // Wait for async operations to complete (longer than the simulated delay)
        TimeUnit.SECONDS.sleep(3);

        // In a real scenario, you'd mock Participant to verify its update/receiveNotification was called.
        // For now, console output provides evidence of execution.
        System.out.println("Async notification test finished. Check console output for confirmation of 'received notification'.");
    }

    @Test
    void testAnnulerEventWithoutObservers() {
        Conference conf = new Conference("CONF010", "Annul No Observers", LocalDateTime.now(), "Venue X", 10, "Theme", new ArrayList<>());
        // No observers registered
        assertDoesNotThrow(() -> conf.annuler()); // Should not throw error even with no observers
        // No direct assertable outcome here, but ensures no NullPointerException etc.
    }


    // --- Serialization/Deserialization Tests ---
    @Test
    void testSerializationAndDeserializationForConference() throws EvenementDejaExistantException {
        Conference conf = new Conference("CONF004", "Serialization Demo", LocalDateTime.now(), "Virtual", 10, "JSON in Java", Arrays.asList(new Intervenant("I001", "Test Speaker", "Tech")));
        gestionEvenements.ajouterEvenement(conf);

        JsonDataManager.saveEvents(new ArrayList<>(gestionEvenements.getEvenements().values()), testJsonFilePath);

        gestionEvenements.getEvenements().clear(); // Clear current events
        List<Evenement> loadedEvents = JsonDataManager.loadEvents(testJsonFilePath);

        assertEquals(1, loadedEvents.size());
        Evenement loadedConf = loadedEvents.get(0);
        assertNotNull(loadedConf);
        assertTrue(loadedConf instanceof Conference);
        assertEquals("CONF004", loadedConf.getId());
        assertEquals("Serialization Demo", loadedConf.getNom());
        assertEquals("JSON in Java", ((Conference) loadedConf).getTheme());
        assertEquals(1, ((Conference) loadedConf).getIntervenants().size());
        assertEquals("Test Speaker", ((Conference) loadedConf).getIntervenants().get(0).getNom());
    }

    @Test
    void testSerializationAndDeserializationForConcert() throws EvenementDejaExistantException {
        Concert concert = new Concert("CONC003", "Music Fest", LocalDateTime.now(), "Outdoor", 1000, "Great Band", "Pop Rock");
        gestionEvenements.ajouterEvenement(concert);

        JsonDataManager.saveEvents(new ArrayList<>(gestionEvenements.getEvenements().values()), testJsonFilePath);

        gestionEvenements.getEvenements().clear();
        List<Evenement> loadedEvents = JsonDataManager.loadEvents(testJsonFilePath);

        assertEquals(1, loadedEvents.size());
        Evenement loadedConcert = loadedEvents.get(0);
        assertNotNull(loadedConcert);
        assertTrue(loadedConcert instanceof Concert);
        assertEquals("CONC003", loadedConcert.getId());
        assertEquals("Music Fest", loadedConcert.getNom());
        assertEquals("Great Band", ((Concert) loadedConcert).getArtiste());
        assertEquals("Pop Rock", ((Concert) loadedConcert).getGenreMusical());
    }

    @Test
    void testLoadEventsFromNonExistentFile() {
        List<Evenement> loadedEvents = JsonDataManager.loadEvents("non_existent_file.json");
        assertTrue(loadedEvents.isEmpty());
    }

    @Test
    void testLoadEventsFromEmptyFile() throws IOException {
        File emptyFile = new File(testJsonFilePath);
        emptyFile.createNewFile(); // Create an empty file

        List<Evenement> loadedEvents = JsonDataManager.loadEvents(testJsonFilePath);
        assertTrue(loadedEvents.isEmpty());
    }

    @Test
    void testSaveEventsWithEmptyList() {
        JsonDataManager.saveEvents(Collections.emptyList(), testJsonFilePath);
        File file = new File(testJsonFilePath);
        assertTrue(file.exists());
        // Verify content, should be an empty JSON array "[]"
        assertDoesNotThrow(() -> {
            String content = Files.readString(file.toPath());
            assertEquals("[]", content.trim());
        });
    }

    // --- Stream and Lambda Tests ---
    @Test
    void testGetEventsByLocationWithStreamsAndLambdas() throws EvenementDejaExistantException {
        Conference conf1 = new Conference("C009", "Local Dev Meetup", LocalDateTime.now(), "Online", 50, "Java Best Practices", null);
        Conference conf2 = new Conference("C010", "Cloud Computing Conf", LocalDateTime.now(), "Offline", 100, "AWS Deep Dive", null);
        Concert concert1 = new Concert("CONC002", "Rock Festival", LocalDateTime.now(), "Offline", 5000, "Various Artists", "Rock");
        Conference conf3 = new Conference("C011", "Remote Work Conf", LocalDateTime.now(), "Online", 75, "Remote Tools", null);

        gestionEvenements.ajouterEvenement(conf1);
        gestionEvenements.ajouterEvenement(conf2);
        gestionEvenements.ajouterEvenement(concert1);
        gestionEvenements.ajouterEvenement(conf3);

        List<Evenement> onlineEvents = gestionEvenements.getEventsByLocation("Online");
        assertEquals(2, onlineEvents.size());
        assertTrue(onlineEvents.contains(conf1));
        assertTrue(onlineEvents.contains(conf3));
        assertFalse(onlineEvents.contains(conf2));

        List<Evenement> offlineEvents = gestionEvenements.getEventsByLocation("Offline");
        assertEquals(2, offlineEvents.size());
        assertTrue(offlineEvents.contains(conf2));
        assertTrue(offlineEvents.contains(concert1));
        assertFalse(offlineEvents.contains(conf1));

        List<Evenement> unknownLocationEvents = gestionEvenements.getEventsByLocation("Unknown");
        assertTrue(unknownLocationEvents.isEmpty());
    }

    @Test
    void testGetEventsByLocationWhenNoEvents() {
        assertTrue(gestionEvenements.getEventsByLocation("Anywhere").isEmpty());
    }

    // --- Constructor, Getter, Setter Coverage (Basic instances and calls) ---

    @Test
    void testParticipantGettersAndSetters() {
        Participant p = new Participant();
        p.setId("PID1");
        p.setNom("Test Participant");
        p.setEmail("test@example.com");

        assertEquals("PID1", p.getId());
        assertEquals("Test Participant", p.getNom());
        assertEquals("test@example.com", p.getEmail());

        Participant p2 = new Participant("PID2", "Another User", "another@example.com");
        assertEquals("PID2", p2.getId());
        assertEquals("Another User", p2.getNom());
        assertEquals("another@example.com", p2.getEmail());
    }

    @Test
    void testOrganisateurGettersAndSetters() {
        Organisateur org = new Organisateur();
        org.setId("ORG1");
        org.setNom("Event Master");
        org.setEmail("master@events.com");
        List<Evenement> organisedEvents = new ArrayList<>();
        org.setEvenementsOrganises(organisedEvents);

        assertEquals("ORG1", org.getId());
        assertEquals("Event Master", org.getNom());
        assertEquals("master@events.com", org.getEmail());
        assertEquals(organisedEvents, org.getEvenementsOrganises());

        Organisateur org2 = new Organisateur("ORG2", "Organizer B", "b@events.com");
        assertEquals("ORG2", org2.getId());
        assertEquals("Organizer B", org2.getNom());
        assertEquals("b@events.com", org2.getEmail());
        assertNotNull(org2.getEvenementsOrganises());
        assertTrue(org2.getEvenementsOrganises().isEmpty());
    }

    @Test
    void testOrganisateurAddOrganizedEvent() {
        Organisateur org = new Organisateur("ORG3", "Organizer C", "c@events.com");
        Conference conf = new Conference("ConfA", "Conf A", LocalDateTime.now(), "Loc A", 10, "Theme A", null);
        org.addOrganizedEvent(conf);
        assertEquals(1, org.getEvenementsOrganises().size());
        assertTrue(org.getEvenementsOrganises().contains(conf));
    }

    @Test
    void testIntervenantGettersAndSetters() {
        Intervenant inv = new Intervenant();
        inv.setId("INV1");
        inv.setNom("Dr. Speaker");
        inv.setSpecialite("Physics");

        assertEquals("INV1", inv.getId());
        assertEquals("Dr. Speaker", inv.getNom());
        assertEquals("Physics", inv.getSpecialite());

        Intervenant inv2 = new Intervenant("INV2", "Prof. Guide", "Math");
        assertEquals("INV2", inv2.getId());
        assertEquals("Prof. Guide", inv2.getNom());
        assertEquals("Math", inv2.getSpecialite());
    }

    @Test
    void testEvenementAbstractGettersAndSetters() {
        // Use a concrete subclass to test abstract class getters/setters
        Conference conf = new Conference("EVE1", "Abstract Test", LocalDateTime.now(), "Place", 50, "Abstract Theme", null);
        conf.setId("EVE_UPDATED");
        conf.setNom("Updated Name");
        conf.setDate(LocalDateTime.of(2026, 1, 1, 10, 0));
        conf.setLieu("Updated Place");
        conf.setCapaciteMax(100);
        conf.setParticipants(new ArrayList<>());
        conf.setObservers(new ArrayList<>()); // Test setting empty list

        assertEquals("EVE_UPDATED", conf.getId());
        assertEquals("Updated Name", conf.getNom());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), conf.getDate());
        assertEquals("Updated Place", conf.getLieu());
        assertEquals(100, conf.getCapaciteMax());
        assertNotNull(conf.getParticipants());
        assertTrue(conf.getParticipants().isEmpty());
        assertNotNull(conf.getObservers());
        assertTrue(conf.getObservers().isEmpty());
    }

    @Test
    void testConferenceGettersAndSetters() {
        List<Intervenant> intervenants = Arrays.asList(new Intervenant("I001", "Test Speaker", "Topic"));
        Conference conf = new Conference("CONFA", "Conf A", LocalDateTime.now(), "Loc A", 10, "Theme A", intervenants);
        conf.setTheme("New Theme");
        conf.setIntervenants(new ArrayList<>()); // Test setting empty list

        assertEquals("New Theme", conf.getTheme());
        assertNotNull(conf.getIntervenants());
        assertTrue(conf.getIntervenants().isEmpty());

        // Test constructor with null intervenants
        Conference confWithNullInt = new Conference("ConfB", "Conf B", LocalDateTime.now(), "Loc B", 10, "Theme B", null);
        assertNotNull(confWithNullInt.getIntervenants());
        assertTrue(confWithNullInt.getIntervenants().isEmpty());
    }

    @Test
    void testConcertGettersAndSetters() {
        Concert concert = new Concert("CONCA", "Concert A", LocalDateTime.now(), "Loc B", 1000, "Artist A", "Genre A");
        concert.setArtiste("New Artist");
        concert.setGenreMusical("New Genre");

        assertEquals("New Artist", concert.getArtiste());
        assertEquals("New Genre", concert.getGenreMusical());
    }

    @Test
    void testConferenceAfficherDetails() {
        Intervenant speaker = new Intervenant("S1", "Speaker Name", "Topic");
        Conference conf = new Conference("C1", "Conf Name", LocalDateTime.of(2025, 6, 1, 9, 0), "Venue", 10, "Theme", Arrays.asList(speaker));
        // This primarily tests the execution path, visual inspection for correctness
        assertDoesNotThrow(() -> conf.afficherDetails());

        // Test with no intervenants
        Conference confNoInt = new Conference("C2", "Conf No Int", LocalDateTime.of(2025, 6, 1, 9, 0), "Venue", 10, "Theme", null);
        assertDoesNotThrow(() -> confNoInt.afficherDetails());
    }

    @Test
    void testConcertAfficherDetails() {
        Concert concert = new Concert("CONC1", "Concert Name", LocalDateTime.of(2025, 7, 1, 20, 0), "Arena", 5000, "Artist Name", "Rock");
        // This primarily tests the execution path, visual inspection for correctness
        assertDoesNotThrow(() -> concert.afficherDetails());
    }


    // --- Advanced Asynchronous/Exception Handling Tests ---
    @Test
    void testReceiveNotificationInterrupted() {
        Participant p = new Participant("PINT", "Interrupted Test", "interrupted@test.com");
        CompletableFuture<Void> future = p.receiveNotification("Test interruption");

        // Force an interruption on the current thread (which the CompletableFuture's thread will pick up)
        // This is tricky and not always reliable in a unit test context
        // as the actual thread running the supplyAsync might be different.
        // A more robust test would use Mockito to mock the ExecutorService.
        Thread.currentThread().interrupt(); // Interrupt calling thread, not the future's thread

        // Wait for the future to complete (it should complete exceptionally or gracefully after catching)
        try {
            future.join(); // This might throw CompletionException if the lambda propagated it
        } catch (CompletionException e) {
            // Expected if the InterruptedException is wrapped and rethrown by join
            assertTrue(e.getCause() instanceof InterruptedException);
            System.out.println("Caught expected CompletionException wrapping InterruptedException.");
        } finally {
            // Clear interrupted status for subsequent tests
            Thread.interrupted();
        }

        // Without mocking, verifying the exact `System.err` output is hard.
        // This test mainly ensures the method doesn't crash unexpectedly under theoretical interruption.
    }

    // --- Helper for clearing interrupted status ---
    // This is good practice if you try to induce InterruptedExceptions in tests.
    @Test
    void testInterruptedStatusCleanup() {
        Thread.currentThread().interrupt();
        assertTrue(Thread.interrupted()); // Checks and clears status
        assertFalse(Thread.interrupted()); // Status is now clear
    }
}