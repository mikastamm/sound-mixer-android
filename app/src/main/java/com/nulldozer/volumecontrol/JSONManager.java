package com.nulldozer.volumecontrol;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Mika on 19.08.2017.
 */
public class JSONManager {
    public static String serialize(Object toSerialize)
    {
        GsonBuilder gsonb = new GsonBuilder();
        Gson gson = gsonb.create();

        return gson.toJson(toSerialize);
    }

    public static <T> T deserialize(String json, Class<T> tClass)
    {
        try {
            GsonBuilder gsonb = new GsonBuilder();
            Gson gson = gsonb.create();
            return gson.fromJson(json, tClass);
        }
        catch(Exception ex)
        {
            Log.i("JSONManager", "String in Question:"+json);
            ex.printStackTrace();
            return null;
        }
    }
}
