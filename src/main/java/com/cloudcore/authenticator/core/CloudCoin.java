package com.cloudcore.authenticator.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

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

    //public Response[] response = new Response[Config.NodeCount];

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

    ;


    //Constructors

    public CloudCoin() {
        an = new ArrayList<>();
    }

    /**
     * CloudCoin Constructor for importing new coins from a JSON-encoded file.
     *
     * @param nn  Network Number
     * @param sn  Serial Number
     * @param ans Authenticity Numbers
     */
    public CloudCoin(int nn, int sn, String[] ans) {
        this.nn = nn;
        this.sn = sn;
        this.an = new ArrayList<>(Arrays.asList(ans));
    }

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

    public static CloudCoin FromCSV(String csvLine) {
        try {
            CloudCoin coin = new CloudCoin();
            String[] values = csvLine.split(",");
            System.out.println(values[0]);
            coin.sn = Integer.parseInt(values[0]);
            coin.nn = Integer.parseInt(values[1]);
            coin.denomination = Integer.parseInt(values[1]);
            coin.an = new ArrayList<>();
            for (int i = 0; i < Config.NodeCount; i++) {
                coin.an.add(values[i + 3]);
            }

            return coin;

        } catch (Exception e) {

        }
        return null;
    }

    public String FileName() {
        return this.getDenomination() + ".CloudCoin." + nn + "." + sn + ".";
    }

    public int charCount(String pown, char character) {
        return pown.length() - pown.replace(Character.toString(character), "").length();
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

    public boolean isFixable() {
        //The coin is considered fixable if it has any of the patersns that would allow the new owner to fix fracked.
        //There are four of these patterns: One for each corner.
        String origPown = pown;
        pown = pown.replace('d', 'e').replace('n', 'e').replace('u', 'e');
        boolean canFix = false;
        // System.out.println(cc.getSn() + " char count p =" + charCount(cc.pown, 'p'));
        if (charCount(pown, 'p') > 5) {
            String doublePown = pown + pown;//double it so we see patters that happen on the ends.

            boolean UP_LEFT = doublePown.matches("(?i)pp[a-z][a-z][a-z]pf");
            boolean UP_RIGHT = doublePown.matches("(?i)pp[a-z][a-z][a-z]fp");
            boolean DOWN_LEFT = doublePown.matches("(?i)pf[a-z][a-z][a-z]pp");
            boolean DOWN_RIGHT = doublePown.matches("(?i)fp[a-z][a-z][a-z]pp");

            boolean UP_LEFT_n = doublePown.matches("(?i)pp[a-z][a-z][a-z]pn");
            boolean UP_RIGHT_n = doublePown.matches("(?i)pp[a-z][a-z][a-z]np");
            boolean DOWN_LEFT_n = doublePown.matches("(?i)pn[a-z][a-z][a-z]pp");
            boolean DOWN_RIGHT_n = doublePown.matches("(?i)np[a-z][a-z][a-z]pp");

            boolean UP_LEFT_e = doublePown.matches("(?i)pp[a-z][a-z][a-z]pe");
            boolean UP_RIGHT_e = doublePown.matches("(?i)pp[a-z][a-z][a-z]ep");
            boolean DOWN_LEFT_e = doublePown.matches("(?i)pe[a-z][a-z][a-z]pp");
            boolean DOWN_RIGHT_e = doublePown.matches("(?i)ep[a-z][a-z][a-z]pp");

            boolean UP_LEFT_u = doublePown.matches("(?i)pp[a-z][a-z][a-z]pu");
            boolean UP_RIGHT_u = doublePown.matches("(?i)pp[a-z][a-z][a-z]up");
            boolean DOWN_LEFT_u = doublePown.matches("(?i)pu[a-z][a-z][a-z]pp");
            boolean DOWN_RIGHT_u = doublePown.matches("(?i)up[a-z][a-z][a-z]pp");

            if (UP_LEFT || UP_RIGHT || DOWN_LEFT || DOWN_RIGHT || UP_LEFT_n || UP_RIGHT_n || DOWN_LEFT_n || DOWN_RIGHT_n
                    || UP_LEFT_e || UP_RIGHT_e || DOWN_LEFT_e || DOWN_RIGHT_e || UP_LEFT_u || UP_RIGHT_u || DOWN_LEFT_u || DOWN_RIGHT_u) {
                canFix = true;
                //System.out.println("isFixable");
            } else {
                canFix = false;
                //System.out.println("Not isFixable");
            }
        } else {
            canFix = false;
//                System.out.println("Not isFixable");
        }
        pown = origPown;
        return canFix;
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

    /**
     * Determines the coin's folder placement based on a simple grading schematic.
     */
    public void GradeSimple() {
        if (isPassingSimple()) {
            if (isFrackedSimple())
                folder = RAIDA.ActiveRAIDA.FS.FrackedFolder;
            else
                folder = RAIDA.ActiveRAIDA.FS.BankFolder;
        }
        else {
            if (isHealthySimple())
                folder = RAIDA.ActiveRAIDA.FS.CounterfeitFolder;
            else
                folder = RAIDA.ActiveRAIDA.FS.LostFolder;
        }
    }

    /**
     * Checks to see if the pown result is a passing grade.
     *
     * @return true if the pown result contains more than 20 passing grades.
     */
    public boolean isPassingSimple() {
        return (charCount(pown, 'p') >= 20);
    }

    /**
     * Checks to see if the pown result is fracked.
     *
     * @return true if the pown result contains more than 5 fracked grades.
     */
    public boolean isFrackedSimple() {
        return (charCount(pown, 'f') >= 5);
    }

    /**
     * Checks to see if the pown result is in good health. Unhealthy grades are errors and no-responses.
     *
     * @return true if the pown result contains more than 20 passing or failing grades.
     */
    public boolean isHealthySimple() {
        return (charCount(pown, 'p') + charCount(pown, 'f') >= 20);
    }


    public void CalcExpirationDate() {
        LocalDate expirationnnnDate = LocalDate.now().plusYears(Config.YEARSTILEXPIRE);
        ed = (expirationnnnDate.getMonth() + "-" + expirationnnnDate.getYear());

        LocalDate zeroDateee = LocalDate.of(2016, 8, 13);
        int monthsAfterZero = (int) (DAYS.between(expirationnnnDate, zeroDateee) / (365.25 / 12));
        //this.edHex = String.format("0x%08X", monthsAfterZero);
    }


    public int getSn() {
        return sn;
    }
    public void setSn(int sn) {
        this.sn = sn;
        denomination = getDenomination();
    }


    public enum DetectionStatus {
    }

}
