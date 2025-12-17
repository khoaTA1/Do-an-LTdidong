package vn.ltdidong.apphoctienganh.base;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import vn.ltdidong.apphoctienganh.R;
import vn.ltdidong.apphoctienganh.dialogs.LoadingDialog;
import vn.ltdidong.apphoctienganh.utils.ErrorHandler;

/**
 * Base Activity cho tất cả các Activity trong app
 * Cung cấp các chức năng chung: loading, error handling, toolbar setup
 */
public abstract class BaseActivity extends AppCompatActivity {

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Setup toolbar với back button
     */
    protected void setupToolbar(Toolbar toolbar, String title, boolean showBackButton) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
                if (showBackButton) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Hiển thị loading dialog
     */
    protected void showLoading(String message) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    protected void showLoading() {
        showLoading("Loading...");
    }

    /**
     * Ẩn loading dialog
     */
    protected void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    /**
     * Hiển thị Toast message
     */
    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void showToastLong(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Hiển thị Snackbar
     */
    protected void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    protected void showSnackbarLong(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Hiển thị Error Dialog
     */
    protected void showErrorDialog(String title, String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setIcon(R.drawable.ic_error)
                .show();
    }

    /**
     * Hiển thị Confirmation Dialog
     */
    protected void showConfirmDialog(String title, String message, 
                                    OnConfirmListener listener) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    if (listener != null) {
                        listener.onConfirm();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Xử lý lỗi chung
     */
    protected void handleError(Exception e) {
        hideLoading();
        String errorMessage = ErrorHandler.getErrorMessage(this, e);
        showErrorDialog("Error", errorMessage);
    }

    /**
     * Xử lý lỗi mạng
     */
    protected void handleNetworkError() {
        hideLoading();
        showSnackbar("No internet connection. Please check your network.");
    }

    /**
     * Interface cho Confirm Dialog
     */
    public interface OnConfirmListener {
        void onConfirm();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
