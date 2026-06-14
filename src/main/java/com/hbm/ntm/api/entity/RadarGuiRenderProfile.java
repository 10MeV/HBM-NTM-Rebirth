package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class RadarGuiRenderProfile {
    public static final int NOISE_GRID_SIZE = 5;
    public static final int NOISE_TILE_SIZE = 40;
    public static final int NOISE_TEXTURE_VARIANTS = 81;
    public static final int SLOT_TOGGLE_BACKGROUND = 0xFF101010;
    public static final int SLOT_TOGGLE_ACTIVE_BORDER = 0xFF1F8F32;
    public static final int SLOT_TOGGLE_INACTIVE_BORDER = 0xFF4C4C4C;

    private static final List<NoiseTile> NOISE_TILES = createNoiseTiles();

    private RadarGuiRenderProfile() {
    }

    public static List<TextureBlit> mainBackground(int leftPos, int topPos) {
        return List.of(
                new TextureBlit(leftPos, topPos, 0, 0, RadarGuiLayout.MAIN_WIDTH, RadarGuiLayout.MAIN_HEIGHT),
                new TextureBlit(leftPos + RadarGuiLayout.MAIN_SIDE_STRIP_X,
                        topPos + RadarGuiLayout.MAIN_SIDE_STRIP_TOP_Y, RadarGuiLayout.MAIN_SIDE_STRIP_U,
                        RadarGuiLayout.MAIN_SIDE_STRIP_TOP_V, RadarGuiLayout.MAIN_SIDE_STRIP_WIDTH,
                        RadarGuiLayout.MAIN_SIDE_STRIP_TOP_HEIGHT),
                new TextureBlit(leftPos + RadarGuiLayout.MAIN_SIDE_STRIP_X,
                        topPos + RadarGuiLayout.MAIN_SIDE_STRIP_BOTTOM_Y, RadarGuiLayout.MAIN_SIDE_STRIP_U,
                        RadarGuiLayout.MAIN_SIDE_STRIP_BOTTOM_V, RadarGuiLayout.MAIN_SIDE_STRIP_WIDTH,
                        RadarGuiLayout.MAIN_SIDE_STRIP_BOTTOM_HEIGHT));
    }

    public static TextureBlit slotBackground(int leftPos, int topPos) {
        return new TextureBlit(leftPos, topPos, 0, 0, RadarGuiLayout.SLOT_WIDTH, RadarGuiLayout.SLOT_HEIGHT);
    }

    public static int mainEnergyWidth() {
        return RadarGuiLayout.MAIN_ENERGY_WIDTH;
    }

    public static int slotEnergyWidth() {
        return RadarGuiLayout.SLOT_ENERGY_WIDTH;
    }

    public static boolean shouldRenderMainControlIcon(boolean active, boolean jammed, boolean flicker) {
        return active ^ (jammed && flicker);
    }

    public static boolean shouldRenderMainControlIcon(RadarMenuState state, RadarControlPanel.Button button,
            boolean flicker) {
        return shouldRenderMainControlIcon(controlActive(state, button), state != null && state.jammed(), flicker);
    }

    public static boolean controlActive(RadarMenuState state, RadarControlPanel.Button button) {
        return state != null && button != null && state.controlActive(button.control());
    }

    public static TextureBlit mainControlIcon(int leftPos, int topPos, RadarControlPanel.Button button) {
        return new TextureBlit(leftPos + button.mainX(), topPos + button.mainY(), button.iconU(), button.iconV(),
                RadarControlPanel.BUTTON_SIZE, RadarControlPanel.BUTTON_SIZE);
    }

    public static void forEachVisibleMainControlIcon(int leftPos, int topPos, RadarMenuState state,
            BooleanSupplier flicker, Consumer<TextureBlit> consumer) {
        for (RadarControlPanel.Button button : RadarControlPanel.buttons()) {
            boolean flickerValue = flicker != null && flicker.getAsBoolean();
            if (shouldRenderMainControlIcon(state, button, flickerValue)) {
                consumer.accept(mainControlIcon(leftPos, topPos, button));
            }
        }
    }

    public static TextureBlit slotControlIcon(int leftPos, int topPos, RadarControlPanel.Button button) {
        return new TextureBlit(leftPos + button.slotX(), topPos + button.slotY(), button.iconU(), button.iconV(),
                RadarControlPanel.BUTTON_SIZE, RadarControlPanel.BUTTON_SIZE);
    }

    public static List<NoiseTile> noiseTiles() {
        return NOISE_TILES;
    }

    public static SlotToggleFrame slotToggleFrame(RadarControlPanel.Button button, boolean active) {
        return new SlotToggleFrame(button.slotX(), button.slotY(),
                active ? SLOT_TOGGLE_ACTIVE_BORDER : SLOT_TOGGLE_INACTIVE_BORDER);
    }

    public static SlotToggleFrame slotToggleFrame(RadarControlPanel.Button button, RadarMenuState state) {
        return slotToggleFrame(button, controlActive(state, button));
    }

    public static void forEachSlotToggle(int leftPos, int topPos, RadarMenuState state, Consumer<SlotToggle> consumer) {
        for (RadarControlPanel.Button button : RadarControlPanel.buttons()) {
            boolean active = controlActive(state, button);
            consumer.accept(new SlotToggle(slotToggleFrame(button, active), slotControlIcon(leftPos, topPos, button),
                    active));
        }
    }

    public static MainContentPlan mainContent(RadarMenuState state, long consumption) {
        if (state == null || !state.hasOperatingPower(consumption)) {
            return MainContentPlan.EMPTY;
        }
        return new MainContentPlan(true, state.jammed(), state.showMap());
    }

    public static EnergyBar mainEnergyBar(int leftPos, int topPos, int width) {
        return new EnergyBar(leftPos + RadarGuiLayout.MAIN_ENERGY_X, topPos + RadarGuiLayout.MAIN_ENERGY_Y,
                RadarGuiLayout.MAIN_ENERGY_U, RadarGuiLayout.MAIN_ENERGY_V, width,
                RadarGuiLayout.MAIN_ENERGY_TEXTURE_HEIGHT);
    }

    public static EnergyBar slotEnergyBar(int leftPos, int topPos, int width) {
        return new EnergyBar(leftPos + RadarGuiLayout.SLOT_ENERGY_X, topPos + RadarGuiLayout.SLOT_ENERGY_Y,
                RadarGuiLayout.SLOT_ENERGY_U, RadarGuiLayout.SLOT_ENERGY_V, width,
                RadarGuiLayout.SLOT_ENERGY_HEIGHT);
    }

    public static MapPixel mapPixel(int leftPos, int topPos, int index, byte height) {
        return new MapPixel(RadarGuiLayout.mapPixelX(leftPos, index), RadarGuiLayout.mapPixelY(topPos, index),
                0xFF000000 | (RadarMap.green(height) << 8));
    }

    public static void forEachMapPixel(int leftPos, int topPos, byte[] map, int maxSize,
            Consumer<MapPixel> consumer) {
        int size = Math.min(map != null ? map.length : 0, maxSize);
        for (int index = 0; index < size; index++) {
            byte height = map[index];
            if (height > 0) {
                consumer.accept(mapPixel(leftPos, topPos, index, height));
            }
        }
    }

    public static ScreenPoint sweepCenter(int leftPos, int topPos) {
        return new ScreenPoint(leftPos + RadarGuiLayout.RADAR_CENTER_X, topPos + RadarGuiLayout.RADAR_CENTER_Y);
    }

    public static float sweepRotation(float partialTick, float previousRotation, float rotation) {
        return Mth.lerp(partialTick, previousRotation, rotation);
    }

    public static Blip blip(int leftPos, int topPos, RadarEntry entry, BlockPos radarPos, int range) {
        RadarDisplayProjection.ScreenOffset offset =
                RadarDisplayProjection.guiBlipOffset(entry.pos(), radarPos, range);
        return new Blip(leftPos + RadarGuiLayout.RADAR_CENTER_X + offset.x(),
                topPos + RadarGuiLayout.RADAR_CENTER_Y + offset.z(), entry.blipLevel());
    }

    public static void forEachBlip(int leftPos, int topPos, List<RadarEntry> entries, BlockPos radarPos,
            int range, Consumer<Blip> consumer) {
        for (RadarEntry entry : entries) {
            consumer.accept(blip(leftPos, topPos, entry, radarPos, range));
        }
    }

    private static List<NoiseTile> createNoiseTiles() {
        List<NoiseTile> tiles = new ArrayList<>(NOISE_GRID_SIZE * NOISE_GRID_SIZE);
        for (int x = 0; x < NOISE_GRID_SIZE; x++) {
            for (int z = 0; z < NOISE_GRID_SIZE; z++) {
                tiles.add(new NoiseTile(RadarGuiLayout.RADAR_AREA_X + x * NOISE_TILE_SIZE,
                        RadarGuiLayout.RADAR_AREA_Y + z * NOISE_TILE_SIZE));
            }
        }
        return List.copyOf(tiles);
    }

    public record TextureBlit(int x, int y, int u, int v, int width, int height) {
    }

    public record NoiseTile(int x, int y) {
    }

    public record SlotToggleFrame(int x, int y, int borderColor) {
        public int outerLeft() {
            return x - 1;
        }

        public int outerTop() {
            return y - 1;
        }

        public int outerRight() {
            return x + RadarControlPanel.BUTTON_SIZE + 1;
        }

        public int outerBottom() {
            return y + RadarControlPanel.BUTTON_SIZE + 1;
        }

        public int iconRight() {
            return x + RadarControlPanel.BUTTON_SIZE;
        }

        public int iconBottom() {
            return y + RadarControlPanel.BUTTON_SIZE;
        }
    }

    public record EnergyBar(int x, int y, int u, int v, int width, int height) {
    }

    public record MapPixel(int x, int y, int color) {
    }

    public record ScreenPoint(int x, int y) {
    }

    public record Blip(double x, double y, int level) {
    }

    public record MainContentPlan(boolean renderContent, boolean renderNoise, boolean renderMap) {
        public static final MainContentPlan EMPTY = new MainContentPlan(false, false, false);

        public boolean renderSweepAndBlips() {
            return renderContent && !renderNoise;
        }
    }

    public record SlotToggle(SlotToggleFrame frame, TextureBlit icon, boolean active) {
    }
}
