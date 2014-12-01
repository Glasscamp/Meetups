package com.glasscamp.meetup.wordtranslator;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.google.android.glass.widget.Slider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends Activity {


    private String mLanguage;
    private final int SPEECH_REQUEST = 1000;
    private final int LANGUAGE_REQUEST = 2000;
    private View mView;
    private TextToSpeech mSpeech;

    @Override
    protected void onCreate(Bundle bundle) {


        // Even though the text-to-speech engine is only used in response to a menu action, we
        // initialize it when the application starts so that we avoid delays that could occur
        // if we waited until it was needed to start it up.
        mSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // Do nothing.
            }
        });


        mView = buildView(getString(R.string.translation_in_progress));
        Slider.from(mView).startIndeterminate();
        setContentView(mView);

        super.onCreate(bundle);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        getWindow().openPanel(WindowUtils.FEATURE_VOICE_COMMANDS, null);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {

            if(item.getItemId() == R.id.other) {
                displaySpeechRecognizer(getString(R.string.prompt_lang), LANGUAGE_REQUEST);
                return true;
            }

            mLanguage  = item.getTitle().toString();
            displaySpeechRecognizer(getString(R.string.prompt) + " " + mLanguage, SPEECH_REQUEST);
            return true;
        }
        // Good practice to pass through to super if not handled
        return super.onMenuItemSelected(featureId, item);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mSpeech.shutdown();
        mSpeech = null;
        super.onDestroy();
    }

    private void getTranslation(String text, String language) {

        if(text == null || text.length() == 0) {
            displaySpeechRecognizer(getString(R.string.prompt) + " " + mLanguage, SPEECH_REQUEST);
            return;
        }

        AsyncTask<String, String, String> downloadTranslation = new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(false);
                    connection.setRequestMethod("GET");

                    final int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        // read stream to end
                        final BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        final StringBuilder response = new StringBuilder(8192);
                        for (inputLine = input.readLine(); inputLine != null; inputLine = input.readLine()) {
                            response.append(inputLine);
                        }

                        final String responseString = response.toString();
                        JSONObject responseObject = new JSONObject(responseString);
                        JSONObject responseData = responseObject.getJSONObject("responseData");

                        return responseData.getString("translatedText");
                    } else {
                        return "SERVER ERROR";
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return "URL ERROR";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "IO EXCEPTION";
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "JSON EXCEPTION";
                }
            }

            @Override
            protected void onPostExecute(String s) {
                mView = buildView(s);
                setContentView(mView);
            }
        };

        String langCode = language.substring(0,3);
        downloadTranslation.execute("http://api.mymemory.translated.net/get?q=" + Uri.encode(text) + "&langpair=en|" + langCode);
        mSpeech.speak(getString(R.string.translating) + " " + text + " " + getString(R.string.into) + " " + mLanguage, TextToSpeech.QUEUE_FLUSH, null);

    }

    private View buildView(String text) {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);

        card.setText(text);
        return card.getView();
    }

    private void displaySpeechRecognizer(String prompt, int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            getTranslation(spokenText, mLanguage);
        }

        if (requestCode == LANGUAGE_REQUEST && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);

            if(spokenText == null) {
                displaySpeechRecognizer(getString(R.string.prompt_lang), LANGUAGE_REQUEST);
                return;
            }

            mLanguage = spokenText.toLowerCase();
            displaySpeechRecognizer(getString(R.string.prompt) + " " + mLanguage, SPEECH_REQUEST);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}
