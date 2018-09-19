package com.example.jcf.proyectowifi;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by jon on 12/11/2015.
 */
public class CanalAsync extends AsyncTask<String, Void, Void>
{
    private static final String TAG = "JCDBG";
    private static final int TENARIS_SOCKET_TIMEOUT = 1000;
    private String ip;
    private int puerto;
    private Socket s;
    private OutputStreamWriter ow;
    private Exception exception;
    private String msg;

    @Override
    protected Void doInBackground(String... params)
    {
        boolean ret = true;

        msg = params[0];
        ret = SendData (msg);
        return null;
    }

    private Boolean SendData (String msg)
    {
        //Boolean ret = false;
        boolean txOK = false;

        //Log.d(TAG, " intenta SendData");

        try
        {
            //ip = "192.168.0.6";
            //puerto = 7777;
            ip = PreferenciasManager.ReadString("ip");
            try
            {
                puerto = Integer.valueOf(PreferenciasManager
                        .ReadString("puerto"));
            }
            catch (NumberFormatException e)
            {
                puerto = 7777;
            }


            txOK = AbrirSocket(ip, puerto);

            if(txOK == true)
            {
                try
                {

                    txOK = Tx(ow, msg, "CanalTenaris Socket");
                }
                catch (Exception e)
                {
                    txOK = false;
                    //Log.e(TAG, " falló new OutputStreamWriter");
                }
            }
            else
            {
                // socket == null
                txOK = false;
                //Log.e(TAG, " AbrirSocket");
            }
        }
        catch(Exception e)
        {

            Log.d(TAG, "CanalTenaris Rx: " + e.toString());
            ow = null;
            s = null;
        }

        CerrarSocket(s);

        return txOK;
    }


    private boolean AbrirSocket(String dstName, int dstPort)
    {
        boolean ret = false;

        try
        {
            s = new Socket(dstName, dstPort); // nuevo socket
            s.setSoTimeout(TENARIS_SOCKET_TIMEOUT);

            try
            {
                ow = new OutputStreamWriter(s.getOutputStream());
                //Log.e(TAG, " AbrirSocket: " + dstName + ":" + dstPort);
                ret = true;
            }
            catch (Exception e)
            {
                s = null;
                ow = null;
                ret = false;
                //Log.e(TAG, " falló new OutputStreamWriter");
            }

            if((s !=null )&& (ow != null))
            {
                //Log.e(TAG, " socket nuevo");
            }
        }
        catch (Exception e)
        {
            s = null;
            ow = null;
            ret = false;
            //Log.d(TAG, " error en la creación del socket: " +  e.toString());
        }

        return ret;
    }

    private boolean EvaluarLectura(String respuesta)
    {
        boolean ret = true;
        try
        {
            if(respuesta == null)
            {
                //Log.d(TAG, " EvaluarLectura: rx null");
                ret = false;
            }
            else
            {
                if(respuesta.substring(0, 2).equals("@@") == false)
                {
                    ret = false;
                }

                int largo;
                int largoInformado;

                largo = respuesta.substring(7).length();
                largo += 2; // \r\n
                largoInformado = Integer.parseInt(respuesta.substring(3, 7));

                if(largo != largoInformado)
                {
                    ret = false;
                }
                else
                {
                    byte crc = 0;
                    int i, fin;
                    int j = 0;

                    fin = respuesta.indexOf('*');

                    if(fin > 0)
                    {
                        for(i=0; i<=fin; i++)
                        {
                            crc += respuesta.charAt(i);
                            j = i;
                        }

                        byte crcInformado = (byte) ((Character.digit(respuesta.charAt(j+1), 16) << 4)
                                + Character.digit(respuesta.charAt(j+2), 16));

                        if(crc != crcInformado)
                        {
                            ret = false;
                        }
                    }
                    else
                    {
                        ret = false;
                    }
                }
            }
        }
        catch (NumberFormatException e)
        {
            //Log.d(TAG, " EvaluarLectura: " + e.toString());
            ret = false;
        }


        if(ret == true)
        {
            //Log.d(TAG, " EvaluarLectura: OK");
        }
        else
        {
            //Log.d(TAG, " EvaluarLectura: MAL");
        }

        return ret;
    }

    private boolean Tx(OutputStreamWriter osw, String msg, String dbg)
    {
        boolean txOk = false;

        try
        {
            osw.write(msg.toCharArray(), 0, msg.length());
            osw.flush();

            //Log.d(TAG, "Tx() " + dbg + ": " + msg);
            txOk = true;
        }
        catch (Exception osw_ex)
        {
            txOk = false;
            //Log.d(TAG, "Tx(): " + dbg + osw_ex.toString());
            try
            {
                if(!s.isClosed())
                {
                    ow.close();
                    s.close();
                }
            }
            catch (Exception e)
            {
                //Log.d(TAG, "Tx(): " + dbg + e.toString());
            }

            ow = null;
            s = null;
        }

        return txOk;
    }


    private void CerrarSocket(Socket s)
    {
        if(s!=null)
        {
            if(s.isClosed() == false)
            {
                try
                {
                    s.close();
                }
                catch (IOException e)
                {
                    //Log.e(TAG, " Falló CerrarSocket: " + e.toString());
                }
            }
        }
    }
}
