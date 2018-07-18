package com.cloudcore.authenticator.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NodeURL {


    @Expose
    @SerializedName("url")
    public String url;
    @Expose
    @SerializedName("port")
    public Integer port;
    @Expose
    @SerializedName("milliseconds")
    public Integer milliseconds;
}
