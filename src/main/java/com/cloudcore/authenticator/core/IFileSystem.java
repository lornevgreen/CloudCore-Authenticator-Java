package com.cloudcore.authenticator.core;

import com.cloudcore.authenticator.utils.FileUtils;
import com.cloudcore.authenticator.utils.Utils;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public abstract class IFileSystem {

    public String RootPath;

    public String ImportFolder;
    public String ImportedFolder;
    public String DetectedFolder;
    public String SuspectFolder;

    public String BankFolder;
    public String FrackedFolder;
    public String CounterfeitFolder;
    public String LostFolder;

    public String LogsFolder;

    public static ArrayList<CloudCoin> importCoins;
    public static ArrayList<CloudCoin> predetectCoins;

    public abstract void LoadFileSystem();

    public ArrayList<CloudCoin> LoadFolderCoins(String folder) {
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

    public abstract void DetectPreProcessing();

    public void RemoveCoinsRealName(ArrayList<CloudCoin> coins, String folder) {
        for (CloudCoin coin : coins) {
            try {
                Files.deleteIfExists(Paths.get(folder + coin.currentFilename));
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    public void WriteCoin(ArrayList<CloudCoin> coins, String folder) {
        WriteCoin(coins, folder, false);
    }

    public void WriteCoin(ArrayList<CloudCoin> coins, String folder, boolean writeAll) {
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

        ArrayList<CloudCoin> folderCoins = LoadFolderCoins(folder);

        for (CloudCoin coin : coins) {
            String fileName = coin.FileName();
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
