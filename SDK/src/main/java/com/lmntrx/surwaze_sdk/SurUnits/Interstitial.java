package com.lmntrx.surwaze_sdk.SurUnits;

import android.animation.Animator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
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

    private List<Question> questions;

    private boolean answered = false;


    private TextView optionATV,
        optionBTV,
        optionCTV,
        optionDTV;

    private TypeWriter questionTV;

    private View optionsParentLayout;

    private ImageView circleLoader;
    private ImageView handGesture;

    private OptionPicker optionPicker;

    private String currentID;

    public interface Callback{
        void onError(SurwazeException exception);
        void onLoadComplete(Interstitial interstitial);
        void onSkipped();
        void onAnswered();
    }


    private Callback callbacks;

    private Timer helper;
    private Animation helperAnimation;

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
        handGesture = (ImageView) findViewById(R.id.handGesture);
        optionsParentLayout = findViewById(R.id.optionsParentLayout);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Interstitial.this.context.unregisterReceiver(revealOptionBR);
                Interstitial.this.context.unregisterReceiver(showHelpBR);
            }
        });
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Interstitial.this.context.unregisterReceiver(revealOptionBR);
                Interstitial.this.context.unregisterReceiver(showHelpBR);
            }
        });
        optionPicker = (OptionPicker) findViewById(R.id.optionPicker);
        optionPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                helper.cancel();
                handGesture.setVisibility(View.INVISIBLE);
                if (helperAnimation!=null){
                    helperAnimation.cancel();
                }
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
                        Constants.VOLLEY_REQUEST_TIMEOUT,
                        Constants.VOLLEY_REQUEST_RETRIES,
                        Constants.VOLLEY_REQUEST_BACKOFF_MULTIPLIER));
                Surwaze.getInstance(Interstitial.this.context).addToRequestQueue(request);
            }
        });
        ImageView skipButton = (ImageView) findViewById(R.id.skipButton);
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
        blink.setDuration(Constants.CIRCULAR_BLINK_ANIMATION_DURATION);
        blink.setInterpolator(new LinearInterpolator());
        blink.setRepeatCount(Animation.INFINITE);
        blink.setRepeatMode(Animation.REVERSE);
        circleLoader.startAnimation(blink);
    }

    private BroadcastReceiver revealOptionBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                int cx = optionsParentLayout.getWidth() / 2;
                int cy = optionsParentLayout.getHeight() / 2;
                float finalRadius = (float) Math.hypot(cx, cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(optionsParentLayout, cx, cy, 0, finalRadius);
                optionsParentLayout.setVisibility(View.VISIBLE);
                anim.start();
            }else {
                Animation fadeIn = new AlphaAnimation(0,1);
                fadeIn.setDuration(Constants.FADE_IN_ANIMATION_DURATION);
                optionsParentLayout.setVisibility(View.VISIBLE);
                fadeIn.start();
            }
            optionPicker.setEnabled(true);
        }
    };

    private BroadcastReceiver showHelpBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            helperAnimation = new TranslateAnimation(0,0,-200,200);
            helperAnimation.setDuration(Constants.HELPER_HAND_GESTURE_ANIMATION_DURATION);
            helperAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            helperAnimation.setRepeatCount(Animation.INFINITE);
            helperAnimation.setRepeatMode(Animation.REVERSE);
            handGesture.setVisibility(View.VISIBLE);
            handGesture.setAnimation(helperAnimation);
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
        handGesture.setVisibility(View.INVISIBLE);
        startCircleLoaderBlink();
        optionPicker.setEnabled(false);
        optionPicker.setProgress(50);
        context.registerReceiver(revealOptionBR,new IntentFilter(Interstitial.this.context.getPackageName() + ".REVEAL_OPTIONS"));
        context.registerReceiver(showHelpBR, new IntentFilter(Interstitial.this.context.getPackageName() + ".SHOW_HELP"));
        optionsParentLayout.setVisibility(View.INVISIBLE);
        helper = new Timer();
        helper.schedule(new TimerTask() {
            @Override
            public void run() {
                Interstitial.this.context.sendBroadcast(new Intent(Interstitial.this.context.getPackageName() + ".SHOW_HELP"));
            }
        },Constants.HELP_TIMER_DURATION);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Interstitial.this.context.sendBroadcast(new Intent(Interstitial.this.context.getPackageName() + ".REVEAL_OPTIONS"));
            }
        },Constants.REVEAL_OPTIONS_TIMER_DURATION);
        try {
            Question question = questions.get(questions.size()-++showCount);
            Log.d("Question",question.getQuestion());
            currentID = question.getQuestionID();
            questionTV.setCharacterDelay(Constants.TYPE_WRITER_SPEED);
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
