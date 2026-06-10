package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientSatelliteData;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.satellite.SatelliteInterfaceItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class SatelliteCoordScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/satellites/gui_sat_coord.png");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 126;

    private final InteractionHand hand;
    private int leftPos;
    private int topPos;
    private EditBox xField;
    private EditBox yField;
    private EditBox zField;

    public SatelliteCoordScreen(InteractionHand hand) {
        super(Component.translatable("item.hbm_ntm_rebirth.sat_coord"));
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
    }

    @Override
    protected void init() {
        leftPos = (width - IMAGE_WIDTH) / 2;
        topPos = (height - IMAGE_HEIGHT) / 2;
        xField = field(leftPos + 66, topPos + 21);
        yField = field(leftPos + 66, topPos + 56);
        zField = field(leftPos + 66, topPos + 92);
        addRenderableWidget(xField);
        addRenderableWidget(yField);
        addRenderableWidget(zField);
        setInitialFocus(xField);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isHeldSatelliteInterface()) {
            onClose();
        }
        updateYFieldState();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        updateYFieldState();
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        renderFocus(graphics);
        ClientSatelliteData.current(currentFrequency()).ifPresent(snapshot -> {
            Satellite satellite = snapshot.satellite();
            if (!satellite.coordActions().contains(Satellite.CoordAction.HAS_Y)) {
                graphics.blit(TEXTURE, leftPos + 61, topPos + 52, 0, 144, 54, 18);
            }
            graphics.blit(TEXTURE, leftPos + 120, topPos + 17, 194, 0, 7, 7);
            if (satellite.satelliteInterface() == Satellite.SatelliteInterface.SAT_COORD) {
                graphics.blit(TEXTURE, leftPos + 120, topPos + 25, 194, 0, 7, 7);
            }
        });
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && mouseX >= leftPos + 133 && mouseX < leftPos + 151
                && mouseY >= topPos + 52 && mouseY < topPos + 70) {
            return sendCoordinateAction();
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue()) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private EditBox field(int x, int y) {
        return LegacyGuiElements.createLegacyTextField(font, x, y, 48, 12, 7, "", 0xFFFFFF);
    }

    private void renderFocus(GuiGraphics graphics) {
        if (xField.isFocused()) {
            graphics.blit(TEXTURE, leftPos + 61, topPos + 16, 0, 126, 54, 18);
        }
        if (yField.visible && yField.isFocused()) {
            graphics.blit(TEXTURE, leftPos + 61, topPos + 52, 0, 126, 54, 18);
        }
        if (zField.isFocused()) {
            graphics.blit(TEXTURE, leftPos + 61, topPos + 88, 0, 126, 54, 18);
        }
    }

    private boolean sendCoordinateAction() {
        ClientSatelliteData.SatelliteSnapshot snapshot = ClientSatelliteData.current(currentFrequency()).orElse(null);
        if (snapshot == null || snapshot.satellite().satelliteInterface() != Satellite.SatelliteInterface.SAT_COORD) {
            return false;
        }
        Integer x = parseLegacyCoordinate(xField.getValue());
        Integer z = parseLegacyCoordinate(zField.getValue());
        if (x == null || z == null) {
            return false;
        }
        int y = 0;
        if (snapshot.satellite().coordActions().contains(Satellite.CoordAction.HAS_Y)) {
            Integer parsedY = parseLegacyCoordinate(yField.getValue());
            if (parsedY == null) {
                return false;
            }
            y = parsedY;
        }
        ModMessages.sendSatCoord(hand, x, y, z, currentFrequency());
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.TOOL_TECH_BLEEP.get(), 1.0F));
        onClose();
        return true;
    }

    private Integer parseLegacyCoordinate(String value) {
        try {
            double parsed = Double.parseDouble(value.trim());
            if (!Double.isFinite(parsed)) {
                return null;
            }
            return (int) parsed;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void updateYFieldState() {
        boolean hasY = ClientSatelliteData.current(currentFrequency())
                .map(snapshot -> snapshot.satellite().coordActions().contains(Satellite.CoordAction.HAS_Y))
                .orElse(false);
        yField.visible = hasY;
        yField.active = hasY;
        if (!hasY && yField.isFocused()) {
            yField.setFocused(false);
        }
    }

    private int currentFrequency() {
        if (minecraft == null || minecraft.player == null) {
            return 0;
        }
        return ISatelliteChip.getFrequencyFromStack(minecraft.player.getItemInHand(hand));
    }

    private boolean isHeldSatelliteInterface() {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }
        ItemStack stack = minecraft.player.getItemInHand(hand);
        return stack.getItem() instanceof SatelliteInterfaceItem item
                && item.mode() == SatelliteInterfaceItem.Mode.COORD;
    }
}
