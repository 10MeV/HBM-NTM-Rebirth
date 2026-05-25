package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.NuclearDeviceBlock;
import com.hbm.ntm.menu.NuclearDeviceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class NuclearDeviceScreen extends AbstractContainerScreen<NuclearDeviceMenu> {
    private final ResourceLocation texture;

    public NuclearDeviceScreen(NuclearDeviceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = menu.layout().imageWidth();
        imageHeight = menu.layout().imageHeight();
        inventoryLabelX = menu.layout().playerInventoryX();
        inventoryLabelY = imageHeight - 94;
        texture = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/weapon/" + menu.layout().texturePath() + ".png");
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderLegacyStatus(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, titleY(), 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private int titleY() {
        return menu.kind() == NuclearDeviceBlock.Kind.MIKE ? 4 : 6;
    }

    private void renderLegacyStatus(GuiGraphics graphics) {
        switch (menu.kind()) {
            case GADGET, MAN -> renderImplosionStatus(graphics);
            case BOY -> renderBoyStatus(graphics);
            case TSAR -> renderTsarStatus(graphics);
            case MIKE -> renderMikeStatus(graphics);
            case FLEIJA -> renderFleijaStatus(graphics);
            case SOLINIUM -> renderSoliniumStatus(graphics);
            case N2 -> renderN2Status(graphics);
            case PROTOTYPE -> {
            }
        }
    }

    private void renderImplosionStatus(GuiGraphics graphics) {
        if (menu.hasComponent(1)) {
            graphics.blit(texture, leftPos + 82, topPos + 19, 176, 0, 24, 24);
        }
        if (menu.hasComponent(2)) {
            graphics.blit(texture, leftPos + 106, topPos + 19, 200, 0, 24, 24);
        }
        if (menu.hasComponent(3)) {
            graphics.blit(texture, leftPos + 82, topPos + 43, 176, 24, 24, 24);
        }
        if (menu.hasComponent(4)) {
            graphics.blit(texture, leftPos + 106, topPos + 43, 200, 24, 24, 24);
        }
        if (menu.isReady()) {
            graphics.blit(texture, leftPos + 134, topPos + 35, 176, 48, 16, 16);
        }
    }

    private void renderBoyStatus(GuiGraphics graphics) {
        if (menu.isReady()) {
            graphics.blit(texture, leftPos + 142, topPos + 90, 176, 0, 16, 16);
        }
        if (menu.hasComponent(0)) {
            graphics.blit(texture, leftPos + 27, topPos + 87, 176, 16, 21, 22);
        }
        if (menu.hasComponent(1)) {
            graphics.blit(texture, leftPos + 27, topPos + 89, 176, 38, 21, 18);
        }
        if (menu.hasComponent(2)) {
            graphics.blit(texture, leftPos + 74, topPos + 94, 176, 57, 19, 8);
        }
        if (menu.hasComponent(3)) {
            graphics.blit(texture, leftPos + 92, topPos + 95, 176, 66, 12, 6);
        }
        if (menu.hasComponent(4)) {
            graphics.blit(texture, leftPos + 107, topPos + 91, 176, 75, 16, 14);
        }
    }

    private void renderTsarStatus(GuiGraphics graphics) {
        if (menu.isFilled()) {
            graphics.blit(texture, leftPos + 18, topPos + 50, 176, 18, 16, 16);
        } else if (menu.isReady()) {
            graphics.blit(texture, leftPos + 18, topPos + 50, 176, 0, 16, 16);
        }
        if (menu.hasComponent(0)) {
            graphics.blit(texture, leftPos + 40, topPos + 36, 209, 1, 23, 23);
        }
        if (menu.hasComponent(2)) {
            graphics.blit(texture, leftPos + 63, topPos + 36, 232, 1, 23, 23);
        }
        if (menu.hasComponent(1)) {
            graphics.blit(texture, leftPos + 40, topPos + 59, 209, 24, 23, 23);
        }
        if (menu.hasComponent(3)) {
            graphics.blit(texture, leftPos + 63, topPos + 59, 232, 24, 23, 23);
        }
        if (menu.hasComponent(5)) {
            graphics.blit(texture, leftPos + 91, topPos + 41, 176, 220, 80, 36);
        }
    }

    private void renderMikeStatus(GuiGraphics graphics) {
        if (menu.isFilled()) {
            graphics.blit(texture, leftPos + 142, topPos + 109, 176, 18, 16, 16);
        } else if (menu.isReady()) {
            graphics.blit(texture, leftPos + 142, topPos + 109, 176, 0, 16, 16);
        }
        int[][] lenses = {{0, 40, 52, 209, 1}, {2, 63, 52, 232, 1}, {1, 40, 75, 209, 24}, {3, 63, 75, 232, 24}};
        for (int[] lens : lenses) {
            if (menu.hasComponent(lens[0])) {
                graphics.blit(texture, leftPos + lens[1], topPos + lens[2], lens[3], lens[4], 23, 23);
            }
        }
        if (menu.hasComponent(6)) {
            graphics.blit(texture, leftPos + 129, topPos + 50, 176, 36, 16, 32);
        }
        if (menu.hasComponent(7)) {
            graphics.blit(texture, leftPos + 105, topPos + 50, 192, 36, 16, 32);
        }
    }

    private void renderFleijaStatus(GuiGraphics graphics) {
        if (menu.isReady()) {
            graphics.blit(texture, leftPos + 143, topPos + 91, 176, 0, 16, 16);
        }
    }

    private void renderSoliniumStatus(GuiGraphics graphics) {
        if (menu.isReady()) {
            graphics.blit(texture, leftPos + 143, topPos + 91, 176, 0, 16, 16);
        }
    }

    private void renderN2Status(GuiGraphics graphics) {
        if (menu.isReady()) {
            graphics.blit(texture, leftPos + 143, topPos + 109, 176, 0, 16, 16);
        }
    }
}
