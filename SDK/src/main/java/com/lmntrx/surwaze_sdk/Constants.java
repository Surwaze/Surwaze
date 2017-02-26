package com.lmntrx.surwaze_sdk;

/***
 * Created by livin on 25/2/17.
 */

public class Constants {

    /* URLS */
    public static final String API_BASE_URL = "http://api.surwaze.com/questions/";

    /* Animations and Timers */
    public static final long HELP_TIMER_DURATION = 5000; //ms
    public static final long REVEAL_OPTIONS_TIMER_DURATION = 2500; //ms
    public static final long CIRCULAR_BLINK_ANIMATION_DURATION = 1000; //ms
    public static final long FADE_IN_ANIMATION_DURATION = 800; //ms
    public static final long HELPER_HAND_GESTURE_ANIMATION_DURATION = 1000; //ms

    /* Volley Constants */
    public static final int VOLLEY_REQUEST_TIMEOUT = 10000; //ms
    public static final int VOLLEY_REQUEST_RETRIES = 2;
    public static final float VOLLEY_REQUEST_BACKOFF_MULTIPLIER = 2;

    /* Vibration Duration*/
    public static final long HAPTIC_FEEDBACK_VIBRATION_DURATION = 50;
}
