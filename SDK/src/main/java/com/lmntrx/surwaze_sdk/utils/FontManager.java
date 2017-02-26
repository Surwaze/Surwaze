package com.lmntrx.surwaze_sdk.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lmntrx.surwaze_sdk.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * Created by livin on 27/11/16.
 * Copyright 2016 DroidAwesome - Livin Mathew
 */

public class FontManager {

    private static final String ROOT = "res/raw/";

    private static String FILE_NAME = "quicksand.otf";

    private static final String FONT_QUICKSAND = ROOT + FILE_NAME;

    /* Cache Font asset to avoid extra memory head */
    private static final Hashtable<String, Typeface> cache = new Hashtable<>();


    /**
     * @param context context passed
     * @return returns TypeFace object from cache
     */
    /* Returns TypeFace */
    private static Typeface getTypeface(Context context) {
        synchronized (cache) {
            if (!cache.containsKey(FONT_QUICKSAND)) {
                cache.put(FONT_QUICKSAND, processFontFromRaw(context));
            }else {
                Log.i(FontManager.class.getSimpleName(),"Loading typeface from cache");
            }
            return cache.get(FONT_QUICKSAND);
        }
    }

    private static Typeface processFontFromRaw(Context context) {
        Typeface typeface = null;
        InputStream inputStream = null;

        String outPath = context.getCacheDir() + "/tmp" + System.currentTimeMillis() + ".raw";

        try {
            inputStream = context.getResources().openRawResource(R.raw.comfortaa_regular);
        } catch (Resources.NotFoundException ignored) {
        }

        try {
            assert inputStream != null;
            byte[] buffer = new byte[inputStream.available()];
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(outPath));
            int num;
            while ((num = inputStream.read(buffer)) > 0) {
                stream.write(buffer, 0, num);
            }
            stream.close();
            typeface = Typeface.createFromFile(outPath);
            if (new File(outPath).delete()){
                Log.i(FontManager.class.getSimpleName(),"Deleted " + outPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return typeface;
    }

    public static void setFontToChildrenOfContainer(Context context, ViewGroup viewGroup){
        for (int i = 0; i <= viewGroup.getChildCount(); i++){
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup){
                setFontToChildrenOfContainer(context, (ViewGroup) view);
            }else if (view instanceof TextView){
                ((TextView)view).setTypeface(FontManager.getTypeface(context));
            }
        }
    }
}
