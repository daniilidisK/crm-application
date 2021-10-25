package com.erpapplication.UnpaidInvoices;

import com.erpapplication.Dashboard.HomeController;
import com.erpapplication.Dashboard.InitializeDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

public class OnCredit implements Runnable{
    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    Document data = new Document();
    public int i;

    @Override
    public void run() {}

    public static void main(String[] args) {
        OnCredit r1 = new OnCredit();
        Thread th1 = new Thread(r1);
        th1.start();
    }

    public int continueCounting(){
        ObservableList<Integer> numbers = FXCollections.observableArrayList();

        InitializeDB.newDatabaseConnection("Notifications", "Notifications");

        for (Document oldDoc : InitializeDB.collection.find()) {
            numbers.add(oldDoc.getInteger("Number"));
        }
        if (!numbers.isEmpty()) i = Collections.max(numbers) + 1;
        else i = 1;

        return i;
    }

    public void OnCredit() {
        String dateString = format.format(new Date());

        data.append("Number", continueCounting())
                .append("Name", "Η πληρωμή για το τιμολόγιο " + HomeController.InvNo + " ορίστηκε σε πίστωση")
                .append("Date", dateString);

        InitializeDB.collection.insertOne(data);
    }

    public void OnCredit1() {
        String dateString = format.format(new Date());

        data.append("Number", continueCounting())
                .append("Name", "Το διάστημα πληρωμής για το τιμολόγιο " + HomeController.InvNo + " καθορίστηκε στον 1 μήνα από σήμερα")
                .append("Date", dateString);

        InitializeDB.collection.insertOne(data);
    }
    public void OnCredit2() {
        String dateString = format.format(new Date());

        data.append("Number", continueCounting())
                .append("Name", "Το διάστημα πληρωμής για το τιμολόγιο " + HomeController.InvNo + " καθορίστηκε στους 2 μήνες από σήμερα")
                .append("Date", dateString);

        InitializeDB.collection.insertOne(data);
    }
    public void OnCredit3() {
        String dateString = format.format(new Date());

        data.append("Number", continueCounting())
                .append("Name", "Το διάστημα πληρωμής για το τιμολόγιο " + HomeController.InvNo + " καθορίστηκε στους 3 μήνες από σήμερα")
                .append("Date", dateString);

        InitializeDB.collection.insertOne(data);
    }
}
