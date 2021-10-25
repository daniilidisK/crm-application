package com.erpapplication.Agenda;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.bson.Document;

import java.util.logging.Level;
import java.util.logging.Logger;

public class getDates {
    private final ObservableList<String> dates = FXCollections.observableArrayList();
    Logger mongoLogger = Logger.getLogger("org.mongodb.driver");

    private getDates() {
        mongoLogger.setLevel(Level.SEVERE);
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("InvoiceDB");
        MongoCollection<Document> collection = database.getCollection("InvoiceDB");

        for (Document oldDoc : collection.find()) {
            dates.add((String) oldDoc.get("Date"));
        }
        System.out.println(dates);
    }

    public static void main(String[] args) {
        new getDates();
    }
}
