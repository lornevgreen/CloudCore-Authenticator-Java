package com.cloudcore.authenticator.core;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.UUID;

import static com.cloudcore.authenticator.core.CoinUtils.Folder.*;
import static java.time.temporal.ChronoUnit.DAYS;

public class CoinUtils {
    //  instance variables
    public CloudCoin cc;
    public String[] pans = new String[25];// Proposed Authenticty Numbers
    public String edHex;// Months from zero date that the coin will expire.
    public int hp;// HitPoints (1-25, One point for each server not failed)
    public String fileName;
    public String json;
    public byte[] jpeg;
    public final int YEARSTILEXPIRE = 2;

    public enum Folder {Suspect, Counterfeit, Fracked, Bank, Trash}

    ;
    public Folder folder;
    public String[] gradeStatus = new String[3];// What passed, what failed, what was undetected


    //CONSTRUCTORS
    public CoinUtils(CloudCoin cc) {
        //  initialise instance variables
        this.cc = cc;
        for (int i = 0; i < 25; i++) {
            pans[i] = this.generatePan();
        } // end for each pan
        edHex = "FF";//Max allowed.
        hp = 25;//Max allowed
        fileName = this.getDenomination() + ".CloudCoin." + cc.nn + "." + cc.getSn() + ".";
        json = "";
        jpeg = null;
    }//end constructor


    //METHODS

    public CoinUtils() {

    }//end constructor

    public String getPastStatus(int raida_id) {
        String returnString = "";
        char[] pownArray = cc.pown.toCharArray();
        switch (pownArray[raida_id]) {
            case 'e':
                returnString = "error";
                break;
            case 'f':
                returnString = "fail";
                break;
            case 'p':
                returnString = "pass";
                break;
            case 'u':
                returnString = "undetected";
                break;
            case 'n':
                returnString = "noresponse";
                break;
        }//end switch
        return returnString;
    }//end getPastStatus

    public boolean setPastStatus(String status, int raida_id) {

        char[] pownArray = cc.pown.toCharArray();
        switch (status) {
            case "error":
                pownArray[raida_id] = 'e';
                break;
            case "fail":
                pownArray[raida_id] = 'f';
                break;
            case "pass":
                pownArray[raida_id] = 'p';
                break;
            case "undetected":
                pownArray[raida_id] = 'u';
                break;
            case "noresponse":
                pownArray[raida_id] = 'n';
                break;
        }//end switch
        cc.pown = new String(pownArray);
        return true;
    }//end set past status

    public String getFolder() {
        String returnString = "";
        switch (folder) {
            case Bank:
                returnString = "Bank";
                break;
            case Counterfeit:
                returnString = "Counterfeit";
                break;
            case Fracked:
                returnString = "Fracked";
                break;
            case Suspect:
                returnString = "Suspect";
                break;
            case Trash:
                returnString = "Trash";
                break;
        }//end switch
        return returnString;
    }//end getPastStatus

    public boolean setFolder(String folderName) {
        boolean setGood = false;
        switch (folderName.toLowerCase()) {
            case "bank":
                folder = Bank;
                break;
            case "counterfeit":
                folder = Counterfeit;
                break;
            case "fracked":
                folder = Fracked;
                break;
            case "suspect":
                folder = Suspect;
                break;
            case "trash":
                folder = Trash;
                break;
        }//end switch
        return setGood;
    }//end set past status

    public int getDenomination() {
        int nom = 0;
        if ((cc.getSn() < 1)) {
            nom = 0;
        } else if ((cc.getSn() < 2097153)) {
            nom = 1;
        } else if ((cc.getSn() < 4194305)) {
            nom = 5;
        } else if ((cc.getSn() < 6291457)) {
            nom = 25;
        } else if ((cc.getSn() < 14680065)) {
            nom = 100;
        } else if ((cc.getSn() < 16777217)) {
            nom = 250;
        } else {
            nom = '0';
        }

        return nom;
    }//end get denomination

