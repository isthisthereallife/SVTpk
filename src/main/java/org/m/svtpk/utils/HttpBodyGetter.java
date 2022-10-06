package org.m.svtpk.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpBodyGetter {
    public static String connectToURLReturnBodyAsString(URL url) {
        System.out.println("connectToURLReturnBodyAsString fick denna URL: "+url.toString());
        BufferedReader reader = null;
        StringBuilder stringBuilder = null;
        try {
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0");
            huc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            huc.connect();
            // read the output from the server
            reader = new BufferedReader(new InputStreamReader(huc.getInputStream(), StandardCharsets.UTF_8));
            stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            huc.disconnect();
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        System.out.println("Returning empty string, something went wrong");
        return "";
    }
}
