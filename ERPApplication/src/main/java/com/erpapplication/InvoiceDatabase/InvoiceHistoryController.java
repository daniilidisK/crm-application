package com.erpapplication.InvoiceDatabase;

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
import java.util.ResourceBundle;

public class InvoiceHistoryController implements Initializable {
    static Parent root;
    public static History data;
    private final ObservableList<History> list = FXCollections.observableArrayList();

    @FXML
    private TableView<History> historyTableView;
    @FXML
    private TextField filterField;
    @FXML
    private TableColumn<History, Void> Invoice_Number, Client, Vat_ID, Date, Tax_Office, City, Total_Amount, credit;
    TableColumn action = new TableColumn<>("Actions");
    TableColumn action1 = new TableColumn<>("");

    public void invoice() {
        Invoice_Number.setCellValueFactory(new PropertyValueFactory<>("invNo"));
        Client.setCellValueFactory(new PropertyValueFactory<>("name"));
        Vat_ID.setCellValueFactory(new PropertyValueFactory<>("vatid"));
        Tax_Office.setCellValueFactory(new PropertyValueFactory<>("DOY"));
        City.setCellValueFactory(new PropertyValueFactory<>("city"));
        Date.setCellValueFactory(new PropertyValueFactory<>("date"));
        credit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        Total_Amount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        InitializeDB.newDatabaseConnection("InvoiceDB", "InvoiceDB");

        for (Document oldDoc : InitializeDB.collection.find()) {
            list.add(new History(
                    oldDoc.getInteger("Invoice_Number"),
                    oldDoc.getString("Client"),
                    oldDoc.getString("Address"),
                    oldDoc.getString("Vat_ID"),
                    oldDoc.getString("Tax_Office"),
                    oldDoc.getString("City"),
                    oldDoc.getString("Date"),
                    oldDoc.getString("Credit"),
                    oldDoc.getDouble("Vat"),
                    oldDoc.getDouble("Total_Amount")));
        }
        historyTableView.setItems(list);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        invoice();
        action.setMaxWidth(2400);
        action1.setMaxWidth(1700);
        functionality();

        historyTableView.getColumns().add(action);
        historyTableView.getColumns().add(action1);

        FilteredList<History> filteredData = new FilteredList<>(list, b -> true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(invoice -> {
            if (newValue == null || newValue.isEmpty()) return true;

            String lowerCaseFilter = newValue.toLowerCase();

            if (String.valueOf(invoice.getInvNo()).contains(lowerCaseFilter)) {
                return true;
            } else if (invoice.getName().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (String.valueOf(invoice.getVatid()).contains(lowerCaseFilter))
                return true;
            else if (String.valueOf(invoice.getDate()).contains(lowerCaseFilter)) {
                return true;
            } else if (String.valueOf(invoice.getDOY()).contains(lowerCaseFilter)) {
                return true;
            } else if (String.valueOf(invoice.getCity()).contains(lowerCaseFilter)) {
                return true;
            } else return String.valueOf(invoice.getTotalAmount()).contains(lowerCaseFilter);
        }));
        SortedList<History> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(historyTableView.comparatorProperty());

        historyTableView.setItems(sortedData);
    }

    void functionality(){
        action.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Disposer.Record, Boolean>, ObservableValue<Boolean>>)
                p -> new SimpleBooleanProperty(p.getValue() != null));
        action1.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Disposer.Record, Boolean>, ObservableValue<Boolean>>)
                pr -> new SimpleBooleanProperty(pr.getValue() != null));
        action.setCellFactory((Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>) p -> new ButtonCell());
        action1.setCellFactory((Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>) pr -> new EditCell());
    }

    @FXML
    private void refresh() {
        list.clear();
        invoice();
        functionality();
    }

    private class ButtonCell extends TableCell<Disposer.Record, Boolean> {
        final Button btn = new Button("");

        ButtonCell() {
            Glyph gf = new Glyph("FontAwesome", "TRASH_ALT").size(20);
            gf.setStyle("-fx-text-fill: white");
            btn.setGraphic(gf);

            btn.setStyle("-fx-font-size: 15px");
            btn.setOnAction(t -> {
                History hist = (History) ButtonCell.this.getTableView().getItems().get(ButtonCell.this.getIndex());
                list.remove(hist);

                BasicDBObject query = new BasicDBObject();
                query.put("City", hist.getCity());
                query.put("Date", hist.getDate());
                query.put("Invoice_Number", hist.getInvNo());
                query.put("Tax_Office", hist.getDOY());
                query.put("Total_Amount", hist.getTotalAmount());
                query.put("Vat_ID", hist.getVatid());
                InitializeDB.collection.deleteOne(query);

                InitializeDB.changeDatabase("InvoiceDB", "Products");
                BasicDBObject query1 = new BasicDBObject();
                query1.put("Invoice_Number", hist.getInvNo());
                InitializeDB.collection.deleteMany(query1);
            });
        }

        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if (!empty) setGraphic(btn);
            else setGraphic(null);
        }
    }

    private class EditCell extends TableCell<Disposer.Record, Boolean> {
        Button btn2 = new Button("");

        EditCell() {
            Glyph gf = new Glyph("FontAwesome", "EDIT").size(20);
            gf.setStyle("-fx-text-fill: white");
            btn2.setGraphic(gf);
            btn2.setStyle("-fx-font-size: 15px");

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.initOwner(HomeDashboard.mainStage);
            historyTableView.setRowFactory(evnt -> {
                TableRow<History> row = new TableRow<>();
                btn2.setOnMouseClicked(event -> {
                    data = row.getItem();
                    try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getClassLoader().getResource("com/erpapplication/InvoiceInfo.fxml"));
                        root = loader.load();

                        stage.initOwner(HomeDashboard.mainStage);
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

                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        data = row.getItem();
                        try {
                            FXMLLoader loader = new FXMLLoader();
                            loader.setLocation(getClass().getClassLoader().getResource("com/erpapplication/InvoiceInfo.fxml"));
                            root = loader.load();

                            stage.initOwner(HomeDashboard.mainStage);
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
                    }
                });
                return row;
            });
        }

        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if (!empty) setGraphic(btn2);
            else setGraphic(null);
        }
    }
}
