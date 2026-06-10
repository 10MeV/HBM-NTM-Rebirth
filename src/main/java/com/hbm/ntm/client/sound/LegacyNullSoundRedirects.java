package com.hbm.ntm.client.sound;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.sound.LegacySoundIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public final class LegacyNullSoundRedirects {
    private static final double SEARCH_RADIUS = 2.0D;
    private static final String NULL_CHOPPER = "misc.null_chopper";
    private static final String NULL_CRASHING = "misc.null_crashing";
    private static final String NULL_MINE = "misc.null_mine";

    public static boolean handle(@Nullable SoundInstance sound) {
        if (sound == null) {
            return false;
        }
        return handle(sound.getLocation(), sound.getX(), sound.getY(), sound.getZ());
    }

    public static boolean handle(@Nullable ResourceLocation soundId, double x, double y, double z) {
        ResourceLocation location = soundId == null ? null : LegacySoundIds.resolveLocation(soundId.toString());
        if (location == null || !HbmNtm.MOD_ID.equals(location.getNamespace())) {
            return false;
        }

        return switch (location.getPath()) {
            case NULL_CHOPPER -> {
                Entity chopper = findNearest(x, y, z, LegacyNullSoundRedirects::isHunterChopper);
                if (chopper != null) {
                    LegacyMovingEntitySound.startChopperFlying(chopper, LegacyNullSoundRedirects::keepFlying);
                }
                yield true;
            }
            case NULL_CRASHING -> {
                Entity chopper = findNearest(x, y, z, LegacyNullSoundRedirects::isHunterChopper);
                if (chopper != null) {
                    LegacyMovingEntitySound.startChopperCrashing(chopper, LegacyNullSoundRedirects::keepCrashing);
                }
                yield true;
            }
            case NULL_MINE -> {
                Entity mine = findNearest(x, y, z, LegacyNullSoundRedirects::isChopperMine);
                if (mine != null) {
                    LegacyMovingEntitySound.startChopperMine(mine, Entity::isAlive);
                }
                yield true;
            }
            default -> false;
        };
    }

    @Nullable
    private static Entity findNearest(double x, double y, double z, Predicate<Entity> predicate) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }

        double bestDistance = SEARCH_RADIUS * SEARCH_RADIUS;
        Entity best = null;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!entity.isAlive() || !predicate.test(entity)) {
                continue;
            }
            double distance = entity.distanceToSqr(x, y, z);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    private static boolean isHunterChopper(Entity entity) {
        return hasLegacyNameParts(entity, "hunter", "chopper");
    }

    private static boolean isChopperMine(Entity entity) {
        return hasLegacyNameParts(entity, "chopper", "mine");
    }

    private static boolean keepFlying(Entity entity) {
        return !legacyDyingFlag(entity, false);
    }

    private static boolean keepCrashing(Entity entity) {
        return legacyDyingFlag(entity, true);
    }

    private static boolean legacyDyingFlag(Entity entity, boolean fallback) {
        try {
            Method method = entity.getClass().getMethod("getIsDying");
            Object value = method.invoke(entity);
            return value instanceof Boolean dying ? dying : fallback;
        } catch (ReflectiveOperationException ignored) {
            return fallback;
        }
    }

    private static boolean hasLegacyNameParts(Entity entity, String... parts) {
        String name = compactName(entity.getClass().getSimpleName());
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (key != null) {
            name += "|" + compactName(key.getPath());
        }

        for (String part : parts) {
            if (!name.contains(compactName(part))) {
                return false;
            }
        }
        return true;
    }

    private static String compactName(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replace("_", "")
                .replace("-", "")
                .replace(".", "");
    }

    private LegacyNullSoundRedirects() {
    }
}
