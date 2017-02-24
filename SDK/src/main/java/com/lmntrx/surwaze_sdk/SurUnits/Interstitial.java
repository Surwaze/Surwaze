package com.lmntrx.surwaze_sdk.SurUnits;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.lmntrx.surwaze_sdk.Constants;
import com.lmntrx.surwaze_sdk.R;
import com.lmntrx.surwaze_sdk.Surwaze;
import com.lmntrx.surwaze_sdk.SurwazeException;
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
 * Created by livin on 25/2/17.
 */

public class Interstitial extends Dialog {

    private Context context;

    private int showCount = 0;

    List<Question> questions;

    private boolean answered = false;


    private TextView questionTV,
        optionATV,
        optionBTV,
        optionCTV,
        optionDTV;

    private String currentID;

    public interface Callback{
        void onError(SurwazeException exception);
        void onComplete(Interstitial interstitial);
        void onSkipped();
    }


    private Callback callbacks;

    public Interstitial(Context context) {
        super(context, android.R.style.Theme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.interstitial);
        this.context = context;
        questionTV = (TextView) findViewById(R.id.questionTV);
        optionATV = (TextView) findViewById(R.id.optionATV);
        optionBTV = (TextView) findViewById(R.id.optionBTV);
        optionCTV = (TextView) findViewById(R.id.optionCTV);
        optionDTV = (TextView) findViewById(R.id.optionDTV);
        setCancelable(false);
    }

    public Interstitial setCallbacks(Callback callbacks){
        this.callbacks = callbacks;
        return this;
    }

    public void load(){
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, Constants.API_BASE_URL, null, new Response.Listener<JSONArray>() {
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
                    callbacks.onComplete(Interstitial.this);
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
        request.setShouldCache(false);
        Surwaze.getInstance(context).addToRequestQueue(request);

    }

    @Override
    public void show(){
        super.show();
        answered = false;
        try {
            Question question = questions.get(questions.size()-++showCount);
            Log.d("Question",question.getQuestion());
            currentID = question.getQuestionID();
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
        }catch (NullPointerException e){
            Log.d("SurwazeInterstitial","Still loading...");
        }catch (ArrayIndexOutOfBoundsException exception){
            Log.d("SurwazeInterstitial","No more questions to show");
        }
    }
}
