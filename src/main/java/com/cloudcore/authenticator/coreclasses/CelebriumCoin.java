package com.cloudcore.authenticator.coreclasses;

import com.cloudcore.authenticator.core.CloudCoin;

class CelebriumCoin extends CloudCoin {

    @Override
    public String FileName() {
        return this.getDenomination() + ".Celebrium." + nn + "." + getSn() + ".";
    }
}
