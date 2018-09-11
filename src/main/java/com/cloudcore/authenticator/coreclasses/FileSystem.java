package com.cloudcore.authenticator.coreclasses;

import com.cloudcore.authenticator.core.*;
import com.cloudcore.authenticator.utils.CoinUtils;
import com.cloudcore.authenticator.utils.FileUtils;
import com.cloudcore.authenticator.utils.Utils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileSystem {


    /* Fields */

    public static String RootPath = "C:" + File.separator + "CloudCoins-Authenticate" + File.separator;

    public static String DetectedFolder = RootPath + Config.TAG_DETECTED + File.separator;
    public static String ImportFolder = RootPath + Config.TAG_IMPORT + File.separator;
    public static String SuspectFolder = RootPath + Config.TAG_SUSPECT + File.separator;

    public static String LogsFolder = RootPath + Config.TAG_LOGS + File.separator;

    public static ArrayList<CloudCoin> importCoins;
    public static ArrayList<CloudCoin> predetectCoins;


    /* Methods */

    public static boolean createDirectories() {
        try {
            Files.createDirectories(Paths.get(RootPath));

            Files.createDirectories(Paths.get(ImportFolder));
            Files.createDirectories(Paths.get(DetectedFolder));
            Files.createDirectories(Paths.get(SuspectFolder));

            Files.createDirectories(Paths.get(LogsFolder));
        } catch (Exception e) {
            System.out.println("FS#CD: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void loadFileSystem() {
        importCoins = loadFolderCoins(ImportFolder);
        predetectCoins = loadFolderCoins(SuspectFolder);
    }

    public static void detectPreProcessing() {
        for (CloudCoin coin : importCoins) {
            String fileName = CoinUtils.generateFilename(coin);
            int coinExists = 0;
            for (CloudCoin folderCoin : predetectCoins)
                if (folderCoin.getSn() == coin.getSn())
                    coinExists++;
            //int coinExists = (int) Arrays.stream(predetectCoins.toArray(new CloudCoin[0])).filter(x -> x.getSn() == coin.getSn()).count();

            //if (coinExists > 0)
            //{
            //    String suffix = Utils.randomString(16);
            //    fileName += suffix.toLowerCase();
            //}

            Stack stack = new Stack(coin);
            try {
                Files.write(Paths.get(SuspectFolder + fileName + ".stack"), Utils.createGson().toJson(stack).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("FS#DPP: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<CloudCoin> loadFolderCoins(String folder) {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();

        String[] fileNames = FileUtils.selectFileNamesInFolder(folder);
        String extension;
        for (String fileName : fileNames) {
            int index = fileName.lastIndexOf('.');
            if (index > 0) {
                extension = fileName.substring(index + 1);
                fileName = folder + fileName;

                switch (extension) {
                    case "celeb":
                    case "celebrium":
                    case "stack":
                        ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromStack(fileName);
                        folderCoins.addAll(coins);
                        break;
                }
            }
        }

        return folderCoins;
    }

    public static void removeCoinsRealName(ArrayList<CloudCoin> coins, String folder) {
        for (CloudCoin coin : coins) {
            try {
                Files.deleteIfExists(Paths.get(folder + coin.currentFilename));
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    public static void writeCoin(ArrayList<CloudCoin> coins, String folder) {
        writeCoin(coins, folder, false);
    }

    public static void writeCoin(ArrayList<CloudCoin> coins, String folder, boolean writeAll) {
        if (writeAll) {
            String file = folder + FileUtils.randomString(16) + ".stack";
            try {
                Stack stack = new Stack(coins);
                Gson gson = Utils.createGson();
                Files.write(Paths.get(file), gson.toJson(stack).getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        ArrayList<CloudCoin> folderCoins = loadFolderCoins(folder);

        for (CloudCoin coin : coins) {
            String fileName = CoinUtils.generateFilename(coin);;
            int coinExists = 0;
            for (CloudCoin folderCoin : folderCoins)
                if (folderCoin.getSn() == coin.getSn())
                    coinExists++;
            //int coinExists = (int) Arrays.stream(folderCoins.toArray(new CloudCoin[0])).filter(x -> x.getSn() == coin.getSn()).count();

            if (coinExists > 0) {
                String suffix = FileUtils.randomString(16);
                fileName += suffix.toLowerCase();
            }

            Stack stack = new Stack(coin);
            try {
                Files.write(Paths.get(folder + fileName + ".stack"), Utils.createGson().toJson(stack).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

