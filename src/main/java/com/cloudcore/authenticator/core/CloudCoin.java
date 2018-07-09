package com.cloudcore.authenticator.core;

import com.cloudcore.authenticator.Config;
import com.cloudcore.authenticator.RAIDA;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;

public class CloudCoin {

    public String[] pan = new String[Config.NodeCount];
    public int hp;// HitPoints (1-25, One point for each server not failed)
    public String edHex;// Months from zero date that the coin will expire. 

    public String folder;

    //public Response[] response = new Response[Config.NodeCount];

    public String[] gradeStatus = new String[3];// What passed, what failed, what was undetected
    //Fields

    public int nn;


    private int sn;
    public int getSn() {
        return pSN;
    }
    public void setSn(int sn) {
        this.sn = sn;
        denomination = getDenomination();
    }


    public List<String> an;


    public String ed;


    public String pown;


    public List<String> aoid;


    public String pastPown = "uuuuuuuuuuuuuuuuuuuuuuuuu";//Used to see if there are any improvments in defracking


    public boolean IsPerfect;

    public boolean IsCounterfeit;

    public boolean IsGradable;

    public boolean IsFracked;

    public int denomination;
    public String DetectionResult;

    public DetectionResult detectionResult;

    public DetectionStatus DetectResult;
    private int PassCount;
    private int passCount = 0;
    private int failCount = 0;
    private int FailCount;

    public int getPassCount() {
        return passCount;
    }
    public void setPassCount(int passCount) {
        this.passCount = passCount;
        DetectionResult = (passCount >= Config.PassCount) ? "Pass" :  "Fail";
    }

    public int getFailCount() {
        return failCount;
    }
    public void setFailCount(int failCount) {
        this.failCount = failCount;
        DetectionResult = (passCount >= Config.PassCount) ? "Pass" :  "Fail";
    }

    public enum Folder { Suspect, Counterfeit, Fracked, Bank, Trash };


    int pSN;
    
    
    //Constructors
    
    public CloudCoin()
    {
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
        this.an = Arrays.asList(ans);
    }

    public CloudCoin(int nn, int sn, List<String> an, String ed, String pown, List<String> aoid)
    {
        this.nn = nn;
        this.sn = sn;
        this.an = an;
        this.ed = ed;
        this.pown = pown;
        this.aoid = aoid;

    }

    public CloudCoin(String fileName)
    {

    }

    public static CloudCoin FromJson(String csvLine)
    {
        try
        {

        }
        catch(Exception e)
        {

        }
        return null;

    }
    public static CloudCoin FromCSV(String csvLine)
    {
        try
        {
            CloudCoin coin = new CloudCoin();
            String[] values = csvLine.split(",");
            System.out.println(values[0]);
            coin.sn = Integer.parseInt(values[0]);
            coin.nn = Integer.parseInt(values[1]);
            coin.denomination = Integer.parseInt(values[1]);
            coin.an = new ArrayList<>();
            for (int i = 0; i < Config.NodeCount; i++)
            {
                coin.an.add(values[i + 3]);
            }

            return coin;

        }
        catch (Exception e)
        {

        }
        return null;
    }

    public String FileName() {
        return this.getDenomination() + ".CloudCoin." + nn + "." + sn + ".";
    }

    public boolean isDangerous() {
        //The coin is considered a threat if it has any of the patersns that would allow the last user to take control.
        //There are four of these patterns: One for each corner.

        //  Console.Out.WriteLine( cc.sn + " char count f =" + charCount(cc.pown, 'f'));
        if ((charCount(pown, 'f') + charCount(pown, 'n')) > 5) {
            String doublePown = pown + pown;//double it so we see patters that happen on the ends.

            boolean UP_LEFT = doublePown.matches("(?i)ff[a-z][a-z][a-z]fp");
            boolean UP_RIGHT = doublePown.matches("(?i)ff[a-z][a-z][a-z]pf");
            boolean DOWN_LEFT = doublePown.matches("(?i)fp[a-z][a-z][a-z]ff");
            boolean DOWN_RIGHT = doublePown.matches("(?i)pf[a-z][a-z][a-z]ff");

            if (UP_LEFT || UP_RIGHT || DOWN_LEFT || DOWN_RIGHT) {
                return true;
            }
        }
        return false;
    }

