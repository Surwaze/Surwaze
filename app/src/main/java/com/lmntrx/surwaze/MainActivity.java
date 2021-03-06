package com.lmntrx.surwaze;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.lmntrx.surwaze_sdk.SurUnits.SurView;
import com.lmntrx.surwaze_sdk.SurwazeException;

public class MainActivity extends AppCompatActivity {

    SurView surView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surView = (SurView) findViewById(R.id.surView);

        surView.setCallbacks(new SurView.Callback() {
            @Override
            public void onError(SurwazeException exception) {
                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete(SurView surView) {
                try {
                    surView.show();
                } catch (SurwazeException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).load();

    }

    public void showSurvey(View view) {
        try {
            surView.show();
        } catch (SurwazeException e) {
            e.printStackTrace();
        }
    }

    public void launchInterstitialSurveyActivity(View view) {
        startActivity(new Intent(this,InterstitialSurUnit.class));
    }
}
