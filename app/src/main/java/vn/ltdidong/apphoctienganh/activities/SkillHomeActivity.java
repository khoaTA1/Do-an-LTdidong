package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;

public class SkillHomeActivity extends AppCompatActivity {
    private TextView loginRedirect;
    private MaterialCardView speakingSkill, writingSkill, listeningSkill, readingSkill;
    private BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skills_page);

        speakingSkill = findViewById(R.id.speakingSkillCard);
        writingSkill = findViewById(R.id.writingSkillCard);
        listeningSkill = findViewById(R.id.listeningSkillCard);
        readingSkill = findViewById(R.id.readingSkillCard);

        loginRedirect = findViewById(R.id.login_redirect);

        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_skills);

        if (SharedPreferencesManager.getInstance(this).isLoggedIn()) {
            setVisibility(true);

            setupProgressAndBasicUserInfo();
            setupSkillSections();

            setOnListenerWhenLogged();
        } else {
            setVisibility(false);

            setOnListenerWhenNotLogged();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(SkillHomeActivity.this, MainActivity.class);
                startActivity(intent);

                return  true;
            } else if (id == R.id.nav_skills) {

                return true;
            } else if (id == R.id.nav_advance_mode) {
                Intent intent = new Intent(SkillHomeActivity.this, AdvanceModeHomeActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(SkillHomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    private void setVisibility(boolean isLogged) {
        findViewById(R.id.user_header_card).setVisibility(isLogged ? View.VISIBLE : View.GONE);
        findViewById(R.id.skillList).setVisibility(isLogged ? View.VISIBLE : View.GONE);

        findViewById(R.id.login_require_overlay).setVisibility(isLogged ? View.GONE : View.VISIBLE);
    }
    private void setOnListenerWhenLogged() {
        speakingSkill.setOnClickListener(v -> {
            Intent intent = new Intent(SkillHomeActivity.this, SpeakingActivity.class);
            startActivity(intent);
            finish();
        });

        writingSkill.setOnClickListener(v -> {
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

    // đặt nội dung chi tiết cho danh sách kĩ năng ngoài layout
    private void setupSkillSections() {
        setupSkill(readingSkill.getId(), "Reading", R.drawable.ic_book,
                new String[]{"Đọc hiểu", "Điền khuyết"});

        setupSkill(listeningSkill.getId(), "Listening", R.drawable.ic_listening,
                new String[]{"Nghe cơ bản"});

        setupSkill(speakingSkill.getId(), "Speaking", R.drawable.ic_speaking,
                new String[]{"Luyện nói"});

        setupSkill(writingSkill.getId(), "Writing", R.drawable.ic_writing,
                new String[]{"Viết câu"});
    }

    private void setupSkill(int skillId, String title, int iconRes, String[] modes) {
        View skillCard = findViewById(skillId);

        ImageView icon = skillCard.findViewById(R.id.skill_icon);
        TextView titleView = skillCard.findViewById(R.id.skill_title);
        LinearLayout modeContainer = skillCard.findViewById(R.id.mode_container);

        icon.setImageResource(iconRes);
        titleView.setText(title);

        modeContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (String modeName : modes) {
            View modeItem = inflater.inflate(R.layout.item_mode_card, modeContainer, false);
            TextView modeText = modeItem.findViewById(R.id.mode_name);
            modeText.setText(modeName);

            // Xử lý click
            modeItem.setOnClickListener(v -> {
                Toast.makeText(this, "Open mode: " + modeName, Toast.LENGTH_SHORT).show();
                handleModeClick(title, modeName);
            });

            modeContainer.addView(modeItem);
        }
    }

    private void handleModeClick(String skill, String mode) {
        switch (skill) {
            case "Reading":
                if (mode.equals("Đọc hiểu")) {
                    // mở activity Đọc hiểu
                    startActivity(new Intent(this, RCTransActivity.class));
                } else if (mode.equals("Điền khuyết")) {
                    // mở activity Điền khuyết
                    startActivity(new Intent(this, CTTransActivity.class));
                }
                break;

            case "Listening":
                if (mode.equals("Nghe cơ bản")) {
                    startActivity(new Intent(this, ListeningListActivity.class));
                }
                break;

            case "Speaking":
                if (mode.equals("Luyện nói")) {
                    startActivity(new Intent(this, SpeakingActivity.class));
                }
                break;

            case "Writing":
                if (mode.equals("Viết câu")) {
                    startActivity(new Intent(this, WritingActivity.class));
                }
                break;
        }
    }


    // đặt nội dung chi tiết cho thanh tiến trình và thông tin cơ bản của người dùng
    private void setupProgressAndBasicUserInfo() {
        TextView textUserFullname = findViewById(R.id.user_fullname);
        textUserFullname.setText(SharedPreferencesManager.getInstance(this).getUserName());
    }
}
