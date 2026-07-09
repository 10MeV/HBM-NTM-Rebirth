package com.hbm.ntm.recipe;

import com.hbm.ntm.blockentity.LegacyPedestalBlockEntity;
import com.hbm.ntm.particle.LegacyParticleCreators;
import com.hbm.ntm.player.HbmPlayerProperties;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class PedestalRecipeRuntime {
    private static final BlockPos[] OFFSETS = {
            new BlockPos(-2, 0, -2),
            new BlockPos(0, 0, -3),
            new BlockPos(2, 0, -2),
            new BlockPos(-3, 0, 0),
            BlockPos.ZERO,
            new BlockPos(3, 0, 0),
            new BlockPos(-2, 0, 2),
            new BlockPos(0, 0, 3),
            new BlockPos(2, 0, 2)
    };

    private static final Comparator<PedestalRecipe> RECIPE_ORDER =
            Comparator.comparingInt(PedestalRecipe::sourceOrder)
                    .thenComparing(recipe -> recipe.getId().toString());

    public static boolean tryCraft(Level level, BlockPos centerPos) {
        if (level == null || level.isClientSide) {
            return false;
        }

        LegacyPedestalBlockEntity[] pedestals = gatherPedestals(level, centerPos);
        LegacyPedestalBlockEntity center = pedestals[PedestalRecipe.CENTER_SLOT];
        if (center == null) {
            return false;
        }

        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, new AABB(centerPos).inflate(20.0D));
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.PEDESTAL.type().get()).stream()
                .sorted(RECIPE_ORDER)
                .filter(recipe -> matches(level, centerPos, nearbyPlayers, pedestals, recipe))
                .findFirst()
                .map(recipe -> craft(level, centerPos, center, pedestals, recipe))
                .orElse(false);
    }

    private static LegacyPedestalBlockEntity[] gatherPedestals(Level level, BlockPos centerPos) {
        LegacyPedestalBlockEntity[] pedestals = new LegacyPedestalBlockEntity[OFFSETS.length];
        for (int i = 0; i < OFFSETS.length; i++) {
            BlockPos pos = centerPos.offset(OFFSETS[i]);
            if (level.getBlockEntity(pos) instanceof LegacyPedestalBlockEntity pedestal) {
                pedestals[i] = pedestal;
            }
        }
        return pedestals;
    }

    private static boolean matches(Level level, BlockPos centerPos, List<Player> nearbyPlayers,
            LegacyPedestalBlockEntity[] pedestals, PedestalRecipe recipe) {
        if (!matchesExtra(level, centerPos, nearbyPlayers, recipe.extra())) {
            return false;
        }
        for (int i = 0; i < PedestalRecipe.SLOT_COUNT; i++) {
            HbmIngredient input = recipe.input(i);
            ItemStack stack = pedestals[i] == null ? ItemStack.EMPTY : pedestals[i].getItem();
            if (stack.isEmpty() && input == null) {
                continue;
            }
            if (stack.isEmpty() || input == null) {
                return false;
            }
            if (!input.test(stack, true) || input.count() != stack.getCount()) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesExtra(Level level, BlockPos centerPos, List<Player> nearbyPlayers,
            PedestalRecipe.ExtraCondition extra) {
        float celestialAngle = level.getTimeOfDay(0.0F);
        return switch (extra) {
            case NONE -> true;
            case FULL_MOON -> celestialAngle >= 0.35F && celestialAngle <= 0.65F && level.getMoonPhase() == 0;
            case NEW_MOON -> celestialAngle >= 0.35F && celestialAngle <= 0.65F && level.getMoonPhase() == 4;
            case SUN -> celestialAngle <= 0.15F || celestialAngle >= 0.85F;
            case GOOD_KARMA -> nearbyPlayers.stream()
                    .anyMatch(player -> HbmPlayerProperties.hasReputationAtLeast(player, 10));
            case BAD_KARMA -> nearbyPlayers.stream()
                    .anyMatch(player -> HbmPlayerProperties.hasReputationAtMost(player, -10));
        };
    }

    private static boolean craft(Level level, BlockPos centerPos, LegacyPedestalBlockEntity center,
            LegacyPedestalBlockEntity[] pedestals, PedestalRecipe recipe) {
        for (int i = 0; i < pedestals.length; i++) {
            if (i != PedestalRecipe.CENTER_SLOT && recipe.input(i) != null && pedestals[i] != null) {
                pedestals[i].clearItem();
            }
        }

        center.setItem(recipe.output().collapse(level.random));
        LegacyParticleCreators.composeEffect(level, centerPos.getX() + 0.5D, centerPos.getY() + 1.5D,
                centerPos.getZ() + 0.5D, 10, 2.5F, 1.0F);
        return true;
    }

    private PedestalRecipeRuntime() {
    }
}
