package com.erpapplication.Dashboard;

import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClient;

public class addSchemaDB {
    public static void main(String[] args) {
        MongoClient mongo = new MongoClient("localhost", 27017);

        MongoDatabase database = mongo.getDatabase("AADE");
        database.createCollection("AADE");
        database.createCollection("Settings");

        database = mongo.getDatabase("Client");
        database.createCollection("ClientDB");

        database = mongo.getDatabase("InvoiceDB");
        database.createCollection("InvoiceDB");
        database.createCollection("ClientDB");
        database.createCollection("Products");

        database = mongo.getDatabase("Notifications");
        database.createCollection("Notifications");
    }
}
