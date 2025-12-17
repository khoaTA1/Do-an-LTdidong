package vn.ltdidong.apphoctienganh.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import vn.ltdidong.apphoctienganh.R;

/**
 * Loading Dialog tùy chỉnh với ProgressBar và message
 */
public class LoadingDialog extends Dialog {

    private TextView tvMessage;
    private String message = "Loading...";

    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading);
        
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        
        tvMessage = findViewById(R.id.tv_loading_message);
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
    }

    public void setMessage(String message) {
        this.message = message;
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
    }
}
