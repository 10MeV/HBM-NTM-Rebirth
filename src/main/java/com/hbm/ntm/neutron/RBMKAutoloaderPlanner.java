package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

public final class RBMKAutoloaderPlanner {
    public static final int INPUT_SLOT_START = 0;
    public static final int INPUT_SLOT_END = 8;
    public static final int OUTPUT_SLOT_START = 9;
    public static final int OUTPUT_SLOT_END = 17;
    public static final int SLOT_COUNT = 18;
    public static final int DEFAULT_CYCLE = 50;
    public static final int MIN_CYCLE = 5;
    public static final int MAX_CYCLE = 95;
    public static final int CYCLE_STEP = 5;
    public static final int ACTION_DELAY = 40;
    public static final double PISTON_SPEED = 0.005D;

    private RBMKAutoloaderPlanner() {
    }

    public static CyclePlan planCycleChange(int currentCycle, boolean minus, boolean plus) {
        int cycle = currentCycle;
        if (minus && cycle > MIN_CYCLE) {
            cycle -= CYCLE_STEP;
        }
        if (plus && cycle < MAX_CYCLE) {
            cycle += CYCLE_STEP;
        }
        return new CyclePlan(clamp(cycle, MIN_CYCLE, MAX_CYCLE));
    }

    public static boolean acceptsInput(RBMKFuelRodSpec spec, RBMKFuelRodState state, int slot, int cycle) {
        return slot >= INPUT_SLOT_START
                && slot <= INPUT_SLOT_END
                && enrichmentPercent(spec, state) >= clamp(cycle, MIN_CYCLE, MAX_CYCLE);
    }

    public static boolean canExtract(int slot) {
        return slot >= OUTPUT_SLOT_START && slot <= OUTPUT_SLOT_END;
    }

    public static List<Integer> accessibleSlots(double piston) {
        if (piston > 0.0D) {
            return List.of();
        }
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            slots.add(i);
        }
        return slots;
    }

    public static InventoryScan scanInventory(List<RodSlot> slots, int cycle) {
        List<RodSlot> safeSlots = normalizeSlots(slots);
        int inputSlot = -1;
        int outputSlot = -1;
        for (int i = INPUT_SLOT_START; i <= INPUT_SLOT_END; i++) {
            RodSlot slot = safeSlots.get(i);
            if (slot.present() && enrichmentPercent(slot.spec(), slot.state()) >= cycle) {
                inputSlot = i;
                break;
            }
        }
        for (int i = OUTPUT_SLOT_START; i <= OUTPUT_SLOT_END; i++) {
            if (!safeSlots.get(i).present()) {
                outputSlot = i;
                break;
            }
        }
        return new InventoryScan(inputSlot >= 0, outputSlot >= 0, inputSlot, outputSlot);
    }

    public static StartCyclePlan planStartCycle(
            boolean isRetracting,
            long worldTime,
            List<RodSlot> slots,
            boolean targetIsFuelRodColumn,
            boolean targetColdEnough,
            boolean targetSlotEmpty,
            RBMKFuelRodSpec targetSpec,
            RBMKFuelRodState targetState,
            int cycle) {
        InventoryScan scan = scanInventory(slots, clamp(cycle, MIN_CYCLE, MAX_CYCLE));
        if (!isRetracting || worldTime % 20L != 0L || !scan.hasEligibleInput() || !scan.hasOutputSpace()
                || !targetIsFuelRodColumn || !targetColdEnough) {
            return new StartCyclePlan(false, scan);
        }
        boolean targetNeedsReplacement = targetSlotEmpty
                || enrichmentPercent(targetSpec, targetState) < clamp(cycle, MIN_CYCLE, MAX_CYCLE);
        return new StartCyclePlan(targetNeedsReplacement, scan);
    }

    public static MotionPlan tickMotion(double piston, boolean isRetracting, int delay) {
        int nextDelay = Math.max(0, delay);
        if (nextDelay > 0) {
            nextDelay--;
        }

        double nextPiston = clamp01(piston);
        boolean nextRetracting = isRetracting;
        boolean reachedEnd = false;

        if (nextDelay <= 0 && nextRetracting && nextPiston > 0.0D) {
            nextPiston -= PISTON_SPEED;
            if (nextPiston <= 0.0D) {
                nextPiston = 0.0D;
                nextDelay = ACTION_DELAY;
                reachedEnd = true;
            }
        }

        if (nextDelay <= 0 && !nextRetracting && nextPiston < 1.0D) {
            nextPiston += PISTON_SPEED;
            if (nextPiston >= 1.0D) {
                nextPiston = 1.0D;
                nextDelay = ACTION_DELAY;
                reachedEnd = true;
            }
        }

        return new MotionPlan(nextPiston, nextRetracting, nextDelay, reachedEnd);
    }

    public static ExchangePlan planExtendedExchange(List<RodSlot> slots, boolean targetHasRod,
            RBMKFuelRodSpec targetSpec, RBMKFuelRodState targetState, int cycle) {
        InventoryScan scan = scanInventory(slots, clamp(cycle, MIN_CYCLE, MAX_CYCLE));
        int removedToSlot = targetHasRod && scan.hasOutputSpace() ? scan.firstOutputSlot() : -1;
        boolean targetEmptyAfterRemoval = !targetHasRod || removedToSlot >= 0;
        int insertedFromSlot = targetEmptyAfterRemoval && scan.hasEligibleInput() ? scan.firstInputSlot() : -1;
        boolean inserted = insertedFromSlot >= 0;

        return new ExchangePlan(
                removedToSlot >= 0,
                removedToSlot,
                inserted,
                insertedFromSlot,
                true,
                ACTION_DELAY);
    }

    private static double enrichmentPercent(RBMKFuelRodSpec spec, RBMKFuelRodState state) {
        if (spec == null || state == null) {
            return 0.0D;
        }
        return state.enrichment(spec) * 100.0D;
    }

    private static List<RodSlot> normalizeSlots(List<RodSlot> slots) {
        List<RodSlot> normalized = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            normalized.add(slots != null && i < slots.size() && slots.get(i) != null
                    ? slots.get(i)
                    : RodSlot.empty());
        }
        return normalized;
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static double clamp01(double value) {
        if (value < 0.0D || Double.isNaN(value)) {
            return 0.0D;
        }
        if (value > 1.0D) {
            return 1.0D;
        }
        return value;
    }

    public record RodSlot(boolean present, RBMKFuelRodSpec spec, RBMKFuelRodState state) {
        public static RodSlot empty() {
            return new RodSlot(false, null, null);
        }
    }

    public record CyclePlan(int cycle) {
    }

    public record InventoryScan(boolean hasEligibleInput, boolean hasOutputSpace, int firstInputSlot,
            int firstOutputSlot) {
    }

    public record StartCyclePlan(boolean startExtending, InventoryScan inventory) {
    }

    public record MotionPlan(double piston, boolean isRetracting, int delay, boolean reachedEnd) {
    }

    public record ExchangePlan(
            boolean removedOldRod,
            int removedToSlot,
            boolean insertedNewRod,
            int insertedFromSlot,
            boolean retractAfterExchange,
            int delayAfterExchange) {
    }
}
