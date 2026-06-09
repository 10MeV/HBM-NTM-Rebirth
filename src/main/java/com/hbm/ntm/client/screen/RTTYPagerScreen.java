package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.RTTYPagerItem;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class RTTYPagerScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/machine/gui_rtty_pager.png");
    private static final int IMAGE_WIDTH = 184;
    private static final int IMAGE_HEIGHT = 42;
    private static final int SAVE_X = 137;
    private static final int SAVE_Y = 17;
    private static final int SAVE_SIZE = 18;

    private final InteractionHand hand;
    private int leftPos;
    private int topPos;
    private EditBox channelField;

    public RTTYPagerScreen(InteractionHand hand) {
        super(Component.translatable("container.rttyPager"));
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
    }

    @Override
    protected void init() {
        leftPos = (width - IMAGE_WIDTH) / 2;
        topPos = (height - IMAGE_HEIGHT) / 2;
        channelField = LegacyGuiElements.createLegacyTextField(font, leftPos + 31, topPos + 23, 82, 14, 10,
                currentChannel());
        addRenderableWidget(channelField);
        setInitialFocus(channelField);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isHeldPager()) {
            onClose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        super.render(graphics, mouseX, mouseY, partialTick);
        LegacyGuiElements.drawCenteredLabel(graphics, font, title, leftPos + IMAGE_WIDTH / 2, topPos + 6,
                IMAGE_WIDTH - 12, 0x404040);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, SAVE_X, SAVE_Y, SAVE_SIZE, SAVE_SIZE)) {
            saveChannel();
            LegacyGuiElements.playClickSound();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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

    private void saveChannel() {
        String channelName = channelField == null ? "" : channelField.getValue();
        CompoundTag tag = new CompoundTag();
        tag.putString(RTTYPagerItem.KEY_CHANNEL, channelName);
        ModMessages.sendNbtItemControl(hand, tag);
        updateHeldStack(channelName);
    }

    private String currentChannel() {
        if (minecraft == null || minecraft.player == null) {
            return "";
        }
        ItemStack stack = minecraft.player.getItemInHand(hand);
        return stack.getItem() instanceof RTTYPagerItem ? RTTYPagerItem.getChannel(stack) : "";
    }

    private boolean isHeldPager() {
        return minecraft != null
                && minecraft.player != null
                && minecraft.player.getItemInHand(hand).getItem() instanceof RTTYPagerItem;
    }

    private void updateHeldStack(String channelName) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        ItemStack stack = minecraft.player.getItemInHand(hand);
        if (stack.getItem() instanceof RTTYPagerItem) {
            RTTYPagerItem.setChannel(stack, channelName);
        }
    }
}
