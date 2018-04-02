package com.nulldozer.volumecontrol;

import android.animation.Keyframe;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Created by Mika on 24.08.2017.
 */
public class VCCryptography {
    private static final String TAG = "VCCryptography";

    private static RSAPublicKey RSAPublicKey;
    private static PrivateKey RSAPrivateKey;

    public static String getDecryptedMessage(String encryptedJSON)
    {
        String[] segments = JSONManager.deserialize(encryptedJSON, String[].class);
        String decrypted = "";

        for(int i = 0; i < segments.length; i++)
        {
            try {
                decrypted += new String(decrypt(segments[i]), "UTF-8");
            }
            catch (UnsupportedEncodingException ex){
                ex.printStackTrace();
            }
        }
        return decrypted;
    }

    public static byte[] decrypt(String encrypted)
    {
        byte[] decrypted = null;
        try {
            byte[] raw = Base64.decode(encrypted, Base64.NO_WRAP);

            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            // encrypt the plain text using the public key

            cipher.init(Cipher.DECRYPT_MODE, RSAPrivateKey);
            decrypted = cipher.doFinal(raw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    public static byte[] encrypt(String text, String publicKeyJSON) {
        byte[] cipherText = null;
        PublicKey key;

        if(publicKeyJSON != null)
            key = getPublicKeyFromString(publicKeyJSON);
        else
            return null;

        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            // encrypt the plain text using the public key

            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public static String getRSAPublicKeyJSON(){
        SimpleRSAPublicKey pKey = new SimpleRSAPublicKey();

        byte[] modulusBytes = RSAPublicKey.getModulus().toByteArray(); //Might need to strip leading zeros
        byte[] modulusBytesWithoutZeros = new byte[modulusBytes.length-1];

        for(int i = 1; i < modulusBytes.length; i++)
        {
            modulusBytesWithoutZeros[i-1] = modulusBytes[i];
        }

        pKey.Modulus = Base64.encodeToString(modulusBytesWithoutZeros, Base64.NO_WRAP);

        byte[] exponentBytes = RSAPublicKey.getPublicExponent().toByteArray();
        pKey.Exponent = Base64.encodeToString(exponentBytes, Base64.NO_WRAP);

        return JSONManager.serialize(pKey);
    }

    public static void generateRSAKeyPair(boolean ignoreGenerated, MainActivity mainActivity){
        SharedPreferences prefs = mainActivity.getPreferences(Context.MODE_PRIVATE);
        try {
            if(ignoreGenerated || (prefs.getString(PrefKeys.RSAPrivateKey, null) == null && prefs.getString(PrefKeys.RSAPublicKey, null) == null))
            {
                Log.i(TAG, "Keys not generated yet, now generating keys");
                KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
                gen.initialize(2048);
                KeyPair keys = gen.genKeyPair();

                PrivateKey privateKey = keys.getPrivate();
                PublicKey publicKey = keys.getPublic();

                byte[] publicKeyBytes = publicKey.getEncoded();
                byte[] privateKeyBytes = privateKey.getEncoded();
                String publicKeyBase64 = Base64.encodeToString(publicKeyBytes, Base64.NO_WRAP);
                String privateKeyBase64 = Base64.encodeToString(privateKeyBytes, Base64.NO_WRAP);

                SharedPreferences.Editor editor = prefs.edit();

                editor.putString(PrefKeys.RSAPrivateKey, privateKeyBase64);
                editor.putString(PrefKeys.RSAPublicKey, publicKeyBase64);
                editor.apply();

                RSAPrivateKey = privateKey;
                RSAPublicKey = (RSAPublicKey)publicKey;
            }
            else{
                String privateKeyBase64 = prefs.getString(PrefKeys.RSAPrivateKey, null);
                String publicKeyBase64 = prefs.getString(PrefKeys.RSAPublicKey, null);

                KeyFactory kf = KeyFactory.getInstance("RSA");

                RSAPrivateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(privateKeyBase64, Base64.NO_WRAP)));
                RSAPublicKey = (RSAPublicKey)kf.generatePublic(new X509EncodedKeySpec(Base64.decode(publicKeyBase64, Base64.NO_WRAP)));

            }
            Log.i(TAG, "Format=" + RSAPrivateKey.getFormat() +"\nPublicKey=" + RSAPublicKey + "\nPrivateKey=" + RSAPrivateKey);
        }
        catch (NoSuchAlgorithmException|InvalidKeySpecException ex)
        {
            ex.printStackTrace();
        }
    }

    public static String getMD5Hash(String toHash)
    {
        byte[] toHashBytes = toHash.getBytes(Charset.forName("UTF-8"));
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(toHashBytes);

            for (int i = 0; i < hash.length; i++) {
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0"
                            + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }
        }
        catch (NoSuchAlgorithmException ex)
        {
            ex.printStackTrace();
        }
        return hexString.toString().toUpperCase();
    }

    public static PublicKey getPublicKeyFromString(String key)
    {
        PublicKey pbKey = null;

        SimpleRSAPublicKey sKey = JSONManager.deserialize(key, SimpleRSAPublicKey.class);

        byte[] modulus = Base64.decode(sKey.Modulus, 0);
        byte[] exponent = Base64.decode(sKey.Exponent, 0);
        BigInteger modBigInteger = new BigInteger(1,modulus);
        BigInteger exBigInteger = new BigInteger(1,exponent);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modBigInteger, exBigInteger);
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            pbKey = factory.generatePublic(spec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  pbKey;
    }


}
