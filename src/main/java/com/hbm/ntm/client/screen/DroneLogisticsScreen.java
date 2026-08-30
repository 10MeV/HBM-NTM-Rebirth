package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.DroneLogisticsBlockEntity;
import com.hbm.ntm.menu.DroneLogisticsMenu;
import com.hbm.ntm.util.LegacyPatternMatcher;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/** Legacy storage GUI backgrounds, with requester filter mode exposed as source-backed tooltip text. */
public class DroneLogisticsScreen extends AbstractContainerScreen<DroneLogisticsMenu> {
    public DroneLogisticsScreen(DroneLogisticsMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        // GUIDroneDock used a 185-pixel panel; provider and requester used 186 pixels.
        imageHeight = menu.getBlockEntity().kind() == DroneLogisticsBlockEntity.Kind.DOCK ? 185 : 186;
        inventoryLabelY = imageHeight - 96 + 2;
    }
    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){graphics.blit(texture(),leftPos,topPos,0,0,imageWidth,imageHeight);}
    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){graphics.drawString(font,title,imageWidth/2-font.width(title)/2,6,0x404040,false);graphics.drawString(font,playerInventoryTitle,8,inventoryLabelY,0x404040,false);}
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){
        renderBackground(graphics);
        super.render(graphics,mouseX,mouseY,partial);
        if (menu.getBlockEntity().kind() == DroneLogisticsBlockEntity.Kind.REQUESTER && menu.getCarried().isEmpty()) {
            for (int index = 0; index < 9; index++) if (isHovering(menu.slots.get(index).x, menu.slots.get(index).y, 16, 16, mouseX, mouseY)) {
                var filter = menu.getBlockEntity().filter(index);
                if (filter != null) graphics.renderComponentTooltip(font, List.of(
                        Component.literal("Right click to change").withStyle(ChatFormatting.RED),
                        LegacyPatternMatcher.label(filter.mode()).copy().withStyle(ChatFormatting.YELLOW)), mouseX, mouseY - 30);
                break;
            }
        }
    }
    private ResourceLocation texture(){String file=switch(menu.getBlockEntity().kind()){case DOCK->"gui_drone_dock";case PROVIDER->"gui_drone_provider";case REQUESTER->"gui_drone_requester";};return new ResourceLocation(HbmNtm.MOD_ID,"textures/gui/storage/"+file+".png");}
}
