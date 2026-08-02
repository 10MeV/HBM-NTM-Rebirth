package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Inventory/hand OBJ view retained from RenderPistonInserter#getRenderer. */
public final class PistonInserterItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final PistonInserterItemRenderer INSTANCE = new PistonInserterItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private PistonInserterItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.0D, -2.5D, 0.0D);
            poseStack.scale(5.0F, 5.0F, 5.0F);
        } else {
            poseStack.scale(2.0F, 2.0F, 2.0F);
        }
        ObjModelLibrary.MACHINE_PISTON_INSERTER.renderAll(ObjMachineModels.PISTON_INSERTER_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
