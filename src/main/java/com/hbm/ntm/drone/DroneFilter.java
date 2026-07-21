package com.hbm.ntm.drone;

import com.hbm.ntm.util.LegacyPatternMatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Modern value form of the legacy ModulePatternMatcher request filter. */
public final class DroneFilter {
    public static final String EXACT = LegacyPatternMatcher.MODE_EXACT;
    public static final String WILDCARD = LegacyPatternMatcher.MODE_WILDCARD;
    public static final String BEDROCK = LegacyPatternMatcher.MODE_BEDROCK;

    private final ItemStack pattern;
    private final LegacyPatternMatcher matcher = new LegacyPatternMatcher(1);

    public DroneFilter(ItemStack pattern, String mode) {
        this.pattern = pattern.copyWithCount(1);
        matcher.initPatternStandard(this.pattern, 0);
        if (mode != null && !mode.isBlank()) {
            matcher.setMode(0, mode);
        }
    }

    public ItemStack pattern() {
        return pattern.copy();
    }

    public String mode() {
        return matcher.getMode(0);
    }

    public void cycleMode() {
        matcher.nextMode(pattern, 0);
    }

    public boolean matches(ItemStack candidate) {
        return matcher.isValidForFilter(pattern, 0, candidate);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (!pattern.isEmpty()) {
            tag.put("pattern", pattern.save(new CompoundTag()));
        }
        String mode = mode();
        if (mode != null) {
            tag.putString("mode", mode);
        }
        return tag;
    }

    public static DroneFilter load(CompoundTag tag) {
        return new DroneFilter(ItemStack.of(tag.getCompound("pattern")), tag.contains("mode") ? tag.getString("mode") : null);
    }
}
