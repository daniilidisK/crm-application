package com.erpapplication.Dashboard;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class InitializeDB {
    public static Logger mongoLogger;
    public static MongoClient mongoClient;
    public static MongoDatabase database;
    public static MongoCollection<Document> collection;

    private InitializeDB() {
        mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);
        mongoClient = new MongoClient("localhost", 27017);
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
