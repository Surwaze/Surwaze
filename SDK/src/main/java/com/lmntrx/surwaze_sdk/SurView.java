package com.lmntrx.surwaze_sdk;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.lmntrx.surwaze_sdk.model.Option;
import com.lmntrx.surwaze_sdk.model.Question;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * Created by livin on 3/2/17.
 */

public class SurView extends RelativeLayout {

    private Context context;

    List<Question> questions;

    private TextView questionTV,
            optionATV,
            optionBTV,
            optionCTV,
            optionDTV;

    private String BASE_URL = "http://api.surwaze.com/";

    public interface Callback{
        void onError(SurwazeException exception);
        void onComplete(SurView surView);
        void onSkipped();
    }

    Callback callbacks;

    public SurView(Context context) {
        super(context);
        init(context,null,0,0);
    }

    public SurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,0,0);
    }

    public SurView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr,0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SurView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs,defStyleAttr,defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.surview,this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        questionTV = (TextView) findViewById(R.id.questionTV);
        optionATV = (TextView) findViewById(R.id.optionATV);
        optionBTV = (TextView) findViewById(R.id.optionBTV);
        optionCTV = (TextView) findViewById(R.id.optionCTV);
        optionDTV = (TextView) findViewById(R.id.optionDTV);
    }



    public SurView setCallbacks(Callback callbacks){

        this.callbacks = callbacks;
        return this;

    }

    public void load(){
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, BASE_URL + "questions/", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length()>0){
                    questions = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++){
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);
                            String id = jsonObject.getString("_id");
                            String questionStr = jsonObject.getString("question");
                            JSONArray optionsArr = jsonObject.getJSONArray("options");
                            ArrayList<Option> options = new ArrayList<>();
                            for (int j = 0; j < optionsArr.length(); j++){
                                String optionStr = optionsArr.getJSONObject(j).getString("option");
                                String sl = optionsArr.getJSONObject(j).getString("sl");
                                options.add(new Option(sl,optionStr));
                            }
                            questions.add(new Question(questionStr,options,id));
                        } catch (JSONException e) {
                            callbacks.onError(new SurwazeException(e.getMessage()));
                            return;
                        }
                    }
                    callbacks.onComplete(SurView.this);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                    callbacks.onError(new SurwazeException(error.getLocalizedMessage()));
                error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<>();
                headers.put("x-access-token",context.getString(R.string.token));
                headers.put("Content-Type", "application/json");

                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10 * 1000,//timeout
                2,//retries
                2));//back off multiplier

        // Access the RequestQueue through your singleton class.
        Surwaze.getInstance(context).addToRequestQueue(request);

    }

    public void show() throws SurwazeException {
        try {
            Question question = questions.get(questions.size()-1);
            questionTV.setText(question.getQuestion());
            ArrayList<Option> options = question.getOptions();
            for (Option option : options){
                switch (option.getOptionSL()){
                    case "a":
                        optionATV.setText(option.getOption());
                        break;
                    case "b":
                        optionBTV.setText(option.getOption());
                        break;
                    case "c":
                        optionCTV.setText(option.getOption());
                        break;
                    case "d":
                        optionDTV.setText(option.getOption());
                        break;
                }
            }
            this.setVisibility(VISIBLE);
        }catch (NullPointerException e){
            throw new SurwazeException("Not Ready Yet");
        }

    }

}
