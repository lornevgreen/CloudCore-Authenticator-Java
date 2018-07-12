package com.cloudcore.authenticator.core;

import com.cloudcore.authenticator.coreclasses.FileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Frack_Fixer {
    /* INSTANCE VARIABLES */
    private IFileSystem fileUtils;
    private int totalValueToBank;
    private int totalValueToFractured;
    private int totalValueToCounterfeit;
    private RAIDA raida;
    public boolean continueExecution = true;
    public boolean IsFixing = false;

    /* CONSTRUCTORS */
    public Frack_Fixer(IFileSystem fileUtils, int timeout) {

        this.fileUtils = fileUtils;
        raida = RAIDA.GetInstance();
        totalValueToBank = 0;
        totalValueToCounterfeit = 0;
        totalValueToFractured = 0;
    }//constructor

    public String fixOneGuidCorner(int raida_ID, CloudCoin cc, int corner, int[] trustedTriad) {
        //RAIDA raida = RAIDA.GetInstance();
        CoinUtils cu = new CoinUtils(cc);
        ProgressChangedEventArgs pge = new ProgressChangedEventArgs();

        /*1. WILL THE BROKEN RAIDA FIX? check to see if it has problems echo, detect, or fix. */
        if (raida.nodes[raida_ID].FailsFix || raida.nodes[raida_ID].FailsEcho || raida.nodes[raida_ID].FailsEcho) {
            System.out.println("RAIDA Fails Echo or Fix. Try again when RAIDA online.");
            pge.MajorProgressMessage = ("RAIDA Fails Echo or Fix. Try again when RAIDA online.");
            raida.OnLogRecieved(pge);
            return "RAIDA Fails Echo or Fix. Try again when RAIDA online.";
        } else {
            /*2. ARE ALL TRUSTED RAIDA IN THE CORNER READY TO HELP?*/

            if (!raida.nodes[trustedTriad[0]].FailsEcho || !raida.nodes[trustedTriad[0]].FailsDetect || !raida.nodes[trustedTriad[1]].FailsEcho || !!raida.nodes[trustedTriad[1]].FailsDetect || !raida.nodes[trustedTriad[2]].FailsEcho || !raida.nodes[trustedTriad[2]].FailsDetect) {
                /*3. GET TICKETS AND UPDATE RAIDA STATUS TICKETS*/
                String[] ans = {cc.an.get(trustedTriad[0]), cc.an.get(trustedTriad[1]), cc.an.get(trustedTriad[2])};
                raida.GetTickets(trustedTriad, ans, cc.nn, cc.getSn(), cu.getDenomination(), 3000);

                /*4. ARE ALL TICKETS GOOD?*/
                if (raida.nodes[trustedTriad[0]].HasTicket && raida.nodes[trustedTriad[1]].HasTicket && raida.nodes[trustedTriad[2]].HasTicket) {
                    /*5.T YES, so REQUEST FIX*/
                    //DetectionAgent da = new DetectionAgent(raida_ID, 5000);
                    if (!continueExecution) {
                        System.out.println("Aborting Fix ");
                        return "Aborting for new operation";
                    }
                    Response fixResponse = RAIDA.GetInstance().nodes[raida_ID].Fix(trustedTriad, raida.nodes[trustedTriad[0]].Ticket, raida.nodes[trustedTriad[1]].Ticket, raida.nodes[trustedTriad[2]].Ticket, cc.an.get(raida_ID));
                    /*6. DID THE FIX WORK?*/
                    if (fixResponse.success) {
                        //Console.ForegroundColor = ConsoleColor.Green;
                        System.out.println("");
                        System.out.println("RAIDA" + raida_ID + " unfracked successfully.");
                        pge.MajorProgressMessage = "RAIDA" + raida_ID + " unfracked successfully.";
                        raida.OnLogRecieved(pge);
                        //CoreLogger.Log("RAIDA" + raida_ID + " unfracked successfully.");
                        System.out.println("");
                        //Console.ForegroundColor = ConsoleColor.White;
                        return "RAIDA" + raida_ID + " unfracked successfully.";

                    } else {
                        //Console.ForegroundColor = ConsoleColor.Red;
                        System.out.println("");
                        System.out.println("RAIDA failed to accept tickets on corner " + corner);
                        pge.MajorProgressMessage = "RAIDA failed to accept tickets on corner " + corner;
                        raida.OnLogRecieved(pge);
                        //CoreLogger.Log("RAIDA failed to accept tickets on corner " + corner);
                        System.out.println("");
                        //Console.ForegroundColor = ConsoleColor.White;
                        return "RAIDA failed to accept tickets on corner " + corner;
                    }//end if fix respons was success or fail
                } else {
                    //Console.ForegroundColor = ConsoleColor.Red;
                    System.out.println("");
                    System.out.println("Trusted servers failed to provide tickets for corner " + corner);
                    pge.MajorProgressMessage = "Trusted servers failed to provide tickets for corner " + corner;
                    raida.OnLogRecieved(pge);
                    //CoreLogger.Log("Trusted servers failed to provide tickets for corner " + corner);
                    System.out.println("");
                    //Console.ForegroundColor = ConsoleColor.White;

                    return "Trusted servers failed to provide tickets for corner " + corner;//no three good tickets
                }//end if all good
            }//end if trused triad will echo and detect (Detect is used to get ticket)

            //Console.ForegroundColor = ConsoleColor.Red;
            System.out.println("");
            System.out.println("One or more of the trusted triad will not echo and detect.So not trying.");
            pge.MajorProgressMessage = "One or more of the trusted triad will not echo and detect.So not trying.";
            raida.OnLogRecieved(pge);

            //CoreLogger.Log("One or more of the trusted triad will not echo and detect.So not trying.");
            System.out.println("");
            //Console.ForegroundColor = ConsoleColor.White;
            return "One or more of the trusted triad will not echo and detect. So not trying.";
        }//end if RAIDA fails to fix.

    }//end fix one


    /* PUBLIC METHODS */
    public int[] FixAll() {
        IsFixing = true;
        continueExecution = true;
        int[] results = new int[3];
        File[] frackedFiles = FileSystem.GetFilesArray(fileUtils.FrackedFolder, Config.allowedExtensions);

        CloudCoin frackedCC;

        ProgressChangedEventArgs pge = new ProgressChangedEventArgs();
        pge.MajorProgressMessage = "Starting Frack Fixing";
        raida.OnLogRecieved(pge);

        //CoinUtils cu = new CoinUtils(frackedCC);
        if (frackedFiles.length < 0) {
            //Console.ForegroundColor = ConsoleColor.Green;
            System.out.println("You have no fracked coins.");
            //CoreLogger.Log("You have no fracked coins.");
            //Console.ForegroundColor = ConsoleColor.White;
        }//no coins to unfrack


        for (int i = 0; i < frackedFiles.length; i++) {
            if (!continueExecution) {
                System.out.println("Aborting Fix 1");
                break;
            }
            System.out.println("Unfracking coin " + (i + 1) + " of " + frackedFiles.length);
            //ProgressChangedEventArgs pge = new ProgressChangedEventArgs();
            pge.MajorProgressMessage = "Unfracking coin " + (i + 1) + " of " + frackedFiles.length;
            raida.OnLogRecieved(pge);
            //CoreLogger.Log("UnFracking coin " + (i + 1) + " of " + frackedFileNames.length);
            try {
                frackedCC = fileUtils.LoadCoin(this.fileUtils.FrackedFolder + frackedFiles[i]);
                if (frackedCC == null)
                    throw new IOException();
                CoinUtils cu = new CoinUtils(frackedCC);
                String value = frackedCC.pown;
                //  System.out.println("Fracked Coin: ");
                cu.consoleReport();

                CoinUtils fixedCC = fixCoin(frackedCC); // Will attempt to unfrack the coin.
                if (!continueExecution) {
                    System.out.println("Aborting Fix 2");
                    break;
                }
                cu.consoleReport();
                switch (fixedCC.getFolder().toLowerCase()) {
                    case "bank":
                        this.totalValueToBank++;
                        this.fileUtils.overWrite(this.fileUtils.BankFolder, fixedCC.cc);
                        this.deleteCoin(this.fileUtils.FrackedFolder + frackedFiles[i].getName());
                        System.out.println("CloudCoin was moved to Bank.");
                        pge.MajorProgressMessage = "CloudCoin was moved to Bank.";
                        raida.OnLogRecieved(pge);

                        //CoreLogger.Log("CloudCoin was moved to Bank.");
                        break;
                    case "counterfeit":
                        this.totalValueToCounterfeit++;
                        this.fileUtils.overWrite(this.fileUtils.CounterfeitFolder, fixedCC.cc);
                        this.deleteCoin(this.fileUtils.FrackedFolder + frackedFiles[i].getName());
                        System.out.println("CloudCoin was moved to Trash.");
                        pge.MajorProgressMessage = "CloudCoin was moved to Trash.";
                        raida.OnLogRecieved(pge);

                        //CoreLogger.Log("CloudCoin was moved to Trash.");
                        break;
                    default://Move back to fracked folder
                        this.totalValueToFractured++;
                        this.deleteCoin(this.fileUtils.FrackedFolder + frackedFiles[i].getName());
                        this.fileUtils.overWrite(this.fileUtils.FrackedFolder, fixedCC.cc);
                        System.out.println("CloudCoin was moved back to Fracked folder.");
                        pge.MajorProgressMessage = "CloudCoin was moved back to Fracked folder.";
                        raida.OnLogRecieved(pge);

                        //CoreLogger.Log("CloudCoin was moved back to Fraked folder.");
                        break;
                }
                // end switch on the place the coin will go
                System.out.println("...................................");
                pge.MajorProgressMessage = "...................................";
                raida.OnLogRecieved(pge);

                System.out.println("");
            } catch (FileNotFoundException ex) {
                //Console.ForegroundColor = ConsoleColor.Red;
                System.out.println(ex);
                //CoreLogger.Log(ex.toString());
                //Console.ForegroundColor = ConsoleColor.White;
            } catch (IOException ioex) {
                //Console.ForegroundColor = ConsoleColor.Red;
                System.out.println(ioex);
                //CoreLogger.Log(ioex.toString());
                //Console.ForegroundColor = ConsoleColor.White;
            } // end try catch
        }// end for each file name that is fracked

        results[0] = this.totalValueToBank;
        results[1] = this.totalValueToCounterfeit; // System.out.println("Counterfeit and Moved to trash: "+totalValueToCounterfeit);
        results[2] = this.totalValueToFractured; // System.out.println("Fracked and Moved to Fracked: "+ totalValueToFractured);
        IsFixing = false;
        continueExecution = true;
        pge.MajorProgressMessage = "Finished Frack Fixing.";
        raida.OnLogRecieved(pge);

        pge.MajorProgressMessage = "Fixed " + totalValueToBank + " CloudCoins and moved them into Bank Folder";
        if (totalValueToBank > 0)
            raida.OnLogRecieved(pge);

        return results;
    }// end fix all

    // End select all file names in a folder
    public boolean deleteCoin(String path) {
        // System.out.println("Deleteing Coin: "+path + this.fileName + extension);
        try {
            Files.delete(Paths.get(path));
        } catch (IOException e) {
            System.out.println(e);
            return false;
            //  CoreLogger.Log(e.toString());
        }
        return true;
    }//end delete coin


    public CoinUtils fixCoin(CloudCoin brokeCoin) {
        CoinUtils cu = new CoinUtils(brokeCoin);
        ProgressChangedEventArgs pge = new ProgressChangedEventArgs();
        /*0. RESET TICKETS IN RAIDA STATUS TO EMPTY*/

        //RAIDA_Status.resetTickets();
        for (Node node : RAIDA.GetInstance().nodes) node.ResetTicket();

        /*0. RESET THE DETECTION to TRUE if it is a new COIN */
        for (Node node : RAIDA.GetInstance().nodes) node.NewCoin();

        //RAIDA_Status.newCoin();

        cu.setAnsToPans();// Make sure we set the RAIDA to the cc ans and not new pans.
        long before = System.currentTimeMillis();

        String fix_result = "";
        FixitHelper fixer;

        /*START*/
        /*1. PICK THE CORNER TO USE TO TRY TO FIX */
        int corner = 1;
        // For every guid, check to see if it is fractured
        for (int raida_ID = 0; raida_ID < 25; raida_ID++) {
            if (!continueExecution) {
                System.out.println("Stopping Execution");
                return cu;
            }
            //  System.out.println("Past Status for " + raida_ID + ", " + brokeCoin.pastStatus[raida_ID]);

            if (cu.getPastStatus(raida_ID).toLowerCase() != "pass")//will try to fix everything that is not perfect pass.
            {

                //Console.ForegroundColor = ConsoleColor.Cyan;
                System.out.println("Attempting to fix RAIDA " + raida_ID);
                pge.MajorProgressMessage = "Attempting to fix RAIDA " + raida_ID;
                raida.OnLogRecieved(pge);
                // CoreLogger.Log("Attempting to fix RAIDA " + raida_ID);
                System.out.println("");
                //Console.ForegroundColor = ConsoleColor.White;

                fixer = new FixitHelper(raida_ID, brokeCoin.an.toArray(new String[0]));

                //trustedServerAns = new String[] { brokeCoin.ans[fixer.currentTriad[0]], brokeCoin.ans[fixer.currentTriad[1]], brokeCoin.ans[fixer.currentTriad[2]] };
                corner = 1;
                while (!fixer.finished) {
                    if (!continueExecution) {
                        System.out.println("Stopping Execution");
                        return cu;
                    }
                    //Console.ForegroundColor = ConsoleColor.White;
                    System.out.println(" Using corner " + corner + " Pown is " + brokeCoin.pown);
                    pge.MajorProgressMessage = " Using corner " + corner;
                    raida.OnLogRecieved(pge);
                    //   CoreLogger.Log(" Using corner " + corner);
                    fix_result = fixOneGuidCorner(raida_ID, brokeCoin, corner, fixer.currentTriad);
                    // System.out.println(" fix_result: " + fix_result + " for corner " + corner);
                    if (fix_result.contains("success")) {
                        //Fixed. Do the fixed stuff
                        cu.setPastStatus("pass", raida_ID);
                        fixer.finished = true;
                        corner = 1;
                    } else {
                        //Still broken, do the broken stuff.
                        corner++;
                        fixer.setCornerToCheck(corner);
                    }
                }//End whild fixer not finnished
            }//end if RAIDA past status is passed and does not need to be fixed
        }//end for each AN

        for (int raida_ID = 24; raida_ID > 0; raida_ID--) {
            //  System.out.println("Past Status for " + raida_ID + ", " + brokeCoin.pastStatus[raida_ID]);
            if (!continueExecution) {
                return cu;
            }

            if (cu.getPastStatus(raida_ID).toLowerCase() != "pass")//will try to fix everything that is not perfect pass.
            {

                //Console.ForegroundColor = ConsoleColor.Cyan;
                System.out.println("");
                System.out.println("Attempting to fix RAIDA " + raida_ID);
                pge.MajorProgressMessage = "Attempting to fix RAIDA " + raida_ID;
                raida.OnLogRecieved(pge);
                //  CoreLogger.Log("Attempting to fix RAIDA " + raida_ID);
                System.out.println("");
                //Console.ForegroundColor = ConsoleColor.White;

                fixer = new FixitHelper(raida_ID, brokeCoin.an.toArray(new String[0]));

                //trustedServerAns = new String[] { brokeCoin.ans[fixer.currentTriad[0]], brokeCoin.ans[fixer.currentTriad[1]], brokeCoin.ans[fixer.currentTriad[2]] };
                corner = 1;
                while (!fixer.finished) {
                    System.out.println(" Using corner " + corner);
                    pge.MajorProgressMessage = " Using corner " + corner;
                    raida.OnLogRecieved(pge);
                    //CoreLogger.Log(" Using corner " + corner);
                    fix_result = fixOneGuidCorner(raida_ID, brokeCoin, corner, fixer.currentTriad);
                    // System.out.println(" fix_result: " + fix_result + " for corner " + corner);
                    if (fix_result.contains("success")) {
                        //Fixed. Do the fixed stuff
                        cu.setPastStatus("pass", raida_ID);
                        fixer.finished = true;
                        corner = 1;
                    } else {
                        //Still broken, do the broken stuff.
                        corner++;
                        fixer.setCornerToCheck(corner);
                    }
                }//End whild fixer not finnished
            }//end if RAIDA past status is passed and does not need to be fixed
        }//end for each AN
        long after = System.currentTimeMillis();
        long ts = after - before;
        System.out.println("Time spent fixing RAIDA in milliseconds: " + ts);
        pge.MajorProgressMessage = "Time spent fixing RAIDA in milliseconds: " + ts;
        raida.OnLogRecieved(pge);
        //CoreLogger.Log("Time spent fixing RAIDA in milliseconds: " + ts.Milliseconds);

        cu.calculateHP();//how many fails did it get
        //  cu.gradeCoin();// sets the grade and figures out what the file extension should be (bank, fracked, counterfeit, lost

        cu.grade();
        cu.calcExpirationDate();
        return cu;
    }// end fix coin

}//end class
