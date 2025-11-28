package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;

public class SkillHomeActivity extends AppCompatActivity {
    private TextView speakingTitle, writingTitle, loginRedirect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skills_page);

        speakingTitle = findViewById(R.id.title_reading_skill);
        writingTitle = findViewById(R.id.title_writing_skill);
        loginRedirect = findViewById(R.id.login_redirect);

        if (SharedPreferencesManager.getInstance(this).isLoggedIn()) {
            setVisibility(true);

            setOnListenerWhenLogged();
        } else {
            setVisibility(false);

            setOnListenerWhenNotLogged();
        }
    }

    private void setVisibility(boolean isLogged) {
        findViewById(R.id.reading_skill_card).setVisibility(isLogged ? View.VISIBLE : View.GONE);
        findViewById(R.id.speaking_skill_card).setVisibility(isLogged ? View.VISIBLE : View.GONE);
        findViewById(R.id.writing_skill_card).setVisibility(isLogged ? View.VISIBLE : View.GONE);

        findViewById(R.id.login_require_card).setVisibility(isLogged ? View.GONE : View.VISIBLE);
    }
    private void setOnListenerWhenLogged() {
        speakingTitle.setOnClickListener(v -> {
            Intent intent = new Intent(SkillHomeActivity.this, SpeakingActivity.class);
            startActivity(intent);
            finish();
        });

        writingTitle.setOnClickListener(v -> {
            Intent intent = new Intent(SkillHomeActivity.this, WritingActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setOnListenerWhenNotLogged() {
        loginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(SkillHomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
