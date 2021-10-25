package com.erpapplication.AADE;

import com.mongodb.client.MongoCursor;
import com.erpapplication.Dashboard.InitializeDB;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class SendInvs {
    public static void main() throws IOException, URISyntaxException {
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

            StringEntity reqEntity = new StringEntity("{body}");
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
                JOptionPane.showMessageDialog(null, EntityUtils.toString(entity), "Error", JOptionPane.ERROR_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
        }

        File testUploadFile = new File(System.getProperty("java.io.tmpdir") + "preview.pdf");

        // build httpentity object and assign the file that need to be uploaded
        HttpEntity postData = MultipartEntityBuilder.create().addBinaryBody("upfile", testUploadFile).build();
        HttpUriRequest postRequest = RequestBuilder.post(String.valueOf(builder)).setEntity(postData).build();

        System.out.println("Executing request " + postRequest.getRequestLine());

        HttpResponse response = httpclient.execute(postRequest);
        BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

        //Throw runtime exception if status code isn't 200
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
        }

        //Create the StringBuffer object and store the response into it.
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            result.append(line);
        }

        System.out.println("Response : \n" + result);
    }
}
