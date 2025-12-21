package vn.ltdidong.apphoctienganh.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.api.AiService;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.models.GeminiResponse;
import vn.ltdidong.apphoctienganh.models.GroqChatCompletionResponse;
import vn.ltdidong.apphoctienganh.models.WordEntry;
import vn.ltdidong.apphoctienganh.views.DrawingOverlayView;

public class CameraActivity extends AppCompatActivity {

    private static final int PERMISSION_CAMERA_CODE = 100;

    private PreviewView viewFinder;
    private ImageView ivPreview;
    private DrawingOverlayView drawingView;
    private TextView tvStatus;
    private ProgressBar progressBar;

    // UI for Result Card
    private CardView cardResult;
    private TextView tvEnglish;
    private TextView tvVietnamese;
    private ImageView btnFavorite;

    private Button btnCapture;
    private Button btnRetake;
    private ImageButton btnBack;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    private AiService aiService;

    private Bitmap currentBitmap;
    private FirebaseFirestore db;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        db = FirebaseFirestore.getInstance();

        viewFinder = findViewById(R.id.viewFinder);
        ivPreview = findViewById(R.id.ivPreview);
        drawingView = findViewById(R.id.drawingView);
        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progressBar);

        cardResult = findViewById(R.id.cardResult);
        tvEnglish = findViewById(R.id.tvEnglish);
        tvVietnamese = findViewById(R.id.tvVietnamese);
        btnFavorite = findViewById(R.id.btnFavorite);

        btnCapture = findViewById(R.id.btnCapture);
        btnRetake = findViewById(R.id.btnRetake);
        btnBack = findViewById(R.id.btnBack);

        aiService = AiService.getInstance();

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA },
                    PERMISSION_CAMERA_CODE);
        }

        btnCapture.setOnClickListener(v -> takePhoto());
        btnRetake.setOnClickListener(v -> resetCamera());
        btnBack.setOnClickListener(v -> finish());

        btnFavorite.setOnClickListener(v -> toggleFavorite());

        drawingView.setOnDrawListener(this::cropAndAnalyze);

        ivPreview.setOnClickListener(v -> {
            if (cardResult.getVisibility() == View.VISIBLE) {
                cardResult.setVisibility(View.GONE);
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                } catch (Exception exc) {
                    Log.e("CameraActivity", "Use case binding failed", exc);
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraActivity", "Camera provider error", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null)
            return;

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                currentBitmap = imageProxyToBitmap(image);
                image.close();

                runOnUiThread(() -> {
                    viewFinder.setVisibility(View.GONE);
                    ivPreview.setVisibility(View.VISIBLE);
                    ivPreview.setImageBitmap(currentBitmap);

                    drawingView.setVisibility(View.VISIBLE);
                    drawingView.clear();

                    btnCapture.setVisibility(View.GONE);
                    tvStatus.setVisibility(View.GONE);
                    cardResult.setVisibility(View.GONE);

                    btnBack.bringToFront();
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraActivity", "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    private void resetCamera() {
        viewFinder.setVisibility(View.VISIBLE);
        ivPreview.setVisibility(View.GONE);
        drawingView.setVisibility(View.GONE);
        btnCapture.setVisibility(View.VISIBLE);
        cardResult.setVisibility(View.GONE);
        tvStatus.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        btnBack.bringToFront();
        currentBitmap = null;
        isFavorite = false;
        updateFavoriteIcon();
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(image.getImageInfo().getRotationDegrees());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void cropAndAnalyze(RectF bounds) {
        if (currentBitmap == null)
            return;

        int viewWidth = ivPreview.getWidth();
        int viewHeight = ivPreview.getHeight();
        int bitmapWidth = currentBitmap.getWidth();
        int bitmapHeight = currentBitmap.getHeight();

        float scale;
        float dx = 0, dy = 0;

        if (viewWidth * bitmapHeight > viewHeight * bitmapWidth) {
            scale = (float) viewHeight / (float) bitmapHeight;
            dx = (viewWidth - bitmapWidth * scale) * 0.5f;
        } else {
            scale = (float) viewWidth / (float) bitmapWidth;
            dy = (viewHeight - bitmapHeight * scale) * 0.5f;
        }

        float left = (bounds.left - dx) / scale;
        float top = (bounds.top - dy) / scale;
        float right = (bounds.right - dx) / scale;
        float bottom = (bounds.bottom - dy) / scale;

        left = Math.max(0, left);
        top = Math.max(0, top);
        right = Math.min(bitmapWidth, right);
        bottom = Math.min(bitmapHeight, bottom);

        if (left >= right || top >= bottom) {
            drawingView.clear();
            return;
        }

        int x = (int) left;
        int y = (int) top;
        int width = (int) (right - left);
        int height = (int) (bottom - top);

        try {
            Bitmap croppedBitmap = Bitmap.createBitmap(currentBitmap, x, y, width, height);
            analyzeImage(croppedBitmap);
            drawingView.clear();
        } catch (IllegalArgumentException e) {
            drawingView.clear();
        }
    }

    private void analyzeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP);

        String prompt = "Identify the main object in this image. " +
                "Provide the answer in this format:\n" +
                "English: [Name]\n" +
                "Vietnamese: [Name]\n" +
                "Ensure only the names are returned.";

        tvStatus.setVisibility(View.GONE);
        cardResult.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE); // Show loading spinner

        aiService.generateGeminiVision(prompt, base64Image)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call,
                            Response<GeminiResponse> response) {
                        progressBar.setVisibility(View.GONE); // Hide loading spinner
                        if (response.isSuccessful() && response.body() != null) {
                            String result = response.body().getOutputText();
                            parseAndDisplayResult(result);
                        } else {
                            Log.e("CameraActivity", "Gemini API Error: " + response.code());
                            Toast.makeText(CameraActivity.this, "Lỗi từ Gemini API", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE); // Hide loading spinner
                        Log.e("CameraActivity", "Network Failure", t);
                        Toast.makeText(CameraActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void parseAndDisplayResult(String text) {
        String english = "Unknown";
        String vietnamese = "Không rõ";

        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().startsWith("english:")) {
                english = line.substring(line.indexOf(":") + 1).trim();
                english = english.replace("**", "");
            } else if (line.toLowerCase().startsWith("vietnamese:")) {
                vietnamese = line.substring(line.indexOf(":") + 1).trim();
                vietnamese = vietnamese.replace("**", "");
            }
        }

        tvEnglish.setText(english);
        tvVietnamese.setText(vietnamese);

        // Check if word is already favorited
        checkIfFavorite(english);

        tvStatus.setVisibility(View.GONE);
        cardResult.setVisibility(View.VISIBLE);
    }

    private void toggleFavorite() {
        String englishWord = tvEnglish.getText().toString();
        String vietnameseWord = tvVietnamese.getText().toString();

        if (englishWord.equals("Loading...") || englishWord.equals("Unknown"))
            return;

        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để lưu từ vựng", Toast.LENGTH_SHORT).show();
            return;
        }

        isFavorite = !isFavorite;
        updateFavoriteIcon();

        if (isFavorite) {
            saveToFavorites(userId, englishWord, vietnameseWord);
        } else {
            removeFromFavorites(userId, englishWord);
        }
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_heart_filled);
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.error)); // Red color
        } else {
            btnFavorite.setImageResource(R.drawable.ic_heart_outline);
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.primary));
        }
    }

    private void checkIfFavorite(String word) {
        String userId = SharedPreferencesManager.getInstance(this).getUserId();
        if (userId == null)
            return;

        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(word) // Check by ID (word itself) instead of field query
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isFavorite = documentSnapshot.exists();
                    updateFavoriteIcon();
                });
    }

    private void saveToFavorites(String userId, String word, String meaning) {
        // Tạo WordEntry để lưu cấu trúc giống như WishlistActivity đọc được
        WordEntry wordEntry = new WordEntry();
        wordEntry.setWord(word);

        // Tạo cấu trúc Meaning đơn giản để chứa nghĩa tiếng Việt
        WordEntry.Meaning meaningObj = new WordEntry.Meaning();
        meaningObj.setPartOfSpeech("noun"); // Giả định là noun

        WordEntry.Definition def = new WordEntry.Definition();
        def.setDefinition(meaning);

        meaningObj.setDefinitions(Collections.singletonList(def));
        wordEntry.setMeanings(Collections.singletonList(meaningObj));

        // Lưu vào Firestore với ID là từ tiếng Anh (để dễ quản lý và trùng khớp với
        // WishlistActivity)
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(word) // Dùng từ làm Document ID
                .set(wordEntry) // Dùng set thay vì add để override nếu tồn tại
                .addOnSuccessListener(aVoid -> Toast
                        .makeText(CameraActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(
                        e -> Toast.makeText(CameraActivity.this, "Lỗi khi lưu", Toast.LENGTH_SHORT).show());
    }

    private void removeFromFavorites(String userId, String word) {
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(word)
                .delete()
                .addOnSuccessListener(aVoid -> Toast
                        .makeText(CameraActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
