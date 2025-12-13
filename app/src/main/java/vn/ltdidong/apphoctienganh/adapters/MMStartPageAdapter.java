package vn.ltdidong.apphoctienganh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.ltdidong.apphoctienganh.R;

public class MMStartPageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int MODE_SINGLE = 0;
    public static final int MODE_TWO = 1;

    private Context context;

    public Spinner spDifficulty;
    public Spinner spFirstPlayer;

    public MMStartPageAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return 2; // 2 slide
    }

    @Override
    public int getItemViewType(int position) {
        return position; // 0 = single, 1 = two
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MODE_SINGLE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_slide_single_player, parent, false);
            return new SingleViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_slide_two_player, parent, false);
            return new TwoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {}

    class SingleViewHolder extends RecyclerView.ViewHolder {
        public SingleViewHolder(@NonNull View itemView) {
            super(itemView);
            spDifficulty = itemView.findViewById(R.id.spDifficulty);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{"Dễ (8 thẻ)", "Vừa (12 thẻ)", "Khó (16 thẻ)"}
            );
            spDifficulty.setAdapter(adapter);
        }
    }

    class TwoViewHolder extends RecyclerView.ViewHolder {
        public TwoViewHolder(@NonNull View itemView) {
            super(itemView);
            spFirstPlayer = itemView.findViewById(R.id.spFirstPlayer);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{"Player 1", "Player 2"}
            );
            spFirstPlayer.setAdapter(adapter);
        }
    }
}

