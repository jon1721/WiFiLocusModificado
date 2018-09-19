package com.example.jcf.proyectowifi;

/**
 * Created by jon on 02/11/2015.
 */
public class PuntoMedido
{
    private float x; // coordenada x en la imagen del plano
    private float y; // coordenada y en la imagen del plano
    long id_mapa;

    public PuntoMedido(float x, float y, long id_mapa)
    {
        this.x = x;
        this.y = y;
        this.id_mapa = id_mapa;
    }

    public float getX()
    {
        return x;
    }

    public void setX(float x)
    {
        this.x = x;
    }

    public float getY()
    {
        return y;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    public long getId_mapa()
    {
        return id_mapa;
    }
}
