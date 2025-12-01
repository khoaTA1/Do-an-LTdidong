package vn.ltdidong.apphoctienganh.functions;

import android.content.Context;
import android.content.SharedPreferences;

import vn.ltdidong.apphoctienganh.models.User;

/**
 * Quản lý SharedPreferences để lưu trữ dữ liệu local
 */
public class SharedPreferencesManager {
    private static final String PREF_NAME = "TaoStorePrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_ADDRESS = "user_address";
    private static final String KEY_USER_AVATAR = "user_avatar";
    private static final String KEY_USER_ROLE = "user_role";

    private static SharedPreferencesManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Lưu thông tin user sau khi đăng nhập
     */
    public void saveUserData(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        // Lưu userId dưới dạng String (Firebase document ID)
        editor.putString(KEY_USER_ID, String.valueOf(user.getId()));
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getFullName());
        editor.putString(KEY_USER_PHONE, user.getPhone());
        editor.putString(KEY_USER_ADDRESS, user.getAddress());
        editor.putString(KEY_USER_AVATAR, user.getAvatarUrl());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.apply();
    }

    /**
     * Kiểm tra xem user đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Lấy User ID (String - Firebase document ID)
     * Migration: Nếu userId cũ là Long, convert sang String
     */
    public String getUserId() {
        try {
            // Thử đọc như String trước
            return sharedPreferences.getString(KEY_USER_ID, null);
        } catch (ClassCastException e) {
            // Nếu lỗi, có nghĩa là dữ liệu cũ là Long, cần migrate
            try {
                long oldUserId = sharedPreferences.getLong(KEY_USER_ID, -1);
                if (oldUserId != -1) {
                    String newUserId = String.valueOf(oldUserId);
                    // Lưu lại dưới dạng String
                    sharedPreferences.edit()
                        .remove(KEY_USER_ID)
                        .putString(KEY_USER_ID, newUserId)
                        .apply();
                    return newUserId;
                }
            } catch (Exception ex) {
                // Ignore
            }
            return null;
        }
    }

    /**
     * Lấy email
     */
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Lấy tên user
     */
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    /**
     * Lấy số điện thoại
     */
    public String getUserPhone() {
        return sharedPreferences.getString(KEY_USER_PHONE, "");
    }

    /**
     * Lấy địa chỉ
     */
    public String getUserAddress() {
        return sharedPreferences.getString(KEY_USER_ADDRESS, "");
    }

    /**
     * Lấy avatar URL
     */
    public String getUserAvatar() {
        return sharedPreferences.getString(KEY_USER_AVATAR, "");
    }

    /**
     * Lấy role của user
     */
    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "user");
    }

    /**
     * Lưu role của user
     */
    public void saveUserRole(String role) {
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    /**
     * Lấy đối tượng User từ SharedPreferences
     */
    public User getUser() {
        if (!isLoggedIn()) {
            return null;
        }
        User user = new User();
        String userIdStr = getUserId();
        user.setId(userIdStr != null ? Integer.parseInt(userIdStr) : -1);
        user.setEmail(getUserEmail());
        user.setFullName(getUserName());
        user.setPhone(getUserPhone());
        user.setAddress(getUserAddress());
        user.setAvatarUrl(getUserAvatar());
        user.setRole(getUserRole());
        return user;
    }

    /**
     * Cập nhật thông tin user
     */
    public void updateUser(User user) {
        saveUserData(user);
    }

    /**
     * Đăng xuất - xóa thông tin user
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
    
    /**
     * Xóa dữ liệu user (alias của logout)
     */
    public void clearUserData() {
        logout();
    }

    /**
     * Lưu mật khẩu cho tài khoản (dùng cho đăng ký local)
     */
    public void savePassword(String email, String password) {
        editor.putString(email + "_password", password);
        editor.apply();
    }

    /**
     * Lấy mật khẩu đã lưu
     */
    public String getPassword(String email) {
        return sharedPreferences.getString(email + "_password", null);
    }
}
