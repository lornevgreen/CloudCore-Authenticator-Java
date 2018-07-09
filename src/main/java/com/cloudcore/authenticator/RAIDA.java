package com.cloudcore.authenticator;

import com.cloudcore.authenticator.core.CloudCoin;
import com.cloudcore.authenticator.core.IFileSystem;
import com.cloudcore.authenticator.core.Node;
import com.cloudcore.authenticator.utils.SimpleLogger;

import java.util.ArrayList;

public class RAIDA {

    public static RAIDA MainNetwork;
    public Node[] nodes = new Node[Config.NodeCount];
    public IFileSystem FS;
    public static IFileSystem FileSystem;
    public CloudCoin coin;
    public IEnumerable<CloudCoin> coins;
    public MultiDetectRequest multiRequest;
    public Network network;
    public int NetworkNumber=1;
    public static ArrayList<RAIDA> networks = new ArrayList<>();
    public static RAIDA ActiveRAIDA;
    public static String Workspace;
    public static SimpleLogger logger;

    public static void ProcessCoins() {
        boolean ChangeANs = true;
        var networks = (from x in IFileSystem.importCoins select x.nn).Distinct().ToList();
        long ts;
        long before = System.currentTimeMillis();
        long after;

        foreach (var nn in networks)
        {
            ActiveRAIDA = (from x in RAIDA.networks where x.NetworkNumber == nn select x).FirstOrDefault();
            int NetworkExists = (from x in RAIDA.networks where x.NetworkNumber == nn select x).Count();
            if (NetworkExists > 0)
            {
                //updateLog("Starting Coins detection for Network " + nn);
                await ProcessNetworkCoins(nn, ChangeANs);
                //updateLog("Coins detection for Network " + nn + "Finished.");
            }
        }
        after = System.currentTimeMillis();
        ts = after - before;

        System.out.println("Detection Completed in : " + ts / 1000);
        // TODO: updateLog("Detection Completed in : " + ts.TotalMilliseconds / 1000);
    }

