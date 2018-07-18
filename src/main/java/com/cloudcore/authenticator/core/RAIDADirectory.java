package com.cloudcore.authenticator.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RAIDADirectory {

    @Expose
    @SerializedName("networks")
    public Network[] networks;
}
