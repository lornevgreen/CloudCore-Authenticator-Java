package com.cloudcore.authenticator.core;

import com.google.gson.annotations.SerializedName;

public class Network {

    @SerializedName("nn")
    public int nn;

    @SerializedName("raida")
    public RAIDANode[] raida;
}

