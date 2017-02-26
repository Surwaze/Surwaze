package com.lmntrx.surwaze_sdk.model;

/***
 * Created by livin on 16/2/17.
 */

public class Option {

    private String option;
    private String optionSL;

    public String getOptionSL() {
        return optionSL;
    }

    public String getOption() {
        return option;
    }

    public Option(String optionSL, String option){
        this.option = option;
        this.optionSL = optionSL;
    }
}
