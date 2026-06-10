package com.hbm.ntm.util;

/**
 * Legacy-name facade for enum indexing helpers.
 */
@Deprecated(forRemoval = false)
public final class EnumUtil {
    private EnumUtil() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum> T grabEnumSafely(Class<? extends Enum> enumType, int index) {
        return (T) HbmEnumUtil.grabEnumSafely((Class) enumType, index);
    }
}
