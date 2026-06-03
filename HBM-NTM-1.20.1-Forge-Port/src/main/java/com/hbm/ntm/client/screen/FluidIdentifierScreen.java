package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.network.ModMessages;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class FluidIdentifierScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_fluid.png");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 54;
    private static final int VISIBLE_FLUIDS = 9;

    private final InteractionHand hand;
    private final FluidType[] visibleFluids = new FluidType[VISIBLE_FLUIDS];
    private int leftPos;
    private int topPos;
    private EditBox search;
    private FluidType primary = HbmFluids.NONE;
    private FluidType secondary = HbmFluids.NONE;

    public FluidIdentifierScreen(InteractionHand hand) {
        super(Component.translatable("item.hbm.fluid_identifier_multi"));
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
    }

    @Override
    protected void init() {
        leftPos = (width - IMAGE_WIDTH) / 2;
        topPos = (height - IMAGE_HEIGHT) / 2;
        search = new EditBox(font, leftPos + 46, topPos + 11, 86, 12, Component.empty());
        search.setBordered(false);
        search.setTextColor(0xFFFFFF);
        search.setMaxLength(32);
        addRenderableWidget(search);
        setInitialFocus(search);
        refreshHeldTypes();
        updateSearch();
    }

    @Override
    public void tick() {
        super.tick();
        if (!isHeldIdentifier()) {
            onClose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        if (search.isFocused()) {
            graphics.blit(TEXTURE, leftPos + 43, topPos + 7, 166, 54, 90, 18);
        }
        renderFluidButtons(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderHover(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int index = hoveredFluidIndex(mouseX, mouseY);
        if (index >= 0 && visibleFluids[index] != null) {
            FluidType selected = visibleFluids[index];
            boolean primaryClick = button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
            boolean secondaryClick = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
            if (primaryClick || secondaryClick) {
                sendSelection(selected, primaryClick);
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                if (primaryClick) {
                    primary = selected;
                } else {
                    secondary = selected;
                }
                updateHeldStack(selected, primaryClick);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean typed = super.charTyped(codePoint, modifiers);
        updateSearch();
        return typed;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue()) {
            onClose();
            return true;
        }
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        updateSearch();
        return handled;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderFluidButtons(GuiGraphics graphics) {
        for (int i = 0; i < visibleFluids.length; i++) {
            FluidType type = visibleFluids[i];
            if (type == null) {
                return;
            }

            int x = leftPos + 7 + i * 18;
            int y = topPos + 29;
            int color = type.getColor();
            graphics.setColor(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F,
                    (color & 0xFF) / 255.0F, 1.0F);
            graphics.blit(TEXTURE, x + 5, y + 2, 12 + i * 18, 56, 8, 14);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

            if (type == primary && type == secondary) {
                graphics.blit(TEXTURE, x, y, 176, 36, 18, 18);
            } else if (type == primary) {
                graphics.blit(TEXTURE, x, y, 176, 0, 18, 18);
            } else if (type == secondary) {
                graphics.blit(TEXTURE, x, y, 176, 18, 18, 18);
            }
        }
    }

    private void renderHover(GuiGraphics graphics, int mouseX, int mouseY) {
        int index = hoveredFluidIndex(mouseX, mouseY);
        if (index < 0 || visibleFluids[index] == null) {
            return;
        }
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(visibleFluids[index].getDisplayName());
        visibleFluids[index].appendInfo(tooltip, hasShiftDown());
        graphics.renderTooltip(font, tooltip.stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
    }

    private void updateSearch() {
        for (int i = 0; i < visibleFluids.length; i++) {
            visibleFluids[i] = null;
        }
        String needle = search == null ? "" : search.getValue().toLowerCase(Locale.ROOT);
        int next = 0;
        for (FluidType type : HbmFluids.niceOrder()) {
            if (type.hasNoId()) {
                continue;
            }
            String displayName = type.getDisplayName().getString().toLowerCase(Locale.ROOT);
            String fallbackName = type.getFallbackDisplayName().getString().toLowerCase(Locale.ROOT);
            if (needle.isBlank() || displayName.contains(needle) || fallbackName.contains(needle) || type.toPath().contains(needle)) {
                visibleFluids[next++] = type;
                if (next >= visibleFluids.length) {
                    return;
                }
            }
        }
    }

    private int hoveredFluidIndex(double mouseX, double mouseY) {
        if (mouseY <= topPos + 29 || mouseY > topPos + 47) {
            return -1;
        }
        for (int i = 0; i < visibleFluids.length; i++) {
            int x = leftPos + 7 + i * 18;
            if (mouseX >= x && mouseX < x + 18) {
                return i;
            }
        }
        return -1;
    }

    private boolean isHeldIdentifier() {
        return minecraft != null
                && minecraft.player != null
                && minecraft.player.getItemInHand(hand).getItem() instanceof FluidIdentifierItem;
    }

    private void refreshHeldTypes() {
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        ItemStack stack = minecraft.player.getItemInHand(hand);
        if (stack.getItem() instanceof FluidIdentifierItem) {
            primary = FluidIdentifierItem.getType(stack, true);
            secondary = FluidIdentifierItem.getType(stack, false);
        }
    }

    private void sendSelection(FluidType type, boolean primarySelection) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(primarySelection ? "primary" : "secondary", type.getId());
        ModMessages.sendNbtItemControl(hand, tag);
    }

    private void updateHeldStack(FluidType type, boolean primarySelection) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        ItemStack stack = minecraft.player.getItemInHand(hand);
        if (stack.getItem() instanceof FluidIdentifierItem) {
            FluidIdentifierItem.setType(stack, type, primarySelection);
        }
    }

}
