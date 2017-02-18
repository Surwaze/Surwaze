package com.lmntrx.surwaze_sdk.model;

import java.util.ArrayList;

/***
 * Created by livin on 16/2/17.
 */

public class Question {

    public String getQuestion() {
        return question;
    }

    public ArrayList<Option> getOptions() {
        return options;
    }

    public String getQuestionID() {
        return questionID;
    }

    private String question;
    private ArrayList<Option> options;
    private String questionID;

    public Question(String question, ArrayList<Option> options, String questionID){
        this.question = question;
        this.options = options;
        this.questionID = questionID;
    }

}
