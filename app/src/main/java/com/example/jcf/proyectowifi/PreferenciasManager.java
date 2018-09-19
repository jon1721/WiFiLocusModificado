package com.example.jcf.proyectowifi;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jon on 14/11/2015.
 */
public class PreferenciasManager
{
    public static void WriteString(String key, String value)
    {

        final SharedPreferences preferencias = Aplicacion.getAppContext().getSharedPreferences(
                Aplicacion.PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String ReadString(String key)
    {

        final SharedPreferences preferencias = Aplicacion.getAppContext().getSharedPreferences(
                Aplicacion.PREFERENCIAS, Context.MODE_PRIVATE);
        return preferencias.getString(key, "");
    }

    public static void WriteInt(String key, int value)
    {

        final SharedPreferences preferencias = Aplicacion.getAppContext().getSharedPreferences(
                Aplicacion.PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int ReadInt(String key)
    {

        final SharedPreferences preferencias = Aplicacion.getAppContext().getSharedPreferences(
                Aplicacion.PREFERENCIAS, Context.MODE_PRIVATE);
        return preferencias.getInt(key, -1);
    }

    public static void WriteBoolean(String key, boolean value)
    {
        final SharedPreferences preferencias = Aplicacion.getAppContext().getSharedPreferences(
                Aplicacion.PREFERENCIAS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean ReadBoolean(String key)
    {
        final SharedPreferences preferencias = Aplicacion.getAppContext().getSharedPreferences(
                Aplicacion.PREFERENCIAS, Context.MODE_PRIVATE);
        return preferencias.getBoolean(key, false);
    }

}
