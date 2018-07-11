package com.cloudcore.authenticator.utils;

import com.google.gson.annotations.SerializedName;

public class NodeEchoResponse {


    @SerializedName("message")
    public String message = "";

    @SerializedName("status")
    public String status = "";

    @SerializedName("version")
    public String version = "";

    @SerializedName("server")
    public String server = "";

    @SerializedName("time")
    public String time = "";
}
