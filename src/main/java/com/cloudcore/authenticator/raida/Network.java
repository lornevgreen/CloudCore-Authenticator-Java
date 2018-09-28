package com.cloudcore.authenticator.raida;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Network {


    @Expose
    @SerializedName("nn")
    public int nn;
    @Expose
    @SerializedName("raida")
    public RAIDANode[] raida;
}

