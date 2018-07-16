package com.cloudcore.authenticator.coreclasses;

import com.cloudcore.authenticator.core.CloudCoin;
import com.cloudcore.authenticator.core.Config;
import com.cloudcore.authenticator.core.IFileSystem;
import com.cloudcore.authenticator.core.Stack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Exporter
{
    /* INSTANCE VARIABLES */
    IFileSystem fileSystem;


    /* CONSTRUCTOR */
    public Exporter(IFileSystem fileUtils)
    {

        this.fileSystem = fileUtils;
    }

    /* PUBLIC METHODS */

    public void writeQRCodeFiles(int m1, int m5, int m25, int m100, int m250, String tag)
    {
        int totalSaved = m1 + (m5 * 5) + (m25 * 25) + (m100 * 100) + (m250 * 250);// Total value of all coins
        int coinCount = m1 + m5 + m25 + m100 + m250; // Total number of coins
        String[] coinsToDelete = new String[coinCount];
        String[] bankedFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.BankFolder, Config.allowedExtensions);
        String[] frackedFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.FrackedFolder, Config.allowedExtensions);
        String[] partialFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.PartialFolder, Config.allowedExtensions);

        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(bankedFileNames));
        list.addAll(Arrays.asList(frackedFileNames));
        list.addAll(Arrays.asList(partialFileNames));

        bankedFileNames = list.toArray(new String[0]); // Add the two arrays together

        String path = this.fileSystem.ExportFolder;//the word path is shorter than other stuff

        // Look at all the money files and choose the ones that are needed.
        for (int i = 0; i < bankedFileNames.length; i++)
        {
            String bankFileName = (this.fileSystem.BankFolder + bankedFileNames[i]);
            String frackedFileName = (this.fileSystem.FrackedFolder + bankedFileNames[i]);
            String partialFileName = (this.fileSystem.PartialFolder + bankedFileNames[i]);

            // Get denominiation
            String denomination = bankedFileNames[i].split(".")[0];
            try
            {
                switch (denomination)
                {
                    case "1":
                        if (m1 > 0)
                        {
                            this.qrCodeWriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m1--;
                        }
                        break;
                    case "5":
                        if (m5 > 0)
                        {

                            this.qrCodeWriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m5--;
                        }
                        break;
                    case "25":
                        if (m25 > 0)
                        {

                            this.qrCodeWriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m25--;
                        }
                        break;

                    case "100":
                        if (m100 > 0)
                        {
                            this.qrCodeWriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m100--;
                        }
                        break;

                    case "250":
                        if (m250 > 0)
                        { this.qrCodeWriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m250--; }
                        break;
                }//end switch

                if (m1 == 0 && m5 == 0 && m25 == 0 && m100 == 0 && m250 == 0)// end if file is needed to write jpeg
                {
                    break;// Break if all the coins have been called for.
                }
            }
            catch (Exception ex)
            {
                System.out.println(ex);
                ex.printStackTrace();
                //CoreLogger.Log(ex.toString());
            }
        }// for each 1 note
    }//end write all jpegs

    public void writeBarCode417CodeFiles(int m1, int m5, int m25, int m100, int m250, String tag)
    {
        int totalSaved = m1 + (m5 * 5) + (m25 * 25) + (m100 * 100) + (m250 * 250);// Total value of all coins
        int coinCount = m1 + m5 + m25 + m100 + m250; // Total number of coins
        String[] coinsToDelete = new String[coinCount];
        String[] bankedFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.BankFolder, Config.allowedExtensions);
        String[] frackedFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.FrackedFolder, Config.allowedExtensions);
        String[] partialFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.PartialFolder, Config.allowedExtensions);

        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(bankedFileNames));
        list.addAll(Arrays.asList(frackedFileNames));
        list.addAll(Arrays.asList(partialFileNames));

        bankedFileNames = list.toArray(new String[0]); // Add the two arrays together

        String path = this.fileSystem.ExportFolder;//the word path is shorter than other stuff

        // Look at all the money files and choose the ones that are needed.
        for (int i = 0; i < bankedFileNames.length; i++)
        {
            String bankFileName = (this.fileSystem.BankFolder + bankedFileNames[i]);
            String frackedFileName = (this.fileSystem.FrackedFolder + bankedFileNames[i]);
            String partialFileName = (this.fileSystem.PartialFolder + bankedFileNames[i]);

            // Get denominiation
            String denomination = bankedFileNames[i].split(".")[0];
            try
            {
                switch (denomination)
                {
                    case "1":
                        if (m1 > 0)
                        {
                            this.barCode417WriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m1--;
                        }
                        break;
                    case "5":
                        if (m5 > 0)
                        {

                            this.barCode417WriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m5--;
                        }
                        break;
                    case "25":
                        if (m25 > 0)
                        {

                            this.barCode417WriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m25--;
                        }
                        break;

                    case "100":
                        if (m100 > 0)
                        {
                            this.barCode417WriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m100--;
                        }
                        break;

                    case "250":
                        if (m250 > 0)
                        { this.barCode417WriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m250--; }
                        break;
                }//end switch

                if (m1 == 0 && m5 == 0 && m25 == 0 && m100 == 0 && m250 == 0)// end if file is needed to write jpeg
                {
                    break;// Break if all the coins have been called for.
                }
            }
            catch (Exception ex)
            {
                System.out.println(ex);
                ex.printStackTrace();
                //CoreLogger.Log(ex.toString());
            }
        }// for each 1 note
    }//end write all jpegs

    public void writeJPEGFiles(int m1, int m5, int m25, int m100, int m250, String tag)
    {
        int totalSaved = m1 + (m5 * 5) + (m25 * 25) + (m100 * 100) + (m250 * 250);// Total value of all coins
        int coinCount = m1 + m5 + m25 + m100 + m250; // Total number of coins
        String[] coinsToDelete = new String[coinCount];
        String[] bankedFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.BankFolder, Config.allowedExtensions);
        String[] frackedFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.FrackedFolder, Config.allowedExtensions);
        String[] partialFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.PartialFolder, Config.allowedExtensions);

        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(bankedFileNames));
        list.addAll(Arrays.asList(frackedFileNames));
        list.addAll(Arrays.asList(partialFileNames));

        bankedFileNames = list.toArray(new String[0]); // Add the two arrays together

        String path = this.fileSystem.ExportFolder;//the word path is shorter than other stuff

        // Look at all the money files and choose the ones that are needed.
        for (int i = 0; i < bankedFileNames.length; i++)
        {
            String bankFileName = (this.fileSystem.BankFolder + bankedFileNames[i]);
            String frackedFileName = (this.fileSystem.FrackedFolder + bankedFileNames[i]);
            String partialFileName = (this.fileSystem.PartialFolder + bankedFileNames[i]);

            // Get denominiation
            String denomination = bankedFileNames[i].split(".")[0];
            try
            {
                switch (denomination)
                {
                    case "1":
                        if (m1 > 0)
                        {
                            this.jpegWriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m1--;
                        }
                        break;
                    case "5":
                        if (m5 > 0)
                        {

                            this.jpegWriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m5--;
                        }
                        break;
                    case "25":
                        if (m25 > 0)
                        {

                            this.jpegWriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m25--;
                        }
                        break;

                    case "100":
                        if (m100 > 0)
                        {
                            this.jpegWriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m100--;
                        }
                        break;

                    case "250":
                        if (m250 > 0)
                        { this.jpegWriteOne(path, tag, bankFileName, frackedFileName, partialFileName); m250--; }
                        break;
                }//end switch

                if (m1 == 0 && m5 == 0 && m25 == 0 && m100 == 0 && m250 == 0)// end if file is needed to write jpeg
                {
                    break;// Break if all the coins have been called for.
                }
            }
            catch (Exception ex)
            {
                System.out.println(ex);
                ex.printStackTrace();
                //CoreLogger.Log(ex.toString());
            }
        }// for each 1 note
    }//end write all jpegs

    /* Write JSON to .stack File  */
    public boolean writeJSONFile(int m1, int m5, int m25, int m100, int m250, String tag)
    {
        boolean jsonExported = true;
        int totalSaved = m1 + (m5 * 5) + (m25 * 25) + (m100 * 100) + (m250 * 250);
        // Track the total coins
        int coinCount = m1 + m5 + m25 + m100 + m250;
        String[] coinsToDelete = new String[coinCount];
        String[] bankedFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.BankFolder, Config.allowedExtensions);
        String[] frackedFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.FrackedFolder, Config.allowedExtensions);
        String[] partialFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.PartialFolder, Config.allowedExtensions);

        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(bankedFileNames));
        list.addAll(Arrays.asList(frackedFileNames));
        list.addAll(Arrays.asList(partialFileNames));

        bankedFileNames = list.toArray(new String[0]); // Add the two arrays together


        // Check to see the denomination by looking at the file start
        int c = 0;
        // c= counter
        String json = "{" + System.lineSeparator();
        json = json + "\t\"cloudcoin\": " + System.lineSeparator();
        json = json + "\t[" + System.lineSeparator();
        String bankFileName;
        String frackedFileName;
        String partialFileName;
        String denomination;
        Stack stack = new Stack();

        // Put all the JSON together and add header and footer
        for (int i = 0; (i < bankedFileNames.length); i++)
        {
            denomination = bankedFileNames[i].split(".")[0];
            bankFileName = this.fileSystem.BankFolder + bankedFileNames[i];//File name in bank folder
            frackedFileName = this.fileSystem.FrackedFolder + bankedFileNames[i];//File name in fracked folder
            partialFileName = this.fileSystem.PartialFolder + bankedFileNames[i];
            if (denomination.equals("1") && m1 > 0)
            {
                if (c != 0)//This is the json seperator between each coin. It is not needed on the first coin
                {
                    json += ",\n";
                }

                if (Files.exists(Paths.get(bankFileName))) // Is it a bank file
                {

                    CloudCoin coinNote = fileSystem.LoadCoin(bankFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = bankFileName;
                    c++;
                }
                else if (Files.exists(Paths.get(partialFileName))) // Is it a partial file
                {
                    CloudCoin coinNote = fileSystem.LoadCoin(partialFileName);
                    //coinNote = fileSystem.loa
                    coinNote.aoid = null;//Clear all owner data
                    json = json + fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = partialFileName;
                    c++;
                }
                else
                {
                    CloudCoin coinNote = this.fileSystem.LoadCoin(frackedFileName);
                    coinNote.aoid = null;
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = frackedFileName;
                    c++;
                }

                m1--;
                // Get the clean JSON of the coin
            }// end if coin is a 1

            if (denomination.equals("5") && m5 > 0)
            {
                if ((c != 0))
                {
                    json += ",\n";
                }

                if (Files.exists(Paths.get(bankFileName)))
                {
                    CloudCoin coinNote = this.fileSystem.LoadCoin(bankFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = bankFileName;
                    c++;
                }
                else if (Files.exists(Paths.get(partialFileName))) // Is it a partial file
                {
                    CloudCoin coinNote = fileSystem.LoadCoin(partialFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = partialFileName;
                    c++;
                }
                else
                {
                    CloudCoin coinNote = this.fileSystem.LoadCoin(frackedFileName);
                    coinNote.aoid = null;
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = frackedFileName;
                    c++;
                }

                m5--;
            } // end if coin is a 5

            if (denomination.equals("25") && m25 > 0)
            {
                if ((c != 0))
                {
                    json += ",\n";
                }

                if (Files.exists(Paths.get(bankFileName)))
                {
                    CloudCoin coinNote = this.fileSystem.LoadCoin(bankFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = bankFileName;
                    c++;
                }
                else if (Files.exists(Paths.get(partialFileName))) // Is it a partial file
                {
                    CloudCoin coinNote = fileSystem.LoadCoin(partialFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = partialFileName;
                    c++;
                }
                else
                {
                    CloudCoin coinNote = this.fileSystem.LoadCoin(frackedFileName);
                    coinNote.aoid = null;
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = frackedFileName;
                    c++;
                }

                m25--;
            }// end if coin is a 25

            if (denomination.equals("100") && m100 > 0)
            {
                if ((c != 0))
                {
                    json += ",\n";
                }

                if (Files.exists(Paths.get(bankFileName)))
                {
                    CloudCoin coinNote = this.fileSystem.LoadCoin(bankFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = bankFileName;
                    c++;
                }
                else if (Files.exists(Paths.get(partialFileName))) // Is it a partial file
                {
                    CloudCoin coinNote = fileSystem.LoadCoin(partialFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = partialFileName;
                    c++;
                }
                else
                {
                    CloudCoin coinNote = this.fileSystem.LoadCoin(frackedFileName);
                    coinNote.aoid = null;
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = frackedFileName;
                    c++;
                }

                m100--;
            } // end if coin is a 100

            if (denomination.equals("250") && m250 > 0)
            {
                if ((c != 0))
                {
                    json += ",\n";
                }

                if (Files.exists(Paths.get(bankFileName)))
                {
                    CloudCoin coinNote = this.fileSystem.LoadCoin(bankFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = bankFileName;
                    c++;
                }
                else if (Files.exists(Paths.get(partialFileName))) // Is it a partial file
                {
                    CloudCoin coinNote = fileSystem.LoadCoin(partialFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = partialFileName;
                    c++;
                }
                else
                {
                    CloudCoin coinNote = this.fileSystem.LoadCoin(frackedFileName);
                    coinNote.aoid = null;
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = frackedFileName;
                    c++;
                }

                m250--;
            }// end if coin is a 250

            if (m1 == 0 && m5 == 0 && m25 == 0 && m100 == 0 && m250 == 0)
            {
                break;
            } // Break if all the coins have been called for.
        }// end for each coin needed

        /*WRITE JSON TO FILE*/
        json = json + "\t] " + System.lineSeparator();
        json += "}";
        String filename = (this.fileSystem.ExportFolder + File.separator + totalSaved + ".CloudCoins." + tag + ".stack");
        if (Files.exists(Paths.get(filename)))
        {
            // tack on a random number if a file already exists with the same tag
            Random rnd = new Random();
            int tagrand = rnd.nextInt(999);
            filename = (this.fileSystem.ExportFolder + File.separator + totalSaved + ".CloudCoins." + tag + tagrand + ".stack");
        }//end if file exists

        try {
            Files.write(Paths.get(filename), json.getBytes(StandardCharsets.UTF_8));
            System.out.println("Writing to : ");
            //CoreLogger.Log("Writing to : " + filename);
            System.out.println(filename);
            /*DELETE FILES THAT HAVE BEEN EXPORTED*/
            for (int cc = 0; cc < coinsToDelete.length; cc++) {
                // System.out.println("Deleting " + coinsToDelete[cc]);
                if (coinsToDelete[cc] != null) {
                    Files.delete(Paths.get(coinsToDelete[cc]));
                }
            }//end for all coins to delete
        } catch (IOException e) {
            e.printStackTrace();
        }

        // end if write was good
        return jsonExported;
    }//end write json to file

    public boolean writeJSONFile(int m1, int m5, int m25, int m100, int m250, String tag, int mode, String backupDir)
    {
        boolean jsonExported = true;
        int totalSaved = m1 + (m5 * 5) + (m25 * 25) + (m100 * 100) + (m250 * 250);
        // Track the total coins
        int coinCount = m1 + m5 + m25 + m100 + m250;
        String[] coinsToDelete = new String[coinCount];
        String[] bankedFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.BankFolder, Config.allowedExtensions);
        String[] frackedFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.FrackedFolder, Config.allowedExtensions);
        String[] partialFileNames = FileSystem.GetFilesNamesArray(this.fileSystem.PartialFolder, Config.allowedExtensions);

        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(bankedFileNames));
        list.addAll(Arrays.asList(frackedFileNames));
        list.addAll(Arrays.asList(partialFileNames));

        bankedFileNames = list.toArray(new String[0]); // Add the two arrays together


        // Check to see the denomination by looking at the file start
        int c = 0;
        // c= counter
        String json = "{" + System.lineSeparator();
        json = json + "\t\"cloudcoin\": " + System.lineSeparator();
        json = json + "\t[" + System.lineSeparator();
        String bankFileName;
        String frackedFileName;
        String partialFileName;
        String denomination;

        // Put all the JSON together and add header and footer
        for (int i = 0; (i < bankedFileNames.length); i++)
        {
            denomination = bankedFileNames[i].split(".")[0];
            bankFileName = this.fileSystem.BankFolder + bankedFileNames[i];//File name in bank folder
            frackedFileName = this.fileSystem.FrackedFolder + bankedFileNames[i];//File name in fracked folder
            partialFileName = this.fileSystem.PartialFolder + bankedFileNames[i];
            if (denomination.equals("1") && m1 > 0)
            {
                if (c != 0)//This is the json seperator between each coin. It is not needed on the first coin
                {
                    json += ",\n";
                }

                if (Files.exists(Paths.get(bankFileName))) // Is it a bank file
                {
                    CloudCoin coinNote = fileSystem.loadOneCloudCoinFromJsonFile(bankFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = bankFileName;
                    c++;
                }
                else if (Files.exists(Paths.get(partialFileName))) // Is it a partial file
                {
                    CloudCoin coinNote = fileSystem.loadOneCloudCoinFromJsonFile(partialFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = partialFileName;
                    c++;
                }
                else
                {
                    CloudCoin coinNote = this.fileSystem.loadOneCloudCoinFromJsonFile(frackedFileName);
                    coinNote.aoid = null;
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = frackedFileName;
                    c++;
                }

                m1--;
                // Get the clean JSON of the coin
            }// end if coin is a 1

            if (denomination.equals("5") && m5 > 0)
            {
                if ((c != 0))
                {
                    json += ",\n";
                }

                if (Files.exists(Paths.get(bankFileName)))
                {
                    CloudCoin coinNote = this.fileSystem.loadOneCloudCoinFromJsonFile(bankFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = bankFileName;
                    c++;
                }
                else if (Files.exists(Paths.get(partialFileName))) // Is it a partial file
                {
                    CloudCoin coinNote = fileSystem.loadOneCloudCoinFromJsonFile(partialFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = partialFileName;
                    c++;
                }
                else
                {
                    CloudCoin coinNote = this.fileSystem.loadOneCloudCoinFromJsonFile(frackedFileName);
                    coinNote.aoid = null;
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = frackedFileName;
                    c++;
                }

                m5--;
            } // end if coin is a 5

            if (denomination.equals("25") && m25 > 0)
            {
                if ((c != 0))
                {
                    json += ",\n";
                }

                if (Files.exists(Paths.get(bankFileName)))
                {
                    CloudCoin coinNote = this.fileSystem.loadOneCloudCoinFromJsonFile(bankFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = bankFileName;
                    c++;
                }
                else if (Files.exists(Paths.get(partialFileName))) // Is it a partial file
                {
                    CloudCoin coinNote = fileSystem.loadOneCloudCoinFromJsonFile(partialFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = partialFileName;
                    c++;
                }
                else
                {
                    CloudCoin coinNote = this.fileSystem.loadOneCloudCoinFromJsonFile(frackedFileName);
                    coinNote.aoid = null;
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = frackedFileName;
                    c++;
                }

                m25--;
            }// end if coin is a 25

            if (denomination.equals("100") && m100 > 0)
            {
                if ((c != 0))
                {
                    json += ",\n";
                }

                if (Files.exists(Paths.get(bankFileName)))
                {
                    CloudCoin coinNote = this.fileSystem.loadOneCloudCoinFromJsonFile(bankFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = bankFileName;
                    c++;
                }
                else if (Files.exists(Paths.get(partialFileName))) // Is it a partial file
                {
                    CloudCoin coinNote = fileSystem.loadOneCloudCoinFromJsonFile(partialFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = partialFileName;
                    c++;
                }
                else
                {
                    CloudCoin coinNote = this.fileSystem.loadOneCloudCoinFromJsonFile(frackedFileName);
                    coinNote.aoid = null;
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = frackedFileName;
                    c++;
                }

                m100--;
            } // end if coin is a 100

            if (denomination.equals("250") && m250 > 0)
            {
                if ((c != 0))
                {
                    json += ",\n";
                }

                if (Files.exists(Paths.get(bankFileName)))
                {
                    CloudCoin coinNote = this.fileSystem.loadOneCloudCoinFromJsonFile(bankFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = bankFileName;
                    c++;
                }
                else if (Files.exists(Paths.get(partialFileName))) // Is it a partial file
                {
                    CloudCoin coinNote = fileSystem.loadOneCloudCoinFromJsonFile(partialFileName);
                    coinNote.aoid = null;//Clear all owner data
                    json = json + fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = partialFileName;
                    c++;
                }
                else
                {
                    CloudCoin coinNote = this.fileSystem.loadOneCloudCoinFromJsonFile(frackedFileName);
                    coinNote.aoid = null;
                    json = json + this.fileSystem.setJSON(coinNote);
                    coinsToDelete[c] = frackedFileName;
                    c++;
                }

                m250--;
            }// end if coin is a 250

            if (m1 == 0 && m5 == 0 && m25 == 0 && m100 == 0 && m250 == 0)
            {
                break;
            } // Break if all the coins have been called for.
            String status = String.format("exported %d of %d coin.", i, bankedFileNames.length);
            int percentCompleted = (i + 1) * 100 / bankedFileNames.length;
        }// end for each coin needed

        /*WRITE JSON TO FILE*/
        json = json + "\t] " + System.lineSeparator();
        json += "}";
        String filename = (this.fileSystem.ExportFolder + File.separator + totalSaved + ".CloudCoins." + tag + ".stack");

        if (mode == 1)
        {
            filename = (backupDir + File.separator + totalSaved + ".CloudCoins." + tag + ".stack");
        }
        if (Files.exists(Paths.get(filename)))
        {
            // tack on a random number if a file already exists with the same tag
            Random rnd = new Random();
            int tagrand = rnd.nextInt(999);
            filename = (this.fileSystem.ExportFolder + File.separator + totalSaved + ".CloudCoins." + tag + tagrand + ".stack");
        }//end if file exists

        try {
            Files.write(Paths.get(filename), json.getBytes(StandardCharsets.UTF_8));
            System.out.println("Writing to : ");
            //CoreLogger.Log("Writing to : " + filename);
            System.out.println(filename);
            /*DELETE FILES THAT HAVE BEEN EXPORTED*/
            for (int cc = 0; cc < coinsToDelete.length; cc++) {
                // System.out.println("Deleting " + coinsToDelete[cc]);
                if (coinsToDelete[cc] != null) {
                    Files.delete(Paths.get(coinsToDelete[cc]));
                }
            }//end for all coins to delete
        } catch (IOException e) {
            e.printStackTrace();
        }

        // end if write was good
        return jsonExported;
    }//end write json to file


    /* PRIVATE METHODS */
    private void qrCodeWriteOne(String path, String tag, String bankFileName, String frackedFileName, String partialFileName)
    {
        try {
            if (Files.exists(Paths.get(bankFileName)))//If the file is a bank file, export a good bank coin
            {
                CloudCoin jpgCoin = this.fileSystem.LoadCoin(bankFileName);
                if (this.fileSystem.writeQrCode(jpgCoin, tag))//If the jpeg writes successfully
                {
                    //String json = JsonConvert.SerializeObject(jpgCoin);
                    //var barcode = new Barcode(json, Settings.Default);
                    //barcode.Canvas.SaveBmp(jpgCoin.FileName+".jpg");
                    Files.delete(Paths.get(bankFileName));//Delete the files if they have been written to
                }//end if write was good.
            } else if (Files.exists(Paths.get(partialFileName)))//If the file is a bank file, export a good bank coin
            {
                CloudCoin jpgCoin = this.fileSystem.LoadCoin(partialFileName);
                if (this.fileSystem.writeQrCode(jpgCoin, tag))//If the jpeg writes successfully
                {
                    Files.delete(Paths.get(partialFileName));//Delete the files if they have been written to
                }//end if write was good.
            } else//Export a fracked coin.
            {
                CloudCoin jpgCoin = fileSystem.LoadCoin(frackedFileName);
                if (this.fileSystem.writeQrCode(jpgCoin, tag)) {
                    Files.delete(Paths.get(frackedFileName));//Delete the files if they have been written to
                }//end if
            }//end else
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//End write one jpeg

    private void barCode417WriteOne(String path, String tag, String bankFileName, String frackedFileName, String partialFileName)
    {
        try {
            if (Files.exists(Paths.get(bankFileName)))//If the file is a bank file, export a good bank coin
            {
                CloudCoin jpgCoin = this.fileSystem.LoadCoin(bankFileName);
                if (this.fileSystem.writeBarCode(jpgCoin, tag))//If the jpeg writes successfully
                {
                    //String json = JsonConvert.SerializeObject(jpgCoin);
                    //var barcode = new Barcode(json, Settings.Default);
                    //barcode.Canvas.SaveBmp(jpgCoin.FileName+".jpg");
                    Files.delete(Paths.get(bankFileName));//Delete the files if they have been written to
                }//end if write was good.
            } else if (Files.exists(Paths.get(partialFileName)))//If the file is a bank file, export a good bank coin
            {
                CloudCoin jpgCoin = this.fileSystem.LoadCoin(partialFileName);
                if (this.fileSystem.writeBarCode(jpgCoin, tag))//If the jpeg writes successfully
                {
                    Files.delete(Paths.get(partialFileName));//Delete the files if they have been written to
                }//end if write was good.
            } else//Export a fracked coin.
            {
                CloudCoin jpgCoin = fileSystem.LoadCoin(frackedFileName);
                if (this.fileSystem.writeBarCode(jpgCoin, tag)) {
                    Files.delete(Paths.get(frackedFileName));//Delete the files if they have been written to
                }//end if
            }//end else
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//End write one jpeg


    /* PRIVATE METHODS */
    private void jpegWriteOne(String path, String tag, String bankFileName, String frackedFileName, String partialFileName)
    {
        try {
            if (Files.exists(Paths.get(bankFileName)))//If the file is a bank file, export a good bank coin
            {
                CloudCoin jpgCoin = this.fileSystem.LoadCoin(bankFileName);
                if (this.fileSystem.writeJpeg(jpgCoin, tag))//If the jpeg writes successfully
                {
                    Files.delete(Paths.get(bankFileName));//Delete the files if they have been written to
                }//end if write was good.
            } else if (Files.exists(Paths.get(partialFileName)))//If the file is a bank file, export a good bank coin
            {
                CloudCoin jpgCoin = this.fileSystem.LoadCoin(partialFileName);
                if (this.fileSystem.writeJpeg(jpgCoin, tag))//If the jpeg writes successfully
                {
                    Files.delete(Paths.get(partialFileName));//Delete the files if they have been written to
                }//end if write was good.
            } else//Export a fracked coin.
            {
                CloudCoin jpgCoin = fileSystem.LoadCoin(frackedFileName);
                if (this.fileSystem.writeJpeg(jpgCoin, tag)) {
                    Files.delete(Paths.get(frackedFileName));//Delete the files if they have been written to
                }//end if
            }//end else
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//End write one jpeg
}// end exporter class
