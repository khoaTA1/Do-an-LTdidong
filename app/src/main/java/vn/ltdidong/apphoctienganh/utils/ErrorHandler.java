package vn.ltdidong.apphoctienganh.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Utility class để xử lý và format error messages
 */
public class ErrorHandler {

    private static final String TAG = "ErrorHandler";

    /**
     * Lấy error message phù hợp từ Exception
     */
    public static String getErrorMessage(Context context, Exception e) {
        Log.e(TAG, "Error occurred: ", e);

        if (e == null) {
            return "Unknown error occurred";
        }

        // Network errors
        if (e instanceof UnknownHostException) {
            return "No internet connection. Please check your network.";
        }
        
        if (e instanceof SocketTimeoutException) {
            return "Connection timeout. Please try again.";
        }
        
        if (e instanceof IOException) {
            return "Network error. Please check your connection.";
        }

        // Firebase errors
        if (e instanceof FirebaseNetworkException) {
            return "Firebase network error. Please check your connection.";
        }
        
        if (e instanceof FirebaseTooManyRequestsException) {
            return "Too many requests. Please try again later.";
        }
        
        if (e instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) e;
            return getFirebaseAuthErrorMessage(authException);
        }

        // Default message
        String message = e.getMessage();
        return message != null && !message.isEmpty() 
            ? message 
            : "An unexpected error occurred";
    }

    /**
     * Xử lý Firebase Authentication errors
     */
    private static String getFirebaseAuthErrorMessage(FirebaseAuthException e) {
        String errorCode = e.getErrorCode();
        
        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":
                return "Invalid email address";
            case "ERROR_WRONG_PASSWORD":
                return "Wrong password";
            case "ERROR_USER_NOT_FOUND":
                return "User not found";
            case "ERROR_USER_DISABLED":
                return "This account has been disabled";
            case "ERROR_EMAIL_ALREADY_IN_USE":
                return "Email already in use";
            case "ERROR_WEAK_PASSWORD":
                return "Password is too weak";
            default:
                return e.getMessage() != null ? e.getMessage() : "Authentication error";
        }
    }

    /**
     * Log error với context
     */
    public static void logError(String tag, String message, Exception e) {
        Log.e(tag, message, e);
        // TODO: Send to Firebase Crashlytics
    }

    /**
     * Kiểm tra lỗi mạng
     */
    public static boolean isNetworkError(Exception e) {
        return e instanceof UnknownHostException
                || e instanceof SocketTimeoutException
                || e instanceof IOException
                || e instanceof FirebaseNetworkException;
    }

    /**
     * Kiểm tra lỗi authentication
     */
    public static boolean isAuthError(Exception e) {
        return e instanceof FirebaseAuthException;
    }
}
