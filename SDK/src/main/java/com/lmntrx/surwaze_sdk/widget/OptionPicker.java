package com.lmntrx.surwaze_sdk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/***
 * Created by livin on 25/2/17.
 */

public class OptionPicker extends SeekBar {


    private OnSeekBarChangeListener myListener;

    private Boolean selectionState = false;

    public OptionPicker(Context context) {
        super(context);
    }

    public OptionPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OptionPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener mListener){
        this.myListener = mListener;
    }

    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);

        super.onDraw(c);
    }

    public void toggleSelectionLock(){
        selectionState = !selectionState;
    }

    public Boolean isSelectionLocked(){
        return selectionState;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(myListener!=null)
                    myListener.onStartTrackingTouch(this);
                break;
            case MotionEvent.ACTION_MOVE:
                setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                myListener.onProgressChanged(this, getMax() - (int) (getMax() * event.getY() / getHeight()), true);
                break;
            case MotionEvent.ACTION_UP:
                myListener.onStopTrackingTouch(this);
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }
}
