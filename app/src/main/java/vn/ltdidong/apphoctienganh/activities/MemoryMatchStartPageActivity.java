package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.MMStartPageAdapter;

public class MemoryMatchStartPageActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MMStartPageAdapter modeAdapter;
    private LinearLayout dotsLayout;
    private Button btnStart;
    private ImageView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_match_start_page);

        viewPager = findViewById(R.id.viewPagerMode);
        dotsLayout = findViewById(R.id.dots);
        btnStart = findViewById(R.id.btnStart);

        modeAdapter = new MMStartPageAdapter(this);
        viewPager.setAdapter(modeAdapter);

        setupDots(2);
        selectDot(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectDot(position);
            }
        });

        btnStart.setOnClickListener(v -> openGame());
    }

    private void openGame() {

        int page = viewPager.getCurrentItem();
        Intent intent = new Intent(this, MemoryMatchActivity.class);

        if (page == 0) {
            // 1 người
            intent.putExtra("mode", 1);
            intent.putExtra("difficulty", modeAdapter.spDifficulty.getSelectedItemPosition());
        } else {
            // 2 người
            intent.putExtra("mode", 2);
            intent.putExtra("firstPlayer",
                    modeAdapter.spFirstPlayer.getSelectedItemPosition() == 0 ? 0 : 1);
        }

        startActivity(intent);
    }

    private void setupDots(int count) {
        dots = new ImageView[count];
        dotsLayout.removeAllViews();

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(R.drawable.dot_inactive);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    20, 20);
            params.setMargins(8, 0, 8, 0);
            dotsLayout.addView(dots[i], params);
        }
    }

    private void selectDot(int index) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageResource(i == index ?
                    R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }
}
