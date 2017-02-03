package com.lmntrx.surwaze;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

/***
 * Created by livin on 3/2/17.
 */

public class SurView extends RelativeLayout {

    Context context;

    TextView sampleTV;

    public interface Callback{
        void onError(SurwazeException exception);
        void onComplete();
        void onSkipped();
    }

    Callback callbacks;

    public SurView(Context context) {
        super(context);
    }

    public SurView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SurView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SurView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.surview,this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        sampleTV = (TextView) findViewById(R.id.sampleTV);
    }

    public void setCallbacks(Callback callbacks){
        this.callbacks = callbacks;
    }

}
