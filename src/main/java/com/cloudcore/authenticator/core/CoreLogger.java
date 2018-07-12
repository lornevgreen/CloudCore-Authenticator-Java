package com.cloudcore.authenticator.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CoreLogger {


    static String assemblyFile = (new System.Uri(Directory.GetCurrentDirectory())).AbsolutePath;


    //Fields
    static String basedir = assemblyFile + File.pathSeparator;
    static String logFolder = basedir + "Logs" + File.pathSeparator;


    //Constructors


    static void createDir() {
        try {
            if (!Files.exists(Paths.get(logFolder))) {
                Files.createDirectory(Paths.get(logFolder));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public static /*async*/ void Log(String message, String classpath) {
        String path = logFolder + "other.log";
        try {
            createDir();
            String classname = classpath.substring(classpath.lastIndexOf(File.pathSeparator) + 1, classpath.lastIndexOf('.')).toLowerCase();
            path = logFolder + classname + ".log";

            TextWriter tw = File.AppendText(path);
            using(tw)
            {
                await tw.WriteLineAsync(DateTime.Now.toString());
                await tw.WriteLineAsync(message);

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
