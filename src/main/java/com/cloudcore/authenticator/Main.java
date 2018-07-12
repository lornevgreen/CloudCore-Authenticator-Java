package com.cloudcore.authenticator;

import com.cloudcore.authenticator.core.Config;
import com.cloudcore.authenticator.core.Frack_Fixer;
import com.cloudcore.authenticator.core.RAIDA;
import com.cloudcore.authenticator.coreclasses.FileSystem;
import com.cloudcore.authenticator.coreclasses.TrustedTradeSocket;
import com.cloudcore.authenticator.utils.SimpleLogger;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cloudcore.authenticator.core.RAIDA.updateLog;

public class Main {

    public static String rootFolder = Paths.get("").toAbsolutePath().toString();

    static FileSystem FS = new FileSystem(rootFolder);
    public static RAIDA raida;
    public static Frack_Fixer fixer;
    public static SimpleLogger logger = new SimpleLogger(FS.LogsFolder + "logs" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.fff")).toLowerCase() + ".log", true);
    static TrustedTradeSocket tts;

    public static int NetworkNumber = 1;

    public static void main(String[] args) {
        setup();

        updateLog("Loading Network Directory");
        SetupRAIDA();
        FS.LoadFileSystem();
        RAIDA.logger = logger;
        fixer = new Frack_Fixer(FS, Config.milliSecondsToTimeOut);

        RAIDA.ProcessNetworkCoins(NetworkNumber);
    }

    private static void setup() {
        FS.CreateDirectories();
        RAIDA raida = RAIDA.GetInstance();
        FS.LoadFileSystem();

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
            System.out.println(e.getMessage());
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
            else
            {
                updateLog("Network Number set to " + NetworkNumber);
            }
        }
        //networks[0]
    }
}
