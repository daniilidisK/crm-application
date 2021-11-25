package com.erpapplication.AADE;

import com.erpapplication.Dashboard.externalclass;
import com.mongodb.client.MongoCursor;
import com.erpapplication.Dashboard.InitializeDB;
import javafx.scene.control.Alert;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
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
import java.net.URI;
import java.net.URISyntaxException;

public class SendInvs {
    public static final String xmlFilePath = "ERPApplication/src/main/java/com/erpapplication/Dashboard/xmlfile.xml";

    public static void main(String[] argv) throws IOException, URISyntaxException {
        HttpClient httpclient = HttpClients.createDefault();
        URIBuilder builder = new URIBuilder("https://mydata-prod-apim.azure-api.net/myDATA/SendInvoices");

        try {
            InitializeDB.newDatabaseConnection("AADE", "AADE");

            MongoCursor<Document> cursor = InitializeDB.collection.find().iterator();
            Document oldDoc = cursor.next();

            String id_pass = (String) oldDoc.get("ID");
            String sub_key = (String) oldDoc.get("Client");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("aade-user-id", "\"" + id_pass + "\"");
            request.setHeader("Ocp-Apim-Subscription-Key", "\"{" + sub_key + "}\"");

            StringEntity reqEntity = new StringEntity("{" + createXMLFile() + "}");
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
                System.out.println(EntityUtils.toString(entity));

        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error");
            a.setHeaderText(e.getMessage());
            e.printStackTrace();
            a.showAndWait();
        }
    }

    private static DOMSource createXMLFile() {
        try {
            double clearAmount = externalclass.invoice_items.get(0).getClearAmount();
            double VatNo = externalclass.invoice_items.get(0).getVAT_Number();

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
            vatNumber.appendChild(document.createTextNode(String.valueOf(externalclass.invoice_items.get(0).getVAT_ID())));
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
            Element address = document.createElement(externalclass.invoice_items.get(0).getAddress());
            counterpart.appendChild(address);
            Element postalCode = document.createElement("postalCode");
            postalCode.appendChild(document.createTextNode("63200"));
            address.appendChild(postalCode);
            Element city = document.createElement("city");
            city.appendChild(document.createTextNode(externalclass.invoice_items.get(0).getCity()));
            address.appendChild(city);


            Element invoiceHeader = document.createElement("invoiceHeader");
            invoice.appendChild(invoiceHeader);
            Element aa = document.createElement("aa");
            aa.appendChild(document.createTextNode(String.valueOf(externalclass.invoice_items.get(0).getInvoiceNumber())));
            invoiceHeader.appendChild(aa);
            Element issueDate = document.createElement("issueDate");
            issueDate.appendChild(document.createTextNode(String.valueOf(externalclass.invoice_items.get(0).getDate())));
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
            paymentMethodInfo.appendChild(document.createTextNode(externalclass.invoice_items.get(0).getPayment()));
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
            StreamResult streamResult = new StreamResult(new File(xmlFilePath));
            transformer.transform(domSource, streamResult);

            return domSource;
        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
        }
        return null;
    }
}
