package com.cloudcore.authenticator.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Stack {

    @Expose
    @SerializedName("cloudcoin")
    public CloudCoin[] cc;

    public Stack(CloudCoin coin) {
        cc = new CloudCoin[1];
        cc[0] = coin;
    }

    public Stack(ArrayList<CloudCoin> coins) {
        cc = coins.toArray(new CloudCoin[0]);
    }

}
