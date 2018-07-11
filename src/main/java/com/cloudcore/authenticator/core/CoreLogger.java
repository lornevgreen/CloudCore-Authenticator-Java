package com.cloudcore.authenticator.core;

public class CoreLogger {


    static String assemblyFile = (new System.Uri(Directory.GetCurrentDirectory())).AbsolutePath;


    //Fields
    static String basedir = assemblyFile + Path.DirectorySeparatorChar;
    static String logFolder = basedir + "Logs" + Path.DirectorySeparatorChar;


    //static String coinutilsLogFile = logFolder + "coinutils.log";
    //static String detectionagentLogFile = logFolder + "detectionagent.log";
    //static String detectorLogFile = logFolder + "detector.log";
    //static String dumperLogFile = logFolder + "dumper.log";
    //static String exporterLogFile = logFolder + "exporter.log";
    //static String fileutilsLogFile = logFolder + "fileutils.log";
    //static String frack_fixerLogFile = logFolder + "frack_fixer.log";
    //static String importerLogFile = logFolder + "importer.log";
    //static String raidaLogFile = logFolder + "raida.log";


    //Constructors


    static void createDir() {
        try {
            if (Directory.Exists(logFolder) == false) {
                Directory.CreateDirectory(logFolder);
            }
        } catch (Exception e) {
            System.out.println(e.Message);
        }


    }


    public static /*async*/ void Log(String message, [System.Runtime.CompilerServices.CallerFilePath]String classpath="") {
        String path = logFolder + "other.log";
        try {
            createDir();
            String classname = Path.GetFileNameWithoutExtension(classpath).toLowerCase();
            path = logFolder + classname + ".log";

            TextWriter tw = File.AppendText(path);
            using(tw)
            {
                await tw.WriteLineAsync(DateTime.Now.toString());
                await tw.WriteLineAsync(message);

            }
        } catch (Exception e) {
            System.out.println(e.Message);
        }


    }


}
