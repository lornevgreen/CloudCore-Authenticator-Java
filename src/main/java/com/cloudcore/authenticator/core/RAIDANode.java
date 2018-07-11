package com.cloudcore.authenticator.core;

import com.google.gson.annotations.SerializedName;

public class RAIDANode {

    @SerializedName("raida_index")
    public int raida_index;

    @SerializedName("failsEcho")
    public boolean failsEcho;

    @SerializedName("failsDetect")
    public boolean failsDetect;

    @SerializedName("failsFix")
    public boolean failsFix;

    @SerializedName("failsTicket")
    public boolean failsTicket;

    @SerializedName("location")
    public String location;

    @SerializedName("urls")
    public NodeURL[] urls;
}
