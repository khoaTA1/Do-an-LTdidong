package vn.ltdidong.apphoctienganh;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import vn.ltdidong.apphoctienganh.functions.DBHelper;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            FirebaseApp.initializeApp(this);
            Log.d("App", "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e("App", "Failed to initialize Firebase: " + e.getMessage());
        }

        // Xóa cache SQLite khi app khởi động
        try {
            DBHelper sqlite = new DBHelper(this);
            sqlite.clearAllTables();
            Log.d("App", "SQLite cache cleared on app start");
        } catch (Exception e) {
            Log.e("App", "Failed to clear SQLite cache", e);
        }

        // tạo channel notification, cần thiết cho foreground service
    }
}
