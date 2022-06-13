package com.apkaproj.metaportrait.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils
{
    private var secretKey: SecretKeySpec? = null
    private val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    private val ivSpec = IvParameterSpec(iv)
    private lateinit var key: ByteArray

    private fun setKey(myKey: String)
    {
        val sha: MessageDigest
        try
        {
            key = myKey.toByteArray(StandardCharsets.UTF_8)
            sha = MessageDigest.getInstance("SHA-1")
            key = sha.digest(key)
            key = key.copyOf(16)
            secretKey = SecretKeySpec(key, "AES")
        }
        catch (e: NoSuchAlgorithmException)
        {
            e.printStackTrace()
        }
    }

    fun encrypt(strToEncrypt: String, Key: String): String?
    {
        try
        {
            setKey(Key)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            return Base64.encodeToString(
                cipher.doFinal(strToEncrypt.toByteArray(StandardCharsets.UTF_8)),
                Base64.DEFAULT
            )
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return null
    }

    fun decrypt(strToDecrypt: String?, Key: String): String?
    {
        try
        {
            setKey(Key)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            return String(
                cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)),
                StandardCharsets.UTF_8
            )
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return null
    }

    fun getEncryptedImageAsByteArray(bitmap: Bitmap, key: String): ByteArray?
    {
        try
        {
            setKey(key)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            return cipher.doFinal(baos.toByteArray())
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return null
    }

    fun getDecryptedImageAsBitmap(bytes: ByteArray?, key: String): Bitmap?
    {
        try
        {
            setKey(key)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val decryptedArray = cipher.doFinal(bytes)
            return BitmapFactory.decodeByteArray(decryptedArray, 0, decryptedArray.size)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return null
    }
}