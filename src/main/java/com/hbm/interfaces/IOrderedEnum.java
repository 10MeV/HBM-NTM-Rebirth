package com.hbm.interfaces;

/**
 * Legacy 1.7.10 package bridge for enums that expose a stable display/order array.
 */
@Deprecated(forRemoval = false)
public interface IOrderedEnum<T extends Enum<T>> extends com.hbm.ntm.api.common.IOrderedEnum<T> {
}
