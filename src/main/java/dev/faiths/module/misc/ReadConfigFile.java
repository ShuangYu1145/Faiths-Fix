package dev.faiths.module.misc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadConfigFile {
    public static void main(String[] args) {
        String filePath = "D:\\todesk\\config.ini";
        String targetKey = "LoginPhone=";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(targetKey)) {
                    String value = line.substring(targetKey.length());
                    System.out.println(value);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

