package com.example.jcf.proyectowifi;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Created by jon on 14/11/2015.
 */
public class Preferencias extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferenciasFragment())
                .commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {

        if(key.equals("modo"))
        {
            Aplicacion.setModoDeOperacion(PreferenciasManager.ReadString(key));
        }
        else if(key.equals("muestras"))
        {
            try
            {
                Aplicacion.setCantidadDeMuestras(Integer.valueOf(PreferenciasManager
                        .ReadString("muestras")));
            }
            catch (NumberFormatException e)
            {
                Aplicacion.setCantidadDeMuestras(5);
            }
        }

        if(key.equals("txdata"))
        {
            Aplicacion.setTxData(PreferenciasManager.ReadBoolean("txdata"));
        }

        if(key.equals("ip"))
        {
            Aplicacion.setIp(PreferenciasManager.ReadString("ip"));
        }

        if(key.equals("puerto"))
        {
            try
            {
                Aplicacion.setPuerto(Integer.valueOf(PreferenciasManager
                        .ReadString("puerto")));
            }
            catch (NumberFormatException e)
            {
                Aplicacion.setPuerto(7777);
            }
        }
    }
}
