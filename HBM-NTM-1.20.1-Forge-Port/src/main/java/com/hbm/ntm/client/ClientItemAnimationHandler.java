package com.hbm.ntm.client;

import com.hbm.ntm.client.anim.LegacyBusAnimation;
import com.hbm.ntm.client.anim.LegacyBusAnimationLoader;
import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class ClientItemAnimationHandler {
    private static final Map<ResourceLocation, Map<String, LegacyBusAnimation>> CACHE = new HashMap<>();

    public static void handle(int slot, int rail, String itemKey, ResourceLocation animationFile, String animationName, boolean holdLastFrame) {
        Map<String, LegacyBusAnimation> animations = CACHE.computeIfAbsent(animationFile, LegacyBusAnimationLoader::load);
        LegacyBusAnimation animation = animations.get(animationName);
        if (animation != null) {
            LegacyHbmAnimations.start(slot, rail, itemKey, animation, holdLastFrame);
        }
    }

    private ClientItemAnimationHandler() {
    }
}
