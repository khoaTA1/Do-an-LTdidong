package vn.ltdidong.apphoctienganh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.adapters.LeaderboardAdapter;
import vn.ltdidong.apphoctienganh.adapters.SocialPagerAdapter;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;
import vn.ltdidong.apphoctienganh.managers.SocialManager;
import vn.ltdidong.apphoctienganh.models.LeaderboardUser;

/**
 * Social Hub Activity - Trung tâm các tính năng xã hội
 * - Bạn bè
 * - Bảng xếp hạng
 * - Thách đấu
 */
public class SocialHubActivity extends AppCompatActivity {
    
    private ImageButton btnBack, btnSearch, btnAddFriend;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    
    private SocialManager socialManager;
    private String userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_hub);
        
        initViews();
        setupManagers();
        setupViewPager();
        setupListeners();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSearch = findViewById(R.id.btnSearch);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
    }
    
    private void setupManagers() {
        userId = SharedPreferencesManager.getInstance(this).getUserId();
        socialManager = new SocialManager(this);
    }
    
    private void setupViewPager() {
        SocialPagerAdapter adapter = new SocialPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("🏆 Global");
                    break;
                case 1:
                    tab.setText("👥 Friends");
                    break;
                case 2:
                    tab.setText("🔔 Activity");
                    break;
            }
        }).attach();
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchFriendsActivity.class);
            startActivity(intent);
        });
        
        btnAddFriend.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchFriendsActivity.class);
            startActivity(intent);
        });
    }
}
