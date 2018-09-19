package com.example.jcf.proyectowifi;

/**
 * Created by jon on 12/11/2015.
 */
public class FrameMaker
{
    public static String Trama(String payload)
    {
        String tramaToSend = "";
        String pre_trama = null;
        String pos_trama = null;

        pre_trama = "$$";
        pos_trama = payload;

        pre_trama += String.format("%04d", pos_trama.length()+2);

        tramaToSend = pre_trama + pos_trama;
        tramaToSend += "\r\n";

        return tramaToSend;
    }
}
