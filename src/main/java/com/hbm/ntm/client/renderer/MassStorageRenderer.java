package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.MassStorageBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

public class MassStorageRenderer implements BlockEntityRenderer<MassStorageBlockEntity> {
    private static final int INFO_VIEW_DISTANCE = 32;
    private static final int FONT_COLOR = 0x00FF00;
    private static final int FONT_SHADOW_COLOR = (FONT_COLOR & 0xFCFCFC) >> 2 | FONT_COLOR & 0xFF000000;

    public MassStorageRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MassStorageBlockEntity storage, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack type = storage.type();
        if (type.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        applyFrontPanelTransform(storage.getBlockState(), poseStack);
        renderItem(type, storage, poseStack, buffer);
        renderCount(storage, poseStack, buffer);
        renderFillBar(storage, poseStack, buffer);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(MassStorageBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return INFO_VIEW_DISTANCE;
    }

    private static void applyFrontPanelTransform(BlockState state, PoseStack poseStack) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(frontYaw(facing)));
        poseStack.translate(0.0D, 0.0D, -0.505D);
        poseStack.translate(-0.5D, 0.5D, 0.0D);
        poseStack.scale(1.0F / 16.0F, -1.0F / 16.0F, 1.0F);
    }

    private static float frontYaw(Direction facing) {
        return switch (facing) {
            case EAST -> -90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            default -> 0.0F;
        };
    }

    private static void renderItem(ItemStack type, MassStorageBlockEntity storage, PoseStack poseStack,
            MultiBufferSource buffer) {
        Minecraft minecraft = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(8.0D, 6.5D, -0.002D);
        poseStack.scale(8.0F, -8.0F, 8.0F);
        minecraft.getItemRenderer().renderStatic(type.copyWithCount(1), ItemDisplayContext.GUI,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, buffer, storage.getLevel(), 0);
        poseStack.popPose();
    }

    private static void renderCount(MassStorageBlockEntity storage, PoseStack poseStack, MultiBufferSource buffer) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        String text = getTextForCount(storage.stockpile(), minecraft.options.forceUnicodeFont().get());
        int textX = 32 - font.width(text) / 2;
        int textY = 44;

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -0.004D);
        poseStack.scale(0.25F, 0.25F, 0.25F);
        font.drawInBatch(text, textX + 1.0F, textY + 1.0F, FONT_SHADOW_COLOR, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poseStack.translate(0.0D, 0.0D, -0.004D);
        font.drawInBatch(text, textX, textY, FONT_COLOR, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static void renderFillBar(MassStorageBlockEntity storage, PoseStack poseStack, MultiBufferSource buffer) {
        double fraction = storage.capacity() <= 0 ? 0.0D : (double) storage.stockpile() / (double) storage.capacity();
        double colorFraction = Math.max(0.0D, Math.min(1.0D, fraction));
        int red = Math.round((float) ((1.0D - colorFraction) * 255.0D));
        int green = Math.round((float) (colorFraction * 255.0D));
        double minX = 2.0D;
        double maxX = 2.0D + fraction * 12.0D;
        double minY = 13.5D;
        double maxY = 14.0D;
        double z = -0.006D;
        int color = red << 16 | green << 8;
        LegacyUntexturedQuadRenderer.quad(poseStack, buffer, LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                minX, maxY, z,
                maxX, maxY, z,
                maxX, minY, z,
                minX, minY, z,
                color, 255, 255, 255, 255);
    }

    private static String getTextForCount(int stackSize, boolean isUnicode) {
        if (stackSize >= 100_000_000 || stackSize >= 1_000_000 && isUnicode) {
            return String.format(Locale.ROOT, "%.0fM", stackSize / 1_000_000.0F);
        }
        if (stackSize >= 1_000_000) {
            return String.format(Locale.ROOT, "%.1fM", stackSize / 1_000_000.0F);
        }
        if (stackSize >= 100_000 || stackSize >= 10_000 && isUnicode) {
            return String.format(Locale.ROOT, "%.0fK", stackSize / 1_000.0F);
        }
        if (stackSize >= 10_000) {
            return String.format(Locale.ROOT, "%.1fK", stackSize / 1_000.0F);
        }
        return String.valueOf(stackSize);
    }
}
