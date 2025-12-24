package se.kth.awad.librarymongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("=== TESTING MONGODB CONNECTION ===\n");

        try {
            // Connection string med autentisering
            String connStr = "mongodb://appUser:AppPass2025!@localhost:27017/library?authSource=library";

            System.out.println("Connecting to MongoDB...");

            // Skapa connection settings
            ConnectionString connectionString = new ConnectionString(connStr);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();

            // Anslut
            MongoClient mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("library");

            System.out.println("Connected to MongoDB!");
            System.out.println("Database: " + database.getName());

            // Testa att läsa collections
            System.out.println("\nCollections in database:");
            for (String collectionName : database.listCollectionNames()) {
                System.out.println("  - " + collectionName);
            }

            // Testa att läsa första boken
            System.out.println("\nTesting read from BOOKS collection:");
            Document firstBook = database.getCollection("BOOKS")
                    .find()
                    .first();

            if (firstBook != null) {
                System.out.println("First book: " + firstBook.get("title"));
            } else {
                System.out.println("No books found");
            }

            // Stäng anslutning
            mongoClient.close();
            System.out.println("\nDisconnected successfully!");

        } catch (Exception e) {
            System.err.println("\nERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}