    public void calculateHP() {
        hp = 25;
        char[] pownArray = cc.pown.toCharArray();
        for (int i = 0; (i < 25); i++) {
            if (pownArray[i] == 'f') {
                this.hp--;
            }
        }
    }//end calculate hp

    /*
            public String gradeCoin()
            {
                int passed = 0;
                int failed = 0;
                int other = 0;
                String passedDesc = "";
                String failedDesc = "";
                String otherDesc = "";
                char[] pownArray = cc.pown.toCharArray();

                for (int i = 0; (i < 25); i++)
                {
                    if ( pownArray[i] == 'p')
                    {
                        passed++;
                    }
                    else if ( pownArray[i] == 'f')
                    {
                        failed++;
                    }
                    else
                    {
                        other++;
                    }
                }// end if pass, fail or unknown

                // Calculate passed
                if (passed == 25)
                {
                    passedDesc = "100% Passed!";
                }
                else if (passed > 17)
                {
                    passedDesc = "Super Majority";
                }
                else if (passed > 13)
                {
                    passedDesc = "Majority";
                }
                else if (passed == 0)
                {
                    passedDesc = "None";
                }
                else if (passed < 5)
                {
                    passedDesc = "Super Minority";
                }
                else
                {
                    passedDesc = "Minority";
                }

                // Calculate failed
                if (failed == 25)
                {
                    failedDesc = "100% Failed!";
                }
                else if (failed > 17)
                {
                    failedDesc = "Super Majority";
                }
                else if (failed > 13)
                {
                    failedDesc = "Majority";
                }
                else if (failed == 0)
                {
                    failedDesc = "None";
                }
                else if (failed < 5)
                {
                    failedDesc = "Super Minority";
                }
                else
                {
                    failedDesc = "Minority";
                }

                // Calcualte Other RAIDA Servers did not help.
                switch (other)
                {
                    case 0:
                        otherDesc = "RAIDA 100% good";
                        break;
                    case 1:
                    case 2:
                        otherDesc = "Four or less RAIDA errors";
                        break;
                    case 3:
                    case 4:
                        otherDesc = "Four or less RAIDA errors";
                        break;
                    case 5:
                    case 6:
                        otherDesc = "Six or less RAIDA errors";
                        break;
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                        otherDesc = "Between 7 and 12 RAIDA errors";
                        break;
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                        otherDesc = "RAIDA total failure";
                        break;
                    default:
                        otherDesc = "FAILED TO EVALUATE RAIDA HEALTH";
                        break;
                }
                // end RAIDA other errors and unknowns
                return "\n " + passedDesc + " said Passed. " + "\n " + failedDesc + " said Failed. \n RAIDA Status: " + otherDesc;
            }// end grade coin
    */

    /**
     * TODO: this code is identical to CloudCoin.CalcExpirationDate().
     */
    public void calcExpirationDate() {
        LocalDate expirationnnnDate = LocalDate.now().plusYears(Config.YEARSTILEXPIRE);
        cc.ed = (expirationnnnDate.getMonth() + "-" + expirationnnnDate.getYear());

        LocalDate zeroDateee = LocalDate.of(2016, 8, 13);
        int monthsAfterZero = (int) (DAYS.between(expirationnnnDate, zeroDateee) / (365.25 / 12));
        this.edHex = String.format("0x%08X", monthsAfterZero);
    }// end calc exp date

    /**
     * TODO: this code is identical to CloudCoin.generatePan().
     */
    public String generatePan() {
        SecureRandom random = new SecureRandom();
        byte[] cryptoRandomBuffer = random.generateSeed(16);

        UUID pan = UUID.nameUUIDFromBytes(cryptoRandomBuffer);
        return String.format("%32s", pan).replace(' ', '0');
    }

