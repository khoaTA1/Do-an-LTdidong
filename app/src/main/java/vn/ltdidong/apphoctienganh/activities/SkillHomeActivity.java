package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

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
                new String[]{"Luyện nói", "Chuyển đổi ngữ điệu", "Nói chuyện"});

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
                // Toast.makeText(this, "Open mode: " + modeName, Toast.LENGTH_SHORT).show(); // removed toast
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
                    startActivity(new Intent(this, ClozeTestActivity.class));
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
                } else if (mode.equals("Nói chuyện")) {
                    showTopicDialog();
                } else if (mode.equals("Chuyển đổi ngữ điệu")) {
                    startActivity(new Intent(this, IntonationActivity.class));
                }
                break;

            case "Writing":
                if (mode.equals("Viết câu")) {
                    startActivity(new Intent(this, WritingActivity.class));
                }
                break;
        }
    }

    private void showTopicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate custom layout
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_choose_topic, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // For rounded corners

        ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroupTopics);
        TextInputLayout inputLayoutTopic = dialogView.findViewById(R.id.inputLayoutTopic);
        EditText etTopicInput = dialogView.findViewById(R.id.etTopicInput);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnStart = dialogView.findViewById(R.id.btnStart);

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                Chip chip = group.findViewById(checkedId);
                String selected = chip.getText().toString();
                if (selected.equals("Other")) {
                    inputLayoutTopic.setVisibility(View.VISIBLE);
                    etTopicInput.requestFocus();
                } else {
                    inputLayoutTopic.setVisibility(View.GONE);
                    etTopicInput.setText(""); // Clear manual input
                }
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnStart.setOnClickListener(v -> {
            String topic = "";
            int checkedId = chipGroup.getCheckedChipId();
            
            if (checkedId != View.NO_ID) {
                Chip chip = chipGroup.findViewById(checkedId);
                String selected = chip.getText().toString();
                
                if (selected.equals("Other")) {
                    topic = etTopicInput.getText().toString().trim();
                    if (topic.isEmpty()) {
                        inputLayoutTopic.setError("Please enter a topic");
                        return;
                    }
                } else {
                    topic = selected;
                }
            } else {
                // Fallback if nothing selected (or enforce selection)
                // Here let's check manual input just in case
                topic = etTopicInput.getText().toString().trim();
                 if (topic.isEmpty()) {
                    Toast.makeText(SkillHomeActivity.this, "Please choose a topic", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Intent intent = new Intent(SkillHomeActivity.this, ConversationActivity.class);
            intent.putExtra("TOPIC", topic);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
    }


    // đặt nội dung chi tiết cho thanh tiến trình và thông tin cơ bản của người dùng
    private void setupProgressAndBasicUserInfo() {
        TextView textUserFullname = findViewById(R.id.user_fullname);
        textUserFullname.setText(SharedPreferencesManager.getInstance(this).getUserName());
    }
}
