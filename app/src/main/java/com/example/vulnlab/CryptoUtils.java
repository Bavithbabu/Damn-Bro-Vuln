package com.example.vulnlab;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    // VULN: Hardcoded static AES key (ECB mode will use it directly)
    private static final byte[] KEY_BYTES = new byte[]{
            0x01, 0x02, 0x03, 0x04,
            0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C,
            0x0D, 0x0E, 0x0F, 0x10
    };

    // VULN: Predictable "random" due to fixed seed
    private static final SecureRandom SR = new SecureRandom(new byte[]{1,2,3});

    // VULN: Uses AES/ECB/PKCS5Padding; deterministic and insecure against pattern analysis
    public static String encryptToBase64(String plaintext) throws Exception {
        SecretKeySpec key = new SecretKeySpec(KEY_BYTES, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // VULN: ECB mode
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(ct, Base64.NO_WRAP);
    }

    public static String decryptFromBase64(String b64) throws Exception {
        SecretKeySpec key = new SecretKeySpec(KEY_BYTES, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // VULN: ECB mode
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] pt = cipher.doFinal(Base64.decode(b64, Base64.NO_WRAP));
        return new String(pt, StandardCharsets.UTF_8);
    }

    // Not used; shows how SR could be misused to generate IVs/keys
    public static byte[] generatePredictableBytes(int len) {
        byte[] out = new byte[len];
        SR.nextBytes(out); // VULN: predictable due to fixed seed
        return out;
    }
}
