package com.hbm.ntm.client.renderer;

import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjProjectileModels;
import com.hbm.ntm.client.obj.ObjTurretModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
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
import com.mojang.math.Axis;
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
                        poseStack, buffer, light, packedOverlay);
            } else if (turret instanceof TurretHimarsBlockEntity himars) {
                renderHimarsPose(himars, yawDegrees(turret, partialTick), pitchDegrees(turret, partialTick),
                        partialTick, poseStack, buffer, light, packedOverlay);
            } else if (turret instanceof TurretSentryBlockEntity
                    || turret instanceof TurretSentryDamagedBlockEntity) {
                renderSentryPose(turret, turret instanceof TurretSentryDamagedBlockEntity,
                        sentryYawDegrees(turret, partialTick), pitchDegrees(turret, partialTick), barrelLeftPos(turret, partialTick),
                        barrelRightPos(turret, partialTick), poseStack, buffer, light, packedOverlay);
            } else if (turret instanceof TurretHowardBlockEntity
                    || turret instanceof TurretHowardDamagedBlockEntity) {
                if (turret instanceof TurretHowardBlockEntity) {
                    renderConnectors(turret, true, false, HbmFluids.NONE, poseStack, buffer, light, packedOverlay);
                }
                renderHowardPose(turret, turret instanceof TurretHowardDamagedBlockEntity, yawDegrees(turret, partialTick),
                        pitchDegrees(turret, partialTick), spinDegrees(turret, partialTick),
                        poseStack, buffer, light, packedOverlay);
            } else {
                renderStandardPose(turret, standardModel(turret), yawDegrees(turret, partialTick),
                        pitchDegrees(turret, partialTick), spinDegrees(turret, partialTick),
                        poseStack, buffer, light, packedOverlay, partialTick);
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
            renderArtilleryPose(null, model, STATIC_YAW, STATIC_PITCH, 0.0F, poseStack, buffer, light, overlay);
        } else if (model == StaticTurretModel.SENTRY || model == StaticTurretModel.SENTRY_DAMAGED) {
            renderSentryPose(null, model == StaticTurretModel.SENTRY_DAMAGED, 0.0F, STATIC_PITCH, 0.0F, 0.0F,
                    poseStack, buffer, light, overlay);
        } else if (model == StaticTurretModel.HOWARD || model == StaticTurretModel.HOWARD_DAMAGED) {
            renderHowardPose(null, model == StaticTurretModel.HOWARD_DAMAGED, STATIC_YAW, STATIC_PITCH, 0.0F,
                    poseStack, buffer, light, overlay);
        } else {
            renderStandardPose(null, model, STATIC_YAW, STATIC_PITCH, 0.0F, poseStack, buffer, light, overlay, 0.0F);
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
            float pitch, float spin, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
            float partialTick) {
        if (turret != null) {
            boolean fluid = turret instanceof TurretFritzBlockEntity;
            FluidType type = fluid ? ((TurretFritzBlockEntity) turret).getTank().getTankType() : HbmFluids.NONE;
            renderConnectors(turret, true, fluid, type, poseStack, buffer, light, overlay);
        }
        renderBase(model == StaticTurretModel.FRIENDLY, poseStack, buffer, light, overlay);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        if (model == StaticTurretModel.MAXWELL) {
            renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.HOWARD, "Carriage",
                    ObjTurretModels.CARRIAGE_CIWS_TEXTURE, poseStack, buffer, light, overlay));
        } else {
            renderAnimated(turret, () -> renderChekhovPart("Carriage",
                    model == StaticTurretModel.FRIENDLY
                            ? ObjTurretModels.CARRIAGE_FRIENDLY_TEXTURE
                            : ObjTurretModels.CARRIAGE_TEXTURE,
                    poseStack, buffer, light, overlay));
        }

        poseStack.translate(0.0D, 1.5D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        poseStack.translate(0.0D, -1.5D, 0.0D);

        if (model == StaticTurretModel.JEREMY) {
            renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.JEREMY, "Gun",
                    ObjTurretModels.JEREMY_TEXTURE, poseStack, buffer, light, overlay));
        } else if (model == StaticTurretModel.RICHARD) {
            int loaded = turret instanceof TurretRichardBlockEntity richard ? richard.getLoaded() : 1;
            renderAnimated(turret, () -> {
                ObjTurretModels.renderPart(ObjTurretModels.RICHARD, "Launcher", ObjTurretModels.RICHARD_TEXTURE,
                        poseStack, buffer, light, overlay);
                renderRichardLoadedMissiles(loaded, poseStack, buffer, light, overlay);
            });
        } else if (model == StaticTurretModel.TAUON) {
            renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.TAUON, "Cannon",
                    ObjTurretModels.TAUON_TEXTURE, poseStack, buffer, light, overlay));
            if (turret != null) {
                renderTauonBeam(turret, poseStack, buffer, partialTick);
            }
            poseStack.pushPose();
            poseStack.translate(0.0D, 1.375D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(-spin));
            poseStack.translate(0.0D, -1.375D, 0.0D);
            renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.TAUON, "Rotor",
                    ObjTurretModels.TAUON_TEXTURE, poseStack, buffer, light, overlay));
            poseStack.popPose();
        } else if (model == StaticTurretModel.MAXWELL) {
            renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.MAXWELL, "Microwave",
                    ObjTurretModels.MAXWELL_TEXTURE, poseStack, buffer, light, overlay));
            if (turret != null) {
                renderMaxwellBeam(turret, poseStack, buffer, partialTick);
            }
        } else if (model == StaticTurretModel.FRITZ) {
            renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.FRITZ, "Gun",
                    ObjTurretModels.FRITZ_TEXTURE, poseStack, buffer, light, overlay));
        } else {
            renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.CHEKHOV, "Body",
                    ObjTurretModels.CHEKHOV_TEXTURE, poseStack, buffer, light, overlay));
            poseStack.pushPose();
            poseStack.translate(0.0D, 1.5D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(-spin));
            poseStack.translate(0.0D, -1.5D, 0.0D);
            renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.CHEKHOV, "Barrels",
                    ObjTurretModels.CHEKHOV_BARRELS_TEXTURE, poseStack, buffer, light, overlay));
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
            ObjTurretModels.renderPart(ObjTurretModels.RICHARD, "MissileLoaded", ObjTurretModels.RICHARD_TEXTURE,
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
            ObjTurretModels.renderPart(ObjTurretModels.HOWARD, "Carriage", ObjTurretModels.CARRIAGE_CIWS_TEXTURE,
                    poseStack, buffer, light, overlay);
            ObjTurretModels.renderPart(ObjTurretModels.MAXWELL, "Microwave", ObjTurretModels.MAXWELL_TEXTURE,
                    poseStack, buffer, light, overlay);
        } else {
            renderChekhovPart("Carriage",
                    model == StaticTurretModel.FRIENDLY
                            ? ObjTurretModels.CARRIAGE_FRIENDLY_TEXTURE
                            : ObjTurretModels.CARRIAGE_TEXTURE,
                    poseStack, buffer, light, overlay);
            if (model == StaticTurretModel.JEREMY) {
                ObjTurretModels.renderPart(ObjTurretModels.JEREMY, "Gun", ObjTurretModels.JEREMY_TEXTURE,
                        poseStack, buffer, light, overlay);
            } else if (model == StaticTurretModel.RICHARD) {
                ObjTurretModels.renderPart(ObjTurretModels.RICHARD, "Launcher", ObjTurretModels.RICHARD_TEXTURE,
                        poseStack, buffer, light, overlay);
            } else if (model == StaticTurretModel.TAUON) {
                ObjTurretModels.renderPart(ObjTurretModels.TAUON, "Cannon", ObjTurretModels.TAUON_TEXTURE,
                        poseStack, buffer, light, overlay);
                ObjTurretModels.renderPart(ObjTurretModels.TAUON, "Rotor", ObjTurretModels.TAUON_TEXTURE,
                        poseStack, buffer, light, overlay);
            } else if (model == StaticTurretModel.FRITZ) {
                ObjTurretModels.renderPart(ObjTurretModels.FRITZ, "Gun", ObjTurretModels.FRITZ_TEXTURE,
                        poseStack, buffer, light, overlay);
            } else {
                ObjTurretModels.renderPart(ObjTurretModels.CHEKHOV, "Body", ObjTurretModels.CHEKHOV_TEXTURE,
                        poseStack, buffer, light, overlay);
                ObjTurretModels.renderPart(ObjTurretModels.CHEKHOV, "Barrels", ObjTurretModels.CHEKHOV_BARRELS_TEXTURE,
                        poseStack, buffer, light, overlay);
            }
        }
    }

    private static void renderHowardItemPose(boolean damaged, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        poseStack.translate(-0.75D, 0.0D, 0.0D);
        renderChekhovPart("Base", damaged ? ObjTurretModels.BASE_RUSTED_TEXTURE : ObjTurretModels.BASE_TEXTURE,
                poseStack, buffer, light, overlay);
        LegacyWavefrontModel bodyModel = damaged ? ObjTurretModels.HOWARD_DAMAGED : ObjTurretModels.HOWARD;
        ObjTurretModels.renderPart(ObjTurretModels.HOWARD, "Carriage", damaged
                ? ObjTurretModels.CARRIAGE_CIWS_RUSTED_TEXTURE
                : ObjTurretModels.CARRIAGE_CIWS_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderPart(bodyModel, "Body",
                damaged ? ObjTurretModels.HOWARD_RUSTED_TEXTURE : ObjTurretModels.HOWARD_TEXTURE,
                poseStack, buffer, light, overlay);
        ResourceLocation barrelsTexture = damaged
                ? ObjTurretModels.HOWARD_BARRELS_RUSTED_TEXTURE
                : ObjTurretModels.HOWARD_BARRELS_TEXTURE;
        ObjTurretModels.renderPart(bodyModel, "BarrelsTop", barrelsTexture, poseStack, buffer, light, overlay);
        ObjTurretModels.renderPart(bodyModel, "BarrelsBottom", barrelsTexture, poseStack, buffer, light, overlay);
    }

    private static void renderSentryItemPose(boolean damaged, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        ResourceLocation texture = damaged ? ObjTurretModels.SENTRY_DAMAGED_TEXTURE : ObjTurretModels.SENTRY_TEXTURE;
        ObjTurretModels.renderPart(ObjTurretModels.SENTRY, "Base", texture, poseStack, buffer, light, overlay);
        ObjTurretModels.renderPart(ObjTurretModels.SENTRY, "Pivot", texture, poseStack, buffer, light, overlay);
        ObjTurretModels.renderPart(ObjTurretModels.SENTRY, "Body", texture, poseStack, buffer, light, overlay);
        ObjTurretModels.renderPart(ObjTurretModels.SENTRY, "Drum", texture, poseStack, buffer, light, overlay);
        ObjTurretModels.renderPart(ObjTurretModels.SENTRY, "BarrelL", texture, poseStack, buffer, light, overlay);
        ObjTurretModels.renderPart(ObjTurretModels.SENTRY, "BarrelR", texture, poseStack, buffer, light, overlay);
    }

    private static void renderArtyItemPose(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjTurretModels.renderPart(ObjTurretModels.ARTY, "Base", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderPart(ObjTurretModels.ARTY, "Carriage", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        poseStack.translate(0.0D, 3.0D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.translate(0.0D, -3.0D, 0.0D);
        ObjTurretModels.renderPart(ObjTurretModels.ARTY, "Cannon", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderPart(ObjTurretModels.ARTY, "Barrel", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
    }

    private static void renderHimarsItemPose(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjTurretModels.renderPart(ObjTurretModels.ARTY, "Base", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderPart(ObjTurretModels.HIMARS, "Carriage", ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderPart(ObjTurretModels.HIMARS, "Launcher", ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderPart(ObjTurretModels.HIMARS, "Crane", ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.renderHimarsTubeStandard(ObjProjectileModels.HIMARS_STANDARD_TEXTURE, poseStack, buffer,
                light, overlay);
    }

    private static void renderTauonBeam(TurretBlockEntityBase turret, PoseStack poseStack,
            MultiBufferSource buffer, float partialTick) {
        LegacyTileRenderPlans.TauonBeamPlan plan = LegacyTileRenderPlans.tauonBeamPlan(turret.getBeamTicks(),
                turret.getBeamDistance(), renderTime(turret, partialTick));
        if (!plan.active() || plan.beam() == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(plan.translateX(), plan.translateY(), plan.translateZ());
        LegacyBeamRenderer.beam(poseStack, buffer, plan.beam());
        poseStack.popPose();
    }

    private static void renderMaxwellBeam(TurretBlockEntityBase turret, PoseStack poseStack,
            MultiBufferSource buffer, float partialTick) {
        LegacyTileRenderPlans.MaxwellBeamPlan plan = LegacyTileRenderPlans.maxwellBeamPlan(turret.getBeamTicks(),
                turret.getBeamDistance(), 2.125D, renderTime(turret, partialTick));
        if (!plan.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(plan.translateX(), plan.translateY(), plan.translateZ());
        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, queuedPose -> {
            for (LegacyBeamRenderer.BeamPlan beam : plan.beams()) {
                LegacyBeamRenderer.beam(queuedPose, buffer, beam);
            }
        });
        poseStack.popPose();
    }

    private static void renderHowardPose(TurretBlockEntityBase turret, boolean damaged, float yaw, float pitch, float spin,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        renderChekhovPart("Base", damaged ? ObjTurretModels.BASE_RUSTED_TEXTURE : ObjTurretModels.BASE_TEXTURE,
                poseStack, buffer, light, overlay);

        LegacyWavefrontModel model = damaged ? ObjTurretModels.HOWARD_DAMAGED : ObjTurretModels.HOWARD;
        ResourceLocation carriageTexture = damaged
                ? ObjTurretModels.CARRIAGE_CIWS_RUSTED_TEXTURE
                : ObjTurretModels.CARRIAGE_CIWS_TEXTURE;
        ResourceLocation bodyTexture = damaged ? ObjTurretModels.HOWARD_RUSTED_TEXTURE : ObjTurretModels.HOWARD_TEXTURE;
        ResourceLocation barrelsTexture = damaged
                ? ObjTurretModels.HOWARD_BARRELS_RUSTED_TEXTURE
                : ObjTurretModels.HOWARD_BARRELS_TEXTURE;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        renderAnimated(turret, () -> ObjTurretModels.renderPart(model, "Carriage", carriageTexture,
                poseStack, buffer, light, overlay));
        poseStack.translate(0.0D, 2.25D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        poseStack.translate(0.0D, -2.25D, 0.0D);
        renderAnimated(turret, () -> ObjTurretModels.renderPart(model, "Body", bodyTexture,
                poseStack, buffer, light, overlay));

        poseStack.pushPose();
        poseStack.translate(0.0D, 2.5D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-spin));
        poseStack.translate(0.0D, -2.5D, 0.0D);
        renderAnimated(turret, () -> ObjTurretModels.renderPart(model, "BarrelsTop", barrelsTexture,
                poseStack, buffer, light, overlay));
        poseStack.popPose();

        if (damaged) {
            renderAnimated(turret, () -> ObjTurretModels.renderPart(model, "BarrelsBottom", barrelsTexture,
                    poseStack, buffer, light, overlay));
        } else {
            poseStack.pushPose();
            poseStack.translate(0.0D, 2.0D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(spin));
            poseStack.translate(0.0D, -2.0D, 0.0D);
            renderAnimated(turret, () -> ObjTurretModels.renderPart(model, "BarrelsBottom", barrelsTexture,
                    poseStack, buffer, light, overlay));
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderSentryPose(TurretBlockEntityBase turret, boolean damaged, float yaw, float pitch,
            float leftRecoil, float rightRecoil, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        ResourceLocation texture = damaged ? ObjTurretModels.SENTRY_DAMAGED_TEXTURE : ObjTurretModels.SENTRY_TEXTURE;

        ObjTurretModels.renderPart(ObjTurretModels.SENTRY, "Base", texture, poseStack, buffer, light, overlay);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.SENTRY, "Pivot", texture,
                poseStack, buffer, light, overlay));
        poseStack.translate(0.0D, 1.25D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.translate(0.0D, -1.25D, 0.0D);
        renderAnimated(turret, () -> {
            ObjTurretModels.renderPart(ObjTurretModels.SENTRY, "Body", texture, poseStack, buffer, light, overlay);
            ObjTurretModels.renderPart(ObjTurretModels.SENTRY, "Drum", texture, poseStack, buffer, light, overlay);
        });

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, leftRecoil * -0.5D);
        renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.SENTRY, "BarrelL", texture,
                poseStack, buffer, light, overlay));
        poseStack.popPose();

        poseStack.pushPose();
        if (damaged) {
            poseStack.translate(0.0D, 1.5D, 0.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
            poseStack.translate(0.0D, -1.5D, -0.5D);
        } else {
            poseStack.translate(0.0D, 0.0D, rightRecoil * -0.5D);
        }
        renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.SENTRY, "BarrelR", texture,
                poseStack, buffer, light, overlay));
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderArtilleryPose(TurretBlockEntityBase turret, StaticTurretModel model, float yaw,
            float pitch, float barrelPos, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        ObjTurretModels.renderPart(ObjTurretModels.ARTY, "Base", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        if (model == StaticTurretModel.ARTY) {
            renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.ARTY, "Carriage",
                    ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay));
            poseStack.translate(0.0D, 3.0D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.translate(0.0D, -3.0D, 0.0D);
            renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.ARTY, "Cannon",
                    ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay));
            poseStack.translate(0.0D, 0.0D, barrelPos * 2.5D);
            renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.ARTY, "Barrel",
                    ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay));
        } else {
            renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.HIMARS, "Carriage",
                    ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay));
            poseStack.translate(0.0D, 2.25D, 2.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.translate(0.0D, -2.25D, -2.0D);
            renderAnimated(turret, () -> {
                ObjTurretModels.renderPart(ObjTurretModels.HIMARS, "Launcher", ObjTurretModels.HIMARS_TEXTURE,
                        poseStack, buffer, light, overlay);
                ObjTurretModels.renderPart(ObjTurretModels.HIMARS, "Crane", ObjTurretModels.HIMARS_TEXTURE,
                        poseStack, buffer, light, overlay);
                ObjTurretModels.renderHimarsTubeStandard(ObjProjectileModels.HIMARS_STANDARD_TEXTURE, poseStack, buffer,
                        light, overlay);
                for (int cap = 1; cap <= 6; cap++) {
                    ObjTurretModels.renderHimarsCapStandard(cap, ObjProjectileModels.HIMARS_STANDARD_TEXTURE,
                            poseStack, buffer, light, overlay);
                }
            });
        }
        poseStack.popPose();
    }

    private static void renderHimarsPose(TurretHimarsBlockEntity turret, float yaw, float pitch, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        ObjTurretModels.renderPart(ObjTurretModels.ARTY, "Base", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.HIMARS, "Carriage",
                ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay));
        poseStack.translate(0.0D, 2.25D, 2.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.translate(0.0D, -2.25D, -2.0D);
        renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.HIMARS, "Launcher",
                ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay));

        float crane = Mth.lerp(partialTick, turret.getLastCrane(), turret.getCrane());
        poseStack.translate(0.0D, 0.0D, crane * -5.0D);
        renderAnimated(turret, () -> ObjTurretModels.renderPart(ObjTurretModels.HIMARS, "Crane",
                ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay));

        int typeLoaded = turret.getTypeLoaded();
        List<LegacyArtilleryAmmoCatalog.HimarsRocket> rockets = LegacyArtilleryAmmoCatalog.himarsRockets();
        if (typeLoaded >= 0 && typeLoaded < rockets.size()) {
            LegacyArtilleryAmmoCatalog.HimarsRocket rocket = rockets.get(typeLoaded);
            ResourceLocation texture = himarsRocketTexture(rocket);
            if (rocket.modelType() == 0) {
                renderAnimated(turret, () -> ObjTurretModels.renderHimarsTubeStandard(texture, poseStack, buffer, light, overlay));
                int loaded = Mth.clamp(turret.getAmmoLoaded(), 0, rocket.amount());
                for (int i = 0; i < loaded; i++) {
                    int cap = 6 - i;
                    renderAnimated(turret, () -> ObjTurretModels.renderHimarsCapStandard(cap, texture,
                            poseStack, buffer, light, overlay));
                }
            } else if (rocket.modelType() == 1) {
                renderAnimated(turret, () -> ObjTurretModels.renderHimarsTubeSingle(texture, poseStack, buffer, light, overlay));
                if (turret.hasAmmo()) {
                    renderAnimated(turret, () -> ObjTurretModels.renderHimarsCapSingle(texture, poseStack, buffer, light, overlay));
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
        renderChekhovPart("Base", friendly ? ObjTurretModels.BASE_FRIENDLY_TEXTURE : ObjTurretModels.BASE_TEXTURE,
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
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.translate(localX, 0.0D, localZ);
        ObjTurretModels.renderPart(ObjTurretModels.CHEKHOV, "Connectors", ObjTurretModels.CONNECTOR_TEXTURE,
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

    private static void renderChekhovPart(String part, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        ObjTurretModels.renderPart(ObjTurretModels.CHEKHOV, part, texture, poseStack, buffer, light, overlay);
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
