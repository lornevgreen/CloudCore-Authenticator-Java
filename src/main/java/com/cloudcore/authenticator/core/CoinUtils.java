package com.cloudcore.authenticator.core;

import java.security.SecureRandom;
import java.util.UUID;

public class CoinUtils {
    //  instance variables
    public CloudCoin cc;
    public String[] pans = new String[25];// Proposed Authenticty Numbers
    public int hp;// HitPoints (1-25, One point for each server not failed)

    public enum Folder {Suspect, Counterfeit, Fracked, Bank, Trash}

    ;
    public Folder folder;


    //CONSTRUCTORS
    public CoinUtils(CloudCoin cc) {
        //  initialise instance variables
        this.cc = cc;
        for (int i = 0; i < 25; i++) {
            pans[i] = this.generatePan();
        } // end for each pan
        hp = 25;//Max allowed
    }//end constructor


    //METHODS

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
     * TODO: this code is identical to CloudCoin.generatePan().
     */
    public String generatePan() {
        SecureRandom random = new SecureRandom();
        byte[] cryptoRandomBuffer = random.generateSeed(16);

        UUID pan = UUID.nameUUIDFromBytes(cryptoRandomBuffer);
        return String.format("%32s", pan).replace(' ', '0');
    }

}
