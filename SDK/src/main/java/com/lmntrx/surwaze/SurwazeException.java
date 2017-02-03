package com.lmntrx.surwaze;

/***
 * Created by livin on 3/2/17.
 */

class SurwazeException extends Exception{

    private String message;

    public SurwazeException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
