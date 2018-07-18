package com.cloudcore.authenticator.coreclasses;

import com.cloudcore.authenticator.Formats;
import com.cloudcore.authenticator.core.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Override
    public boolean CreateFolderStructure() {
        // Create the Actual Folder Structure
        return CreateDirectories();
        //return true;
    }

    public void CopyTemplates() {
            /* TODO: see if this is necessary
            String[] fileNames = Assembly.GetExecutingAssembly().GetManifestResourceNames();
            for (String fileName : fileNames)
            {
                if (fileName.contains("jpeg") || fileName.contains("jpg"))
                {

                }
            }*/
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
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }


        return true;
    }

    @Override
    public void LoadFileSystem() {
        // TODO: See if these need re-enabled
        importCoins = LoadFolderCoins(ImportFolder);
        ArrayList<CloudCoin> csvCoins = LoadCoinsByFormat(ImportFolder + File.separator + "CSV", Formats.CSV);
        ArrayList<CloudCoin> qrCoins = LoadCoinsByFormat(ImportFolder + File.separator + "QrCodes", Formats.QRCode);
        ArrayList<CloudCoin> BarCodeCoins = LoadCoinsByFormat(ImportFolder + File.separator + "Barcodes", Formats.BarCode);

        // Add Additional File formats if present
        //importCoins = importCoins.Concat(csvCoins);
        importCoins.addAll(BarCodeCoins);
        importCoins.addAll(qrCoins);

        //exportCoins = LoadFolderCoins(ExportFolder);
        bankCoins = LoadFolderCoins(BankFolder);
        lostCoins = LoadFolderCoins(LostFolder);
        //importedCoins = LoadFolderCoins(ImportedFolder);
        //trashCoins = LoadFolderCoins(TrashFolder);
        suspectCoins = LoadFolderCoins(SuspectFolderOld);
        detectedCoins = LoadFolderCoins(DetectedFolder);
        frackedCoins = LoadFolderCoins(FrackedFolder);
        //LoadFolderCoins(TemplateFolder);
        partialCoins = LoadFolderCoins(PartialFolder);
        //counterfeitCoins = LoadFolderCoins(CounterfeitFolder);
        predetectCoins = LoadFolderCoins(SuspectFolder);
        dangerousCoins = LoadFolderCoins(DangerousFolder);

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
                e.printStackTrace();
            }
        }
    }

    @Override
    public void ProcessCoins(ArrayList<CloudCoin> coins) {

        ArrayList<CloudCoin> detectedCoins = LoadFolderCoins(DetectedFolder);


        for (CloudCoin coin : detectedCoins) {
            if (coin.getPassCount() >= Config.PassCount) {
                WriteCoin(coin, BankFolder);
            } else {
                WriteCoin(coin, CounterfeitFolder);
            }
        }
    }

    public String GetCoinName(String CoinName) {
        return CoinName;
    }

    public void TransferCoins(ArrayList<CloudCoin> coins, String sourceFolder, String targetFolder) {
        TransferCoins(coins, sourceFolder, targetFolder, ".stack");
    }

    public void TransferCoins(ArrayList<CloudCoin> coins, String sourceFolder, String targetFolder, String extension) {
        ArrayList<CloudCoin> folderCoins = LoadFolderCoins(targetFolder);

        for (CloudCoin coin : coins) {
            String fileName = GetCoinName(coin.FileName());
            try {
                Stack stack = new Stack(coin);
                try {
                    Files.write(Paths.get(targetFolder + fileName + extension), Utils.createGson().toJson(stack).getBytes(StandardCharsets.UTF_8));
                    Files.delete(Paths.get(sourceFolder + GetCoinName(coin.FileName()) + extension));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void ClearCoins(String FolderName) {
        File[] files = GetFilesArray(FolderName, Config.allowedExtensions);

        for (File file : files)
            try {
                file.setReadable(true);
                file.setWritable(true);
                file.delete();
            } catch (SecurityException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

    }

    public boolean WriteTextFile(String fileName, String text) {
        try {
            Path path = Paths.get(fileName);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            Files.write(path, text.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            // MainWindow.logger.Error(e.getMessage());
            return false;
        }
        return true;
    }

    public static File[] GetFilesArray(String path, String[] extensions) {
        final ArrayList<String> extensionsArray = new ArrayList<>(Arrays.asList(extensions));
        return new File(path).listFiles(pathname -> {
            String filename = pathname.getAbsolutePath();
            String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
            return extensionsArray.contains(extension);
        });
    }

    public static String[] GetFilesNamesArray(String path, String[] extensions) {
        File[] files = GetFilesArray(path, extensions);
        String[] filenames = new String[files.length];
        for (int i = 0; i < files.length; i++)
            filenames[i] = files[i].getName();
        return filenames;
    }

    public static ArrayList<File> GetFiles(String path, String[] extensions) {
        return new ArrayList<>(Arrays.asList(GetFilesArray(path, extensions)));
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

    @Override
    public boolean WriteCoinToJpeg(CloudCoin cloudCoin, String TemplateFile, String OutputFile, String tag) {
            /*  TODO: Deal with Barcode
        }
            OutputFile = OutputFile.replace("\\\\", "\\");
            boolean fileSavedSuccessfully = true;

            // BUILD THE CLOUDCOIN STRING //
            String cloudCoinStr = "01C34A46494600010101006000601D05"; //THUMBNAIL HEADER BYTES
            for (int i = 0; (i < 25); i++)
            {
                cloudCoinStr = cloudCoinStr + cloudCoin.an.get(i);
            } // end for each an

            //cloudCoinStr += "204f42455920474f4420262044454645415420545952414e545320";// Hex for " OBEY GOD & DEFEAT TYRANTS "
            //cloudCoinStr += "20466f756e6465727320372d352d3137";// Founders 7-5-17
            cloudCoinStr += "4c6976652046726565204f7220446965";// Live Free or Die
            cloudCoinStr += "00000000000000000000000000";//Set to unknown so program does not export user data
            // for (int i =0; i < 25; i++) {
            //     switch () { }//end switch pown char
            // }//end for each pown
            cloudCoinStr += "00"; // HC: Has comments. 00 = No
            cloudCoin.CalcExpirationDate();
            cloudCoinStr += cloudCoin.edHex; // 01;//Expiration date Sep 2016 (one month after zero month)
            cloudCoinStr += "01";//  cc.nn;//network number
            String hexSN = cloudCoin.getSn().ToString("X6");
            String fullHexSN = "";
            switch (hexSN.length())
            {
                case 1: fullHexSN = ("00000" + hexSN); break;
                case 2: fullHexSN = ("0000" + hexSN); break;
                case 3: fullHexSN = ("000" + hexSN); break;
                case 4: fullHexSN = ("00" + hexSN); break;
                case 5: fullHexSN = ("0" + hexSN); break;
                case 6: fullHexSN = hexSN; break;
            }
            cloudCoinStr = (cloudCoinStr + fullHexSN);
            // BYTES THAT WILL GO FROM 04 to 454 (Inclusive)//
            byte[] ccArray = this.hexStringToByteArray(cloudCoinStr);


            /* READ JPEG TEMPLATE//
            byte[] jpegBytes = null;

            //jpegBytes = readAllBytes(filePath);
            jpegBytes = File.ReadAllBytes(TemplateFile);

            /* WRITE THE SERIAL NUMBER ON THE JPEG //

            //Bitmap bitmapimage;
            //jpegBytes = readAllBytes(filePath);
            jpegBytes = File.ReadAllBytes(TemplateFile);

            /* WRITE THE SERIAL NUMBER ON THE JPEG //

            Bitmap bitmapimage;

            using (var ms = new MemoryStream(jpegBytes))
            {
                bitmapimage = new Bitmap(ms);
            }

            Graphics graphics = Graphics.FromImage(bitmapimage);
            graphics.SmoothingMode = SmoothingMode.AntiAlias;
            graphics.InterpolationMode = InterpolationMode.HighQualityBicubic;
            PointF drawPointAddress = new PointF(30.0F, 25.0F);
            graphics.DrawString(String.format("{0:N0}", cloudCoin.getSn()) + " of 16,777,216 on Network: 1", new Font("Arial", 10), Brushes.White, drawPointAddress);

            ImageConverter converter = new ImageConverter();
            byte[] snBytes = (byte[])converter.ConvertTo(bitmapimage, typeof(byte[]));

            ArrayList<byte> b1 = new ArrayList<byte>(snBytes);
            ArrayList<byte> b2 = new ArrayList<byte>(ccArray);
            b1.InsertRange(4, b2);

            if (tag == "random")
            {
                Random r = new Random();
                int rInt = r.Next(100000, 1000000); //for ints
                tag = rInt.toString();
            }

            //String fileName = targetPath;

            String fileName = ExportFolder + cloudCoin.FileName + ".jpg";
            File.WriteAllBytes(OutputFile, b1.toArray());
            //System.out.println("Writing to " + fileName);
            //CoreLogger.Log("Writing to " + fileName);

            return fileSavedSuccessfully;
            */
        return false;
    }

    @Override
    public boolean WriteCoinToQRCode(CloudCoin cloudCoin, String OutputFile, String tag) {
            /*  TODO: Deal with QR Code
            int width = 250; // width of the Qr Code
            int height = 250; // height of the Qr Code
            int margin = 0;
            var qrCodeWriter = new ZXing.BarcodeWriterPixelData
            {
                Format = ZXing.BarcodeFormat.QR_CODE,
                        Options = new QrCodeEncodingOptions
                {
                    Height = height,
                            Width = width,
                            Margin = margin
                }
            };
            String coinJson = JsonConvert.SerializeObject(cloudCoin);
            var pixelData = qrCodeWriter.Write(coinJson);
            // creating a bitmap from the raw pixel data; if only black and white colors are used it makes no difference
            // that the pixel data ist BGRA oriented and the bitmap is initialized with RGB
            using (var bitmap = new System.Drawing.Bitmap(pixelData.Width, pixelData.Height, System.Drawing.Imaging.PixelFormatFormat32bppRgb))
            using (var ms = new MemoryStream())
            {
                var bitmapData = bitmap.LockBits(new System.Drawing.Rectangle(0, 0, pixelData.Width, pixelData.Height), System.Drawing.Imaging.ImageLockMode.WriteOnly, System.Drawing.Imaging.PixelFormatFormat32bppRgb);
                try
                {
                    // we assume that the row stride of the bitmap is aligned to 4 byte multiplied by the width of the image
                    System.Runtime.InteropServices.Marshal.Copy(pixelData.Pixels, 0, bitmapData.Scan0, pixelData.Pixels.length);
                }
                finally
                {
                    bitmap.UnlockBits(bitmapData);
                }
                // save to stream as PNG
                bitmap.Save(ms, System.Drawing.Imaging.ImageFormat.Jpeg);
                bitmap.Save(OutputFile);
            }


            return true;
            */
        return false;
    }

    @Override
    public boolean WriteCoinToBARCode(CloudCoin cloudCoin, String OutputFile, String tag) {
            /*  TODO: Deal with Barcode
            var writer = new BarcodeWriter
            {
                Format = BarcodeFormat.PDF_417,
                        Options = new EncodingOptions { Width = 200, Height = 50 } //optional
            };
            cloudCoin.pan = null;
            var coinJson = JsonConvert.SerializeObject(cloudCoin);
            var imgBitmap = writer.Write(coinJson);
            using (var stream = new MemoryStream())
            {
                imgBitmap.Save(stream, ImageFormat.Png);
                stream.toArray();
                imgBitmap.Save(OutputFile);
            }
            return true;
            */
        return false;
    }
}

