package com.hrpayroll.util;

import java.util.Random;

/**
 * Precision baseline primitive #2: java.util.Random used ONLY to generate a
 * cache-busting query-string suffix for a static asset URL. This is
 * NON-CRYPTOGRAPHIC by design - it exists purely so browsers refetch changed
 * CSS/JS. Do NOT replace with SecureRandom; that would be pointless here
 * since the value carries no security meaning.
 */
public final class AssetVersion {

    private static final Random RANDOM = new Random();
    private static final int SUFFIX = RANDOM.nextInt(900000) + 100000;

    private AssetVersion() {
    }

    public static String suffix() {
        return String.valueOf(SUFFIX);
    }
}
