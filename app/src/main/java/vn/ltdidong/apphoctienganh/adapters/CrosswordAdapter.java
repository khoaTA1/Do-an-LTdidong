package vn.ltdidong.apphoctienganh.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.models.CrosswordRow;

public class CrosswordAdapter extends RecyclerView.Adapter<CrosswordAdapter.ViewHolder> {

    private Context context;
    private List<CrosswordRow> rowList;
    private OnRowClickListener listener;
    private int selectedRowIndex = -1;
    private int pivotColumn = 4; // Default
    private int totalColumns = 10; // Default

    public interface OnRowClickListener {
        void onRowClick(CrosswordRow row, int position);
    }

    public CrosswordAdapter(Context context, List<CrosswordRow> rowList, OnRowClickListener listener) {
        this.context = context;
        this.rowList = rowList;
        this.listener = listener;
    }

    public void setGridDimensions(int pivotColumn, int totalColumns) {
        this.pivotColumn = pivotColumn;
        this.totalColumns = totalColumns;
        notifyDataSetChanged();
    }

    public void setSelectedRowIndex(int index) {
        int previousIndex = this.selectedRowIndex;
        this.selectedRowIndex = index;
        if (previousIndex != -1)
            notifyItemChanged(previousIndex);
        if (selectedRowIndex != -1)
            notifyItemChanged(selectedRowIndex);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_crossword_row, parent, false);
        return new ViewHolder(view);
    }

    // Helper to create background with border
    private android.graphics.drawable.GradientDrawable createBoxBackground(int color) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        gd.setColor(color);
        gd.setStroke((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics()), 
                     Color.parseColor("#888888"));
        gd.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics()));
        return gd;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CrosswordRow row = rowList.get(position);

        holder.tvIndex.setText(String.valueOf(row.getIndex()));

        // Clear previous boxes
        holder.llCharContainer.removeAllViews();

        int wordLength = row.getWord().length();
        String currentInput = row.getCurrentInput();

        // Calculate dynamic box size
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int parentPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32,
                context.getResources().getDisplayMetrics()); // 16dp * 2
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                context.getResources().getDisplayMetrics()); // Reduce margin

        // Ensure totalColumns is at least 1 to avoid division by zero
        int cols = Math.max(totalColumns, 1);
        int availableWidth = screenWidth - parentPadding;
        int boxSize = (availableWidth / cols) - (margin * 2);

        // Min/Max constraints
        int minBoxSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, context.getResources().getDisplayMetrics());
        int maxBoxSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, context.getResources().getDisplayMetrics());
        
        if (boxSize < minBoxSize) boxSize = minBoxSize;
        if (boxSize > maxBoxSize) boxSize = maxBoxSize;

        // Alignment Logic:
        // Padding Left = (Pivot Column - Key Index) * (Box + Margin * 2)
        int keyIndex = row.getKeyIndex();

        if (keyIndex != -1) {
            int offsetBoxes = pivotColumn - keyIndex;
            if (offsetBoxes < 0)
                offsetBoxes = 0;

            int paddingLeft = offsetBoxes * (boxSize + (margin * 2));
            holder.llCharContainer.setPadding(paddingLeft, 0, 0, 0);
        } else {
            holder.llCharContainer.setPadding(0, 0, 0, 0);
        }

        // Highlight Selected Row
        if (position == selectedRowIndex) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E3F2FD")); // Light Blue
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        for (int i = 0; i < wordLength; i++) {
            TextView box = new TextView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(boxSize, boxSize);
            params.setMargins(margin, 0, margin, 0);
            box.setLayoutParams(params);

            box.setGravity(Gravity.CENTER);
            box.setTextSize(18);
            box.setTypeface(null, Typeface.BOLD);
            
            // Default styling
            int bgColor = Color.WHITE;
            int textColor = Color.BLACK;

            // Highlight Vertical Key Column
            if (i == keyIndex) {
                bgColor = Color.parseColor("#FFF59D"); // Yellow for key column
                textColor = Color.parseColor("#D84315"); // Deep Orange Text for contrast
            }

            // If solved, override background
            if (row.isSolved()) {
                bgColor = Color.parseColor("#C8E6C9"); // Light Green
                // Keep text color logic or reset? 
                // Let's keep key index text color distinct even if solved
                if (i != keyIndex) textColor = Color.BLACK; 
            }
            
            box.setBackground(createBoxBackground(bgColor));
            box.setTextColor(textColor);

            // If user has input logic or if revealed
            if (i < currentInput.length()) {
                box.setText(String.valueOf(currentInput.charAt(i)));
            } else {
                box.setText("");
            }

            holder.llCharContainer.addView(box);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRowClick(row, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rowList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex;
        LinearLayout llCharContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvIndex);
            llCharContainer = itemView.findViewById(R.id.llCharContainer);
        }
    }
}