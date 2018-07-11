package com.cloudcore.authenticator.utils;

import com.cloudcore.authenticator.core.Config;

public class EchoResponse {


    public int ReadyCount = 0;
    public int NotReadyCount = 0;
    public int NetworkNumber = 0;

    public NodeEchoResponse[] responses = new NodeEchoResponse[Config.NodeCount];
}
