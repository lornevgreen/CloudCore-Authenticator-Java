package com.cloudcore.authenticator.core;

import com.cloudcore.authenticator.coreclasses.FileSystem;
import com.cloudcore.authenticator.utils.CoinUtils;
import com.cloudcore.authenticator.utils.SimpleLogger;
import com.cloudcore.authenticator.utils.Utils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class RAIDA {

    public static RAIDA MainNetwork;
    public Node[] nodes = new Node[Config.nodeCount];
    public MultiDetectRequest multiRequest;
    public int NetworkNumber = 1;

    public ArrayList<CloudCoin> coins;

    public static ArrayList<RAIDA> networks = new ArrayList<>();
    public static RAIDA ActiveRAIDA;
    public static SimpleLogger logger;

    // Singleton Pattern implemented using private constructor
    // This allows only one instance of RAIDA per application

    private RAIDA() {
        for (int i = 0; i < Config.nodeCount; i++) {
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
            nodesJson = Utils.getHtmlFromURL(Config.URL_DIRECTORY);
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
        return new RAIDA(network);
    }

    public static CompletableFuture<Object> ProcessNetworkCoins(int NetworkNumber) {
        return ProcessNetworkCoins(NetworkNumber, true);
    }

    public static CompletableFuture<Object> ProcessNetworkCoins(int NetworkNumber, boolean ChangeANS) {
        return CompletableFuture.supplyAsync(() -> {
            FileSystem.loadFileSystem();
            FileSystem.detectPreProcessing();

            System.out.println("Getting coins...");
            ArrayList<CloudCoin> folderSuspectCoins = FileSystem.loadFolderCoins(FileSystem.SuspectFolder);
            ArrayList<CloudCoin> suspectCoins = new ArrayList<>();
            for (CloudCoin oldPredetectCoin : folderSuspectCoins) {
                if (NetworkNumber == oldPredetectCoin.getNn()) {
                    suspectCoins.add(oldPredetectCoin);
                }
            }

            FileSystem.predetectCoins = suspectCoins;

            System.out.println("Getting network...");
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
            int LotCount = suspectCoins.size() / Config.multiDetectLoad;
            if (suspectCoins.size() % Config.multiDetectLoad > 0)
                LotCount++;

            int coinCount = 0;
            int totalCoinCount = suspectCoins.size();
            int progress;
            for (int i = 0; i < LotCount; i++) {
                ArrayList<CloudCoin> coins = new ArrayList<>();
                try { // Pick up to 200 Coins and send them to RAIDA
                    coins = new ArrayList<>(suspectCoins.subList(i * Config.multiDetectLoad, Math.min(suspectCoins.size(), 200)));
                    raida.coins = coins;
                } catch (Exception e) {
                    System.out.println(":" + e.getLocalizedMessage());
                    e.printStackTrace();
                }
                ArrayList<CompletableFuture<Node.MultiDetectResponse>> tasks = raida.GetMultiDetectTasks(raida.coins, ChangeANS);
                try {
                    try {
                        System.out.println("Waiting for futures...");
                        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).get();
                    } catch (Exception e) {
                        System.out.println("RAIDA#PNC:" + e.getLocalizedMessage());
                    }

                    for (int j = 0; j < coins.size(); j++) {
                        CloudCoin coin = coins.get(j);
                        StringBuilder pownString = new StringBuilder();
                        coin.setPown("");
                        for (int k = 0; k < Config.nodeCount; k++) {
                            pownString.append(raida.nodes[k].MultiResponse.responses[j].outcome, 0, 1);
                        }
                        coin.setPown(pownString.toString());
                        coinCount++;

                        updateLog("No. " + coinCount + ". Coin Detected. sn - " + coin.getSn() + ". Pass Count - " + CoinUtils.getPassCount(coin) +
                                ". Fail Count  - " + CoinUtils.getFailCount(coin) + ". Result - " + CoinUtils.getDetectionResult(coin) + "." + coin.getPown());
                        System.out.println("Coin Detected. sn - " + coin.getSn() + ". Pass Count - " + CoinUtils.getPassCount(coin) +
                                ". Fail Count  - " + CoinUtils.getFailCount(coin) + ". Result - " + CoinUtils.getDetectionResult(coin));
                        //coin.sortToFolder();
                        progress = (coinCount) * 100 / totalCoinCount;
                        System.out.println("Minor Progress- " + progress);
                    }
                    progress = (coinCount - 1) * 100 / totalCoinCount;
                    System.out.println("Minor Progress- " + progress);
                    FileSystem.writeCoin(coins, FileSystem.DetectedFolder);
                    FileSystem.removeCoinsRealName(coins, FileSystem.SuspectFolder);

                    updateLog(progress + " % of Coins on Network " + NetworkNumber + " processed.");
                } catch (Exception e) {
                    System.out.println("RAIDA#PNC: " + e.getLocalizedMessage());
                }/* catch (Exception e) {
                    System.out.println("RAIDA#PNC: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }*/
            }

            return null;
        });
    }

    public ArrayList<CompletableFuture<Node.MultiDetectResponse>> GetMultiDetectTasks(ArrayList<CloudCoin> coins, boolean changeANs) {
        this.coins = coins;

        int[] nns = new int[coins.size()];
        int[] sns = new int[coins.size()];

        String[][] ans = new String[Config.nodeCount][];
        String[][] pans = new String[Config.nodeCount][];

        int[] dens = new int[coins.size()]; // Denominations
        ArrayList<CompletableFuture<Node.MultiDetectResponse>> detectTasks = new ArrayList<>(); // Stripe the coins

        for (int i = 0; i < coins.size(); i++) {
            CloudCoin coin = coins.get(i);
            if (changeANs)
                CoinUtils.generatePAN(coin);
            else
                CoinUtils.setAnsToPans(coin);
            nns[i] = coin.getNn();
            sns[i] = coin.getSn();
            dens[i] = CoinUtils.getDenomination(coin);
            System.out.println(coin.toString());
        }

        try {
        multiRequest = new MultiDetectRequest();
        multiRequest.timeout = Config.milliSecondsToTimeOut;
        for (int nodeNumber = 0; nodeNumber < Config.nodeCount; nodeNumber++) {
            ans[nodeNumber] = new String[coins.size()];
            pans[nodeNumber] = new String[coins.size()];

            for (int i = 0; i < coins.size(); i++) {
                ans[nodeNumber][i] = coins.get(i).getAn().get(nodeNumber);
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
            for (int nodeNumber = 0; nodeNumber < Config.nodeCount; nodeNumber++) {
                detectTasks.add(nodes[nodeNumber].MultiDetect());
            }
        } catch (Exception e) {
            System.out.println("/1" + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return detectTasks;
    }

    public static void updateLog(String message) {
        System.out.println(message);
        logger.Info(message);
    }
}
