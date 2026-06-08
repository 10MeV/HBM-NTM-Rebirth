package com.hbm.ntm.world.saveddata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class WorldSavedDataHelper {
    public static <T extends SavedData> T get(ServerLevel level, String name,
            Function<CompoundTag, T> loader, Supplier<T> factory) {
        return level.getDataStorage().computeIfAbsent(loader, factory, name);
    }

    public static <T extends SavedData> T getWithFallback(ServerLevel level, String name,
            Function<CompoundTag, T> loader, Supplier<T> factory, String... fallbackNames) {
        Optional<T> existing = getExistingWithFallback(level, name, loader, fallbackNames);
        if (existing.isPresent()) {
            return existing.get();
        }
        return level.getDataStorage().computeIfAbsent(loader, factory, name);
    }

    public static <T extends SavedData> Optional<T> getExistingWithFallback(ServerLevel level, String name,
            Function<CompoundTag, T> loader, String... fallbackNames) {
        T data = level.getDataStorage().get(loader, name);
        if (data != null) {
            return Optional.of(data);
        }
        for (String fallbackName : fallbackNames) {
            data = level.getDataStorage().get(loader, fallbackName);
            if (data != null) {
                data.setDirty();
                level.getDataStorage().set(name, data);
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }

    public static <T extends SavedData> Optional<T> get(Level level, String name,
            Function<CompoundTag, T> loader, Supplier<T> factory) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        return Optional.of(get(serverLevel, name, loader, factory));
    }

    public static Optional<ServerLevel> asServerLevel(Level level) {
        return level instanceof ServerLevel serverLevel ? Optional.of(serverLevel) : Optional.empty();
    }

    private WorldSavedDataHelper() {
    }
}
