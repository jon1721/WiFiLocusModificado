package com.example.jcf.proyectowifi;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Created by jon on 07/10/2015.
 */
public class Aplicacion extends Application implements TextToSpeech.OnInitListener
{
    private static final String TAG = "JCDBG";
    public  static final String PREFERENCIAS = "com.example.jcf.proyectowifi_preferences";
    public  static final String MODO_ENTRENAMIENTO = "0";
    public  static final String MODO_USO = "1";

    private static Vector<String> Macs;
    private static Vector<String> Routers;
    private static Vector<Integer> Niveles;
    private static Vector<String> MacsAlmacenar;
    private static Vector<Integer> NivelesAlmacenar;
    private static Vector<String> RoutersAlmacenar;
    private static WifiManager wifiMng;
    private static WiFiScanReceiver wifiReceiver;
    private static Vector<PuntoMedido> PuntoMedidoVector;
    private static boolean horizontal;
    public final static String WF_DATABASE = "Puntoswifi.db";
    public final static int WF_DATABASE_VERSION = 1;
    private static boolean ScanDisponible = false;
    private static boolean AdquiriendoPunto = false;
    private static DatosSQLite base;
    private static TextView escaneos = null;
    public static Context contexto;
    private static int conmutador = 0;
    private static Toast toast = null;
    private static long ahora = 0;
    private static long antes = 0;
    private static TextToSpeech tts;
    private static Aplicacion instancia;
    private static int CantidadDeMuestras = 5;
    private static String ModoDeOperacion = MODO_ENTRENAMIENTO;
    private static boolean TxData = false;
    private static boolean PlantaVisible = false;
    private static String Ip;
    private static int Puerto;
    private static long rowId = -1;
    private static boolean obteniendoRouters = false;
    private static MiImageView planta;

    @Override
    public void onCreate()
    {
        super.onCreate();
        contexto = getApplicationContext();
        instancia = this;
        Macs = new Vector<String>();
        Routers = new Vector<String>();
        Niveles = new Vector<Integer>();
        MacsAlmacenar = new Vector<String>();
        NivelesAlmacenar = new Vector<Integer>();
        PuntoMedidoVector = new Vector<PuntoMedido>();
        RoutersAlmacenar = new Vector<String>();


        WiFiInit();
        base = new DatosSQLite(this, Aplicacion.WF_DATABASE, null, Aplicacion.WF_DATABASE_VERSION);

        if(PreferenciasManager.ReadBoolean("borrarSqlite") == true)
        {
            base.borrarTodosLosRegistrosDeLaBase();
            base.insertarMapa("zonas.png");
        }
        else
        {
            PuntoMedidoVector.clear();
            base.ObtenerPuntosMedidos(PuntoMedidoVector);
        }
        TTSInit();
        //Simulacion();

        ModoDeOperacion = PreferenciasManager.ReadString("modo");
        if(ModoDeOperacion.equals(""))
        {
            PreferenciasManager.WriteString("modo", MODO_ENTRENAMIENTO);
            ModoDeOperacion = MODO_ENTRENAMIENTO;
        }

        try
        {
            CantidadDeMuestras = Integer.valueOf(PreferenciasManager.ReadString("muestras"));
        }
        catch (NumberFormatException e)
        {
            PreferenciasManager.WriteString("muestras", "5");
            CantidadDeMuestras = 5;
        }


        Ip = PreferenciasManager.ReadString("ip");
        try
        {
            Puerto = Integer.valueOf(PreferenciasManager
                    .ReadString("puerto"));
        }
        catch (NumberFormatException e)
        {
            Puerto = 7777;
        }

        TxData = PreferenciasManager.ReadBoolean("txdata");

    }

