package com.example.jcf.proyectowifi;

import android.database.sqlite.SQLiteDatabase;

import java.util.Vector;

/**
 * Created by jon on 14/11/2015.
 */
public class Medicion
{
    private static final String TAG = "JCDBG";

    public Vector<String> macs;
    public Vector<Integer> niveles;
    SQLiteDatabase db;

    public Medicion(Vector<String> macs, Vector<Integer> niveles, SQLiteDatabase db)
    {
        this.macs = macs;
        this.niveles = niveles;
        this.db = db;
    }
}
