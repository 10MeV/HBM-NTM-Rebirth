package com.hbm.util;

/**
 * Legacy 1.7.10 package bridge for enum indexing helpers.
 */
@Deprecated(forRemoval = false)
public final class EnumUtil {
    private EnumUtil() {
    }

    public static <T extends Enum> T grabEnumSafely(Class<? extends Enum> enumType, int index) {
        return com.hbm.ntm.util.EnumUtil.grabEnumSafely(enumType, index);
    }
}
