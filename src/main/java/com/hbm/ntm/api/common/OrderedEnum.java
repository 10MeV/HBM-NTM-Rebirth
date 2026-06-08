package com.hbm.ntm.api.common;

public interface OrderedEnum<T extends Enum<T>> {
    T[] getOrder();
}
