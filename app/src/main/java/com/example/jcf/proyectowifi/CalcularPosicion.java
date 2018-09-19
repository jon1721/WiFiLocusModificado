package com.example.jcf.proyectowifi;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by jon on 14/11/2015.
 */
public class CalcularPosicion extends AsyncTask<Medicion, Void, Void>
{
    private static final String TAG = "JCDBG";
    private final int umbralDeDeteccion = 50;

    private Vector<String> macs;
    private Vector<Integer> niveles;
    private SQLiteDatabase db;
    private Medicion m;

    @Override
    protected Void doInBackground(Medicion... params)
    {
        int i, j, k;
        String orMacs = "";
        Vector<Float> distancia = new Vector<Float>();
        Vector<Float> espejo = new Vector<Float>();
        int idPuntoCercano = -1;
        int id_punto;
        int id_puntoEntrenamiento;
        int terminos;
        String mac;
        int indice;
        int countRegsCalculo;
        int countPuntos = 0;
        int diferencia2;
        float tempdist = 0;
        //float distanciaMinima[] = new float[3];
        Vector<Integer> indiceDistanciaMinima = new Vector<Integer>();
        float distProgresiva;
        float calc = 0;
        Cursor cursorPosicion;
        Vector<PuntoMedido> PuntoDistanciaVector = new Vector<PuntoMedido>();
        PuntoMedido pm;

        Log.i(TAG, "WiFiScanReceiver doInBackground");


        m = params[0];
        macs = m.macs;
        db = m.db;

        niveles = m.niveles;

        for(i=0; i<macs.size(); i++)
        {
            //macsCopia.add(macs.elementAt(i));
            if(i==0)
            {
                orMacs = "mac = '" + macs.elementAt(i) + "'";
            }
            else
            {
                orMacs += " OR mac = '" + macs.elementAt(i) + "'";
            }
        }

        Cursor cursorPuntos = db.rawQuery("SELECT DISTINCT id_punto FROM potencias " +
                "WHERE (" + orMacs + ")", null);

        if(cursorPuntos != null)
        {
            if (cursorPuntos.getCount() > 0)
            {
                Cursor cursorCalculo = db.rawQuery("SELECT _id, id_punto, potencia, mac FROM potencias " +
                        "WHERE (" + orMacs + ")", null);

                if ((cursorCalculo != null) && (cursorPuntos != null))
                {
                    countRegsCalculo = cursorCalculo.getCount();
                    countPuntos = cursorPuntos.getCount();

                    if (countPuntos == 0)
                    {
                        String str = "Nuevo scan disponible: " + macs.size() + " routers";
                        //Aplicacion.Cartelito(str);
                    }

                    distProgresiva = 0;
                    for (i = 0; i < countPuntos; i++)
                    {
                        distancia.add((float) 0);
                        cursorPuntos.moveToNext();
                        id_punto = cursorPuntos.getInt(cursorPuntos.getColumnIndex("id_punto"));
                        terminos = 0;

                        cursorCalculo.moveToFirst();
                        for (j = 0; j < countRegsCalculo; j++)
                        {
                            id_puntoEntrenamiento = cursorCalculo.getInt(cursorCalculo.getColumnIndex("id_punto"));

                            if (id_punto == id_puntoEntrenamiento)
                            {
                                mac = cursorCalculo.getString(cursorCalculo.getColumnIndex("mac"));

                                indice = macs.indexOf(mac);

                                if (indice >= 0)
                                {
                                    //macsCopia.setElementAt("procesado", indice);
                                    diferencia2 = (cursorCalculo.getInt(cursorCalculo.getColumnIndex("potencia"))
                                            - niveles.elementAt(indice));
                                    terminos++;
                                    //Log.i(TAG, "Nivel[" + indice + "]=" + niveles.elementAt(indice));

                                    diferencia2 *= diferencia2;
                                    tempdist = distancia.elementAt(i);
                                    tempdist += diferencia2;

                                    distancia.set(i, tempdist);
                                }
                            }

                            if (i != 0)
                            {
                                calc = tempdist / countRegsCalculo;
                                if (calc > distProgresiva)
                                {
                                    Log.i(TAG, "Posible acelerador: i=" + i + "/ " + countPuntos +
                                            " j=" + j + "/" + countRegsCalculo);
                                    distProgresiva = calc;
                                }
                            }
                            cursorCalculo.moveToNext();
                        }

                        tempdist = distancia.elementAt(i) / terminos;
                        //tempdist = distancia.elementAt(i);
                        distancia.set(i, tempdist);

                        if (i == 0)
                        {
                            distProgresiva = tempdist;
                        }
                    }
                }

                for (i = 0; i < distancia.size(); i++)
                {
                    espejo.add(distancia.elementAt(i));
                }

                Collections.sort(distancia);
                float float_i;
                float float_j;

                for (i=0, k=0; i < distancia.size(); i++)
                {
                    for(j=0; j<distancia.size(); j++)
                    {
                        if ((k < countPuntos) && (k < 10))
                        {
                            float_i = distancia.elementAt(i);
                            float_j = espejo.elementAt(j);

                            if (Float.compare(float_i, float_j) == 0)
                            {
                                indiceDistanciaMinima.add(j);
                                k++;
                            }
                        }
                        else
                        {
                            break;
                        }
                    }
                }

                if(indiceDistanciaMinima.size() < 1)
                {
                    Log.i(TAG, "Eeeepa!");
                }
                else
                {
                    for(i=0; i<indiceDistanciaMinima.size(); i++)
                    {
                        if (cursorPuntos.moveToPosition(indiceDistanciaMinima.elementAt(i)) == true)
                        {
                            id_punto = cursorPuntos.getInt(cursorPuntos.getColumnIndex("id_punto"));
                            cursorPosicion = db.rawQuery("SELECT _id, id_mapa, coord_x, coord_y FROM puntos " +
                                    "WHERE _id = " + id_punto, null);

                            if (cursorPosicion != null)
                            {
                                if (cursorPosicion.getCount() == 1)
                                {
                                    cursorPosicion.moveToFirst();

                                    int indiceCoord_x = cursorPosicion.getColumnIndex("coord_x");
                                    int indiceCoord_y = cursorPosicion.getColumnIndex("coord_y");
                                    int indiceIdMapa = cursorPosicion.getColumnIndex("id_mapa");

                                    Float coord_x = cursorPosicion.getFloat(indiceCoord_x);
                                    Float coord_y = cursorPosicion.getFloat(indiceCoord_y);
                                    long id_mapa = cursorPosicion.getLong(indiceIdMapa);
                                    pm = new PuntoMedido(coord_x, coord_y, id_mapa);
                                    PuntoDistanciaVector.add(pm);
                                }
                            }
                        }
                    }

                    Float bariPunto_x = (float)0.0;
                    Float bariPunto_y = (float)0.0;
                    Float distanciaPunto;
                    Float bariDistancia = (float)0.0;

                    for(i=0; i<PuntoDistanciaVector.size(); i++)
                    {
                        distanciaPunto = 1/(float)distancia.elementAt(i);
                        bariPunto_x += PuntoDistanciaVector.elementAt(i).getX()*distanciaPunto;
                        bariPunto_y += PuntoDistanciaVector.elementAt(i).getY()*distanciaPunto;
                        bariDistancia += distanciaPunto;
                    }

                    bariPunto_x /= bariDistancia;
                    bariPunto_y /= bariDistancia;

                    Intent intento = new Intent();
                    intento.setAction(Planta.ReceptorNuevaPosicion.ACTION_NUEVA_POSICION);
                    intento.addCategory(Intent.CATEGORY_DEFAULT);
                    intento.putExtra("coord_x", bariPunto_x);
                    intento.putExtra("coord_y", bariPunto_y);
                    Aplicacion.contexto.sendBroadcast(intento);
                    Log.i(TAG, "Enviando Nueva posición recibida");

                }

                if (cursorCalculo != null) cursorCalculo.close();
            }

            if (cursorPuntos != null) cursorPuntos.close();
        }
        return null;
    }

    @Override
    protected void onCancelled()
    {
        Aplicacion.Escanear();
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        Log.i(TAG, "WiFiScanReceiver onPostExecute");
        Aplicacion.Escanear();
        super.onPostExecute(aVoid);
    }
}
