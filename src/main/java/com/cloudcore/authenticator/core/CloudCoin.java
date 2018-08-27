package com.cloudcore.authenticator.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.security.SecureRandom;
import java.util.*;

public class CloudCoin {


    @Expose
    @SerializedName("nn")
    public int nn;
    @Expose
    @SerializedName("sn")
    private int sn;
    @Expose
    @SerializedName("an")
    public ArrayList<String> an;
    @Expose
    @SerializedName("ed")
    public String ed;
    @Expose
    @SerializedName("pown")
    public String pown;
    @Expose
    @SerializedName("aoid")
    public ArrayList<String> aoid;

    public transient String[] pan = new String[Config.NodeCount];

    public transient String folder;

    public transient String currentFilename;

    //Fields

    public transient Response[] response = new Response[Config.NodeCount];

    public transient int denomination;
    public transient String DetectionResult;


    private transient int passCount = 0;
    private transient int failCount = 0;

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int passCount) {
        this.passCount = passCount;
        if (passCount >= Config.PassCount) {
            DetectionResult = "Pass";
            an = new ArrayList<>(Arrays.asList(pan));
        } else
            DetectionResult = "Fail";
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
        DetectionResult = (passCount >= Config.PassCount) ? "Pass" : "Fail";
    }


    //Constructors

    public CloudCoin(String currentFilename, int nn, int sn, ArrayList<String> an, String ed, String pown, ArrayList<String> aoid) {
        this.currentFilename = currentFilename;
        this.nn = nn;
        this.sn = sn;
        this.an = an;
        this.ed = ed;
        this.pown = pown;
        this.aoid = aoid;

        denomination = getDenomination();
    }

    @Override
    public String toString() {
        return "cloudcoin: (nn:" + nn + ", sn:" + sn + ", ed:" + ed + ", aoid:" + aoid.toString() + ", an:" + an.toString() + ",\n pan:" + Arrays.toString(pan);
    }

    public String FileName() {
        return this.getDenomination() + ".CloudCoin." + nn + "." + sn + ".";
    }

    public int getDenomination() {
        int nom;
        if ((sn < 1))
            nom = 0;
        else if ((sn < 2097153))
            nom = 1;
        else if ((sn < 4194305))
            nom = 5;
        else if ((sn < 6291457))
            nom = 25;
        else if ((sn < 14680065))
            nom = 100;
        else if ((sn < 16777217))
            nom = 250;
        else
            nom = 0;

        return nom;
    }

    public void GeneratePAN() {
        pan = new String[Config.NodeCount];
        for (int i = 0; i < Config.NodeCount; i++) {
            pan[i] = this.generatePan();
        }
    }


    public void SetAnsToPans() {
        for (int i = 0; (i < Config.NodeCount); i++) {
            this.an.set(i, an.get(i));
        }
    }


    public String generatePan() {
        SecureRandom random = new SecureRandom();
        byte[] cryptoRandomBuffer = random.generateSeed(16);

        UUID pan = UUID.nameUUIDFromBytes(cryptoRandomBuffer);
        return pan.toString().replace("-", "");
    }


    public int getSn() {
        return sn;
    }

    public void setFullFilePath(String fullFilePath) {
        this.folder = fullFilePath.substring(0, 1 + fullFilePath.lastIndexOf(File.separatorChar));
        this.currentFilename = fullFilePath.substring(1 + fullFilePath.lastIndexOf(File.separatorChar, fullFilePath.length()));
    }
}
