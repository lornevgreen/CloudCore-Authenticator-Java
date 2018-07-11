package com.cloudcore.authenticator.core;

import com.google.gson.annotations.SerializedName;

public class NodeURL {

    @SerializedName("url")
    public String url;

    @SerializedName("port")
    public Integer port;

    @SerializedName("milliseconds")
    public Integer milliseconds;
}
