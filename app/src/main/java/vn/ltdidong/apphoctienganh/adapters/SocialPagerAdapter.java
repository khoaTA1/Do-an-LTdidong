package vn.ltdidong.apphoctienganh.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import vn.ltdidong.apphoctienganh.fragments.FriendRequestsFragment;
import vn.ltdidong.apphoctienganh.fragments.FriendsLeaderboardFragment;
import vn.ltdidong.apphoctienganh.fragments.GlobalLeaderboardFragment;
import vn.ltdidong.apphoctienganh.fragments.SocialActivityFragment;

/**
 * ViewPager Adapter cho Social Hub tabs
 */
public class SocialPagerAdapter extends FragmentStateAdapter {
    
    public SocialPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new GlobalLeaderboardFragment();
            case 1:
                return new FriendsLeaderboardFragment();
            case 2:
                return new FriendRequestsFragment();
            case 3:
                return new SocialActivityFragment();
            default:
                return new GlobalLeaderboardFragment();
        }
    }
    
    @Override
    public int getItemCount() {
        return 4;
    }
}
