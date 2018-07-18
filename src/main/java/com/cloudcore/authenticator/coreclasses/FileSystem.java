package com.cloudcore.authenticator.coreclasses;

import com.cloudcore.authenticator.Formats;
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
        ExportFolder = RootPath + File.separator + Config.TAG_EXPORT + File.separator;
        ImportedFolder = RootPath + File.separator + Config.TAG_IMPORTED + File.separator;
        TemplateFolder = RootPath + File.separator + Config.TAG_TEMPLATES + File.separator;
        LanguageFolder = RootPath + File.separator + Config.TAG_LANGUAGE + File.separator;
        CounterfeitFolder = RootPath + File.separator + Config.TAG_COUNTERFEIT + File.separator;
        PartialFolder = RootPath + File.separator + Config.TAG_PARTIAL + File.separator;
        FrackedFolder = RootPath + File.separator + Config.TAG_FRACKED + File.separator;
        DetectedFolder = RootPath + File.separator + Config.TAG_DETECTED + File.separator;
        SuspectFolderOld = RootPath + File.separator + Config.TAG_SUSPECT_OLD + File.separator;
        TrashFolder = RootPath + File.separator + Config.TAG_TRASH + File.separator;
        BankFolder = RootPath + File.separator + Config.TAG_BANK + File.separator;
        SuspectFolder = RootPath + File.separator + Config.TAG_SUSPECT + File.separator;
        LostFolder = RootPath + File.separator + Config.TAG_LOST + File.separator;
        RequestsFolder = RootPath + File.separator + Config.TAG_REQUESTS + File.separator;
        DangerousFolder = RootPath + File.separator + Config.TAG_DANGEROUS + File.separator;
        LogsFolder = RootPath + File.separator + Config.TAG_LOGS + File.separator;
        QRFolder = ImportFolder + Config.TAG_QR;
        BarCodeFolder = ImportFolder + Config.TAG_BARCODE;
        CSVFolder = ImportFolder + Config.TAG_CSV;
    }

    public boolean CreateDirectories() {
        // Create Subdirectories as per the RootFolder Location
        // Failure will return false

        try {
            Files.createDirectories(Paths.get(RootPath));
            Files.createDirectories(Paths.get(ImportFolder));
            Files.createDirectories(Paths.get(ExportFolder));
            Files.createDirectories(Paths.get(BankFolder));
            Files.createDirectories(Paths.get(ImportedFolder));
            Files.createDirectories(Paths.get(LostFolder));
            Files.createDirectories(Paths.get(TrashFolder));
            Files.createDirectories(Paths.get(SuspectFolderOld));
            Files.createDirectories(Paths.get(DetectedFolder));
            Files.createDirectories(Paths.get(FrackedFolder));
            Files.createDirectories(Paths.get(TemplateFolder));
            Files.createDirectories(Paths.get(PartialFolder));
            Files.createDirectories(Paths.get(CounterfeitFolder));
            Files.createDirectories(Paths.get(LanguageFolder));
            Files.createDirectories(Paths.get(SuspectFolder));
            Files.createDirectories(Paths.get(RequestsFolder));
            Files.createDirectories(Paths.get(DangerousFolder));
            Files.createDirectories(Paths.get(LogsFolder));
            Files.createDirectories(Paths.get(QRFolder));
            Files.createDirectories(Paths.get(BarCodeFolder));
            Files.createDirectories(Paths.get(CSVFolder));
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

        for (int i = 0; i < files.length; i++) {
            MoveFile(files[i].getAbsolutePath(), ImportedFolder + File.separator + files[i].getName(), FileMoveOptions.Rename);
        }

            /* TODO: Deal with QRCode
            var filesqr = Directory
                    .GetFiles(ImportFolder + File.separator + "QrCodes")
                    .Where(file => Config.allowedExtensions.Any(file.toLowerCase().EndsWith))
              .ToList();

            String[] fnamesqr = new String[filesqr.count()];
            for (int i = 0; i < filesqr.count(); i++)
            {
                MoveFile(filesqr[i], ImportedFolder + File.separator + Path.GetFileName(filesqr[i]), FileMoveOptions.Rename);
            }*/

            /* TODO: Deal with Barcode
            var filesbar = Directory
                    .GetFiles(ImportFolder + File.separator + "Barcodes")
                    .Where(file => Config.allowedExtensions.Any(file.toLowerCase().EndsWith))
              .ToList();

            String[] fnamesbar = new String[filesbar.count()];
            for (int i = 0; i < filesbar.count(); i++)
            {
                MoveFile(filesbar[i], ImportedFolder + File.separator + Path.GetFileName(filesbar[i]), FileMoveOptions.Rename);
            }*/
    }

}

