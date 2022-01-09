package com.erpapplication.UnpaidInvoices;

import com.erpapplication.InvoiceDatabase.History;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.erpapplication.Dashboard.InitializeDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.bson.Document;

import java.net.URL;
import java.util.ResourceBundle;

public class UnpaidController implements Initializable {
    private final ObservableList<History> list = FXCollections.observableArrayList();
    @FXML
    private TableView<History> unpaidTable;
    @FXML
    private TableColumn<History, Void> Invoice_Number, Client, Vat_ID, Date, Tax_Office, City, Total_Amount, credit;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InitializeDB.changeDatabase("InvoiceDB", "InvoiceDB");

        Invoice_Number.setCellValueFactory(new PropertyValueFactory<>("invNo"));
        Client.setCellValueFactory(new PropertyValueFactory<>("name"));
        Vat_ID.setCellValueFactory(new PropertyValueFactory<>("vatid"));
        Tax_Office.setCellValueFactory(new PropertyValueFactory<>("DOY"));
        City.setCellValueFactory(new PropertyValueFactory<>("city"));
        Date.setCellValueFactory(new PropertyValueFactory<>("date"));
        credit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        Total_Amount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        BasicDBObject regexQuery = new BasicDBObject();
        regexQuery.put("Credit", new BasicDBObject("$regex", "ΜΕ ΠΙΣΤΩΣΗ.*").append("$options", "i"));

        FindIterable<Document> cursor = InitializeDB.collection.find(regexQuery);
        for (Document oldDoc : cursor) {
            list.add(new History(
                    oldDoc.getInteger("Invoice_Number"),
                    (String) oldDoc.get("Client"),
                    oldDoc.getString("Address"),
                    (String) oldDoc.get("Vat_ID"),
                    (String) oldDoc.get("Tax_Office"),
                    (String) oldDoc.get("City"),
                    (String) oldDoc.get("Date"),
                    (String) oldDoc.get("Credit"),
                    oldDoc.getDouble("Vat"),
                    oldDoc.getDouble("Total_Amount")));
        }
        unpaidTable.setItems(list);
    }
}