    public static void TTSInit()
    {
        tts = new TextToSpeech(instancia, instancia);
    }
    private void Simulacion()
    {
        PuntoMedido pm;
        long id_mapa = base.getIdMapa("zonas.png");


        PuntoMedidoVector.clear();
        Macs.clear();
        Niveles.clear();
        Macs.add("R1");
        Macs.add("R2");
        Macs.add("R3");
        Macs.add("R4");

        // 0
        pm = new PuntoMedido(0, 0, id_mapa);
        Niveles.clear();
        Niveles.add(-32);	Niveles.add(-52);	Niveles.add(-97);	Niveles.add(-88);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 1
        Niveles.clear();
        pm = new PuntoMedido(0, 350, id_mapa);
        Niveles.add(-14);	Niveles.add(-43);	Niveles.add(-72);	Niveles.add(-60);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 2
        Niveles.clear();
        pm = new PuntoMedido(0, 700, id_mapa);
        Niveles.add(-32); Niveles.add(-52);	Niveles.add(-52);	Niveles.add(-32);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 3
        Niveles.clear();
        pm = new PuntoMedido(0, 1050, id_mapa);
        Niveles.add(-60); Niveles.add(-72);	Niveles.add(-43);	Niveles.add(-14);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 4
        Niveles.clear();
        pm = new PuntoMedido(0, 1400, id_mapa);
        Niveles.add(-88); Niveles.add(-97);	Niveles.add(-52);	Niveles.add(-32);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 5
        Niveles.clear();
        pm = new PuntoMedido(175, 1400, id_mapa);
        Niveles.add(-87); Niveles.add(-92);	Niveles.add(-41); Niveles.add(-29);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 6
        Niveles.clear();
        pm = new PuntoMedido(350, 1400, id_mapa);
        Niveles.add(-88); Niveles.add(-88);	Niveles.add(-32); Niveles.add(-32);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 7
        Niveles.clear();
        pm = new PuntoMedido(525, 1400, id_mapa);
        Niveles.add(-92); Niveles.add(-87);	Niveles.add(-29); Niveles.add(-41);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 8
        Niveles.clear();
        pm = new PuntoMedido(700, 1400, id_mapa);
        Niveles.add(-97); Niveles.add(-88);	Niveles.add(-32);	Niveles.add(-52);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 9
        Niveles.clear();
        pm = new PuntoMedido(700, 1050, id_mapa);
        Niveles.add(-72);
        Niveles.add(-60);	Niveles.add(-14);	Niveles.add(-43);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 10
        Niveles.clear();
        pm = new PuntoMedido(700, 700, id_mapa);
        Niveles.add(-52);
        Niveles.add(-32);	Niveles.add(-32);	Niveles.add(-52);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 11
        Niveles.clear();
        pm = new PuntoMedido(700, 350, id_mapa);
        Niveles.add(-43);
        Niveles.add(-14);	Niveles.add(-60);	Niveles.add(-72);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 12
        Niveles.clear();
        pm = new PuntoMedido(700, 0, id_mapa);
        Niveles.add(-52);
        Niveles.add(-32);	Niveles.add(-88);	Niveles.add(-97);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 13
        Niveles.clear();
        pm = new PuntoMedido(525, 0, id_mapa);
        Niveles.add(-41);
        Niveles.add(-29);	Niveles.add(-87);	Niveles.add(-92);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 14
        Niveles.clear();
        pm = new PuntoMedido(350, 0, id_mapa);
        Niveles.add(-32);
        Niveles.add(-32);	Niveles.add(-88);	Niveles.add(-88);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);

