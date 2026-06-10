package com.hbm.ntm.api.entity;

import java.util.ArrayList;
import java.util.List;

public final class RadarMenuLayout {
    public static final int MACHINE_LINK_START_X = 26;
    public static final int MACHINE_LINK_Y = 17;
    public static final int MACHINE_SLOT_STEP_X = 18;
    public static final int SCREEN_LINKER_X = 26;
    public static final int SCREEN_LINKER_Y = 44;
    public static final int BATTERY_X = 152;
    public static final int BATTERY_Y = 44;
    public static final int PLAYER_INV_START_X = 8;
    public static final int PLAYER_INV_START_Y = 103;
    public static final int PLAYER_HOTBAR_Y = 161;
    public static final int PLAYER_SLOT_STEP = 18;
    public static final int PLAYER_INV_ROWS = 3;
    public static final int PLAYER_INV_COLUMNS = 9;

    private RadarMenuLayout() {
    }

    public static List<MachineSlot> machineSlots() {
        List<MachineSlot> slots = new ArrayList<>(RadarInventoryProfile.SLOT_COUNT);
        for (int slot = 0; slot < RadarInventoryProfile.COMMAND_LINK_SLOT_COUNT; slot++) {
            slots.add(new MachineSlot(slot, MACHINE_LINK_START_X + slot * MACHINE_SLOT_STEP_X, MACHINE_LINK_Y));
        }
        slots.add(new MachineSlot(RadarInventoryProfile.SLOT_LINKER, SCREEN_LINKER_X, SCREEN_LINKER_Y));
        slots.add(new MachineSlot(RadarInventoryProfile.SLOT_BATTERY, BATTERY_X, BATTERY_Y));
        return slots;
    }

    public static List<PlayerSlot> playerInventorySlots() {
        List<PlayerSlot> slots = new ArrayList<>(PLAYER_INV_ROWS * PLAYER_INV_COLUMNS + PLAYER_INV_COLUMNS);
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int column = 0; column < PLAYER_INV_COLUMNS; column++) {
                slots.add(new PlayerSlot(column + row * PLAYER_INV_COLUMNS + PLAYER_INV_COLUMNS,
                        PLAYER_INV_START_X + column * PLAYER_SLOT_STEP,
                        PLAYER_INV_START_Y + row * PLAYER_SLOT_STEP));
            }
        }
        for (int column = 0; column < PLAYER_INV_COLUMNS; column++) {
            slots.add(new PlayerSlot(column, PLAYER_INV_START_X + column * PLAYER_SLOT_STEP, PLAYER_HOTBAR_Y));
        }
        return slots;
    }

    public record MachineSlot(int slot, int x, int y) {
    }

    public record PlayerSlot(int slot, int x, int y) {
    }
}