    public boolean isCounterfeit() {
        //The coin is considered counterfeit if it has so many fails it cannot be fixed
        boolean returnTruth = false;
        if ((charCount(pown, 'p') < 6 && (charCount(pown, 'f') > 13)))
        {
            returnTruth = true;
            //   Console.Out.WriteLine("isCounterfeit");
        }
        else
        {
            // Console.Out.WriteLine("Not isCounterfeit");
        }
        return returnTruth;
    }

    public int charCount(String pown, char character)
    {
        return pown.length() - pown.replace(Character.toString(character), "").length();
    }

    public String GetCSV()
    {
        String csv = this.sn + "," + this.nn + ",";


        for (int i = 0; i < Config.NodeCount; i++)
        {
            csv += an.get(i) + ",";
        }

        return csv.substring(0, csv.length() - 1);
    }
    public boolean isFracked()
    {
        //The coin is considered fracked if it has any fails
        boolean returnTruth = false;
        if (charCount(pown, 'f') > 0 || charCount(pown, 'n') > 0)
        {
            returnTruth = true;
        }
        return returnTruth;
    }

    public boolean isPerfect()
    {
        boolean returnTruth = false;
        if (pown == "ppppppppppppppppppppppppp")
        {
            returnTruth = true;
        }
        return returnTruth;
    }
    public int getDenomination()
    {
        int nom = 0;
        if ((sn < 1))
        {
            nom = 0;
        }
        else if ((sn < 2097153))
        {
            nom = 1;
        }
        else if ((sn < 4194305))
        {
            nom = 5;
        }
        else if ((sn < 6291457))
        {
            nom = 25;
        }
        else if ((sn < 14680065))
        {
            nom = 100;
        }
        else if ((sn < 16777217))
        {
            nom = 250;
        }
        else
        {
            nom = '0';
        }

        return nom;
    }

    public List<Task> detectTaskList = new List<Task>();
    public List<Task> GetDetectTasks()
    {
        var raida = RAIDA.GetInstance();

        CloudCoin cc = this;
        int i = 0;

        for (int j = 0; j < Config.NodeCount; j++)
        {
            Task t = Task.Factory.StartNew(() => raida.nodes[i].Detect(cc));
            detectTaskList.add(t);
        }

        return detectTaskList;
    }
    public void GeneratePAN()
    {
        for (int i = 0; i < Config.NodeCount; i++)
        {
            pan[i] = this.generatePan();
        }
    }

