package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.blockentity.MachineBatterySocketBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyHorseRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import com.hbm.ntm.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class MachineBatterySocketRenderer implements BlockEntityRenderer<MachineBatterySocketBlockEntity> {
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(HbmNtm.MOD_ID, "models/block/machines/battery.obj");
    static final ResourceLocation SOCKET_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/block/machines/battery_socket.png");
    private static final ResourceLocation SELF_CHARGING_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/block/machines/battery_sc.png");
    static final LegacyWavefrontModel MODEL = new LegacyWavefrontModel(MODEL_LOCATION, SOCKET_TEXTURE);
    private static final LegacyHorseRenderer CREATIVE_HORSE = new LegacyHorseRenderer();
    private static final int CREATIVE_BEAM_OUTER = 0x404040;
    private static final int CREATIVE_BEAM_INNER = 0x002040;

    public MachineBatterySocketRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(MachineBatterySocketBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(MachineBatterySocketBlockEntity socket, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(socket, packedLight);
        poseStack.pushPose();
        applyLegacySocketTransform(socket.getBlockState(), poseStack);

        MODEL.renderPart("Socket", SOCKET_TEXTURE, poseStack, buffer, modelLight, packedOverlay);
        if (socket.hasFrame()) {
            MODEL.renderPart("Supports", SOCKET_TEXTURE, poseStack, buffer, modelLight, packedOverlay);
        }

        ItemStack stack = socket.getBatteryStack();
        if (stack.getItem() instanceof HbmBatteryPackItem pack) {
            ResourceLocation texture = new ResourceLocation(HbmNtm.MOD_ID, "textures/block/machines/" + pack.getLegacyTextureName() + ".png");
            String part = pack.isCapacitor() ? "Capacitor" : "Battery";
            MODEL.renderPart(part, texture, poseStack, buffer, modelLight, packedOverlay);
        } else if (stack.getItem() instanceof HbmSelfChargingBatteryItem) {
            MODEL.renderPart("Battery", SELF_CHARGING_TEXTURE, poseStack, buffer, modelLight, packedOverlay);
        } else if (stack.is(ModItems.BATTERY_CREATIVE.get())) {
            renderCreativeBatteryEffect(socket, partialTick, poseStack, buffer, modelLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private static void renderCreativeBatteryEffect(MachineBatterySocketBlockEntity socket, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = socket.getLevel();
        if (level == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(0.75F, 0.75F, 0.75F);
        poseStack.mulPose(Axis.YN.rotationDegrees(((level.getGameTime() % 360L) + partialTick) * 25.0F));
        CREATIVE_HORSE.reset();
        CREATIVE_HORSE.enableHorn();
        CREATIVE_HORSE.render(poseStack, buffer, LegacyHorseRenderer.SUNBURST_TEXTURE, packedLight, packedOverlay);
        poseStack.popPose();

        Random random = new Random(level.getGameTime() / 5L);
        random.nextBoolean();
        int start = (int) (System.currentTimeMillis() % 1000L) / 50;

        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                if (random.nextInt(4) != 0) {
                    continue;
                }

                poseStack.pushPose();
                poseStack.translate(0.0D, 0.75D, 0.0D);
                double x = 0.4375D * i;
                double z = 0.4375D * j;
                LegacyBeamRenderer.solidBeam(poseStack, buffer, x, 1.1875D, z,
                        LegacyBeamRenderer.WaveType.RANDOM, CREATIVE_BEAM_OUTER, CREATIVE_BEAM_INNER,
                        start, 15, 0.0625F, 3, 0.025F);
                LegacyBeamRenderer.solidBeam(poseStack, buffer, x, 1.1875D, z,
                        LegacyBeamRenderer.WaveType.RANDOM, CREATIVE_BEAM_OUTER, CREATIVE_BEAM_INNER,
                        start, 1, 0.0F, 3, 0.025F);
                poseStack.popPose();
            }
        }
    }

    private static void applyLegacySocketTransform(BlockState state, PoseStack poseStack) {
        Direction facing = state.hasProperty(MachineBatterySocketBlock.FACING)
                ? state.getValue(MachineBatterySocketBlock.FACING)
                : Direction.SOUTH;
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(270.0F - facing.toYRot()));
        poseStack.translate(-0.5D, 0.0D, 0.5D);
    }
}
