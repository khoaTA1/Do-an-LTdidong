package vn.ltdidong.apphoctienganh.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;

public class DailyGoalDialog extends Dialog {
    
    private RadioGroup radioGroup;
    private MaterialButton btnCancel, btnSave;
    private OnGoalChangedListener listener;
    
    public interface OnGoalChangedListener {
        void onGoalChanged(String goal);
    }
    
    public DailyGoalDialog(@NonNull Context context, OnGoalChangedListener listener) {
        super(context);
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_daily_goal);
        
        radioGroup = findViewById(R.id.radioGroupGoal);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        
        // Load current goal
        String currentGoal = SharedPreferencesManager.getInstance(getContext())
            .getDailyGoal();
        
        // Select current goal
        if (currentGoal.equals("10 phút/ngày")) {
            radioGroup.check(R.id.radioGoal10);
        } else if (currentGoal.equals("15 phút/ngày")) {
            radioGroup.check(R.id.radioGoal15);
        } else if (currentGoal.equals("20 phút/ngày")) {
            radioGroup.check(R.id.radioGoal20);
        } else if (currentGoal.equals("30 phút/ngày")) {
            radioGroup.check(R.id.radioGoal30);
        }
        
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            RadioButton selectedRadio = findViewById(selectedId);
            String newGoal = selectedRadio.getText().toString();
            
            // Save to SharedPreferences
            SharedPreferencesManager.getInstance(getContext())
                .setDailyGoal(newGoal);
            
            Toast.makeText(getContext(), "Đã cập nhật mục tiêu", Toast.LENGTH_SHORT).show();
            
            if (listener != null) {
                listener.onGoalChanged(newGoal);
            }
            dismiss();
        });
    }
}
