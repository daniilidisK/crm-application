package com.erpapplication.Notifications;

public record Notifications(int number, String name, String date) {

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public String getDate() {
        return date;
    }
}