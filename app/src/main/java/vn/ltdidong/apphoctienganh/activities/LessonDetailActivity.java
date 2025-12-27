package vn.ltdidong.apphoctienganh.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.io.IOException;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.viewmodel.ListeningViewModel;

/**
 * Activity hiển thị chi tiết bài học
 * Cho phép nghe audio, xem thông tin bài học và bắt đầu quiz
 */
public class LessonDetailActivity extends AppCompatActivity {
    
    private ListeningViewModel viewModel;
    private ListeningLesson lesson;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    
    // UI components
    private Toolbar toolbar;
    private ImageView ivLessonImage;
    private TextView tvLessonTitle;
    private TextView tvLessonDescription;
    private Chip chipDifficulty;
    private TextView tvDuration;
    private TextView tvQuestionCount;
    private ImageButton btnPlayPause;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private MaterialButton btnStartQuiz;
    private MaterialButton btnBack;

    private boolean isPlaying = false;
    private boolean isMediaPlayerInitialized = false;
    private int lessonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ListeningViewModel.class);

        // Lấy lesson ID từ Intent
        lessonId = getIntent().getIntExtra("lesson_id", -1);
        if (lessonId == -1) {
            Toast.makeText(this, "Error loading lesson", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Load lesson data
        loadLessonData();

        // Setup audio controls
        setupAudioControls();

        // Setup buttons
        setupButtons();
    }

    /**
     * Khởi tạo tất cả views
     */
    @SuppressLint("WrongViewCast")
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ivLessonImage = findViewById(R.id.ivLessonImage);
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        tvLessonDescription = findViewById(R.id.tvLessonDescription);
        chipDifficulty = findViewById(R.id.chipDifficulty);
        tvDuration = findViewById(R.id.tvDuration);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnForward = findViewById(R.id.btnForward);
        btnBackward = findViewById(R.id.btnBackward);
        seekBar = findViewById(R.id.seekBarAudio);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        btnStartQuiz = findViewById(R.id.btnStartLesson);
    }

    /**
     * Load data bài học từ database
     */
    private void loadLessonData() {
        viewModel.getLessonById(lessonId).removeObservers(this);
        viewModel.getLessonById(lessonId).observe(this, loadedLesson -> {
            if (loadedLesson != null && !isMediaPlayerInitialized) {
                lesson = loadedLesson;
                displayLessonInfo();
                prepareMediaPlayer();
                isMediaPlayerInitialized = true;
            }
        });
    }

    /**
     * Hiển thị thông tin bài học lên UI
     */
    private void displayLessonInfo() {
        tvLessonTitle.setText(lesson.getTitle());
        tvLessonDescription.setText(lesson.getDescription());
        chipDifficulty.setText(lesson.getDifficulty());

        // Set màu cho chip theo độ khó
        switch (lesson.getDifficulty()) {
            case "EASY":
                chipDifficulty.setChipBackgroundColorResource(R.color.difficulty_easy);
                break;
            case "MEDIUM":
                chipDifficulty.setChipBackgroundColorResource(R.color.difficulty_medium);
                break;
            case "HARD":
                chipDifficulty.setChipBackgroundColorResource(R.color.difficulty_hard);
                break;
        }

        tvDuration.setText(lesson.getFormattedDuration());
        tvQuestionCount.setText(lesson.getQuestionCount() + " questions");
        tvTotalTime.setText(lesson.getFormattedDuration());

        // Set image (nếu có)
        // Picasso.get().load(lesson.getImageUrl()).into(ivLessonImage);
    }

    /**
     * Chuẩn bị MediaPlayer để phát audio
     */
    private void prepareMediaPlayer() {
        // Release MediaPlayer cũ nếu có
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            // Kiểm tra xem audioUrl có phải raw resource không
            String audioUrl = lesson.getAudioUrl();
            if (audioUrl != null && audioUrl.startsWith("raw://")) {
                // Load từ raw resource
                String resourceName = audioUrl.substring(6);
                int resourceId = getResources().getIdentifier(resourceName, "raw", getPackageName());
                if (resourceId != 0) {
                    Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
                    mediaPlayer.setDataSource(this, uri);
                } else {
                    // Resource không tồn tại, hiển thị lỗi
                    Toast.makeText(this, "Audio file not found: " + resourceName, Toast.LENGTH_LONG).show();
                    btnPlayPause.setEnabled(false);
                    return;
                }
            } else if (audioUrl != null && !audioUrl.isEmpty()) {
                // Load từ URL hoặc tên resource trực tiếp
                int resourceId = getResources().getIdentifier(audioUrl, "raw", getPackageName());
                if (resourceId != 0) {
                    Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
                    mediaPlayer.setDataSource(this, uri);
                } else {
                    // Thử load như URL
                    mediaPlayer.setDataSource(audioUrl);
                }
            } else {
                Toast.makeText(this, "No audio available", Toast.LENGTH_SHORT).show();
                btnPlayPause.setEnabled(false);
                return;
            }

            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                seekBar.setMax(mediaPlayer.getDuration());
                btnPlayPause.setEnabled(true);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnPlayPause.setImageResource(R.drawable.ic_play);
                seekBar.setProgress(0);
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading audio", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Setup các audio controls (play, pause, forward, backward, seekbar)
     */
    private void setupAudioControls() {
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer == null) return;

            if (isPlaying) {
                pauseAudio();
            } else {
                playAudio();
            }
        });

        btnForward.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int newPosition = currentPosition + 10000; // Forward 10 seconds
                if (newPosition < mediaPlayer.getDuration()) {
                    mediaPlayer.seekTo(newPosition);
                } else {
                    mediaPlayer.seekTo(mediaPlayer.getDuration());
                }
            }
        });

        btnBackward.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int newPosition = currentPosition - 10000; // Backward 10 seconds
                if (newPosition > 0) {
                    mediaPlayer.seekTo(newPosition);
                } else {
                    mediaPlayer.seekTo(0);
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
                tvCurrentTime.setText(formatTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Update seekbar progress
        updateSeekBar();
    }

    /**
     * Phát audio
     */
    private void playAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPlaying = true;
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        }
    }

    /**
     * Tạm dừng audio
     */
    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    /**
     * Cập nhật seekbar position liên tục
     */
    private void updateSeekBar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition));
                }
                handler.postDelayed(this, 100);
            }
        }, 100);
    }

    /**
     * Format time từ milliseconds sang MM:SS
     */
    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Setup các buttons
     */
    private void setupButtons() {
        btnStartQuiz.setOnClickListener(v -> {
            // Dừng audio nếu đang phát
            if (isPlaying) {
                pauseAudio();
            }

            // Chuyển sang QuizActivity
            Intent intent = new Intent(LessonDetailActivity.this, QuizActivity.class);
            intent.putExtra("lesson_id", lessonId);
            
            // Truyền thêm thông tin từ daily challenge nếu có
            if (getIntent().hasExtra("from_daily_challenge")) {
                intent.putExtra("from_daily_challenge", true);
                intent.putExtra("challenge_type", getIntent().getStringExtra("challenge_type"));
                intent.putExtra("challenge_xp", getIntent().getIntExtra("challenge_xp", 15));
                intent.putExtra("lesson_difficulty", lesson != null ? lesson.getDifficulty() : "EASY");
                startActivityForResult(intent, 200);
            } else {
                startActivity(intent);
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            // Nhận kết quả từ QuizActivity và truyền tiếp cho DailyChallengeActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("challenge_type", data.getStringExtra("challenge_type"));
            resultIntent.putExtra("xp_earned", data.getIntExtra("xp_earned", 15));
            resultIntent.putExtra("lesson_difficulty", data.getStringExtra("lesson_difficulty"));
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
        isMediaPlayerInitialized = false;
        handler.removeCallbacksAndMessages(null);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (isPlaying) {
            pauseAudio();
        }
    }
}
