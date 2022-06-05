package com.apkaproj.metaportrait.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class PreferenceUtils
{
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static PreferenceUtils instance=null;
    private static String IS_FIRST_TIME="IS_FIRST_TIME";
    private static String NEEDS_DB_UPDATE="NEEDS_DB_UPDATE";

    private PreferenceUtils(Context context)
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static synchronized PreferenceUtils getInstance(Context con)
    {
        if(instance==null)
        {
            instance=new PreferenceUtils(con.getApplicationContext());
        }
        return instance;
    }

    public boolean getIsFirstTime()
    {
        return sharedPreferences.getBoolean(IS_FIRST_TIME,true);
    }

    public void setIsFirstTime(boolean value)
    {
        editor = sharedPreferences.edit();
        editor.putBoolean(IS_FIRST_TIME,value);
        editor.apply();
    }

    public boolean getNeedsDbUpdate()
    {
        return sharedPreferences.getBoolean(NEEDS_DB_UPDATE,true);
    }

    public void setNeedsDbUpdate(boolean value)
    {
        editor = sharedPreferences.edit();
        editor.putBoolean(NEEDS_DB_UPDATE,value);
        editor.apply();
    }
}
