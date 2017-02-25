package com.lmntrx.surwaze_sdk.SurUnits;

import android.animation.Animator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.lmntrx.surwaze_sdk.widget.OptionPicker;
import com.lmntrx.surwaze_sdk.widget.TypeWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/***
 * Created by livin on 25/2/17.
 */

public class Interstitial extends Dialog {

    private Context context;

    private int showCount = 0;

    List<Question> questions;

    private boolean answered = false;


    private TextView optionATV,
        optionBTV,
        optionCTV,
        optionDTV;

    private TypeWriter questionTV;

    private View optionsParentLayout;

    private ImageView skipButton, circleLoader;

    private OptionPicker optionPicker;

    private String currentID;

    public interface Callback{
        void onError(SurwazeException exception);
        void onLoadComplete(Interstitial interstitial);
        void onSkipped();
        void onAnswered();
    }


    private Callback callbacks;

    public Interstitial(Context context) {
        super(context, android.R.style.Theme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.interstitial);
        this.context = context;
        questionTV = (TypeWriter) findViewById(R.id.questionTV);
        optionATV = (TextView) findViewById(R.id.optionATV);
        optionBTV = (TextView) findViewById(R.id.optionBTV);
        optionCTV = (TextView) findViewById(R.id.optionCTV);
        optionDTV = (TextView) findViewById(R.id.optionDTV);
        circleLoader = (ImageView) findViewById(R.id.circleLoader);
        optionsParentLayout = findViewById(R.id.optionsParentLayout);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Interstitial.this.context.unregisterReceiver(revealOptionBR);
            }
        });
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Interstitial.this.context.unregisterReceiver(revealOptionBR);
            }
        });
        optionPicker = (OptionPicker) findViewById(R.id.optionPicker);
        optionPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                dismiss();
                callbacks.onAnswered();
                String sl;
                if (progress > 80){
                    sl = "a";
                }else if (progress > 50){
                    sl = "b";
                }else if (progress > 20){
                    sl = "c";
                }else {
                    sl = "d";
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
                        headers.put("x-access-token",Interstitial.this.context.getString(R.string.token));
                        return headers;
                    }
                };
                request.setRetryPolicy(new DefaultRetryPolicy(
                        10 * 1000,//timeout
                        2,//retries
                        2));//back off multiplier
                Surwaze.getInstance(Interstitial.this.context).addToRequestQueue(request);
            }
        });
        skipButton = (ImageView) findViewById(R.id.skipButton);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!answered){
                    callbacks.onSkipped();
                }
                dismiss();
            }
        });
        setCancelable(false);
    }

    private void startCircleLoaderBlink() {
        final Animation blink = new AlphaAnimation(1, 0);
        blink.setDuration(500);
        blink.setInterpolator(new LinearInterpolator());
        blink.setRepeatCount(Animation.INFINITE);
        blink.setRepeatMode(Animation.REVERSE);
        circleLoader.startAnimation(blink);
    }

    private BroadcastReceiver revealOptionBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int cx = optionsParentLayout.getWidth() / 2;
            int cy = optionsParentLayout.getHeight() / 2;
            float finalRadius = (float) Math.hypot(cx, cy);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Animator anim = ViewAnimationUtils.createCircularReveal(optionsParentLayout, cx, cy, 0, finalRadius);
                optionsParentLayout.setVisibility(View.VISIBLE);
                anim.start();
                optionPicker.setEnabled(true);
            }else {

            }
        }
    };

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
                    callbacks.onLoadComplete(Interstitial.this);
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
        startCircleLoaderBlink();
        optionPicker.setEnabled(false);
        context.registerReceiver(revealOptionBR,new IntentFilter(Interstitial.this.context.getPackageName() + "REVEAL_OPTIONS"));
        optionsParentLayout.setVisibility(View.INVISIBLE);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Interstitial.this.context.sendBroadcast(new Intent(Interstitial.this.context.getPackageName() + "REVEAL_OPTIONS"));
            }
        },1500);
        try {
            Question question = questions.get(questions.size()-++showCount);
            Log.d("Question",question.getQuestion());
            currentID = question.getQuestionID();
            questionTV.setCharacterDelay(50);
            questionTV.animateText(question.getQuestion());
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
            dismiss();
        }
    }
}
