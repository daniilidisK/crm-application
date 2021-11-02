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

import com.calendarfx.model.*;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.view.CalendarView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.bson.Document;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class CalendarApp extends Application {
    private final ObservableList<CalStructure> list = FXCollections.observableArrayList();
    Document data;

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

        CalendarSource CalendarSource = new CalendarSource("Family");
        CalendarSource.getCalendars().addAll(invDates, paymentDates, events);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

        InitializeDB.newDatabaseConnection("Calendar","Calendar");
        String timezone;
        for (Document doc : InitializeDB.collection.find()) {
            timezone = doc.getString("Zoneid");
            list.add(new CalStructure(
                    doc.getString("Name"),
                    doc.getBoolean("isFullday"),
                    doc.getString("Calendar"),
                    doc.getDate("StartDate").toInstant().atZone(ZoneId.of(timezone)).toLocalDate(),
                    doc.getDate("endDate").toInstant().atZone(ZoneId.of(timezone)).toLocalDate(),
                    LocalTime.parse(doc.getString("startTime"), dtf),
                    LocalTime.parse(doc.getString("endTime"), dtf),
                    doc.getString("Location"),
                    ZoneId.of(timezone),
                    doc.getString("RecurrenceRule")));
        }

        Entry<String> eventEntry;
        for (CalStructure list : list) {
            eventEntry = new Entry<>(list.getEventName());

            if (list.getCalendar().equals(invDates.getName()))
                eventEntry.setCalendar(invDates);
            else if (list.getCalendar().equals(paymentDates.getName()))
                eventEntry.setCalendar(paymentDates);
            else eventEntry.setCalendar(events);

            eventEntry.setInterval(list.getStartDate(),
                    list.getStartTime(),
                    list.getEndDate(),
                    list.getEndTime(),
                    list.getZoneid());
            eventEntry.setFullDay(list.getFullDay());
            eventEntry.setLocation(list.getLocation());
            eventEntry.setRecurrenceRule(list.getRecurrenceRule());
        }

        invDates.addEventHandler(CalendarEvent.ENTRY_CALENDAR_CHANGED, this::Entry2DB);
        paymentDates.addEventHandler(CalendarEvent.ENTRY_CALENDAR_CHANGED, this::Entry2DB);
        events.addEventHandler(CalendarEvent.ENTRY_CALENDAR_CHANGED, this::Entry2DB);

        calendarView.getCalendarSources().setAll(CalendarSource);
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
                        sleep(1000); // update every 40 seconds
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

    public record CalStructure(String eventName, boolean isFullDay, String calendar, LocalDate startDate, LocalDate endDate,
                               LocalTime startTime, LocalTime endTime, String location, ZoneId zoneid, String RecurrenceRule) {

        public String getEventName() {
            return eventName;
        }

        public boolean getFullDay() {
            return isFullDay;
        }

        public String getCalendar() {
            return calendar;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public String getLocation() {
            return location;
        }

        public ZoneId getZoneid() {
            return zoneid;
        }

        public String getRecurrenceRule() {
            return RecurrenceRule;
        }
    }

    public void Entry2DB(CalendarEvent entry) {
        DateTimeFormatter timeColonFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        data = new Document();
        data.append("Calendar", entry.getEntry().getCalendar().getName())
                .append("Location", entry.getEntry().getLocation())
                .append("Name", entry.getEntry().getTitle())
                .append("RecurrenceRule", entry.getEntry().getRecurrenceRule())
                .append("StartDate", entry.getEntry().getStartDate())
                .append("Zoneid", entry.getEntry().getZoneId().toString())
                .append("endDate", entry.getEntry().getEndDate())
                .append("endTime", entry.getEntry().getEndTime().format(timeColonFormatter))
                .append("isFullday", entry.getEntry().isFullDay())
                .append("startTime", entry.getEntry().getStartTime().format(timeColonFormatter));

        InitializeDB.collection.insertOne(data);
        data.clear();
    }
}
