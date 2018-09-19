package com.example.jcf.proyectowifi;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class MainActivity extends AppCompatActivity
{
    private int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 123;
    private static final String TAG = "JCDBGDB";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Aplicacion.setEscaneos((TextView) findViewById(R.id.escaneos));

        Button botonPlano = (Button) findViewById(R.id.plano);
        botonPlano.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                lanzarPlano();
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

        }else{
            //getScanningResults();
            //do something, permission was previously granted; or legacy device
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            //mWifiListener.getScanningResults();
        }
    }

    @Override
    protected void onPause()
    {
        unregisterReceiver(Aplicacion.getWifiReceiver());
        Aplicacion.TTSstop();
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Aplicacion.setEscaneos((TextView) findViewById(R.id.escaneos));
        Button botonPlano = (Button) findViewById(R.id.plano);
        botonPlano.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                lanzarPlano();
            }
        });
        RegistrarReceptorWiFi();
        Aplicacion.Escanear();

        String modo = Aplicacion.getModoDeOperacion();
        String appname = getResources().getString(R.string.app_name);

        if(modo.equals(Aplicacion.MODO_ENTRENAMIENTO))
        {
            this.setTitle(appname + ": entrenamiento");
        }
        else if(modo.equals(Aplicacion.MODO_USO))
        {
            this.setTitle(appname);
        }
        else
        {
            this.setTitle("WiFi Locus");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent i = new Intent(this, Preferencias.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_borrarDB)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Confirma borrar entrenamiento?").setPositiveButton("Si", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
            return true;
        }

        if (id == R.id.action_copiarDB) {
            copyDbToExternal(this);
        }

        return super.onOptionsItemSelected(item);
    }

    private void copyDbToExternal(Context context) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + context.getApplicationContext().getPackageName() + "//databases//" + Aplicacion.WF_DATABASE;
                String backupDBPath = Aplicacion.WF_DATABASE;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            } else {
                Log.d(TAG, "No se puedo escribir la SD");
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception de escritura en SD: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void lanzarPlano()
    {
        Intent i = new Intent(this, Planta.class);
        startActivity(i);
    }

    @Override
    protected void onDestroy()
    {
        Aplicacion.TTSshutdown();
        super.onDestroy();
    }

    private void RegistrarReceptorWiFi()
    {
        WiFiScanReceiver wifiReceiver = Aplicacion.getWifiReceiver();

        if (wifiReceiver != null)
        {
            registerReceiver(wifiReceiver,
                    new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            WifiManager wifiMng = (WifiManager) Aplicacion.contexto.getSystemService(this.WIFI_SERVICE);
            wifiMng.setWifiEnabled(true);
            Aplicacion.NuevoScan();
        }

    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    DatosSQLite base = new DatosSQLite(Aplicacion.getAppContext(),
                            Aplicacion.WF_DATABASE, null, Aplicacion.WF_DATABASE_VERSION);
                    base.borrarTodosLosRegistrosDeLaBase();
                    Vector<PuntoMedido> pmV = Aplicacion.getPuntoMedidoVector();
                    pmV.clear();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };
}