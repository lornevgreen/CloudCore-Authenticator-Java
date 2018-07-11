package com.cloudcore.authenticator.core;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

public class Utils {


    public static CloudCoin[] LoadJson(String filename) {
        try {
            byte[] json = Files.readAllBytes(Paths.get(filename));
            Gson gson = new Gson();
            Stack coins = gson.fromJson(new String(json), Stack.class);
            return coins.cc;
        } catch (Exception e) {
            return null;
        }
    }

    public static StringBuilder CoinsToCSV(ArrayList<CloudCoin> coins) {
        StringBuilder csv = new StringBuilder();

        String headerLine = String.format("sn,denomination,nn,");
        String headeranString = "";
        for (int i = 0; i < Config.NodeCount; i++) {
            headeranString += "an" + (i + 1) + ",";
        }

        // Write the Header Record
        csv.append(headerLine + headeranString + System.lineSeparator());

        // Write the Coin Serial Numbers
        for (CloudCoin coin : coins) {
            csv.append(coin.GetCSV() + System.lineSeparator());
        }
        return csv;
    }

    public static String WriteObjecttoString() {
        // TODO: Never Implemented: MemoryStream ms = new MemoryStream();

        // Serializer the User Object to the stream.
        return "";
    }


    private static Random random = new Random();
    private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String RandomString(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

    /**
     * Method ordinalIndexOf used to parse cloudcoins. Finds the nth number of a character within a String
     *
     * @param str    The String to search in
     * @param substr What to count in the String
     * @param n      The nth number
     * @return The index of the nth number
     */
    public static int ordinalIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1) {
            pos = str.indexOf(substr, (pos + 1));
        }
        return pos;
    }//end ordinal Index of


    public static String GetHtmlFromURL(String urlAddress) {
        String data = "";

        try {
            URL url = new URL(urlAddress);
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            if (200 != connect.getResponseCode())
                return data;

            BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));

            StringBuilder builder = new StringBuilder();
            while ((data = in.readLine()) != null)
                builder.append(data);
            in.close();
            data = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            data = "";
        }

        return data;
    }//end get HTML
}
