package com.erpapplication.Settings;

import com.mongodb.client.MongoCursor;
import com.erpapplication.Dashboard.InitializeDB;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

public class settingsController implements Initializable {

    @FXML
    private PasswordField id, subKey;
    @FXML
    private ImageView img;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        InitializeDB.newDatabaseConnection("AADE", "AADE");

        try (MongoCursor<Document> cur = InitializeDB.collection.find().iterator()) {
            var doc = cur.next();
            var values = new ArrayList<>(doc.values());

            id.setText((String) values.get(1));
            subKey.setText((String) values.get(2));

            InitializeDB.changeDatabase("AADE", "Settings");

            String logo = "";
            for (Document oldDoc : InitializeDB.collection.find()) logo = (oldDoc.getString("Logo"));
            if (!logo.equals("")) img.setImage(new Image(logo));

        }
    }

    @FXML
    private void getPassword() {
        String id_pass = id.getText();
        String sub_pass = subKey.getText();

        InitializeDB.changeDatabase("AADE", "AADE");

        Document data = new Document();
        Bson filter = new Document();
        InitializeDB.collection.deleteMany(filter);

        data.append("ID", id_pass).append("Sub_Key", sub_pass);
        InitializeDB.collection.insertOne(data);
    }

    @FXML
    public void changeImage() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Image");

            FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.JPG)", "*.JPG");
            FileChooser.ExtensionFilter extFilterjpg = new FileChooser.ExtensionFilter("jpg files (*.jpg)", "*.jpg");
            FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.PNG)", "*.PNG");
            FileChooser.ExtensionFilter extFilterpng = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
            chooser.getExtensionFilters().addAll(extFilterJPG, extFilterjpg, extFilterPNG, extFilterpng);

            File file = chooser.showOpenDialog(null);

            if(file.isFile()){
                Image image = new Image(file.toURI().toString());
                img.setImage(image);

                InitializeDB.changeDatabase("AADE", "Settings");

                Bson filter = new Document();
                InitializeDB.collection.deleteMany(filter);

                Document data = new Document();
                data.append("Logo", file.toURI().toString());

                InitializeDB.collection.insertOne(data);
            }
        } catch (NoSuchElementException e) {
            JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NullPointerException ignored) {}
    }
}