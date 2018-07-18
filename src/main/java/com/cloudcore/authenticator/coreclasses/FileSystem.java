package com.cloudcore.authenticator.coreclasses;

import com.cloudcore.authenticator.core.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class FileSystem extends IFileSystem {

    public FileSystem(String RootPath) {
        this.RootPath = RootPath;

        ImportFolder = RootPath + File.separator + Config.TAG_IMPORT + File.separator;
        ImportedFolder = RootPath + File.separator + Config.TAG_IMPORTED + File.separator;
        DetectedFolder = RootPath + File.separator + Config.TAG_DETECTED + File.separator;
        SuspectFolder = RootPath + File.separator + Config.TAG_SUSPECT + File.separator;

        BankFolder = RootPath + File.separator + Config.TAG_BANK + File.separator;
        FrackedFolder = RootPath + File.separator + Config.TAG_FRACKED + File.separator;
        CounterfeitFolder = RootPath + File.separator + Config.TAG_COUNTERFEIT + File.separator;
        LostFolder = RootPath + File.separator + Config.TAG_LOST + File.separator;

        LogsFolder = RootPath + File.separator + Config.TAG_LOGS + File.separator;
    }

    public boolean CreateDirectories() {
        // Create Subdirectories as per the RootFolder Location
        // Failure will return false

        try {
            Files.createDirectories(Paths.get(RootPath));

            Files.createDirectories(Paths.get(ImportFolder));
            Files.createDirectories(Paths.get(ImportedFolder));
            Files.createDirectories(Paths.get(DetectedFolder));
            Files.createDirectories(Paths.get(SuspectFolder));

            Files.createDirectories(Paths.get(BankFolder));
            Files.createDirectories(Paths.get(FrackedFolder));
            Files.createDirectories(Paths.get(CounterfeitFolder));
            Files.createDirectories(Paths.get(LostFolder));

            Files.createDirectories(Paths.get(LogsFolder));
        } catch (Exception e) {
            System.out.println("FS#CD: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void LoadFileSystem() {
        importCoins = LoadFolderCoins(ImportFolder);
        predetectCoins = LoadFolderCoins(SuspectFolder);
    }

    @Override
    public void DetectPreProcessing() {
        for (CloudCoin coin : importCoins) {
            String fileName = GetCoinName(coin.FileName());
            int coinExists = 0;
            for (CloudCoin folderCoin : predetectCoins)
                if (folderCoin.getSn() == coin.getSn())
                    coinExists++;
            //int coinExists = (int) Arrays.stream(predetectCoins.toArray(new CloudCoin[0])).filter(x -> x.getSn() == coin.getSn()).count();

            //if (coinExists > 0)
            //{
            //    String suffix = Utils.RandomString(16);
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

    public String GetCoinName(String CoinName) {
        return CoinName;
    }

    public static File[] GetFilesArray(String path, String[] extensions) {
        final ArrayList<String> extensionsArray = new ArrayList<>(Arrays.asList(extensions));
        return new File(path).listFiles(pathname -> {
            String filename = pathname.getAbsolutePath();
            String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
            return extensionsArray.contains(extension);
        });
    }

    @Override
    public void MoveImportedFiles() {
        File[] files = GetFilesArray(ImportedFolder, Config.allowedExtensions);

        for (File file : files) {
            MoveFile(file.getAbsolutePath(), ImportedFolder + File.separator + file.getName(), FileMoveOptions.Rename);
        }
    }
}

