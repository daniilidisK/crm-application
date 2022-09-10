package com.erpapplication.Dashboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.prefs.Preferences;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptCredentials {
    Preferences preferences = Preferences.userNodeForPackage(this.getClass());

    private static final File CREDENTIALS_DIRECTORY = new File(System.getProperty("user.home"), ".store/crm");
    private static final byte[] bFile = new byte[(int) CREDENTIALS_DIRECTORY.length()];

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

    public static void main() throws IOException {
        EncryptCredentials g = new EncryptCredentials();
        g.setCredentials("diagnosis", "MVHONfIcGgqjulBP");

//        byte[] enc = encrypt(key,g.getUsername() + "\n" + g.getPassword());
//
//        try (FileOutputStream outputStream = new FileOutputStream(CREDENTIALS_DIRECTORY)) {
//            outputStream.write(enc);
//        }

//        byte[] dec = decrypt(key, bFile);
//        System.out.println(new String(dec));
//
//        byte[] ret = returnCredentials();
//        System.out.println(new String(ret));
    }

    public static byte[] returnCredentials(String key){
        return decrypt(key, bFile, CREDENTIALS_DIRECTORY);
    }

    public static byte[] encrypt(String key, String content) {
        try {
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            return cipher.doFinal(content.getBytes());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return key.getBytes();
    }

    public static byte[] decrypt(String key, byte[] encrypted, File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();

            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.DECRYPT_MODE, aesKey);

            return cipher.doFinal(encrypted);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return encrypted;
    }
}