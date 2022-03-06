package com.apkaproj.metaportrait.helpers;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class RandomStringGenerator
{
    public static String getRandomString(int length)
    {
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return new String(array, StandardCharsets.UTF_8);
    }
}
