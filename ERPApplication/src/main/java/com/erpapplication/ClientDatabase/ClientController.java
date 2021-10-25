package com.erpapplication.ClientDatabase;

import com.mongodb.BasicDBObject;
import com.sun.prism.impl.Disposer;
import com.erpapplication.Dashboard.HomeDashboard;
import com.erpapplication.Dashboard.InitializeDB;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.bson.Document;
import org.controlsfx.glyphfont.Glyph;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    private Parent root;
    final Button edit_btn = new Button("");
    public static Client client_data;
    private final ObservableList<Client> list = FXCollections.observableArrayList();
    @FXML
    private TableView<Client> ClientTableView;
    @FXML
    private TextField filterFieldClient;
    @FXML
    private TableColumn<Client, String> Client, Address, City, DOY, Occupation, BankAccount, vatid;
    TableColumn action = new TableColumn<>("Action");
    TableColumn action1 = new TableColumn<>("");

    private void client() {
        Client.setCellValueFactory(new PropertyValueFactory<>("ClientName"));
        Address.setCellValueFactory(new PropertyValueFactory<>("Address"));
        City.setCellValueFactory(new PropertyValueFactory<>("City"));
        DOY.setCellValueFactory(new PropertyValueFactory<>("DOY"));
        Occupation.setCellValueFactory(new PropertyValueFactory<>("Occupation"));
        BankAccount.setCellValueFactory(new PropertyValueFactory<>("BankAccount"));
        vatid.setCellValueFactory(new PropertyValueFactory<>("VATID"));

        InitializeDB.newDatabaseConnection("Client", "ClientDB");

        for (Document oldDoc : InitializeDB.collection.find()) {
            list.addAll(new Client(
                    (String) oldDoc.get("Address"),
                    (String) oldDoc.get("BankAccount"),
                    (String) oldDoc.get("City"),
                    (String) oldDoc.get("Client"),
                    (String) oldDoc.get("DOY"),
                    (String) oldDoc.get("Occupation"),
                    (String) oldDoc.get("VAT_ID")));
        }
        ClientTableView.setItems(list);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        client();
        action.setMaxWidth(2400);
        action1.setMaxWidth(1700);
        functionality();

        ClientTableView.getColumns().add(action);
        ClientTableView.getColumns().add(action1);

        FilteredList<Client> filteredData = new FilteredList<>(list, b -> true);
        filterFieldClient.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(client -> {

            if (newValue == null || newValue.isEmpty()) return true;

            String lowerCaseFilter = newValue.toLowerCase();

            if (String.valueOf(client.getClientName()).contains(lowerCaseFilter)) {
                return true;
            } else if (client.getAddress().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (String.valueOf(client.getCity()).contains(lowerCaseFilter)) {
                return true;
            } else if (String.valueOf(client.getDOY()).contains(lowerCaseFilter)) {
                return true;
            } else if (String.valueOf(client.getOccupation()).contains(lowerCaseFilter)) {
                return true;
            } else return String.valueOf(client.getBankAccount()).contains(lowerCaseFilter);
        }));
        SortedList<Client> sortedData = new SortedList<>(filteredData);

        sortedData.comparatorProperty().bind(ClientTableView.comparatorProperty());

        ClientTableView.setItems(sortedData);
    }

    void functionality(){
        action.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Disposer.Record, Boolean>, ObservableValue<Boolean>>)
                p -> new SimpleBooleanProperty(p.getValue() != null));
        action1.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Disposer.Record, Boolean>, ObservableValue<Boolean>>)
                pr -> new SimpleBooleanProperty(pr.getValue() != null));
        action.setCellFactory((Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>) p -> new DeleteButton());
        action1.setCellFactory((Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>) pr -> new EditCell());
    }

    @FXML
    private void refresh() {
        list.clear();
        client();
        functionality();
    }

    private class DeleteButton extends TableCell<Disposer.Record, Boolean> {
        final Button btn = new Button("");

        DeleteButton(){
            Glyph gf = new Glyph("FontAwesome", "TRASH_ALT").size(20);
            gf.setStyle("-fx-text-fill: white");
            btn.setGraphic(gf);

            btn.setStyle("-fx-font-size: 15px");
            btn.setOnAction(t -> {
                Client hist = (Client) DeleteButton.this.getTableView().getItems().get(DeleteButton.this.getIndex());
                list.remove(hist);

                BasicDBObject query = new BasicDBObject();
                query.put("Address", hist.getAddress());
                query.put("BankAccount", hist.getBankAccount());
                query.put("City", hist.getCity());
                query.put("Client", hist.getClientName());
                query.put("DOY", hist.getDOY());
                query.put("Occupation", hist.getOccupation());
                query.put("VAT_ID", hist.getVATID());
                InitializeDB.collection.deleteOne(query);
            });
        }

        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if(!empty) setGraphic(btn);
            else setGraphic(null);
        }
    }

    private class EditCell extends TableCell<Disposer.Record, Boolean> {
        EditCell() {
            Glyph gf = new Glyph("FontAwesome", "EDIT").size(20);
            gf.setStyle("-fx-text-fill: white");
            edit_btn.setGraphic(gf);
            edit_btn.setStyle("-fx-font-size: 15px");

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.initOwner(HomeDashboard.mainStage);
            ClientTableView.setRowFactory(evnt -> {
                TableRow<Client> client_row = new TableRow<>();
                edit_btn.setOnMouseClicked(event -> {
                    client_data = client_row.getItem();
                    try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getClassLoader().getResource("com/erpapplication/ClientInfo.fxml"));
                        root = loader.load();

                        stage.setTitle("Diagnosis Multisystems ERP");
                        stage.getIcons().add(new Image("com/erpapplication/images/dm_LOGO1.jpg"));
                        stage.setResizable(false);
                        stage.setScene(new Scene(root));
                        stage.showAndWait();
                    } catch (IOException e) {
                        Alert a = new Alert(Alert.AlertType.ERROR);
                        a.setTitle("Error");
                        a.setHeaderText("Error Editing the invoice information");
                        a.setContentText(e.getMessage());
                        a.showAndWait();
                    }
                });

                client_row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!client_row.isEmpty())) {
                        client_data = client_row.getItem();
                        try {
                            FXMLLoader loader = new FXMLLoader();
                            loader.setLocation(getClass().getClassLoader().getResource("com/erpapplication/ClientInfo.fxml"));
                            root = loader.load();

                            stage.setTitle("Diagnosis Multisystems ERP");
                            stage.getIcons().add(new Image("com/erpapplication/images/dm_LOGO1.jpg"));
                            stage.setResizable(false);
                            stage.setScene(new Scene(root));
                            stage.showAndWait();
                        } catch (IOException e) {
                            Alert a = new Alert(Alert.AlertType.ERROR);
                            a.setTitle("Error");
                            a.setHeaderText("Error Editing the invoice information");
                            a.setContentText(e.getMessage());
                            e.printStackTrace();
                            a.showAndWait();
                        }
                    }
                });
                return client_row;
            });
        }

        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if (!empty) setGraphic(edit_btn);
            else setGraphic(null);
        }
    }
}
