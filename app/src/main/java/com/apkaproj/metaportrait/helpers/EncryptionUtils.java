package com.apkaproj.metaportrait.helpers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;

public class EncryptionUtils
{
    private static SecretKeySpec secretKey;
    private static  byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static IvParameterSpec ivspec = new IvParameterSpec(iv);
    private static byte[] key;
    private static void setKey(String myKey)
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    public static String encrypt(String strToEncrypt,String Key)
    {
        try
        {
            setKey(Key);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,secretKey,ivspec);
            return Base64.encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)),Base64.DEFAULT);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String Key)
    {
        try
        {
            setKey(Key);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE,secretKey,ivspec);
            return new String(cipher.doFinal(Base64.decode(strToDecrypt,Base64.DEFAULT)),
                    StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}