        // 15
        Niveles.clear();
        pm = new PuntoMedido(175, 0, id_mapa);
        Niveles.add(-29);
        Niveles.add(-41);   Niveles.add(-92);	Niveles.add(-87);
        base.insertarPuntoyPotencias(pm);
        PuntoMedidoVector.add(pm);
    }

    private static void SimulPos(int caso)
    {
        Macs.clear();
        Niveles.clear();
        Macs.add("R1");
        Macs.add("R2");
        Macs.add("R3");
        Macs.add("R4");

        switch(caso)
        {
            case 0:
                Niveles.add(-32);	Niveles.add(-52);	Niveles.add(-97);	Niveles.add(-88);
            break;
            case 1:
                Niveles.add(-14); Niveles.add(-43);	Niveles.add(-72); Niveles.add(-60);
                break;
            case 2:
                Niveles.add(-32); Niveles.add(-52);	Niveles.add(-52); Niveles.add(-32);
                break;
            case 3:
                Niveles.add(-60); Niveles.add(-72);	Niveles.add(-43); Niveles.add(-14);
                break;
            case 4:
                Niveles.add(-88); Niveles.add(-97);	Niveles.add(-52); Niveles.add(-32);
                break;
            case 5:
                Niveles.add(-87); Niveles.add(-92);	Niveles.add(-41); Niveles.add(-29);
                break;
            case 6:
                Niveles.add(-88); Niveles.add(-88);	Niveles.add(-32); Niveles.add(-32);
                break;
            case 7:
                Niveles.add(-92); Niveles.add(-87);	Niveles.add(-29); Niveles.add(-41);
                break;
            case 8:
                Niveles.add(-97); Niveles.add(-88);	Niveles.add(-32);	Niveles.add(-52);
                break;
            case 9:
                Niveles.add(-72); Niveles.add(-60);	Niveles.add(-14); Niveles.add(-43);
                break;
            case 10:
                Niveles.add(-52); Niveles.add(-32); Niveles.add(-32); Niveles.add(-52);
                break;
            case 11:
                Niveles.add(-43); Niveles.add(-14);	Niveles.add(-60); Niveles.add(-72);
                break;
            case 12:
                Niveles.add(-52); Niveles.add(-32);	Niveles.add(-88); Niveles.add(-97);
                break;
            case 13:
                Niveles.add(-41); Niveles.add(-29);	Niveles.add(-87); Niveles.add(-92);
                break;
            case 14:
                Niveles.add(-32); Niveles.add(-32);	Niveles.add(-88); Niveles.add(-88);
                break;
            case 15:
                Niveles.add(-29); Niveles.add(-41); Niveles.add(-92); Niveles.add(-87);
                break;
        }

        Log.i(TAG, "Hallados niveles: " + Niveles.elementAt(0) + " " + Niveles.elementAt(1) + " "
                + Niveles.elementAt(2) + " " + Niveles.elementAt(3));
    }

    private void WiFiInit()
    {
        wifiMng = (WifiManager)getSystemService(this.WIFI_SERVICE);
        wifiMng.setWifiEnabled(true);
        wifiReceiver = new WiFiScanReceiver();
        Escanear();
    }

    public static void getRouters()
    {
        List<ScanResult> wifiScanList = wifiMng.getScanResults();
        ScanResult scanres;
        String macAdd = "";
        String ssid = "";
        int nivel = -1000000;
        String dbg = "";

        ScanDisponible = false;
        //RemoveRouters();

        Macs.clear();
        Niveles.clear();
        Routers.clear();

        if(Macs.size() > 0) {
            Log.d(TAG, "**** ERROR GetRouters: Macs.size() > 0: " + Macs.size());
        }

        if(Niveles.size() > 0) {
            Log.d(TAG, "**** ERROR GetRouters: Niveles.size() > 0: " + Niveles.size());
        }

        if(Routers.size() > 0) {
            Log.d(TAG, "**** ERROR GetRouters: Routers.size() > 0: " + Routers.size());
        }

        for(int i = 0; i < wifiScanList.size(); i++)
        {
            scanres = wifiScanList.get(i);
            macAdd = scanres.BSSID;
            nivel = scanres.level;
            ssid = scanres.SSID;
            Macs.add(macAdd);
            Niveles.add(nivel);
            Routers.add(ssid);
            Log.d(TAG, "GetRouters: " + macAdd + " " + nivel);
        }

        Vector<String> MacsFiltradas = new Vector<String>();
        Vector<Integer> NivelesFiltrados = new Vector<Integer>();

        int cantidad = Niveles.size();

        for(int i=0; i < cantidad; i++)
        {
            for(int j=i+1; j < cantidad; j++)
            {
                if(Niveles.elementAt(i).compareTo(Niveles.elementAt(j)) < 0)
                {
                    Collections.swap(Niveles, i, j);
                    Collections.swap(Macs, i, j);
                    Collections.swap(Routers, i, j);
                }
            }
        }

        NivelesAlmacenar.clear();
        MacsAlmacenar.clear();
        RoutersAlmacenar.clear();

        for (int i = 0; i < Niveles.size(); i++)
        {
            NivelesAlmacenar.add(Niveles.elementAt(i));
            MacsAlmacenar.add(Macs.elementAt(i));
            RoutersAlmacenar.add(Routers.elementAt(i));
        }

        dbg = "Nivel\t\t\tMAC\t\t\t\t\t\t\t\t\t\t\t\tSSID\n\n";
        for(int i=0; i < Niveles.size(); i++)
        {
            //Log.d(TAG, "GetRouters: " + Macs.elementAt(i) + " " + Niveles.elementAt(i));
            dbg += Niveles.elementAt(i) + "\t\t" + Macs.elementAt(i) + "\t\t\t\t\t\t" + Routers.elementAt(i) + "\n";
        }
        escaneos.setText(dbg);


        if(TxData == true)
        {
            dbg = "|";
            int pow;
            for (int i = 0; i < Niveles.size(); i++)
            {
                pow = Niveles.elementAt(i);
                if (pow > -100)
                {
                    pow += 100;
                    dbg += Macs.elementAt(i) + "," + pow + "|";
                    //dbg += Routers.elementAt(i) + "," + pow + "|";
                }
            }

            Comunicaciones com = new Comunicaciones(FrameMaker.Trama(dbg));
            com.start();
        }




        if(isAdquiriendoPunto() == false)
        {
            Log.i(TAG, "WiFiScanReceiver AdquiriendoPunto = FALSE");
            if(Aplicacion.isPlantaVisible())
            {
                Log.i(TAG, "WiFiScanReceiver Calculando Nueva posición recibida");
                base.ObtenerSubsetRouters((Vector<String>) Macs.clone(),
                        (Vector<Integer>) Niveles.clone());
            }
            else
            {
                Log.i(TAG, "WiFiScanReceiver Planta Invisible");
                Escanear();
            }
        }
        else
        {
            Log.i(TAG, "WiFiScanReceiver AdquiriendoPunto = TRUE");
        }

        Aplicacion.setObteniendoRouters(false);
        Aplicacion.setScanDisponible(true);
    }


    public static void Escanear()
    {
        if(isAdquiriendoPunto() == false)
        {
            if(wifiMng.startScan() == true) {
                Log.i(TAG, "WiFiScanReceiver: Escanear");
            } else {
                Log.i(TAG, "WiFiScanReceiver: falló startScan");
            }
        }
    }

    public static void NuevoScan()
    {
        if(Aplicacion.isObteniendoRouters() == false) {
            Aplicacion.setObteniendoRouters(true);
            wifiMng.startScan();
            Log.i(TAG, "startScan: NuevoScan");
        }
    }


    public static WiFiScanReceiver getWifiReceiver()
    {
        return wifiReceiver;
    }

    public static String getMacsAlmacenarElement(int pos)
    {
        return MacsAlmacenar.elementAt(pos);
    }

    public static Vector<String> getMacsAlmacenar()
    {
        return MacsAlmacenar;
    }

    public static Vector<String> getRoutersAlmacenar()
    {
        return RoutersAlmacenar;
    }

    public static String getRoutersAlmacenarElement(int pos)
    {
        return RoutersAlmacenar.elementAt(pos);
    }

    public static Vector<Integer> getNivelesAlmacenar()
    {
        return NivelesAlmacenar;
    }

    public static int getNivelesAlmacenarElement(int pos)
    {
        return NivelesAlmacenar.elementAt(pos);
    }

    public static int getNivelesAlmacenarSize()
    {
        return NivelesAlmacenar.size();
    }

    public static void RemoveRouters()
    {
        Macs.clear();
        Niveles.clear();
        Routers.clear();
    }

    public static Vector<PuntoMedido> getPuntoMedidoVector()
    {
        return PuntoMedidoVector;
    }

    public static boolean isHorizontal()
    {
        return horizontal;
    }

    public static void setHorizontal(boolean horizontal)
    {
        Aplicacion.horizontal = horizontal;
    }

    public static boolean isScanDisponible()
    {
        return ScanDisponible;
    }

    public static void setScanDisponible(boolean scanDisponible)
    {
        ScanDisponible = scanDisponible;
    }

    public static void setEscaneos(TextView escaneos)
    {
        Aplicacion.escaneos = escaneos;
    }

    public static void Cartelito(String str)
    {
        Date d = new Date();
        ahora = d.getTime();

        if(ahora > (antes+900))
        {
            antes = ahora;

            if (toast != null)
            {
                toast.setText(str);
            }
            else
            {
                toast = Toast.makeText(Aplicacion.contexto, str, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
            }
            toast.show();
        }
    }

    public static boolean isAdquiriendoPunto()
    {
        return AdquiriendoPunto;
    }

    public static void setAdquiriendoPunto(boolean adquiriendoPunto)
    {
        AdquiriendoPunto = adquiriendoPunto;
    }

    @Override
    public void onInit(int status)
    {
        if (status == TextToSpeech.SUCCESS)
        {

            Locale loc = new Locale("es_AR");

            tts.setOnUtteranceProgressListener(new VozReceiver(this));
            tts.setSpeechRate((float) 1.0);
            tts.setPitch((float) 1.0);
            int result = tts.setLanguage(loc);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                //Log.e(TAG, "Idioma no soportado");
            }
            else
            {
                //Log.i(TAG, "Idioma OK");
            }
        }
        else
        {
            //Log.e(TAG, "Falló la inicialización de TTS!");
        }
    }

    public static void Di(String txt)
    {
        if(tts != null)
        {
            //if (tts.isSpeaking()) tts.stop();

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
            tts.speak(txt, TextToSpeech.QUEUE_FLUSH, map);
        }
        else
        {
            TTSInit();
        }
    }

    public static void TTSstop()
    {
        if(tts != null)
        {
            tts.stop();
        }
    }
    public static void TTSshutdown()
    {
        if(tts != null)
        {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    public static Context getAppContext()
    {
        return contexto;
    }

    public static int getCantidadDeMuestras()
    {
        return CantidadDeMuestras;
    }

    public static void setCantidadDeMuestras(int cantidadDeMuestras)
    {
        CantidadDeMuestras = cantidadDeMuestras;
    }

    public static String getModoDeOperacion()
    {
        return ModoDeOperacion;
    }

    public static void setModoDeOperacion(String modoDeOperacion)
    {
        ModoDeOperacion = modoDeOperacion;
    }

    public static void setTxData(boolean txData)
    {
        TxData = txData;
    }

    public static boolean isPlantaVisible()
    {
        return PlantaVisible;
    }

    public static void setPlantaVisible(boolean plantaVisible)
    {
        PlantaVisible = plantaVisible;
        if(PlantaVisible)
        {
            Log.i(TAG, "WiFiScanReceiver PlantaVisible");
        }
        else
        {
            Log.i(TAG, "WiFiScanReceiver Planta NO Visible");
        }
    }

    public static String getIp()
    {
        return Ip;
    }

    public static int getPuerto()
    {
        return Puerto;
    }

    public static void setIp(String ip)
    {
        Ip = ip;
    }

    public static void setPuerto(int puerto)
    {
        Puerto = puerto;
    }

    public static long getRowId() {
        return rowId;
    }

    public static void setRowId(long rowId) {
        Aplicacion.rowId = rowId;
    }

    public static boolean isObteniendoRouters() {
        return obteniendoRouters;
    }

    public static void setObteniendoRouters(boolean obteniendoRouters) {
        Aplicacion.obteniendoRouters = obteniendoRouters;
    }

    public static void setPlanta(MiImageView planta) {
        Aplicacion.planta = planta;
    }

    public static void redibujar() {
        Aplicacion.planta.invalidate();
    }
}
