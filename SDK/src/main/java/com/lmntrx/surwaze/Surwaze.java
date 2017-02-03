package com.lmntrx.surwaze;

/***
 * Created by livin on 2/2/17.
 */

class Surwaze {

    private Surwaze(){}

    private static Surwaze surwaze = new Surwaze();

    public static Surwaze getInstance(){
        return surwaze;
    }

}
