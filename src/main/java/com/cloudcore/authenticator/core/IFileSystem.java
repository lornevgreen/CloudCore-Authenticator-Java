package com.cloudcore.authenticator.core;

import com.cloudcore.authenticator.Formats;
import com.cloudcore.authenticator.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.cloudcore.authenticator.Formats.BarCode;

public abstract class IFileSystem {

    public enum FileMoveOptions { Copy, Replace, Rename, Skip }
    public String RootPath;
    public String ImportFolder;
    public String ExportFolder;
    public String BankFolder;
    public String ImportedFolder;
    public String LostFolder;
    public String TrashFolder;
    public String SuspectFolder;
    public String DetectedFolder;
    public String FrackedFolder;
    public String TemplateFolder;
    public String PartialFolder;
    public String CounterfeitFolder;
    public String LanguageFolder;
    public String PreDetectFolder;
    public String RequestsFolder;
    public String DangerousFolder;
    public String LogsFolder;
    public String QRFolder;
    public String BarCodeFolder;
    public String CSVFolder;

    //public abstract IFileSystem(String path);

    public static IEnumerable<CloudCoin> importCoins;
    public static IEnumerable<CloudCoin> exportCoins;
    public static IEnumerable<CloudCoin> importedCoins;
    public static IEnumerable<FileInfo> templateFiles;
    public static IEnumerable<CloudCoin> languageCoins;
    public static IEnumerable<CloudCoin> counterfeitCoins;
    public static IEnumerable<CloudCoin> partialCoins;
    public static IEnumerable<CloudCoin> frackedCoins;
    public static IEnumerable<CloudCoin> detectedCoins;
    public static IEnumerable<CloudCoin> suspectCoins;
    public static IEnumerable<CloudCoin> trashCoins;
    public static IEnumerable<CloudCoin> bankCoins;
    public static IEnumerable<CloudCoin> lostCoins;
    public static IEnumerable<CloudCoin> predetectCoins;
    public static IEnumerable<CloudCoin> dangerousCoins;

    public abstract boolean CreateFolderStructure();

    public abstract void LoadFileSystem();

    public abstract void ClearCoins(String FolderName);

