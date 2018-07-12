package com.cloudcore.authenticator.coreclasses;

import com.cloudcore.authenticator.Formats;
import com.cloudcore.authenticator.core.CloudCoin;
import com.cloudcore.authenticator.core.Config;
import com.cloudcore.authenticator.core.IFileSystem;
import com.cloudcore.authenticator.core.Stack;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileSystem extends IFileSystem {

        public FileSystem(String RootPath)
        {
            this.RootPath = RootPath;
            ImportFolder = RootPath + File.pathSeparator + Config.TAG_IMPORT + File.pathSeparator;
            ExportFolder = RootPath + File.pathSeparator + Config.TAG_EXPORT + File.pathSeparator;
            ImportedFolder = RootPath + File.pathSeparator + Config.TAG_IMPORTED + File.pathSeparator;
            TemplateFolder = RootPath + File.pathSeparator + Config.TAG_TEMPLATES + File.pathSeparator;
            LanguageFolder = RootPath + File.pathSeparator + Config.TAG_LANGUAGE + File.pathSeparator;
            CounterfeitFolder = RootPath + File.pathSeparator + Config.TAG_COUNTERFEIT + File.pathSeparator;
            PartialFolder = RootPath + File.pathSeparator + Config.TAG_PARTIAL + File.pathSeparator;
            FrackedFolder = RootPath + File.pathSeparator + Config.TAG_FRACKED + File.pathSeparator;
            DetectedFolder = RootPath + File.pathSeparator + Config.TAG_DETECTED + File.pathSeparator;
            SuspectFolder = RootPath + File.pathSeparator + Config.TAG_SUSPECT + File.pathSeparator;
            TrashFolder = RootPath + File.pathSeparator + Config.TAG_TRASH + File.pathSeparator;
            BankFolder = RootPath + File.pathSeparator + Config.TAG_BANK + File.pathSeparator;
            PreDetectFolder = RootPath + File.pathSeparator + Config.TAG_PREDETECT + File.pathSeparator;
            LostFolder = RootPath + File.pathSeparator + Config.TAG_LOST + File.pathSeparator;
            RequestsFolder = RootPath + File.pathSeparator + Config.TAG_REQUESTS + File.pathSeparator;
            DangerousFolder = RootPath + File.pathSeparator + Config.TAG_DANGEROUS + File.pathSeparator;
            LogsFolder = RootPath + File.pathSeparator + Config.TAG_LOGS + File.pathSeparator;
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

        public void CopyTemplates()
        {
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
                Files.createDirectory(Paths.get(RootPath));
                Files.createDirectory(Paths.get(RootPath));
                Files.createDirectory(Paths.get(ImportFolder));
                Files.createDirectory(Paths.get(ExportFolder));
                Files.createDirectory(Paths.get(BankFolder));
                Files.createDirectory(Paths.get(ImportedFolder));
                Files.createDirectory(Paths.get(LostFolder));
                Files.createDirectory(Paths.get(TrashFolder));
                Files.createDirectory(Paths.get(SuspectFolder));
                Files.createDirectory(Paths.get(DetectedFolder));
                Files.createDirectory(Paths.get(FrackedFolder));
                Files.createDirectory(Paths.get(TemplateFolder));
                Files.createDirectory(Paths.get(PartialFolder));
                Files.createDirectory(Paths.get(CounterfeitFolder));
                Files.createDirectory(Paths.get(LanguageFolder));
                Files.createDirectory(Paths.get(PreDetectFolder));
                Files.createDirectory(Paths.get(RequestsFolder));
                Files.createDirectory(Paths.get(DangerousFolder));
                Files.createDirectory(Paths.get(LogsFolder));
                Files.createDirectory(Paths.get(QRFolder));
                Files.createDirectory(Paths.get(BarCodeFolder));
                Files.createDirectory(Paths.get(CSVFolder));
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
                return false;
            }


            return true;
        }

        @Override
        public void LoadFileSystem()
        {
            // TODO: See if these need re-enabled
            importCoins = LoadFolderCoins(ImportFolder);
            ArrayList<CloudCoin> csvCoins = LoadCoinsByFormat(ImportFolder +File.pathSeparator + "CSV", Formats.CSV);
            ArrayList<CloudCoin> qrCoins = LoadCoinsByFormat(ImportFolder + File.pathSeparator + "QrCodes", Formats.QRCode);
            ArrayList<CloudCoin> BarCodeCoins = LoadCoinsByFormat(ImportFolder + File.pathSeparator + "Barcodes", Formats.BarCode);

            // Add Additional File formats if present
            //importCoins = importCoins.Concat(csvCoins);
            importCoins.addAll(BarCodeCoins);
            importCoins.addAll(qrCoins);

            System.out.println("Count -" + importCoins.size());

            //exportCoins = LoadFolderCoins(ExportFolder);
            bankCoins = LoadFolderCoins(BankFolder);
            lostCoins = LoadFolderCoins(LostFolder);
            //importedCoins = LoadFolderCoins(ImportedFolder);
            //trashCoins = LoadFolderCoins(TrashFolder);
            suspectCoins = LoadFolderCoins(SuspectFolder);
            detectedCoins = LoadFolderCoins(DetectedFolder);
            frackedCoins = LoadFolderCoins(FrackedFolder);
            //LoadFolderCoins(TemplateFolder);
            partialCoins = LoadFolderCoins(PartialFolder);
            //counterfeitCoins = LoadFolderCoins(CounterfeitFolder);
            predetectCoins = LoadFolderCoins(PreDetectFolder);
            dangerousCoins = LoadFolderCoins(DangerousFolder);

        }


    @Override
        public void DetectPreProcessing()
        {
            for (CloudCoin coin : importCoins)
            {
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
                    Files.write(Paths.get(PreDetectFolder + fileName + ".stack"), new Gson().toJson(stack).getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    @Override
        public void ProcessCoins(ArrayList<CloudCoin> coins)
        {

            ArrayList<CloudCoin> detectedCoins = LoadFolderCoins(DetectedFolder);


            for (CloudCoin coin : detectedCoins)
            {
                if (coin.getPassCount() >= Config.PassCount)
                {
                    WriteCoin(coin, BankFolder);
                }
                else
                {
                    WriteCoin(coin, CounterfeitFolder);
                }
            }
        }

        public String GetCoinName(String CoinName)
        {
            return CoinName;
        }

        public void TransferCoins(ArrayList<CloudCoin> coins, String sourceFolder, String targetFolder) {
            TransferCoins(coins, sourceFolder, targetFolder, ".stack");
        }
        public void TransferCoins(ArrayList<CloudCoin> coins, String sourceFolder, String targetFolder,String extension)
        {
            ArrayList<CloudCoin> folderCoins = LoadFolderCoins(targetFolder);

            for (CloudCoin coin : coins)
            {
                String fileName = GetCoinName(coin.FileName());
                try
                {
                    Stack stack = new Stack(coin);
                    try {
                        Files.write(Paths.get(targetFolder + fileName + extension), new Gson().toJson(stack).getBytes(StandardCharsets.UTF_8));
                        Files.delete(Paths.get(sourceFolder + GetCoinName(coin.FileName()) + extension));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                catch (Exception e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }




    @Override
        public void ClearCoins(String FolderName)
        {

            var fii = GetFiles(FolderName, Config.allowedExtensions);

            DirectoryInfo di = new DirectoryInfo(FolderName);


            for (FileInfo file : fii)
            try
            {
                file.Attributes = FileAttributes.Normal;
                File.Delete(file.FullName);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }

        }

        public boolean WriteTextFile(String fileName,String text)
        {
            try
            {
                Path path = Paths.get(fileName);
                if (!Files.exists(path))
                    Files.createFile(path);

                Files.write(path, text.getBytes(StandardCharsets.UTF_8));
            }
            catch(Exception e)
            {
                e.printStackTrace();
                // MainWindow.logger.Error(e.getMessage());
                return false;
            }
            return true;
        }
        public ArrayList<FileInfo> GetFiles(String path, params String[] extensions)
        {
            ArrayList<FileInfo> list = new ArrayList<FileInfo>();
            for (String ext : extensions)
            list.addRange(new DirectoryInfo(path).GetFiles("*" + ext).Where(p =>
                    p.Extension.Equals(ext, StringComparison.CurrentCultureIgnoreCase))
                    .toArray());
            return list;
        }
    @Override
        public void MoveImportedFiles()
        {
            var files = Directory
                    .GetFiles(ImportFolder)
                    .Where(file => Config.allowedExtensions.Any(file.toLowerCase().EndsWith))
              .ToList();

            String[] fnames = new String[files.count()];
            for (int i = 0; i < files.count(); i++)
            {
                MoveFile(files[i], ImportedFolder + File.pathSeparator + Path.GetFileName(files[i]), FileMoveOptions.Rename);
            }

            var filesqr = Directory
                    .GetFiles(ImportFolder + File.pathSeparator + "QrCodes")
                    .Where(file => Config.allowedExtensions.Any(file.toLowerCase().EndsWith))
              .ToList();

            String[] fnamesqr = new String[filesqr.count()];
            for (int i = 0; i < filesqr.count(); i++)
            {
                MoveFile(filesqr[i], ImportedFolder + File.pathSeparator + Path.GetFileName(filesqr[i]), FileMoveOptions.Rename);
            }

            var filesbar = Directory
                    .GetFiles(ImportFolder + File.pathSeparator + "Barcodes")
                    .Where(file => Config.allowedExtensions.Any(file.toLowerCase().EndsWith))
              .ToList();

            String[] fnamesbar = new String[filesbar.count()];
            for (int i = 0; i < filesbar.count(); i++)
            {
                MoveFile(filesbar[i], ImportedFolder + File.pathSeparator + Path.GetFileName(filesbar[i]), FileMoveOptions.Rename);
            }
        }

    @Override
        public boolean WriteCoinToJpeg(CloudCoin cloudCoin, String TemplateFile, String OutputFile, String tag)
        {
            OutputFile = OutputFile.replace("\\\\", "\\");
            boolean fileSavedSuccessfully = true;

            /* BUILD THE CLOUDCOIN STRING */
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
            /* BYTES THAT WILL GO FROM 04 to 454 (Inclusive)*/
            byte[] ccArray = this.hexStringToByteArray(cloudCoinStr);


            /* READ JPEG TEMPLATE*/
            byte[] jpegBytes = null;

            //jpegBytes = readAllBytes(filePath);
            jpegBytes = File.ReadAllBytes(TemplateFile);

            /* WRITE THE SERIAL NUMBER ON THE JPEG */

            //Bitmap bitmapimage;
            //jpegBytes = readAllBytes(filePath);
            jpegBytes = File.ReadAllBytes(TemplateFile);

            /* WRITE THE SERIAL NUMBER ON THE JPEG */

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
        }

    @Override
        public boolean WriteCoinToQRCode(CloudCoin cloudCoin, String OutputFile, String tag)
        {
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
        }

    @Override
        public boolean WriteCoinToBARCode(CloudCoin cloudCoin, String OutputFile, String tag)
        {
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
        }
}

