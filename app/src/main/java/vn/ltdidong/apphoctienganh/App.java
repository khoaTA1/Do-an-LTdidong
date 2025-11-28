package vn.ltdidong.apphoctienganh;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import vn.ltdidong.apphoctienganh.functions.DBHelper;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        // Xóa cache SQLite khi app khởi động
        DBHelper sqlite = new DBHelper(this);
        try {
            sqlite.clearAllTables();
            Log.d("App", "SQLite cache cleared on app start");
        } catch (Exception e) {
            Log.e("App", "Failed to clear SQLite cache", e);
        }
    }
}
