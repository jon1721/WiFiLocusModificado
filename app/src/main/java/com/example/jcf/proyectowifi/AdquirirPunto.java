package com.example.jcf.proyectowifi;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by jon on 14/11/2015.
 */
public class AdquirirPunto extends AsyncTask <PuntoMedido, Long, Void>
{
    private static final String TAG = "JCDBG";
    private static final int SCAN_TIMEOUT = 10*10;
    PuntoMedido pm;
    private DatosSQLite base = null;
    long cantidadDeMuestras;

    @Override
    protected Void doInBackground(PuntoMedido... pMedido)
    {
        long i, j;
        Long rowId = -1L;

        Aplicacion.Di("Adquiriendo posición");
        pm = pMedido[0];
        Log.i(TAG, "AdquirirPunto: x=" + pm.getX() + " y=" + pm.getY());
        Aplicacion.setAdquiriendoPunto(true);
        cantidadDeMuestras = Aplicacion.getCantidadDeMuestras();
        base = new DatosSQLite(Aplicacion.contexto, Aplicacion.WF_DATABASE,
                null, Aplicacion.WF_DATABASE_VERSION);

        if(base != null)
        {
            for(i=0; i<cantidadDeMuestras; i++)
            {
                Log.i(TAG, "AdquirirPunto: muestra[" + i + "]");
                Aplicacion.setScanDisponible(false);
                Aplicacion.NuevoScan();
                for(j=0; j<SCAN_TIMEOUT && (Aplicacion.isScanDisponible() == false); j++)
                {
                    // wait;
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        //e.printStackTrace();
                    }
                }

                if(j>=SCAN_TIMEOUT)
                {
                    Log.i(TAG, "AdquirirPunto: TIMEOUT");
                }
                else {
                    rowId = base.insertarPuntoyPotencias(pm);
                    if(i == 0){
                        Aplicacion.setRowId(rowId);
                    }
                }
                this.publishProgress(i, rowId);
            }
        }

        Aplicacion.setAdquiriendoPunto(false);
        return null;
    }

    @Override
    protected void onProgressUpdate(Long... values)
    {
        super.onProgressUpdate(values);
        String progreso;

        //progreso = "" + (values[0]+1) + "/" + cantidadDeMuestras;
        progreso = "" + (values[0]+1) + "/" + cantidadDeMuestras;
        Aplicacion.Cartelito(progreso);
    }

    @Override
    protected void onCancelled()
    {
        Aplicacion.setAdquiriendoPunto(false);
        Aplicacion.setObteniendoRouters(false);
        Aplicacion.NuevoScan();
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);
        base.close();
        Aplicacion.setAdquiriendoPunto(false);
        Aplicacion.setObteniendoRouters(false);
        Aplicacion.NuevoScan();
        Log.i(TAG, "AdquirirPunto: onPostExecute");
        Aplicacion.Di("Adquisición finalizada");
        Aplicacion.redibujar();
    }
}
