package com.erpapplication.Dashboard;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class InitializeDB {
    public static MongoClient mongoClient;
    public static MongoDatabase database;
    public static MongoCollection<Document> collection;
    public static boolean alreadyExecuted = false;

    private static final String key = "Bar12345Bar12345";   // 128-bit key

    private InitializeDB() throws IOException {
        File CREDENTIALS_DIRECTORY = new File(System.getProperty("user.home"), ".store/crm");

        if (CREDENTIALS_DIRECTORY.exists() && !CREDENTIALS_DIRECTORY.isDirectory()) {
            byte[] dec = EncryptCredentials.returnCredentials(key);
            String[] lines = new String(dec).split("\n");

            if (!alreadyExecuted) {
                ConnectionString connectionString = new ConnectionString("mongodb+srv://" +
                        lines[0] + ":" +
                        lines[1] +
                        "@crm.sk4lw.mongodb.net/?retryWrites=true&w=majority");

                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyConnectionString(connectionString)
                        .build();
                mongoClient = MongoClients.create(settings);
                alreadyExecuted = true;
            }
        } else {
            EncryptCredentials g = new EncryptCredentials();
            g.setCredentials("diagnosis", "MVHONfIcGgqjulBP");

            byte[] enc = EncryptCredentials.encrypt(key,g.getUsername() + "\n" + g.getPassword());

            try (FileOutputStream outputStream = new FileOutputStream(CREDENTIALS_DIRECTORY)) {
                outputStream.write(enc);
            }

            byte[] dec = EncryptCredentials.returnCredentials(key);
            String[] lines = new String(dec).split("\n");

            if (!alreadyExecuted) {
                ConnectionString connectionString = new ConnectionString("mongodb+srv://" +
                        lines[0] + ":" +
                        lines[1] +
                        "@crm.sk4lw.mongodb.net/?retryWrites=true&w=majority");

                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyConnectionString(connectionString)
                        .build();
                mongoClient = MongoClients.create(settings);
                alreadyExecuted = true;
            }
        }
    }

    public static void newDatabaseConnection(String db, String coll) throws IOException {
        new InitializeDB();
        database = mongoClient.getDatabase(db);
        collection = database.getCollection(coll);
    }

    public static void changeDatabase(String db, String coll) {
        database = mongoClient.getDatabase(db);
        collection = database.getCollection(coll);
    }
}
