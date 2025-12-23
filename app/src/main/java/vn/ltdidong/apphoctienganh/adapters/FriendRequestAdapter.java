package vn.ltdidong.apphoctienganh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.ltdidong.apphoctienganh.R;

/**
 * Adapter for friend requests list
 */
public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    
    private Context context;
    private List<Map<String, Object>> requests;
    private OnAcceptClickListener acceptListener;
    private OnRejectClickListener rejectListener;
    
    public interface OnAcceptClickListener {
        void onAcceptClick(Map<String, Object> request, int position);
    }
    
    public interface OnRejectClickListener {
        void onRejectClick(Map<String, Object> request, int position);
    }
    
    public FriendRequestAdapter(Context context, List<Map<String, Object>> requests) {
        this.context = context;
        this.requests = requests;
    }
    
    public void setOnAcceptClickListener(OnAcceptClickListener listener) {
        this.acceptListener = listener;
    }
    
    public void setOnRejectClickListener(OnRejectClickListener listener) {
        this.rejectListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> request = requests.get(position);
        
        String name = (String) request.get("fromUserName");
        String email = (String) request.get("fromUserEmail");
        Long level = (Long) request.get("level");
        Long timestamp = (Long) request.get("timestamp");
        
        holder.tvName.setText(name != null ? name : "Unknown User");
        holder.tvEmail.setText(email != null ? email : "");
        holder.tvLevel.setText("Level " + (level != null ? level : 1));
        
        if (timestamp != null) {
            String timeAgo = getTimeAgo(timestamp);
            holder.tvTime.setText(timeAgo);
        } else {
            holder.tvTime.setText("");
        }
        
        holder.btnAccept.setOnClickListener(v -> {
            if (acceptListener != null) {
                acceptListener.onAcceptClick(request, holder.getAdapterPosition());
            }
        });
        
        holder.btnReject.setOnClickListener(v -> {
            if (rejectListener != null) {
                rejectListener.onRejectClick(request, holder.getAdapterPosition());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return requests.size();
    }
    
    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvLevel, tvTime;
        Button btnAccept, btnReject;
        
        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvName);
            tvEmail = view.findViewById(R.id.tvEmail);
            tvLevel = view.findViewById(R.id.tvLevel);
            tvTime = view.findViewById(R.id.tvTime);
            btnAccept = view.findViewById(R.id.btnAccept);
            btnReject = view.findViewById(R.id.btnReject);
        }
    }
}
