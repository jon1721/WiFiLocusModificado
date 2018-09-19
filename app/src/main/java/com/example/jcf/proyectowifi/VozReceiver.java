package com.example.jcf.proyectowifi;

import android.content.Context;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

/**
 * Created by jon on 14/11/2015.
 */
public class VozReceiver extends UtteranceProgressListener
{
    private final static String TAG = "JCDBG";
    private final Context context;

    public VozReceiver(Context context)
    {
        this.context = context;
    }

    @Override
    public void onDone(String utteranceId)
    {
        Log.i(TAG, "TTS onDone");
    }

    @Override
    public void onStart(String utteranceId)
    {
        Log.i(TAG, "TTS onStart");
    }

    @Override
    public void onError(String utteranceId)
    {
        Log.i(TAG, "TTS onError");
    }
}
