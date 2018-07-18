package com.cloudcore.authenticator.core;

import com.cloudcore.authenticator.Formats;
import com.cloudcore.authenticator.utils.FileUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public abstract class IFileSystem {

    public enum FileMoveOptions {Replace, Rename}

    public String RootPath;
    public String ImportFolder;
    public String ExportFolder;
    public String BankFolder;
    public String ImportedFolder;
    public String LostFolder;
    public String TrashFolder;
    public String SuspectFolderOld;
    public String DetectedFolder;
    public String FrackedFolder;
    public String TemplateFolder;
    public String PartialFolder;
    public String CounterfeitFolder;
    public String LanguageFolder;
    public String SuspectFolder;
    public String RequestsFolder;
    public String DangerousFolder;
    public String LogsFolder;
    public String QRFolder;
    public String BarCodeFolder;
    public String CSVFolder;

    //public abstract IFileSystem(String path);

    public static ArrayList<CloudCoin> importCoins;
    public static ArrayList<CloudCoin> predetectCoins;

    public abstract void LoadFileSystem();

    public ArrayList<CloudCoin> LoadCoinsByFormat(String folder, Formats format) {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();
        CloudCoin coin;

        String allowedExtension = "";
        switch (format) {
            case CSV:
                allowedExtension = "csv";
                break;
            case BarCode:
            case QRCode:
                allowedExtension = "jpg";
                break;
        }

        String[] fileNames = FileUtils.selectFileNamesInFolder(folder);
        String extension;
        for (String fileName : fileNames) {
            int index = fileName.lastIndexOf('.');
            if (index > 0) {
                extension = fileName.substring(index + 1);

                if (allowedExtension.equalsIgnoreCase(extension)) {
                    switch (format) {
                        case CSV:
                            ArrayList<CloudCoin> csvCoins = ReadCSVCoins(fileName);
                            csvCoins.remove(null);
                            folderCoins.addAll(csvCoins);
                            break;
                        case BarCode:
                            //  TODO: Deal with Barcode
                            //coin = ReadBARCode(fileName);
                            //folderCoins.add(coin);
                            break;
                        case QRCode:
                            //  TODO: Deal with QR Code
                            //coin = ReadQRCode(fileName);
                            //folderCoins.add(coin);
                            break;
                    }
                }
            }
        }

        return folderCoins;
    }

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
                    //case "jpg":
                    //case "jpeg":
                    //    CloudCoin coin = importJPEG(fileName);
                    //    folderCoins.add(coin);
                    //    break;
                    //case "csv":
                    //    ArrayList<String> lines;
                    //    try {
                    //        ArrayList<CloudCoin> csvCoins = new ArrayList<>();
                    //        lines = new ArrayList<>(Files.readAllLines(Paths.get(fileName)));
                    //        for (String line : lines) {
                    //            csvCoins.add(CloudCoin.FromCSV(line));
                    //        }
                    //        csvCoins.remove(null);
                    //        folderCoins.addAll(csvCoins);
                    //    } catch (IOException e) {
                    //        e.printStackTrace();
                    //    }
                    //    break;
                }
            }
        }

        return folderCoins;
    }

    private ArrayList<CloudCoin> ReadCSVCoins(String fileName)//Read a CloudCoin from CSV .
    {
        ArrayList<CloudCoin> cloudCoins = new ArrayList<>();
        ArrayList<String> lines;
        try {
            lines = new ArrayList<>(Files.readAllLines(Paths.get(fileName)));
            //var lines = File.ReadAllLines(fileName).Select(a => a.split(","));
            for (String line : lines)
                cloudCoins.add(CloudCoin.FromCSV(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cloudCoins;
    }

    public CloudCoin LoadCoin(String fileName) {
        CloudCoin[] coins = Utils.LoadJson(fileName);

        if (coins != null && coins.length > 0)
            return coins[0];
        return null;
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

    // en d json test
    public String setJSON(CloudCoin cc) {
        final String quote = "\"";
        final String tab = "\t";
        String json = (tab + tab + "{ " + System.lineSeparator());// {
        json += tab + tab + quote + "nn" + quote + ":" + quote + cc.nn + quote + ", " + System.lineSeparator();// "nn":"1",
        json += tab + tab + quote + "sn" + quote + ":" + quote + cc.getSn() + quote + ", " + System.lineSeparator();// "sn":"367544",
        json += tab + tab + quote + "an" + quote + ": [" + quote;// "an": ["
        for (int i = 0; (i < 25); i++) {
            json += cc.an.get(i);// 8551995a45457754aaaa44
            if (i == 4 || i == 9 || i == 14 || i == 19) {
                json += quote + "," + System.lineSeparator() + tab + tab + tab + quote; //",
            } else if (i == 24) {
                // json += "\""; last one do nothing
            } else { // end if is line break
                json += quote + ", " + quote;
            }

            // end else
        }// end for 25 ans

        json += quote + "]," + System.lineSeparator();//"],
        // End of ans
        //CoinUtils cu = new CoinUtils(cc);
        //cu.calcExpirationDate();
        cc.CalcExpirationDate();
        json += tab + tab + quote + "ed" + quote + ":" + quote + cc.ed + quote + "," + System.lineSeparator(); // "ed":"9-2016",
        if (cc.pown == null || cc.pown.length() == 0) {
            cc.pown = "uuuuuuuuuuuuuuuuuuuuuuuuu";
        }//Set pown to unknow if it is not set.
        json += tab + tab + quote + "pown" + quote + ":" + quote + cc.pown + quote + "," + System.lineSeparator();// "pown":"uuupppppffpppppfuuf",
        json += tab + tab + quote + "aoid" + quote + ": []" + System.lineSeparator();
        json += tab + tab + "}" + System.lineSeparator();
        // Keep expiration date when saving (not a truley accurate but good enought )
        return json;
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


    public String bytesToHexString(byte[] data) {
        int length = data.length;
        char[] hex = new char[length * 2];
        int num1 = 0;
        for (int index = 0; index < length * 2; index += 2) {
            byte num2 = data[num1++];
            hex[index] = GetHexValue(num2 / 0x10);
            hex[index + 1] = GetHexValue(num2 % 0x10);
        }
        return new String(hex);
    }//End NewConverted//

    private char GetHexValue(int i) {
        if (i < 10) {
            return (char) (i + 0x30);
        }
        return (char) ((i - 10) + 0x41);
    }//end GetHexValue

    /* Writes a JPEG To the Export Folder */

    public boolean writeTo(String folder, CloudCoin cc) {
        //CoinUtils cu = new CoinUtils(cc);
        final String quote = "\"";
        final String tab = "\t";
        String wholeJson = "{" + System.lineSeparator(); //{
        boolean alreadyExists = true;
        String json = this.setJSON(cc);

        try {
            if (!Files.exists(Paths.get(folder + cc.FileName() + ".stack"))) {
                wholeJson += tab + quote + "cloudcoin" + quote + ": [" + System.lineSeparator(); // "cloudcoin" : [
                wholeJson += json;
                wholeJson += System.lineSeparator() + tab + "]" + System.lineSeparator() + "}";
                alreadyExists = true;
                Files.write(Paths.get(folder + cc.FileName() + ".stack"), wholeJson.getBytes(StandardCharsets.UTF_8));
            } else {
                if (folder.contains("Counterfeit") || folder.contains("Trash")) {
                    //Let the program delete it
                    alreadyExists = false;
                    return alreadyExists;
                } else if (folder.contains("Imported")) {
                    alreadyExists = false;
                    Files.delete(Paths.get(folder + cc.FileName() + ".stack"));
                    Files.write(Paths.get(folder + cc.FileName() + ".stack"), wholeJson.getBytes(StandardCharsets.UTF_8));
                    return alreadyExists;
                } else {
                    System.out.println(cc.FileName() + ".stack" + " already exists in the folder " + folder);
                    //CoreLogger.Log(cu.fileName + ".stack" + " already exists in the folder " + folder);
                    return alreadyExists;
                }//end else
            }//File Exists
            // TODO: Should not write text twice
            Files.write(Paths.get(folder + cc.FileName() + ".stack"), wholeJson.getBytes(StandardCharsets.UTF_8));
            alreadyExists = false;
            return alreadyExists;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }//End Write To

}
