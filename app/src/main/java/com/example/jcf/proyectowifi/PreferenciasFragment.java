package com.example.jcf.proyectowifi;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by jon on 14/11/2015.
 */
public class PreferenciasFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
