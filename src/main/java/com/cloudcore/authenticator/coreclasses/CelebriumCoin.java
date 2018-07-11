package com.cloudcore.authenticator.coreclasses;

class CelebriumCoin :CloudCoin
        {

        [JsonIgnore]
public new String FileName
        {
        get
        {
        return this.getDenomination()+".Celebrium."+nn+"."+sn+".";
        }
        }
        }
