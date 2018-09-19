package com.example.jcf.proyectowifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Vector;

/**
 * Created by jon on 12/11/2015.
 */
public class Planta extends Activity implements View.OnTouchListener
{
    private final String TAG = "JCDBG";
    //private VistaPlano vistaPlano;
    private MiImageView planta;
    private int ancho_vista;
    private int alto_vista;
    //private int ancho_layout;
    //private int alto_layout;
    private float x_almacenado;
    private float y_almacenado;
    private Vector<PuntoMedido> puntoMedidoVector;
    private DatosSQLite base = null;
    private ReceptorNuevaPosicion receptorNuevaPosicion;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planta);
        puntoMedidoVector = Aplicacion.getPuntoMedidoVector();
        //planta = (ImageView) findViewById(R.id.imagePlanta);
        planta = (MiImageView) findViewById(R.id.Miimageview);
        Aplicacion.setPlanta(planta);
        planta.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        planta.setAdjustViewBounds(true);
        planta.setOnTouchListener(this);
        base = new DatosSQLite(this, Aplicacion.WF_DATABASE, null, Aplicacion.WF_DATABASE_VERSION);

        receptorNuevaPosicion = new ReceptorNuevaPosicion();
        RegistrarReceptores();
        Aplicacion.Escanear();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Aplicacion.Escanear();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        ancho_vista = planta.getWidth();
        alto_vista = planta.getHeight();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        //super.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                if(Aplicacion.getModoDeOperacion().equals(Aplicacion.MODO_ENTRENAMIENTO))
                {
                    if (Aplicacion.isScanDisponible())
                    {
                        if (Aplicacion.isAdquiriendoPunto() == false)
                        {
                            Aplicacion.setAdquiriendoPunto(true);
                            x_almacenado = 1000 * x / ancho_vista;
                            y_almacenado = 1000 * y / alto_vista;
                            Log.i(TAG, "x=" + x + " y=" + y);
                            x = ancho_vista * x_almacenado / 1000;
                            y = alto_vista * y_almacenado / 1000;
                            Log.i(TAG, "rep x=" + x + " rep y=" + y);
                            long id_mapa = base.getIdMapa("zonas.png"); // trabajar en siguiente versión
                            PuntoMedido pm = new PuntoMedido(x_almacenado, y_almacenado, id_mapa);
                            puntoMedidoVector.add(pm);
                            planta.dibujarCirculo(x, y, 10);
                            AlmacenarPuntoyPotencias(pm);
                        }
                        else
                        {
                            Log.i(TAG, "Aplicacion.isAdquiriendoPunto() es TRUE");
                        }
                    }
                }

                break;
            default:
                break;
        }
        return true;
    }

    private void AlmacenarPuntoyPotencias(PuntoMedido puntoMedido)
    {
        if(Aplicacion.isObteniendoRouters() == false) {
            AdquirirPunto ap = new AdquirirPunto();
            ap.execute(puntoMedido);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Aplicacion.setPlantaVisible(true);
        RegistrarReceptores();
        Aplicacion.Escanear();
    }

    @Override
    protected void onPause()
    {
        Aplicacion.setPlantaVisible(false);
        desRegistrarReceptores();
        Aplicacion.TTSstop();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private void RegistrarReceptores()
    {
        WiFiScanReceiver wifiReceiver = Aplicacion.getWifiReceiver();
        if(wifiReceiver != null)
        {
            registerReceiver(wifiReceiver,
                    new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            //WifiManager wifiMng = (WifiManager)getSystemService(this.WIFI_SERVICE);
            WifiManager wifiMng = (WifiManager)Aplicacion.contexto.getSystemService(this.WIFI_SERVICE);

            wifiMng.setWifiEnabled(true);
            Aplicacion.NuevoScan();
            //wifiMng.startScan();
        }
        else
        {
            if(toast != null) toast.cancel();
            toast = Toast.makeText(this, "No pudo registarse WiFimanager", Toast.LENGTH_SHORT);
            toast.show();
        }


        if(receptorNuevaPosicion != null)
        {
            IntentFilter filtro = new IntentFilter(ReceptorNuevaPosicion.ACTION_NUEVA_POSICION);
            filtro.addCategory(Intent.CATEGORY_DEFAULT);
            registerReceiver(receptorNuevaPosicion, filtro);
        }
        else
        {
            if(toast != null) toast.cancel();
            toast = Toast.makeText(this, "No pudo registarse receptorNuevaPosicion", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    private void desRegistrarReceptores()
    {
        unregisterReceiver(Aplicacion.getWifiReceiver());
        unregisterReceiver(receptorNuevaPosicion);
    }



    public class ReceptorNuevaPosicion extends BroadcastReceiver
    {
        public static final String ACTION_NUEVA_POSICION =
                "com.example.jcf.proyectowifi.intentservice.intent.action.NUEVA_POSICION";

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Float coord_x = intent.getFloatExtra("coord_x", 0);
            Float coord_y = intent.getFloatExtra("coord_y", 0);
            planta.dibujarPosicion(coord_x, coord_y);
        }
    }

    public int getStatusBarHeight()
    {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
        {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
