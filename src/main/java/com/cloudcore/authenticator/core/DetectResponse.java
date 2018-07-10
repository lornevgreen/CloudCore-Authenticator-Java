package com.cloudcore.authenticator.core;

import com.google.gson.annotations.SerializedName;

public class DetectResponse {
    
    
    @SerializedName("server")
    public String server;

    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String Message;

    @SerializedName("time")
    public String time;

    @SerializedName("version")
    public String version;
}
