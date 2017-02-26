package com.lmntrx.surwaze_sdk.SurUnits;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
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
 * Created by livin on 3/2/17.
 */

public class SurView extends RelativeLayout {

    private Context context;

    private int showCount = 0;

    List<Question> questions;

    private TextView questionTV;
    private RadioButton optionATV,
            optionBTV,
            optionCTV,
            optionDTV;

    String currentID;

    public interface Callback{
        void onError(SurwazeException exception);
        void onComplete(SurView surView);
    }

    Callback callbacks;

    public SurView(Context context) {
        super(context);
        init(context);
    }

    public SurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SurView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.surview,this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setVisibility(GONE);
        questionTV = (TextView) findViewById(R.id.questionTV);
        optionATV = (RadioButton) findViewById(R.id.optionATV);
        optionBTV = (RadioButton) findViewById(R.id.optionBTV);
        optionCTV = (RadioButton) findViewById(R.id.optionCTV);
        optionDTV = (RadioButton) findViewById(R.id.optionDTV);
        OnClickListener optionClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                SurView.this.setVisibility(GONE);
                String sl;
                if (v.getId() == R.id.optionATV){
                    sl = "a";
                }else if (v.getId() == R.id.optionBTV){
                    sl = "b";
                }else if (v.getId() == R.id.optionCTV){
                    sl = "c";
                }else if (v.getId() == R.id.optionDTV){
                    sl = "d";
                }else {
                    sl = "INVALID";
                }
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Constants.API_BASE_URL + "hit/" + currentID + "?option=" + sl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("SurwazeOption","Recorded");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {

                        Map<String, String> headers = new HashMap<>();
                        headers.put("x-access-token",context.getString(R.string.token));
                        return headers;

                    }
                };
                request.setRetryPolicy(new DefaultRetryPolicy(
                        10 * 1000,//timeout
                        2,//retries
                        2));//back off multiplier
                Surwaze.getInstance(context).addToRequestQueue(request);
            }
        };
        optionATV.setOnClickListener(optionClickListener);
        optionBTV.setOnClickListener(optionClickListener);
        optionCTV.setOnClickListener(optionClickListener);
        optionDTV.setOnClickListener(optionClickListener);
    }



    public SurView setCallbacks(Callback callbacks){

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
        request.setShouldCache(false);
        Surwaze.getInstance(context).addToRequestQueue(request);

    }

    public void show() throws SurwazeException {
        try {
            Question question = questions.get(questions.size()-++showCount);
            Log.d("Question",question.getQuestion());
            currentID = question.getQuestionID();
            questionTV.setText(question.getQuestion());
            optionATV.setChecked(false);
            optionBTV.setChecked(false);
            optionCTV.setChecked(false);
            optionDTV.setChecked(false);
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
        }catch (ArrayIndexOutOfBoundsException exception){
            throw new SurwazeException("No more questions to show");
        }

    }

}
