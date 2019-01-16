package com.cloudcore.authenticator.core;

import com.cloudcore.authenticator.core.*;
import com.cloudcore.authenticator.utils.CoinUtils;
import com.cloudcore.authenticator.utils.FileUtils;
import com.cloudcore.authenticator.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class FileSystem {


    /* Fields */

    public static String RootPath = "C:\\Users\\Public\\Documents\\CloudCoin\\";

    public static String DetectedFolder = RootPath + Config.TAG_DETECTED + File.separator;
    public static String ImportFolder = RootPath + Config.TAG_IMPORT + File.separator;
    public static String SuspectFolder = RootPath + Config.TAG_SUSPECT + File.separator;

    public static String LogsFolder = RootPath + Config.TAG_LOGS + File.separator + Config.MODULE_NAME + File.separator;

    public static ArrayList<CloudCoin> importCoins;
    public static ArrayList<CloudCoin> predetectCoins;


    /* Methods */

    public static boolean createDirectories() {
        try {
            Files.createDirectories(Paths.get(RootPath));

            Files.createDirectories(Paths.get(DetectedFolder));
            Files.createDirectories(Paths.get(ImportFolder));
            Files.createDirectories(Paths.get(SuspectFolder));

            Files.createDirectories(Paths.get(LogsFolder));
        } catch (Exception e) {
            System.out.println("FS#CD: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void changeRootPath(String rootPath) {
        RootPath = rootPath;
        DetectedFolder = RootPath + Config.TAG_DETECTED + File.separator;
        ImportFolder = RootPath + Config.TAG_IMPORT + File.separator;
        SuspectFolder = RootPath + Config.TAG_SUSPECT + File.separator;

        LogsFolder = RootPath + Config.TAG_LOGS + File.separator + Config.MODULE_NAME + File.separator;
    }

    public static void loadFileSystem() {
        importCoins = loadFolderCoins(ImportFolder);
        predetectCoins = loadFolderCoins(SuspectFolder);
    }

    public static void detectPreProcessing() {
        for (CloudCoin coin : importCoins) {
            String fileName = CoinUtils.generateFilename(coin);

            Stack stack = new Stack(coin);
            try {
                Files.write(Paths.get(SuspectFolder + fileName + ".stack"), Utils.createGson().toJson(stack).getBytes(StandardCharsets.UTF_8));
                Files.deleteIfExists(Paths.get(ImportFolder + coin.currentFilename));
            } catch (IOException e) {
                System.out.println("FS#DPP: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads all CloudCoins from a specific folder.
     *
     * @param folder the folder to search for CloudCoin files.
     * @return an ArrayList of all CloudCoins in the specified folder.
     */
    public static ArrayList<CloudCoin> loadFolderCoins(String folder) {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();

        String[] filenames = FileUtils.selectFileNamesInFolder(folder);
        for (String filename : filenames) {
            int index = filename.lastIndexOf('.');
            if (index == -1) continue;

            String extension = filename.substring(index + 1);

            switch (extension) {
                case "stack":
                    ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromStack(folder, filename);
                    folderCoins.addAll(coins);
                    break;
            }
        }

        return folderCoins;
    }

    public static void moveCoin(CloudCoin coin, String sourceFolder, String targetFolder, String extension) {
        String fileName = FileUtils.ensureFilenameUnique(CoinUtils.generateFilename(coin), extension, targetFolder);

        try {
            Files.move(Paths.get(sourceFolder + coin.currentFilename), Paths.get(targetFolder + fileName),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}

