package com.cloudcore.authenticator;

import com.cloudcore.authenticator.core.*;
import com.cloudcore.authenticator.utils.SimpleLogger;
import com.sun.deploy.jcp.controller.Network;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class RAIDA {

    public static RAIDA MainNetwork;
    public Node[] nodes = new Node[Config.NodeCount];
    public MultiDetectRequest multiRequest;
    public Network network;
    public int NetworkNumber=1;

    public static IFileSystem FileSystem;
    public IFileSystem FS;

    public ArrayList<CloudCoin> coins;
    public CloudCoin coin;

    public static ArrayList<RAIDA> networks = new ArrayList<>();
    public static RAIDA ActiveRAIDA;
    public static String Workspace;
    public static SimpleLogger logger;

    public static void ProcessCoins() {
        int[] networks = new int[IFileSystem.importCoins.size()];
        for (int i = 0; i < IFileSystem.importCoins.size(); i++) {
            CloudCoin cc = IFileSystem.importCoins.get(i);
            networks[i] = cc.nn;
        }

        // TODO: remove duplicates in array

        boolean ChangeANs = true;
        long ts;
        long before = System.currentTimeMillis();
        long after;

        for (int nn : networks) {
            ActiveRAIDA = null;

            for (RAIDA network : RAIDA.networks) {
                if (ActiveRAIDA == null && nn == network.NetworkNumber) {
                    ActiveRAIDA = network;
                }
            }

            int NetworkExists = 0;
            for (RAIDA network : RAIDA.networks) {
                if (nn == network.NetworkNumber) {
                    NetworkExists++;
                }
            }
            if (NetworkExists != 0) {
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

    public static CompletableFuture<Object> ProcessNetworkCoins(int NetworkNumber) {
        return ProcessNetworkCoins(NetworkNumber, true);
    }

    public static CompletableFuture<Object> ProcessNetworkCoins(int NetworkNumber, boolean ChangeANS) {
        FileSystem.LoadFileSystem();
        FileSystem.DetectPreProcessing();

        ArrayList<CloudCoin> oldPredetectCoins = FileSystem.LoadFolderCoins(FileSystem.PreDetectFolder);
        ArrayList<CloudCoin> predetectCoins = new ArrayList<>();
        for (int i = 0; i < oldPredetectCoins.size(); i++) {
            if (NetworkNumber == oldPredetectCoins.get(i).nn) {
                predetectCoins.add(oldPredetectCoins.get(i));
            }
        }

        IFileSystem.predetectCoins = predetectCoins;

        RAIDA raida;
        for (RAIDA network : RAIDA.networks) {
            if (raida == null && NetworkNumber == network.NetworkNumber) {
                raida = network;
            }
        }

        if (raida == null)
            return null;

        // Process Coins in Lots of 200. Can be changed from Config File
        int LotCount = predetectCoins.size() / Config.MultiDetectLoad;
        if (predetectCoins.size() % Config.MultiDetectLoad > 0)
            LotCount++;
        ProgressChangedEventArgs pge = new ProgressChangedEventArgs();

        int CoinCount = 0;
        int totalCoinCount = predetectCoins.size();
        for (int i = 0; i < LotCount; i++) {
            //Pick up 200 Coins and send them to RAIDA
            ArrayList<CloudCoin> coins = new ArrayList<>(predetectCoins.subList(i * Config.MultiDetectLoad, Math.min(predetectCoins.size(), 200)));
            try {
                raida.coins = coins;
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            var tasks = raida.GetMultiDetectTasks(coins, Config.milliSecondsToTimeOut,ChangeANS);
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

    public ArrayList<CompletableFuture<Object>> GetMultiDetectTasks(ArrayList<CloudCoin> coins, int milliSecondsToTimeOut) {
        return GetMultiDetectTasks(coins, milliSecondsToTimeOut, true);
    }

    public ArrayList<CompletableFuture<Object>> GetMultiDetectTasks(ArrayList<CloudCoin> coins, int milliSecondsToTimeOut, boolean changeANs) {
        this.coins = coins;

        int[] nns = new int[coins.size()];
        int[] sns = new int[coins.size()];

        String[][] ans = new String[Config.NodeCount][];
        String[][] pans = new String[Config.NodeCount][];

        int[] dens = new int[coins.size()]; // Denominations
        ArrayList<Runnable> detectTasks = new ArrayList<>(); // Stripe the coins

        for (int i = 0; i < coins.size(); i++) {
            if (changeANs)
                coins.get(i).GeneratePAN();
            else
                coins.get(i).SetAnsToPans();
            nns[i] = coins.get(i).nn;
            sns[i] = coins.get(i).getSn();
            dens[i] = coins.get(i).denomination;
        }

        multiRequest = new MultiDetectRequest();
        multiRequest.timeout = Config.milliSecondsToTimeOut;
        for (int nodeNumber = 0; nodeNumber < Config.NodeCount; nodeNumber++) {
            ans[nodeNumber] = new String[coins.size()];
            pans[nodeNumber] = new String[coins.size()];

            for (int i = 0; i < coins.size(); i++) {
                ans[nodeNumber][i] = coins.get(i).an.get(nodeNumber);
                pans[nodeNumber][i] = coins.get(i).pan[nodeNumber];
            }
            multiRequest.an[nodeNumber] = ans[nodeNumber];
            multiRequest.pan[nodeNumber] = pans[nodeNumber];
            multiRequest.nn = nns;
            multiRequest.sn = sns;
            multiRequest.d = dens;
        }


        for (int nodeNumber = 0; nodeNumber < Config.NodeCount; nodeNumber++) {
            detectTasks.add(nodes[nodeNumber].MultiDetect());
        }

        return detectTasks;
    }

    public Event ProgressChanged;
    public Event LoggerHandler;
    public Event CoinDetected;

    public int ReadyCount { get { return nodes.Where(x => x.RAIDANodeStatus == NodeStatus.Ready).Count(); } }
    public int NotReadyCount { get { return nodes.Where(x => x.RAIDANodeStatus == NodeStatus.NotReady).Count(); } }

    public void OnProgressChanged(ProgressChangedEventArgs e)
    {
        ProgressChanged.Invoke(this, e);
    }

    public void OnLogRecieved(ProgressChangedEventArgs e)
    {
        LoggerHandler.Invoke(this, e);
    }

    protected void OnCoinDetected(DetectEventArgs e)
    {
        CoinDetected.Invoke(this, e);
    }
}
