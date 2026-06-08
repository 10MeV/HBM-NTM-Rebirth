package com.hbm.ntm.neutron;

import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class RBMKThermalRuntime {
    private RBMKThermalRuntime() {
    }

    public static double passiveCooling(RBMKRuntimeSettings settings, int neighbors) {
        return settings.passiveCoolingInner()
                + (settings.passiveCooling() - settings.passiveCoolingInner())
                * ((4.0D - Mth.clamp(neighbors, 0, 4)) / 4.0D);
    }

    public static void coolPassively(RBMKThermalState state, RBMKRuntimeSettings settings, int neighbors) {
        state.setHeat(state.heat() - passiveCooling(settings, neighbors));
        if (state.heat() < RBMKThermalState.MIN_PASSIVE_HEAT) {
            state.setHeat(RBMKThermalState.MIN_PASSIVE_HEAT);
        }
    }

    public static int boilWater(RBMKThermalState state, RBMKRuntimeSettings settings) {
        if (state.heat() < 100.0D || settings.boilerHeatConsumption() <= 0.0D) {
            return 0;
        }
        double availableHeat = (state.heat() - 100.0D) / settings.boilerHeatConsumption();
        double availableWater = state.reasimWater();
        double availableSpace = RBMKThermalState.MAX_STEAM - state.reasimSteam();
        int processedWater = (int) Math.floor(Math.min(Math.min(availableHeat, availableWater), availableSpace)
                * settings.reasimBoilerSpeed());
        if (processedWater <= 0) {
            return 0;
        }
        state.setReasimWater(state.reasimWater() - processedWater);
        state.setReasimSteam(state.reasimSteam() + processedWater);
        state.setHeat(state.heat() - processedWater * settings.boilerHeatConsumption());
        return processedWater;
    }

    public static boolean equalizeWithNeighbors(
            RBMKThermalState primary,
            List<RBMKThermalState> neighbors,
            RBMKRuntimeSettings settings) {
        List<RBMKThermalState> columns = new ArrayList<>();
        columns.add(primary);
        columns.addAll(neighbors);
        if (columns.size() <= 1) {
            return false;
        }

        double heatTotal = 0.0D;
        int waterTotal = 0;
        int steamTotal = 0;
        for (RBMKThermalState column : columns) {
            heatTotal += column.heat();
            if (settings.reasimBoilers()) {
                waterTotal += column.reasimWater();
                steamTotal += column.reasimSteam();
            }
        }

        double targetHeat = heatTotal / columns.size();
        for (RBMKThermalState column : columns) {
            column.setHeat(column.heat() + (targetHeat - column.heat()) * settings.columnHeatFlow());
        }

        if (settings.reasimBoilers()) {
            int targetWater = waterTotal / columns.size();
            int targetSteam = steamTotal / columns.size();
            int waterRemainder = waterTotal % columns.size();
            int steamRemainder = steamTotal % columns.size();
            for (RBMKThermalState column : columns) {
                column.setReasimWater(targetWater);
                column.setReasimSteam(targetSteam);
            }
            primary.setReasimWater(primary.reasimWater() + waterRemainder);
            primary.setReasimSteam(primary.reasimSteam() + steamRemainder);
        }
        return true;
    }
}
