package com.erpapplication.Dashboard;

import com.erpapplication.AADE.SendInvs;
import com.erpapplication.ClientDatabase.Client;
import com.erpapplication.PDFPreview.PDF_Viewer;
import com.erpapplication.UnpaidInvoices.OnCredit;
import com.erpapplication.VatChecker.EUVatCheckResponse;
import com.erpapplication.VatChecker.EUVatChecker;
import com.erpapplication.VatChecker.GRVatCheckResponse;
import com.erpapplication.VatChecker.GRVatChecker;
import com.itextpdf.text.DocumentException;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.controlsfx.control.textfield.TextFields;
import com.erpapplication.products.TableData;
import com.erpapplication.products.productController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HomeController implements Initializable {
    public static int InvNo;
    private final ObservableList<Integer> invNo_list = FXCollections.observableArrayList();
    private final ObservableList<Client> clients = FXCollections.observableArrayList();
    ObservableList<String> payment = FXCollections.observableArrayList("ΜΕΤΡΗΤΑ", "ΜΕ ΠΙΣΤΩΣΗ...");
    ObservableList<String> timeSelector = FXCollections.observableArrayList("ΜΕ ΠΙΣΤΩΣΗ", "ΜΕ ΠΙΣΤΩΣΗ 1 ΜΗΝΑ", "ΜΕ ΠΙΣΤΩΣΗ 2 ΜΗΝΩΝ", "ΜΕ ΠΙΣΤΩΣΗ 3 ΜΗΝΩΝ");
    @FXML
    private TextField tf1, tf2, tf3, tf4, tf5, tf6, tf7, tf8, tf12;
    @FXML
    private JFXComboBox<String> comboBox, time;
    @FXML
    private Label totalamount, label_time;
    @FXML
    private DatePicker datePicker;
    @FXML
    private JFXButton checkVat;

    public void initialize(URL url, ResourceBundle rb) {
        checkVat.visibleProperty().bind(tf7.textProperty().greaterThan(""));
        comboBox.setItems(payment);

        InitializeDB.newDatabaseConnection("InvoiceDB", "InvoiceDB");

        ObservableList<String> suggest = FXCollections.observableArrayList();
        ObservableList<String> suggest1 = FXCollections.observableArrayList();
        ObservableList<String> suggest2 = FXCollections.observableArrayList();
        ObservableList<String> suggest3 = FXCollections.observableArrayList();

        for (Document oldDoc : InitializeDB.collection.find()) {
            suggest.addAll(Collections.singleton(((String) oldDoc.get("Client"))));
            suggest1.addAll(((String) oldDoc.get("City")));
            suggest2.addAll(((String) oldDoc.get("Tax_Office")));
            suggest3.addAll(((String) oldDoc.get("Vat_ID")));
        }

        InitializeDB.changeDatabase("Client", "ClientDB");

        for (Document oldDoc : InitializeDB.collection.find()) {
            suggest.addAll(Collections.singleton(((String) oldDoc.get("Client"))));
            suggest1.addAll(((String) oldDoc.get("City")));
            suggest2.addAll(((String) oldDoc.get("DOY")));
            suggest3.addAll(((String) oldDoc.get("VAT_ID")));
        }
        TextFields.bindAutoCompletion(tf1, suggest.stream().distinct().collect(Collectors.toList()));
        TextFields.bindAutoCompletion(tf3, suggest1.stream().distinct().collect(Collectors.toList()));
        TextFields.bindAutoCompletion(tf4, suggest2.stream().distinct().collect(Collectors.toList()));
        TextFields.bindAutoCompletion(tf7, suggest3.stream().distinct().collect(Collectors.toList()));
    }

    @FXML
    private void createinvoice() throws Exception {
        InitializeDB.changeDatabase("InvoiceDB", "InvoiceDB");

        for (Document oldDoc : InitializeDB.collection.find())
            invNo_list.add(oldDoc.getInteger("Invoice_Number"));

        String t1 = returnText(tf1);
        String t2 = returnText(tf2);
        String t3 = returnText(tf3);
        String t4 = returnText(tf4);
        InvNo = Integer.parseInt(returnText(tf5));
        String t6 = returnText(tf6);
        String t7 = returnText(tf7);
        double t8 = Double.parseDouble(returnText(tf8));

        if (invNo_list.contains(InvNo)) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Invoice Number Error");
            a.setHeaderText("The Invoice number already exist in previous invoice");
            a.setContentText("Please change the invoice number with another non-used number");
            a.showAndWait();
            return;
        }

        InitializeDB.changeDatabase("Client", "ClientDB");

        for (Document oldDoc : InitializeDB.collection.find()) {
            clients.add(new Client(
                    (String) oldDoc.get("Address"),
                    (String) oldDoc.get("City"),
                    (String) oldDoc.get("Client"),
                    (String) oldDoc.get("DOY"),
                    (String) oldDoc.get("Occupation"),
                    (String) oldDoc.get("VAT_ID")));
        }

        if (!clients.contains(new Client(t2, t3, t1, t4, t6, t7))) {
            TextInputDialog td = new TextInputDialog("Insert Bank Account");
            td.setTitle("Insert Bank Account for this client");
            td.setHeaderText("Enter Bank Account");
            td.showAndWait();

            String bankAcc = td.getEditor().getText();
            Document data = new Document();
            data.append("Address", t2)
                    .append("BankAccount", bankAcc)
                    .append("City", t3)
                    .append("Client", t1)
                    .append("DOY", t4)
                    .append("Occupation", t6)
                    .append("VAT_ID", t7);
            InitializeDB.collection.insertOne(data);
        }

        try {
            String t12 = "";
            if (comboBox.getValue().equals("ΜΕ ΠΙΣΤΩΣΗ...")) {
                t12 = time.getValue();

                if (time.getValue().equals("ΜΕ ΠΙΣΤΩΣΗ"))
                    new OnCredit().OnCredit();
                if (time.getValue().equals("ΜΕ ΠΙΣΤΩΣΗ 1 ΜΗΝΑ"))
                    new OnCredit().OnCredit1();
                if (time.getValue().equals("ΜΕ ΠΙΣΤΩΣΗ 2 ΜΗΝΩΝ"))
                    new OnCredit().OnCredit2();
                if (time.getValue().equals("ΜΕ ΠΙΣΤΩΣΗ 3 ΜΗΝΩΝ"))
                    new OnCredit().OnCredit3();
            }
            if (comboBox.getValue().equals("ΜΕΤΡΗΤΑ")) t12 = comboBox.getValue();

            LocalDate date = datePicker.getValue();

            new externalclass(InvNo, date, t1, t2, t3, t4, t6, t8, t7, t12);
            double total_amount = Math.round(externalclass.returnAmount() * ((t8 / 100.0) + 1) * 100.0) / 100.0;

            totalamount.setVisible(true);
            tf12.setVisible(true);
            tf12.setText("€ " + String.format("%.2f", total_amount));

            Document data = new Document("_id", new ObjectId());
            data.append("Invoice_Number", InvNo)
                    .append("Client", t1)
                    .append("Date", (String.valueOf(date)))
                    .append("Vat_ID", t7)
                    .append("Address", t2)
                    .append("Tax_Office", t4)
                    .append("City", t3)
                    .append("Credit", t12)
                    .append("Vat", t8)
                    .append("Total_Amount", total_amount);

            InitializeDB.changeDatabase("InvoiceDB", "InvoiceDB");

            InitializeDB.collection.insertOne(data);
            clearAllfields();

            Document prod = new Document();
            for (TableData pr : productController.list_items) {
                prod.append("Invoice_Number", InvNo)
                        .append("Item", pr.getDesc())
                        .append("Quantity", pr.getQuantity())
                        .append("Unit_Price", pr.getItem_price());

                InitializeDB.changeDatabase("InvoiceDB", "Products");

                InitializeDB.collection.insertOne(prod);
                prod.clear();
            }
        } catch (IOException | DocumentException e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("All fields must be completed");
            a.setHeaderText("Complete all fields properly");
            a.setContentText(e.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void previewInvoice() throws Exception {
        try {
            String t1 = returnText(tf1);
            String t2 = returnText(tf2);
            String t3 = returnText(tf3);
            String t4 = returnText(tf4);
            InvNo = Integer.parseInt(returnText(tf5));
            String t6 = returnText(tf6);
            String t7 = returnText(tf7);
            double t8 = Double.parseDouble(returnText(tf8));
            LocalDate date = datePicker.getValue();

            String t12 = "";
            if (comboBox.getValue().equals("ΜΕ ΠΙΣΤΩΣΗ...")) t12 = time.getValue();
            if (comboBox.getValue().equals("ΜΕΤΡΗΤΑ")) t12 = comboBox.getValue();

            new previewInvoice(InvNo, date, t1, t2, t3, t4, t6, t8, t7, t12);

            totalamount.setVisible(true);
            tf12.setVisible(true);
            double total_amount = previewInvoice.returnAmount() * ((Double.parseDouble(String.valueOf(t8)) / 100.0) + 1);
            tf12.setText("€ " + String.format("%.2f", total_amount));

//            if (Desktop.isDesktopSupported()) {
                File myFile = new File(System.getProperty("java.io.tmpdir") + "preview.pdf");
                PDF_Viewer.main(myFile.toString());
//            }
        } catch (IOException | DocumentException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("All fields must be completed");
            a.setHeaderText("Complete all fields properly");
            a.setContentText(ex.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void aadeUpload() throws Exception {
        previewInvoice();
        SendInvs.main();
    }

    @FXML
    public void addItems() throws IOException {
        Stage stage = new Stage(StageStyle.DECORATED);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("com/erpapplication/products.fxml"));
        Pane pane = fxmlLoader.load();

        stage.initOwner(HomeDashboard.mainStage);
        stage.setTitle("Diagnosis Multisystems ERP");
        stage.getIcons().add(new Image("com/erpapplication/dm_LOGO1.jpg"));
        stage.setResizable(false);
        stage.setScene(new Scene(pane));
        stage.show();
    }

    public void clearAllfields() {
        tf1.clear();
        tf2.clear();
        tf3.clear();
        tf4.clear();
        tf5.clear();
        tf6.clear();
        tf7.clear();
        tf8.clear();
        datePicker.getEditor().clear();
    }

    @FXML
    private void combo() {
        if (comboBox.getValue().equals("ΜΕ ΠΙΣΤΩΣΗ...")) {
            time.setVisible(true);
            time.setItems(timeSelector);
            label_time.setVisible(true);
            time.getSelectionModel().selectFirst();
        } else {
            time.setVisible(false);
            label_time.setVisible(false);
        }
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

    @FXML
    private void checkVat() {
        if (tf7.getText().startsWith("EL") | Character.isDigit(tf7.getText().charAt(0))) checkGreekVAT();
        else checkEUVat();
    }

    public void checkEUVat() {
        String vat = tf7.getText().trim();
        vat = vat.replaceAll(" +", "");

        EUVatCheckResponse res = EUVatChecker.doCheck(vat.substring(0, 2), vat.substring(2).trim());

        Alert alert;
        if (res.isValid()){
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Vat Information");
            alert.setHeaderText("The Vat belongs to: \n");
            alert.setContentText(res.getName() + "\n" + res.getAddress());
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Vat Information");
            alert.setHeaderText("The Vat Number is not valid\n");
            alert.setContentText("We cannot validate this Vat Number");
        }
        alert.showAndWait();
    }

    public void checkGreekVAT() {
        String vat = tf7.getText().replaceAll("[^\\d]", "");
        vat = vat.trim();
        vat = vat.replaceAll(" +", " ");

        GRVatCheckResponse resp = GRVatChecker.doCheck(vat);

        if (resp.isValidStructure() & resp.isValidSyntax()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Vat Information");
            alert.setHeaderText("The Vat is valid: \n");
            alert.setContentText("Valid Structure:\t" + resp.isValidStructure() + "\nValid Syntax:\t\t" + resp.isValidSyntax());
            alert.showAndWait();
        } else if (resp.isValidStructure() | resp.isValidSyntax()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Vat Information");
            alert.setHeaderText("""
                    The VAT you inserted meets the specifications\s
                    of the VAT structure, but does not meet the\s
                    syntax specifications in the specific EU country:\s
                    """);
            alert.setContentText("Valid Structure:\t" + resp.isValidStructure() + "\nValid Syntax:\t\t" + resp.isValidSyntax());
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Vat Information");
            alert.setHeaderText("The Vat Number is not valid\n");
            alert.setContentText("We cannot validate this Vat Number");
            alert.showAndWait();
        }
    }
}