    public async static Task ProcessNetworkCoins(int NetworkNumber, bool ChangeANS= true)
    {
        IFileSystem FS = FileSystem;
        FileSystem.LoadFileSystem();
        FileSystem.DetectPreProcessing();

        var predetectCoins = FS.LoadFolderCoins(FS.PreDetectFolder);
        predetectCoins = (from x in predetectCoins
        where x.nn == NetworkNumber
        select x).ToList();

        IFileSystem.predetectCoins = predetectCoins;

        RAIDA raida = (from x in networks
        where x.NetworkNumber == NetworkNumber
        select x).FirstOrDefault();
        if (raida == null)
            return;
        // Process Coins in Lots of 200. Can be changed from Config File
        int LotCount = predetectCoins.Count() / Config.MultiDetectLoad;
        if (predetectCoins.Count() % Config.MultiDetectLoad > 0) LotCount++;
        ProgressChangedEventArgs pge = new ProgressChangedEventArgs();

        int CoinCount = 0;
        int totalCoinCount = predetectCoins.Count();
        for (int i = 0; i < LotCount; i++)
        {
            //Pick up 200 Coins and send them to RAIDA
            var coins = predetectCoins.Skip(i * Config.MultiDetectLoad).Take(200);
            try
            {
                raida.coins = coins;
            }
            catch(Exception e)
            {
                Console.WriteLine(e.Message);
            }
            var tasks = raida.GetMultiDetectTasks(coins.ToArray(), Config.milliSecondsToTimeOut,ChangeANS);
            try
            {
                String requestFileName = Utils.RandomString(16).ToLower() + DateTime.Now.ToString("yyyyMMddHHmmss") + ".stack";
                // Write Request To file before detect
                FS.WriteCoinsToFile(coins, FS.RequestsFolder + requestFileName);
                await Task.WhenAll(tasks.AsParallel().Select(async task => await task()));
                int j = 0;
                foreach (var coin in coins)
                {
                    coin.pown = "";
                    for (int k = 0; k < CloudCoinCore.Config.NodeCount; k++)
                    {
                        coin.response[k] = raida.nodes[k].MultiResponse.responses[j];
                        coin.pown += coin.response[k].outcome.SubString(0, 1);
                    }
                    int countp = coin.response.Where(x => x.outcome == "pass").Count();
                    int countf = coin.response.Where(x => x.outcome == "fail").Count();
                    coin.PassCount = countp;
                    coin.FailCount = countf;
                    CoinCount++;


                    updateLog("No. " + CoinCount + ". Coin Deteced. S. No. - " + coin.sn + ". Pass Count - " + coin.PassCount + ". Fail Count  - " + coin.FailCount + ". Result - " + coin.DetectionResult + "." + coin.pown);
                    System.out.println("Coin Deteced. S. No. - " + coin.sn + ". Pass Count - " + coin.PassCount + ". Fail Count  - " + coin.FailCount + ". Result - " + coin.DetectionResult);
                    //coin.sortToFolder();
                    pge.MinorProgress = (CoinCount) * 100 / totalCoinCount;
                    System.out.println("Minor Progress- " + pge.MinorProgress);
                    raida.OnProgressChanged(pge);
                    j++;
                }
                pge.MinorProgress = (CoinCount - 1) * 100 / totalCoinCount;
                System.out.println("Minor Progress- " + pge.MinorProgress);
                raida.OnProgressChanged(pge);
                FS.WriteCoin(coins, FS.DetectedFolder);
                FS.RemoveCoins(coins, FS.PreDetectFolder);

                updateLog(pge.MinorProgress + " % of Coins on Network " + NetworkNumber + " processed.");
                //FS.WriteCoin(coins, FS.DetectedFolder);

            }
            catch (Exception ex)
            {
                System.out.println(ex.Message);
            }


        }
        pge.MinorProgress = 100;
        System.out.println("Minor Progress- " + pge.MinorProgress);
        raida.OnProgressChanged(pge);
        var detectedCoins = FS.LoadFolderCoins(FS.DetectedFolder);
        //detectedCoins.ForEach(x => x.pown= "ppppppppppppppppppppppppp");

        // Apply Sort to Folder to all detected coins at once.
        updateLog("Starting Sort.....");
        detectedCoins.ForEach(x => x.SortToFolder());
        updateLog("Ended Sort........");

        var passedCoins = (from x in detectedCoins
        where x.folder == FS.BankFolder
        select x).ToList();

        var frackedCoins = (from x in detectedCoins
        where x.folder == FS.FrackedFolder
        select x).ToList();

        var failedCoins = (from x in detectedCoins
        where x.folder == FS.CounterfeitFolder
        select x).ToList();
        var lostCoins = (from x in detectedCoins
        where x.folder == FS.LostFolder
        select x).ToList();
        var suspectCoins = (from x in detectedCoins
        where x.folder == FS.SuspectFolder
        select x).ToList();

        System.out.println("Total Passed Coins - " + (passedCoins.Count()+ frackedCoins.Count()));
        System.out.println("Total Failed Coins - " + failedCoins.Count());
        updateLog("Coin Detection finished.");
        updateLog("Total Passed Coins - " + (passedCoins.Count() + frackedCoins.Count()) + "");
        updateLog("Total Failed Coins - " + failedCoins.Count() + "");
        updateLog("Total Lost Coins - " + lostCoins.Count() + "");
        updateLog("Total Suspect Coins - " + suspectCoins.Count() + "");

        // Move Coins to their respective folders after sort
        FS.MoveCoins(passedCoins, FS.DetectedFolder, FS.BankFolder);
        FS.MoveCoins(frackedCoins, FS.DetectedFolder, FS.FrackedFolder);

        //FS.WriteCoin(failedCoins, FS.CounterfeitFolder, true);
        FS.MoveCoins(lostCoins, FS.DetectedFolder, FS.LostFolder);
        FS.MoveCoins(suspectCoins, FS.DetectedFolder, FS.SuspectFolder);

        // Clean up Detected Folder
        FS.RemoveCoins(failedCoins, FS.DetectedFolder);
        FS.RemoveCoins(lostCoins, FS.DetectedFolder);
        FS.RemoveCoins(suspectCoins, FS.DetectedFolder);

        FS.MoveImportedFiles();

        //after = DateTime.Now;
        //ts = after.Subtract(before);

        //System.out.println("Detection Completed in - " + ts.TotalMilliseconds / 1000);
        //updateLog("Detection Completed in - " + ts.TotalMilliseconds / 1000);


        pge.MinorProgress = 100;
        System.out.println("Minor Progress- " + pge.MinorProgress);
    }
}
