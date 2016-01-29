package com.example.jamesearle.pebblesmokesensor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingsActivity extends AppCompatActivity {

    private final UUID WATCHAPP_UUID = UUID.fromString("22abac42-af9f-4223-ba06-d2b491f9456c");
    private static final int DATA_LOG_TAG_COMPASS = 52;

    // Must be global for inner class to have write access.
    private StringBuilder resultBuilder = new StringBuilder();
    private PebbleKit.PebbleDataLogReceiver dataloggingReceiver;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        grabPebbleSettings();

        textView = (TextView)findViewById(R.id.loggedDataText);
    }

    public void grabPebbleSettings() {
        StringBuilder builder = new StringBuilder();
        builder.append("Pebble Info\n\n");

        // Is the watch connected?
        boolean isConnected = PebbleKit.isWatchConnected(this);
        builder.append("Watch connected? " + isConnected).append("\n");

        // What is the firmware version?
        PebbleKit.FirmwareVersionInfo info = PebbleKit.getWatchFWVersion(this);
        builder.append("Firmware version: ");
        builder.append(info.getMajor()).append(".");
        builder.append(info.getMinor()).append("\n");

        boolean loggingSupported = PebbleKit.isDataLoggingSupported(this);
        builder.append("Data Logging supported: " + loggingSupported + "\n");

        // Is AppMesage supported?
        boolean appMessageSupported = PebbleKit.areAppMessagesSupported(this);
        builder.append("AppMessage supported: " + (appMessageSupported ? "true" : "false"));

        TextView textView = (TextView)findViewById(R.id.pebble_info_text);
        textView.setText(builder.toString());
    }

    public void sendNotificationButtonClick(View v) {
        // Sending a notification
        final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

        final Map data = new HashMap();
        data.put("title", "Test Message");
        data.put("body", "Whoever said nothing was impossible never tried to slam a revolving door.");
        final JSONObject jsonData = new JSONObject(data);
        final String notificationData = new JSONArray().put(jsonData).toString();

        i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", "PebbleKit Android");
        i.putExtra("notificationData", notificationData);
        sendBroadcast(i);

        Toast.makeText(this, "Notification Delivered to Pebble", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Define data reception behavior
        PebbleKit.PebbleDataLogReceiver dataloggingReceiver = new PebbleKit.PebbleDataLogReceiver(WATCHAPP_UUID) {

            @Override
            public void receiveData(Context context, UUID logUuid, Long timestamp, Long tag, int data) {
                // Check this is the compass headings log
                if(tag.intValue() == DATA_LOG_TAG_COMPASS) {
                    // Get the compass value and append to result StringBuilder
                    resultBuilder.append("Heading: " + data + " degrees");
                    resultBuilder.append("\n");
                }
            }

            @Override
            public void onFinishSession(Context context, UUID logUuid, Long timestamp, Long tag) {
                super.onFinishSession(context, logUuid, timestamp, tag);

                // Display all compass headings received
                textView.setText("Session finished!\n" + "Results were: \n\n" + resultBuilder.toString());
            }

        };

        // Register DataLogging Receiver
        PebbleKit.registerDataLogReceiver(this, dataloggingReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Always unregister callbacks
        if(dataloggingReceiver != null) {
            unregisterReceiver(dataloggingReceiver);
        }
    }
}
