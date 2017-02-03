package com.lmntrx.surwaze;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/***
 * Created by livin on 3/2/17.
 */

public class SurView extends RelativeLayout {

    public interface Callback{
        void onError(SurwazeException exception);
        void onComplete();
        void onSkipped();
    }
    
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
}