    public String[] grade() {
        int passed = 0;
        int failed = 0;
        int other = 0;
        String passedDesc = "";
        String failedDesc = "";
        String otherDesc = "";
        char[] pownArray = cc.pown.toCharArray();

        for (int i = 0; (i < 25); i++) {
            if (pownArray[i] == 'p') {
                passed++;
            } else if (pownArray[i] == 'f') {
                failed++;
            } else {
                other++;
            }// end if pass, fail or unknown
        }

        // for each status
        // Calculate passed
        if (passed == 25) {
            passedDesc = "100% Passed!";
        } else if (passed > 17) {
            passedDesc = "Super Majority";
        } else if (passed > 13) {
            passedDesc = "Majority";
        } else if (passed == 0) {
            passedDesc = "None";
        } else if (passed < 5) {
            passedDesc = "Super Minority";
        } else {
            passedDesc = "Minority";
        }

        // Calculate failed
        if (failed == 25) {
            failedDesc = "100% Failed!";
        } else if (failed > 17) {
            failedDesc = "Super Majority";
        } else if (failed > 13) {
            failedDesc = "Majority";
        } else if (failed == 0) {
            failedDesc = "None";
        } else if (failed < 5) {
            failedDesc = "Super Minority";
        } else {
            failedDesc = "Minority";
        }

        // Calcualte Other RAIDA Servers did not help.
        switch (other) {
            case 0:
                otherDesc = "100% of RAIDA responded";
                break;
            case 1:
            case 2:
                otherDesc = "Two or less RAIDA errors";
                break;
            case 3:
            case 4:
                otherDesc = "Four or less RAIDA errors";
                break;
            case 5:
            case 6:
                otherDesc = "Six or less RAIDA errors";
                break;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                otherDesc = "Between 7 and 12 RAIDA errors";
                break;
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
                otherDesc = "RAIDA total failure";
                break;
            default:
                otherDesc = "FAILED TO EVALUATE RAIDA HEALTH";
                break;
        }
        // end RAIDA other errors and unknowns
        // Coin will go to bank, counterfeit or fracked
        if (other > 12) {
            // not enough RAIDA to have a quorum
            folder = Suspect;
        } else if (failed > passed) {
            // failed out numbers passed with a quorum: Counterfeit
            folder = Counterfeit;
        } else if (failed > 0) {
            // The quorum majority said the coin passed but some disagreed: fracked.
            folder = Fracked;
        } else {
            // No fails, all passes: bank
            folder = Bank;
        }

        gradeStatus[0] = passedDesc;
        gradeStatus[1] = failedDesc;
        gradeStatus[2] = otherDesc;
        return this.gradeStatus;
    }// end gradeStatus

    public void setAnsToPans() {
        for (int i = 0; (i < 25); i++) {
            this.pans[i] = cc.an[i];
        }// end for 25 ans
    }// end setAnsToPans

    public void setAnsToPansIfPassed(boolean partial =false) {
        // now set all ans that passed to the new pans
        char[] pownArray = cc.pown.toCharArray();

        for (int i = 0; (i < 25); i++) {
            if (pownArray[i] == 'p')//1 means pass
            {
                ccan.set(i, pans[i]);
            } else if (pownArray[i] == 'u' && !RAIDA.GetInstance().nodes[i].FailsEcho && partial == false)//Timed out but there server echoed. So it probably changed the PAN just too slow of a response
            {
                ccan.set(i, pans[i]);
            } else {
                // Just keep the ans and do not change. Hopefully they are not fracked.
            }
        }// for each guid in coin
    }// end set ans to pans if passed

    public char[] consoleReport() {
        // Used only for console apps
        //  System.out.println("Finished detecting coin index " + j);
        // PRINT OUT ALL COIN'S RAIDA STATUS AND SET AN TO NEW PAN
        char[] pownArray = cc.pown.toCharArray();
        String report = "   Authenticity Report SN #" + StringFormat("{0,8}", cc.getSn()) + ", Denomination: " + StringFormat("{0,3}", this.getDenomination()) + "  ";

        return pownArray;

    }//Console Report
}
