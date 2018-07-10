package com.cloudcore.authenticator.core;

import com.cloudcore.authenticator.Config;
import com.google.gson.Gson;

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

        var headerLine = String.Format("sn,denomination,nn,");
        String headeranString = "";
        for (int i = 0; i < Config.NodeCount; i++) {
            headeranString += "an" + (i + 1) + ",";
        }

        // Write the Header Record
        csv.AppendLine(headerLine + headeranString);

        // Write the Coin Serial Numbers
        for (CloudCoin coin : coins) {
            csv.AppendLine(coin.GetCSV());
        }
        return csv;
    }

    public static String WriteObjectToString() {
        MemoryStream ms = new MemoryStream();

        // Serializer the User object to the stream.  
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
        int pos = str.IndexOf(substr);
        while (--n > 0 && pos != -1) {
            pos = str.IndexOf(substr, (pos + 1));
        }
        return pos;
    }//end ordinal Index of


    public static async Task

    <String> GetHtmlFromURL(String urlAddress) {

        String data = "";
        try {
            using(var cli = new HttpClient())
            {
                HttpResponseMessage response = await cli.GetAsync(urlAddress);
                if (response.IsSuccessStatusCode)
                    data = await response.Content.ReadAsStringAsync();
                //Debug.WriteLine(data);
            }
        } catch (Exception ex) {
            return ex.Message;
        }
        return data;
    }//end get HTML

}
