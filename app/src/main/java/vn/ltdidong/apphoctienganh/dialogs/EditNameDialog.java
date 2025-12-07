package vn.ltdidong.apphoctienganh.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.functions.SharedPreferencesManager;

public class EditNameDialog extends Dialog {
    
    private EditText etNewName;
    private MaterialButton btnCancel, btnSave;
    private String currentName;
    private OnNameChangedListener listener;
    
    public interface OnNameChangedListener {
        void onNameChanged(String newName);
    }
    
    public EditNameDialog(@NonNull Context context, String currentName, OnNameChangedListener listener) {
        super(context);
        this.currentName = currentName;
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_name);
        
        etNewName = findViewById(R.id.etNewName);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        
        etNewName.setText(currentName);
        etNewName.setSelection(currentName.length());
        
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            String newName = etNewName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(getContext(), "Tên không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Update to Firebase
            String userId = SharedPreferencesManager.getInstance(getContext()).getUserId();
            if (userId != null) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("username", newName)
                    .addOnSuccessListener(aVoid -> {
                        SharedPreferencesManager.getInstance(getContext()).saveUserName(newName);
                        Toast.makeText(getContext(), "Đã cập nhật tên", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onNameChanged(newName);
                        }
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            }
        });
    }
}
