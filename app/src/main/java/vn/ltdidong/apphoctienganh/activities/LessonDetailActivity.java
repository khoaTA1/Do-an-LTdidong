package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.ListeningLesson;
import vn.ltdidong.apphoctienganh.viewmodel.ListeningViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Locale;

/**
 * Activity hiển thị chi tiết bài học và phát audio
 * Cho phép người dùng nghe audio trước khi làm bài quiz
 */
public class LessonDetailActivity extends AppCompatActivity {
    
    private ListeningViewModel viewModel;
    private ListeningLesson currentLesson;
    private int lessonId;
    
    // UI Components
    private TextView tvLessonTitle;
    private TextView tvLessonDescription;
    private Chip chipDifficulty;
    private TextView tvDuration;
    private TextView tvQuestionCount;
    private SeekBar seekBarAudio;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private ImageButton btnBackward;
    private FloatingActionButton btnPlayPause;
    private ImageButton btnForward;
    private Button btnReplay;
    private Button btnStartLesson;
    
    // Media Player
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private boolean isPlaying = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);
        
        // Get lesson ID từ Intent
        lessonId = getIntent().getIntExtra("LESSON_ID", -1);
        if (lessonId == -1) {
            Toast.makeText(this, "Error: Lesson not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ListeningViewModel.class);
        
        // Initialize views
        initViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Load lesson data
        loadLessonData();
        
        // Setup audio player
        setupAudioPlayer();
        
        // Setup buttons
        setupButtons();
    }
    
    /**
     * Khởi tạo các views
     */
    private void initViews() {
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        tvLessonDescription = findViewById(R.id.tvLessonDescription);
        chipDifficulty = findViewById(R.id.chipDifficulty);
        tvDuration = findViewById(R.id.tvDuration);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        seekBarAudio = findViewById(R.id.seekBarAudio);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        btnBackward = findViewById(R.id.btnBackward);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnForward = findViewById(R.id.btnForward);
        btnReplay = findViewById(R.id.btnReplay);
        btnStartLesson = findViewById(R.id.btnStartLesson);
    }
    
    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    /**
     * Load thông tin bài học từ database
     */
    private void loadLessonData() {
        viewModel.getLessonById(lessonId).observe(this, lesson -> {
            if (lesson != null) {
                currentLesson = lesson;
                displayLessonInfo(lesson);
                
                // Set toolbar title
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(lesson.getTitle());
                }
            }
        });
    }
    
    /**
     * Hiển thị thông tin bài học lên UI
     */
    private void displayLessonInfo(ListeningLesson lesson) {
        tvLessonTitle.setText(lesson.getTitle());
        tvLessonDescription.setText(lesson.getDescription());
        tvDuration.setText(lesson.getFormattedDuration());
        tvQuestionCount.setText(String.valueOf(lesson.getQuestionCount()));
        
        // Set difficulty chip
        chipDifficulty.setText(lesson.getDifficulty());
        int difficultyColor = getDifficultyColor(lesson.getDifficulty());
        chipDifficulty.setChipBackgroundColorResource(difficultyColor);
    }
    
    /**
     * Setup Audio Player với MediaPlayer
     * Note: Trong production, nên dùng ExoPlayer thay vì MediaPlayer
     */
    private void setupAudioPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
            new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        );
        
        // Prepare listener
        mediaPlayer.setOnPreparedListener(mp -> {
            // Khi audio đã sẵn sàng
            int duration = mp.getDuration();
            seekBarAudio.setMax(duration);
            tvTotalTime.setText(formatTime(duration));
            tvCurrentTime.setText("0:00");
        });
        
        // Completion listener
        mediaPlayer.setOnCompletionListener(mp -> {
            // Khi audio phát xong
            isPlaying = false;
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            seekBarAudio.setProgress(0);
            tvCurrentTime.setText("0:00");
        });
        
        // Error listener
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, getString(R.string.audio_error), Toast.LENGTH_SHORT).show();
            return true;
        });
        
        // Setup SeekBar change listener
        seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Load audio - trong demo này dùng sample audio
        // Trong production, load từ currentLesson.getAudioUrl()
        loadAudio();
    }
    
    /**
     * Load audio file
     * Note: Đây là demo, trong thực tế cần load từ URL hoặc raw resource
     */
    private void loadAudio() {
        try {
            // Demo: Sử dụng một audio mặc định từ system
            // Trong production, thay bằng:
            // if (audioUrl.startsWith("http")) {
            //     mediaPlayer.setDataSource(audioUrl);
            // } else {
            //     mediaPlayer.setDataSource(context, Uri.parse(audioUrl));
            // }
            
            // Tạm thời disable audio player vì không có file audio thật
            // mediaPlayer.prepareAsync();
            
            Toast.makeText(this, "Audio player ready (Demo mode)", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.audio_error), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Setup các buttons
     */
    private void setupButtons() {
        // Play/Pause button
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        
        // Backward button (lùi 10s)
        btnBackward.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int newPosition = Math.max(0, currentPosition - 10000); // Lùi 10 giây
                mediaPlayer.seekTo(newPosition);
            }
        });
        
        // Forward button (tua 10s)
        btnForward.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                int newPosition = Math.min(duration, currentPosition + 10000); // Tua 10 giây
                mediaPlayer.seekTo(newPosition);
            }
        });
        
        // Replay button (phát lại từ đầu)
        btnReplay.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(0);
                if (!isPlaying) {
                    togglePlayPause();
                }
            }
        });
        
        // Start Lesson button (bắt đầu làm bài quiz)
        btnStartLesson.setOnClickListener(v -> {
            // Pause audio nếu đang phát
            if (isPlaying) {
                togglePlayPause();
            }
            
            // Mở QuizActivity
            Intent intent = new Intent(LessonDetailActivity.this, QuizActivity.class);
            intent.putExtra("LESSON_ID", lessonId);
            startActivity(intent);
        });
    }
    
    /**
     * Toggle play/pause
     */
    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        
        if (isPlaying) {
            // Pause
            mediaPlayer.pause();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            isPlaying = false;
            handler.removeCallbacks(updateSeekBarRunnable);
        } else {
            // Play
            mediaPlayer.start();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            isPlaying = true;
            updateSeekBar();
        }
    }
    
    /**
     * Runnable để update SeekBar progress
     */
    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBarAudio.setProgress(currentPosition);
                tvCurrentTime.setText(formatTime(currentPosition));
                handler.postDelayed(this, 100); // Update mỗi 100ms
            }
        }
    };
    
    /**
     * Bắt đầu update SeekBar
     */
    private void updateSeekBar() {
        handler.post(updateSeekBarRunnable);
    }
    
    /**
     * Format thời gian từ milliseconds thành MM:SS
     */
    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }
    
    /**
     * Lấy màu theo độ khó
     */
    private int getDifficultyColor(String difficulty) {
        switch (difficulty.toUpperCase()) {
            case "EASY":
                return R.color.difficulty_easy;
            case "MEDIUM":
                return R.color.difficulty_medium;
            case "HARD":
                return R.color.difficulty_hard;
            default:
                return R.color.primary;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release media player resources
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBarRunnable);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
