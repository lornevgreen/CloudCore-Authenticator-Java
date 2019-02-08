package com.cloudcore.authenticator.core;

public class Config  {


    /* Constant Fields */

    public static final String MODULE_NAME = "authenticator";

    public static final boolean DEBUG_MODE = false;

    public static final String URL_DIRECTORY = "http://michael.pravoslavnye.ru/";

    public static final String TAG_DETECTED = "Detected";
    public static final String TAG_IMPORT = "Import";
    public static final String TAG_SUSPECT = "Suspect";

    public static final String TAG_LOGS = "Logs";


    /* Fields */

    public static int milliSecondsToTimeOut = 2000;
    public static int multiDetectLoad = 200;
    public static int nodeCount = 25;
    public static int passCount = 16;

}
