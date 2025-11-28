package vn.ltdidong.apphoctienganh.functions;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {

    private static final int ITERATIONS = 65536; // số lần lặp
    private static final int KEY_LENGTH = 256;   // độ mạnh (bit)

    // Tạo salt ngẫu nhiên
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    // Băm mật khẩu + salt
    public static String hashPassword(String password, String salt) {
        try {
            byte[] saltBytes = Base64.decode(salt, Base64.NO_WRAP);

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    saltBytes,
                    ITERATIONS,
                    KEY_LENGTH
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.encodeToString(hash, Base64.NO_WRAP);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi băm mật khẩu", e);
        }
    }

    // So sánh mật khẩu khi đăng nhập
    public static boolean verifyPassword(String inputPassword,
                                         String storedHash,
                                         String storedSalt) {

        String inputHash = hashPassword(inputPassword, storedSalt);
        return inputHash.equals(storedHash);
    }
}