package vn.ltdidong.apphoctienganh.activities;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import vn.ltdidong.apphoctienganh.R;

public class HelpActivity extends AppCompatActivity {
    
    private MaterialToolbar toolbar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Trợ giúp & Hỗ trợ");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
