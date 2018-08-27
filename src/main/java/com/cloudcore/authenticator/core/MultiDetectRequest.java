package com.cloudcore.authenticator.core;

public class MultiDetectRequest {


    public int[] nn ;
    public int[] sn;
    public String[][] an = new String[Config.nodeCount][];
    public String[][] pan = new String[Config.nodeCount][];
    public int[] d;
    public int timeout;
}
