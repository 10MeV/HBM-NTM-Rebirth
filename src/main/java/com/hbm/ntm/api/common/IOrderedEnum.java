package com.hbm.ntm.api.common;

/**
 * Legacy-name bridge for enums that expose a stable display/order array.
 */
@Deprecated(forRemoval = false)
public interface IOrderedEnum<T extends Enum<T>> extends OrderedEnum<T> {
}
