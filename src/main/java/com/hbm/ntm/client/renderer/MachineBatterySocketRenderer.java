package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.blockentity.MachineBatterySocketBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyHorseRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import com.hbm.ntm.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MachineBatterySocketRenderer implements BlockEntityRenderer<MachineBatterySocketBlockEntity> {
    static final ResourceLocation SOCKET_TEXTURE = ObjMachineModels.BATTERY_SOCKET_TEXTURE;
    private static final ResourceLocation SELF_CHARGING_TEXTURE = ObjMachineModels.BATTERY_SC_TEXTURE;
    static final LegacyWavefrontModel MODEL = ObjMachineModels.BATTERY_SOCKET_LEGACY;
    private static final LegacyWavefrontModel.SelectionHandle SOCKET =
            MODEL.prepareRenderOnlyInCallOrder("Socket");
    private static final LegacyWavefrontModel.SelectionHandle SUPPORTS =
            MODEL.prepareRenderOnlyInCallOrder("Supports");
    private static final LegacyWavefrontModel.SelectionHandle BATTERY =
            MODEL.prepareRenderOnlyInCallOrder("Battery");
    private static final LegacyWavefrontModel.SelectionHandle CAPACITOR =
            MODEL.prepareRenderOnlyInCallOrder("Capacitor");
    private static final LegacyHorseRenderer CREATIVE_HORSE = new LegacyHorseRenderer();
    private static final ThreadLocal<Random> CREATIVE_BEAM_RANDOM = ThreadLocal.withInitial(Random::new);

    public MachineBatterySocketRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(MachineBatterySocketBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(MachineBatterySocketBlockEntity socket, Vec3 cameraPos) {
        return hasRenderableContent(socket)
                && BlockEntityRenderer.super.shouldRender(socket, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(socket, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(MachineBatterySocketBlockEntity socket, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stack = socket.getBatteryStack();
        boolean hasFrame = socket.hasFrame();
        if (!hasFrame && !isRenderableBatteryStack(stack)) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(socket, getViewDistance())) {
            return;
        }
        boolean creativeBattery = stack.is(ModItems.BATTERY_CREATIVE.get());

        int modelLight = LegacyRenderLighting.resolveMultiblockLight(socket, packedLight);
        poseStack.pushPose();
        BlockState state = socket.getBlockState();
        applyLegacySocketTransform(state, poseStack);

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(socket)) {
            if (hasFrame) {
                renderModelPart(SUPPORTS, SOCKET_TEXTURE, poseStack, buffer, modelLight, packedOverlay,
                        LegacyTexturedRenderMode.CUTOUT_CULL);
            }

            if (stack.getItem() instanceof HbmBatteryPackItem pack) {
                ResourceLocation texture = BatteryPackItemRenderer.textureFor(pack);
                renderModelPart(pack.isCapacitor() ? CAPACITOR : BATTERY, texture, poseStack, buffer, modelLight,
                        packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
            } else if (stack.getItem() instanceof HbmSelfChargingBatteryItem) {
                renderModelPart(BATTERY, SELF_CHARGING_TEXTURE, poseStack, buffer, modelLight, packedOverlay,
                        LegacyTexturedRenderMode.CUTOUT_CULL);
            }
        }
        if (creativeBattery) {
            renderCreativeBatteryEffect(socket, partialTick, poseStack, buffer, modelLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private static boolean hasRenderableContent(MachineBatterySocketBlockEntity socket) {
        return socket.hasFrame() || isRenderableBatteryStack(socket.getBatteryStack());
    }

    private static boolean isRenderableBatteryStack(ItemStack stack) {
        return stack.getItem() instanceof HbmBatteryPackItem
                || stack.getItem() instanceof HbmSelfChargingBatteryItem
                || stack.is(ModItems.BATTERY_CREATIVE.get());
    }

    private static void renderCreativeBatteryEffect(MachineBatterySocketBlockEntity socket, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = socket.getLevel();
        if (level == null) {
            return;
        }

        long worldTime = level.getGameTime();
        double horseYaw = ((worldTime % 360L) + partialTick) * 25.0D;

        poseStack.pushPose();
        poseStack.scale((float) LegacyTileRenderPlans.CREATIVE_BATTERY_HORSE_SCALE,
                (float) LegacyTileRenderPlans.CREATIVE_BATTERY_HORSE_SCALE,
                (float) LegacyTileRenderPlans.CREATIVE_BATTERY_HORSE_SCALE);
        poseStack.mulPose(Axis.YN.rotationDegrees((float) horseYaw));
        CREATIVE_HORSE.reset();
        CREATIVE_HORSE.enableHorn();
        CREATIVE_HORSE.render(poseStack, buffer, LegacyHorseRenderer.SUNBURST_TEXTURE, packedLight, packedOverlay);
        poseStack.popPose();

        int beamMask = creativeBatteryBeamMask(worldTime);
        if (beamMask != 0) {
            int start = (int) (System.currentTimeMillis()
                    % LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_PERIOD_MILLIS)
                    / LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_START_DIVISOR;
            poseStack.pushPose();
            poseStack.translate(0.0D, LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_TRANSLATE_Y, 0.0D);
            LegacyMachineEffectPresenter.enqueueSolidBeamGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                    false, beams -> {
                int bit = 1;
                for (int i = -1; i <= 1; i += 2) {
                    for (int j = -1; j <= 1; j += 2) {
                        if ((beamMask & bit) != 0) {
                            double x = LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_XZ * i;
                            double z = LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_XZ * j;
                            beams.add(
                                    x, LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_Y, z,
                                    LegacyBeamRenderer.WaveType.RANDOM,
                                    LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_OUTER_COLOR,
                                    LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_INNER_COLOR,
                                    start, LegacyTileRenderPlans.CREATIVE_BATTERY_LONG_BEAM_SEGMENTS,
                                    LegacyTileRenderPlans.CREATIVE_BATTERY_LONG_BEAM_SIZE,
                                    LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_LAYERS,
                                    LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_THICKNESS);
                            beams.add(
                                    x, LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_Y, z,
                                    LegacyBeamRenderer.WaveType.RANDOM,
                                    LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_OUTER_COLOR,
                                    LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_INNER_COLOR,
                                    start, LegacyTileRenderPlans.CREATIVE_BATTERY_SHORT_BEAM_SEGMENTS,
                                    LegacyTileRenderPlans.CREATIVE_BATTERY_SHORT_BEAM_SIZE,
                                    LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_LAYERS,
                                    LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_THICKNESS);
                        }
                        bit <<= 1;
                    }
                }
            });
            poseStack.popPose();
        }
    }

    private static int creativeBatteryBeamMask(long worldTime) {
        Random random = CREATIVE_BEAM_RANDOM.get();
        random.setSeed(worldTime / 5L);
        random.nextBoolean();
        int mask = 0;
        int bit = 1;
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                if (random.nextInt(LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_RANDOM_BOUND) == 0) {
                    mask |= bit;
                }
                bit <<= 1;
            }
        }
        return mask;
    }

    private static void applyLegacySocketTransform(BlockState state, PoseStack poseStack) {
        Direction facing = state.hasProperty(MachineBatterySocketBlock.FACING)
                ? state.getValue(MachineBatterySocketBlock.FACING)
                : Direction.SOUTH;
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(270.0F - facing.toYRot()));
        poseStack.translate(-0.5D, 0.0D, 0.5D);
    }

    static void renderModelPart(String partName, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        if (handle != null) {
            MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle);
            return;
        }
        MODEL.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderModelPart(LegacyWavefrontModel.SelectionHandle handle, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle, renderMode);
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Socket" -> SOCKET;
            case "Supports" -> SUPPORTS;
            case "Battery" -> BATTERY;
            case "Capacitor" -> CAPACITOR;
            default -> null;
        };
    }
}
