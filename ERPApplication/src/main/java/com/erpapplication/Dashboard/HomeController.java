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
import javafx.application.Platform;
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
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.time.LocalDate;
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
            suggest.add(((String) oldDoc.get("Client")));
            suggest1.addAll(((String) oldDoc.get("City")));
            suggest2.addAll(((String) oldDoc.get("Tax_Office")));
            suggest3.addAll(((String) oldDoc.get("Vat_ID")));
        }

        InitializeDB.changeDatabase("Client", "ClientDB");

        for (Document oldDoc : InitializeDB.collection.find()) {
            suggest.add(((String) oldDoc.get("Client")));
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
                    .append("Date", String.valueOf(date))
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

            File myFile = new File(System.getProperty("java.io.tmpdir") + "preview.pdf");
            PDF_Viewer.main(myFile.toString());

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
        if (comboBox.getValue().equals("ΜΕ ΠΙΣΤΩΣΗ..."))
            t12 = time.getValue();
        if (comboBox.getValue().equals("ΜΕΤΡΗΤΑ"))
            t12 = comboBox.getValue();

        ObservableList<Invoice> inv_data = externalclass.getInvoiceData(InvNo, date, t1, t2, t3, t4, t6, t8, t7, t12);

        try {
            double clearAmount = inv_data.get(0).getClearAmount();
            double VatNo = inv_data.get(0).getVAT_Number();

            org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element requestElement = document.createElementNS("http://www.aade.gr/myDATA/invoice/v1.0", "documentObject");
            requestElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            requestElement.setAttribute("xmlns:icls", "https://www.aade.gr/myDATA/incomeClassificaton/v1.0");
            requestElement.setAttribute("xmlns:ecls", "https://www.aade.gr/myDATA/expensesClassificaton/v1.0");
            requestElement.setAttribute("xsi:schemaLocation", "http://www.aade.gr/myDATA/invoice/v1.0/InvoicesDoc-v0.6.xsd");
            document.appendChild(requestElement);

            Element invoice = document.createElement("invoice");
            requestElement.appendChild(invoice);
            Element issuer = document.createElement("issuer");
            invoice.appendChild(issuer);
            Element vatNumber = document.createElement("vatNumber");
            vatNumber.appendChild(document.createTextNode(String.valueOf(inv_data.get(0).VAT_ID)));
            issuer.appendChild(vatNumber);
            Element country = document.createElement("country");
            country.appendChild(document.createTextNode("GR"));
            issuer.appendChild(country);
            Element branch = document.createElement("branch");
            branch.appendChild(document.createTextNode("1"));
            issuer.appendChild(branch);


            Element counterpart = document.createElement("counterpart");
            invoice.appendChild(counterpart);
            Element vatNumber1 = document.createElement("vatNumber");
            vatNumber1.appendChild(document.createTextNode("54646645555"));
            counterpart.appendChild(vatNumber1);
            Element country1 = document.createElement("country");
            country1.appendChild(document.createTextNode("GR"));
            counterpart.appendChild(country1);
            Element branch1 = document.createElement("branch");
            branch1.appendChild(document.createTextNode("1"));
            counterpart.appendChild(branch1);
            Element address = document.createElement("address");
            counterpart.appendChild(address);
            Element postalCode = document.createElement("postalCode");
            postalCode.appendChild(document.createTextNode("63200"));
            address.appendChild(postalCode);
            Element city = document.createElement("city");
            city.appendChild(document.createTextNode(inv_data.get(0).getCity()));
            address.appendChild(city);


            Element invoiceHeader = document.createElement("invoiceHeader");
            invoice.appendChild(invoiceHeader);
            Element aa = document.createElement("aa");
            aa.appendChild(document.createTextNode(String.valueOf(inv_data.get(0).getInvoiceNumber())));
            invoiceHeader.appendChild(aa);
            Element issueDate = document.createElement("issueDate");
            issueDate.appendChild(document.createTextNode(String.valueOf(inv_data.get(0).getDate())));
            invoiceHeader.appendChild(issueDate);
            Element currency = document.createElement("currency");
            currency.appendChild(document.createTextNode("EUR"));
            invoiceHeader.appendChild(currency);


            Element paymentMethods = document.createElement("paymentMethods");
            invoice.appendChild(paymentMethods);
            Element paymentMethodDetails = document.createElement("paymentMethodDetails");
            paymentMethods.appendChild(paymentMethodDetails);
            Element amount = document.createElement("amount");
            amount.appendChild(document.createTextNode(String.valueOf(clearAmount * ((VatNo/100.0) + 1))));
            paymentMethodDetails.appendChild(amount);
            Element paymentMethodInfo = document.createElement("paymentMethodInfo");
            paymentMethodInfo.appendChild(document.createTextNode(inv_data.get(0).getPayment()));
            paymentMethodDetails.appendChild(paymentMethodInfo);


            Element invoiceDetails = document.createElement("invoiceDetails");
            invoice.appendChild(invoiceDetails);
            Element netValue = document.createElement("netValue");
            netValue.appendChild(document.createTextNode(String.valueOf(clearAmount)));
            invoiceDetails.appendChild(netValue);
            Element vatAmount = document.createElement("vatAmount");
            vatAmount.appendChild(document.createTextNode(String.valueOf(clearAmount * VatNo/100.0)));
            invoiceDetails.appendChild(vatAmount);


            Element invoiceSummary = document.createElement("invoiceSummary");
            invoice.appendChild(invoiceSummary);
            Element totalNetValue = document.createElement("totalNetValue");
            totalNetValue.appendChild(document.createTextNode(String.valueOf(clearAmount)));
            invoiceSummary.appendChild(totalNetValue);
            Element totalVatAmount = document.createElement("totalVatAmount");
            totalVatAmount.appendChild(document.createTextNode(String.valueOf(clearAmount * VatNo/100.0)));
            invoiceSummary.appendChild(totalVatAmount);
            Element totalGrossValue = document.createElement("totalGrossValue");
            totalGrossValue.appendChild(document.createTextNode(String.valueOf(clearAmount * ((VatNo/100.0) + 1))));
            invoiceSummary.appendChild(totalGrossValue);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);

            StreamResult result = new StreamResult(new StringWriter());
            transformer.transform(domSource, result);
            String xmlString = result.getWriter().toString();

            String[] argv = new String[1];
            argv[0] = xmlString;

            SendInvs.main(argv);
            inv_data.clear();
        } catch (ParserConfigurationException | TransformerException pce) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error in Connection");
            a.setHeaderText(pce.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    public void addItems() throws IOException {
        Stage stage = new Stage(StageStyle.DECORATED);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("com/erpapplication/products.fxml"));
        Pane pane = fxmlLoader.load();

        stage.setTitle("Diagnosis Multisystems ERP");
        stage.getIcons().add(new Image("com/erpapplication/images/dm_LOGO1.jpg"));
        stage.setResizable(false);
        stage.initOwner(HomeDashboard.mainStage);
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
            stage.getIcons().add(new Image("com/erpapplication/images/closeWhite.png"));
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