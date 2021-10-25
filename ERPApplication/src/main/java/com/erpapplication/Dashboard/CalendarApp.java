/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.erpapplication.Dashboard;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.CalendarView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;

public class CalendarApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        CalendarView calendarView = new CalendarView();

        Calendar invDates = new Calendar("Invoice Dates");
        Calendar paymentDates = new Calendar("Payment dates");
        Calendar events = new Calendar("My events");

        invDates.setShortName("Invoice Dates");
        paymentDates.setShortName("Payment dates");
        events.setShortName("My events");

        invDates.setStyle(Style.STYLE1);
        paymentDates.setStyle(Style.STYLE3);
        events.setStyle(Style.STYLE5);

        CalendarSource familyCalendarSource = new CalendarSource("Family");
        familyCalendarSource.getCalendars().addAll(invDates, paymentDates, events);

        InitializeDB.newDatabaseConnection("Calendar","Calendar");
        Entry<String> dentistAppointment = new Entry<>("Dentist");
        dentistAppointment.setInterval(new Interval(
                LocalDate.of(2021, Month.OCTOBER, 21),
                LocalTime.of(13, 22, 12),
                LocalDate.of(2021, Month.OCTOBER, 21),
                LocalTime.of(15, 42, 42)));
        dentistAppointment.setLocation("sxolh");
        dentistAppointment.setZoneId(ZoneId.of("Europe/Athens"));
        invDates.addEntry(dentistAppointment);

//        System.out.println(dentistAppointment.getTitle()+"\n"+
//                dentistAppointment.getCalendar().getName()+"\n"+
//                dentistAppointment.getLocation() +"\n"+
//                dentistAppointment.getStartDate()+"\n"+
//                dentistAppointment.getEndDate() +"\n"+
//                dentistAppointment.getStartTime()+"\n"+
//                dentistAppointment.getEndTime() + "\n" +
//                dentistAppointment.getZoneId());

        calendarView.getCalendarSources().setAll(familyCalendarSource);
        calendarView.setRequestedTime(LocalTime.now());

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(calendarView);

        Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                while (true) {
                    Platform.runLater(() -> {
                        calendarView.setToday(LocalDate.now());
                        calendarView.setTime(LocalTime.now());
                    });

                    try {
                        sleep(40000); // update every 40 seconds
                    } catch (InterruptedException e) {
                        Alert a = new Alert(Alert.AlertType.ERROR);
                        a.setTitle("Error");
                        a.setHeaderText("Application Error");
                        a.setContentText(e.getMessage());
                        a.showAndWait();
                    }
                }
            }
        };

        updateTimeThread.setPriority(Thread.MIN_PRIORITY);
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();

        Scene scene = new Scene(stackPane);
        primaryStage.setTitle("Calendar");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.getIcons().addAll(new Image("com/erpapplication/dm_LOGO1.jpg"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
