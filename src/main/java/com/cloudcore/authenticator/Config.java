package com.cloudcore.authenticator;

public class Config  {

    public static final int YEARSTILEXPIRE = 2;

    public static String TAG_IMPORT = "Import";
    public static String TAG_EXPORT = "Export";
    public static String TAG_BANK = "Bank";
    public static String TAG_LOST = "Lost";
    public static String TAG_IMPORTED = "Imported";
    public static String TAG_FRACKED = "Fracked";
    public static String TAG_TEMPLATES = "Templates";
    public static String TAG_COUNTERFEIT = "Counterfeit";
    public static String TAG_DETECTED = "Detected";
    public static String TAG_LANGUAGE = "Language";
    public static String TAG_PARTIAL = "Partial";
    public static String TAG_TRASH = "Trash";
    public static String TAG_SUSPECT = "Suspect";
    public static String TAG_PREDETECT = "Predetect";
    public static String URL_DIRECTORY = "http://michael.pravoslavnye.ru/";
    public static String TAG_REQUESTS = "Requests";

    public static int milliSecondsToTimeOut = 20000;
    public static int MultiDetectLoad = 200;
    public static int NodeCount = 25;
    public static int PassCount = 16;
    public static int MinimumReadyCount = 16;

    public static int NetworkNumber = 1;

    public enum Folder { Suspect, Counterfeit, Fracked, Bank, Trash };

    public static String[] allowedExtensions = new String[] { ".stack", ".jpeg", ".chest", ".bank", ".jpg",".celebrium",".celeb",".csv" };

    public static String TAG_DANGEROUS = "Dangerous";
    public static String TAG_LOGS = "Logs";
    public static String TAG_QR = "QrCodes";
    public static String TAG_BARCODE = "Barcodes";
    public static String TAG_CSV = "CSV";

    public static String URL_JPEG_Exists = "https://raida.tech/jpeg_exists.php?nn={0}&sn={1}";
    public static String URL_GET_TICKET = "get_ticket?nn={0}&sn={1}&an={2}&pan={3}&denomination={4}";
    public static String URL_GET_IMAGE = "https://raida.tech/get_template.php?nn={0}&sn={1}&fromserver1={2}&message1={3}";

}
