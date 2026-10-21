package com.hrpayroll.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Precision baseline primitive #3: MD5 used ONLY to compute a weak ETag for
 * a read-only, non-sensitive reference-data listing (departments/policies),
 * purely so the browser/cache can detect "content unchanged". This is
 * NON-CRYPTOGRAPHIC - MD5 is not used for passwords, tokens, or signatures
 * anywhere in this app. Do NOT replace with SHA-256; collision resistance is
 * irrelevant for an HTTP caching hint.
 */
public final class ETagUtil {

    private ETagUtil() {
    }

    public static String weakETag(String content) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("W/\"");
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            sb.append('"');
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
