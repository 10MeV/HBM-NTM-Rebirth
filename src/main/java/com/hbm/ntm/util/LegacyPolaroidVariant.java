package com.hbm.ntm.util;

import java.util.Random;

/**
 * Process-local 1.7.10 {@code MainRegistry.polaroidID} selection.
 *
 * <p>The source rerolled this once during pre-initialization, selecting an ID
 * from 1 through 18 except 4 and 9.  It was not world data or a synchronized
 * gameplay setting, so this intentionally remains a bootstrap-time process
 * value in the modern port.</p>
 */
public final class LegacyPolaroidVariant {
    public static final int P11 = 11;

    private static final int ACTIVE_ID = roll(new Random());

    /** Forces source-timed class initialization during mod construction. */
    public static void bootstrap() {
        // Reading the field deliberately triggers class initialization.
        int ignored = ACTIVE_ID;
    }

    public static int activeId() {
        return ACTIVE_ID;
    }

    public static boolean isActive(int variantId) {
        return ACTIVE_ID == variantId;
    }

    static int roll(Random random) {
        int id;
        do {
            id = random.nextInt(18) + 1;
        } while (id == 4 || id == 9);
        return id;
    }

    private LegacyPolaroidVariant() {
    }
}
