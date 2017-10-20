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

    public boolean active;

    private final String SALT = "1AQQB-90KXZ-Z1Y91-UINT8";

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

    public String getHashedPassword(){
        return  VCCryptography.getMD5Hash(SALT + standardPassword);
    }

    @Override
    public String toString(){
        return "\nName="+name+"\nhasPassword="+hasPassword+"\nIPAddress="+IPAddress+"\nstandardPassword="+standardPassword+"\nRSAPublicKey="+RSAPublicKey;
    }
}
