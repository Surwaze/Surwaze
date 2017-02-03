package com.lmntrx.surwaze_sdk;

import android.annotation.SuppressLint;
import android.content.Context;

/***
 * Created by livin on 2/2/17.
 */

class Surwaze {

    private Context context;

    private Surwaze(Context context){
        this.context = context;
    }

    @SuppressLint("StaticFieldLeak")
    private static Surwaze surwaze;

    public static void init(Context context){
        if (surwaze == null)
            surwaze = new Surwaze(context);
    }

    public static Surwaze getInstance(){
        return surwaze;
    }



}
