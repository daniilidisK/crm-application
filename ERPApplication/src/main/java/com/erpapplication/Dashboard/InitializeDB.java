package com.erpapplication.Dashboard;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public final class InitializeDB {
    public static MongoClient mongoClient;
    public static MongoDatabase database;
    public static MongoCollection<Document> collection;
    public static boolean alreadyExecuted = false;

    private InitializeDB() {
        if (!alreadyExecuted) {
            ConnectionString connectionString =
                    new ConnectionString("mongodb+srv://diagnosis:1245789936ASDfg%21%40@crm.sk4lw.mongodb.net/?retryWrites=true&w=majority");

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            mongoClient = MongoClients.create(settings);
            alreadyExecuted = true;
        }
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
