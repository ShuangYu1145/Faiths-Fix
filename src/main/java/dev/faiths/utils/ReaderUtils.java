package dev.faiths.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class ReaderUtils {

    public static String readText(InputStreamReader reader) throws IOException {
        int buffer;
        StringBuilder stringBuilder = new StringBuilder();
        while ((buffer = reader.read()) != -1){
            stringBuilder.append((char) buffer);
        }
        return stringBuilder.toString();
    }

    public static InputStreamReader toReader(final HttpURLConnection urlConnection) throws IOException {
        return new InputStreamReader(urlConnection.getInputStream());
    }

    public static String readText(final HttpURLConnection connection) throws IOException {
        return readText(toReader(connection));
    }
}
