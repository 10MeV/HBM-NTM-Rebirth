package com.hbm.ntm.client.renderer;

import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.ObjProjectileModels;
import com.hbm.ntm.client.obj.ObjTurretModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.energy.HbmEnergyConnectionUtil;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnector;
import com.hbm.ntm.fluid.HbmFluidConnectorBlock;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.turret.TurretArtyBlockEntity;
import com.hbm.ntm.turret.TurretBlockEntityBase;
import com.hbm.ntm.turret.TurretFriendlyBlockEntity;
import com.hbm.ntm.turret.TurretFritzBlockEntity;
import com.hbm.ntm.turret.TurretHimarsBlockEntity;
import com.hbm.ntm.turret.TurretHowardBlockEntity;
import com.hbm.ntm.turret.TurretHowardDamagedBlockEntity;
import com.hbm.ntm.turret.TurretJeremyBlockEntity;
import com.hbm.ntm.turret.TurretMaxwellBlockEntity;
import com.hbm.ntm.turret.TurretRichardBlockEntity;
import com.hbm.ntm.turret.TurretSentryBlockEntity;
import com.hbm.ntm.turret.TurretSentryDamagedBlockEntity;
import com.hbm.ntm.turret.TurretTauonBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TurretBlockEntityRenderer<T extends TurretBlockEntityBase> implements BlockEntityRenderer<T> {
    private static final float STATIC_YAW = -90.0F;
    private static final float STATIC_PITCH = 0.0F;
    private static final AABB DEFAULT_STATIC_BOUNDS = new AABB(-3.5D, 0.0D, -3.5D, 3.5D, 5.5D, 3.5D);
    private static final AABB ARTY_STATIC_BOUNDS = new AABB(-4.5D, 0.0D, -5.5D, 4.5D, 5.5D, 5.5D);
    private static final AABB HIMARS_STATIC_BOUNDS = new AABB(-4.5D, 0.0D, -5.5D, 4.5D, 6.5D, 5.5D);
    private static final AABB SENTRY_STATIC_BOUNDS = new AABB(-2.0D, 0.0D, -2.0D, 2.0D, 3.5D, 2.0D);

    public TurretBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(T turret, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(turret, getViewDistance())) {
            return;
        }
        int light = LegacyRenderLighting.resolveMultiblockLight(turret, packedLight);

        poseStack.pushPose();
        Vec3 offset = turret.getRenderHorizontalOffset();
        poseStack.translate(offset.x, 0.0D, offset.z);

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(turret)) {
            if (turret instanceof TurretArtyBlockEntity) {
                renderArtilleryPose(turret, StaticTurretModel.ARTY, yawDegrees(turret, partialTick),
                        pitchDegrees(turret, partialTick), artyBarrelPos((TurretArtyBlockEntity) turret, partialTick),
                        poseStack, buffer, light, packedLight, packedOverlay);
            } else if (turret instanceof TurretHimarsBlockEntity himars) {
                renderHimarsPose(himars, yawDegrees(turret, partialTick), pitchDegrees(turret, partialTick),
                        partialTick, poseStack, buffer, light, packedLight, packedOverlay);
            } else if (turret instanceof TurretSentryBlockEntity
                    || turret instanceof TurretSentryDamagedBlockEntity) {
                renderSentryPose(turret, turret instanceof TurretSentryDamagedBlockEntity,
                        sentryYawDegrees(turret, partialTick), pitchDegrees(turret, partialTick), barrelLeftPos(turret, partialTick),
                        barrelRightPos(turret, partialTick), poseStack, buffer, light, packedLight, packedOverlay);
            } else if (turret instanceof TurretHowardBlockEntity
                    || turret instanceof TurretHowardDamagedBlockEntity) {
                if (turret instanceof TurretHowardBlockEntity) {
                    renderConnectors(turret, true, false, HbmFluids.NONE, poseStack, buffer, light, packedOverlay);
                }
                renderHowardPose(turret, turret instanceof TurretHowardDamagedBlockEntity, yawDegrees(turret, partialTick),
                        pitchDegrees(turret, partialTick), spinDegrees(turret, partialTick),
                        poseStack, buffer, light, packedLight, packedOverlay);
            } else {
                renderStandardPose(turret, standardModel(turret), yawDegrees(turret, partialTick),
                        pitchDegrees(turret, partialTick), spinDegrees(turret, partialTick),
                        poseStack, buffer, light, packedLight, packedOverlay, partialTick);
            }
        }

        poseStack.popPose();
    }

    public static StaticTurretModel staticModelForBlock(Block block) {
        ResourceLocation key = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
        String path = key == null ? "" : key.getPath();
        return switch (path) {
            case "turret_friendly" -> StaticTurretModel.FRIENDLY;
            case "turret_jeremy" -> StaticTurretModel.JEREMY;
            case "turret_richard" -> StaticTurretModel.RICHARD;
            case "turret_tauon" -> StaticTurretModel.TAUON;
            case "turret_howard" -> StaticTurretModel.HOWARD;
            case "turret_sentry" -> StaticTurretModel.SENTRY;
            case "turret_howard_damaged" -> StaticTurretModel.HOWARD_DAMAGED;
            case "turret_sentry_damaged" -> StaticTurretModel.SENTRY_DAMAGED;
            case "turret_maxwell" -> StaticTurretModel.MAXWELL;
            case "turret_arty" -> StaticTurretModel.ARTY;
            case "turret_himars" -> StaticTurretModel.HIMARS;
            case "turret_fritz" -> StaticTurretModel.FRITZ;
            default -> StaticTurretModel.CHEKHOV;
        };
    }

    public static void renderStaticModel(StaticTurretModel model, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        if (model == StaticTurretModel.ARTY || model == StaticTurretModel.HIMARS) {
            renderArtilleryPose(null, model, STATIC_YAW, STATIC_PITCH, 0.0F, poseStack, buffer, light, light, overlay);
        } else if (model == StaticTurretModel.SENTRY || model == StaticTurretModel.SENTRY_DAMAGED) {
            renderSentryPose(null, model == StaticTurretModel.SENTRY_DAMAGED, 0.0F, STATIC_PITCH, 0.0F, 0.0F,
                    poseStack, buffer, light, light, overlay);
        } else if (model == StaticTurretModel.HOWARD || model == StaticTurretModel.HOWARD_DAMAGED) {
            renderHowardPose(null, model == StaticTurretModel.HOWARD_DAMAGED, STATIC_YAW, STATIC_PITCH, 0.0F,
                    poseStack, buffer, light, light, overlay);
        } else {
            renderStandardPose(null, model, STATIC_YAW, STATIC_PITCH, 0.0F,
                    poseStack, buffer, light, light, overlay, 0.0F);
        }
    }

    public static void renderLegacyItemModel(StaticTurretModel model, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        switch (model) {
            case ARTY -> renderArtyItemPose(poseStack, buffer, light, overlay);
            case HIMARS -> renderHimarsItemPose(poseStack, buffer, light, overlay);
            case SENTRY, SENTRY_DAMAGED -> renderSentryItemPose(model == StaticTurretModel.SENTRY_DAMAGED,
                    poseStack, buffer, light, overlay);
            case HOWARD, HOWARD_DAMAGED -> renderHowardItemPose(model == StaticTurretModel.HOWARD_DAMAGED,
                    poseStack, buffer, light, overlay);
            default -> renderStandardItemPose(model, poseStack, buffer, light, overlay);
        }
    }

    private static StaticTurretModel standardModel(TurretBlockEntityBase turret) {
        if (turret instanceof TurretFriendlyBlockEntity) {
            return StaticTurretModel.FRIENDLY;
        }
        if (turret instanceof TurretJeremyBlockEntity) {
            return StaticTurretModel.JEREMY;
        }
        if (turret instanceof TurretRichardBlockEntity) {
            return StaticTurretModel.RICHARD;
        }
        if (turret instanceof TurretTauonBlockEntity) {
            return StaticTurretModel.TAUON;
        }
        if (turret instanceof TurretMaxwellBlockEntity) {
            return StaticTurretModel.MAXWELL;
        }
        if (turret instanceof TurretFritzBlockEntity) {
            return StaticTurretModel.FRITZ;
        }
        return StaticTurretModel.CHEKHOV;
    }

    private static void renderStandardPose(TurretBlockEntityBase turret, StaticTurretModel model, float yaw,
            float pitch, float spin, PoseStack poseStack, MultiBufferSource buffer, int light, int activityLight,
            int overlay, float partialTick) {
        if (turret != null) {
            boolean fluid = turret instanceof TurretFritzBlockEntity;
            FluidType type = fluid ? ((TurretFritzBlockEntity) turret).getTank().getTankType() : HbmFluids.NONE;
            renderConnectors(turret, true, fluid, type, poseStack, buffer, light, overlay);
        }
        renderBase(model == StaticTurretModel.FRIENDLY, poseStack, buffer, light, overlay);

        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, yaw);
        if (model == StaticTurretModel.MAXWELL) {
            renderAnimated(turret, () -> ObjTurretModels.renderHowardCarriage(false,
                    ObjTurretModels.CARRIAGE_CIWS_TEXTURE, poseStack, buffer, activityLight, overlay));
        } else {
            renderAnimated(turret, () -> ObjTurretModels.renderChekhovCarriage(
                    model == StaticTurretModel.FRIENDLY
                            ? ObjTurretModels.CARRIAGE_FRIENDLY_TEXTURE
                            : ObjTurretModels.CARRIAGE_TEXTURE,
                    poseStack, buffer, activityLight, overlay));
        }

        poseStack.translate(0.0D, 1.5D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, pitch);
        poseStack.translate(0.0D, -1.5D, 0.0D);

        if (model == StaticTurretModel.JEREMY) {
            renderAnimated(turret, () -> ObjTurretModels.renderJeremyGun(
                    ObjTurretModels.JEREMY_TEXTURE, poseStack, buffer, activityLight, overlay));
        } else if (model == StaticTurretModel.RICHARD) {
            int loaded = turret instanceof TurretRichardBlockEntity richard ? richard.getLoaded() : 1;
            renderAnimated(turret, () -> {
                ObjTurretModels.renderRichardLauncher(ObjTurretModels.RICHARD_TEXTURE,
                        poseStack, buffer, activityLight, overlay);
                renderRichardLoadedMissiles(loaded, poseStack, buffer, activityLight, overlay);
            });
        } else if (model == StaticTurretModel.TAUON) {
            renderAnimated(turret, () -> ObjTurretModels.renderTauonCannon(
                    ObjTurretModels.TAUON_TEXTURE, poseStack, buffer, activityLight, overlay));
            if (turret != null) {
                renderTauonBeam(turret, poseStack, buffer, partialTick);
            }
            poseStack.pushPose();
            poseStack.translate(0.0D, 1.375D, 0.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, -spin);
            poseStack.translate(0.0D, -1.375D, 0.0D);
            renderAnimated(turret, () -> ObjTurretModels.renderTauonRotor(
                    ObjTurretModels.TAUON_TEXTURE, poseStack, buffer, activityLight, overlay));
            poseStack.popPose();
        } else if (model == StaticTurretModel.MAXWELL) {
            renderAnimated(turret, () -> ObjTurretModels.renderMaxwellMicrowave(
                    ObjTurretModels.MAXWELL_TEXTURE, poseStack, buffer, activityLight, overlay));
            if (turret != null) {
                renderMaxwellBeam(turret, poseStack, buffer, partialTick);
            }
        } else if (model == StaticTurretModel.FRITZ) {
            renderAnimated(turret, () -> ObjTurretModels.renderFritzGun(
                    ObjTurretModels.FRITZ_TEXTURE, poseStack, buffer, activityLight, overlay));
        } else {
            renderAnimated(turret, () -> ObjTurretModels.renderChekhovBody(
                    ObjTurretModels.CHEKHOV_TEXTURE, poseStack, buffer, activityLight, overlay));
            poseStack.pushPose();
            poseStack.translate(0.0D, 1.5D, 0.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, -spin);
            poseStack.translate(0.0D, -1.5D, 0.0D);
            renderAnimated(turret, () -> ObjTurretModels.renderChekhovBarrels(
                    ObjTurretModels.CHEKHOV_BARRELS_TEXTURE, poseStack, buffer, activityLight, overlay));
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void renderRichardLoadedMissiles(int loaded, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        if (loaded <= 0) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.375D, 0.1875D);
        for (int i = 0; i < loaded; i++) {
            ObjTurretModels.renderRichardMissileLoaded(ObjTurretModels.RICHARD_TEXTURE,
                    poseStack, buffer, light, overlay);
            if (i == 2 || i == 6 || i == 9 || i == 13) {
                poseStack.translate(0.0D, -0.1875D, 0.46875D);
            } else {
                poseStack.translate(0.0D, 0.0D, -0.1875D);
            }
        }
        poseStack.popPose();
    }

    private static void renderStandardItemPose(StaticTurretModel model, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        if (model == StaticTurretModel.CHEKHOV || model == StaticTurretModel.FRIENDLY
                || model == StaticTurretModel.JEREMY) {
            poseStack.translate(model == StaticTurretModel.JEREMY ? -0.5D : -0.75D, 0.0D, 0.0D);
        }

        renderBase(model == StaticTurretModel.FRIENDLY, poseStack, buffer, light, overlay);
        if (model == StaticTurretModel.MAXWELL) {
            ObjTurretModels.renderHowardCarriage(false, ObjTurretModels.CARRIAGE_CIWS_TEXTURE,
                    poseStack, buffer, light, overlay);
            ObjTurretModels.renderMaxwellMicrowave(ObjTurretModels.MAXWELL_TEXTURE,
                    poseStack, buffer, light, overlay);
        } else {
            ObjTurretModels.renderChekhovCarriage(
                    model == StaticTurretModel.FRIENDLY
                            ? ObjTurretModels.CARRIAGE_FRIENDLY_TEXTURE
                            : ObjTurretModels.CARRIAGE_TEXTURE,
                    poseStack, buffer, light, overlay);
            if (model == StaticTurretModel.JEREMY) {
                ObjTurretModels.renderJeremyGun(ObjTurretModels.JEREMY_TEXTURE,
                        poseStack, buffer, light, overlay);
            } else if (model == StaticTurretModel.RICHARD) {
                ObjTurretModels.renderRichardLauncher(ObjTurretModels.RICHARD_TEXTURE,
                        poseStack, buffer, light, overlay);
            } else if (model == StaticTurretModel.TAUON) {
                ObjTurretModels.renderTauonCannon(ObjTurretModels.TAUON_TEXTURE,
                        poseStack, buffer, light, overlay);
                ObjTurretModels.renderTauonRotor(ObjTurretModels.TAUON_TEXTURE,
                        poseStack, buffer, light, overlay);
            } else if (model == StaticTurretModel.FRITZ) {
                ObjTurretModels.renderFritzGun(ObjTurretModels.FRITZ_TEXTURE,
                        poseStack, buffer, light, overlay);
            } else {
                ObjTurretModels.renderChekhovBody(ObjTurretModels.CHEKHOV_TEXTURE,
                        poseStack, buffer, light, overlay);
                ObjTurretModels.renderChekhovBarrels(ObjTurretModels.CHEKHOV_BARRELS_TEXTURE,
                        poseStack, buffer, light, overlay);
            }
        }
    }

    private static void renderHowardItemPose(boolean damaged, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        poseStack.translate(-0.75D, 0.0D, 0.0D);
        ObjTurretModels.renderChekhovBase(damaged ? ObjTurretModels.BASE_RUSTED_TEXTURE : ObjTurretModels.BASE_TEXTURE,
                poseStack, buffer, light, overlay);
        ObjTurretModels.renderHowardCarriage(damaged, damaged
                ? ObjTurretModels.CARRIAGE_CIWS_RUSTED_TEXTURE
                : ObjTurretModels.CARRIAGE_CIWS_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderHowardBody(damaged,
                damaged ? ObjTurretModels.HOWARD_RUSTED_TEXTURE : ObjTurretModels.HOWARD_TEXTURE,
                poseStack, buffer, light, overlay);
        ResourceLocation barrelsTexture = damaged
                ? ObjTurretModels.HOWARD_BARRELS_RUSTED_TEXTURE
                : ObjTurretModels.HOWARD_BARRELS_TEXTURE;
        ObjTurretModels.renderHowardBarrelsTop(damaged, barrelsTexture, poseStack, buffer, light, overlay);
        ObjTurretModels.renderHowardBarrelsBottom(damaged, barrelsTexture, poseStack, buffer, light, overlay);
    }

    private static void renderSentryItemPose(boolean damaged, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        ResourceLocation texture = damaged ? ObjTurretModels.SENTRY_DAMAGED_TEXTURE : ObjTurretModels.SENTRY_TEXTURE;
        ObjTurretModels.renderSentryBase(texture, poseStack, buffer, light, overlay);
        ObjTurretModels.renderSentryPivot(texture, poseStack, buffer, light, overlay);
        ObjTurretModels.renderSentryBody(texture, poseStack, buffer, light, overlay);
        ObjTurretModels.renderSentryDrum(texture, poseStack, buffer, light, overlay);
        ObjTurretModels.renderSentryBarrelL(texture, poseStack, buffer, light, overlay);
        ObjTurretModels.renderSentryBarrelR(texture, poseStack, buffer, light, overlay);
    }

    private static void renderArtyItemPose(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjTurretModels.renderArtyBase(ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderArtyCarriage(ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        poseStack.translate(0.0D, 3.0D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, 45.0F);
        poseStack.translate(0.0D, -3.0D, 0.0D);
        ObjTurretModels.renderArtyCannon(ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderArtyBarrel(ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
    }

    private static void renderHimarsItemPose(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjTurretModels.renderArtyBase(ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderHimarsCarriage(ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderHimarsLauncher(ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderHimarsCrane(ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderHimarsTubeStandard(ObjProjectileModels.HIMARS_STANDARD_TEXTURE, poseStack, buffer,
                light, overlay);
    }

    private static void renderTauonBeam(TurretBlockEntityBase turret, PoseStack poseStack,
            MultiBufferSource buffer, float partialTick) {
        double beamDistance = turret.getBeamDistance();
        if (turret.getBeamTicks() <= 0 || beamDistance <= 0.0D) {
            return;
        }
        double renderTime = renderTime(turret, partialTick);
        int start = (int) ((renderTime / 5.0D) % 360.0D);
        poseStack.pushPose();
        poseStack.translate(0.0D, LegacyTileRenderPlans.TAUON_BEAM_TRANSLATE_Y, 0.0D);
        LegacyMachineEffectPresenter.enqueueLineBeam(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                beamDistance, 0.0D, 0.0D, LegacyBeamRenderer.WaveType.RANDOM,
                LegacyTileRenderPlans.TAUON_BEAM_OUTER_COLOR,
                LegacyTileRenderPlans.TAUON_BEAM_INNER_COLOR, start, (int) beamDistance + 1,
                LegacyTileRenderPlans.TAUON_BEAM_SIZE);
        poseStack.popPose();
    }

    private static void renderMaxwellBeam(TurretBlockEntityBase turret, PoseStack poseStack,
            MultiBufferSource buffer, float partialTick) {
        double beamDistance = turret.getBeamDistance();
        double length = Math.max(0.0D, beamDistance - LegacyTileRenderPlans.MAXWELL_BARREL_LENGTH);
        if (turret.getBeamTicks() <= 0 || length <= 0.0D) {
            return;
        }
        double renderTime = renderTime(turret, partialTick);
        int segments = (int) (beamDistance + 1.0D);
        poseStack.pushPose();
        poseStack.translate(LegacyTileRenderPlans.MAXWELL_BARREL_LENGTH,
                LegacyTileRenderPlans.MAXWELL_BEAM_TRANSLATE_Y, 0.0D);
        LegacyMachineEffectPresenter.enqueueSpiralSolidBeamFan(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                false, length, 0.0D, 0.0D, LegacyTileRenderPlans.MAXWELL_BEAM_COLOR,
                renderTime * LegacyTileRenderPlans.MAXWELL_BEAM_SPIN_SPEED,
                LegacyTileRenderPlans.MAXWELL_BEAM_PHASE_STEP, LegacyTileRenderPlans.MAXWELL_BEAM_COUNT, segments,
                LegacyTileRenderPlans.MAXWELL_BEAM_SIZE, LegacyTileRenderPlans.MAXWELL_BEAM_LAYERS,
                LegacyTileRenderPlans.MAXWELL_BEAM_THICKNESS);
        poseStack.popPose();
    }

    private static void renderHowardPose(TurretBlockEntityBase turret, boolean damaged, float yaw, float pitch, float spin,
            PoseStack poseStack, MultiBufferSource buffer, int light, int activityLight, int overlay) {
        ObjTurretModels.renderChekhovBase(damaged ? ObjTurretModels.BASE_RUSTED_TEXTURE : ObjTurretModels.BASE_TEXTURE,
                poseStack, buffer, light, overlay);

        ResourceLocation carriageTexture = damaged
                ? ObjTurretModels.CARRIAGE_CIWS_RUSTED_TEXTURE
                : ObjTurretModels.CARRIAGE_CIWS_TEXTURE;
        ResourceLocation bodyTexture = damaged ? ObjTurretModels.HOWARD_RUSTED_TEXTURE : ObjTurretModels.HOWARD_TEXTURE;
        ResourceLocation barrelsTexture = damaged
                ? ObjTurretModels.HOWARD_BARRELS_RUSTED_TEXTURE
                : ObjTurretModels.HOWARD_BARRELS_TEXTURE;

        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, yaw);
        renderAnimated(turret, () -> ObjTurretModels.renderHowardCarriage(damaged, carriageTexture,
                poseStack, buffer, activityLight, overlay));
        poseStack.translate(0.0D, 2.25D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, pitch);
        poseStack.translate(0.0D, -2.25D, 0.0D);
        renderAnimated(turret, () -> ObjTurretModels.renderHowardBody(damaged, bodyTexture,
                poseStack, buffer, activityLight, overlay));

        poseStack.pushPose();
        poseStack.translate(0.0D, 2.5D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, -spin);
        poseStack.translate(0.0D, -2.5D, 0.0D);
        renderAnimated(turret, () -> ObjTurretModels.renderHowardBarrelsTop(damaged, barrelsTexture,
                poseStack, buffer, activityLight, overlay));
        poseStack.popPose();

        if (damaged) {
            renderAnimated(turret, () -> ObjTurretModels.renderHowardBarrelsBottom(true, barrelsTexture,
                    poseStack, buffer, activityLight, overlay));
        } else {
            poseStack.pushPose();
            poseStack.translate(0.0D, 2.0D, 0.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, spin);
            poseStack.translate(0.0D, -2.0D, 0.0D);
            renderAnimated(turret, () -> ObjTurretModels.renderHowardBarrelsBottom(false, barrelsTexture,
                    poseStack, buffer, activityLight, overlay));
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderSentryPose(TurretBlockEntityBase turret, boolean damaged, float yaw, float pitch,
            float leftRecoil, float rightRecoil, PoseStack poseStack, MultiBufferSource buffer, int light,
            int activityLight, int overlay) {
        ResourceLocation texture = damaged ? ObjTurretModels.SENTRY_DAMAGED_TEXTURE : ObjTurretModels.SENTRY_TEXTURE;

        ObjTurretModels.renderSentryBase(texture, poseStack, buffer, light, overlay);
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, yaw);
        renderAnimated(turret, () -> ObjTurretModels.renderSentryPivot(texture,
                poseStack, buffer, activityLight, overlay));
        poseStack.translate(0.0D, 1.25D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, -pitch);
        poseStack.translate(0.0D, -1.25D, 0.0D);
        renderAnimated(turret, () -> {
            ObjTurretModels.renderSentryBody(texture, poseStack, buffer, activityLight, overlay);
            ObjTurretModels.renderSentryDrum(texture, poseStack, buffer, activityLight, overlay);
        });

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, leftRecoil * -0.5D);
        renderAnimated(turret, () -> ObjTurretModels.renderSentryBarrelL(texture,
                poseStack, buffer, activityLight, overlay));
        poseStack.popPose();

        poseStack.pushPose();
        if (damaged) {
            poseStack.translate(0.0D, 1.5D, 0.5D);
            LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
            poseStack.translate(0.0D, -1.5D, -0.5D);
        } else {
            poseStack.translate(0.0D, 0.0D, rightRecoil * -0.5D);
        }
        renderAnimated(turret, () -> ObjTurretModels.renderSentryBarrelR(texture,
                poseStack, buffer, activityLight, overlay));
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderArtilleryPose(TurretBlockEntityBase turret, StaticTurretModel model, float yaw,
            float pitch, float barrelPos, PoseStack poseStack, MultiBufferSource buffer, int light,
            int activityLight, int overlay) {
        ObjTurretModels.renderArtyBase(ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, yaw - 90.0F);
        if (model == StaticTurretModel.ARTY) {
            renderAnimated(turret, () -> ObjTurretModels.renderArtyCarriage(
                    ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, activityLight, overlay));
            poseStack.translate(0.0D, 3.0D, 0.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, pitch);
            poseStack.translate(0.0D, -3.0D, 0.0D);
            renderAnimated(turret, () -> ObjTurretModels.renderArtyCannon(
                    ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, activityLight, overlay));
            poseStack.translate(0.0D, 0.0D, barrelPos * 2.5D);
            renderAnimated(turret, () -> ObjTurretModels.renderArtyBarrel(
                    ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, activityLight, overlay));
        } else {
            renderAnimated(turret, () -> ObjTurretModels.renderHimarsCarriage(
                    ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, activityLight, overlay));
            poseStack.translate(0.0D, 2.25D, 2.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, pitch);
            poseStack.translate(0.0D, -2.25D, -2.0D);
            renderAnimated(turret, () -> {
                ObjTurretModels.renderHimarsLauncher(ObjTurretModels.HIMARS_TEXTURE,
                        poseStack, buffer, activityLight, overlay);
                ObjTurretModels.renderHimarsCrane(ObjTurretModels.HIMARS_TEXTURE,
                        poseStack, buffer, activityLight, overlay);
                ObjTurretModels.renderHimarsTubeStandard(ObjProjectileModels.HIMARS_STANDARD_TEXTURE, poseStack, buffer,
                        activityLight, overlay);
                for (int cap = 1; cap <= 6; cap++) {
                    ObjTurretModels.renderHimarsCapStandard(cap, ObjProjectileModels.HIMARS_STANDARD_TEXTURE,
                            poseStack, buffer, activityLight, overlay);
                }
            });
        }
        poseStack.popPose();
    }

    private static void renderHimarsPose(TurretHimarsBlockEntity turret, float yaw, float pitch, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int light, int activityLight, int overlay) {
        ObjTurretModels.renderArtyBase(ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, yaw - 90.0F);
        renderAnimated(turret, () -> ObjTurretModels.renderHimarsCarriage(
                ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, activityLight, overlay));
        poseStack.translate(0.0D, 2.25D, 2.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, pitch);
        poseStack.translate(0.0D, -2.25D, -2.0D);
        renderAnimated(turret, () -> ObjTurretModels.renderHimarsLauncher(
                ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, activityLight, overlay));

        float crane = Mth.lerp(partialTick, turret.getLastCrane(), turret.getCrane());
        poseStack.translate(0.0D, 0.0D, crane * -5.0D);
        renderAnimated(turret, () -> ObjTurretModels.renderHimarsCrane(
                ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, activityLight, overlay));

        int typeLoaded = turret.getTypeLoaded();
        List<LegacyArtilleryAmmoCatalog.HimarsRocket> rockets = LegacyArtilleryAmmoCatalog.himarsRockets();
        if (typeLoaded >= 0 && typeLoaded < rockets.size()) {
            LegacyArtilleryAmmoCatalog.HimarsRocket rocket = rockets.get(typeLoaded);
            ResourceLocation texture = himarsRocketTexture(rocket);
            if (rocket.modelType() == 0) {
                renderAnimated(turret, () -> ObjTurretModels.renderHimarsTubeStandard(
                        texture, poseStack, buffer, activityLight, overlay));
                int loaded = Mth.clamp(turret.getAmmoLoaded(), 0, rocket.amount());
                for (int i = 0; i < loaded; i++) {
                    int cap = 6 - i;
                    renderAnimated(turret, () -> ObjTurretModels.renderHimarsCapStandard(cap, texture,
                            poseStack, buffer, activityLight, overlay));
                }
            } else if (rocket.modelType() == 1) {
                renderAnimated(turret, () -> ObjTurretModels.renderHimarsTubeSingle(
                        texture, poseStack, buffer, activityLight, overlay));
                if (turret.hasAmmo()) {
                    renderAnimated(turret, () -> ObjTurretModels.renderHimarsCapSingle(
                            texture, poseStack, buffer, activityLight, overlay));
                }
            }
        }
        poseStack.popPose();
    }

    private static ResourceLocation himarsRocketTexture(LegacyArtilleryAmmoCatalog.HimarsRocket rocket) {
        return switch (rocket.legacyName()) {
            case "ammo_himars_standard_he" -> ObjProjectileModels.HIMARS_STANDARD_HE_TEXTURE;
            case "ammo_himars_standard_wp" -> ObjProjectileModels.HIMARS_STANDARD_WP_TEXTURE;
            case "ammo_himars_standard_tb" -> ObjProjectileModels.HIMARS_STANDARD_TB_TEXTURE;
            case "ammo_himars_standard_lava" -> ObjProjectileModels.HIMARS_STANDARD_LAVA_TEXTURE;
            case "ammo_himars_standard_mini_nuke" -> ObjProjectileModels.HIMARS_STANDARD_MINI_NUKE_TEXTURE;
            case "ammo_himars_single" -> ObjProjectileModels.HIMARS_SINGLE_TEXTURE;
            case "ammo_himars_single_tb" -> ObjProjectileModels.HIMARS_SINGLE_TB_TEXTURE;
            default -> ObjProjectileModels.HIMARS_STANDARD_TEXTURE;
        };
    }

    private static void renderBase(boolean friendly, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        ObjTurretModels.renderChekhovBase(friendly ? ObjTurretModels.BASE_FRIENDLY_TEXTURE : ObjTurretModels.BASE_TEXTURE,
                poseStack, buffer, light, overlay);
    }

    private static void renderConnectors(TurretBlockEntityBase turret, boolean power, boolean fluid, FluidType type,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        ConnectorProbe probe = connectorProbe(turret, power, fluid, type);
        if (probe == null) {
            return;
        }
        renderConnectorIfConnected(probe, -2, 0, 0, 0, 0.0F, Direction.WEST,
                poseStack, buffer, light, overlay);
        renderConnectorIfConnected(probe, -2, -1, 0, -1, 0.0F, Direction.WEST,
                poseStack, buffer, light, overlay);

        renderConnectorIfConnected(probe, -1, 1, 0, -1, 90.0F, Direction.SOUTH,
                poseStack, buffer, light, overlay);
        renderConnectorIfConnected(probe, 0, 1, 0, 0, 90.0F, Direction.SOUTH,
                poseStack, buffer, light, overlay);

        renderConnectorIfConnected(probe, 1, 0, 0, -1, 180.0F, Direction.EAST,
                poseStack, buffer, light, overlay);
        renderConnectorIfConnected(probe, 1, -1, 0, 0, 180.0F, Direction.EAST,
                poseStack, buffer, light, overlay);

        renderConnectorIfConnected(probe, 0, -2, 0, -1, 270.0F, Direction.NORTH,
                poseStack, buffer, light, overlay);
        renderConnectorIfConnected(probe, -1, -2, 0, 0, 270.0F, Direction.NORTH,
                poseStack, buffer, light, overlay);
    }

    private static void renderConnectorIfConnected(ConnectorProbe probe, int checkX, int checkZ,
            int localX, int localZ, float yaw, Direction cableSide,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (!hasConnector(probe, checkX, checkZ, cableSide)) {
            return;
        }
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, yaw);
        poseStack.translate(localX, 0.0D, localZ);
        ObjTurretModels.renderChekhovConnectors(ObjTurretModels.CONNECTOR_TEXTURE,
                poseStack, buffer, light, overlay);
        poseStack.popPose();
    }

    private static ConnectorProbe connectorProbe(TurretBlockEntityBase turret, boolean power,
            boolean fluid, FluidType type) {
        Level level = turret.getLevel();
        if (level == null) {
            return null;
        }
        Vec3 offset = turret.getRenderHorizontalOffset();
        BlockPos pivot = turret.getBlockPos().offset((int) offset.x, 0, (int) offset.z);
        boolean fluidActive = fluid && type != null && type != HbmFluids.NONE;
        return new ConnectorProbe(level, pivot, power, fluidActive, type);
    }

    private static boolean hasConnector(ConnectorProbe probe, int checkX, int checkZ, Direction cableSide) {
        BlockPos checkPos = probe.pivot().offset(checkX, 0, checkZ);
        if (probe.power() && HbmEnergyConnectionUtil.canConnectLegacy(probe.level(), checkPos, cableSide)) {
            return true;
        }
        if (!probe.fluid()) {
            return false;
        }
        return canConnectFluidLegacy(probe.level(), checkPos, probe.type(), cableSide);
    }

    private static boolean canConnectFluidLegacy(Level level, BlockPos targetPos, FluidType type,
            Direction ductSide) {
        if (targetPos.getY() < level.getMinBuildHeight() || targetPos.getY() >= level.getMaxBuildHeight()) {
            return false;
        }
        Direction targetSide = ductSide.getOpposite();
        Block block = level.getBlockState(targetPos).getBlock();
        if (block instanceof HbmFluidConnectorBlock connectorBlock
                && connectorBlock.canConnectFluid(level, targetPos, type, targetSide)) {
            return true;
        }
        BlockEntity blockEntity = level.getBlockEntity(targetPos);
        return blockEntity instanceof HbmFluidConnector connector
                && connector.canConnectFluid(type, targetSide);
    }

    private record ConnectorProbe(Level level, BlockPos pivot, boolean power, boolean fluid, FluidType type) {
    }

    private static void renderAnimated(TurretBlockEntityBase turret, Runnable action) {
        if (turret == null) {
            action.run();
            return;
        }
        try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(turret)) {
            action.run();
        }
    }

    private static float yawDegrees(TurretBlockEntityBase turret, float partialTick) {
        return -Mth.RAD_TO_DEG * (float) legacyYawLerpRadians(partialTick,
                turret.getLastRotationYaw(), turret.getRotationYaw()) - 90.0F;
    }

    private static float sentryYawDegrees(TurretBlockEntityBase turret, float partialTick) {
        return -Mth.RAD_TO_DEG * (float) legacyYawLerpRadians(partialTick,
                turret.getLastRotationYaw(), turret.getRotationYaw());
    }

    private static double legacyYawLerpRadians(float partialTick, double previous, double current) {
        return previous + (current - previous) * partialTick;
    }

    private static float pitchDegrees(TurretBlockEntityBase turret, float partialTick) {
        return Mth.RAD_TO_DEG * Mth.lerp(partialTick,
                (float) turret.getLastRotationPitch(),
                (float) turret.getRotationPitch());
    }

    private static float spinDegrees(TurretBlockEntityBase turret, float partialTick) {
        return Mth.lerp(partialTick, turret.getLastSpin(), turret.getSpin());
    }

    private static float barrelLeftPos(TurretBlockEntityBase turret, float partialTick) {
        return Mth.lerp(partialTick, turret.getLastBarrelLeftPos(), turret.getBarrelLeftPos());
    }

    private static float barrelRightPos(TurretBlockEntityBase turret, float partialTick) {
        return Mth.lerp(partialTick, turret.getLastBarrelRightPos(), turret.getBarrelRightPos());
    }

    private static float artyBarrelPos(TurretArtyBlockEntity turret, float partialTick) {
        return Mth.lerp(partialTick, turret.getLastBarrelPos(), turret.getBarrelPos());
    }

    private static double renderTime(TurretBlockEntityBase turret, float partialTick) {
        return turret.getLevel() == null ? partialTick : turret.getLevel().getGameTime() + partialTick;
    }

    public enum StaticTurretModel {
        CHEKHOV(DEFAULT_STATIC_BOUNDS),
        FRIENDLY(DEFAULT_STATIC_BOUNDS),
        JEREMY(DEFAULT_STATIC_BOUNDS),
        RICHARD(DEFAULT_STATIC_BOUNDS),
        TAUON(DEFAULT_STATIC_BOUNDS),
        HOWARD(DEFAULT_STATIC_BOUNDS),
        SENTRY(SENTRY_STATIC_BOUNDS),
        HOWARD_DAMAGED(DEFAULT_STATIC_BOUNDS),
        SENTRY_DAMAGED(SENTRY_STATIC_BOUNDS),
        MAXWELL(DEFAULT_STATIC_BOUNDS),
        ARTY(ARTY_STATIC_BOUNDS),
        HIMARS(HIMARS_STATIC_BOUNDS),
        FRITZ(DEFAULT_STATIC_BOUNDS);

        private final AABB renderBounds;

        StaticTurretModel(AABB renderBounds) {
            this.renderBounds = renderBounds;
        }

        public AABB renderBounds() {
            return renderBounds;
        }
    }
}
