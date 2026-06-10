package com.hbm.ntm.world.saveddata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
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

    public static <T extends SavedData> T get(MinecraftServer server, String name,
            Function<CompoundTag, T> loader, Supplier<T> factory) {
        return get(server.overworld(), name, loader, factory);
    }

    public static <T extends SavedData> Optional<T> get(MinecraftServer server, ResourceKey<Level> dimension,
            String name, Function<CompoundTag, T> loader, Supplier<T> factory) {
        ServerLevel level = server.getLevel(dimension);
        return level == null ? Optional.empty() : Optional.of(get(level, name, loader, factory));
    }

    public static <T extends SavedData> Optional<T> getExisting(ServerLevel level, String name,
            Function<CompoundTag, T> loader) {
        return Optional.ofNullable(level.getDataStorage().get(loader, name));
    }

    public static <T extends SavedData> Optional<T> getExisting(MinecraftServer server, String name,
            Function<CompoundTag, T> loader) {
        return getExisting(server.overworld(), name, loader);
    }

    public static <T extends SavedData> Optional<T> getExisting(MinecraftServer server, ResourceKey<Level> dimension,
            String name, Function<CompoundTag, T> loader) {
        ServerLevel level = server.getLevel(dimension);
        return level == null ? Optional.empty() : getExisting(level, name, loader);
    }

    public static <T extends SavedData> Optional<T> getExisting(Level level, String name,
            Function<CompoundTag, T> loader) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        return getExisting(serverLevel, name, loader);
    }

    public static <T extends SavedData> T getWithFallback(ServerLevel level, String name,
            Function<CompoundTag, T> loader, Supplier<T> factory, String... fallbackNames) {
        Optional<T> existing = promoteExistingWithFallback(level, name, loader, fallbackNames);
        if (existing.isPresent()) {
            return existing.get();
        }
        return level.getDataStorage().computeIfAbsent(loader, factory, name);
    }

    public static <T extends SavedData> Optional<T> getExistingWithFallback(ServerLevel level, String name,
            Function<CompoundTag, T> loader, String... fallbackNames) {
        return findExistingWithFallback(level, name, loader, fallbackNames).map(ExistingDataLookup::data);
    }

    public static <T extends SavedData> Optional<T> promoteExistingWithFallback(ServerLevel level, String name,
            Function<CompoundTag, T> loader, String... fallbackNames) {
        Optional<ExistingDataLookup<T>> lookup = findExistingWithFallback(level, name, loader, fallbackNames);
        if (lookup.isPresent()) {
            ExistingDataLookup<T> result = lookup.get();
            promoteLookup(level, name, result);
            return Optional.of(result.data());
        }
        return Optional.empty();
    }

    public static <T extends SavedData> Optional<ExistingDataLookup<T>> findExistingWithFallback(ServerLevel level,
            String name, Function<CompoundTag, T> loader, String... fallbackNames) {
        T data = level.getDataStorage().get(loader, name);
        if (data != null) {
            return Optional.of(new ExistingDataLookup<>(name, name, data));
        }
        if (fallbackNames == null) {
            return Optional.empty();
        }
        for (String fallbackName : fallbackNames) {
            if (fallbackName == null || fallbackName.isBlank()) {
                continue;
            }
            data = level.getDataStorage().get(loader, fallbackName);
            if (data != null) {
                return Optional.of(new ExistingDataLookup<>(name, fallbackName, data));
            }
        }
        return Optional.empty();
    }

    public static <T extends SavedData> Optional<ExistingDataLookup<T>> findExistingWithFallback(Level level,
            String name, Function<CompoundTag, T> loader, String... fallbackNames) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        return findExistingWithFallback(serverLevel, name, loader, fallbackNames);
    }

    public static <T extends SavedData> Optional<ExistingDataLookup<T>> findExistingWithFallback(MinecraftServer server,
            ResourceKey<Level> dimension, String name, Function<CompoundTag, T> loader, String... fallbackNames) {
        ServerLevel level = server.getLevel(dimension);
        return level == null ? Optional.empty()
                : findExistingWithFallback(level, name, loader, fallbackNames);
    }

    public static <T extends SavedData> boolean promoteLookup(ServerLevel level, String name,
            ExistingDataLookup<T> result) {
        if (result == null || result.primary()) {
            return false;
        }
        result.data().setDirty();
        level.getDataStorage().set(name, result.data());
        return true;
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

    public static Optional<ServerLevel> level(MinecraftServer server, ResourceKey<Level> dimension) {
        return Optional.ofNullable(server.getLevel(dimension));
    }

    public static ServerLevel overworld(MinecraftServer server) {
        return server.overworld();
    }

    private WorldSavedDataHelper() {
    }

    public record ExistingDataLookup<T extends SavedData>(String requestedName, String foundName, T data) {
        public ExistingDataLookup {
            if (requestedName == null || requestedName.isBlank()) {
                throw new IllegalArgumentException("requestedName");
            }
            if (foundName == null || foundName.isBlank()) {
                throw new IllegalArgumentException("foundName");
            }
        }

        public boolean primary() {
            return requestedName.equals(foundName);
        }

        public String source() {
            return primary() ? "primary" : "fallback";
        }

        public String summary() {
            return "requested=" + requestedName
                    + " found=" + foundName
                    + " source=" + source();
        }
    }
}
