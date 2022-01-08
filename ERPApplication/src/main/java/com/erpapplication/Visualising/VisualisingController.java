package com.erpapplication.Visualising;

import com.erpapplication.Dashboard.InitializeDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.bson.Document;

import java.net.URL;
import java.util.ResourceBundle;

public class VisualisingController implements Initializable {
    @FXML
    private LineChart<String, Double> chart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InitializeDB.changeDatabase("InvoiceDB", "InvoiceDB");

        ObservableList<String> date = FXCollections.observableArrayList();
        ObservableList<Double> numbers= FXCollections.observableArrayList();
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        int i = 0;
        for (Document oldDoc : InitializeDB.collection.find()) {
            numbers.add(((Double) oldDoc.get("Total_Amount")));
            date.add(oldDoc.getString("Date"));

            final XYChart.Data<String, Double> data = new XYChart.Data<>(oldDoc.getString("Date"), (Double) oldDoc.get("Total_Amount"));
            data.setNode(new HoveredThresholdNode(numbers.get(i), date.get(i)));

            series.getData().add(data);
            i++;
        }
        chart.getData().add(series);
    }

    public static class HoveredThresholdNode extends StackPane {
        public HoveredThresholdNode(double value, String date) {
            setPrefSize(15, 15);

            final Label label = createDataThresholdLabel(value, date);

            setOnMouseEntered(mouseEvent -> {
                getChildren().setAll(label);
                toFront();
            });
            setOnMouseExited(mouseEvent -> getChildren().clear());
        }

        private Label createDataThresholdLabel(double value, String date) {
            final Label label = new Label(value + "€" + "\n" + date);
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }
}