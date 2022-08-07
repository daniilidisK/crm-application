package com.erpapplication.Dashboard;

import com.erpapplication.Notifications.NotificationController;
import com.erpapplication.Notifications.Notifications;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.bson.Document;
import com.erpapplication.products.productController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller extends Application implements Initializable {

    @FXML
    private Pane main_pane, topPane;
    @FXML
    private Circle circle;
    @FXML
    private Label nnumber;
    @FXML
    private VBox sidebar;

    int lsize;
    Stage stage = new Stage();
    private Pane pane;

    public void initialize(URL url, ResourceBundle rb) {
        try {
            topPane.getStyleClass().add("head-background");
            topPane.getStylesheets().add("com/erpapplication/style-light.css");

            sidebar.getStyleClass().add("sidebar");
            sidebar.getStylesheets().add("com/erpapplication/jfoenix.css");

            issue();
//            changeNotification();
        } catch (IOException e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error");
            a.setHeaderText("Error initializing application");
            a.setContentText(e.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void issue() throws IOException {
        safeFXMLLoad("com/erpapplication/issueInvoice.fxml", true);
//        try {
//            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("com/erpapplication/issueInvoice.fxml"));
//            pane = fxmlLoader.load();
//            main_pane.getChildren().setAll(pane);
//
//            productController.list_items.clear();
//        } catch (IOException e) {
//            pane = new FXMLLoader(getClass().getClassLoader().getResource("com/erpapplication/NetIssue.fxml")).load();
//            main_pane.getChildren().setAll(pane);
//        }
    }

    @FXML
    private void invoiceHistory() throws IOException {
        safeFXMLLoad("com/erpapplication/InvoiceHistory.fxml", false);
    }

    @FXML
    private void unpaidInvoices() throws IOException {
        safeFXMLLoad("com/erpapplication/UnpaidInvoices.fxml", false);
    }

    @FXML
    private void writeClient() throws IOException {
        if (InitializeDB.alreadyExecuted)
            safeFXMLLoad("com/erpapplication/insertCL.fxml", false);
        else {
            pane = new FXMLLoader(getClass().getClassLoader().getResource("com/erpapplication/NetIssue.fxml")).load();
            main_pane.getChildren().setAll(pane);
        }
    }

    @FXML
    private void clientData() throws IOException {
        safeFXMLLoad("com/erpapplication/ClientDatabase.fxml", false);
    }

    @FXML
    private void settings() throws IOException {
        safeFXMLLoad("com/erpapplication/settings.fxml", false);
    }

    @FXML
    private void information() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("com/erpapplication/information.fxml"));
        pane = fxmlLoader.load();
        main_pane.getChildren().setAll(pane);
    }

    @FXML
    private void notification() throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("com/erpapplication/notifications.fxml"));
            pane = fxmlLoader.load();
            main_pane.getChildren().setAll(pane);

            changeNotification();
        } catch (IOException e) {
            pane = new FXMLLoader(getClass().getClassLoader().getResource("com/erpapplication/NetIssue.fxml")).load();
            main_pane.getChildren().setAll(pane);
        }
    }

    @FXML
    private void visualising() throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("com/erpapplication/visualising.fxml"));
            pane = fxmlLoader.load();
            main_pane.getChildren().setAll(pane);

            changeNotification();
        } catch (IOException e) {
            pane = new FXMLLoader(getClass().getClassLoader().getResource("com/erpapplication/NetIssue.fxml")).load();
            main_pane.getChildren().setAll(pane);
        }
    }

    @FXML
    private void calendar() {
        start(stage);
    }

    public void changeNotification(){
        InitializeDB.changeDatabase("Notifications", "Notifications");

        NotificationController.list_items.clear();
        for (Document oldDoc : InitializeDB.collection.find()) {
            NotificationController.list_items.add(new Notifications(
                    oldDoc.getInteger("Number"),
                    oldDoc.getString("Name"),
                    oldDoc.getString("Date")));
        }

        lsize = NotificationController.list_items.size();
        if (lsize > 0) {
            circle.setVisible(true);
            nnumber.setVisible(true);
            nnumber.setText(String.valueOf(lsize));
        }
    }

    @Override
    public void start(Stage stage) {
        try {
            Platform.runLater(() -> new CalendarApp().start(stage));
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Calendar Error");
            a.setHeaderText("Error Starting Calendar App");
            a.setContentText(e.getMessage());
            a.showAndWait();
        }
    }

    private void safeFXMLLoad(String fxml, boolean clearProductList) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(fxml));
            pane = fxmlLoader.load();
            main_pane.getChildren().setAll(pane);

            if (clearProductList)
                productController.list_items.clear();
        } catch (IOException e) {
            pane = new FXMLLoader(getClass().getClassLoader().getResource("com/erpapplication/NetIssue.fxml")).load();
            main_pane.getChildren().setAll(pane);
        }
    }
}
