package com.cloudcore.authenticator;

import com.cloudcore.authenticator.core.*;
import com.cloudcore.authenticator.coreclasses.FileSystem;
import com.cloudcore.authenticator.utils.SimpleLogger;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cloudcore.authenticator.core.RAIDA.updateLog;

public class Main {

    public static String rootFolder = Paths.get("C:/CloudCoins-Authenticate").toAbsolutePath().toString();

    static FileSystem FS;
    public static SimpleLogger logger;

    public static int NetworkNumber = 1;

    public static void main(String[] args) {
        try {
            setup();

            RAIDA.logger = logger;
            updateLog("Loading Network Directory");
            SetupRAIDA();
            FS.LoadFileSystem();

            System.out.println("Processing Network Coins...");
            RAIDA.ProcessNetworkCoins(NetworkNumber);
        } catch (Exception e) {
            System.out.println("Uncaught exception - " + e.getLocalizedMessage());
            //e.printStackTrace();
        }

    }

    private static void setup() {
        FS = new FileSystem(rootFolder);
        FS.CreateDirectories();
        RAIDA raida = RAIDA.GetInstance();
        FS.LoadFileSystem();

        logger = new SimpleLogger(FS.LogsFolder + "logs" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")).toLowerCase() + ".log", true);

        //Connect to Trusted Trade Socket
        //tts = new TrustedTradeSocket("wss://escrow.cloudcoin.digital/ws/", 10, OnWord, OnStatusChange, OnReceive, OnProgress);
        //tts.Connect().Wait();
    }
    public static void SetupRAIDA()
    {
        RAIDA.FileSystem = new FileSystem(rootFolder);
        try
        {
            RAIDA.Instantiate();
        }
        catch(Exception e)
        {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(1);
        }
        if (RAIDA.networks.size() == 0)
        {
            updateLog("No Valid Network found.Quitting!!");
            System.exit(1);
        }
        else
        {
            updateLog(RAIDA.networks.size() + " Networks found.");
            RAIDA raida = RAIDA.networks.get(0);
            for (RAIDA r : RAIDA.networks)
                if (NetworkNumber == r.NetworkNumber) {
                    raida = r;
                    break;
                }

            raida.FS = FS;
            RAIDA.ActiveRAIDA = raida;
            if (raida == null)
            {
                updateLog("Selected Network Number not found. Quitting.");
                System.exit(0);
            }
        }
        //networks[0]
    }
}
