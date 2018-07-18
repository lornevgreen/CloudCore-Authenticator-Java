package com.cloudcore.authenticator.core;

import com.cloudcore.authenticator.utils.SimpleLogger;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RAIDA {

    public static RAIDA MainNetwork;
    public Node[] nodes = new Node[Config.NodeCount];
    public MultiDetectRequest multiRequest;
    public int NetworkNumber = 1;

    public static IFileSystem FileSystem;
    public IFileSystem FS;

    public ArrayList<CloudCoin> coins;

    public static ArrayList<RAIDA> networks = new ArrayList<>();
    public static RAIDA ActiveRAIDA;
    public static SimpleLogger logger;

    // Singleton Pattern implemented using private constructor
    // This allows only one instance of RAIDA per application

    private RAIDA() {
        FS = RAIDA.FileSystem;
        for (int i = 0; i < Config.NodeCount; i++) {
            nodes[i] = new Node(i + 1);
        }
    }

    private RAIDA(Network network) {
        nodes = new Node[network.raida.length];
        this.NetworkNumber = network.nn;
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new Node(i + 1, network.raida[i]);
        }
    }

    // This method was introduced breaking the previously used Singleton pattern.
    // This was done in order to support multiple networks concurrently.
    // We can now have multiple RAIDA objects each containing different networks
    // RAIDA details are read from Directory URL first.
    // In case of failure, it falls back to a file on the file system
    public static ArrayList<RAIDA> Instantiate() {
        String nodesJson = "";
        networks.clear();

        try {
            nodesJson = Utils.GetHtmlFromURL(Config.URL_DIRECTORY);
        } catch (Exception e) {
            System.out.println(": " + e.getLocalizedMessage());
            e.printStackTrace();
            if (!Files.exists(Paths.get("directory.json"))) {
                System.out.println("RAIDA instantiation failed. No Directory found on server or local path");
                System.exit(-1);
                return null;
            }
            try {
                nodesJson = new String(Files.readAllBytes(Paths.get(Paths.get("").toAbsolutePath().toString()
                        + File.separator + "directory.json")));
            } catch (IOException e1) {
                System.out.println("| " + e.getLocalizedMessage());
                e1.printStackTrace();
            }
        }

        try {
            Gson gson = Utils.createGson();
            RAIDADirectory dir = gson.fromJson(nodesJson, RAIDADirectory.class);

            for (Network network : dir.networks) {
                System.out.println("Available Networks: " + network.raida[0].urls[0].url + " , " + network.nn);
                networks.add(RAIDA.GetInstance(network));
            }
        } catch (Exception e) {
            System.out.println("RAIDA instantiation failed. No Directory found on server or local path");
            e.printStackTrace();
            System.exit(-1);
        }

        if (networks == null || networks.size() == 0) {
            System.out.println("RAIDA instantiation failed. No Directory found on server or local path");
            System.exit(-1);
            return null;
        }
        return networks;
    }

    // Return Main RAIDA Network populated with default Nodes Addresses(Network 1)
    public static RAIDA GetInstance() {
        if (MainNetwork != null)
            return MainNetwork;
        else {
            MainNetwork = new RAIDA();
            return MainNetwork;
        }
    }

    public static RAIDA GetInstance(Network network) {
        RAIDA raida = new RAIDA(network);
        raida.FS = FileSystem;
        return raida;
    }

    public static CompletableFuture<Object> ProcessNetworkCoins(int NetworkNumber) {
        return ProcessNetworkCoins(NetworkNumber, true);
    }

    public static CompletableFuture<Object> ProcessNetworkCoins(int NetworkNumber, boolean ChangeANS) {
        return CompletableFuture.supplyAsync(() -> {
            FileSystem.LoadFileSystem();
            FileSystem.DetectPreProcessing();

            ArrayList<CloudCoin> oldPredetectCoins = FileSystem.LoadFolderCoins(FileSystem.SuspectFolder);
            ArrayList<CloudCoin> predetectCoins = new ArrayList<>();
            for (int i = 0; i < oldPredetectCoins.size(); i++) {
                if (NetworkNumber == oldPredetectCoins.get(i).nn) {
                    predetectCoins.add(oldPredetectCoins.get(i));
                }
            }

            IFileSystem.predetectCoins = predetectCoins;

            RAIDA raida = null;
            for (RAIDA network : RAIDA.networks) {
                if (network != null && NetworkNumber == network.NetworkNumber) {
                    raida = network;
                    break;
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
                ArrayList<CloudCoin> coins = new ArrayList<>();
                try { // Pick up to 200 Coins and send them to RAIDA
                    coins = new ArrayList<>(predetectCoins.subList(i * Config.MultiDetectLoad, Math.min(predetectCoins.size(), 200)));
                    raida.coins = coins;
                } catch (Exception e) {
                    System.out.println(":" + e.getLocalizedMessage());
                    e.printStackTrace();
                }
                ArrayList<CompletableFuture<Node.MultiDetectResponse>> tasks = raida.GetMultiDetectTasks(raida.coins, ChangeANS);
                try {
                    try {
                        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).get();
                    } catch (Exception e) {
                        System.out.println("RAIDA#PNC:" + e.getLocalizedMessage());
                    }

                    for (int j = 0; j < coins.size(); j++) {
                        CloudCoin coin = coins.get(j);
                        StringBuilder pownString = new StringBuilder();
                        coin.pown = "";
                        int countp = 0;
                        int countf = 0;
                        for (int k = 0; k < Config.NodeCount; k++) {
                            coin.response[k] = raida.nodes[k].MultiResponse.responses[j];
                            pownString.append(coin.response[k].outcome.substring(0, 1));
                            if ("pass".equals(coin.response[k].outcome))
                                countp++;
                            else
                                countf++;
                        }
                        coin.pown = pownString.toString();
                        coin.setPassCount(countp);
                        coin.setFailCount(countf);
                        CoinCount++;

                        updateLog("No. " + CoinCount + ". Coin Detected. sn - " + coin.getSn() + ". Pass Count - " + coin.getPassCount() +
                                ". Fail Count  - " + coin.getFailCount() + ". Result - " + coin.DetectionResult + "." + coin.pown);
                        System.out.println("Coin Detected. sn - " + coin.getSn() + ". Pass Count - " + coin.getPassCount() +
                                ". Fail Count  - " + coin.getFailCount() + ". Result - " + coin.DetectionResult);
                        //coin.sortToFolder();
                        pge.MinorProgress = (CoinCount) * 100 / totalCoinCount;
                        System.out.println("Minor Progress- " + pge.MinorProgress);
                        raida.OnProgressChanged();
                    }
                    pge.MinorProgress = (CoinCount - 1) * 100 / totalCoinCount;
                    System.out.println("Minor Progress- " + pge.MinorProgress);
                    raida.OnProgressChanged();
                    FileSystem.WriteCoin(coins, FileSystem.DetectedFolder);
                    FileSystem.RemoveCoinsRealName(coins, FileSystem.SuspectFolder);

                    updateLog(pge.MinorProgress + " % of Coins on Network " + NetworkNumber + " processed.");
                } catch (Exception e) {
                    System.out.println("RAIDA#PNC: " + e.getLocalizedMessage());
                }/* catch (Exception e) {
                    System.out.println("RAIDA#PNC: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }*/
            }

            pge.MinorProgress = 100;
            System.out.println("Minor Progress- " + pge.MinorProgress);
            raida.OnProgressChanged();
            ArrayList<CloudCoin> detectedCoins = FileSystem.LoadFolderCoins(FileSystem.DetectedFolder);

            detectedCoins.forEach(CloudCoin::GradeSimple); // Apply Grading to all detected coins at once.

            ArrayList<CloudCoin> passedCoins = new ArrayList<>();
            ArrayList<CloudCoin> frackedCoins = new ArrayList<>();
            ArrayList<CloudCoin> failedCoins = new ArrayList<>();
            ArrayList<CloudCoin> lostCoins = new ArrayList<>();

            for (CloudCoin coin : detectedCoins) {
                if (coin.folder.equals(FileSystem.BankFolder)) passedCoins.add(coin);
                else if (coin.folder.equals(FileSystem.FrackedFolder)) frackedCoins.add(coin);
                else if (coin.folder.equals(FileSystem.CounterfeitFolder)) failedCoins.add(coin);
                else if (coin.folder.equals(FileSystem.LostFolder)) lostCoins.add(coin);
            }
            /*ArrayList<CloudCoin> passedCoins = new ArrayList<>(Arrays.asList((CloudCoin[])
                    Arrays.stream(detectedCoins.toArray(new CloudCoin[0]))
                            .filter(x -> x.folder.equals(FileSystem.BankFolder)).toArray()));*/

            System.out.println("Total Passed Coins - " + (passedCoins.size() + frackedCoins.size()));
            System.out.println("Total Failed Coins - " + failedCoins.size());
            updateLog("Coin Detection finished.");
            updateLog("Total Passed Coins - " + (passedCoins.size() + frackedCoins.size()) + "");
            updateLog("Total Failed Coins - " + failedCoins.size() + "");
            updateLog("Total Lost Coins - " + lostCoins.size() + "");

            // Move Coins to their respective folders after sort
            FileSystem.MoveCoins(passedCoins, FileSystem.DetectedFolder, FileSystem.BankFolder);
            FileSystem.MoveCoins(frackedCoins, FileSystem.DetectedFolder, FileSystem.FrackedFolder);

            //FileSystem.WriteCoin(failedCoins, FileSystem.CounterfeitFolder, true);
            FileSystem.MoveCoins(lostCoins, FileSystem.DetectedFolder, FileSystem.LostFolder);

            // Clean up Detected Folder
            FileSystem.RemoveCoins(failedCoins, FileSystem.DetectedFolder);
            FileSystem.RemoveCoins(lostCoins, FileSystem.DetectedFolder);

            FileSystem.MoveImportedFiles();

            pge.MinorProgress = 100;
            System.out.println("Minor Progress- " + pge.MinorProgress);

            return null;
        });
    }

    public ArrayList<CompletableFuture<Node.MultiDetectResponse>> GetMultiDetectTasks(ArrayList<CloudCoin> coins, boolean changeANs) {
        this.coins = coins;

        int[] nns = new int[coins.size()];
        int[] sns = new int[coins.size()];

        String[][] ans = new String[Config.NodeCount][];
        String[][] pans = new String[Config.NodeCount][];

        int[] dens = new int[coins.size()]; // Denominations
        ArrayList<CompletableFuture<Node.MultiDetectResponse>> detectTasks = new ArrayList<>(); // Stripe the coins

        for (int i = 0; i < coins.size(); i++) {
            if (changeANs)
                coins.get(i).GeneratePAN();
            else
                coins.get(i).SetAnsToPans();
            nns[i] = coins.get(i).nn;
            sns[i] = coins.get(i).getSn();
            dens[i] = coins.get(i).denomination;
            System.out.println(coins.get(i).toString());
        }

        try {
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
        } catch (Exception e) {
            System.out.println("/0" + e.getLocalizedMessage());
            e.printStackTrace();
        }

        try {
            for (int nodeNumber = 0; nodeNumber < Config.NodeCount; nodeNumber++) {
                detectTasks.add(nodes[nodeNumber].MultiDetect());
            }
        } catch (Exception e) {
            System.out.println("/1" + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return detectTasks;
    }

    public void OnProgressChanged() {
        //ProgressChanged.Invoke(this, e);
    }

    public static void updateLog(String message) {
        System.out.println(message);
        logger.Info(message);
    }
}
