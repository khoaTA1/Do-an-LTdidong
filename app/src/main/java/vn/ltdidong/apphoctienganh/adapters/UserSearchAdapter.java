package vn.ltdidong.apphoctienganh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import vn.ltdidong.apphoctienganh.R;

/**
 * Adapter cho User Search Results
 */
public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {
    
    private Context context;
    private List<Map<String, Object>> users;
    private OnAddFriendClickListener listener;
    
    public interface OnAddFriendClickListener {
        void onAddFriendClick(Map<String, Object> user);
    }
    
    public UserSearchAdapter(Context context, List<Map<String, Object>> users) {
        this.context = context;
        this.users = users;
    }
    
    public void setOnAddFriendClickListener(OnAddFriendClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> user = users.get(position);
        
        String username = (String) user.get("username");
        String email = (String) user.get("email");
        Object levelObj = user.get("level");
        int level = levelObj != null ? ((Long) levelObj).intValue() : 0;
        
        holder.tvUsername.setText(username != null ? username : "Unknown");
        holder.tvEmail.setText(email != null ? email : "");
        holder.tvLevel.setText("Level " + level);
        
        holder.btnAddFriend.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddFriendClick(user);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail, tvLevel;
        Button btnAddFriend;
        
        ViewHolder(View view) {
            super(view);
            tvUsername = view.findViewById(R.id.tvUsername);
            tvEmail = view.findViewById(R.id.tvEmail);
            tvLevel = view.findViewById(R.id.tvLevel);
            btnAddFriend = view.findViewById(R.id.btnAddFriend);
        }
    }
}
