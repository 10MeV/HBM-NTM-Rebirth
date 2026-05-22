package com.hbm.ntm.client;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientMuzzleFlashEffects {
    private static final long RETAIN_MILLIS = 2_000L;
    private static final Map<Integer, Long> FLASHES = new HashMap<>();

    public static void mark(int entityId) {
        FLASHES.put(entityId, System.currentTimeMillis());
    }

    public static long lastFlashMillis(Entity entity) {
        return entity == null ? 0L : FLASHES.getOrDefault(entity.getId(), 0L);
    }

    public static boolean hasRecentFlash(Entity entity, long durationMillis) {
        long last = lastFlashMillis(entity);
        return last > 0L && System.currentTimeMillis() - last <= durationMillis;
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, Long>> iterator = FLASHES.entrySet().iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().getValue() > RETAIN_MILLIS) {
                iterator.remove();
            }
        }
    }

    private ClientMuzzleFlashEffects() {
    }
}
