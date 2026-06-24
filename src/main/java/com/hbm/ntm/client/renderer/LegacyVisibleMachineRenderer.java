package com.hbm.ntm.client.renderer;

import com.hbm.config.ClientConfig;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachineRenderProfile;
import com.hbm.ntm.block.LegacyVisibleMachineBlock;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.BoilerBlockEntity;
import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.blockentity.CombinationOvenBlockEntity;
import com.hbm.ntm.blockentity.CompressorBlockEntity;
import com.hbm.ntm.blockentity.CrucibleBlockEntity;
import com.hbm.ntm.blockentity.CyclotronBlockEntity;
import com.hbm.ntm.blockentity.DieselGeneratorBlockEntity;
import com.hbm.ntm.blockentity.FireboxHeaterBlockEntity;
import com.hbm.ntm.blockentity.FelBlockEntity;
import com.hbm.ntm.blockentity.AmmoPressBlockEntity;
import com.hbm.ntm.blockentity.ArcWelderBlockEntity;
import com.hbm.ntm.blockentity.AshpitBlockEntity;
import com.hbm.ntm.blockentity.BatteryReddBlockEntity;
import com.hbm.ntm.blockentity.HephaestusBlockEntity;
import com.hbm.ntm.blockentity.IntakeBlockEntity;
import com.hbm.ntm.blockentity.LegacyFurnaceBlockEntity;
import com.hbm.ntm.blockentity.LegacyGenericSelectorMachineBlockEntity;
import com.hbm.ntm.blockentity.MixerBlockEntity;
import com.hbm.ntm.blockentity.PoweredCondenserBlockEntity;
import com.hbm.ntm.blockentity.ProcessingMachineBlockEntity;
import com.hbm.ntm.blockentity.RadGenBlockEntity;
import com.hbm.ntm.blockentity.RefineryBlockEntity;
import com.hbm.ntm.blockentity.RotaryFurnaceBlockEntity;
import com.hbm.ntm.blockentity.SawmillBlockEntity;
import com.hbm.ntm.blockentity.SolarBoilerBlockEntity;
import com.hbm.ntm.blockentity.StrandCasterBlockEntity;
import com.hbm.ntm.blockentity.StirlingBlockEntity;
import com.hbm.ntm.blockentity.WaterPumpBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.energy.HbmEnergyConnectionUtil;
import com.hbm.ntm.energy.HbmEnergyConnector;
import com.hbm.ntm.item.LaserWavelength;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.util.BobMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class LegacyVisibleMachineRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();
    private static final LegacyWavefrontModel.SelectionHandle DIESEL_GENERATOR =
            ObjMachineModels.DIESELGEN_LEGACY.prepareRenderOnlyInCallOrder("Generator");
    private static final LegacyWavefrontModel.SelectionHandle DIESEL_ENGINE =
            ObjMachineModels.DIESELGEN_LEGACY.prepareRenderOnlyInCallOrder("Engine");
    private static final LegacyWavefrontModel.SelectionHandle COMPRESSOR_BODY =
            ObjMachineModels.COMPRESSOR.prepareRenderOnlyInCallOrder("Compressor");
    private static final LegacyWavefrontModel.SelectionHandle COMPRESSOR_PUMP =
            ObjMachineModels.COMPRESSOR.prepareRenderOnlyInCallOrder("Pump");
    private static final LegacyWavefrontModel.SelectionHandle COMPRESSOR_FAN =
            ObjMachineModels.COMPRESSOR.prepareRenderOnlyInCallOrder("Fan");
    private static final LegacyWavefrontModel.SelectionHandle CONDENSER_BODY =
            ObjMachineModels.CONDENSER.prepareRenderOnlyInCallOrder("Condenser");
    private static final LegacyWavefrontModel.SelectionHandle CONDENSER_FAN_1 =
            ObjMachineModels.CONDENSER.prepareRenderOnlyInCallOrder("Fan1");
    private static final LegacyWavefrontModel.SelectionHandle CONDENSER_FAN_2 =
            ObjMachineModels.CONDENSER.prepareRenderOnlyInCallOrder("Fan2");
    private static final LegacyWavefrontModel.SelectionHandle PUMP_BASE =
            ObjMachineModels.PUMP.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle PUMP_ROTOR =
            ObjMachineModels.PUMP.prepareRenderOnlyInCallOrder("Rotor");
    private static final LegacyWavefrontModel.SelectionHandle PUMP_ARMS =
            ObjMachineModels.PUMP.prepareRenderOnlyInCallOrder("Arms");
    private static final LegacyWavefrontModel.SelectionHandle PUMP_PISTON =
            ObjMachineModels.PUMP.prepareRenderOnlyInCallOrder("Piston");
    private static final LegacyWavefrontModel.SelectionHandle ANNIHILATOR_BODY =
            ObjMachineModels.ANNIHILATOR.prepareRenderOnlyInCallOrder("Annihilator");
    private static final LegacyWavefrontModel.SelectionHandle ANNIHILATOR_ROLLER =
            ObjMachineModels.ANNIHILATOR.prepareRenderOnlyInCallOrder("Roller");
    private static final LegacyWavefrontModel.SelectionHandle ANNIHILATOR_BELT =
            ObjMachineModels.ANNIHILATOR.prepareRenderOnlyInCallOrder("Belt");
    private static final LegacyWavefrontModel.SelectionHandle COMBUSTION_ENGINE_BODY =
            ObjMachineModels.COMBUSTION_ENGINE.prepareRenderOnlyInCallOrder("Engine");
    private static final LegacyWavefrontModel.SelectionHandle COMBUSTION_ENGINE_CANISTER =
            ObjMachineModels.COMBUSTION_ENGINE.prepareRenderOnlyInCallOrder("Canister");
    private static final LegacyWavefrontModel.SelectionHandle COMBUSTION_ENGINE_HATCH =
            ObjMachineModels.COMBUSTION_ENGINE.prepareRenderOnlyInCallOrder("Hatch");
    private static final LegacyWavefrontModel.SelectionHandle FIREBOX_MAIN =
            ObjMachineModels.FIREBOX_LEGACY.prepareRenderOnlyInCallOrder("Main");
    private static final LegacyWavefrontModel.SelectionHandle FIREBOX_DOOR =
            ObjMachineModels.FIREBOX_LEGACY.prepareRenderOnlyInCallOrder("Door");
    private static final LegacyWavefrontModel.SelectionHandle FIREBOX_INNER_EMPTY =
            ObjMachineModels.FIREBOX_LEGACY.prepareRenderOnlyInCallOrder("InnerEmpty");
    private static final LegacyWavefrontModel.SelectionHandle FIREBOX_INNER_BURNING =
            ObjMachineModels.FIREBOX_LEGACY.prepareRenderOnlyInCallOrder("InnerBurning");
    private static final LegacyWavefrontModel.SelectionHandle HEATING_OVEN_MAIN =
            ObjMachineModels.HEATING_OVEN_LEGACY.prepareRenderOnlyInCallOrder("Main");
    private static final LegacyWavefrontModel.SelectionHandle HEATING_OVEN_DOOR =
            ObjMachineModels.HEATING_OVEN_LEGACY.prepareRenderOnlyInCallOrder("Door");
    private static final LegacyWavefrontModel.SelectionHandle HEATING_OVEN_INNER =
            ObjMachineModels.HEATING_OVEN_LEGACY.prepareRenderOnlyInCallOrder("Inner");
    private static final LegacyWavefrontModel.SelectionHandle HEATING_OVEN_INNER_BURNING =
            ObjMachineModels.HEATING_OVEN_LEGACY.prepareRenderOnlyInCallOrder("InnerBurning");
    private static final LegacyWavefrontModel.SelectionHandle HEPHAESTUS_MAIN =
            ObjMachineModels.HEPHAESTUS_LEGACY.prepareRenderOnlyInCallOrder("Main");
    private static final LegacyWavefrontModel.SelectionHandle HEPHAESTUS_ROTOR =
            ObjMachineModels.HEPHAESTUS_LEGACY.prepareRenderOnlyInCallOrder("Rotor");
    private static final LegacyWavefrontModel.SelectionHandle HEPHAESTUS_CORE =
            ObjMachineModels.HEPHAESTUS_LEGACY.prepareRenderOnlyInCallOrder("Core");
    private static final LegacyWavefrontModel.SelectionHandle MIXER_MAIN =
            ObjMachineModels.MIXER.prepareRenderOnlyInCallOrder("Main");
    private static final LegacyWavefrontModel.SelectionHandle MIXER_BLADE =
            ObjMachineModels.MIXER.prepareRenderOnlyInCallOrder("Mixer");
    private static final LegacyWavefrontModel.SelectionHandle MIXER_FLUID =
            ObjMachineModels.MIXER.prepareRenderOnlyInCallOrder("Fluid");
    private static final LegacyWavefrontModel.SelectionHandle INTAKE_BASE =
            ObjMachineModels.INTAKE_LEGACY.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle INTAKE_FAN =
            ObjMachineModels.INTAKE_LEGACY.prepareRenderOnlyInCallOrder("Fan");
    private static final LegacyWavefrontModel.SelectionHandle CRYSTALLIZER_BODY =
            ObjMachineModels.ACIDIZER.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle CRYSTALLIZER_SPINNER =
            ObjMachineModels.ACIDIZER.prepareRenderOnlyInCallOrder("Spinner");
    private static final LegacyWavefrontModel.SelectionHandle CRYSTALLIZER_FLUID =
            ObjMachineModels.ACIDIZER.prepareRenderOnlyInCallOrder("Fluid");
    private static final LegacyWavefrontModel.SelectionHandle AMMO_PRESS_FRAME =
            ObjMachineModels.AMMO_PRESS.prepareRenderOnlyInCallOrder("Frame");
    private static final LegacyWavefrontModel.SelectionHandle AMMO_PRESS_PRESS =
            ObjMachineModels.AMMO_PRESS.prepareRenderOnlyInCallOrder("Press");
    private static final LegacyWavefrontModel.SelectionHandle AMMO_PRESS_SHELLS =
            ObjMachineModels.AMMO_PRESS.prepareRenderOnlyInCallOrder("Shells");
    private static final LegacyWavefrontModel.SelectionHandle AMMO_PRESS_BULLETS =
            ObjMachineModels.AMMO_PRESS.prepareRenderOnlyInCallOrder("Bullets");
    private static final LegacyWavefrontModel.SelectionHandle ROTARY_FURNACE_BODY =
            ObjMachineModels.ROTARY_FURNACE.prepareRenderOnlyInCallOrder("Furnace");
    private static final LegacyWavefrontModel.SelectionHandle ROTARY_FURNACE_PISTON =
            ObjMachineModels.ROTARY_FURNACE.prepareRenderOnlyInCallOrder("Piston");
    private static final LegacyWavefrontModel.SelectionHandle SAWMILL_MAIN =
            ObjMachineModels.SAWMILL.prepareRenderOnlyInCallOrder("Main");
    private static final LegacyWavefrontModel.SelectionHandle SAWMILL_BLADE =
            ObjMachineModels.SAWMILL.prepareRenderOnlyInCallOrder("Blade");
    private static final LegacyWavefrontModel.SelectionHandle SAWMILL_GEAR_LEFT =
            ObjMachineModels.SAWMILL.prepareRenderOnlyInCallOrder("GearLeft");
    private static final LegacyWavefrontModel.SelectionHandle SAWMILL_GEAR_RIGHT =
            ObjMachineModels.SAWMILL.prepareRenderOnlyInCallOrder("GearRight");
    private static final LegacyWavefrontModel.SelectionHandle STIRLING_BASE =
            ObjMachineModels.STIRLING.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle STIRLING_COG =
            ObjMachineModels.STIRLING.prepareRenderOnlyInCallOrder("Cog");
    private static final LegacyWavefrontModel.SelectionHandle STIRLING_COG_SMALL =
            ObjMachineModels.STIRLING.prepareRenderOnlyInCallOrder("CogSmall");
    private static final LegacyWavefrontModel.SelectionHandle STIRLING_PISTON =
            ObjMachineModels.STIRLING.prepareRenderOnlyInCallOrder("Piston");
    private static final LegacyWavefrontModel.SelectionHandle PUREX_BASE =
            ObjMachineModels.PUREX.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle PUREX_FRAME =
            ObjMachineModels.PUREX.prepareRenderOnlyInCallOrder("Frame");
    private static final LegacyWavefrontModel.SelectionHandle PUREX_FAN =
            ObjMachineModels.PUREX.prepareRenderOnlyInCallOrder("Fan");
    private static final LegacyWavefrontModel.SelectionHandle PUREX_PUMP =
            ObjMachineModels.PUREX.prepareRenderOnlyInCallOrder("Pump");
    private static final LegacyWavefrontModel.SelectionHandle CYCLOTRON_BODY =
            ObjMachineModels.CYCLOTRON.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle CYCLOTRON_PLUG_1 =
            ObjMachineModels.CYCLOTRON.prepareRenderOnlyInCallOrder("B1");
    private static final LegacyWavefrontModel.SelectionHandle CYCLOTRON_PLUG_2 =
            ObjMachineModels.CYCLOTRON.prepareRenderOnlyInCallOrder("B2");
    private static final LegacyWavefrontModel.SelectionHandle CYCLOTRON_PLUG_3 =
            ObjMachineModels.CYCLOTRON.prepareRenderOnlyInCallOrder("B3");
    private static final LegacyWavefrontModel.SelectionHandle CYCLOTRON_PLUG_4 =
            ObjMachineModels.CYCLOTRON.prepareRenderOnlyInCallOrder("B4");
    private static final LegacyWavefrontModel.SelectionHandle BATTERY_REDD_BASE =
            ObjMachineModels.BATTERY_REDD_LEGACY.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle BATTERY_REDD_WHEEL =
            ObjMachineModels.BATTERY_REDD_LEGACY.prepareRenderOnlyInCallOrder("Wheel");
    private static final LegacyWavefrontModel.SelectionHandle BATTERY_REDD_LIGHTS =
            ObjMachineModels.BATTERY_REDD_LEGACY.prepareRenderOnlyInCallOrder("Lights");
    private static final LegacyWavefrontModel.SelectionHandle BATTERY_REDD_PLASMA =
            ObjMachineModels.BATTERY_REDD_LEGACY.prepareRenderOnlyInCallOrder("Plasma");
    private static final ResourceLocation BOILER_BURST_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/machines/boiler_burst.obj");
    private static final ResourceLocation REFINERY_EXPLODED_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/refinery_exploded.obj");
    private static final ResourceLocation COMBINATION_FIRE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/rbmk_fire.png");
    private static final ResourceLocation HEPHAESTUS_CORE_ACTIVE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/lava.png");
    private static final ResourceLocation HEPHAESTUS_CORE_IDLE_TEXTURE =
            new ResourceLocation("minecraft", "textures/block/cobblestone.png");
    private static final ResourceLocation STRAND_CASTER_LAVA_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/lava_gray.png");
    private static final ResourceLocation CRUCIBLE_LAVA_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/lava.png");
    private static final ResourceLocation STANDARD_GALACTIC_FONT =
            new ResourceLocation("minecraft", "alt");
    private static final String CYCLOTRON_GLYPH_MESSAGE = "plures necat crapula quam gladius";
    private static final int CYCLOTRON_GLYPH_COLOR = 0x600060;
    private static final ResourceLocation BATTERY_REDD_PLASMA_TEXTURE =
            ObjFusionModels.PLASMA_TEXTURE;
    private static final ResourceLocation BATTERY_REDD_PLASMA_SPARKLE_TEXTURE =
            ObjFusionModels.PLASMA_SPARKLE_TEXTURE;
    private static final ResourceLocation[] CYCLOTRON_EMPTY_TEXTURES = {
            cyclotronPlugTexture("cyclotron_ashes"),
            cyclotronPlugTexture("cyclotron_book"),
            cyclotronPlugTexture("cyclotron_gavel"),
            cyclotronPlugTexture("cyclotron_coin")
    };
    private static final ResourceLocation[] CYCLOTRON_FILLED_TEXTURES = {
            cyclotronPlugTexture("cyclotron_ashes_filled"),
            cyclotronPlugTexture("cyclotron_book_filled"),
            cyclotronPlugTexture("cyclotron_gavel_filled"),
            cyclotronPlugTexture("cyclotron_coin_filled")
    };
    private static final Map<ResourceLocation, LegacyWavefrontModel> EXTRA_MODELS = new IdentityHashMap<>();

    public LegacyVisibleMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        LegacyMachineDefinition definition = visibleDefinition(state);
        if (definition == null) {
            return;
        }

        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()).asVBO());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay)
                .withRenderMode(LegacyMachinePartRenderContexts.renderMode(definition.renderMode()));
        if (definition.renderAll()) {
            if (renderProfile(blockEntity, partialTick, definition, model, context, poseStack)) {
                poseStack.popPose();
                return;
            }
            model.renderAll(definition.textureLocation(), context);
        } else {
            if (!renderProfile(blockEntity, partialTick, definition, model, context, poseStack)) {
                renderParts(definition, model, context);
            }
        }

        renderFelBeam(blockEntity, poseStack, buffer);

        poseStack.popPose();
        renderSolarBoilerBeams(blockEntity, poseStack, buffer);
    }

    private static void renderFelBeam(BlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer) {
        if (!(blockEntity instanceof FelBlockEntity fel) || !fel.isBeamActive()) {
            return;
        }
        int length = fel.getDistance() - 3;
        if (length <= 0) {
            return;
        }
        long gameTime = gameTime(blockEntity);
        int color = beamColor(fel.getMode(), gameTime);
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, -1.5D);
        LegacyBeamRenderer.beam(poseStack, buffer, LegacyBeamRenderer.beamPlanWithDepth(
                0.0D, 0.0D, -length - 1.0D,
                LegacyBeamRenderer.WaveType.SPIRAL, LegacyBeamRenderer.BeamType.SOLID,
                color, color, 0, 1, 0.0F, 2, 0.0625F));
        LegacyBeamRenderer.beam(poseStack, buffer, LegacyBeamRenderer.beamPlanWithDepth(
                0.0D, 0.0D, -length - 1.0D,
                LegacyBeamRenderer.WaveType.RANDOM, LegacyBeamRenderer.BeamType.SOLID,
                color, color, (int) (gameTime % 1_000L / 2L),
                length / 2 + 1, 0.0625F, 2, 0.0625F));
        poseStack.popPose();
    }

    private static int beamColor(LaserWavelength wavelength, long gameTime) {
        if (wavelength == LaserWavelength.VISIBLE) {
            return Mth.hsvToRgb(Mth.frac(gameTime / 50.0F), 0.5F, 0.1F);
        }
        int color = wavelength.renderedBeamColor();
        return color == 0 ? 0xFFFFFF : color;
    }

    private static long gameTime(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        return level == null ? 0L : level.getGameTime();
    }

    private static void renderSolarBoilerBeams(BlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (!(blockEntity instanceof SolarBoilerBlockEntity boiler) || !fancyGraphics()) {
            return;
        }
        List<LegacyTileRenderPlans.SolarBeamTargetPlan> targets = new ArrayList<>();
        BlockPos boilerPos = boiler.getBlockPos();
        for (BlockPos mirrorPos : boiler.getSolarMirrorBeamTargets()) {
            targets.add(new LegacyTileRenderPlans.SolarBeamTargetPlan(
                    boilerPos.getX() - mirrorPos.getX(),
                    boilerPos.getY() - mirrorPos.getY(),
                    boilerPos.getZ() - mirrorPos.getZ()));
        }
        LegacyTileRenderPlans.SolarBoilerBeamPlan plan =
                LegacyTileRenderPlans.solarBoilerBeamPlan(targets, ClientConfig.renderHeliostatBeamLimit());
        if (!plan.active()) {
            return;
        }
        VertexConsumer consumer = LegacyUntexturedQuadRenderer.additiveNoCull(buffer);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        for (LegacyTileRenderPlans.SolarBeamPlan beam : plan.beams()) {
            poseStack.pushPose();
            LegacyTileRenderPlans.SolarBeamTargetPlan target = beam.target();
            poseStack.translate(-target.dx(), -target.dy(), -target.dz());
            poseStack.translate(0.0D, 1.0D, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) beam.yawDegrees()));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) beam.pitchDegrees()));
            poseStack.translate(0.0D, -1.0D, 0.0D);
            renderSolarBeamQuads(consumer, poseStack, beam.quads());
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static boolean fancyGraphics() {
        GraphicsStatus graphics = Minecraft.getInstance().options.graphicsMode().get();
        return graphics == GraphicsStatus.FANCY || graphics == GraphicsStatus.FABULOUS;
    }

    private static void renderSolarBeamQuads(VertexConsumer consumer, PoseStack poseStack,
            List<LegacyTileRenderPlans.UntexturedQuadPlan> quads) {
        PoseStack.Pose pose = poseStack.last();
        for (LegacyTileRenderPlans.UntexturedQuadPlan quad : quads) {
            for (LegacyTileRenderPlans.UntexturedVertexPlan vertex : quad.vertices()) {
                LegacyTileRenderPlans.RgbaPlan color = vertex.color();
                LegacyUntexturedQuadRenderer.vertexRgbaF(consumer, pose,
                        vertex.x(), vertex.y(), vertex.z(),
                        color.red(), color.green(), color.blue(), color.alpha());
            }
        }
    }

    private static void renderParts(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context) {
        LegacyMachinePartRenderSelection.Selection selection = LegacyMachinePartRenderSelection.world(definition);
        renderParts(selection.opaqueRuns(), model, context);
        renderParts(selection.translucentRuns(), model, context);
    }

    private static void renderParts(List<LegacyMachinePartRenderSelection.Run> parts, LegacyWavefrontModel model,
            ObjRenderContext context) {
        LegacyMachinePartBatchRenderer.renderRuns(parts, model, context);
    }

    private static ObjRenderContext partContext(LegacyMachineDefinition definition, String part,
            ObjRenderContext context) {
        return LegacyMachinePartRenderContexts.apply(context, definition.partRenderProperties().get(part));
    }

    private static boolean renderProfile(BlockEntity blockEntity, float partialTick, LegacyMachineDefinition definition,
            LegacyWavefrontModel model, ObjRenderContext context, PoseStack poseStack) {
        long currentMillis;
        switch (definition.renderProfile()) {
            case ANNIHILATOR_UV_SCROLL -> {
                currentMillis = System.currentTimeMillis();
                renderAnnihilatorPart(model, "Annihilator", definition.textureLocation(), context);
                renderAnnihilatorRotatingPart(model, LegacyTileRenderPlans.annihilatorRollerPlan(currentMillis),
                        definition.textureLocation(), context, poseStack);
                LegacyTileRenderPlans.TextureMatrixPartPlan belt = LegacyTileRenderPlans.annihilatorBeltPlan(currentMillis);
                renderAnnihilatorPart(model, belt.partName(), definition.partTextures().getOrDefault(belt.partName(),
                        definition.textureLocation()), context.withTextureMatrixPlan(belt.textureMatrix()));
                return true;
            }
            case RADGEN_STATIC_SPECIAL -> {
                renderRadGen(definition, model, context, poseStack, blockEntity);
                return true;
            }
            case BATTERY_REDD_STATIC_SPECIAL -> {
                renderBatteryRedd(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case CRYSTALLIZER_STATIC_SPECIAL -> {
                renderVisibleMachineStaticPlan(definition, model,
                        LegacyTileRenderPlans.crystallizerStaticPlan(false), context);
                return true;
            }
            case CRYSTALLIZER_RUNNING_PARTS -> {
                renderCrystallizer(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case AMMO_PRESS_RUNNING_PARTS -> {
                renderAmmoPress(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case PRECASS_RUNNING_PARTS -> {
                renderPrecass(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case PUREX_RUNNING_PARTS -> {
                renderPurex(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case FURNACE_IRON_BURN_STATE -> {
                renderFurnaceIron(definition, model, context, blockEntity);
                return true;
            }
            case FURNACE_STEEL_FIRE -> {
                renderFurnaceSteel(definition, model, context, poseStack, blockEntity);
                return true;
            }
            case COMBINATION_OVEN_FIRE -> {
                renderCombinationOven(definition, model, context, poseStack, blockEntity);
                return true;
            }
            case MIXER_RUNNING_PARTS -> {
                renderMixer(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case CYCLOTRON_PLUGS -> {
                renderCyclotron(definition, model, context, blockEntity);
                return true;
            }
            case STRAND_CASTER_MOLTEN -> {
                renderStrandCaster(definition, model, context, poseStack, blockEntity);
                return true;
            }
            case CRUCIBLE_MOLTEN -> {
                renderCrucible(definition, model, context, blockEntity);
                return true;
            }
            case ROTARY_FURNACE_PISTON -> {
                renderRotaryFurnace(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case ASHPIT_DOOR_INNER -> {
                renderAshpit(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case ARC_WELDER_DISPLAY_OUTPUT -> {
                renderArcWelder(definition, model, context, poseStack, blockEntity);
                return true;
            }
            case ARC_FURNACE_STATIC_PREVIEW -> {
                LegacyArcFurnaceRenderHelper.renderPlan(model,
                        LegacyTileRenderPlans.arcFurnaceStaticPreviewPlan(), context, poseStack);
                return true;
            }
            case RTG_CONNECTORS -> {
                renderRtg(definition, model, context, poseStack, blockEntity);
                return true;
            }
            case COMPRESSOR_RUNNING_PARTS -> {
                renderCompressor(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case COMPRESSOR_COMPACT_RUNNING_FANS -> {
                renderCompressorCompact(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case POWERED_CONDENSER_FANS -> {
                renderPoweredCondenser(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case COMBUSTION_ENGINE_DOOR_CANISTER -> {
                renderCombustionEngine(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case DIESEL_GENERATOR_RUNNING_PARTS -> {
                renderDieselGenerator(definition, model, context, poseStack, blockEntity);
                return true;
            }
            case FIREBOX_HEATER -> {
                renderFireboxHeater(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case HEAT_BOILER -> {
                renderHeatBoiler(definition, model, context, poseStack, blockEntity);
                return true;
            }
            case HEPHAESTUS_RUNNING_CORE -> {
                renderHephaestus(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case INTAKE_FAN -> {
                renderIntake(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case STIRLING_RUNNING_PARTS -> {
                renderStirling(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case SAWMILL_RUNNING_PARTS -> {
                renderSawmill(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case REFINERY_DAMAGE_STATE -> {
                renderRefinery(definition, model, context, poseStack, blockEntity);
                return true;
            }
            case BLAST_FURNACE_TILTED_STATE -> {
                renderBlastFurnace(definition, model, context, poseStack, blockEntity);
                return true;
            }
            case GAS_FLARE_TILTED_STATE -> {
                renderGasFlare(definition, model, context, poseStack, blockEntity);
                return true;
            }
            case PUMP_RUNNING_PARTS -> {
                renderWaterPump(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static void renderRtg(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        model.renderPart("Gen", definition.textureLocation(), context);
        if (!(blockEntity instanceof HbmEnergyConnector connector) || blockEntity.getLevel() == null) {
            return;
        }
        renderRtgConnector(definition, model, context, poseStack, blockEntity, connector, Direction.EAST, 0.0F);
        renderRtgConnector(definition, model, context, poseStack, blockEntity, connector, Direction.WEST, 180.0F);
        renderRtgConnector(definition, model, context, poseStack, blockEntity, connector, Direction.NORTH, 90.0F);
        renderRtgConnector(definition, model, context, poseStack, blockEntity, connector, Direction.SOUTH, -90.0F);
    }

    private static void renderRtgConnector(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, HbmEnergyConnector connector,
            Direction direction, float yawDegrees) {
        if (!HbmEnergyConnectionUtil.canConnect(blockEntity.getLevel(), blockEntity.getBlockPos(),
                connector, direction)) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yawDegrees));
        model.renderPart("Connector", definition.textureLocation(), context);
        poseStack.popPose();
    }

    private static ResourceLocation cyclotronPlugTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/" + name + ".png");
    }

    private static LegacyMachineDefinition visibleDefinition(BlockState state) {
        if (state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block) {
            return block.definition();
        }
        if (state.getBlock() instanceof LegacyVisibleMachineBlock block) {
            return block.definition();
        }
        return null;
    }

    private static void renderCyclotron(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, BlockEntity blockEntity) {
        renderCyclotronPart(model, "Body", definition.textureLocation(), context);
        boolean plugged = true;
        for (int index = 0; index < 4; index++) {
            boolean filled = blockEntity instanceof CyclotronBlockEntity cyclotron && cyclotron.getPlug(index);
            plugged &= filled;
            ResourceLocation texture = filled ? CYCLOTRON_FILLED_TEXTURES[index] : CYCLOTRON_EMPTY_TEXTURES[index];
            renderCyclotronPart(model, "B" + (index + 1), texture, context);
        }
        if (plugged) {
            renderCyclotronGlyphRing(context.poseStack(), context.buffer());
        }
    }

    static void renderCyclotronItemParts(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context) {
        renderCyclotronPart(model, "Body", definition.textureLocation(), context);
        for (int index = 0; index < 4; index++) {
            renderCyclotronPart(model, "B" + (index + 1), CYCLOTRON_EMPTY_TEXTURES[index], context);
        }
    }

    private static void renderCyclotronPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.CYCLOTRON) ? cyclotronHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.CYCLOTRON.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle cyclotronHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Body" -> CYCLOTRON_BODY;
            case "B1" -> CYCLOTRON_PLUG_1;
            case "B2" -> CYCLOTRON_PLUG_2;
            case "B3" -> CYCLOTRON_PLUG_3;
            case "B4" -> CYCLOTRON_PLUG_4;
            default -> null;
        };
    }

    private static void renderCyclotronGlyphRing(PoseStack poseStack, MultiBufferSource buffer) {
        Font font = Minecraft.getInstance().font;
        double spin = System.currentTimeMillis() * 0.025D % 360.0D;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) spin));
        poseStack.translate(0.0D, 2.0D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        float rotation = 0.0F;
        for (int index = 0; index < CYCLOTRON_GLYPH_MESSAGE.length(); index++) {
            String glyph = CYCLOTRON_GLYPH_MESSAGE.substring(index, index + 1);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            rotation -= font.width(glyph) * 2.0F;
            poseStack.translate(2.75D, 0.0D, 0.0D);
            poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
            poseStack.scale(0.1F, 0.1F, 0.1F);
            font.drawInBatch(Component.literal(glyph)
                            .withStyle(style -> style.withFont(STANDARD_GALACTIC_FONT))
                            .getVisualOrderText(),
                    0.0F, 0.0F, CYCLOTRON_GLYPH_COLOR, false, poseStack.last().pose(), buffer,
                    Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void renderStrandCaster(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        model.renderPart("caster", definition.textureLocation(), context);
        if (!(blockEntity instanceof StrandCasterBlockEntity caster)
                || caster.getInstalledMold() == null
                || caster.getMoltenAmount() <= 0) {
            return;
        }
        LegacyTileRenderPlans.StrandCasterPlan plan = LegacyTileRenderPlans.strandCasterPlan(
                caster.getMoltenAmount(), caster.getCapacity(), caster.getInstalledMold().cost(),
                caster.getMoltenColor());
        if (!plan.active()) {
            return;
        }
        LegacyTileRenderPlans.StrandCasterPlatePlan plate = plan.plate();
        if (plate != null) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, plate.translateZ());
            LegacyTileRenderPlans.RgbaPlan color = plate.color();
            LegacyTileRenderPlans.ClipPlanePlan clip = plate.clipPlane();
            double clipD = clip.d() + clip.z() * plate.translateZ();
            model.renderPartClipped(plate.partName(), definition.textureLocation(),
                    context.withRgba(color.redByte(), color.greenByte(), color.blueByte(), color.alphaByte()),
                    clip.x(), clip.y(), clip.z(), clipD);
            poseStack.popPose();
        }
        LegacyTileRenderPlans.StrandCasterLavaPlan lava = plan.lava();
        if (lava != null && lava.quad() != null) {
            renderNormalTexturedQuad(STRAND_CASTER_LAVA_TEXTURE, lava.quad(),
                    context.fullBright().withRgba(lava.color().redByte(), lava.color().greenByte(),
                            lava.color().blueByte(), lava.color().alphaByte()));
        }
    }

    private static void renderRotaryFurnace(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        renderRotaryFurnacePart(model, "Furnace", definition.textureLocation(), context);
        float offset = blockEntity instanceof RotaryFurnaceBlockEntity furnace
                ? furnace.getPistonOffset(partialTick)
                : 0.0F;
        renderRotaryFurnaceTranslatedPart(model, new LegacyTileRenderPlans.TranslatedModelPartPlan(
                "rotary_furnace_piston", "Piston", true, 0.0D, offset, 0.0D),
                definition.textureLocation(), context, poseStack);
    }

    private static void renderRotaryFurnaceTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        if (!part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        renderRotaryFurnacePart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderRotaryFurnacePart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.ROTARY_FURNACE) ? rotaryFurnaceHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.ROTARY_FURNACE.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle rotaryFurnaceHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Furnace" -> ROTARY_FURNACE_BODY;
            case "Piston" -> ROTARY_FURNACE_PISTON;
            default -> null;
        };
    }

    private static void renderCrucible(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, BlockEntity blockEntity) {
        model.renderAll(definition.textureLocation(), context.poseStack(), context.buffer(),
                context.packedLight(), context.packedOverlay());
        if (!(blockEntity instanceof CrucibleBlockEntity crucible) || crucible.getTotalMaterialAmount() <= 0) {
            return;
        }
        double y = 0.5D + crucible.getMoltenLevel();
        LegacyTexturedQuadRenderer.quad(CRUCIBLE_LAVA_TEXTURE, context.fullBright(), 0.0F, 1.0F, 0.0F,
                LegacyTexturedQuadRenderer.vertex(-1.0D, y, -1.0D, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.vertex(-1.0D, y, 1.0D, 0.0D, 1.0D),
                LegacyTexturedQuadRenderer.vertex(1.0D, y, 1.0D, 1.0D, 1.0D),
                LegacyTexturedQuadRenderer.vertex(1.0D, y, -1.0D, 1.0D, 0.0D));
    }

    private static void renderCompressor(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        float lift = blockEntity instanceof CompressorBlockEntity compressor ? compressor.getPiston(partialTick) : 0.0F;
        float fan = blockEntity instanceof CompressorBlockEntity compressor ? compressor.getFanSpin(partialTick) : 0.0F;
        renderCompressorPlan(definition, model, LegacyTileRenderPlans.compressorPlan(lift, fan), context, poseStack);
    }

    private static void renderCompressorCompact(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        float fan = blockEntity instanceof CompressorBlockEntity compressor ? compressor.getFanSpin(partialTick) : 0.0F;
        renderCompressorPlan(definition, model, LegacyTileRenderPlans.compressorCompactPlan(fan), context, poseStack);
    }

    private static void renderPoweredCondenser(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        float fan = blockEntity instanceof PoweredCondenserBlockEntity condenser
                ? condenser.getFanSpin(partialTick)
                : 0.0F;
        renderCompressorPlan(definition, model, LegacyTileRenderPlans.compressorCompactPlan(fan), context, poseStack);
    }

    static void renderCompressorPlan(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            LegacyTileRenderPlans.CompressorPlan plan, ObjRenderContext context, PoseStack poseStack) {
        renderCompressorPart(model, plan.bodyPartName(), definition.textureLocation(), context);
        LegacyTileRenderPlans.TranslatedModelPartPlan pump = plan.pump();
        if (pump != null && pump.active()) {
            poseStack.pushPose();
            poseStack.translate(pump.translateX(), pump.translateY(), pump.translateZ());
            renderCompressorPart(model, pump.partName(), definition.textureLocation(), context);
            poseStack.popPose();
        }
        for (LegacyTileRenderPlans.RotatingModelPartPlan fan : plan.fans()) {
            renderCompressorRotatingPart(model, fan, definition.textureLocation(), context, poseStack);
        }
    }

    private static void renderCompressorRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderCompressorPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    static void renderCompressorPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        if (sameModel(model, ObjMachineModels.COMPRESSOR)) {
            LegacyWavefrontModel.SelectionHandle handle = compressorHandle(partName);
            if (handle != null) {
                ObjMachineModels.COMPRESSOR.renderOnlyInCallOrder(texture, context, handle);
                return;
            }
        }
        if (sameModel(model, ObjMachineModels.CONDENSER)) {
            LegacyWavefrontModel.SelectionHandle handle = condenserHandle(partName);
            if (handle != null) {
                ObjMachineModels.CONDENSER.renderOnlyInCallOrder(texture, context, handle);
                return;
            }
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle compressorHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Compressor" -> COMPRESSOR_BODY;
            case "Pump" -> COMPRESSOR_PUMP;
            case "Fan" -> COMPRESSOR_FAN;
            default -> null;
        };
    }

    private static LegacyWavefrontModel.SelectionHandle condenserHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Condenser" -> CONDENSER_BODY;
            case "Fan1" -> CONDENSER_FAN_1;
            case "Fan2" -> CONDENSER_FAN_2;
            default -> null;
        };
    }

    private static void renderAnnihilatorRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderAnnihilatorPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    static void renderAnnihilatorPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.ANNIHILATOR) ? annihilatorHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.ANNIHILATOR.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle annihilatorHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Annihilator" -> ANNIHILATOR_BODY;
            case "Roller" -> ANNIHILATOR_ROLLER;
            case "Belt" -> ANNIHILATOR_BELT;
            default -> null;
        };
    }

    private static void renderWaterPump(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        double rotor = blockEntity instanceof WaterPumpBlockEntity pump ? pump.getRotor(partialTick) : 0.0D;
        renderPumpPlan(definition, model, LegacyTileRenderPlans.pumpPlan(rotor), context, poseStack);
    }

    private static void renderRadGen(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        boolean on = blockEntity instanceof RadGenBlockEntity radGen && radGen.isOn();
        model.renderPart("Base", definition.textureLocation(), context);
        double rotorAngle = on ? (System.currentTimeMillis() % 3600L) * -0.1D : 0.0D;
        renderRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "radgen_rotor", "Rotor", 0.0D, 1.5D, 0.0D, 1.0F, 0.0F, 0.0F,
                rotorAngle), context, poseStack);
        model.renderPart("Glass", definition.textureLocation(), context);
        renderPlannedPart(model, definition.textureLocation(), LegacyTileRenderPlans.radgenLightPlan(on), context);
        renderPlannedPart(model, definition.textureLocation(), LegacyTileRenderPlans.radgenGlassPlan(), context);
    }

    private static void renderBatteryRedd(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        BatteryReddBlockEntity battery = blockEntity instanceof BatteryReddBlockEntity redd ? redd : null;
        float rotation = battery != null ? battery.getInterpolatedRotation(partialTick) : 0.0F;
        float speed = battery != null ? battery.getSpeed() : 0.0F;
        renderBatteryReddPart(model, "Base", definition.textureLocation(), context);
        poseStack.pushPose();
        poseStack.translate(0.0D, 5.5D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.translate(0.0D, -5.5D, 0.0D);
        renderBatteryReddPart(model, "Wheel", definition.textureLocation(), context);
        renderPreparedPlannedPart(model, definition.textureLocation(),
                LegacyTileRenderPlans.batteryReddStaticPlan().parts().get(2), context);
        poseStack.pushPose();
        poseStack.translate(0.0D, 5.5D, 0.0D);
        renderBatteryReddTrail(LegacyTileRenderPlans.batteryReddWheelTrailPlan(speed), poseStack, context);
        poseStack.popPose();
        renderBatteryReddPlasma(definition, model, context, blockEntity, speed);
        poseStack.popPose();

        Level level = blockEntity.getLevel();
        if (level != null) {
            renderBatteryReddZaps(LegacyTileRenderPlans.batteryReddZapPlan(speed > 0.0F,
                    level.getGameTime(), System.currentTimeMillis()), poseStack, context);
        }
    }

    private static void renderBatteryReddTrail(LegacyTileRenderPlans.BatteryReddTrailPlan plan,
            PoseStack poseStack, ObjRenderContext context) {
        if (!plan.active()) {
            return;
        }
        VertexConsumer consumer = LegacyUntexturedQuadRenderer.lightning(context.buffer());
        PoseStack.Pose pose = poseStack.last();
        for (LegacyTileRenderPlans.UntexturedQuadPlan quad : plan.quads()) {
            for (LegacyTileRenderPlans.UntexturedVertexPlan vertex : quad.vertices()) {
                LegacyTileRenderPlans.RgbaPlan color = vertex.color();
                LegacyUntexturedQuadRenderer.vertexRgbaF(consumer, pose,
                        vertex.x(), vertex.y(), vertex.z(),
                        color.red(), color.green(), color.blue(), color.alpha());
            }
        }
    }

    private static void renderBatteryReddPlasma(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, BlockEntity blockEntity, float speed) {
        boolean extraLayers = isPlayerWithin(blockEntity, 100.0D);
        LegacyTileRenderPlans.BatteryReddPlasmaPlan plan =
                LegacyTileRenderPlans.batteryReddPlasmaPlan(System.currentTimeMillis(), speed, extraLayers);
        if (!plan.active()) {
            return;
        }
        for (LegacyTileRenderPlans.TextureMatrixPartPlan layer : plan.layers()) {
            LegacyTileRenderPlans.RgbaPlan color = layer.color();
            ResourceLocation texture = "sparkle".equals(layer.role())
                    ? BATTERY_REDD_PLASMA_SPARKLE_TEXTURE
                    : BATTERY_REDD_PLASMA_TEXTURE;
            ObjRenderContext layerContext = context.withAdditiveTranslucency()
                    .withLegacyLightmap(plan.fullbright().lightmapX(), plan.fullbright().lightmapY())
                    .withColor(color.red(), color.green(), color.blue(), color.alpha())
                    .withTextureMatrixPlan(layer.textureMatrix());
            renderBatteryReddPart(model, layer.partName(), texture, layerContext);
        }
    }

    static void renderBatteryReddPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.BATTERY_REDD_LEGACY) ? batteryReddHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.BATTERY_REDD_LEGACY.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle batteryReddHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> BATTERY_REDD_BASE;
            case "Wheel" -> BATTERY_REDD_WHEEL;
            case "Lights" -> BATTERY_REDD_LIGHTS;
            case "Plasma" -> BATTERY_REDD_PLASMA;
            default -> null;
        };
    }

    private static boolean isPlayerWithin(BlockEntity blockEntity, double range) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }
        double dx = minecraft.player.getX() - (blockEntity.getBlockPos().getX() + 0.5D);
        double dy = minecraft.player.getY() - (blockEntity.getBlockPos().getY() + 2.5D);
        double dz = minecraft.player.getZ() - (blockEntity.getBlockPos().getZ() + 0.5D);
        return dx * dx + dy * dy + dz * dz < range * range;
    }

    private static void renderBatteryReddZaps(LegacyTileRenderPlans.BatteryReddZapPlan plan,
            PoseStack poseStack, ObjRenderContext context) {
        if (!plan.active()) {
            return;
        }
        for (LegacyTileRenderPlans.TranslatedBeamPlan beam : plan.beams()) {
            poseStack.pushPose();
            poseStack.translate(beam.translateX(), beam.translateY(), beam.translateZ());
            LegacyBeamRenderer.beam(poseStack, context.buffer(), beam.beam());
            poseStack.popPose();
        }
    }

    static void renderPumpPlan(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            LegacyTileRenderPlans.PumpPlan plan, ObjRenderContext context, PoseStack poseStack) {
        renderPumpPart(model, plan.basePartName(), definition.textureLocation(), context);
        renderPumpRotatingPart(model, plan.rotor(), definition.textureLocation(), context, poseStack);
        renderPumpPivotedPart(model, plan.arms(), definition.textureLocation(), context, poseStack);
        renderPumpTranslatedPart(model, plan.piston(), definition.textureLocation(), context, poseStack);
    }

    private static void renderPumpRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderPumpPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderPumpPivotedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.PivotedModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderPumpPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderPumpTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        if (!part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        renderPumpPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    static void renderPumpPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.PUMP) ? pumpHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.PUMP.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle pumpHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> PUMP_BASE;
            case "Rotor" -> PUMP_ROTOR;
            case "Arms" -> PUMP_ARMS;
            case "Piston" -> PUMP_PISTON;
            default -> null;
        };
    }

    private static void renderCombustionEngine(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        renderCombustionEnginePart(model, "Engine", definition.textureLocation(), context);
        ObjRenderContext canisterContext = context;
        if (blockEntity instanceof CombustionEngineBlockEntity engine) {
            int color = engine.getCanisterColor();
            canisterContext = context.withRgb(color >>> 16 & 255, color >>> 8 & 255, color & 255);
        }
        renderCombustionEnginePart(model, "Canister", definition.textureLocation(), canisterContext);
        float doorAngle = blockEntity instanceof CombustionEngineBlockEntity engine
                ? engine.getDoorAngle(partialTick)
                : 0.0F;
        renderCombustionEngineRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "combustion_engine_hatch", "Hatch", 1.0D, 0.0D, -2.6875D, 0.0F, -1.0F, 0.0F,
                doorAngle), definition.textureLocation(), context, poseStack);
    }

    private static void renderCombustionEngineRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderCombustionEnginePart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderCombustionEnginePart(LegacyWavefrontModel model, String partName,
            ResourceLocation texture, ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.COMBUSTION_ENGINE) ? combustionEngineHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.COMBUSTION_ENGINE.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle combustionEngineHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Engine" -> COMBUSTION_ENGINE_BODY;
            case "Canister" -> COMBUSTION_ENGINE_CANISTER;
            case "Hatch" -> COMBUSTION_ENGINE_HATCH;
            default -> null;
        };
    }

    private static void renderDieselGenerator(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        renderDieselGeneratorPart(model, "Generator", definition.textureLocation(), context);
        if (blockEntity instanceof DieselGeneratorBlockEntity diesel && diesel.isBurning()) {
            long currentMillis = System.currentTimeMillis();
            double swingSide = Math.sin(currentMillis / 50.0D) * 0.005D;
            double swingFront = Math.sin(currentMillis / 25.0D) * 0.005D;
            poseStack.pushPose();
            poseStack.translate(swingFront, 0.0D, swingSide);
            renderDieselGeneratorPart(model, "Engine", definition.textureLocation(), context);
            poseStack.popPose();
            return;
        }
        renderDieselGeneratorPart(model, "Engine", definition.textureLocation(), context);
    }

    static void renderDieselGeneratorPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.DIESELGEN_LEGACY) ? dieselGeneratorHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.DIESELGEN_LEGACY.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle dieselGeneratorHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Generator" -> DIESEL_GENERATOR;
            case "Engine" -> DIESEL_ENGINE;
            default -> null;
        };
    }

    private static void renderHeatBoiler(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        boolean exploded = blockEntity instanceof BoilerBlockEntity boiler && boiler.hasExploded();
        int steamFill = blockEntity instanceof BoilerBlockEntity boiler ? boiler.getSteamFill() : 0;
        int steamMaxFill = blockEntity instanceof BoilerBlockEntity boiler ? boiler.getSteamMaxFill() : 0;
        LegacyTileRenderPlans.BoilerPlan plan = LegacyTileRenderPlans.boilerPlan(exploded, steamFill,
                steamMaxFill, System.currentTimeMillis());
        poseStack.pushPose();
        if (plan.overpressure()) {
            poseStack.scale((float) plan.scaleX(), (float) plan.scaleY(), (float) plan.scaleZ());
        }
        LegacyWavefrontModel selected = exploded
                ? EXTRA_MODELS.computeIfAbsent(BOILER_BURST_MODEL,
                        key -> new LegacyWavefrontModel(key, definition.textureLocation()).asVBO())
                : model;
        selected.renderAll(definition.textureLocation(), poseStack, context.buffer(), context.packedLight(),
                context.packedOverlay());
        poseStack.popPose();
    }

    private static void renderHephaestus(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        renderHephaestusPart(model, "Main", definition.textureLocation(), context);
        float rotor = blockEntity instanceof HephaestusBlockEntity hephaestus ? hephaestus.getRotor(partialTick) : 0.0F;
        boolean active = blockEntity instanceof HephaestusBlockEntity hephaestus && hephaestus.isActive();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotor));
        for (int i = 0; i < 3; i++) {
            renderHephaestusPart(model, "Rotor", definition.textureLocation(), context);
            poseStack.mulPose(Axis.YP.rotationDegrees(120.0F));
        }
        poseStack.popPose();

        ObjRenderContext coreContext = active
                ? context.fullBright().withLegacyTextureMatrix(0.5F, 0.5F, 0.0F, rotor / 10.0F)
                : context.withColor(0x808080).withLegacyTextureMatrix(0.5F, 0.5F, 0.0F, rotor / 10.0F);
        renderHephaestusPart(model, "Core", active ? HEPHAESTUS_CORE_ACTIVE_TEXTURE : HEPHAESTUS_CORE_IDLE_TEXTURE,
                coreContext);
    }

    private static void renderHephaestusPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.HEPHAESTUS_LEGACY) ? hephaestusHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.HEPHAESTUS_LEGACY.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle hephaestusHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Main" -> HEPHAESTUS_MAIN;
            case "Rotor" -> HEPHAESTUS_ROTOR;
            case "Core" -> HEPHAESTUS_CORE;
            default -> null;
        };
    }

    private static void renderRefinery(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        boolean exploded = blockEntity instanceof RefineryBlockEntity refinery && refinery.isExploded();
        boolean tilted = blockEntity instanceof RefineryBlockEntity refinery && refinery.isTilted();
        LegacyTileRenderPlans.RefineryDamagePlan plan =
                LegacyTileRenderPlans.refineryDamagePlan(exploded, tilted);
        LegacyWavefrontModel selected = plan.exploded()
                ? EXTRA_MODELS.computeIfAbsent(REFINERY_EXPLODED_MODEL,
                        key -> new LegacyWavefrontModel(key, definition.textureLocation()).asVBO())
                : model;

        poseStack.pushPose();
        if (plan.tilted()) {
            poseStack.translate(0.0D, plan.translateY(), 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) plan.rotationZDegrees()));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) plan.rotationYDegrees()));
        }
        selected.renderAll(definition.textureLocation(), poseStack, context.buffer(), context.packedLight(),
                context.packedOverlay());
        poseStack.popPose();
    }

    private static void renderGasFlare(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        boolean tilted = blockEntity instanceof HbmLegacyLoadedTile loadedTile && loadedTile.isTilted();
        LegacyTileRenderPlans.GasFlareTiltPlan plan = LegacyTileRenderPlans.gasFlareTiltPlan(tilted);
        poseStack.pushPose();
        if (plan.tilted()) {
            poseStack.translate(0.0D, plan.translateY(), 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) plan.rotationZDegrees()));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) plan.rotationYDegrees()));
        }
        model.renderAll(definition.textureLocation(), poseStack, context.buffer(), context.packedLight(),
                context.packedOverlay());
        poseStack.popPose();
    }

    private static void renderBlastFurnace(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        boolean tilted = blockEntity instanceof HbmLegacyLoadedTile loadedTile && loadedTile.isTilted();
        LegacyTileRenderPlans.BlastFurnaceTiltPlan plan = LegacyTileRenderPlans.blastFurnaceTiltPlan(tilted);
        poseStack.pushPose();
        if (plan.tilted()) {
            poseStack.translate(0.0D, plan.translateY(), 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) plan.rotationZDegrees()));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) plan.rotationYDegrees()));
        }
        model.renderAll(definition.textureLocation(), poseStack, context.buffer(), context.packedLight(),
                context.packedOverlay());
        poseStack.popPose();
    }

    private static void renderIntake(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        float fan = blockEntity instanceof IntakeBlockEntity intake ? intake.getFanSpin(partialTick) : 0.0F;
        renderIntakePart(model, "Base", definition.textureLocation(), context);
        renderIntakeRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "intake_fan", "Fan", 0.0D, 0.0D, 0.0D, 0.0F, -1.0F, 0.0F,
                fan), definition.textureLocation(), context, poseStack);
    }

    private static void renderIntakeRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderIntakePart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderIntakePart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.INTAKE_LEGACY) ? intakeHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.INTAKE_LEGACY.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle intakeHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> INTAKE_BASE;
            case "Fan" -> INTAKE_FAN;
            default -> null;
        };
    }

    private static void renderCrystallizer(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        boolean on = blockEntity instanceof ProcessingMachineBlockEntity processing && processing.isOn();
        float angle = blockEntity instanceof ProcessingMachineBlockEntity processing
                ? processing.getAngle(partialTick)
                : 0.0F;
        renderCrystallizerPart(model, "Body", definition.textureLocation(), context);
        renderCrystallizerRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "crystallizer_spinner", "Spinner", 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, 0.0F,
                angle), definition.textureLocation(), context, poseStack);
        ResourceLocation fluidTexture = blockEntity instanceof ProcessingMachineBlockEntity processing
                ? processing.getCrystallizerTank().getTankType().getTexture()
                : definition.textureLocation();
        renderPlannedPart(model, fluidTexture, LegacyTileRenderPlans.crystallizerFluidPlan(on), context);
    }

    private static void renderCrystallizerRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderCrystallizerPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderCrystallizerPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.ACIDIZER) ? crystallizerHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.ACIDIZER.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle crystallizerHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Body" -> CRYSTALLIZER_BODY;
            case "Spinner" -> CRYSTALLIZER_SPINNER;
            case "Fluid" -> CRYSTALLIZER_FLUID;
            default -> null;
        };
    }

    private static void renderAshpit(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        float door = blockEntity instanceof AshpitBlockEntity ashpit ? ashpit.getDoorAngle(partialTick) : 0.0F;
        boolean full = blockEntity instanceof AshpitBlockEntity ashpit && ashpit.isFull();
        model.renderPart("Main", definition.textureLocation(), context);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, door * 0.75D / 135.0D);
        model.renderPart("Door", definition.textureLocation(), context);
        poseStack.popPose();
        model.renderPart(full ? "InnerBurning" : "Inner", definition.textureLocation(), context);
    }

    private static void renderArcWelder(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        model.renderAll(definition.textureLocation(), context);
        if (!(blockEntity instanceof ArcWelderBlockEntity welder)) {
            return;
        }
        ItemStack display = welder.getDisplayOutput();
        if (display.isEmpty()) {
            return;
        }
        ItemStack stack = display.copy();
        stack.setCount(1);
        poseStack.pushPose();
        poseStack.translate(0.0625D * 2.5D, 1.125D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.scale(1.5F, 1.5F, 1.5F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED,
                context.packedLight(), OverlayTexture.NO_OVERLAY, poseStack, context.buffer(),
                blockEntity.getLevel(), 0);
        poseStack.popPose();
    }

    private static void renderAmmoPress(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        float press = blockEntity instanceof AmmoPressBlockEntity ammoPress ? ammoPress.getPress(partialTick) : 0.0F;
        float lift = blockEntity instanceof AmmoPressBlockEntity ammoPress ? ammoPress.getLift(partialTick) : 0.0F;
        boolean bullets = blockEntity instanceof AmmoPressBlockEntity ammoPress && ammoPress.shouldRenderBullets();

        renderAmmoPressPart(model, "Frame", definition.textureLocation(), context);
        renderAmmoPressTranslatedPart(model, new LegacyTileRenderPlans.TranslatedModelPartPlan(
                "ammo_press_press", "Press", true,
                0.0D, -press * 0.25D, 0.0D), definition.textureLocation(), context, poseStack);
        renderAmmoPressTranslatedPart(model, new LegacyTileRenderPlans.TranslatedModelPartPlan(
                "ammo_press_shells", "Shells", true,
                0.0D, lift * 0.5D - 0.5D, 0.0D), definition.textureLocation(), context, poseStack);
        renderAmmoPressTranslatedPart(model, new LegacyTileRenderPlans.TranslatedModelPartPlan(
                "ammo_press_bullets", "Bullets", bullets,
                0.0D, lift * 0.5D - 0.5D, 0.0D), definition.textureLocation(), context, poseStack);
    }

    private static void renderAmmoPressTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        if (!part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        renderAmmoPressPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderAmmoPressPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.AMMO_PRESS) ? ammoPressHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.AMMO_PRESS.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle ammoPressHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Frame" -> AMMO_PRESS_FRAME;
            case "Press" -> AMMO_PRESS_PRESS;
            case "Shells" -> AMMO_PRESS_SHELLS;
            case "Bullets" -> AMMO_PRESS_BULLETS;
            default -> null;
        };
    }

    private static void renderMixer(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        renderMixerPart(model, "Main", definition.textureLocation(), context);
        float rotation = blockEntity instanceof MixerBlockEntity mixer ? mixer.getRotation(partialTick) : 0.0F;
        renderMixerRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "mixer_blade", "Mixer", 0.0D, 0.0D, 0.0D, 0.0F, -1.0F, 0.0F,
                rotation), definition.textureLocation(), context, poseStack);

        if (!(blockEntity instanceof MixerBlockEntity mixer) || mixer.getTotalFluidFill() <= 0
                || mixer.getTotalFluidCapacity() <= 0) {
            return;
        }
        int color = mixer.getOutputTank().getTankType().getColor();
        double scale = Math.min(0.99D, mixer.getTotalFluidFill() / (double) mixer.getTotalFluidCapacity() * 0.99D);
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        poseStack.scale(1.0F, (float) scale, 1.0F);
        poseStack.translate(0.0D, -1.0D, 0.0D);
        renderMixerPartUntextured(model, "Fluid", context.withRgba(color >>> 16 & 255, color >>> 8 & 255,
                color & 255, 191).withTranslucencyNoDepthWrite());
        poseStack.popPose();
    }

    private static void renderMixerRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderMixerPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderMixerPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.MIXER) ? mixerHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.MIXER.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static void renderMixerPartUntextured(LegacyWavefrontModel model, String partName,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.MIXER) ? mixerHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.MIXER.renderOnlyUntextured(context, handle);
            return;
        }
        model.renderPartUntextured(partName, context);
    }

    private static LegacyWavefrontModel.SelectionHandle mixerHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Main" -> MIXER_MAIN;
            case "Mixer" -> MIXER_BLADE;
            case "Fluid" -> MIXER_FLUID;
            default -> null;
        };
    }

    private static void renderCombinationOven(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        model.renderAll(definition.textureLocation(), poseStack, context.buffer(), context.packedLight(),
                context.packedOverlay());
        if (!(blockEntity instanceof CombinationOvenBlockEntity oven) || !oven.wasOn()) {
            return;
        }

        int texIndex = (int) (blockEntity.getLevel().getGameTime() / 2L % 14L);
        float frameWidth = 1.0F / 14.0F;
        float uMin = (texIndex % 5) * frameWidth;
        float uMax = uMin + frameWidth;
        float yRot = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.75D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        ObjRenderContext fireContext = context.fullBright().withAdditiveTranslucency();
        LegacyTexturedQuadRenderer.quad(COMBINATION_FIRE_TEXTURE, fireContext,
                LegacyTexturedQuadRenderer.vertex(-1.0D, 0.0D, 0.0D, uMax, 1.0D),
                LegacyTexturedQuadRenderer.vertex(-1.0D, 3.0D, 0.0D, uMax, 0.0D),
                LegacyTexturedQuadRenderer.vertex(1.0D, 3.0D, 0.0D, uMin, 0.0D),
                LegacyTexturedQuadRenderer.vertex(1.0D, 0.0D, 0.0D, uMin, 1.0D));
        poseStack.popPose();
    }

    private static void renderFurnaceIron(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, BlockEntity blockEntity) {
        model.renderPart("Main", definition.textureLocation(), context);
        if (blockEntity instanceof LegacyFurnaceBlockEntity furnace && furnace.wasOn()) {
            model.renderPart("On", definition.textureLocation(), context.fullBright());
            return;
        }
        model.renderPart("Off", definition.textureLocation(), context);
    }

    private static void renderFurnaceSteel(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        model.renderAll(definition.textureLocation(), context);
        if (!(blockEntity instanceof LegacyFurnaceBlockEntity furnace) || !furnace.wasOn()) {
            return;
        }

        float col = (float) Math.sin(System.currentTimeMillis() * 0.001D);
        int red = Math.round((0.875F + col * 0.125F) * 255.0F);
        int green = Math.round((0.625F + col * 0.375F) * 255.0F);
        ObjRenderContext fireContext = context.fullBright().withAdditiveTranslucency().withRgba(red, green, 0, 128);
        for (int i = 0; i < 4; i++) {
            double x = 1.0D + i * 0.0625D;
            LegacyUntexturedQuadRenderer.quad(fireContext,
                    x, 1.0D, -1.0D,
                    x, 1.0D, 1.0D,
                    x, 0.5D, 1.0D,
                    x, 0.5D, -1.0D,
                    0xFFFFFF, 255, 255, 255, 255);
        }
    }

    private static void renderStirling(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        float rot = blockEntity instanceof StirlingBlockEntity stirling ? stirling.getSpin(partialTick) : 0.0F;
        boolean hasCog = !(blockEntity instanceof StirlingBlockEntity stirling) || stirling.hasCog();

        renderStirlingPart(model, "Base", definition.textureLocation(), context);
        if (hasCog) {
            renderStirlingRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                    "stirling_cog", "Cog", 0.0D, 1.375D, 0.0D, 0.0F, 0.0F, -1.0F,
                    rot), definition.textureLocation(), context, poseStack);
        }
        renderStirlingRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "stirling_cog_small", "CogSmall", 0.0D, 1.375D, 0.25D, 1.0F, 0.0F, 0.0F,
                rot * 2.0F + 3.0F), definition.textureLocation(), context, poseStack);
        renderStirlingTranslatedPart(model, new LegacyTileRenderPlans.TranslatedModelPartPlan(
                "stirling_piston", "Piston", true,
                Math.sin(rot * Math.PI / 90.0D) * 0.25D + 0.125D, 0.0D, 0.0D),
                definition.textureLocation(), context, poseStack);
    }

    private static void renderSawmill(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        float rot = blockEntity instanceof SawmillBlockEntity sawmill ? sawmill.getSpin(partialTick) : 0.0F;
        boolean hasBlade = !(blockEntity instanceof SawmillBlockEntity sawmill) || sawmill.hasBlade();

        renderSawmillPart(model, "Main", definition.textureLocation(), context);
        if (hasBlade) {
            renderSawmillRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                    "sawmill_blade", "Blade", 0.0D, 1.375D, 0.0D, 0.0F, 0.0F, 1.0F,
                    -rot * 2.0F), definition.textureLocation(), context, poseStack);
        }
        renderSawmillRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "sawmill_gear_left", "GearLeft", 0.5625D, 1.375D, 0.0D, 0.0F, 0.0F, 1.0F,
                rot), definition.textureLocation(), context, poseStack);
        renderSawmillRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "sawmill_gear_right", "GearRight", -0.5625D, 1.375D, 0.0D, 0.0F, 0.0F, 1.0F,
                -rot), definition.textureLocation(), context, poseStack);
    }

    private static void renderSawmillRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderSawmillPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderStirlingRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderStirlingPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderStirlingTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        if (!part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        renderStirlingPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderSawmillPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.SAWMILL) ? sawmillHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.SAWMILL.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle sawmillHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Main" -> SAWMILL_MAIN;
            case "Blade" -> SAWMILL_BLADE;
            case "GearLeft" -> SAWMILL_GEAR_LEFT;
            case "GearRight" -> SAWMILL_GEAR_RIGHT;
            default -> null;
        };
    }

    private static void renderStirlingPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.STIRLING) ? stirlingHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.STIRLING.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle stirlingHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> STIRLING_BASE;
            case "Cog" -> STIRLING_COG;
            case "CogSmall" -> STIRLING_COG_SMALL;
            case "Piston" -> STIRLING_PISTON;
            default -> null;
        };
    }

    private static void renderPrecass(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        boolean frame = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                && machine.shouldRenderFrame();
        double ring = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                ? machine.getPrecassRing(partialTick)
                : 0.0D;
        double[] arm = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                ? machine.getPrecassArm(partialTick)
                : new double[] { 45.0D, -30.0D, 45.0D };

        AssemblyMachineRenderer.renderModelPart(model, "Base", definition.textureLocation(), context);
        if (frame) {
            AssemblyMachineRenderer.renderModelPart(model, "Frame", definition.textureLocation(), context);
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) ring));
        AssemblyMachineRenderer.renderModelPart(model, "Ring", definition.textureLocation(), context);
        AssemblyMachineRenderer.renderModelPart(model, "Ring2", definition.textureLocation(), context);
        for (int i = 0; i < 4; i++) {
            double striker = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                    ? machine.getPrecassStriker(i, partialTick)
                    : 0.0D;
            renderPrecassArm(definition, model, context, poseStack, arm, striker);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        }
        poseStack.popPose();

        if (blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                && LegacyRecipeIconRenderer.shouldRender(machine)) {
            LegacyRecipeIconRenderer.renderInLegacyMachineSpace(machine.getSelectedRecipeDefinition(),
                    machine.getLevel(), poseStack, context.buffer(), context.packedLight());
        }
    }

    static void renderPrecassArm(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, double[] arm, double striker) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.625D, 0.9375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) arm[0]));
        poseStack.translate(0.0D, -1.625D, -0.9375D);
        AssemblyMachineRenderer.renderModelPart(model, "ArmLower1", definition.textureLocation(), context);

        poseStack.translate(0.0D, 2.375D, 0.9375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) arm[1]));
        poseStack.translate(0.0D, -2.375D, -0.9375D);
        AssemblyMachineRenderer.renderModelPart(model, "ArmUpper1", definition.textureLocation(), context);

        poseStack.translate(0.0D, 2.375D, 0.4375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) arm[2]));
        poseStack.translate(0.0D, -2.375D, -0.4375D);
        AssemblyMachineRenderer.renderModelPart(model, "Head1", definition.textureLocation(), context);
        poseStack.translate(0.0D, striker, 0.0D);
        AssemblyMachineRenderer.renderModelPart(model, "Spike1", definition.textureLocation(), context);
        poseStack.popPose();
    }

    private static void renderPurex(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        boolean frame = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                && machine.shouldRenderFrame();
        float anim = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                ? machine.getPurexAnim(partialTick)
                : 0.0F;

        renderPurexPart(model, "Base", definition.textureLocation(), context);
        if (frame) {
            renderPurexPart(model, "Frame", definition.textureLocation(), context);
        }
        renderPurexRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "purex_fan", "Fan", 1.5D, 1.25D, 0.0D, 0.0F, 0.0F, 1.0F,
                anim * 45.0F), definition.textureLocation(), context, poseStack);
        renderPurexTranslatedPart(model, new LegacyTileRenderPlans.TranslatedModelPartPlan(
                "purex_pump", "Pump", true, BobMathUtil.sps(anim * 0.25D) * 0.5D, 0.0D, 0.0D),
                definition.textureLocation(), context, poseStack);
    }

    private static void renderPurexRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderPurexPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderPurexTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, ResourceLocation texture,
            ObjRenderContext context, PoseStack poseStack) {
        if (!part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        renderPurexPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    static void renderPurexPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.PUREX) ? purexHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.PUREX.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle purexHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> PUREX_BASE;
            case "Frame" -> PUREX_FRAME;
            case "Fan" -> PUREX_FAN;
            case "Pump" -> PUREX_PUMP;
            default -> null;
        };
    }

    private static void renderFireboxHeater(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        FireboxHeaterBlockEntity.Kind kind = blockEntity instanceof FireboxHeaterBlockEntity firebox
                ? firebox.kind()
                : FireboxHeaterBlockEntity.Kind.FIREBOX;
        float door = blockEntity instanceof FireboxHeaterBlockEntity firebox
                ? firebox.getDoorAngle(partialTick)
                : 0.0F;
        boolean burning = blockEntity instanceof FireboxHeaterBlockEntity firebox && firebox.wasOn();

        renderFireboxHeaterPart(model, "Main", definition.textureLocation(), partContext(definition, "Main", context));
        if (kind == FireboxHeaterBlockEntity.Kind.OVEN) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, door * 0.75D / 135.0D);
            renderFireboxHeaterPart(model, "Door", definition.textureLocation(),
                    partContext(definition, "Door", context));
            poseStack.popPose();
            String innerPart = burning ? "InnerBurning" : "Inner";
            ObjRenderContext innerContext = partContext(definition, innerPart, context);
            renderFireboxHeaterPart(model, innerPart, definition.textureLocation(),
                    burning && innerContext == context ? innerContext.fullBright() : innerContext);
            return;
        }

        poseStack.pushPose();
        poseStack.translate(1.375D, 0.0D, 0.375D);
        poseStack.mulPose(Axis.YN.rotationDegrees(door));
        poseStack.translate(-1.375D, 0.0D, -0.375D);
        renderFireboxHeaterPart(model, "Door", definition.textureLocation(),
                partContext(definition, "Door", context));
        poseStack.popPose();
        String innerPart = burning ? "InnerBurning" : "InnerEmpty";
        ObjRenderContext innerContext = partContext(definition, innerPart, context);
        renderFireboxHeaterPart(model, innerPart, definition.textureLocation(),
                burning && innerContext == context ? innerContext.fullBright() : innerContext);
    }

    private static void renderFireboxHeaterPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        if (sameModel(model, ObjMachineModels.FIREBOX_LEGACY)) {
            LegacyWavefrontModel.SelectionHandle handle = fireboxHandle(partName);
            if (handle != null) {
                ObjMachineModels.FIREBOX_LEGACY.renderOnlyInCallOrder(texture, context, handle);
                return;
            }
        }
        if (sameModel(model, ObjMachineModels.HEATING_OVEN_LEGACY)) {
            LegacyWavefrontModel.SelectionHandle handle = heatingOvenHandle(partName);
            if (handle != null) {
                ObjMachineModels.HEATING_OVEN_LEGACY.renderOnlyInCallOrder(texture, context, handle);
                return;
            }
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle fireboxHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Main" -> FIREBOX_MAIN;
            case "Door" -> FIREBOX_DOOR;
            case "InnerEmpty" -> FIREBOX_INNER_EMPTY;
            case "InnerBurning" -> FIREBOX_INNER_BURNING;
            default -> null;
        };
    }

    private static LegacyWavefrontModel.SelectionHandle heatingOvenHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Main" -> HEATING_OVEN_MAIN;
            case "Door" -> HEATING_OVEN_DOOR;
            case "Inner" -> HEATING_OVEN_INNER;
            case "InnerBurning" -> HEATING_OVEN_INNER_BURNING;
            default -> null;
        };
    }

    static void renderVisibleMachineStaticPlan(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            LegacyTileRenderPlans.VisibleMachineStaticPlan plan, ObjRenderContext context) {
        renderVisibleMachineStaticPlanPass(definition, model, plan, context, false);
        renderVisibleMachineStaticPlanPass(definition, model, plan, context, true);
    }

    private static void renderVisibleMachineStaticPlanPass(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            LegacyTileRenderPlans.VisibleMachineStaticPlan plan, ObjRenderContext context, boolean translucentPass) {
        for (LegacyTileRenderPlans.ModelPartTintPlan part : plan.parts()) {
            if (plannedPartTranslucent(part) != translucentPass) {
                continue;
            }
            renderPlannedPart(model, definition.textureLocation(), part, context);
        }
    }

    private static boolean plannedPartTranslucent(LegacyTileRenderPlans.ModelPartTintPlan plan) {
        return plan != null
                && plan.blend() != null
                && plan.blend().modernRenderMode().translucent();
    }

    private static void renderPlannedPart(LegacyWavefrontModel model, net.minecraft.resources.ResourceLocation texture,
            LegacyTileRenderPlans.ModelPartTintPlan plan, ObjRenderContext context) {
        renderPreparedPlannedPart(model, texture, plan, context);
    }

    private static void renderPreparedPlannedPart(LegacyWavefrontModel model,
            net.minecraft.resources.ResourceLocation texture, LegacyTileRenderPlans.ModelPartTintPlan plan,
            ObjRenderContext context) {
        if (!plan.active()) {
            return;
        }
        ObjRenderContext resolved = applyTintPlan(context, plan);
        if (tryRenderPreparedPlannedPart(model, texture, plan, resolved)) {
            return;
        }
        if (plan.textured()) {
            model.renderPart(plan.partName(), texture, resolved);
        } else {
            model.renderPartUntextured(plan.partName(), resolved);
        }
    }

    private static boolean tryRenderPreparedPlannedPart(LegacyWavefrontModel model, ResourceLocation texture,
            LegacyTileRenderPlans.ModelPartTintPlan plan, ObjRenderContext context) {
        if (plan.textured() && sameModel(model, ObjMachineModels.BATTERY_REDD_LEGACY)) {
            LegacyWavefrontModel.SelectionHandle handle = batteryReddHandle(plan.partName());
            if (handle != null) {
                ObjMachineModels.BATTERY_REDD_LEGACY.renderOnlyInCallOrder(texture, context, handle);
                return true;
            }
        }
        if (sameModel(model, ObjMachineModels.ACIDIZER)) {
            LegacyWavefrontModel.SelectionHandle handle = crystallizerHandle(plan.partName());
            if (handle != null) {
                if (plan.textured()) {
                    ObjMachineModels.ACIDIZER.renderOnlyInCallOrder(texture, context, handle);
                } else {
                    ObjMachineModels.ACIDIZER.renderOnlyUntextured(context, handle);
                }
                return true;
            }
        }
        return false;
    }

    private static ObjRenderContext applyTintPlan(ObjRenderContext context,
            LegacyTileRenderPlans.ModelPartTintPlan plan) {
        ObjRenderContext resolved = context;
        if (plan.blend() != null) {
            resolved = resolved.withRenderMode(plan.blend().modernRenderMode());
        }
        if (plan.color() != null) {
            resolved = resolved.withRgba(plan.color().redByte(), plan.color().greenByte(),
                    plan.color().blueByte(), plan.color().alphaByte());
        }
        if (plan.fullbright() != null) {
            resolved = resolved.withLegacyLightmap(plan.fullbright().lightmapX(), plan.fullbright().lightmapY());
        }
        return resolved;
    }

    private static void renderRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        model.renderPart(part.partName(), context);
        poseStack.popPose();
    }

    private static void renderPivotedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.PivotedModelPartPlan part, ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        model.renderPart(part.partName(), context);
        poseStack.popPose();
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, ObjRenderContext context, PoseStack poseStack) {
        if (!part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        model.renderPart(part.partName(), context);
        poseStack.popPose();
    }

    private static boolean sameModel(LegacyWavefrontModel model, LegacyWavefrontModel expected) {
        return model == expected || model.modelLocation().equals(expected.modelLocation());
    }

    private static void renderNormalTexturedQuad(ResourceLocation texture,
            LegacyTileRenderPlans.NormalTexturedQuadPlan quad, ObjRenderContext context) {
        if (quad.vertices().size() != 4) {
            return;
        }
        LegacyTileRenderPlans.NormalTexturedVertexPlan v0 = quad.vertices().get(0);
        LegacyTileRenderPlans.NormalTexturedVertexPlan v1 = quad.vertices().get(1);
        LegacyTileRenderPlans.NormalTexturedVertexPlan v2 = quad.vertices().get(2);
        LegacyTileRenderPlans.NormalTexturedVertexPlan v3 = quad.vertices().get(3);
        LegacyTexturedQuadRenderer.quad(texture, context, quad.normalX(), quad.normalY(), quad.normalZ(),
                LegacyTexturedQuadRenderer.vertex(v0.x(), v0.y(), v0.z(), v0.u(), v0.v()),
                LegacyTexturedQuadRenderer.vertex(v1.x(), v1.y(), v1.z(), v1.u(), v1.v()),
                LegacyTexturedQuadRenderer.vertex(v2.x(), v2.y(), v2.z(), v2.u(), v2.v()),
                LegacyTexturedQuadRenderer.vertex(v3.x(), v3.y(), v3.z(), v3.u(), v3.v()));
    }

    private static void rotate(PoseStack poseStack, float axisX, float axisY, float axisZ, double degrees) {
        if (axisX != 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) (degrees * axisX)));
        }
        if (axisY != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (degrees * axisY)));
        }
        if (axisZ != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (degrees * axisZ)));
        }
    }
}
