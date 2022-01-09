package com.erpapplication.Settings;

import com.mongodb.client.MongoCursor;
import com.erpapplication.Dashboard.InitializeDB;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.PasswordField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
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
    private PasswordField id, subKey, db_name, db_password;
    @FXML
    private ImageView img;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        InitializeDB.changeDatabase("AADE", "AADE");

        try (MongoCursor<Document> cur = InitializeDB.collection.find().iterator()) {
            var doc = cur.next();
            var values = new ArrayList<>(doc.values());

            id.setSkin(new VisiblePasswordFieldSkin(id));
            id.setText((String) values.get(1));
            subKey.setSkin(new VisiblePasswordFieldSkin(subKey));
            subKey.setText((String) values.get(2));
            db_name.setSkin(new VisiblePasswordFieldSkin(db_name));
            db_password.setSkin(new VisiblePasswordFieldSkin(db_password));

            db_name.setText((String) values.get(3));
            db_password.setText((String) values.get(4));

            InitializeDB.changeDatabase("AADE", "Settings");

            String logo = "";
            for (Document oldDoc : InitializeDB.collection.find())
                logo = (oldDoc.getString("Logo"));

            if (!logo.equals("")) img.setImage(new Image(logo));
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error");
            a.setHeaderText("Error Reading Database");
            a.setContentText(e.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void changePassword() {
        InitializeDB.changeDatabase("AADE", "AADE");

        Document data = new Document();

        Document updatedData = new Document();
        updatedData.append("ID", id.getText()).
                append("Sub_Key", subKey.getText()).
                append("DB_Name", db_name.getText()).
                append("DB_Password", db_password.getText());

        Document updateDoc = new Document("$set", updatedData);
        InitializeDB.collection.findOneAndUpdate(data, updateDoc);
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

class VisiblePasswordFieldSkin extends TextFieldSkin {
    private final Button actionButton = new Button("View");
    private final SVGPath actionIcon = new SVGPath();
    private boolean mask = true;

    public VisiblePasswordFieldSkin(PasswordField textField) {
        super(textField);

        actionButton.getStyleClass().clear();
        actionButton.setId("actionButton");
        actionButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        actionButton.setPrefSize(30,30);
        actionButton.setFocusTraversable(false);
        actionButton.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, new Insets(0))));

        getChildren().add(actionButton);
        actionButton.setCursor(Cursor.HAND);
        actionButton.toFront();

        actionIcon.setContent(Icons.VIEWER.getContent());
        actionButton.setGraphic(actionIcon);
        actionButton.setVisible(false);

        actionButton.setOnMouseClicked(event -> {
            if(mask) {
                actionIcon.setContent(Icons.VIEWER_OFF.getContent());
                mask = false;
            } else {
                actionIcon.setContent(Icons.VIEWER.getContent());
                mask = true;
            }
            textField.setText(textField.getText());
            textField.end();
        });
        textField.textProperty().addListener((observable, oldValue, newValue) -> actionButton.setVisible(!newValue.isEmpty()));
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        layoutInArea(actionButton, x, y, w, h,0, HPos.RIGHT, VPos.CENTER);
    }

    @Override
    protected String maskText(String txt) {
        if (getSkinnable() instanceof PasswordField && mask) {
            int n = txt.length();
            return "\u25CF".repeat(n);
        } else return txt;
    }
}

enum Icons {
    VIEWER_OFF("M12 6c3.79 0 7.17 2.13 8.82 5.5-.59 1.22-1.42 2.27-2." +
            "41 3.12l1.41 1.41c1.39-1.23 2.49-2.77 3.18-4.53C21.27 7.11 17 4 12 4c-1.27 " +
            "0-2.49.2-3.64.57l1.65 1.65C10.66 6.09 11.32 6 12 6zm-1.07 1.14L13 9.21c.57.25 1.03.71 " +
            "1.28 1.28l2.07 2.07c.08-.34.14-.7.14-1.07C16.5 9.01 14.48 7 12 7c-.37 0-.72.05-1.07." +
            "14zM2.01 3.87l2.68 2.68C3.06 7.83 1.77 9.53 1 11.5 2.73 15.89 7 19 12 19c1.52 0 2.98-.29 " +
            "4.32-.82l3.42 3.42 1.41-1.41L3.42 2.45 2.01 3.87zm7.5 7.5l2.61 2.61c-.04.01-.08.02-.12.02-1.38 " +
            "0-2.5-1.12-2.5-2.5 0-.05.01-.08.01-.13zm-3.4-3.4l1.75 1.75c-.23.55-.36 1.15-.36 1.78 0 2.48 2.02 " +
            "4.5 4.5 4.5.63 0 1.23-.13 1.77-.36l.98.98c-.88.24-1.8.38-2.75.38-3.79 0-7.17-2.13-8.82-5.5.7-1.43 1.72-2.61 2.93-3.53z"),

    VIEWER("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7." +
            "5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");

    private final String content;

    Icons(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}