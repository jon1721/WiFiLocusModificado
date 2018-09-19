package com.example.jcf.proyectowifi;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Vector;

/**
 * Created by jon on 03/11/2015.
 */
public class DatosSQLite extends SQLiteOpenHelper
{
    private static final String TAG = "JCDBG";
    private final int potenciaMinima = -150;
    private final int umbralDeDeteccion = 50;

    public DatosSQLite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String sql_stat;

        sql_stat = "DROP TABLE IF EXISTS mapas";
        db.execSQL(sql_stat);
        sql_stat = "DROP TABLE IF EXISTS puntos";
        db.execSQL(sql_stat);
        sql_stat = "DROP TABLE IF EXISTS potencias";
        db.execSQL(sql_stat);

        sql_stat = "CREATE TABLE IF NOT EXISTS mapas (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uri_mapa TEXT)";
        db.execSQL(sql_stat);

        sql_stat = "CREATE TABLE IF NOT EXISTS puntos (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_mapa INTEGER, " +
                "coord_x REAL, " +
                "coord_y REAL, " +
                "FOREIGN KEY(id_mapa) REFERENCES mapas(_id))";
        db.execSQL(sql_stat);

        sql_stat = "CREATE TABLE IF NOT EXISTS potencias (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_punto INTEGER, " +
                "potencia INTEGER, " +
                "mac TEXT, " +
                "ssid TEXT, " +
                "FOREIGN KEY(id_punto) REFERENCES puntos(_id))";
        db.execSQL(sql_stat);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

    public void borrarTodosLosRegistrosDeLaBase()
    {
        boolean result = false;
        int cantidad_borrados;

        SQLiteDatabase db = getWritableDatabase();

        cantidad_borrados = db.delete("potencias", "", null);
        cantidad_borrados = db.delete("puntos", "", null);
        cantidad_borrados = db.delete("mapas", "", null);
    }

    public boolean insertarMapa(String uri_mapa)
    {
        long rowID;
        boolean ret = true;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues insertValues = new ContentValues();
        insertValues.put("uri_mapa", uri_mapa);
        rowID = db.insert("mapas", null, insertValues);
        if(rowID < 0)
        {
            ret = false;
        }

        return ret;
    }

    public long insertarPuntoyPotencias(PuntoMedido puntoMedido)
    {
        long ret = -1L;
        long rowId = -1L;
        int i;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues insertValues = new ContentValues();
        insertValues.put("id_mapa", puntoMedido.getId_mapa());
        insertValues.put("coord_x", puntoMedido.getX());
        insertValues.put("coord_y", puntoMedido.getY());
        rowId = db.insert("puntos", null, insertValues);
        ret = rowId;
        if(rowId < 0)
        {
            Log.d(TAG, "DatosSQLite insertarPuntoyPotencias: error al insertar punto");

        }
        else
        {
            for(i=0; i<Aplicacion.getNivelesAlmacenarSize(); i++)
            {
                insertValues.clear();
                insertValues.put("id_punto", rowId);
                insertValues.put("potencia", Aplicacion.getNivelesAlmacenarElement(i));
                insertValues.put("mac", Aplicacion.getMacsAlmacenarElement(i));
                insertValues.put("ssid", Aplicacion.getRoutersAlmacenarElement(i));

                if(db.insert("potencias", null, insertValues)<0)
                {
                    Log.d(TAG, "DatosSQLite insertarPuntoyPotencias: error al insertar potencia");
                    //ret = rowId;
                }
            }
        }

        return ret;
    }

    public long getIdMapa(String uri_mapa)
    {
        long id = -1;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id FROM mapas " +
                "WHERE uri_mapa = '" + uri_mapa + "' LIMIT 1", null);
        if(cursor != null)
        {
            if (cursor.getCount() == 1)
            {
                cursor.moveToNext();
                //result = cursor.getString(0);
                id = cursor.getLong(0);
                cursor.close();
                Log.d(TAG, "DatosSQLite Mapa id: " + id);
            }
        }

        return id;
    }

    public int contarMacOccurencias(String mac)
    {
        int ret = 0;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT mac FROM potencias " +
                "WHERE mac = '" + mac + "'", null);

        if(cursor != null)
        {
            ret =  cursor.getCount();
            cursor.close();
        }
        return ret;
    }

    public void ObtenerSubsetRouters(Vector<String> macs, Vector<Integer> niveles)
    {
        if(Aplicacion.isPlantaVisible() && (Aplicacion.getModoDeOperacion() != Aplicacion.MODO_ENTRENAMIENTO))
        {
            int i, j;
            String orMacs = "";

            if(Aplicacion.isAdquiriendoPunto() == false) {
                SQLiteDatabase db = getWritableDatabase();

                Log.i(TAG, "WiFiScanReceiver ObtenerSubsetRouters Planta visible");
                Medicion m = new Medicion(macs, niveles, db);
                CalcularPosicion cp = new CalcularPosicion();
                cp.execute(m);
            }
        }
        else
        {
            Log.i(TAG, "WiFiScanReceiver ObtenerSubsetRouters Planta NO visible");
            Aplicacion.Escanear();
        }
    }

    public void ObtenerPuntosMedidos(Vector<PuntoMedido> pmv)
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursorPuntos = db.rawQuery("SELECT id_mapa, coord_x, coord_y FROM puntos", null);
        int countPuntos = cursorPuntos.getCount();

        for(int i=0; i<countPuntos; i++)
        {
            cursorPuntos.moveToNext();
            PuntoMedido pm = new PuntoMedido(
                    cursorPuntos.getFloat(cursorPuntos.getColumnIndex("coord_x")),
                    cursorPuntos.getFloat(cursorPuntos.getColumnIndex("coord_y")),
                    cursorPuntos.getInt(cursorPuntos.getColumnIndex("id_mapa")));
            pmv.add(pm);
        }
    }
}
