package com.erpapplication.Notifications;

import com.erpapplication.Dashboard.InitializeDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.bson.Document;

import java.net.URL;
import java.util.ResourceBundle;

public class NotificationController implements Initializable {

    public static final ObservableList<Notifications> list_items = FXCollections.observableArrayList();
    public static int lsize = 0;
    @FXML
    public TableView<Notifications> notification;
    @FXML
    public TableColumn<Notifications, Notifications> name, number, time;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InitializeDB.changeDatabase("Notifications", "Notifications");

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        number.setCellValueFactory(new PropertyValueFactory<>("number"));
        time.setCellValueFactory(new PropertyValueFactory<>("date"));

        list_items.clear();
        for (Document oldDoc : InitializeDB.collection.find()) {
            list_items.add(new Notifications(
                    (int) oldDoc.get("Number"),
                    oldDoc.getString("Name"),
                    oldDoc.getString("Date")));
        }

        lsize = list_items.size();
        notification.setItems(list_items);
    }

//    public static ArrayList<Integer> getInvNumber(){
//        final ObservableList<String> test = FXCollections.observableArrayList();
//        ArrayList<Integer> array = new ArrayList<>();
//
//        for (Document oldDoc : collection.find())
//            test.add(oldDoc.getString("Name"));
//
//        for (String s : test) {
//            Pattern p = Pattern.compile("[^\\d]*([\\d]+)[^\\d]+[\\d]*");
//            Matcher m = p.matcher(s);
//            while(m.find())
//                array.add(Integer.parseInt(m.group(1)));
//        }
//
//        return array;
//    }
}