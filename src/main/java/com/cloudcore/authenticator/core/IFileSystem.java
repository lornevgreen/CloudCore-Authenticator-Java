package com.cloudcore.authenticator.core;

import com.cloudcore.authenticator.utils.FileUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public abstract class IFileSystem {

    public enum FileMoveOptions {Replace, Rename}

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
                        ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromJSON(fileName);
                        if (coins != null)
                            folderCoins.addAll(coins);
                        break;
                }
            }
        }

        return folderCoins;
    }

    public abstract void DetectPreProcessing();


    public void MoveFile(String SourcePath, String TargetPath, FileMoveOptions options) {
        try {
            if (!Files.exists(Paths.get(TargetPath))) {
                Files.move(Paths.get(SourcePath), Paths.get(TargetPath));
            } else {
                if (options == FileMoveOptions.Replace) {
                    Files.delete(Paths.get(TargetPath));
                    Files.move(Paths.get(SourcePath), Paths.get(TargetPath));
                }
                if (options == FileMoveOptions.Rename) {
                    String targetFileName = SourcePath.substring(SourcePath.lastIndexOf(File.separator) + 1, SourcePath.lastIndexOf('.'));
                    targetFileName += Utils.RandomString(8).toLowerCase() + ".stack";
                    String targetPath = TargetPath + File.separator + targetFileName;
                    Files.move(Paths.get(SourcePath), Paths.get(targetPath));

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // end get JSON

    public abstract void MoveImportedFiles();

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

    public void RemoveCoins(ArrayList<CloudCoin> coins, String folder) {
        RemoveCoins(coins, folder, ".stack");
    }
    public void RemoveCoins(ArrayList<CloudCoin> coins, String folder, String extension) {
        for (CloudCoin coin : coins) {
            try {
                System.out.println("deleting" + folder + coin.currentFilename + extension);
                Files.deleteIfExists(Paths.get(folder + coin.currentFilename + extension));
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    public void MoveCoins(ArrayList<CloudCoin> coins, String sourceFolder, String targetFolder) {
        MoveCoins(coins, sourceFolder, targetFolder, ".stack", false);
    }

    public void MoveCoins(ArrayList<CloudCoin> coins, String sourceFolder, String targetFolder, String extension, boolean replaceCoins) {
        ArrayList<CloudCoin> folderCoins = LoadFolderCoins(targetFolder);

        for (CloudCoin coin : coins) {
            String fileName = (coin.FileName());
            int coinExists = 0;
            for (CloudCoin folderCoin : folderCoins)
                if (folderCoin.getSn() == coin.getSn())
                    coinExists++;
            //int coinExists = (int) Arrays.stream(folderCoins.toArray(new CloudCoin[0])).filter(x -> x.getSn() == coin.getSn()).count();

            if (coinExists > 0 && !replaceCoins) {
                String suffix = Utils.RandomString(16);
                fileName += suffix.toLowerCase();
            }
            try {
                Gson gson = Utils.createGson();
                Stack stack = new Stack(coin);
                Files.write(Paths.get(targetFolder + fileName + extension), gson.toJson(stack).getBytes(StandardCharsets.UTF_8));
                Files.deleteIfExists(Paths.get(sourceFolder + coin.currentFilename));
            } catch (Exception e) {
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
            String file = folder + Utils.RandomString(16) + ".stack";
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
                String suffix = Utils.RandomString(16);
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


    /* Writes a JPEG To the Export Folder */

}
