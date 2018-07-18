package com.cloudcore.authenticator.utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NodeEchoResponse {


    @Expose
    @SerializedName("message")
    public String message = "";
    @Expose
    @SerializedName("status")
    public String status = "";
    @Expose
    @SerializedName("version")
    public String version = "";
    @Expose
    @SerializedName("server")
    public String server = "";
    @Expose
    @SerializedName("time")
    public String time = "";
}
