package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjProjectileModels;
import com.hbm.ntm.client.obj.ObjTurretModels;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(T turret, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int light = LegacyRenderLighting.resolveBlockEntityLight(turret, packedLight);

        poseStack.pushPose();
        Vec3 offset = turret.getRenderHorizontalOffset();
        poseStack.translate(offset.x, 0.0D, offset.z);

        if (turret instanceof TurretArtyBlockEntity) {
            renderArtilleryPose(StaticTurretModel.ARTY, yawDegrees(turret, partialTick), pitchDegrees(turret, partialTick),
                    artyBarrelPos((TurretArtyBlockEntity) turret, partialTick), poseStack, buffer, light, packedOverlay);
        } else if (turret instanceof TurretHimarsBlockEntity) {
            renderArtilleryPose(StaticTurretModel.HIMARS, yawDegrees(turret, partialTick), pitchDegrees(turret, partialTick),
                    0.0F, poseStack, buffer, light, packedOverlay);
        } else if (turret instanceof TurretSentryBlockEntity
                || turret instanceof TurretSentryDamagedBlockEntity) {
            renderSentryPose(turret instanceof TurretSentryDamagedBlockEntity, sentryYawDegrees(turret, partialTick),
                    pitchDegrees(turret, partialTick), barrelLeftPos(turret, partialTick),
                    barrelRightPos(turret, partialTick), poseStack, buffer, light, packedOverlay);
        } else if (turret instanceof TurretHowardBlockEntity
                || turret instanceof TurretHowardDamagedBlockEntity) {
            renderHowardPose(turret instanceof TurretHowardDamagedBlockEntity, yawDegrees(turret, partialTick),
                    pitchDegrees(turret, partialTick), spinDegrees(turret, partialTick),
                    poseStack, buffer, light, packedOverlay);
        } else {
            renderStandardPose(turret, standardModel(turret), yawDegrees(turret, partialTick),
                    pitchDegrees(turret, partialTick), spinDegrees(turret, partialTick),
                    poseStack, buffer, light, packedOverlay, partialTick);
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
            renderArtilleryPose(model, STATIC_YAW, STATIC_PITCH, 0.0F, poseStack, buffer, light, overlay);
        } else if (model == StaticTurretModel.SENTRY || model == StaticTurretModel.SENTRY_DAMAGED) {
            renderSentryPose(model == StaticTurretModel.SENTRY_DAMAGED, 0.0F, STATIC_PITCH, 0.0F, 0.0F,
                    poseStack, buffer, light, overlay);
        } else if (model == StaticTurretModel.HOWARD || model == StaticTurretModel.HOWARD_DAMAGED) {
            renderHowardPose(model == StaticTurretModel.HOWARD_DAMAGED, STATIC_YAW, STATIC_PITCH, 0.0F,
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
            ObjTurretModels.HOWARD.renderPart("Carriage", ObjTurretModels.CARRIAGE_CIWS_TEXTURE,
                    poseStack, buffer, light, overlay);
        } else {
            renderChekhovPart("Carriage",
                    model == StaticTurretModel.FRIENDLY
                            ? ObjTurretModels.CARRIAGE_FRIENDLY_TEXTURE
                            : ObjTurretModels.CARRIAGE_TEXTURE,
                    poseStack, buffer, light, overlay);
        }

        poseStack.translate(0.0D, 1.5D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        poseStack.translate(0.0D, -1.5D, 0.0D);

        if (model == StaticTurretModel.JEREMY) {
            ObjTurretModels.JEREMY.renderPart("Gun", ObjTurretModels.JEREMY_TEXTURE, poseStack, buffer, light, overlay);
        } else if (model == StaticTurretModel.RICHARD) {
            ObjTurretModels.RICHARD.renderPart("Launcher", ObjTurretModels.RICHARD_TEXTURE, poseStack, buffer, light, overlay);
            renderRichardLoadedMissiles(turret instanceof TurretRichardBlockEntity richard ? richard.getLoaded() : 1,
                    poseStack, buffer, light, overlay);
        } else if (model == StaticTurretModel.TAUON) {
            ObjTurretModels.TAUON.renderPart("Cannon", ObjTurretModels.TAUON_TEXTURE, poseStack, buffer, light, overlay);
            if (turret != null) {
                renderTauonBeam(turret, poseStack, buffer, partialTick);
            }
            poseStack.pushPose();
            poseStack.translate(0.0D, 1.375D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(-spin));
            poseStack.translate(0.0D, -1.375D, 0.0D);
            ObjTurretModels.TAUON.renderPart("Rotor", ObjTurretModels.TAUON_TEXTURE, poseStack, buffer, light, overlay);
            poseStack.popPose();
        } else if (model == StaticTurretModel.MAXWELL) {
            ObjTurretModels.MAXWELL.renderPart("Microwave", ObjTurretModels.MAXWELL_TEXTURE, poseStack, buffer, light, overlay);
            if (turret != null) {
                renderMaxwellBeam(turret, poseStack, buffer, partialTick);
            }
        } else if (model == StaticTurretModel.FRITZ) {
            ObjTurretModels.FRITZ.renderPart("Gun", ObjTurretModels.FRITZ_TEXTURE, poseStack, buffer, light, overlay);
        } else {
            ObjTurretModels.CHEKHOV.renderPart("Body", ObjTurretModels.CHEKHOV_TEXTURE, poseStack, buffer, light, overlay);
            poseStack.pushPose();
            poseStack.translate(0.0D, 1.5D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(-spin));
            poseStack.translate(0.0D, -1.5D, 0.0D);
            ObjTurretModels.CHEKHOV.renderPart("Barrels", ObjTurretModels.CHEKHOV_BARRELS_TEXTURE,
                    poseStack, buffer, light, overlay);
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
            ObjTurretModels.RICHARD.renderPart("MissileLoaded", ObjTurretModels.RICHARD_TEXTURE,
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
            ObjTurretModels.HOWARD.renderPart("Carriage", ObjTurretModels.CARRIAGE_CIWS_TEXTURE,
                    poseStack, buffer, light, overlay);
            ObjTurretModels.MAXWELL.renderPart("Microwave", ObjTurretModels.MAXWELL_TEXTURE,
                    poseStack, buffer, light, overlay);
        } else {
            renderChekhovPart("Carriage",
                    model == StaticTurretModel.FRIENDLY
                            ? ObjTurretModels.CARRIAGE_FRIENDLY_TEXTURE
                            : ObjTurretModels.CARRIAGE_TEXTURE,
                    poseStack, buffer, light, overlay);
            if (model == StaticTurretModel.JEREMY) {
                ObjTurretModels.JEREMY.renderPart("Gun", ObjTurretModels.JEREMY_TEXTURE,
                        poseStack, buffer, light, overlay);
            } else if (model == StaticTurretModel.RICHARD) {
                ObjTurretModels.RICHARD.renderPart("Launcher", ObjTurretModels.RICHARD_TEXTURE,
                        poseStack, buffer, light, overlay);
            } else if (model == StaticTurretModel.TAUON) {
                ObjTurretModels.TAUON.renderPart("Cannon", ObjTurretModels.TAUON_TEXTURE,
                        poseStack, buffer, light, overlay);
                ObjTurretModels.TAUON.renderPart("Rotor", ObjTurretModels.TAUON_TEXTURE,
                        poseStack, buffer, light, overlay);
            } else if (model == StaticTurretModel.FRITZ) {
                ObjTurretModels.FRITZ.renderPart("Gun", ObjTurretModels.FRITZ_TEXTURE,
                        poseStack, buffer, light, overlay);
            } else {
                ObjTurretModels.CHEKHOV.renderPart("Body", ObjTurretModels.CHEKHOV_TEXTURE,
                        poseStack, buffer, light, overlay);
                ObjTurretModels.CHEKHOV.renderPart("Barrels", ObjTurretModels.CHEKHOV_BARRELS_TEXTURE,
                        poseStack, buffer, light, overlay);
            }
        }
    }

    private static void renderHowardItemPose(boolean damaged, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        poseStack.translate(-0.75D, 0.0D, 0.0D);
        renderChekhovPart("Base", damaged ? ObjTurretModels.BASE_RUSTED_TEXTURE : ObjTurretModels.BASE_TEXTURE,
                poseStack, buffer, light, overlay);
        LegacyWavefrontModel model = damaged ? ObjTurretModels.HOWARD_DAMAGED : ObjTurretModels.HOWARD;
        model.renderPart("Carriage", damaged
                ? ObjTurretModels.CARRIAGE_CIWS_RUSTED_TEXTURE
                : ObjTurretModels.CARRIAGE_CIWS_TEXTURE, poseStack, buffer, light, overlay);
        model.renderPart("Body", damaged ? ObjTurretModels.HOWARD_RUSTED_TEXTURE : ObjTurretModels.HOWARD_TEXTURE,
                poseStack, buffer, light, overlay);
        ResourceLocation barrelsTexture = damaged
                ? ObjTurretModels.HOWARD_BARRELS_RUSTED_TEXTURE
                : ObjTurretModels.HOWARD_BARRELS_TEXTURE;
        model.renderPart("BarrelsTop", barrelsTexture, poseStack, buffer, light, overlay);
        model.renderPart("BarrelsBottom", barrelsTexture, poseStack, buffer, light, overlay);
    }

    private static void renderSentryItemPose(boolean damaged, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        ResourceLocation texture = damaged ? ObjTurretModels.SENTRY_DAMAGED_TEXTURE : ObjTurretModels.SENTRY_TEXTURE;
        ObjTurretModels.SENTRY.renderPart("Base", texture, poseStack, buffer, light, overlay);
        ObjTurretModels.SENTRY.renderPart("Pivot", texture, poseStack, buffer, light, overlay);
        ObjTurretModels.SENTRY.renderPart("Body", texture, poseStack, buffer, light, overlay);
        ObjTurretModels.SENTRY.renderPart("Drum", texture, poseStack, buffer, light, overlay);
        ObjTurretModels.SENTRY.renderPart("BarrelL", texture, poseStack, buffer, light, overlay);
        ObjTurretModels.SENTRY.renderPart("BarrelR", texture, poseStack, buffer, light, overlay);
    }

    private static void renderArtyItemPose(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjTurretModels.ARTY.renderPart("Base", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.ARTY.renderPart("Carriage", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        poseStack.translate(0.0D, 3.0D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.translate(0.0D, -3.0D, 0.0D);
        ObjTurretModels.ARTY.renderPart("Cannon", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.ARTY.renderPart("Barrel", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
    }

    private static void renderHimarsItemPose(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjTurretModels.ARTY.renderPart("Base", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.HIMARS.renderPart("Carriage", ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.HIMARS.renderPart("Launcher", ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.HIMARS.renderPart("Crane", ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
        ObjTurretModels.HIMARS.renderPart("TubeStandard", ObjProjectileModels.HIMARS_STANDARD_TEXTURE,
                poseStack, buffer, light, overlay);
    }

    private static void renderTauonBeam(TurretBlockEntityBase turret, PoseStack poseStack,
            MultiBufferSource buffer, float partialTick) {
        if (turret.getBeamTicks() <= 0) {
            return;
        }
        double length = Math.max(0.0D, turret.getBeamDistance());
        if (length <= 0.0D) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, 0.0D);
        int start = (int) ((renderTime(turret, partialTick) / 5.0D) % 360.0D);
        LegacyBeamRenderer.lineBeam(poseStack, buffer, length, 0.0D, 0.0D,
                LegacyBeamRenderer.WaveType.RANDOM, 0xffa200, 0xffd000,
                start, (int) length + 1, 0.1F);
        poseStack.popPose();
    }

    private static void renderMaxwellBeam(TurretBlockEntityBase turret, PoseStack poseStack,
            MultiBufferSource buffer, float partialTick) {
        if (turret.getBeamTicks() <= 0) {
            return;
        }
        double length = Math.max(0.0D, turret.getBeamDistance() - 2.125D);
        if (length <= 0.0D) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(2.125D, 2.0D, 0.0D);
        double time = renderTime(turret, partialTick);
        int segments = (int) (turret.getBeamDistance() + 1.0D);
        for (int i = 0; i < 8; i++) {
            int start = (int) ((time * -50.0D + i * 45.0D) % 360.0D);
            LegacyBeamRenderer.solidBeam(poseStack, buffer, length, 0.0D, 0.0D,
                    LegacyBeamRenderer.WaveType.SPIRAL, 0x2020ff, 0x2020ff,
                    start, segments, 0.375F, 2, 0.05F);
        }
        poseStack.popPose();
    }

    private static void renderHowardPose(boolean damaged, float yaw, float pitch, float spin,
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
        model.renderPart("Carriage", carriageTexture, poseStack, buffer, light, overlay);
        poseStack.translate(0.0D, 2.25D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        poseStack.translate(0.0D, -2.25D, 0.0D);
        model.renderPart("Body", bodyTexture, poseStack, buffer, light, overlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 2.5D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-spin));
        poseStack.translate(0.0D, -2.5D, 0.0D);
        model.renderPart("BarrelsTop", barrelsTexture, poseStack, buffer, light, overlay);
        poseStack.popPose();

        if (damaged) {
            model.renderPart("BarrelsBottom", barrelsTexture, poseStack, buffer, light, overlay);
        } else {
            poseStack.pushPose();
            poseStack.translate(0.0D, 2.0D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(spin));
            poseStack.translate(0.0D, -2.0D, 0.0D);
            model.renderPart("BarrelsBottom", barrelsTexture, poseStack, buffer, light, overlay);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderSentryPose(boolean damaged, float yaw, float pitch, float leftRecoil, float rightRecoil,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        ResourceLocation texture = damaged ? ObjTurretModels.SENTRY_DAMAGED_TEXTURE : ObjTurretModels.SENTRY_TEXTURE;

        ObjTurretModels.SENTRY.renderPart("Base", texture, poseStack, buffer, light, overlay);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        ObjTurretModels.SENTRY.renderPart("Pivot", texture, poseStack, buffer, light, overlay);
        poseStack.translate(0.0D, 1.25D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.translate(0.0D, -1.25D, 0.0D);
        ObjTurretModels.SENTRY.renderPart("Body", texture, poseStack, buffer, light, overlay);
        ObjTurretModels.SENTRY.renderPart("Drum", texture, poseStack, buffer, light, overlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, leftRecoil * -0.5D);
        ObjTurretModels.SENTRY.renderPart("BarrelL", texture, poseStack, buffer, light, overlay);
        poseStack.popPose();

        poseStack.pushPose();
        if (damaged) {
            poseStack.translate(0.0D, 1.5D, 0.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
            poseStack.translate(0.0D, -1.5D, -0.5D);
        } else {
            poseStack.translate(0.0D, 0.0D, rightRecoil * -0.5D);
        }
        ObjTurretModels.SENTRY.renderPart("BarrelR", texture, poseStack, buffer, light, overlay);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderArtilleryPose(StaticTurretModel model, float yaw, float pitch, float barrelPos,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        ObjTurretModels.ARTY.renderPart("Base", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        if (model == StaticTurretModel.ARTY) {
            ObjTurretModels.ARTY.renderPart("Carriage", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
            poseStack.translate(0.0D, 3.0D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.translate(0.0D, -3.0D, 0.0D);
            ObjTurretModels.ARTY.renderPart("Cannon", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
            poseStack.translate(0.0D, 0.0D, barrelPos * 2.5D);
            ObjTurretModels.ARTY.renderPart("Barrel", ObjTurretModels.ARTY_TEXTURE, poseStack, buffer, light, overlay);
        } else {
            ObjTurretModels.HIMARS.renderPart("Carriage", ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
            poseStack.translate(0.0D, 2.25D, 2.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.translate(0.0D, -2.25D, -2.0D);
            ObjTurretModels.HIMARS.renderPart("Launcher", ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
            ObjTurretModels.HIMARS.renderPart("Crane", ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
            ObjTurretModels.HIMARS.renderPart("TubeStandard", ObjTurretModels.HIMARS_TEXTURE, poseStack, buffer, light, overlay);
            for (int cap = 1; cap <= 6; cap++) {
                ObjTurretModels.HIMARS.renderPart("CapStandard" + cap, ObjTurretModels.HIMARS_TEXTURE,
                        poseStack, buffer, light, overlay);
            }
        }
        poseStack.popPose();
    }

    private static void renderBase(boolean friendly, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        renderChekhovPart("Base", friendly ? ObjTurretModels.BASE_FRIENDLY_TEXTURE : ObjTurretModels.BASE_TEXTURE,
                poseStack, buffer, light, overlay);
    }

    private static void renderConnectors(TurretBlockEntityBase turret, boolean power, boolean fluid, FluidType type,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        renderConnectorIfConnected(turret, power, fluid, type, -2, 0, 0, 0, 0.0F, Direction.WEST,
                poseStack, buffer, light, overlay);
        renderConnectorIfConnected(turret, power, fluid, type, -2, -1, 0, -1, 0.0F, Direction.WEST,
                poseStack, buffer, light, overlay);

        renderConnectorIfConnected(turret, power, fluid, type, -1, 1, 0, -1, 90.0F, Direction.SOUTH,
                poseStack, buffer, light, overlay);
        renderConnectorIfConnected(turret, power, fluid, type, 0, 1, 0, 0, 90.0F, Direction.SOUTH,
                poseStack, buffer, light, overlay);

        renderConnectorIfConnected(turret, power, fluid, type, 1, 0, 0, -1, 180.0F, Direction.EAST,
                poseStack, buffer, light, overlay);
        renderConnectorIfConnected(turret, power, fluid, type, 1, -1, 0, 0, 180.0F, Direction.EAST,
                poseStack, buffer, light, overlay);

        renderConnectorIfConnected(turret, power, fluid, type, 0, -2, 0, -1, 270.0F, Direction.NORTH,
                poseStack, buffer, light, overlay);
        renderConnectorIfConnected(turret, power, fluid, type, -1, -2, 0, 0, 270.0F, Direction.NORTH,
                poseStack, buffer, light, overlay);
    }

    private static void renderConnectorIfConnected(TurretBlockEntityBase turret, boolean power, boolean fluid,
            FluidType type, int checkX, int checkZ, int localX, int localZ, float yaw, Direction cableSide,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (!hasConnector(turret, power, fluid, type, checkX, checkZ, cableSide)) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.translate(localX, 0.0D, localZ);
        ObjTurretModels.CHEKHOV.renderPart("Connectors", ObjTurretModels.CONNECTOR_TEXTURE,
                poseStack, buffer, light, overlay);
        poseStack.popPose();
    }

    private static boolean hasConnector(TurretBlockEntityBase turret, boolean power, boolean fluid, FluidType type,
            int checkX, int checkZ, Direction cableSide) {
        if (turret.getLevel() == null) {
            return false;
        }
        Vec3 offset = turret.getRenderHorizontalOffset();
        BlockPos pivot = turret.getBlockPos().offset((int) offset.x, 0, (int) offset.z);
        BlockPos checkPos = pivot.offset(checkX, 0, checkZ);
        if (power && HbmEnergyConnectionUtil.canConnectLegacy(turret.getLevel(), checkPos, cableSide)) {
            return true;
        }
        if (!fluid || type == null || type == HbmFluids.NONE) {
            return false;
        }
        return canConnectFluidLegacy(turret, checkPos, type, cableSide);
    }

    private static boolean canConnectFluidLegacy(TurretBlockEntityBase turret, BlockPos targetPos, FluidType type,
            Direction ductSide) {
        if (turret.getLevel() == null || targetPos.getY() < turret.getLevel().getMinBuildHeight()
                || targetPos.getY() >= turret.getLevel().getMaxBuildHeight()) {
            return false;
        }
        Direction targetSide = ductSide.getOpposite();
        Block block = turret.getLevel().getBlockState(targetPos).getBlock();
        if (block instanceof HbmFluidConnectorBlock connectorBlock
                && connectorBlock.canConnectFluid(turret.getLevel(), targetPos, type, targetSide)) {
            return true;
        }
        BlockEntity blockEntity = turret.getLevel().getBlockEntity(targetPos);
        return blockEntity instanceof HbmFluidConnector connector
                && connector.canConnectFluid(type, targetSide);
    }

    private static void renderChekhovPart(String part, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        ObjTurretModels.CHEKHOV.renderPart(part, texture, poseStack, buffer, light, overlay);
    }

    private static float yawDegrees(TurretBlockEntityBase turret, float partialTick) {
        return -Mth.RAD_TO_DEG * (float) shortAngleLerpRadians(partialTick,
                turret.getLastRotationYaw(), turret.getRotationYaw()) - 90.0F;
    }

    private static float sentryYawDegrees(TurretBlockEntityBase turret, float partialTick) {
        return -Mth.RAD_TO_DEG * (float) shortAngleLerpRadians(partialTick,
                turret.getLastRotationYaw(), turret.getRotationYaw());
    }

    private static double shortAngleLerpRadians(float partialTick, double previous, double current) {
        double delta = (current - previous) % (Math.PI * 2.0D);
        if (delta < -Math.PI) {
            delta += Math.PI * 2.0D;
        } else if (delta > Math.PI) {
            delta -= Math.PI * 2.0D;
        }
        return previous + delta * partialTick;
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