    public boolean isFixable()
    {
        //The coin is considered fixable if it has any of the patersns that would allow the new owner to fix fracked.
        //There are four of these patterns: One for each corner. 
        String origPown = pown;
        pown = pown.replace('d', 'e').replace('n', 'e').replace('u', 'e');
        boolean canFix = false;
        // Console.Out.WriteLine(cc.sn + " char count p =" + charCount(cc.pown, 'p'));
        if (charCount(pown, 'p') > 5)
        {
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
                //Console.Out.WriteLine("isFixable");
            }
            else {
                canFix = false;
                //Console.Out.WriteLine("Not isFixable");
            }
        }
        else {
            canFix = false;
//                Console.Out.WriteLine("Not isFixable");
        }
        pown = origPown;
        return canFix;
    }


    public void SetAnsToPans()
    {
        for (int i = 0; (i < Config.NodeCount); i++)
        {
            this.pan[i] = an.get(i);
        }
    }

    public void SetAnsToPansIfPassed(boolean partial = false)
    {
        // now set all ans that passed to the new pans
        char[] pownArray = pown.toCharArray();

        for (int i = 0; (i < Config.NodeCount); i++)
        {
            if (pownArray[i] == 'p')//1 means pass
            {
                an[i] = pan[i];
            }
            else if (pownArray[i] == 'u' && !(RAIDA.GetInstance().nodes[i].RAIDANodeStatus == Node.NodeStatus.NotReady) && partial == false)//Timed out but there server echoed. So it probably changed the PAN just too slow of a response
            {
                an[i] = pan[i];
            }
            else
            {
                // Just keep the ans and do not change. Hopefully they are not fracked. 
            }
        }// for each guid in coin
    }

    public void CalculateHP()
    {
        hp = Config.NodeCount;
        char[] pownArray = pown.toCharArray();
        for (int i = 0; (i < Config.NodeCount); i++)
        {
            if (pownArray[i] == 'f')
            {
                this.hp--;
            }
        }
    }


    public boolean setPastStatus(String status, int raida_id)
    {
        char[] pownArray = this.pown.toCharArray();
        switch (status)
        {
            case "error": pownArray[raida_id] = 'e'; break;
            case "fail": pownArray[raida_id] = 'f'; break;
            case "pass": pownArray[raida_id] = 'p'; break;
            case "undetected": pownArray[raida_id] = 'u'; break;
            case "noresponse": pownArray[raida_id] = 'n'; break;
        }
        this.pown = new String(pownArray);
        return true;
    }

    public void doPostProcessing()
    {
        setPastStatus();
        SetAnsToPansIfPassed();
        CalculateHP();
        CalcExpirationDate();
        //grade();
    }
    public boolean setPastStatus()
    {
        char[] pownArray = pown.toCharArray();
        for (int i = 0; i < Config.NodeCount; i++)
        {
            if (response[i] != null)
            {
                String status = response[i].outcome;
                switch (status)
                {
                    case "error": pownArray[i] = 'e'; break;
                    case "fail": pownArray[i] = 'f'; break;
                    case "pass": pownArray[i] = 'p'; break;
                    case "undetected": pownArray[i] = 'u'; break;
                    case "noresponse": pownArray[i] = 'n'; break;
                }
            }
            else
            {
                pownArray[i] = 'u';
            };// should be pass, fail, error or undetected. 
        }


        this.pown = new String(pownArray);
        return true;
    }

    public String generatePan() {
        SecureRandom random = new SecureRandom();
        byte[] cryptoRandomBuffer = random.generateSeed(16);

        UUID pan = UUID.nameUUIDFromBytes(cryptoRandomBuffer);
        return String.format("%32s", pan).replace(' ', '0');
    }

    public void recordPown()
    {
        //records the last pown so we can see if there are improvments
        pastPown = pown;
    }

    public void SortToFolder()
    {
        //figures out which folder to put it in. 
        //pown = pown.Replace('d', 'e').Replace('n', 'e').Replace('u', 'e');
        //pown = pown.Replace('n','e');
        //pown = pown.Replace('u', 'e');
        if (isPerfect())
        {
            folder =  RAIDA.ActiveRAIDA.FS.BankFolder;
            //folder = Folder.Bank;
            return;
        }//if is perfect

        if (isCounterfeit())
        {
            folder = RAIDA.ActiveRAIDA.FS.CounterfeitFolder;
            //folder = Folder.Counterfeit;
            return;
        }//if is counterfeit

        //--------------------------------------
        /*Now look  at fracked coins*/

        if (isGradablePass())
        {
            if (!isFracked())
            {
                folder = RAIDA.ActiveRAIDA.FS.BankFolder;
                return;
            }
            else
            {
                if (isDangerous())
                {
                    if (isFixable())
                    {
                        recordPown();
                        folder = RAIDA.ActiveRAIDA.FS.DangerousFolder;
                        return;

                    }
                    else
                    {
                        folder = RAIDA.ActiveRAIDA.FS.CounterfeitFolder;
                        return;
                    }
                }
                else
                {
                    if (!isFixable())
                    {
                        folder = RAIDA.ActiveRAIDA.FS.CounterfeitFolder;
                        return;
                    }
                    else
                    {
                        folder = RAIDA.ActiveRAIDA.FS.FrackedFolder;
                        return;
                    }
                }
            }
        }
        else
        {
            if (noResponses())
            {
                folder = RAIDA.ActiveRAIDA.FS.LostFolder;
                //folder = Folder.Lost;
                return;
            }
            else
            {
                folder = RAIDA.ActiveRAIDA.FS.SuspectFolder;
                //folder = Folder.Lost;
                return;
            }
        }
    }
    public boolean noResponses()
    {
        //Does the coin have no-responses from the RIDA. This means the RAIDA may be using its PAN or AN
        //These must be fixed in a special way using both.  
        boolean returnTruth = false;
        if (charCount(pown, 'n') > 0)
        {
            returnTruth = true;
        }
        return returnTruth;
    }


    public boolean isGradablePass()
    {
        //The coin is considered ungradable if it does not get more than 19 RAIDA available
        boolean returnTruth = false;
        if (charCount(pown, 'f') + charCount(pown, 'p') > 16 && isFixable())
        {
            returnTruth = true;
            //Console.Out.WriteLine("isGradable");
        }
        else
        {
            //Console.Out.WriteLine("Not isGradable");
        }
        return returnTruth;
    }


    public String[] grade()
    {
        int total = Config.NodeCount;

        int passed = response.Where(x => x.outcome == "pass").Count();
        int failed = response.Where(x => x.outcome == "fail").Count();
        int other = total - passed - failed;

        if (passed > Config.PassCount)
        {
            DetectResult = DetectionStatus.Passed;
        }
        else
        {
            DetectResult = DetectionStatus.Failed;
        }

        String passedDesc = "";
        String failedDesc = "";
        String otherDesc = "";

        // for each status
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
        
        // Coin will go to bank, counterfeit or fracked
        if (other > 12)
        {
            // not enough RAIDA to have a quorum
            folder = RAIDA.GetInstance().FS.SuspectFolder;
        }
        else if (failed > passed)
        {
            // failed out numbers passed with a quorum: Counterfeit
            folder = RAIDA.GetInstance().FS.CounterfeitFolder;
        }
        else if (failed > 0)
        {
            // The quorum majority said the coin passed but some disagreed: fracked. 
            folder = RAIDA.GetInstance().FS.FrackedFolder;
        }
        else
        {
            // No fails, all passes: bank
            folder = RAIDA.GetInstance().FS.BankFolder;

        }

        gradeStatus[0] = passedDesc;
        gradeStatus[1] = failedDesc;
        gradeStatus[2] = otherDesc;
        return this.gradeStatus;
    }

    public void CalcExpirationDate() {
        LocalDate expirationnnnDate = LocalDate.now().plusYears(Config.YEARSTILEXPIRE);
        ed = (expirationnnnDate.getMonth() + "-" + expirationnnnDate.getYear());

        LocalDate zeroDateee = LocalDate.of(2016, 8, 13);
        int monthsAfterZero = (int) (DAYS.between(expirationnnnDate, zeroDateee) / (365.25 / 12));
        this.edHex = String.format("0x%08X", monthsAfterZero);
    }

    public boolean  containsThreat()
    {
        boolean threat = false;
        String doublePown = pown + pown;
        //There are four threat patterns that would allow attackers to seize other 
        //String UP_LEFT = "ff***f";
        //String UP_RIGHT = "ff***pf";
        //String DOWN_LEFT = "fp***ff";
        //String DOWN_RIGHT = "pf***ff";

        boolean UP_LEFT = doublePown.matches("(?i)ff[a-z][a-z][a-z]fp");
        boolean UP_RIGHT = doublePown.matches("(?i)ff[a-z][a-z][a-z]pf");
        boolean DOWN_LEFT = doublePown.matches("(?i)fp[a-z][a-z][a-z]ff");
        boolean DOWN_RIGHT = doublePown.matches("(?i)pf[a-z][a-z][a-z]ff");

        //Check if 
        if (UP_LEFT || UP_RIGHT || DOWN_LEFT || DOWN_RIGHT) {
            threat = true;
        }


        return threat;
    }



    public enum DetectionStatus
    {
        Passed,
        Failed,
        Other
    }
    public class DetectionResult
    {
        public DetectionStatus Result;
        public int PassCount;
        public int FailCount;
        public int OtherCount;

        public int Description;
    }
}
