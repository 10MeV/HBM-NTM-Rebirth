package com.hbm.ntm.client.screen;

import com.hbm.ntm.block.TrinketVariant;
import com.hbm.ntm.registry.ModSounds;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

/** Modern no-container equivalent of 1.7.10 GUIScreenBobble and GUIScreenSnowglobe. */
public final class TrinketInfoScreen extends Screen {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 150;
    private static final int TITLE_COLOR = 0x00FF00;
    private static final int TEXT_COLOR = 0x009900;
    private static final int PANEL_COLOR = 0xCC003300;

    private final TrinketVariant.Kind kind;
    private final int variant;

    public TrinketInfoScreen(TrinketVariant.Kind kind, int variant) {
        super(Component.empty());
        this.kind = kind;
        this.variant = TrinketVariant.clamp(kind, variant);
    }

    @Override
    protected void init() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.BLOCK_BOBBLE.get(), 1.0F));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = (width - WIDTH) / 2;
        int top = (height - HEIGHT) / 2;
        graphics.fill(left, top, left + WIDTH, top + HEIGHT, PANEL_COLOR);

        List<Component> lines = TrinketVariant.infoLines(kind, variant);
        int nextLine = top + 10;
        if (!lines.isEmpty()) {
            drawCentered(graphics, lines.get(0), left, nextLine, TITLE_COLOR);
            nextLine += 10;
        }
        if (lines.size() > 1) {
            drawCentered(graphics, lines.get(1), left, nextLine, TEXT_COLOR);
            nextLine += 20;
        }
        int index = 2;
        if (index < lines.size() && lines.get(index).getString().equals("Has contributed")) {
            drawCentered(graphics, lines.get(index++), left, nextLine, TITLE_COLOR);
            nextLine += 10;
            while (index < lines.size()
                    && !lines.get(index).getString().equals("On the bottom is the following inscription:")) {
                drawCentered(graphics, lines.get(index++), left, nextLine, TEXT_COLOR);
                nextLine += 10;
            }
            nextLine += 10;
        }
        if (index < lines.size()
                && lines.get(index).getString().equals("On the bottom is the following inscription:")) {
            drawCentered(graphics, lines.get(index++), left, nextLine, TITLE_COLOR);
            nextLine += 10;
            while (index < lines.size()) {
                for (FormattedCharSequence part : font.split(lines.get(index++), 280)) {
                    graphics.drawString(font, part, left + WIDTH / 2 - font.width(part) / 2,
                            nextLine, TEXT_COLOR, true);
                    nextLine += 10;
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawCentered(GuiGraphics graphics, Component line, int left, int y, int color) {
        graphics.drawString(font, line, left + WIDTH / 2 - font.width(line) / 2, y, color, true);
    }
}
