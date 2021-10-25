package com.erpapplication.InsertClient;

import com.erpapplication.Dashboard.InitializeDB;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.bson.Document;

public class InsertClientController {

    @FXML
    private TextField client, address, doy, vatid, baccount, job, city;
    @FXML
    private Label success, fail;

    @FXML
    public void insertClient() throws Exception {
        String sClient = returnText(client);
        String sAddress = returnText(address);
        String sCity = returnText(city);
        String sDoy = returnText(doy);
        String sBaccount = returnText(baccount);
        String sVatid = returnText(vatid);
        String sJob = returnText(job);

        if(!(sCity.isEmpty() && sAddress.isEmpty() && sBaccount.isEmpty() && sClient.isEmpty() && sJob.isEmpty() &&
                sDoy.isEmpty() && sVatid.isEmpty())){

            InitializeDB.newDatabaseConnection("Client", "ClientDB");

            Document data = new Document();
            data.append("Address", sAddress)
                    .append("BankAccount", sBaccount)
                    .append("City", sCity)
                    .append("Client", sClient)
                    .append("DOY", sDoy)
                    .append("Occupation", sJob)
                    .append("VAT_ID", sVatid);
            InitializeDB.collection.insertOne(data);

            success.setVisible(true);
            fail.setVisible(false);
            clearAllfields();
        } else {
            success.setVisible(false);
            fail.setVisible(true);
        }
    }

    public void clearAllfields(){
        city.clear();
        client.clear();
        doy.clear();
        job.clear();
        vatid.clear();
        baccount.clear();
        address.clear();
    }

    private String returnText(TextField text) throws Exception {
        String txt;
        if (!text.getText().isEmpty())
            txt = text.getText();
        else {
            Alert a = new Alert(Alert.AlertType.ERROR, "All fields must be completed");
            a.setTitle("All fields must be completed");
            a.setHeaderText("Complete the field " + text.getId());
            a.setContentText("Complete the fields and try again.");
            Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image("images/closeWhite.png"));
            a.showAndWait();

            throw new Exception();
        }
        return txt;
    }
}
