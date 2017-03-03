package com.lmntrx.surwaze_sdk.SurUnits;

import android.animation.Animator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
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
import com.lmntrx.surwaze_sdk.utils.FontManager;
import com.lmntrx.surwaze_sdk.widget.OptionPicker;

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

    private Boolean canShow = true;

    private int showCount = 0;

    private List<Question> questions;

    private boolean answered = false;

    private TextView optionATV,
        optionBTV,
        optionCTV,
        optionDTV;

    private TextView questionTV;

    private View optionsParentLayout;

    private ImageView circleLoader;
    private ImageView handGesture;

    private OptionPicker optionPicker;

    private String currentID;

    private Boolean shouldVibrate = true;

    public interface Callback{
        void onError(SurwazeException exception);
        void onLoadComplete(Interstitial interstitial);
        void onSkipped();
        void onAnswered();
    }


    private Callback callbacks;

    private Timer helper;
    private Animation helperAnimation;

    public Interstitial(final Context context) {
        super(context, android.R.style.Theme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.interstitial);
        this.context = context;
        questionTV = (TextView) findViewById(R.id.questionTV);
        optionATV = (TextView) findViewById(R.id.optionATV);
        optionBTV = (TextView) findViewById(R.id.optionBTV);
        optionCTV = (TextView) findViewById(R.id.optionCTV);
        optionDTV = (TextView) findViewById(R.id.optionDTV);
        circleLoader = (ImageView) findViewById(R.id.circleLoader);
        handGesture = (ImageView) findViewById(R.id.handGesture);
        optionsParentLayout = findViewById(R.id.optionsParentLayout);
        optionPicker = (OptionPicker) findViewById(R.id.optionPicker);
        ImageView skipButton = (ImageView) findViewById(R.id.skipButton);
        View.OnClickListener optionClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.optionATV){
                    hitOption("A");
                }else if (id == R.id.optionBTV){
                    hitOption("B");
                }else if (id == R.id.optionCTV){
                    hitOption("C");
                }else if (id == R.id.optionDTV){
                    hitOption("D");
                }
                if (shouldVibrate){
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator.hasVibrator()){
                        vibrator.vibrate(Constants.HAPTIC_FEEDBACK_VIBRATION_DURATION);
                    }
                }
                dismiss();
            }
        };
        optionATV.setOnClickListener(optionClickListener);
        optionBTV.setOnClickListener(optionClickListener);
        optionCTV.setOnClickListener(optionClickListener);
        optionDTV.setOnClickListener(optionClickListener);
        FontManager.setFontToChildrenOfContainer(this.context,(ViewGroup) findViewById(R.id.interstitial_root));
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
                Interstitial.this.context.unregisterReceiver(answeredSurveyBR);
                Interstitial.this.context.unregisterReceiver(errorReceivedBR);
            }
        });
        optionPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!optionPicker.isSelectionLocked()){
                    helper.cancel();
                    handGesture.setVisibility(View.GONE);
                    handGesture.setImageDrawable(new ColorDrawable(ContextCompat.getColor(context,android.R.color.transparent)));
                    if (helperAnimation!=null){
                        helperAnimation.cancel();
                    }
                    if (progress > 80){
                        optionATV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionBBackground));
                        optionBTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
                        optionCTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
                        optionDTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
                    }else if (progress > 50){
                        optionBTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionBBackground));
                        optionATV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
                        optionCTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
                        optionDTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
                    }else if (progress > 20){
                        optionCTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionBBackground));
                        optionBTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
                        optionATV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
                        optionDTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
                    }else {
                        optionDTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionBBackground));
                        optionBTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
                        optionCTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
                        optionATV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (!optionPicker.isSelectionLocked()){
                    helper.cancel();
                    handGesture.setVisibility(View.INVISIBLE);
                    handGesture.setImageDrawable(new ColorDrawable(ContextCompat.getColor(context,android.R.color.transparent)));
                    if (helperAnimation!=null){
                        helperAnimation.cancel();
                    }
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!optionPicker.isSelectionLocked()){
                    if (shouldVibrate){
                        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator.hasVibrator()){
                            vibrator.vibrate(Constants.HAPTIC_FEEDBACK_VIBRATION_DURATION);
                        }
                    }
                    dismiss();
                    int progress = seekBar.getProgress();
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
                    hitOption(sl);
                }
            }
        });
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

    private void hitOption(String sl) {
        sl = sl.toUpperCase();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Constants.API_BASE_URL + "hit/" + currentID + "?option=" + sl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbacks.onError(new SurwazeException(error.getLocalizedMessage()));
            }
        }){
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response.statusCode == 200){
                    Interstitial.this.context.sendBroadcast(new Intent(
                            Interstitial.this.context.getPackageName() + ".ANSWERED"
                    ));
                }else {
                    Interstitial.this.context.sendBroadcast(new Intent(
                            Interstitial.this.context.getPackageName() + ".ERROR"
                    ));
                }
                return super.parseNetworkResponse(response);
            }

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
            optionPicker.toggleSelectionLock();
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
            handGesture.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.hand_gesture));
            handGesture.setAnimation(helperAnimation);
        }
    };

    private BroadcastReceiver answeredSurveyBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            callbacks.onAnswered();
        }
    };

    private BroadcastReceiver errorReceivedBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            callbacks.onError(new SurwazeException("Network or Server error"));
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
                Constants.VOLLEY_REQUEST_TIMEOUT,
                Constants.VOLLEY_REQUEST_RETRIES,
                Constants.VOLLEY_REQUEST_BACKOFF_MULTIPLIER));

        // Access the RequestQueue through your singleton class.
        request.setShouldCache(false);
        Surwaze.getInstance(context).addToRequestQueue(request);

    }

    @Override
    public void show(){
        super.show();
        optionATV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
        optionBTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
        optionCTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));
        optionDTV.setBackgroundColor(ContextCompat.getColor(context,R.color.colorOptionCnBackground));

        context.registerReceiver(revealOptionBR,new IntentFilter(Interstitial.this.context.getPackageName() + ".REVEAL_OPTIONS"));
        context.registerReceiver(showHelpBR, new IntentFilter(Interstitial.this.context.getPackageName() + ".SHOW_HELP"));
        context.registerReceiver(answeredSurveyBR, new IntentFilter(Interstitial.this.context.getPackageName() + ".ANSWERED"));
        context.registerReceiver(errorReceivedBR, new IntentFilter(Interstitial.this.context.getPackageName() + ".ERROR"));

        answered = false;
        handGesture.setVisibility(View.INVISIBLE);
        startCircleLoaderBlink();
        optionPicker.toggleSelectionLock();
        optionPicker.setProgress(90);
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
            questionTV.setText(question.getQuestion());
            ArrayList<Option> options = question.getOptions();
            for (Option option : options){
                switch (option.getOptionSL()){
                    case "A":
                        optionATV.setText(option.getOption());
                        break;
                    case "B":
                        optionBTV.setText(option.getOption());
                        break;
                    case "C":
                        optionCTV.setText(option.getOption());
                        break;
                    case "D":
                        optionDTV.setText(option.getOption());
                        break;
                    default:
                        Toast.makeText(context, "default " + option.getOptionSL(), Toast.LENGTH_SHORT).show();
                }
            }
            canShow = questions.size() > showCount;
        }catch (NullPointerException e){
            Log.d("SurwazeInterstitial","Still loading...");
        }catch (ArrayIndexOutOfBoundsException exception){
            Log.d("SurwazeInterstitial","No more questions to show");
            dismiss();
        }
    }

    public Boolean canShow(){
        return canShow;
    }
}
