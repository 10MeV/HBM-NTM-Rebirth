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
    private static final ResourceLocation MIKE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/weapon/ivy_mike_schematic.png");

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
        blit(graphics, texture, 0, 0, 0, 0, imageWidth, imageHeight);
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
            blit(graphics, texture, 82, 19, 176, 0, 24, 24);
        }
        if (menu.hasComponent(2)) {
            blit(graphics, texture, 106, 19, 200, 0, 24, 24);
        }
        if (menu.hasComponent(3)) {
            blit(graphics, texture, 82, 43, 176, 24, 24, 24);
        }
        if (menu.hasComponent(4)) {
            blit(graphics, texture, 106, 43, 200, 24, 24, 24);
        }
        if (menu.isReady()) {
            blit(graphics, texture, 134, 35, 176, 48, 16, 16);
        }
    }

    private void renderBoyStatus(GuiGraphics graphics) {
        if (menu.isReady()) {
            blit(graphics, texture, 142, 90, 176, 0, 16, 16);
        }
        if (menu.hasComponent(0)) {
            blit(graphics, texture, 27, 87, 176, 16, 21, 22);
        }
        if (menu.hasComponent(1)) {
            blit(graphics, texture, 27, 89, 176, 38, 21, 18);
        }
        if (menu.hasComponent(2)) {
            blit(graphics, texture, 74, 94, 176, 57, 19, 8);
        }
        if (menu.hasComponent(3)) {
            blit(graphics, texture, 92, 95, 176, 66, 12, 6);
        }
        if (menu.hasComponent(4)) {
            blit(graphics, texture, 107, 91, 176, 75, 16, 14);
        }
    }

    private void renderTsarStatus(GuiGraphics graphics) {
        if (menu.isFilled()) {
            blit(graphics, MIKE_TEXTURE, 18, 50, 176, 18, 16, 16);
        } else if (menu.isReady()) {
            blit(graphics, MIKE_TEXTURE, 18, 50, 176, 0, 16, 16);
        }
        if (menu.hasComponent(0)) {
            blit(graphics, MIKE_TEXTURE, 40, 36, 209, 1, 23, 23);
        }
        if (menu.hasComponent(2)) {
            blit(graphics, MIKE_TEXTURE, 63, 36, 232, 1, 23, 23);
        }
        if (menu.hasComponent(1)) {
            blit(graphics, MIKE_TEXTURE, 40, 59, 209, 24, 23, 23);
        }
        if (menu.hasComponent(3)) {
            blit(graphics, MIKE_TEXTURE, 63, 59, 232, 24, 23, 23);
        }
        if (menu.hasComponent(5)) {
            blit(graphics, MIKE_TEXTURE, 91, 41, 176, 220, 80, 36);
        }
    }

    private void renderMikeStatus(GuiGraphics graphics) {
        if (menu.isFilled()) {
            blit(graphics, texture, 5, 35, 177, 19, 16, 16);
        } else if (menu.isReady()) {
            blit(graphics, texture, 5, 35, 177, 1, 16, 16);
        }
        int[][] lenses = {{0, 40, 52, 209, 1}, {2, 63, 52, 232, 1}, {1, 40, 75, 209, 24}, {3, 63, 75, 232, 24}};
        for (int[] lens : lenses) {
            if (menu.hasComponent(lens[0])) {
                blit(graphics, texture, lens[1] - 16, lens[2] - 32, lens[3], lens[4], 23, 23);
            }
        }
        if (menu.hasComponent(5)) {
            blit(graphics, texture, 75, 25, 176, 49, 80, 36);
        }
        if (menu.hasComponent(6)) {
            blit(graphics, texture, 79, 30, 180, 88, 58, 26);
        }
        if (menu.hasComponent(7)) {
            blit(graphics, texture, 140, 30, 240, 88, 12, 26);
        }
    }

    private void renderFleijaStatus(GuiGraphics graphics) {
        if (menu.hasComponent(0)) {
            blit(graphics, texture, 7, 88, 176, 0, 30, 20);
        }
        if (menu.hasComponent(1)) {
            blit(graphics, texture, 139, 88, 206, 0, 30, 20);
        }
        if (menu.hasComponent(2)) {
            blit(graphics, texture, 57, 77, 176, 62, 18, 14);
        }
        if (menu.hasComponent(3)) {
            blit(graphics, texture, 57, 91, 176, 76, 18, 14);
        }
        if (menu.hasComponent(4)) {
            blit(graphics, texture, 57, 105, 176, 90, 18, 14);
        }
        if (menu.hasComponent(5)) {
            blit(graphics, texture, 85, 77, 176, 20, 18, 15);
        }
        if (menu.hasComponent(6)) {
            blit(graphics, texture, 103, 77, 194, 20, 18, 15);
        }
        if (menu.hasComponent(7)) {
            blit(graphics, texture, 85, 92, 176, 35, 18, 12);
        }
        if (menu.hasComponent(8)) {
            blit(graphics, texture, 103, 92, 194, 35, 18, 12);
        }
        if (menu.hasComponent(9)) {
            blit(graphics, texture, 85, 104, 176, 47, 18, 15);
        }
        if (menu.hasComponent(10)) {
            blit(graphics, texture, 103, 104, 194, 47, 18, 15);
        }
    }

    private void renderSoliniumStatus(GuiGraphics graphics) {
        if (menu.hasComponent(0)) {
            blit(graphics, texture, 24, 84, 0, 222, 22, 14);
        }
        if (menu.hasComponent(1)) {
            blit(graphics, texture, 46, 84, 22, 222, 18, 14);
        }
        if (menu.hasComponent(2)) {
            blit(graphics, texture, 76, 84, 52, 222, 18, 14);
        }
        if (menu.hasComponent(3)) {
            blit(graphics, texture, 94, 84, 70, 222, 22, 14);
        }
        if (menu.hasComponent(4)) {
            blit(graphics, texture, 64, 84, 40, 222, 12, 28);
        }
        if (menu.hasComponent(5)) {
            blit(graphics, texture, 24, 98, 0, 236, 22, 14);
        }
        if (menu.hasComponent(6)) {
            blit(graphics, texture, 46, 98, 22, 236, 18, 14);
        }
        if (menu.hasComponent(7)) {
            blit(graphics, texture, 76, 98, 52, 236, 18, 14);
        }
        if (menu.hasComponent(8)) {
            blit(graphics, texture, 94, 98, 70, 236, 22, 14);
        }
        if (menu.isReady()) {
            blit(graphics, texture, 134, 90, 176, 0, 16, 16);
        }
    }

    private void renderN2Status(GuiGraphics graphics) {
        int count = 0;
        for (int slot = 0; slot < 12; slot++) {
            if (menu.hasComponent(slot)) {
                count++;
            }
        }
        if (count > 0) {
            blit(graphics, texture, 35, 120 - 6 * count, 176, 0, 34, 6 * count);
        }
    }

    private void blit(GuiGraphics graphics, ResourceLocation source, int x, int y, int u, int v, int width, int height) {
        graphics.blit(source, leftPos + x, topPos + y, u, v, width, height, 256, 256);
    }
}
