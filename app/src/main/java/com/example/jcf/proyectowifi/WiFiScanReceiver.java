package com.example.jcf.proyectowifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jon on 07/10/2015.
 */
public class WiFiScanReceiver extends BroadcastReceiver
{
    private static final String TAG = "JCDBG";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "WiFiScanReceiver onReceive");
        Aplicacion.getRouters();
        Aplicacion.setObteniendoRouters(false);
    }
}
