package com.hbm.ntm.client.screen;

import com.hbm.items.special.ItemHolotapeImage;
import com.hbm.ntm.registry.ModSounds;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/** Modern no-container equivalent of 1.7.10 {@code GUIScreenHolotape}. */
public final class HolotapeScreen extends Screen {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 150;
    private static final int TEXT_COLOR = 0x009900;
    private static final int PANEL_COLOR = 0xCC003300;

    private final InteractionHand hand;
    private ItemHolotapeImage.EnumHoloImage holo;

    public HolotapeScreen(InteractionHand hand) {
        super(Component.empty());
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
    }

    @Override
    protected void init() {
        if (minecraft == null || minecraft.player == null) {
            onClose();
            return;
        }
        ItemStack stack = minecraft.player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ItemHolotapeImage)) {
            onClose();
            return;
        }
        holo = ItemHolotapeImage.typeFor(stack);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.BLOCK_BOBBLE.get(), 1.0F));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        if (holo == null) {
            return;
        }
        int left = (width - WIDTH) / 2;
        int top = (height - HEIGHT) / 2;
        graphics.fill(left, top, left + WIDTH, top + HEIGHT, PANEL_COLOR);

        int nextLine = top + 30;
        List<FormattedCharSequence> lines = font.split(Component.literal(holo.getText()), 275);
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, left + WIDTH / 2 - font.width(line) / 2, nextLine, TEXT_COLOR, true);
            nextLine += 10;
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
}
