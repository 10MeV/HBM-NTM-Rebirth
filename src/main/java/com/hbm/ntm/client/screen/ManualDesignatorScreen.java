package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.MissileDesignatorItem;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ManualDesignatorScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_designator.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 178;
    private static final List<DesignatorButton> BUTTONS = List.of(
            new DesignatorButton(25, 26, 0, 0, 0, 1, ""),
            new DesignatorButton(52, 26, 1, 0, 0, 5, ""),
            new DesignatorButton(79, 26, 2, 0, 0, 10, ""),
            new DesignatorButton(106, 26, 3, 0, 0, 50, ""),
            new DesignatorButton(133, 26, 4, 0, 0, 100, ""),
            new DesignatorButton(25, 62, 5, 1, 0, 1, ""),
            new DesignatorButton(52, 62, 6, 1, 0, 5, ""),
            new DesignatorButton(79, 62, 7, 1, 0, 10, ""),
            new DesignatorButton(106, 62, 8, 1, 0, 50, ""),
            new DesignatorButton(133, 62, 9, 1, 0, 100, ""),
            new DesignatorButton(133, 44, 10, 2, 0, 0, "Set coord to current X position..."),
            new DesignatorButton(25, 98, 0, 0, 1, 1, ""),
            new DesignatorButton(52, 98, 1, 0, 1, 5, ""),
            new DesignatorButton(79, 98, 2, 0, 1, 10, ""),
            new DesignatorButton(106, 98, 3, 0, 1, 50, ""),
            new DesignatorButton(133, 98, 4, 0, 1, 100, ""),
            new DesignatorButton(25, 134, 5, 1, 1, 1, ""),
            new DesignatorButton(52, 134, 6, 1, 1, 5, ""),
            new DesignatorButton(79, 134, 7, 1, 1, 10, ""),
            new DesignatorButton(106, 134, 8, 1, 1, 50, ""),
            new DesignatorButton(133, 134, 9, 1, 1, 100, ""),
            new DesignatorButton(133, 116, 10, 2, 1, 0, "Set coord to current Z position...")
    );

    private final InteractionHand hand;
    private int shownX;
    private int shownZ;

    public ManualDesignatorScreen(InteractionHand hand) {
        super(Component.translatable("item.hbm_ntm_rebirth.designator_manual"));
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
    }

    public static void open(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new ManualDesignatorScreen(hand));
    }

    @Override
    protected void init() {
        ItemStack stack = heldStack();
        shownX = stack.getOrCreateTag().getInt(MissileDesignatorItem.TAG_X);
        shownZ = stack.getOrCreateTag().getInt(MissileDesignatorItem.TAG_Z);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = left();
        int top = top();
        graphics.blit(TEXTURE, left, top, 0, 0, WIDTH, HEIGHT);
        for (DesignatorButton button : BUTTONS) {
            boolean hovered = button.contains(mouseX - left, mouseY - top);
            graphics.blit(TEXTURE, left + button.x(), top + button.y(), hovered ? 194 : 176, button.type() * 18, 18, 18);
            if (hovered && !button.tooltip().isEmpty()) {
                graphics.renderTooltip(font, Component.literal(button.tooltip()), mouseX, mouseY);
            }
        }
        String x = "X: " + shownX;
        String z = "Z: " + shownZ;
        graphics.drawString(font, x, left + WIDTH / 2 - font.width(x) / 2, top + 50, 0x404040, false);
        graphics.drawString(font, z, left + WIDTH / 2 - font.width(z) / 2, top + 122, 0x404040, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        int relX = (int) mouseX - left();
        int relY = (int) mouseY - top();
        for (DesignatorButton button : BUTTONS) {
            if (button.contains(relX, relY)) {
                execute(button);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, buttonId);
    }

    @Override
    public void tick() {
        if (heldStack().isEmpty() || heldStack().getItem() != ModItems.DESIGNATOR_MANUAL.get()) {
            onClose();
        }
    }

    private void execute(DesignatorButton button) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        ModMessages.sendDesignatorAction(hand, button.operator(), button.value(), button.reference());
        int result = button.operator() == 1 ? -button.value() : button.value();
        if (button.operator() == 2 && minecraft.player != null) {
            BlockPos pos = minecraft.player.blockPosition();
            if (button.reference() == 0) {
                shownX = pos.getX();
            } else {
                shownZ = pos.getZ();
            }
            return;
        }
        if (button.reference() == 0) {
            shownX += result;
        } else {
            shownZ += result;
        }
    }

    private ItemStack heldStack() {
        return minecraft == null || minecraft.player == null ? ItemStack.EMPTY : minecraft.player.getItemInHand(hand);
    }

    private int left() {
        return (width - WIDTH) / 2;
    }

    private int top() {
        return (height - HEIGHT) / 2;
    }

    private record DesignatorButton(int x, int y, int type, int operator, int reference, int value, String tooltip) {
        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX < x + 18 && mouseY > y && mouseY <= y + 18;
        }
    }
}
