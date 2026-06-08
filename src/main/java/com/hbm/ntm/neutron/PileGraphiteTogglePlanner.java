package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public final class PileGraphiteTogglePlanner {
    private PileGraphiteTogglePlanner() {
    }

    public static TogglePlan manualRodToggle(
            BlockPos origin,
            int oldMeta,
            Direction clickedSide,
            ChainStateLookup chainLookup) {
        if (!PileGraphiteMetadata.sideMatchesAxis(oldMeta, clickedSide)) {
            return TogglePlan.empty();
        }
        return propagate(origin, oldMeta, clickedSide, chainLookup, true);
    }

    public static TogglePlan detectorTriggeredRodToggle(
            BlockPos detectorPos,
            int oldMeta,
            ChainStateLookup chainLookup) {
        Direction direction = PileGraphiteMetadata.positiveDirection(oldMeta);
        return propagate(detectorPos, oldMeta, direction, chainLookup, false);
    }

    private static TogglePlan propagate(
            BlockPos origin,
            int oldMeta,
            Direction direction,
            ChainStateLookup chainLookup,
            boolean includeOrigin) {
        if (chainLookup == null || direction == null) {
            return TogglePlan.empty();
        }

        int newMeta = PileGraphiteMetadata.toggleActive(oldMeta);
        List<ToggleMutation> mutations = new ArrayList<>();
        if (includeOrigin) {
            mutations.add(new ToggleMutation(origin, newMeta));
        }

        for (int sign = -1; sign <= 1; sign += 2) {
            BlockPos.MutableBlockPos cursor = origin.mutable();
            while (true) {
                cursor.move(direction, sign);
                ChainState state = chainLookup.get(cursor);
                if (state == null || !state.sameLegacyBlock() || state.meta() != oldMeta) {
                    break;
                }
                mutations.add(new ToggleMutation(cursor.immutable(), newMeta));
            }
        }
        return new TogglePlan(oldMeta, newMeta, mutations);
    }

    @FunctionalInterface
    public interface ChainStateLookup {
        ChainState get(BlockPos pos);
    }

    public record ChainState(boolean sameLegacyBlock, int meta) {
    }

    public record ToggleMutation(BlockPos pos, int newMeta) {
    }

    public record TogglePlan(int oldMeta, int newMeta, List<ToggleMutation> mutations) {
        public static TogglePlan empty() {
            return new TogglePlan(0, 0, List.of());
        }

        public boolean hasMutations() {
            return !mutations.isEmpty();
        }
    }
}
