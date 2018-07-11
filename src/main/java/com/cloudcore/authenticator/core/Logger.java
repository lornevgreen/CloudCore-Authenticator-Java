package com.cloudcore.authenticator.core;

public abstract class Logger {
    public abstract void DisplayMessage(String message, LogLevel logLevel=LogLevel.INFO, boolean writeToLog =false);
}


