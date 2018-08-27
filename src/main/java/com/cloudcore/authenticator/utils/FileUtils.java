package com.cloudcore.authenticator.utils;

import com.cloudcore.authenticator.core.CloudCoin;
import com.cloudcore.authenticator.core.Stack;
import com.google.gson.JsonSyntaxException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class FileUtils {


    /* Fields */

    private static Random random = new Random();
    private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";


    /* Methods */

    public static String randomString(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

    /**
     * Loads an array of CloudCoins from a Stack file.
     *
     * @param fullFilePath the absolute filepath of the Stack file.
     * @return ArrayList of CloudCoins.
     */
    public static ArrayList<CloudCoin> loadCloudCoinsFromStack(String fullFilePath) {
        try {
            String file = new String(Files.readAllBytes(Paths.get(fullFilePath)));
            Stack stack = Utils.createGson().fromJson(file, Stack.class);
            for (CloudCoin coin : stack.cc)
                coin.setFullFilePath(fullFilePath);
            return new ArrayList<>(Arrays.asList(stack.cc));
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Returns an array containing all filenames in a directory.
     *
     * @param folderPath the directory to check for files
     * @return String[]
     */
    public static String[] selectFileNamesInFolder(String folderPath) {
        File folder = new File(folderPath);
        Collection<String> files = new ArrayList<>();
        if (folder.isDirectory()) {
            File[] filenames = folder.listFiles();

            if (null != filenames) {
                for (File file : filenames) {
                    if (file.isFile()) {
                        files.add(file.getName());
                    }
                }
            }
        }
        return files.toArray(new String[]{});
    }
}
