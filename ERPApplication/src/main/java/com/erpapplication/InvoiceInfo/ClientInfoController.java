package com.erpapplication.InvoiceInfo;

import com.erpapplication.ClientDatabase.ClientController;
import com.erpapplication.Dashboard.InitializeDB;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bson.Document;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientInfoController implements Initializable {
    @FXML
    private Label name, address, city, doy, occup, vatid, bankacc;
    @FXML
    private ImageView image;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InitializeDB.changeDatabase("AADE", "Settings");

        String logo = "";
        for (Document oldDoc : InitializeDB.collection.find())
            logo = ((String) oldDoc.get("Logo"));

        Image img = new Image(logo);
        image.setImage(img);

        name.setText(ClientController.client_data.getClientName());
        address.setText(ClientController.client_data.getAddress() + ",");
        city.setText(ClientController.client_data.getCity());
        doy.setText(String.valueOf(ClientController.client_data.getDOY()));
        occup.setText(ClientController.client_data.getOccupation());
        bankacc.setText(ClientController.client_data.getBankAccount());
        vatid.setText(ClientController.client_data.getVATID());
    }
}
