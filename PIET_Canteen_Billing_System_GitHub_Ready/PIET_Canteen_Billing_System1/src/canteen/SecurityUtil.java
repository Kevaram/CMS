package canteen;

import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class SecurityUtil {
    private static final int ITERATIONS = 210000;
    private SecurityUtil() {}
    public static String hash(char[] password) {
        byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(derived);
    }
    public static boolean verify(char[] password, String stored) {
        if (stored == null || !stored.startsWith("pbkdf2$")) return false;
        try {
            String[] p=stored.split("\\$"); int rounds=Integer.parseInt(p[1]);
            byte[] salt=Base64.getDecoder().decode(p[2]), expected=Base64.getDecoder().decode(p[3]);
            return MessageDigest.isEqual(expected, derive(password,salt,rounds));
        } catch (RuntimeException ex) { return false; }
    }
    private static byte[] derive(char[] password, byte[] salt, int rounds) {
        PBEKeySpec spec=new PBEKeySpec(password,salt,rounds,256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (GeneralSecurityException e) { throw new IllegalStateException("Secure password hashing is unavailable",e); }
        finally { spec.clearPassword(); }
    }
}
