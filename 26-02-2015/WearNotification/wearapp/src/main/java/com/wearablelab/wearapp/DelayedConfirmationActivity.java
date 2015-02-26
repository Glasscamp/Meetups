package com.wearablelab.wearapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;
import android.widget.TextView;


public class DelayedConfirmationActivity extends Activity implements
        DelayedConfirmationView.DelayedConfirmationListener {

    private DelayedConfirmationView mDelayedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delayed_confirmation);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            if(bundle.containsKey("spokenText")) {
                String spokenText = bundle.getString("spokenText");
                if(spokenText != null && spokenText.length() > 0) {
                    TextView title = (TextView) findViewById(R.id.spokenText);
                    title.setText(spokenText);
                }
            }
        }


        mDelayedView =
                (DelayedConfirmationView) findViewById(R.id.delayed_confirmation);
        mDelayedView.setListener(this);
        // Two seconds to cancel the action
        mDelayedView.setTotalTimeMs(5000);
        // Start the timer
        mDelayedView.start();
    }

    @Override
    public void onTimerFinished(View view) {
        finish();
    }

    @Override
    public void onTimerSelected(View view) {
        finish();
    }
}