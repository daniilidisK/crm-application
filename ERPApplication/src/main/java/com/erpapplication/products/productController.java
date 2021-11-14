package com.erpapplication.products;

import com.jfoenix.controls.JFXButton;
import com.sun.prism.impl.Disposer;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

public class productController implements Initializable {

    public static final ObservableList<TableData> list_items = FXCollections.observableArrayList();
    @FXML
    private TextField description, quantity, itemprice;
    @FXML
    public TableView<TableData> table;
    @FXML
    private TableColumn<TableData, String> c_desc, c_quant, c_itemprice;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableColumn action = new TableColumn<>("Action");
        action.setSortable(false);
        table.getColumns().add(action);

        action.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Disposer.Record, Boolean>, ObservableValue<Boolean>>)
                p -> new SimpleBooleanProperty(p.getValue() != null));
        action.setCellFactory((Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>) p -> new DeleteButton());

        if (!list_items.isEmpty())
            table.setItems(list_items);
    }

    @FXML
    public void minimize(MouseEvent e) {
        ((JFXButton)e.getSource()).getScene().getWindow().hide();
    }

    @FXML
    private void insertData() {
        try {
            String DESC = description.getText();
            int QUANTITY = Integer.parseInt(quantity.getText());
            double ITEM_PRICE = Double.parseDouble(itemprice.getText());

            list_items.add(new TableData(DESC, QUANTITY, ITEM_PRICE));

            c_desc.setCellValueFactory(new PropertyValueFactory<>("desc"));
            c_quant.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            c_itemprice.setCellValueFactory(new PropertyValueFactory<>("item_price"));

            table.setItems(list_items);

            description.clear();
            quantity.clear();
            itemprice.clear();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Wrong Value");
            a.setHeaderText("Wrong Value");
            a.setContentText(e.getMessage());
            a.showAndWait();
        }
    }

    private static class DeleteButton extends TableCell<Disposer.Record, Boolean> {
        final Button btn = new Button("Delete");

        DeleteButton(){
            btn.setStyle("-fx-font-size: 15px");
            btn.setOnAction(t -> {
                TableData hist = (TableData) DeleteButton.this.getTableView().getItems().get(DeleteButton.this.getIndex());
                list_items.remove(hist);
            });
        }

        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if(!empty) setGraphic(btn);
            else setGraphic(null);
        }
    }
}