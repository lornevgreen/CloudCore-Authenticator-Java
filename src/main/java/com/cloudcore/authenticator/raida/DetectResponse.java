package com.cloudcore.authenticator.raida;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class DetectResponse {


    @Expose
    @SerializedName("server")
    public String server;
    @Expose
    @SerializedName("status")
    public String status;
    @Expose
    @SerializedName("message")
    public String Message;
    @Expose
    @SerializedName("time")
    public String time;
    @Expose
    @SerializedName("version")
    public String version;
}
