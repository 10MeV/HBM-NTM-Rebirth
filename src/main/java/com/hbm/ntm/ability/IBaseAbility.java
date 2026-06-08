package com.hbm.ntm.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface IBaseAbility extends Comparable<IBaseAbility> {
    String getName();

    default String getExtension(int level) {
        return "";
    }

    default MutableComponent getFullName(int level) {
        MutableComponent name = getName().isEmpty() ? Component.empty() : Component.translatable(getName());
        String extension = getExtension(level);
        return extension.isEmpty() ? name : name.append(Component.literal(extension));
    }

    default boolean isAllowed() {
        return true;
    }

    default int levels() {
        return 1;
    }

    default int sortOrder() {
        return hashCode();
    }

    @Override
    default int compareTo(IBaseAbility other) {
        return Integer.compare(sortOrder(), other.sortOrder());
    }
}
