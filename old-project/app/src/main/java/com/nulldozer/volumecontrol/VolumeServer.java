package com.nulldozer.volumecontrol;

import android.util.Xml;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UnknownFormatConversionException;
import java.util.UnknownFormatFlagsException;

/**
 * Created by Mika on 18.08.2017.
 */
public class VolumeServer {
    public String name;
    public boolean hasPassword;
    public String IPAddress;
    public String standardPassword;
    public String RSAPublicKey;

    public VolumeServer(boolean hasPassword, String name, String IPAddress, String standardPassword)
    {
        this.name = name;
        this.IPAddress = IPAddress;
        this.standardPassword = standardPassword;
        this.hasPassword = hasPassword;
    }

    public VolumeServer(boolean hasPassword, String name, String IPAddress)
    {
        this.name = name;
        this.IPAddress = IPAddress;
        this.standardPassword = "";
        this.hasPassword = hasPassword;
    }

    @Override
    public String toString(){
        return "\nName="+name+"\nhasPassword="+hasPassword+"\nIPAddress="+IPAddress+"\nstandardPassword="+standardPassword+"\nRSAPublicKey="+RSAPublicKey;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o.getClass().equals(VolumeServer.class))
        {
            return ((VolumeServer)o).RSAPublicKey.equals(RSAPublicKey);
        }
        else {
            return super.equals(o);
        }
    }
}
