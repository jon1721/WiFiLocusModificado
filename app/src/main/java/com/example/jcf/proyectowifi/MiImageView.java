package com.example.jcf.proyectowifi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;



import java.util.Vector;

/**
 * Created by jon on 12/11/2015.
 */
public class MiImageView extends ImageView
{
    private int ancho_vista;
    private int alto_vista;
    private boolean draw_circulo = false;
    private Paint pincelAzul, pincelRojo, pincelVerde, pincelTexto;
    private Circulo cir;
    private Circulo pos;
    public int prueba;
    private Vector<PuntoMedido> pmV;
    private String rowIdString = "---";


    public MiImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        pmV = Aplicacion.getPuntoMedidoVector();
        pincelAzul = PincelMaker(Color.BLUE, 8, Paint.Style.STROKE);
        pincelRojo = PincelMaker(Color.RED, 8, Paint.Style.STROKE);
        pincelVerde = PincelMaker(Color.GREEN, 8, Paint.Style.STROKE);
        pincelTexto = PincelMaker(Color.BLACK, 8, Paint.Style.FILL);
        pincelTexto.setTextSize(60);

        cir = new Circulo(0, 0, 0);
        pos = new Circulo(0, 0, 0);
    }

    private Paint PincelMaker(int color, float grosor, Paint.Style estilo)
    {
        Paint p = new Paint();
        p.setColor(color);
        p.setStrokeWidth(grosor);
        p.setStyle(estilo);
        return p;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        float x_real;
        float y_real;

        ancho_vista = getWidth();
        alto_vista = getHeight();

        canvas.drawCircle(pos.x, pos.y, pos.r, pincelVerde);

        rowIdString = "" + Aplicacion.getRowId();
        canvas.drawText(rowIdString, 10, 100, pincelTexto);

        if(Aplicacion.getModoDeOperacion().equals(Aplicacion.MODO_ENTRENAMIENTO))
        {
            canvas.drawCircle(cir.x, cir.y, cir.r, pincelAzul);
            for (PuntoMedido pm : pmV)
            {
                x_real = ancho_vista * pm.getX() / 1000;
                y_real = alto_vista * pm.getY() / 1000;
                canvas.drawCircle(x_real, y_real, 20, pincelRojo);
            }
        }
    }

    public void dibujarPosicion(float x, float y)
    {
        float x_real;
        float y_real;

        x_real = ancho_vista*x/1000;
        y_real = alto_vista*y/1000;
        pos.x = x_real;
        pos.y = y_real;
        pos.r = 30;
        invalidate();
    }

    public void dibujarCirculo(float x, float y, float r)
    {
        cir.x = x;
        cir.y = y;
        cir.r = r;
        draw_circulo = true;
        invalidate();
    }

    public class Circulo
    {
        public float x;
        public float y;
        public float r;

        public Circulo()
        {

        }

        public Circulo(float x, float y, float r)
        {
            this.x = x;
            this.y = y;
            this.r = r;
        }
    }
}
