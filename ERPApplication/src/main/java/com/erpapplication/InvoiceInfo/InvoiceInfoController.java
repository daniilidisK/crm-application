package com.erpapplication.InvoiceInfo;

import com.erpapplication.InvoiceDatabase.InvoiceHistoryController;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.erpapplication.Dashboard.InitializeDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bson.Document;

import java.net.URL;
import java.util.ResourceBundle;

public class InvoiceInfoController implements Initializable {
    private final ObservableList<Products> list = FXCollections.observableArrayList();
    private final ObservableList<Stringable> total_list = FXCollections.observableArrayList();
    private double total_without_vat = 0;
    @FXML
    private Label name, address, city, number, date, payment;
    @FXML
    private TableView<Products> products;
    @FXML
    private TableView<Stringable> total;
    @FXML
    private TableColumn<Products, Void> item, qnt, unitprice, subtotal;
    @FXML
    private TableColumn<Stringable, Void> summary;
    @FXML
    private ImageView image;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InitializeDB.newDatabaseConnection("AADE", "Settings");

        String logo = "";
        for (Document oldDoc : InitializeDB.collection.find())
            logo = ((String) oldDoc.get("Logo"));

        Image img = new Image(logo);
        image.setImage(img);

        name.setText(InvoiceHistoryController.data.getName());
        city.setText(InvoiceHistoryController.data.getCity());
        number.setText(String.valueOf(InvoiceHistoryController.data.getInvNo()));
        date.setText(InvoiceHistoryController.data.getDate());
        payment.setText(InvoiceHistoryController.data.getCredit());
        address.setText(InvoiceHistoryController.data.getAddress() + ",");

        InitializeDB.changeDatabase("InvoiceDB", "Products");

        item.setCellValueFactory(new PropertyValueFactory<>("item"));
        qnt.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitprice.setCellValueFactory(new PropertyValueFactory<>("unit_Price"));
        subtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        double total_amount;
        BasicDBObject query = new BasicDBObject();
        query.put("Invoice_Number", InvoiceHistoryController.data.getInvNo());

        FindIterable<Document> cursor = InitializeDB.collection.find(query);
        for (Document oldDoc : cursor) {
            total_amount = oldDoc.getInteger("Quantity") * oldDoc.getDouble("Unit_Price");
            list.add(new Products(
                    oldDoc.getString("Item"),
                    oldDoc.getInteger("Quantity"),
                    oldDoc.getDouble("Unit_Price"),
                    total_amount));
            total_without_vat += total_amount;
        }
        products.setItems(list);

        summary.setCellValueFactory(new PropertyValueFactory<>("stringable"));
        total_list.add(new Stringable("Subtotal\t\t\t\t" + String.format("%.2f", total_without_vat)));
        total_list.add(new Stringable("Tax (" + InvoiceHistoryController.data.getVat() + "%)\t\t\t" +
                String.format("%.2f", total_without_vat * InvoiceHistoryController.data.getVat()/100.0)));
        total_list.add(new Stringable("Total\t\t\t\t" +
                String.format("%.2f", total_without_vat * (1 + InvoiceHistoryController.data.getVat()/100.0))));

        total.setItems(total_list);
    }

    public record Stringable(String stringable) {
        public String getStringable() {
            return stringable;
        }
    }
}
