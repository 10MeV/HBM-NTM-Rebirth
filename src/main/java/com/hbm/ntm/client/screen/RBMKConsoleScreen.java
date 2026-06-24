package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RBMKConsoleBlockEntity;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.menu.RBMKConsoleMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.neutron.RBMKConsolePlanner;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class RBMKConsoleScreen extends AbstractContainerScreen<RBMKConsoleMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_rbmk_console.png");
    private static final int GRID_X = 86;
    private static final int GRID_Y = 11;
    private static final int CELL = 10;
    private static final int GRID_SIZE = RBMKConsolePlanner.CONSOLE_GRID_SIZE;
    private final boolean[] selected = new boolean[GRID_SIZE * GRID_SIZE];
    private EditBox levelBox;
    private boolean az5Lid = true;
    private long lastAz5Press;

    public RBMKConsoleScreen(RBMKConsoleMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 244;
        imageHeight = 172;
    }

    @Override
    protected void init() {
        super.init();
        levelBox = LegacyGuiElements.createLegacyTextField(font, leftPos + 9, topPos + 84,
                35, 9, 3, "0");
        addRenderableWidget(levelBox);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (az5Lid) {
            graphics.blit(TEXTURE, leftPos + 30, topPos + 138, 228, 172, 28, 28);
        }
        RBMKConsolePlanner.ScreenState[] screens = console().screens();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 2; column++) {
                int id = row * 2 + column;
                RBMKConsolePlanner.ScreenType type = id < screens.length ? screens[id].type() : RBMKConsolePlanner.ScreenType.NONE;
                graphics.blit(TEXTURE, leftPos + 6 + 40 * column, topPos + 8 + 21 * row,
                        type.offset(), 238, 18, 18);
            }
        }
        renderColumns(graphics, console().columns());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int[] flux = console().fluxBuffer();
        if (flux.length > 0) {
            int highest = Integer.MIN_VALUE;
            int lowest = Integer.MAX_VALUE;
            for (int value : flux) {
                highest = Math.max(highest, value);
                lowest = Math.min(lowest, value);
            }
            graphics.pose().pushPose();
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            drawScaledString(graphics, highest + "", 8, 98);
            drawScaledString(graphics, highest + "", 80 - font.width(highest + "") / 2, 98);
            drawScaledString(graphics, lowest + "", 8, 128);
            drawScaledString(graphics, lowest + "", 80 - font.width(lowest + "") / 2, 128);
            graphics.pose().popPose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int gridLeft = leftPos + GRID_X;
        int gridTop = topPos + GRID_Y;
        if (mouseX >= gridLeft && mouseX < gridLeft + GRID_SIZE * CELL
                && mouseY >= gridTop && mouseY < gridTop + GRID_SIZE * CELL) {
            int x = ((int) mouseX - gridLeft) / CELL;
            int y = ((int) mouseY - gridTop) / CELL;
            int index = y * GRID_SIZE + x;
            RBMKConsolePlanner.ColumnSnapshot column = columnAt(console().columns(), index);
            if (column != null) {
                selected[index] = !selected[index];
                LegacyGuiElements.playClickSound();
                return true;
            }
        }
        if (isHovering(72, 70, 10, 10, mouseX, mouseY)) {
            for (int i = 0; i < selected.length; i++) {
                selected[i] = false;
            }
            LegacyGuiElements.playClickSound();
            return true;
        }
        if (isHovering(61, 70, 10, 10, mouseX, mouseY)) {
            selectAllManualControlRods();
            LegacyGuiElements.playClickSound();
            return true;
        }
        if (isHovering(70, 82, 12, 12, mouseX, mouseY)) {
            cycleCompressor();
            return true;
        }
        for (int color = 0; color < 5; color++) {
            if (isHovering(6 + color * 11, 70, 10, 10, mouseX, mouseY)) {
                if (button == 0) {
                    selectManualColorGroup(color);
                } else if (button == 1) {
                    assignColor(color);
                }
                LegacyGuiElements.playClickSound();
                return true;
            }
        }
        if (isHovering(30, 138, 28, 28, mouseX, mouseY)) {
            pressAz5();
            return true;
        }
        if (isHovering(48, 82, 12, 12, mouseX, mouseY)) {
            applyLevel();
            return true;
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 2; column++) {
                int slot = row * 2 + column;
                if (isHovering(6 + 40 * column, 8 + 21 * row, 18, 18, mouseX, mouseY)) {
                    toggleScreen(slot);
                    return true;
                }
                if (isHovering(24 + 40 * column, 8 + 21 * row, 18, 18, mouseX, mouseY)) {
                    bindSelection(slot);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void applyLevel() {
        CompoundTag tag = selectedIndexTag();
        tag.putDouble("level", parsePercent(levelBox.getValue()));
        send(tag);
        LegacyGuiElements.playClickSound();
    }

    private void assignColor(int color) {
        CompoundTag tag = new CompoundTag();
        tag.putByte("assignColor", (byte) color);
        tag.putIntArray("cols", selectedColumns());
        send(tag);
    }

    private void cycleCompressor() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("compressor", true);
        tag.putIntArray("cols", selectedColumns());
        send(tag);
    }

    private CompoundTag selectedIndexTag() {
        CompoundTag tag = new CompoundTag();
        int count = 0;
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                tag.putInt("sel_" + count++, i);
            }
        }
        return tag;
    }

    private int[] selectedColumns() {
        return RBMKConsolePlanner.selectedColumns(selected);
    }

    private static double parsePercent(String value) {
        try {
            return Math.max(0.0D, Math.min(1.0D, Double.parseDouble(value) / 100.0D));
        } catch (NumberFormatException ignored) {
            return 0.0D;
        }
    }

    private static RBMKConsolePlanner.ColumnSnapshot columnAt(RBMKConsolePlanner.ColumnSnapshot[] columns, int index) {
        return columns == null || index < 0 || index >= columns.length ? null : columns[index];
    }

    private RBMKConsoleBlockEntity console() {
        return menu.getBlockEntity();
    }

    private void send(CompoundTag tag) {
        ModMessages.sendTileControl(console().getBlockPos(), tag);
    }

    private void toggleScreen(int slot) {
        CompoundTag tag = new CompoundTag();
        tag.putByte("toggle", (byte) slot);
        send(tag);
        LegacyGuiElements.playClickSound();
    }

    private void bindSelection(int slot) {
        CompoundTag tag = new CompoundTag();
        tag.putByte("id", (byte) slot);
        for (int i = 0; i < selected.length; i++) {
            tag.putBoolean("s" + i, selected[i]);
        }
        send(tag);
        LegacyGuiElements.playClickSound();
    }

    private void pressAz5() {
        if (az5Lid) {
            az5Lid = false;
            LegacySoundPlayer.playSoundClient(console().getBlockPos(), "hbm:block.rbmk_az5_cover", 0.5F, 1.0F);
            return;
        }
        long now = System.currentTimeMillis();
        if (lastAz5Press + 3000L >= now) {
            return;
        }
        lastAz5Press = now;
        LegacySoundPlayer.playSoundClient(console().getBlockPos(), "hbm:block.shutdown", 1.0F, 1.0F);
        CompoundTag tag = new CompoundTag();
        tag.putDouble("level", 0.0D);
        RBMKConsolePlanner.ColumnSnapshot[] columns = console().columns();
        for (int i = 0; i < columns.length; i++) {
            RBMKConsolePlanner.ColumnSnapshot column = columns[i];
            if (column != null && column.type() == RBMKConsolePlanner.ColumnType.CONTROL) {
                tag.putInt("sel_" + i, i);
            }
        }
        send(tag);
        LegacyGuiElements.playClickSound();
    }

    private void selectAllManualControlRods() {
        RBMKConsolePlanner.ColumnSnapshot[] columns = console().columns();
        for (int i = 0; i < selected.length; i++) {
            RBMKConsolePlanner.ColumnSnapshot column = columnAt(columns, i);
            selected[i] = column != null && column.type() == RBMKConsolePlanner.ColumnType.CONTROL;
        }
    }

    private void selectManualColorGroup(int color) {
        RBMKConsolePlanner.ColumnSnapshot[] columns = console().columns();
        for (int i = 0; i < selected.length; i++) {
            RBMKConsolePlanner.ColumnSnapshot column = columnAt(columns, i);
            selected[i] = column != null
                    && column.type() == RBMKConsolePlanner.ColumnType.CONTROL
                    && column.data().getShort("color") == color;
        }
    }

    private void renderColumns(GuiGraphics graphics, RBMKConsolePlanner.ColumnSnapshot[] columns) {
        for (int i = 0; i < columns.length; i++) {
            RBMKConsolePlanner.ColumnSnapshot column = columns[i];
            if (column == null || column.type() == null) {
                continue;
            }
            int x = leftPos + GRID_X + CELL * (i % GRID_SIZE);
            int y = topPos + GRID_Y + CELL * (i / GRID_SIZE);
            graphics.blit(TEXTURE, x, y, column.type().offset(), 172, CELL, CELL);
            renderColumnOverlays(graphics, x, y, column);
            if (selected[i]) {
                graphics.blit(TEXTURE, x, y, 0, 192, CELL, CELL);
            }
        }
    }

    private void renderColumnOverlays(GuiGraphics graphics, int x, int y, RBMKConsolePlanner.ColumnSnapshot column) {
        CompoundTag data = column.data();
        int heat = Math.min(scaledCeil(data.getDouble("heat") - 20.0D, 10.0D, data.getDouble("maxHeat")), 10);
        graphics.blit(TEXTURE, x, y + CELL - heat, 0, 192 - heat, 10, heat);
        switch (column.type()) {
            case CONTROL -> {
                int color = data.getShort("color");
                if (color > -1) {
                    graphics.blit(TEXTURE, x, y, color * CELL, 202, CELL, CELL);
                }
                renderControlLevel(graphics, x, y, data);
            }
            case CONTROL_AUTO -> renderControlLevel(graphics, x, y, data);
            case FUEL, FUEL_SIM -> renderFuelColumn(graphics, x, y, data);
            case BOILER -> renderBoilerColumn(graphics, x, y, data);
            case HEATEX -> renderHeaterColumn(graphics, x, y, data);
            default -> {
            }
        }
    }

    private void renderControlLevel(GuiGraphics graphics, int x, int y, CompoundTag data) {
        int level = 8 - Mth.ceil(data.getDouble("level") * 8.0D);
        graphics.blit(TEXTURE, x + 4, y + 1, 24, 183, 2, level);
    }

    private void renderFuelColumn(GuiGraphics graphics, int x, int y, CompoundTag data) {
        if (!data.contains("c_heat")) {
            return;
        }
        int skin = Math.min(scaledCeil(data.getDouble("c_heat") - 20.0D, 8.0D, data.getDouble("c_maxHeat")), 8);
        graphics.blit(TEXTURE, x + 1, y + CELL - skin - 1, 11, 191 - skin, 2, skin);
        int enrichment = Math.min(Mth.ceil(data.getDouble("enrichment") * 8.0D), 8);
        graphics.blit(TEXTURE, x + 4, y + CELL - enrichment - 1, 14, 191 - enrichment, 2, enrichment);
        int xenon = Math.min(Mth.ceil(data.getDouble("xenon") * 8.0D / 100.0D), 8);
        graphics.blit(TEXTURE, x + 7, y + CELL - xenon - 1, 17, 191 - xenon, 2, xenon);
    }

    private void renderBoilerColumn(GuiGraphics graphics, int x, int y, CompoundTag data) {
        int water = scaledCeil(data.getInt("water"), 8.0D, data.getDouble("maxWater"));
        graphics.blit(TEXTURE, x + 1, y + CELL - water - 1, 41, 191 - water, 3, water);
        int steam = scaledCeil(data.getInt("steam"), 8.0D, data.getDouble("maxSteam"));
        graphics.blit(TEXTURE, x + 6, y + CELL - steam - 1, 46, 191 - steam, 3, steam);
        short type = data.getShort("type");
        if (type == HbmFluids.STEAM.getId()) {
            graphics.blit(TEXTURE, x + 4, y + 1, 44, 183, 2, 2);
        }
        if (type == HbmFluids.HOTSTEAM.getId()) {
            graphics.blit(TEXTURE, x + 4, y + 3, 44, 185, 2, 2);
        }
        if (type == HbmFluids.SUPERHOTSTEAM.getId()) {
            graphics.blit(TEXTURE, x + 4, y + 5, 44, 187, 2, 2);
        }
        if (type == HbmFluids.ULTRAHOTSTEAM.getId()) {
            graphics.blit(TEXTURE, x + 4, y + 7, 44, 189, 2, 2);
        }
    }

    private void renderHeaterColumn(GuiGraphics graphics, int x, int y, CompoundTag data) {
        int cold = scaledCeil(data.getInt("water"), 8.0D, data.getDouble("maxWater"));
        graphics.blit(TEXTURE, x + 1, y + CELL - cold - 1, 131, 191 - cold, 3, cold);
        int hot = scaledCeil(data.getInt("steam"), 8.0D, data.getDouble("maxSteam"));
        graphics.blit(TEXTURE, x + 6, y + CELL - hot - 1, 136, 191 - hot, 3, hot);
    }

    private int scaledCeil(double fill, double scale, double max) {
        if (max == 0.0D) {
            return 0;
        }
        return Mth.ceil(fill * scale / max);
    }

    private void drawScaledString(GuiGraphics graphics, String text, int x, int y) {
        graphics.drawString(font, text, (int) ((leftPos + x) / 0.5F), (int) ((topPos + y) / 0.5F),
                0x00FF00, false);
    }
}
