package com.cloudcore.authenticator.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.security.SecureRandom;
import java.util.*;

public class CloudCoin {


    /* JSON Fields */

    @Expose
    @SerializedName("nn")
    private int nn;
    @Expose
    @SerializedName("sn")
    private int sn;
    @Expose
    @SerializedName("an")
    private ArrayList<String> an;
    @Expose
    @SerializedName("ed")
    private String ed;
    @Expose
    @SerializedName("pown")
    private String pown;
    @Expose
    @SerializedName("aoid")
    private ArrayList<String> aoid;

    public transient String[] pan = new String[Config.nodeCount];

    public transient String folder;

    public transient String currentFilename;


    /* Fields */

    public transient Response[] response = new Response[Config.nodeCount];


    /* Methods */

    @Override
    public String toString() {
        return "cloudcoin: (nn:" + getNn() + ", sn:" + getSn() + ", ed:" + getEd() + ", aoid:" + getAoid().toString() + ", an:" + getAn().toString() + ",\n pan:" + Arrays.toString(pan);
    }


    /* Getters and Setters */

    public int getNn() { return nn; }
    public int getSn() { return sn; }
    public ArrayList<String> getAn() { return an; }
    public String getEd() { return ed; }
    public String getPown() { return pown; }
    public ArrayList<String> getAoid() { return aoid; }

    public void setAn(ArrayList<String> an) { this.an = an; }
    public void setPown(String pown) { this.pown = pown; }
    public void setFullFilePath(String fullFilePath) {
        this.folder = fullFilePath.substring(0, 1 + fullFilePath.lastIndexOf(File.separatorChar));
        this.currentFilename = fullFilePath.substring(1 + fullFilePath.lastIndexOf(File.separatorChar, fullFilePath.length()));
    }
}