    public ArrayList<CloudCoin> LoadCoinsByFormat(String folder, Formats format)  {
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
                            coin = ReadBARCode(fileName);
                            folderCoins.add(coin);
                            break;
                        case QRCode:
                            coin = ReadQRCode(fileName);
                            folderCoins.add(coin);
                            break;
                    }
                }
            }
        }

        return folderCoins;
    }

    public ArrayList<CloudCoin> LoadFolderBarCodes(String folder)
    {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();

        return folderCoins;
    }

    public ArrayList<CloudCoin> LoadFolderCoins(String folder)
    {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();

        String[] fileNames = FileUtils.selectFileNamesInFolder(folder);
        String extension;
        for (String fileName : fileNames) {
            int index = fileName.lastIndexOf('.');
            if (index > 0) {
                extension = fileName.substring(index + 1);

                switch (extension) {
                    case "celeb":
                    case "celebrium":
                    case "stack":
                        ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromJSON(fileName);
                        if (coins != null)
                            folderCoins.addAll(coins);
                        break;
                    case "jpg":
                    case "jpeg":
                        CloudCoin coin = importJPEG(fileName);
                        folderCoins.add(coin);
                        break;
                    case "csv":
                        List<String> lines = null;
                        try {
                            ArrayList<CloudCoin> csvCoins = new ArrayList<>();
                            lines = Files.readAllLines(Paths.get(fileName));
                            for (String line : lines) {
                                csvCoins.add(CloudCoin.FromCSV(line));
                            }
                            csvCoins.remove(null);
                            folderCoins.addAll(csvCoins);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }

        return folderCoins;
    }

    private CloudCoin ReadQRCode(String fileName)//Read a CloudCoin from QR Code 
    {
        CloudCoin coin = new CloudCoin();

        try
        {
            Bitmap bitmap = new Bitmap(fileName);
            BarcodeReader reader = new BarcodeReader { AutoRotate = true, TryInverted = true };
            Result result = reader.Decode(bitmap);
            String decoded = result.ToString().Trim();

            CloudCoin cloudCoin = JsonConvert.DeserializeObject<CloudCoin>(decoded);
            return cloudCoin;
        }
        catch (Exception)
        {
            return null;
        }
    }

    private CloudCoin ReadBARCode(String fileName)//Read a CloudCoin from BAR Code . 
    {
        CloudCoin coin = new CloudCoin();

        try
        {
            var barcodeReader = new BarcodeReader();
            Bitmap bitmap = new Bitmap(fileName);

            var barcodeResult = barcodeReader.Decode(bitmap);
            String decoded = barcodeResult.ToString().Trim();

            CloudCoin cloudCoin = JsonConvert.DeserializeObject<CloudCoin>(decoded);
            return cloudCoin;
        }
        catch (Exception)
        {
            return null;
        }
    }

    private ArrayList<CloudCoin> ReadCSVCoins(String fileName)//Read a CloudCoin from CSV . 
    {
        ArrayList<CloudCoin> cloudCoins = new ArrayList<CloudCoin>();
        var lines = File.ReadAllLines(fileName);
        //var lines = File.ReadAllLines(fileName).Select(a => a.Split(','));

        CloudCoin coin = new CloudCoin();

        foreach(var line in lines)
        {
            cloudCoins.Add( CloudCoin.FromCSV(line));
        }
        return cloudCoins;
    }
    private CloudCoin importJPEG(String fileName)//Move one jpeg to suspect folder. 
    {
        // boolean isSuccessful = false;
        // Console.Out.WriteLine("Trying to load: " + this.fileUtils.importFolder + fileName );
        System.out.println("Trying to load: " + ImportFolder + fileName);
        try
        {
            //  Console.Out.WriteLine("Loading coin: " + fileUtils.importFolder + fileName);
            //CloudCoin tempCoin = this.fileUtils.loadOneCloudCoinFromJPEGFile( fileUtils.importFolder + fileName );

            /*Begin import from jpeg*/

            /* GET the first 455 bytes of he jpeg where the coin is located */
            String wholeString = "";
            byte[] jpegHeader = new byte[455];
            // Console.Out.WriteLine("Load file path " + fileUtils.importFolder + fileName);
            FileStream fileStream = new FileStream(fileName, FileMode.Open, FileAccess.Read);
            try
            {
                int count;                            // actual number of bytes read
                int sum = 0;                          // total number of bytes read

                // read until Read method returns 0 (end of the stream has been reached)
                while ((count = fileStream.Read(jpegHeader, sum, 455 - sum)) > 0)
                    sum += count;  // sum is a buffer offset for next reading
            }
            finally
            {
                fileStream.Dispose();
                //fileStream.Close();
            }
            wholeString = bytesToHexString(jpegHeader);

            CloudCoin tempCoin = parseJpeg(wholeString);
            // Console.Out.WriteLine("From FileUtils returnCC.fileName " + tempCoin.fileName);

            /*end import from jpeg file */



            //   Console.Out.WriteLine("Loaded coin filename: " + tempCoin.fileName);

            writeTo(SuspectFolder, tempCoin);
            return tempCoin;
        }
        catch (FileNotFoundException ex)
        {
            Console.Out.WriteLine("File not found: " + fileName + ex);
            //CoreLogger.Log("File not found: " + fileName + ex);
        }
        catch (IOException ioex)
        {
            Console.Out.WriteLine("IO Exception:" + fileName + ioex);
            //CoreLogger.Log("IO Exception:" + fileName + ioex);
        }// end try catch
        return null;
    }


    public CloudCoin LoadCoin(String fileName)
    {
        var coins = Utils.LoadJson(fileName);

        if (coins != null && coins.Length > 0)
            return coins[0];
        return null;
    }
    public IEnumerable<CloudCoin> LoadCoins(String fileName)
    {
        var coins = Utils.LoadJson(fileName);

        if (coins != null && coins.Length > 0)
            return coins;
        return null;
    }
    public ArrayList<FileInfo> LoadFiles(String folder)
    {
        ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
        var files = Directory
                .GetFiles(folder)
                .ToList();
        foreach (var item in files)
        {
            fileInfos.Add(new FileInfo(item));
            System.out.println("Read File-" + item);
        }

        System.out.println("Total " + files.Count + " items read");

        return fileInfos;
    }

    public ArrayList<FileInfo> LoadFiles(String folder, String[] allowedExtensions)
    {
        ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
        var files = Directory
                .GetFiles(folder)
                .Where(file => allowedExtensions.Any(file.ToLower().EndsWith))
                .ToList();
        foreach (var item in files)
        {
            fileInfos.Add(new FileInfo(item));
            //System.out.println(item);
        }

        //System.out.println("Total " + files.Count + " items read");

        return fileInfos;
    }

    public abstract void ProcessCoins(IEnumerable<CloudCoin> coins);
    public abstract void DetectPreProcessing();


    public CloudCoin loadOneCloudCoinFromJsonFile(String loadFilePath)
    {

        CloudCoin returnCC = new CloudCoin();

        //Load file as JSON
        String incomeJson = this.importJSON(loadFilePath);
        //STRIP UNESSARY test
        int secondCurlyBracket = ordinalIndexOf(incomeJson, "{", 2) - 1;
        int firstCloseCurlyBracket = ordinalIndexOf(incomeJson, "}", 0) - secondCurlyBracket;
        // incomeJson = incomeJson.SubString(secondCurlyBracket, firstCloseCurlyBracket);
        incomeJson = incomeJson.SubString(secondCurlyBracket, firstCloseCurlyBracket + 1);
        // Console.Out.WriteLine(incomeJson);
        //Deserial JSON

        try
        {
            returnCC = JsonConvert.DeserializeObject<CloudCoin>(incomeJson);

        }
        catch (JsonReaderException)
        {
            Console.WriteLine("There was an error reading files in your bank.");
            Console.WriteLine("You may have the aoid memo bug that uses too many double quote marks.");
            Console.WriteLine("Your bank files are stored using and older version that did not use properly formed JSON.");
            Console.WriteLine("Would you like to upgrade these files to the newer standard?");
            Console.WriteLine("Your files will be edited.");
            Console.WriteLine("1 for yes, 2 for no.");


        }

        return returnCC;
    }//end load one CloudCoin from JSON

    public void MoveFile(String SourcePath, String TargetPath, FileMoveOptions options)
    {
        if (!File.Exists(TargetPath))
            File.Move(SourcePath, TargetPath);
        else
        {
            if (options == FileMoveOptions.Replace)
            {
                File.Delete(TargetPath);
                File.Move(SourcePath, TargetPath);
            }
            if (options == FileMoveOptions.Rename)
            {
                String targetFileName = Path.GetFileNameWithoutExtension(SourcePath);
                targetFileName += Utils.RandomString(8).ToLower() + ".stack";
                String targetPath = Path.GetDirectoryName(TargetPath) + Path.DirectorySeparatorChar + targetFileName;
                File.Move(SourcePath, targetPath);

            }
        }
    }

    public String importJSON(String jsonfile)
    {
        String jsonData = "";
        String line;

        try
        {
            // Create an instance of StreamReader to read from a file.
            // The using statement also closes the StreamReader.

            using (var sr = File.OpenText(jsonfile))
            {
                // Read and display lines from the file until the end of 
                // the file is reached.
                while (true)
                {
                    line = sr.ReadLine();
                    if (line == null)
                    {
                        break;
                    }//End if line is null
                    jsonData = (jsonData + line + "\n");
                }//end while true
            }//end using
        }
        catch (Exception e)
        {
            // Let the user know what went wrong.
            Console.WriteLine("The file " + jsonfile + " could not be read:");
            Console.WriteLine(e.Message);
        }
        return jsonData;
    }//end importJSON

    // en d json test
    public String setJSON(CloudCoin cc)
    {
            const String quote = "\"";
            const String tab = "\t";
        String json = (tab + tab + "{ " + Environment.NewLine);// {
        json += tab + tab + quote + "nn" + quote + ":" + quote + cc.nn + quote + ", " + Environment.NewLine;// "nn":"1",
        json += tab + tab + quote + "sn" + quote + ":" + quote + cc.sn + quote + ", " + Environment.NewLine;// "sn":"367544",
        json += tab + tab + quote + "an" + quote + ": [" + quote;// "an": ["
        for (int i = 0; (i < 25); i++)
        {
            json += cc.an[i];// 8551995a45457754aaaa44
            if (i == 4 || i == 9 || i == 14 || i == 19)
            {
                json += quote + "," + Environment.NewLine + tab + tab + tab + quote; //", 
            }
            else if (i == 24)
            {
                // json += "\""; last one do nothing
            }
            else
            { // end if is line break
                json += quote + ", " + quote;
            }

            // end else
        }// end for 25 ans

        json += quote + "]," + Environment.NewLine;//"],
        // End of ans
        //CoinUtils cu = new CoinUtils(cc);
        //cu.calcExpirationDate();
        cc.CalcExpirationDate();
        json += tab + tab + quote + "ed" + quote + ":" + quote + cc.ed + quote + "," + Environment.NewLine; // "ed":"9-2016",
        if (String.IsNullOrEmpty(cc.pown)) { cc.pown = "uuuuuuuuuuuuuuuuuuuuuuuuu"; }//Set pown to unknow if it is not set. 
        json += tab + tab + quote + "pown" + quote + ":" + quote + cc.pown + quote + "," + Environment.NewLine;// "pown":"uuupppppffpppppfuuf",
        json += tab + tab + quote + "aoid" + quote + ": []" + Environment.NewLine;
        json += tab + tab + "}" + Environment.NewLine;
        // Keep expiration date when saving (not a truley accurate but good enought )
        return json;
    }
    // end get JSON

    public abstract void MoveImportedFiles();
    public void RemoveCoins(IEnumerable<CloudCoin> coins, String folder)
    {

        foreach (var coin in coins)
        {
            File.Delete(folder + (coin.FileName) + ".stack");

        }
    }

    public void RemoveCoins(IEnumerable<CloudCoin> coins, String folder, String extension)
    {

        foreach (var coin in coins)
        {
            File.Delete(folder + (coin.FileName) + extension);

        }
    }

    public void MoveCoins(IEnumerable<CloudCoin> coins, String sourceFolder, String targetFolder, boolean replaceCoins = false)
    {
        var folderCoins = LoadFolderCoins(targetFolder);

        foreach (var coin in coins)
        {
            String fileName = (coin.FileName);
            int coinExists = (from x in folderCoins
            where x.sn == coin.sn
            select x).Count();
            if (coinExists > 0 && !replaceCoins)
            {
                String suffix = Utils.RandomString(16);
                fileName += suffix.ToLower();
            }
            try
            {
                JsonSerializer serializer = new JsonSerializer();
                serializer.Converters.Add(new JavaScriptDateTimeConverter());
                serializer.NullValueHandling = NullValueHandling.Ignore;
                Stack stack = new Stack(coin);
                using (StreamWriter sw = new StreamWriter(targetFolder + fileName + ".stack"))
                using (JsonWriter writer = new JsonTextWriter(sw))
                {
                    serializer.Serialize(writer, stack);
                }
                File.Delete(sourceFolder + (coin.FileName) + ".stack");
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }


        }
    }

    public void MoveCoins(IEnumerable<CloudCoin> coins, String sourceFolder, String targetFolder, String extension, boolean replaceCoins = false)
    {
        var folderCoins = LoadFolderCoins(targetFolder);

        foreach (var coin in coins)
        {
            String fileName = (coin.FileName);
            int coinExists = (from x in folderCoins
            where x.sn == coin.sn
            select x).Count();
            if (coinExists > 0 && !replaceCoins)
            {
                String suffix = Utils.RandomString(16);
                fileName += suffix.ToLower();
            }
            try
            {
                JsonSerializer serializer = new JsonSerializer();
                serializer.Converters.Add(new JavaScriptDateTimeConverter());
                serializer.NullValueHandling = NullValueHandling.Ignore;
                Stack stack = new Stack(coin);
                using (StreamWriter sw = new StreamWriter(targetFolder + fileName + extension))
                using (JsonWriter writer = new JsonTextWriter(sw))
                {
                    serializer.Serialize(writer, stack);
                }
                File.Delete(sourceFolder + (coin.FileName) + extension);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }


        }
    }

    public void WriteCoinsToFile(IEnumerable<CloudCoin> coins, String fileName, String extension = ".stack")
    {
        JsonSerializer serializer = new JsonSerializer();
        serializer.Converters.Add(new JavaScriptDateTimeConverter());
        serializer.NullValueHandling = NullValueHandling.Ignore;
        Stack stack = new Stack(coins.ToArray());
        using (StreamWriter sw = new StreamWriter(fileName + extension))
        using (JsonWriter writer = new JsonTextWriter(sw))
        {
            serializer.Serialize(writer, stack);
        }
    }

    public void WriteCoin(CloudCoin coin, String folder)
    {
        var folderCoins = LoadFolderCoins(folder);
        String fileName = (coin.FileName());
        int coinExists = (from x in folderCoins
        where x.sn == coin.sn
        select x).Count();
        if (coinExists > 0)
        {
            String suffix = Utils.RandomString(16);
            fileName += suffix.ToLower();
        }
        JsonSerializer serializer = new JsonSerializer();
        serializer.Converters.Add(new JavaScriptDateTimeConverter());
        serializer.NullValueHandling = NullValueHandling.Ignore;
        Stack stack = new Stack(coin);
        using (StreamWriter sw = new StreamWriter(folder + Path.DirectorySeparatorChar + fileName + ".stack"))
        using (JsonWriter writer = new JsonTextWriter(sw))
        {
            serializer.Serialize(writer, stack);
        }
    }

    public void WriteCoinToFile(CloudCoin coin, String filename)
    {


        JsonSerializer serializer = new JsonSerializer();
        serializer.Converters.Add(new JavaScriptDateTimeConverter());
        serializer.NullValueHandling = NullValueHandling.Ignore;
        Stack stack = new Stack(coin);
        using (StreamWriter sw = new StreamWriter(filename))
        using (JsonWriter writer = new JsonTextWriter(sw))
        {
            serializer.Serialize(writer, stack);
        }
    }

    public void WriteCoin(IEnumerable<CloudCoin> coins, String folder, boolean writeAll = false)
    {
        if (writeAll)
        {
            String fileName = Utils.RandomString(16) + ".stack";
            JsonSerializer serializer = new JsonSerializer();
            serializer.Converters.Add(new JavaScriptDateTimeConverter());
            serializer.NullValueHandling = NullValueHandling.Ignore;
            Stack stack = new Stack(coins.ToArray());
            using (StreamWriter sw = new StreamWriter(folder + fileName + ".stack"))
            using (JsonWriter writer = new JsonTextWriter(sw))
            {
                serializer.Serialize(writer, stack);
            }
            return;
        }
        var folderCoins = LoadFolderCoins(folder);

        foreach (var coin in coins)
        {
            String fileName = coin.FileName;
            int coinExists = (from x in folderCoins
            where x.sn == coin.sn
            select x).Count();
            if (coinExists > 0)
            {
                String suffix = Utils.RandomString(16);
                fileName += suffix.ToLower();
            }
            JsonSerializer serializer = new JsonSerializer();
            serializer.Converters.Add(new JavaScriptDateTimeConverter());
            serializer.NullValueHandling = NullValueHandling.Ignore;
            Stack stack = new Stack(coin);
            using (StreamWriter sw = new StreamWriter(folder + fileName + ".stack"))
            using (JsonWriter writer = new JsonTextWriter(sw))
            {
                serializer.Serialize(writer, stack);
            }

        }
    }

    public void WriteCoin(CloudCoin coin, String folder, String extension)
    {
        var folderCoins = LoadFolderCoins(folder);
        String fileName = (coin.FileName());
        int coinExists = (from x in folderCoins
        where x.sn == coin.sn
        select x).Count();
        if (coinExists > 0)
        {
            String suffix = Utils.RandomString(16);
            fileName += suffix.ToLower();
        }
        JsonSerializer serializer = new JsonSerializer();
        serializer.Converters.Add(new JavaScriptDateTimeConverter());
        serializer.NullValueHandling = NullValueHandling.Ignore;
        Stack stack = new Stack(coin);
        using (StreamWriter sw = new StreamWriter(folder + Path.DirectorySeparatorChar + fileName + extension))
        using (JsonWriter writer = new JsonTextWriter(sw))
        {
            serializer.Serialize(writer, stack);
        }
    }
    public int ordinalIndexOf(String str, String substr, int n)
    {
        int pos = str.IndexOf(substr);
        while (--n > 0 && pos != -1)
            pos = str.IndexOf(substr, pos + 1);
        return pos;
    }

    public boolean writeQrCode(CloudCoin cc, String tag)
    {
        String fileName = ExportFolder + cc.FileName() + "qr." + tag + ".jpg";
        cc.pan = null;
        QRCodeGenerator qrGenerator = new QRCodeGenerator();
        String json = JsonConvert.SerializeObject(cc);

        try
        {
            json.Replace("\\", "");
            QRCodeData qrCodeData = qrGenerator.CreateQrCode(cc.GetCSV(), QRCodeGenerator.ECCLevel.Q);
            QRCode qrCode = new QRCode(qrCodeData);
            System.Drawing.Bitmap qrCodeImage = qrCode.GetGraphic(20);

            qrCodeImage.Save(fileName);

            return true;
        }
        catch(Exception e)
        {
            Console.WriteLine(e.Message);
            return false;
        }
    }


    public boolean writeBarCode(CloudCoin cc, String tag)
    {
        //String fileName = ExportFolder + cc.FileName + "barcode." + tag + ".jpg";
        //cc.pan = null;
        //QRCodeGenerator qrGenerator = new QRCodeGenerator();


        //try
        //{
        //    String json = JsonConvert.SerializeObject(cc);
        //    var barcode = new Barcode(json, Settings.Default);
        //    barcode.Canvas.SaveBmp(fileName);

        //    return true;
        //}
        //catch (Exception e)
        //{
        //    Console.WriteLine(e.Message);
        //    return false;
        //}
        return true;
    }

    public abstract boolean WriteCoinToJpeg(CloudCoin cloudCoin, String TemplateFile,String OutputFile, String tag);

    public abstract boolean WriteCoinToQRCode(CloudCoin cloudCoin, String OutputFile, String tag);

    public abstract boolean WriteCoinToBARCode(CloudCoin cloudCoin, String OutputFile, String tag);

    public String GetCoinTemplate(CloudCoin cloudCoin)
    {
        int denomination = cloudCoin.denomination;
        String TemplatePath = "";
        switch (denomination)
        {
            case 1:
                TemplatePath = this.TemplateFolder + "jpeg1.jpg";
                break;
            case 5:
                TemplatePath = this.TemplateFolder + "jpeg5.jpg";
                break;
            case 25:
                TemplatePath = this.TemplateFolder + "jpeg25.jpg";
                break;
            case 100:
                TemplatePath = this.TemplateFolder + "jpeg100.jpg";
                break;
            case 250:
                TemplatePath = this.TemplateFolder + "jpeg250.jpg";
                break;

            default:
                break;

        }
        return TemplatePath;
    }
    public boolean writeJpeg(CloudCoin cc, String tag)
    {
        // Console.Out.WriteLine("Writing jpeg " + cc.sn);

        //  CoinUtils cu = new CoinUtils(cc);

        boolean fileSavedSuccessfully = true;

        /* BUILD THE CLOUDCOIN String */
        String cloudCoinStr = "01C34A46494600010101006000601D05"; //THUMBNAIL HEADER BYTES
        for (int i = 0; (i < 25); i++)
        {
            cloudCoinStr = cloudCoinStr + cc.an[i];
        } // end for each an

        //cloudCoinStr += "204f42455920474f4420262044454645415420545952414e545320";// Hex for " OBEY GOD & DEFEAT TYRANTS "
        //cloudCoinStr += "20466f756e6465727320372d352d3137";// Founders 7-5-17
        cloudCoinStr += "4c6976652046726565204f7220446965";// Live Free or Die
        cloudCoinStr += "00000000000000000000000000";//Set to unknown so program does not export user data
        // for (int i =0; i < 25; i++) {
        //     switch () { }//end switch pown char
        // }//end for each pown
        cloudCoinStr += "00"; // HC: Has comments. 00 = No
        cc.CalcExpirationDate();
        cloudCoinStr += cc.edHex; // 01;//Expiration date Sep 2016 (one month after zero month)
        cloudCoinStr += "01";//  cc.nn;//network number
        String hexSN = cc.sn.ToString("X6");
        String fullHexSN = "";
        switch (hexSN.Length)
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
        switch (cc.getDenomination())
        {
            case 1: jpegBytes = readAllBytes(this.TemplateFolder + "jpeg1.jpg"); break;
            case 5: jpegBytes = readAllBytes(this.TemplateFolder + "jpeg5.jpg"); break;
            case 25: jpegBytes = readAllBytes(this.TemplateFolder + "jpeg25.jpg"); break;
            case 100: jpegBytes = readAllBytes(this.TemplateFolder + "jpeg100.jpg"); break;
            case 250: jpegBytes = readAllBytes(this.TemplateFolder + "jpeg250.jpg"); break;
        }// end switch


        /* WRITE THE SERIAL NUMBER ON THE JPEG */

        //Bitmap bitmapimage;
        SKBitmap bitmapimage;
        //using (var ms = new MemoryStream(jpegBytes))
        {

            //bitmapimage = new Bitmap(ms);
            bitmapimage = SKBitmap.Decode(jpegBytes);
        }
        SKCanvas canvas = new SKCanvas(bitmapimage);
        //Graphics graphics = Graphics.FromImage(bitmapimage);
        //graphics.SmoothingMode = SmoothingMode.AntiAlias;
        //graphics.InterpolationMode = InterpolationMode.HighQualityBicubic;
        SKPaint textPaint = new SKPaint()
        {
            IsAntialias = true,
            Color = SKColors.White,
            TextSize = 14,
            Typeface = SKTypeface.FromFamilyName("Arial")
        };
        //PointF drawPointAddress = new PointF(30.0F, 25.0F);

        canvas.DrawText(String.Format("{0:N0}", cc.sn) + " of 16,777,216 on Network: 1", 30, 40, textPaint);
        //graphics.DrawString(String.Format("{0:N0}", cc.sn) + " of 16,777,216 on Network: 1", new Font("Arial", 10), Brushes.White, drawPointAddress);

        //ImageConverter converter = new ImageConverter();
        //byte[] snBytes = (byte[])converter.ConvertTo(bitmapimage, typeof(byte[]));
        SKImage image = SKImage.FromBitmap(bitmapimage);
        SKData data = image.Encode(SKEncodedImageFormat.Jpeg, 100);
        byte[] snBytes = data.ToArray();

        ArrayList<byte> b1 = new ArrayList<byte>(snBytes);
        ArrayList<byte> b2 = new ArrayList<byte>(ccArray);
        b1.InsertRange(4, b2);

        if (tag == "random")
        {
            Random r = new Random();
            int rInt = r.Next(100000, 1000000); //for ints
            tag = rInt.ToString();
        }

        String fileName = ExportFolder + cc.FileName() + tag + ".jpg";
        File.WriteAllBytes(fileName, b1.ToArray());
        Console.Out.WriteLine("Writing to " + fileName);
        //CoreLogger.Log("Writing to " + fileName);
        return fileSavedSuccessfully;
    }//end write JPEG

    public boolean writeJpeg(CloudCoin cc, String tag,String filePath)
    {
        // Console.Out.WriteLine("Writing jpeg " + cc.sn);

        //  CoinUtils cu = new CoinUtils(cc);
        filePath = filePath.Replace("\\\\","\\");
        boolean fileSavedSuccessfully = true;

        /* BUILD THE CLOUDCOIN String */
        String cloudCoinStr = "01C34A46494600010101006000601D05"; //THUMBNAIL HEADER BYTES
        for (int i = 0; (i < 25); i++)
        {
            cloudCoinStr = cloudCoinStr + cc.an[i];
        } // end for each an

        //cloudCoinStr += "204f42455920474f4420262044454645415420545952414e545320";// Hex for " OBEY GOD & DEFEAT TYRANTS "
        //cloudCoinStr += "20466f756e6465727320372d352d3137";// Founders 7-5-17
        cloudCoinStr += "4c6976652046726565204f7220446965";// Live Free or Die
        cloudCoinStr += "00000000000000000000000000";//Set to unknown so program does not export user data
        // for (int i =0; i < 25; i++) {
        //     switch () { }//end switch pown char
        // }//end for each pown
        cloudCoinStr += "00"; // HC: Has comments. 00 = No
        cc.CalcExpirationDate();
        cloudCoinStr += cc.edHex; // 01;//Expiration date Sep 2016 (one month after zero month)
        cloudCoinStr += "01";//  cc.nn;//network number
        String hexSN = cc.sn.ToString("X6");
        String fullHexSN = "";
        switch (hexSN.Length)
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
        jpegBytes = File.ReadAllBytes(filePath);

        /* WRITE THE SERIAL NUMBER ON THE JPEG */

        //Bitmap bitmapimage;
        SKBitmap bitmapimage;
        //using (var ms = new MemoryStream(jpegBytes))
        {

            //bitmapimage = new Bitmap(ms);
            bitmapimage = SKBitmap.Decode(jpegBytes);
        }
        SKCanvas canvas = new SKCanvas(bitmapimage);
        //Graphics graphics = Graphics.FromImage(bitmapimage);
        //graphics.SmoothingMode = SmoothingMode.AntiAlias;
        //graphics.InterpolationMode = InterpolationMode.HighQualityBicubic;
        SKPaint textPaint = new SKPaint()
        {
            IsAntialias = true,
            Color = SKColors.White,
            TextSize = 14,
            Typeface = SKTypeface.FromFamilyName("Arial")
        };
        //PointF drawPointAddress = new PointF(30.0F, 25.0F);

        canvas.DrawText(String.Format("{0:N0}", cc.sn) + " of 16,777,216 on Network: 1", 30, 40, textPaint);
        //graphics.DrawString(String.Format("{0:N0}", cc.sn) + " of 16,777,216 on Network: 1", new Font("Arial", 10), Brushes.White, drawPointAddress);

        //ImageConverter converter = new ImageConverter();
        //byte[] snBytes = (byte[])converter.ConvertTo(bitmapimage, typeof(byte[]));
        SKImage image = SKImage.FromBitmap(bitmapimage);
        SKData data = image.Encode(SKEncodedImageFormat.Jpeg, 100);
        byte[] snBytes = data.ToArray();

        ArrayList<byte> b1 = new ArrayList<byte>(snBytes);
        ArrayList<byte> b2 = new ArrayList<byte>(ccArray);
        b1.InsertRange(4, b2);

        if (tag == "random")
        {
            Random r = new Random();
            int rInt = r.Next(100000, 1000000); //for ints
            tag = rInt.ToString();
        }

        String fileName = ExportFolder + cc.FileName()  + ".jpg";
        File.WriteAllBytes(fileName, b1.ToArray());
        Console.Out.WriteLine("Writing to " + fileName);
        //CoreLogger.Log("Writing to " + fileName);
        return fileSavedSuccessfully;
    }//end write JPEG

    public boolean writeJpeg(CloudCoin cc, String tag, String filePath,String targetPath)
    {
        // Console.Out.WriteLine("Writing jpeg " + cc.sn);

        //  CoinUtils cu = new CoinUtils(cc);
        filePath = filePath.Replace("\\\\", "\\");
        boolean fileSavedSuccessfully = true;

        /* BUILD THE CLOUDCOIN String */
        String cloudCoinStr = "01C34A46494600010101006000601D05"; //THUMBNAIL HEADER BYTES
        for (int i = 0; (i < 25); i++)
        {
            cloudCoinStr = cloudCoinStr + cc.an[i];
        } // end for each an

        //cloudCoinStr += "204f42455920474f4420262044454645415420545952414e545320";// Hex for " OBEY GOD & DEFEAT TYRANTS "
        //cloudCoinStr += "20466f756e6465727320372d352d3137";// Founders 7-5-17
        cloudCoinStr += "4c6976652046726565204f7220446965";// Live Free or Die
        cloudCoinStr += "00000000000000000000000000";//Set to unknown so program does not export user data
        // for (int i =0; i < 25; i++) {
        //     switch () { }//end switch pown char
        // }//end for each pown
        cloudCoinStr += "00"; // HC: Has comments. 00 = No
        cc.CalcExpirationDate();
        cloudCoinStr += cc.edHex; // 01;//Expiration date Sep 2016 (one month after zero month)
        cloudCoinStr += "01";//  cc.nn;//network number
        String hexSN = cc.sn.ToString("X6");
        String fullHexSN = "";
        switch (hexSN.Length)
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
        jpegBytes = File.ReadAllBytes(filePath);

        /* WRITE THE SERIAL NUMBER ON THE JPEG */

        //Bitmap bitmapimage;
        SKBitmap bitmapimage;
        //using (var ms = new MemoryStream(jpegBytes))
        {

            //bitmapimage = new Bitmap(ms);
            bitmapimage = SKBitmap.Decode(jpegBytes);
        }
        SKCanvas canvas = new SKCanvas(bitmapimage);
        //Graphics graphics = Graphics.FromImage(bitmapimage);
        //graphics.SmoothingMode = SmoothingMode.AntiAlias;
        //graphics.InterpolationMode = InterpolationMode.HighQualityBicubic;
        SKPaint textPaint = new SKPaint()
        {
            IsAntialias = true,
            Color = SKColors.White,
            TextSize = 14,
            Typeface = SKTypeface.FromFamilyName("Arial")
        };
        //PointF drawPointAddress = new PointF(30.0F, 25.0F);

        canvas.DrawText(String.Format("{0:N0}", cc.sn) + " of 16,777,216 on Network: 1", 30, 40, textPaint);
        //graphics.DrawString(String.Format("{0:N0}", cc.sn) + " of 16,777,216 on Network: 1", new Font("Arial", 10), Brushes.White, drawPointAddress);

        //ImageConverter converter = new ImageConverter();
        //byte[] snBytes = (byte[])converter.ConvertTo(bitmapimage, typeof(byte[]));
        SKImage image = SKImage.FromBitmap(bitmapimage);
        SKData data = image.Encode(SKEncodedImageFormat.Jpeg, 100);
        byte[] snBytes = data.ToArray();

        ArrayList<byte> b1 = new ArrayList<byte>(snBytes);
        ArrayList<byte> b2 = new ArrayList<byte>(ccArray);
        b1.InsertRange(4, b2);

        if (tag == "random")
        {
            Random r = new Random();
            int rInt = r.Next(100000, 1000000); //for ints
            tag = rInt.ToString();
        }

        String fileName = targetPath;
        File.WriteAllBytes(fileName, b1.ToArray());
        Console.Out.WriteLine("Writing to " + fileName);
        //CoreLogger.Log("Writing to " + fileName);
        return fileSavedSuccessfully;
    }//end write JPEG

    public boolean writeJpeg(CloudCoin cc, String tag, String filePath, String targetPath,String printMessage)
    {
        // Console.Out.WriteLine("Writing jpeg " + cc.sn);

        //  CoinUtils cu = new CoinUtils(cc);
        filePath = filePath.Replace("\\\\", "\\");
        boolean fileSavedSuccessfully = true;

        /* BUILD THE CLOUDCOIN String */
        String cloudCoinStr = "01C34A46494600010101006000601D05"; //THUMBNAIL HEADER BYTES
        for (int i = 0; (i < 25); i++)
        {
            cloudCoinStr = cloudCoinStr + cc.an[i];
        } // end for each an

        //cloudCoinStr += "204f42455920474f4420262044454645415420545952414e545320";// Hex for " OBEY GOD & DEFEAT TYRANTS "
        //cloudCoinStr += "20466f756e6465727320372d352d3137";// Founders 7-5-17
        cloudCoinStr += "4c6976652046726565204f7220446965";// Live Free or Die
        cloudCoinStr += "00000000000000000000000000";//Set to unknown so program does not export user data
        // for (int i =0; i < 25; i++) {
        //     switch () { }//end switch pown char
        // }//end for each pown
        cloudCoinStr += "00"; // HC: Has comments. 00 = No
        cc.CalcExpirationDate();
        cloudCoinStr += cc.edHex; // 01;//Expiration date Sep 2016 (one month after zero month)
        cloudCoinStr += "01";//  cc.nn;//network number
        String hexSN = cc.sn.ToString("X6");
        String fullHexSN = "";
        switch (hexSN.Length)
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
        jpegBytes = File.ReadAllBytes(filePath);

        /* WRITE THE SERIAL NUMBER ON THE JPEG */

        //Bitmap bitmapimage;
        SKBitmap bitmapimage;
        //using (var ms = new MemoryStream(jpegBytes))
        {

            //bitmapimage = new Bitmap(ms);
            bitmapimage = SKBitmap.Decode(jpegBytes);
        }
        SKCanvas canvas = new SKCanvas(bitmapimage);
        //Graphics graphics = Graphics.FromImage(bitmapimage);
        //graphics.SmoothingMode = SmoothingMode.AntiAlias;
        //graphics.InterpolationMode = InterpolationMode.HighQualityBicubic;
        SKPaint textPaint = new SKPaint()
        {
            IsAntialias = true,
            Color = SKColors.White,
            TextSize = 14,
            Typeface = SKTypeface.FromFamilyName("Arial")
        };
        //PointF drawPointAddress = new PointF(30.0F, 25.0F);

        canvas.DrawText(printMessage, 30, 40, textPaint);
        //graphics.DrawString(String.Format("{0:N0}", cc.sn) + " of 16,777,216 on Network: 1", new Font("Arial", 10), Brushes.White, drawPointAddress);

        //ImageConverter converter = new ImageConverter();
        //byte[] snBytes = (byte[])converter.ConvertTo(bitmapimage, typeof(byte[]));
        SKImage image = SKImage.FromBitmap(bitmapimage);
        SKData data = image.Encode(SKEncodedImageFormat.Jpeg, 100);
        byte[] snBytes = data.ToArray();

        ArrayList<byte> b1 = new ArrayList<byte>(snBytes);
        ArrayList<byte> b2 = new ArrayList<byte>(ccArray);
        b1.InsertRange(4, b2);

        if (tag == "random")
        {
            Random r = new Random();
            int rInt = r.Next(100000, 1000000); //for ints
            tag = rInt.ToString();
        }

        String fileName = targetPath;
        File.WriteAllBytes(fileName, b1.ToArray());
        Console.Out.WriteLine("Writing to " + fileName);
        //CoreLogger.Log("Writing to " + fileName);
        return fileSavedSuccessfully;
    }//end write JPEG
    public String bytesToHexString(byte[] data)
    {
        if (data == null)
        {
            throw new ArgumentNullException("data");
        }

        int length = data.Length;
        char[] hex = new char[length * 2];
        int num1 = 0;
        for (int index = 0; index < length * 2; index += 2)
        {
            byte num2 = data[num1++];
            hex[index] = GetHexValue(num2 / 0x10);
            hex[index + 1] = GetHexValue(num2 % 0x10);
        }
        return new String(hex);
    }//End NewConverted

    private char GetHexValue(int i)
    {
        if (i < 10)
        {
            return (char)(i + 0x30);
        }
        return (char)((i - 10) + 0x41);
    }//end GetHexValue

    /* Writes a JPEG To the Export Folder */

    /* OPEN FILE AND READ ALL CONTENTS AS BYTE ARRAY */
    public byte[] readAllBytes(String fileName)
    {
        byte[] buffer = null;
        using (FileStream fs = new FileStream(fileName, FileMode.Open, FileAccess.Read))
        {
            buffer = new byte[fs.Length];
            int fileLength = Convert.ToInt32(fs.Length);
            fs.Read(buffer, 0, fileLength);
        }
        return buffer;
    }//end read all bytes

    public boolean writeTo(String folder, CloudCoin cc)
    {
        //CoinUtils cu = new CoinUtils(cc);
            const String quote = "\"";
            const String tab = "\t";
        String wholeJson = "{" + Environment.NewLine; //{
        boolean alreadyExists = true;
        String json = this.setJSON(cc);
        if (!File.Exists(folder + cc.FileName() + ".stack"))
        {
            wholeJson += tab + quote + "cloudcoin" + quote + ": [" + Environment.NewLine; // "cloudcoin" : [
            wholeJson += json;
            wholeJson += Environment.NewLine + tab + "]" + Environment.NewLine + "}";
            File.WriteAllText(folder + cc.FileName() + ".stack", wholeJson);
        }
        else
        {
            if (folder.Contains("Counterfeit") || folder.Contains("Trash"))
            {
                //Let the program delete it
                alreadyExists = false;
                return alreadyExists;
            }
            else if (folder.Contains("Imported"))
            {
                File.Delete(folder + cc.FileName() + ".stack");
                File.WriteAllText(folder + cc.FileName() + ".stack", wholeJson);
                alreadyExists = false;
                return alreadyExists;
            }
            else
            {
                Console.WriteLine(cc.FileName() + ".stack" + " already exists in the folder " + folder);
                //CoreLogger.Log(cu.fileName + ".stack" + " already exists in the folder " + folder);
                return alreadyExists;

            }//end else

        }//File Exists
        File.WriteAllText(folder + cc.FileName() + ".stack", wholeJson);
        alreadyExists = false;
        return alreadyExists;

    }//End Write To

    public void overWrite(String folder, CloudCoin cc)
    {
        //CoinUtils cu = new CoinUtils(cc);
            const String quote = "\"";
            const String tab = "\t";
        String wholeJson = "{" + Environment.NewLine; //{
        String json = this.setJSON(cc);

        wholeJson += tab + quote + "cloudcoin" + quote + ": [" + Environment.NewLine; // "cloudcoin" : [
        wholeJson += json;
        wholeJson += Environment.NewLine + tab + "]" + Environment.NewLine + "}";

        File.WriteAllText(folder + cc.FileName() + ".stack", wholeJson);
    }//End Overwrite

    public CloudCoin loadOneCloudCoinFromJPEGFile(String loadFilePath)
    {
        /* GET the first 455 bytes of he jpeg where the coin is located */
        String wholeString = "";
        byte[] jpegHeader = new byte[455];
        Console.Out.WriteLine("Load file path " + loadFilePath);
        using (FileStream fileStream = new FileStream(loadFilePath, FileMode.Open, FileAccess.Read))
        {
            try
            {
                int count;                            // actual number of bytes read
                int sum = 0;                          // total number of bytes read

                // read until Read method returns 0 (end of the stream has been reached)
                while ((count = fileStream.Read(jpegHeader, sum, 455 - sum)) > 0)
                    sum += count;  // sum is a buffer offset for next reading
            }
            finally { }
        }
        wholeString = bytesToHexString(jpegHeader);
        CloudCoin returnCC = this.parseJpeg(wholeString);
        // Console.Out.WriteLine("From FileUtils returnCC.fileName " + returnCC.fileName);
        return returnCC;
    }//end load one CloudCoin from JSON

    public CloudCoin parseJpeg(String wholeString)
    {

        CloudCoin cc = new CloudCoin();
        int startAn = 40;
        for (int i = 0; i < 25; i++)
        {
            cc.an.Add(wholeString.SubString(startAn, 32));
            //cc.an[i] = wholeString.SubString(startAn, 32);
            // Console.Out.WriteLine(i +": " + cc.an[i]);
            startAn += 32;
        }

        // end for
        cc.aoid = null;
        // wholeString.subString( 840, 895 );
        //cc.hp = 25;
        // Integer.parseInt(wholeString.subString( 896, 896 ), 16);
        cc.ed = wholeString.SubString(898, 4);
        cc.nn = Convert.ToInt32(wholeString.SubString(902, 2), 16);
        cc.sn = Convert.ToInt32(wholeString.SubString(904, 6), 16);
        cc.pown = "uuuuuuuuuuuuuuuuuuuuuuuuu";
        //  Console.Out.WriteLine("parseJpeg cc.fileName " + cc.fileName);
        return cc;
    }// end parse Jpeg

    // en d json test
    public byte[] hexStringToByteArray(String HexString)
    {
        int NumberChars = HexString.Length;
        byte[] bytes = new byte[NumberChars / 2];
        for (int i = 0; i < NumberChars; i += 2)
        {
            bytes[i / 2] = Convert.ToByte(HexString.SubString(i, 2), 16);
        }
        return bytes;
    }//End hex String to byte array
}
