package com.wearablelab.wearnotification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button pushButton = (Button) findViewById(R.id.pushbutton);
        pushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotification();
            }
        });
        Bundle remoteInput = RemoteInput.getResultsFromIntent(getIntent());
        if (remoteInput != null) {
            TextView protestText = (TextView) findViewById(R.id.protestText);
            protestText.setText(remoteInput.getCharSequence("500"));
        }
    }

    private void showNotification() {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("fb://profile"));
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 500, intent , PendingIntent.FLAG_UPDATE_CURRENT);

        // Create builder for the main notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setGroupSummary(true)
                        .setGroup("Movie")
                        .setSmallIcon(R.drawable.abc_ic_voice_search_api_mtrl_alpha)
                        .setContentTitle("Star Wars")
                        .setContentText("A long time ago, in a galaxy far, far away...")
                        .setColor(Color.BLUE)
                        .setContentIntent(viewPendingIntent);

        // Create a big text style for the second page
        NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
        secondPageStyle.setBigContentTitle("Introduction")
                .bigText("A long time ago, in a galaxy far,\n" +
                        "far away....\n" +
                        "\n" +
                        "It is a period of civil war.\n" +
                        "Rebel spaceships, striking\n" +
                        "from a hidden base, have won\n" +
                        "their first victory against\n" +
                        "the evil Galactic Empire.\n" +
                        "\n" +
                        "During the battle, rebel\n" +
                        "spies managed to steal secret\n" +
                        "plans to the Empire's\n" +
                        "ultimate weapon, the DEATH\n" +
                        "STAR, an armored space\n" +
                        "station with enough power to\n" +
                        "destroy an entire planet.\n" +
                        "\n" +
                        "Pursued by the Empire's\n" +
                        "sinister agents, Princess\n" +
                        "Leia races home aboard her\n" +
                        "starship, custodian of the\n" +
                        "stolen plans that can save\n" +
                        "her people and restore\n" +
                        "freedom to the galaxy....");

        // Create second page notification
        Notification secondPageNotification =
                new NotificationCompat.Builder(this)
                        .setStyle(secondPageStyle)
                        .build();

        //Add a nice background
        Drawable drawable = getResources().getDrawable(R.drawable.darth);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        //Remote input
        RemoteInput remoteInput = new RemoteInput.Builder("500").setLabel("Say something young Padawan").build();
        //Add action
        Intent replyIntent = new Intent(this, MainActivity.class);
        PendingIntent replyPendingIntent = PendingIntent.getActivity(this, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.abc_ic_voice_search_api_mtrl_alpha,
                        "Protest", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();


        // Extend the notification builder with the second page
        Notification notification = notificationBuilder
                .extend(new NotificationCompat.WearableExtender()
                        .addPage(secondPageNotification)
                        .addAction(action)
                        .setBackground(bitmap))
                .build();

        // Issue the notification
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        notificationManager.notify((int) (Math.random()*100), notification);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
