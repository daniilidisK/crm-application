package com.erpapplication.Dashboard;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public final class InitializeDB {
    public static MongoClient mongoClient;
    public static MongoDatabase database;
    public static MongoCollection<Document> collection;

    private InitializeDB() {
        //mongoLogger = Logger.getLogger("org.mongodb.driver");
        //mongoLogger.setLevel(Level.SEVERE);
        //mongoClient = new MongoClient("localhost", 27017);


        ConnectionString connectionString =
                new ConnectionString("mongodb+srv://diagnosis:DiagnosisMultisystems2000!@crm.sk4lw.mongodb.net/Invoice?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        mongoClient = MongoClients.create(settings);
    }

    public static void newDatabaseConnection(String db, String coll) {
        new InitializeDB();
        database = mongoClient.getDatabase(db);
        collection = database.getCollection(coll);
    }

    public static void changeDatabase(String db, String coll) {
        database = mongoClient.getDatabase(db);
        collection = database.getCollection(coll);
    }
}
