package com.erpapplication.ClientDatabase;

import com.mongodb.BasicDBObject;
import com.erpapplication.Dashboard.HomeDashboard;
import com.erpapplication.Dashboard.InitializeDB;
import javafx.beans.property.SimpleObjectProperty;
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
import org.bson.Document;
import org.controlsfx.glyphfont.Glyph;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    private Parent root;
    public static Client client_data;
    private final ObservableList<Client> list = FXCollections.observableArrayList();
    @FXML
    private TableView<Client> ClientTableView;
    @FXML
    private TextField filterFieldClient;
    @FXML
    private TableColumn<Client, String> Client, Address, City, DOY, Occupation, BankAccount, vatid;
    TableColumn<Client, Client> action = new TableColumn<>("Action");
    TableColumn<Client, Client> action1 = new TableColumn<>("");

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
        action.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        action.setCellFactory(param -> new DeleteButton());

        action1.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue()));
        action1.setCellFactory(p -> new EditCell());
    }

    @FXML
    private void refresh() {
        list.clear();
        client();
        functionality();
    }

    private class DeleteButton extends TableCell<Client, Client> {
        final Button btn = new Button("");

        DeleteButton(){
            Glyph gf = new Glyph("FontAwesome", "TRASH_ALT").size(20);
            gf.setStyle("-fx-text-fill: white");
            btn.setGraphic(gf);
            btn.setStyle("-fx-font-size: 15px");

            btn.setOnMouseClicked(t -> {
                Client hist = getTableView().getItems().get(getIndex());

                BasicDBObject query = new BasicDBObject();
                query.put("Address", hist.getAddress());
                query.put("BankAccount", hist.getBankAccount());
                query.put("City", hist.getCity());
                query.put("Client", hist.getClientName());
                query.put("DOY", hist.getDOY());
                query.put("Occupation", hist.getOccupation());
                query.put("VAT_ID", hist.getVATID());

                list.remove(hist);
                InitializeDB.collection.deleteOne(query);
            });
        }

        @Override
        protected void updateItem(Client client, boolean empty) {
            super.updateItem(client, empty);

            if (client == null) {
                setGraphic(null);
                return;
            }
            setGraphic(btn);
        }
    }

    private class EditCell extends TableCell<Client, Client> {
        final Button edit_btn = new Button("");

        EditCell() {
            Glyph gf = new Glyph("FontAwesome", "EDIT").size(20);
            gf.setStyle("-fx-text-fill: white");
            edit_btn.setGraphic(gf);
            edit_btn.setStyle("-fx-font-size: 15px");

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.initOwner(HomeDashboard.mainStage);
            TableRow<Client> client_row = new TableRow<>();

            ClientTableView.setRowFactory(ev -> {
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
                        } catch (IllegalStateException ignored) {}
                    }
                });
                return client_row;
            });

            edit_btn.setOnMouseClicked(event -> {
                client_data = ClientTableView.getItems().get(getIndex());
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
                } catch (IllegalStateException ignored) {}
            });
        }

        @Override
        protected void updateItem(Client client, boolean empty) {
            super.updateItem(client, empty);

            if (client == null) {
                setGraphic(null);
                return;
            }
            setGraphic(edit_btn);
        }
    }
}
