package com.example.jamesearle.pebblesmokesensor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    final String PREFS_FILE = "SmokeFreePreferences";
    private static final UUID WATCHAPP_UUID = UUID.fromString("0a007011-a69b-4442-8187-f0d57105236b");
    private static final int DATA_LOG_TAG_XYZ = 42;
    private StringBuilder resultBuilder = new StringBuilder();
    public int cigaretteCount = 0;
    public SharedPreferences sp;
    PebbleKit.PebbleDataLogReceiver dataloggingReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sp = getSharedPreferences(WATCHAPP_UUID.toString(), 0);
        cigaretteCount = sp.getInt("numCigarettes", 0);

        TextView cigarettesToday = (TextView)findViewById(R.id.cigarettes_smoked);
        // Display all compass headings received
        cigarettesToday.setText(cigaretteCount + " Today");
    }

    public void addEntry(View v) {
        cigaretteCount = 0;
        sp.edit().putInt("numCigarettes", cigaretteCount).commit();

        Toast.makeText(this, "Cigarette Count Reset", Toast.LENGTH_LONG).show();

        TextView cigarettesToday = (TextView)findViewById(R.id.cigarettes_smoked);
        // Display all compass headings received
        cigarettesToday.setText("0 Today!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                break;
            case R.id.action_profile:
                openMyProfile();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openMyProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        PebbleKit.PebbleDataLogReceiver dataloggingReceiver = new PebbleKit.PebbleDataLogReceiver(WATCHAPP_UUID) {
            @Override
            public void receiveData(Context context, UUID logUuid, Long timestamp, Long tag, int data) {
                if(tag.intValue() == DATA_LOG_TAG_XYZ) {
                    if(data == 1) { // Confirmed cigarette
                        cigaretteCount += data;
                        sp.edit().putInt("numCigarettes", cigaretteCount).commit();
                        System.out.println("num-->" + sp.getInt("numCigarettes", 0));
                        System.out.println(data);
                    } else {
                        System.out.println(data);
                    }
                }
            }

            @Override
            public void onFinishSession(Context context, UUID logUuid, Long timestamp, Long tag) {
                super.onFinishSession(context, logUuid, timestamp, tag);

                TextView cigarettesToday = (TextView)findViewById(R.id.cigarettes_smoked);
                // Display all compass headings received
                cigarettesToday.setText(cigaretteCount + " Today");
            }
        };

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
