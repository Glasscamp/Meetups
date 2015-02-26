package com.wearablelab.wearapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.CardFragment;
import android.view.View;

public class MainActivity extends Activity {
    private static final int SPEECH_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        CardFragment cardFragment = CardFragment.create("Star Wars",
                "In My Custom Galaxy.. far away");
        fragmentTransaction.add(R.id.frame_layout, cardFragment);
        fragmentTransaction.commit();

        findViewById(R.id.frame_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySpeechRecognizer();
            }
        });


    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SPEECH_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Intent intent = new Intent(this, DelayedConfirmationActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("spokenText", data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
