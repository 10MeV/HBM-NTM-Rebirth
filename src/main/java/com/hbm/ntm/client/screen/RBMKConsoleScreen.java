package com.hbm.ntm.client.screen;

import com.hbm.ntm.blockentity.RBMKConsoleBlockEntity;
import com.hbm.ntm.menu.RBMKConsoleMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.neutron.RBMKConsolePlanner;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RBMKConsoleScreen extends AbstractContainerScreen<RBMKConsoleMenu> {
    private static final int GRID_X = 8;
    private static final int GRID_Y = 20;
    private static final int CELL = 8;
    private static final int GRID_SIZE = RBMKConsolePlanner.CONSOLE_GRID_SIZE;
    private final boolean[] selected = new boolean[GRID_SIZE * GRID_SIZE];
    private EditBox levelBox;
    private int colorIndex;

    public RBMKConsoleScreen(RBMKConsoleMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 256;
        imageHeight = 214;
        titleLabelX = 8;
        titleLabelY = 8;
        inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        levelBox = LegacyGuiElements.createLegacyTextField(font, leftPos + 150, topPos + 141,
                34, 14, 4, "0");
        addRenderableWidget(levelBox);
        for (int slot = 0; slot < RBMKConsolePlanner.CONSOLE_SCREEN_COUNT; slot++) {
            int y = topPos + 18 + slot * 18;
            final int screenSlot = slot;
            addRenderableWidget(Button.builder(Component.literal("T" + slot),
                    button -> toggleScreen(screenSlot))
                    .bounds(leftPos + 150, y, 28, 14)
                    .build());
            addRenderableWidget(Button.builder(Component.literal("S" + slot),
                    button -> bindSelection(screenSlot))
                    .bounds(leftPos + 182, y, 28, 14)
                    .build());
        }
        addRenderableWidget(Button.builder(Component.literal("L"), button -> applyLevel())
                .bounds(leftPos + 188, topPos + 141, 18, 14)
                .build());
        addRenderableWidget(Button.builder(Component.literal("C"), button -> assignColor())
                .bounds(leftPos + 210, topPos + 141, 18, 14)
                .build());
        addRenderableWidget(Button.builder(Component.literal("B"), button -> cycleCompressor())
                .bounds(leftPos + 232, topPos + 141, 18, 14)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xff1c1c1c);
        graphics.fill(leftPos + GRID_X - 1, topPos + GRID_Y - 1,
                leftPos + GRID_X + GRID_SIZE * CELL + 1, topPos + GRID_Y + GRID_SIZE * CELL + 1, 0xff404040);
        RBMKConsolePlanner.ColumnSnapshot[] columns = console().columns();
        for (int index = 0; index < GRID_SIZE * GRID_SIZE; index++) {
            int x = leftPos + GRID_X + (index % GRID_SIZE) * CELL;
            int y = topPos + GRID_Y + (index / GRID_SIZE) * CELL;
            graphics.fill(x, y, x + CELL - 1, y + CELL - 1, columnColor(columnAt(columns, index)));
            if (selected[index]) {
                graphics.renderOutline(x, y, CELL - 1, CELL - 1, 0xffffffff);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xe0e0e0, false);
        RBMKConsoleBlockEntity console = console();
        graphics.drawString(font, Component.literal("Target " + console.target().toShortString()),
                8, 146, 0xb0b0b0, false);
        int[] flux = console.fluxBuffer();
        int lastFlux = flux.length == 0 ? 0 : flux[flux.length - 1];
        graphics.drawString(font, Component.literal("Flux " + lastFlux), 8, 158, 0xb0b0b0, false);

        RBMKConsolePlanner.ScreenState[] screens = console.screens();
        for (int slot = 0; slot < Math.min(screens.length, RBMKConsolePlanner.CONSOLE_SCREEN_COUNT); slot++) {
            RBMKConsolePlanner.ScreenState screen = screens[slot];
            String display = screen.display() == null ? screen.type().name() : screen.display();
            graphics.drawString(font, Component.literal(display), 214, 22 + slot * 18, 0xd0d0d0, false);
        }
        graphics.drawString(font, Component.literal("Level").withStyle(ChatFormatting.GRAY),
                150, 130, 0xa0a0a0, false);
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
            selected[index] = !selected[index];
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void toggleScreen(int slot) {
        CompoundTag tag = new CompoundTag();
        tag.putByte("toggle", (byte) slot);
        send(tag);
    }

    private void bindSelection(int slot) {
        CompoundTag tag = new CompoundTag();
        tag.putByte("id", (byte) slot);
        for (int i = 0; i < selected.length; i++) {
            tag.putBoolean("s" + i, selected[i]);
        }
        send(tag);
    }

    private void applyLevel() {
        CompoundTag tag = selectedIndexTag();
        tag.putDouble("level", parsePercent(levelBox.getValue()));
        send(tag);
    }

    private void assignColor() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("assignColor", (byte) colorIndex++);
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

    private static int columnColor(RBMKConsolePlanner.ColumnSnapshot column) {
        if (column == null || column.type() == null) {
            return 0xff101010;
        }
        return switch (column.type()) {
            case FUEL, FUEL_SIM -> 0xff4f8f4f;
            case CONTROL, CONTROL_AUTO -> 0xff4f6f9f;
            case BOILER, HEATEX, COOLER -> 0xff4f8f9f;
            case MODERATOR -> 0xff606060;
            case ABSORBER -> 0xff5f4f7f;
            case REFLECTOR -> 0xffa0a0a0;
            case OUTGASSER -> 0xff8f7f4f;
            case STORAGE -> 0xff7f6f4f;
            case BREEDER -> 0xff6f8f5f;
            case BLANK -> 0xff303030;
        };
    }

    private RBMKConsoleBlockEntity console() {
        return menu.getBlockEntity();
    }

    private void send(CompoundTag tag) {
        ModMessages.sendTileControl(console().getBlockPos(), tag);
    }
}
