package vn.ltdidong.apphoctienganh.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import vn.ltdidong.apphoctienganh.R;

/**
 * Social Activity Feed Fragment
 * Hiển thị hoạt động của bạn bè
 */
public class SocialActivityFragment extends Fragment {
    
    private TextView tvComingSoon;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social_activity, container, false);
        
        tvComingSoon = view.findViewById(R.id.tvComingSoon);
        tvComingSoon.setText("🔔 Social Activity Feed\n\nComing soon:\n• See friends' achievements\n• Recent completions\n• Challenges\n• Activity updates");
        
        return view;
    }
}
