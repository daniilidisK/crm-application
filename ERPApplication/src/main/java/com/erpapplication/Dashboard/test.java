package com.erpapplication.Dashboard;

import java.util.prefs.Preferences;

public class test {
    Preferences preferences = Preferences.userNodeForPackage(test.class);

    public void setCredentials(String username, String password) {
        preferences.put("db_username", username);
        preferences.put("db_password", password);
    }

    public String getUsername() {
        return preferences.get("db_username", null);
    }

    public String getPassword() {
        return preferences.get("db_password", null);
    }

    // your code here
    public static void main(String[] args) {
        test g = new test();
        g.setCredentials("username", "password");

        System.out.println(g.getUsername() + "\n" + g.getPassword());
    }
}