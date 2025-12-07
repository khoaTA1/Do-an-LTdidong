package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.appbar.MaterialToolbar;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;

public class SettingsActivity extends AppCompatActivity {
    
    private MaterialToolbar toolbar;
    private SwitchCompat switchNotifications;
    private SwitchCompat switchSound;
    private SwitchCompat switchVibrate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        initViews();
        loadSettings();
        setupListeners();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchSound = findViewById(R.id.switchSound);
        switchVibrate = findViewById(R.id.switchVibrate);
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cài đặt");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void loadSettings() {
        SharedPreferencesManager prefs = SharedPreferencesManager.getInstance(this);
        
        switchNotifications.setChecked(prefs.getNotificationsEnabled());
        switchSound.setChecked(prefs.getSoundEnabled());
        switchVibrate.setChecked(prefs.getVibrateEnabled());
    }
    
    private void setupListeners() {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesManager.getInstance(this)
                .setNotificationsEnabled(isChecked);
        });
        
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesManager.getInstance(this)
                .setSoundEnabled(isChecked);
        });
        
        switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesManager.getInstance(this)
                .setVibrateEnabled(isChecked);
        });
    }
}
