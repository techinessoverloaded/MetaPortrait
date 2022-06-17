package com.apkaproj.metaportrait.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.lang.Exception;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils
{
    private static SecretKeySpec secretKey;
    private static byte [] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static IvParameterSpec ivSpec = new IvParameterSpec(iv);
    private static byte[] key;

    private static void setKey(String myKey)
    {
        MessageDigest sha;
        try {
            key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String encrypt(String strToEncrypt, String keyForEncryption)
    {
        try
        {
            setKey(keyForEncryption);
            Log.d("secretKey",secretKey.toString());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            return Base64.encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)),Base64.DEFAULT);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
            Log.d("exception",e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String keyForDecryption)
    {
        try
        {
            setKey(keyForDecryption);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            String result = new String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)), StandardCharsets.UTF_8);
            Log.d("decryptionResult",result);
            return result;
        }
        catch (Exception e)
        {
            Log.d("exception", e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getEncryptedImageAsByteArray(Bitmap bitmap, String keyForEncryption)
    {
        try
        {
            setKey(keyForEncryption);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return cipher.doFinal(baos.toByteArray());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getDecryptedImageAsBitmap(byte[] bytes, String keyForDecryption)
    {
        try
        {
            setKey(keyForDecryption);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decryptedArray = cipher.doFinal(bytes);
            return BitmapFactory.decodeByteArray(decryptedArray, 0, decryptedArray.length);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}