package com.erpapplication.AADE;

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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SendInvs {
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

            StringEntity reqEntity = new StringEntity("{" + argv[0] + "}");
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
}
