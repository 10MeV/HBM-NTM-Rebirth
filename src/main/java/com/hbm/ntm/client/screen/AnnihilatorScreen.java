package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.menu.AnnihilatorMenu;
import com.hbm.ntm.network.ModMessages;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class AnnihilatorScreen extends AbstractContainerScreen<AnnihilatorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_annihilator.png");
    private static final NumberFormat COUNT_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    private EditBox poolField;
    private boolean updatingPoolField;

    public AnnihilatorScreen(AnnihilatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 208;
        titleLabelX = 70;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();
        poolField = addRenderableWidget(LegacyGuiElements.createLegacyTextField(font,
                leftPos + 31, topPos + 85, 80, 8, 20, menu.getPool()));
        poolField.setResponder(this::poolChanged);
    }

    private void poolChanged(String value) {
        if (updatingPoolField) {
            return;
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("pool", value);
        ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (poolField != null && !poolField.isFocused() && !poolField.getValue().equals(menu.getPool())) {
            updatingPoolField = true;
            poolField.setValue(menu.getPool());
            updatingPoolField = false;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = 70 - font.width(title) / 2;
        graphics.drawString(font, title, titleX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(151, 35, 18, 18, mouseX, mouseY)) {
            List<Component> tooltip = monitorTooltip();
            if (!tooltip.isEmpty()) {
                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private List<Component> monitorTooltip() {
        ItemStack stack = menu.getMonitorStack();
        if (stack.isEmpty()) {
            return List.of();
        }
        Component name;
        if (stack.getItem() instanceof IFluidIdentifierItem identifier) {
            name = identifier.getIdentifiedFluid(minecraft == null ? null : minecraft.level,
                    menu.getBlockEntity().getBlockPos(), stack).getDisplayName();
        } else {
            name = stack.getHoverName();
        }
        BigInteger amount = menu.getMonitorBigInt();
        return List.of(
                name.copy().append(Component.literal(":")),
                Component.literal(COUNT_FORMAT.format(amount)));
    }
}
