package com.hbm.hazard;

import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.hazard.type.HazardTypeBase;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Legacy package facade for 1.7.10 hazard entries.
 */
@Deprecated(forRemoval = false)
public class HazardEntry {
    public final HazardTypeBase type;
    public final float baseLevel;
    public List<HazardModifier> mods = new ArrayList<>();

    public HazardEntry(HazardTypeBase type) {
        this(type, 1.0F);
    }

    public HazardEntry(HazardTypeBase type, float level) {
        this.type = type;
        this.baseLevel = level;
    }

    public HazardEntry addMod(HazardModifier mod) {
        this.mods.add(mod);
        return this;
    }

    public void applyHazard(ItemStack stack, LivingEntity entity) {
        com.hbm.ntm.radiation.HazardExposureUtil.applyHazard(entity, stack, type.modernType(), modifiedLevel(stack, entity));
    }

    public HazardTypeBase getType() {
        return this.type;
    }

    public float modifiedLevel(ItemStack stack, LivingEntity holder) {
        return HazardModifier.evalAllModifiers(stack, holder, baseLevel, mods);
    }

    public com.hbm.ntm.radiation.HazardEntry toModern() {
        com.hbm.ntm.radiation.HazardEntry entry =
                new com.hbm.ntm.radiation.HazardEntry(type.modernType(), baseLevel);
        for (HazardModifier mod : mods) {
            entry.withModifier(mod::modify);
        }
        return entry;
    }

    public static HazardEntry fromModern(com.hbm.ntm.radiation.HazardEntry entry) {
        HazardEntry legacy = new HazardEntry(HazardTypeBase.fromModern(entry.type()), entry.level());
        if (!entry.modifiers().isEmpty()) {
            legacy.addMod(new HazardModifier() {
                @Override
                public float modify(ItemStack stack, LivingEntity holder, float level) {
                    return entry.modifiedLevel(stack, holder);
                }
            });
        }
        return legacy;
    }

    @Override
    public HazardEntry clone() {
        return clone(1.0F);
    }

    public HazardEntry clone(float mult) {
        HazardEntry clone = new HazardEntry(type, baseLevel * mult);
        clone.mods = this.mods;
        return clone;
    }
}
