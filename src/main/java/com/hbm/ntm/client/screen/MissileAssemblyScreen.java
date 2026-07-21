package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.client.obj.ObjMissilePartModels.LegacyMissilePart;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.missile.CustomMissilePartProfile;
import com.hbm.ntm.menu.MissileAssemblyMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class MissileAssemblyScreen extends AbstractContainerScreen<MissileAssemblyMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/gui_missile_assembly.png");

    public MissileAssemblyScreen(MissileAssemblyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        // GUIMachineMissileAssembly only marked a present but incompatible fins
        // part red. The other four slots only received their positive green mark.
        drawState(graphics, menu.getChipState(), 13, false);
        drawState(graphics, menu.getWarheadState(), 31, false);
        drawState(graphics, menu.getFuselageState(), 49, false);
        drawState(graphics, menu.getStabilityState(), 67, true);
        drawState(graphics, menu.getThrusterState(), 85, false);
        if (menu.canBuild()) {
            graphics.blit(TEXTURE, leftPos + 115, topPos + 35, 176, 0, 18, 18);
        }

        ItemStack preview = menu.previewMissileStack();
        if (!preview.isEmpty()) {
            renderMultipartPreview(graphics, preview);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(115, 35, 18, 18, mouseX, mouseY)) {
            if (minecraft != null) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0, 0);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawState(GuiGraphics graphics, int state, int x, boolean showInvalid) {
        if (state == 1) {
            graphics.blit(TEXTURE, leftPos + x, topPos + 23, 194, 0, 6, 8);
        } else if (showInvalid && state == 0) {
            graphics.blit(TEXTURE, leftPos + x, topPos + 23, 200, 0, 6, 8);
        }
    }

    private void renderMultipartPreview(GuiGraphics graphics, ItemStack preview) {
        CustomMissilePartProfile.Assembly assembly = CustomMissilePartProfile.assemblyFromStack(preview);
        if (assembly == null) {
            return;
        }
        LegacyMissilePart thruster = part(assembly.thruster());
        LegacyMissilePart fins = part(assembly.fins());
        LegacyMissilePart fuselage = part(assembly.fuselage());
        LegacyMissilePart warhead = part(assembly.warhead());
        ObjMissilePartModels.MissileRenderPlan plan =
                ObjMissilePartModels.missileRenderPlan(thruster, fins, fuselage, warhead);
        if (plan.steps().isEmpty()) {
            return;
        }

        // GUIMachineMissileAssembly: (88, 98, 100), its historical 144px fitting
        // constant, then the exact Y/-X/-Z multipart orientation chain.
        double scale = 8.0D * 18.0D / Math.max(plan.multipartHeight(), 6.0D);
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 88.0D, topPos + 98.0D, 100.0D);
        LegacyPoseRotations.rotateYDegrees(graphics.pose(), -((System.currentTimeMillis() / 10L) % 360L));
        graphics.pose().translate(plan.multipartHeight() / 2.0D * scale, 0.0D, 0.0D);
        graphics.pose().scale((float) scale, (float) scale, (float) scale);
        LegacyPoseRotations.rotateXDegrees(graphics.pose(), 90.0F);
        LegacyPoseRotations.rotateZDegrees(graphics.pose(), -90.0F);
        graphics.pose().scale(-1.0F, -1.0F, -1.0F);
        ObjMissilePartModels.renderMissile(thruster, fins, fuselage, warhead, graphics.pose(), graphics.bufferSource(),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        graphics.pose().popPose();
        graphics.flush();
    }

    private static LegacyMissilePart part(CustomMissilePartProfile.ResolvedPart part) {
        return part == null ? null : ObjMissilePartModels.part(part.legacyName());
    }
}
