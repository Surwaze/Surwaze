package com.lmntrx.surwaze;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.lmntrx.surwaze_sdk.SurUnits.Interstitial;
import com.lmntrx.surwaze_sdk.SurwazeException;

public class InterstitialSurUnit extends AppCompatActivity {

    Interstitial surView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial_sur_unit);
        surView = new Interstitial(this);
        surView.setCallbacks(new Interstitial.Callback() {
            @Override
            public void onError(SurwazeException exception) {
                Toast.makeText(InterstitialSurUnit.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoadComplete(Interstitial interstitial) {
                findViewById(R.id.showInterstitialButton).setEnabled(true);
            }

            @Override
            public void onSkipped() {
                Toast.makeText(InterstitialSurUnit.this, "Skipped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAnswered() {
                Toast.makeText(InterstitialSurUnit.this, "Answered", Toast.LENGTH_SHORT).show();
            }
        }).load();
    }

    public void showSurvey(View view) {
        surView.show();
    }
}
