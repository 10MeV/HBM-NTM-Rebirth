package com.hbm.ntm.client.renderer;

import com.hbm.config.ClientConfig;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.audit.HeatBoilerRenderAuditEvidence;
import com.hbm.ntm.block.BlastFurnaceBlock;
import com.hbm.ntm.block.ChimneyBlock;
import com.hbm.ntm.block.CoolingTowerBlock;
import com.hbm.ntm.block.DeuteriumTowerBlock;
import com.hbm.ntm.block.DrainBlock;
import com.hbm.ntm.block.ElectricHeaterBlock;
import com.hbm.ntm.block.FelBlock;
import com.hbm.ntm.block.FractionSpacerBlock;
import com.hbm.ntm.block.GasCentBlock;
import com.hbm.ntm.block.GasFlareBlock;
import com.hbm.ntm.block.HeatBoilerBlock;
import com.hbm.ntm.block.HeaterHeatexBlock;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachinePartRenderProperties;
import com.hbm.ntm.block.LegacyMachineRenderProfile;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.LegacyVisibleMachineBlock;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.block.OilDrillBlock;
import com.hbm.ntm.block.OilburnerBlock;
import com.hbm.ntm.block.ProcessingMachineBlock;
import com.hbm.ntm.block.RadiolysisBlock;
import com.hbm.ntm.block.RemoteFluidMachineBlock;
import com.hbm.ntm.block.RefineryBlock;
import com.hbm.ntm.block.SatelliteDockBlock;
import com.hbm.ntm.block.SilexBlock;
import com.hbm.ntm.block.SolarBoilerBlock;
import com.hbm.ntm.block.WoodBurnerBlock;
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
import com.hbm.ntm.blockentity.RtgBlockEntity;
import com.hbm.ntm.blockentity.SawmillBlockEntity;
import com.hbm.ntm.blockentity.SolarBoilerBlockEntity;
import com.hbm.ntm.blockentity.StrandCasterBlockEntity;
import com.hbm.ntm.blockentity.StirlingBlockEntity;
import com.hbm.ntm.blockentity.WaterPumpBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.TexturedObjPartGroup;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.UntexturedQuadGroup;
import com.hbm.ntm.client.render.LegacyRenderRandom;
import com.hbm.ntm.client.render.culling.HbmRenderFrameCulling;
import com.hbm.ntm.client.render.culling.HbmRenderFrameCulling.MachineRenderRoute;
import com.hbm.ntm.client.render.shader.HbmShaderCompatibilityDetector;
import com.hbm.ntm.energy.HbmEnergyConnectionUtil;
import com.hbm.ntm.energy.HbmEnergyConnector;
import com.hbm.ntm.item.LaserWavelength;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.util.BobMathUtil;
import com.hbm.ntm.util.HbmModelRenderDistances;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraftforge.registries.ForgeRegistries;

public class LegacyVisibleMachineRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();
    private static final int[] NO_SOLAR_BEAM_OFFSETS = new int[0];
    private static final int RADGEN_LIGHT_ON_RED = 0;
    private static final int RADGEN_LIGHT_ON_GREEN = 255;
    private static final int RADGEN_LIGHT_ON_BLUE = 0;
    private static final int RADGEN_LIGHT_OFF_RED = 0;
    private static final int RADGEN_LIGHT_OFF_GREEN = 25;
    private static final int RADGEN_LIGHT_OFF_BLUE = 0;
    private static final int RADGEN_GLASS_RED = 127;
    private static final int RADGEN_GLASS_GREEN = 191;
    private static final int RADGEN_GLASS_BLUE = 255;
    private static final int RADGEN_GLASS_ALPHA = 76;
    private static final ResourceLocation WHITE_TEXTURE =
            new ResourceLocation("minecraft", "textures/misc/white.png");
    private static final double LEGACY_TILTED_TRANSLATE_Y = -0.25D;
    private static final float LEGACY_TILTED_ROTATION_Z = 10.0F;
    private static final float LEGACY_TILTED_ROTATION_Y = 5.0F;
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
    private static final LegacyWavefrontModel.SelectionHandle RTG_GEN =
            ObjMachineModels.RTG_LEGACY.prepareRenderOnlyInCallOrder("Gen");
    private static final LegacyWavefrontModel.SelectionHandle RTG_CONNECTOR =
            ObjMachineModels.RTG_LEGACY.prepareRenderOnlyInCallOrder("Connector");
    private static final LegacyWavefrontModel.SelectionHandle FURNACE_IRON_MAIN =
            ObjMachineModels.FURNACE_IRON_LEGACY.prepareRenderOnlyInCallOrder("Main");
    private static final LegacyWavefrontModel.SelectionHandle FURNACE_IRON_ON =
            ObjMachineModels.FURNACE_IRON_LEGACY.prepareRenderOnlyInCallOrder("On");
    private static final LegacyWavefrontModel.SelectionHandle FURNACE_IRON_OFF =
            ObjMachineModels.FURNACE_IRON_LEGACY.prepareRenderOnlyInCallOrder("Off");
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
    private static final LegacyWavefrontModel.SelectionHandle STRAND_CASTER_CASTER =
            ObjMachineModels.STRAND_CASTER.prepareRenderOnlyInCallOrder("caster");
    private static final LegacyWavefrontModel.SelectionHandle RADGEN_BASE =
            ObjMachineModels.RADGEN.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle RADGEN_ROTOR =
            ObjMachineModels.RADGEN.prepareRenderOnlyInCallOrder("Rotor");
    private static final LegacyWavefrontModel.SelectionHandle RADGEN_LIGHT =
            ObjMachineModels.RADGEN.prepareRenderOnlyInCallOrder("Light");
    private static final LegacyWavefrontModel.SelectionHandle RADGEN_GLASS =
            ObjMachineModels.RADGEN.prepareRenderOnlyInCallOrder("Glass");
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
        return HbmShaderCompatibilityDetector.shouldRenderBlockEntityOffScreen();
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        BlockState state = blockEntity.getBlockState();
        if (rendersChunkBakedStaticBodyOnly(state)
                || rendersChunkBakedHeatBoilerNormal(state)
                || !BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)) {
            return false;
        }
        return !rendersEmptyDynamicProfile(blockEntity, state)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    protected int resolveModelLight(T blockEntity, BlockState state, LegacyMachineDefinition definition,
            int packedLight) {
        return LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        LegacyMachineDefinition definition = visibleDefinition(state);
        if (definition == null) {
            return;
        }
        AABB renderBounds = blockEntity.getRenderBoundingBox();
        if (!HbmRenderFrameCulling.shouldRender(blockEntity, renderBounds,
                HbmModelRenderDistances.SQUARED_BLOCKS)) {
            return;
        }

        int modelLight = resolveModelLight(blockEntity, state, definition, packedLight);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                LegacyVisibleMachineRenderer::createModel);

        try (HbmRenderFrameCulling.MachineRendererSubmissionScope cullingScope =
                        HbmRenderFrameCulling.pushMachineRendererSubmissionScope(blockEntity);
                LegacyRenderLighting.ModelViewSamplingScope ignored =
                LegacyRenderLighting.pushModelViewSampling(blockEntity, poseStack.last().pose())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            LegacyPoseRotations.rotateYDegrees(poseStack, definition.yRotation(state));
            Vec3 translation = definition.modelTranslation(state);
            poseStack.translate(translation.x, translation.y, translation.z);
            LegacyPoseRotations.rotateYDegrees(poseStack, definition.postModelYRotation(state));

            LegacyTexturedRenderMode renderMode = LegacyMachinePartRenderContexts.renderMode(definition.renderMode());
            if (definition.renderProfile() == LegacyMachineRenderProfile.DEFAULT) {
                if (definition.renderAll()) {
                    if (rendersChunkBakedStaticBodyOnly(state)) {
                        HbmRenderFrameCulling.recordMachineRendererSubmission(blockEntity, 0,
                                MachineRenderRoute.DEFAULT_RENDER_ALL, 0);
                        poseStack.popPose();
                        return;
                    }
                    if (rendersChunkBakedDefaultRenderAllBodyOnly(state)) {
                        HbmRenderFrameCulling.recordMachineRendererSubmission(blockEntity, 0,
                                MachineRenderRoute.DEFAULT_RENDER_ALL, 0);
                    } else {
                        HbmRenderFrameCulling.recordMachineRendererSubmission(blockEntity, 0,
                                MachineRenderRoute.DEFAULT_RENDER_ALL, 0);
                        model.renderAll(definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay,
                                renderMode);
                    }
                } else {
                    if (rendersChunkBakedDefaultPartsOnly(state)) {
                        HbmRenderFrameCulling.recordMachineRendererSubmission(blockEntity, 0,
                                MachineRenderRoute.DEFAULT_PARTS, 0);
                    } else {
                        LegacyMachinePartRenderSelection.Selection selection =
                                LegacyMachinePartRenderSelection.world(definition);
                        HbmRenderFrameCulling.recordMachineRendererSubmission(blockEntity, 0,
                                MachineRenderRoute.DEFAULT_PARTS, partRunCount(selection));
                        renderParts(definition, selection, model, poseStack, buffer, modelLight, packedOverlay);
                    }
                }
            } else {
                if (definition.renderAll()) {
                    boolean direct = renderProfileDirect(blockEntity, partialTick, definition, model, poseStack, buffer,
                            modelLight, packedLight, packedOverlay, renderMode);
                    if (!direct) {
                        HbmRenderFrameCulling.recordMachineRendererSubmission(blockEntity, 0,
                                MachineRenderRoute.PROFILE_FALLBACK_RENDER_ALL, 0);
                        model.renderAll(definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay,
                                renderMode);
                    } else {
                        HbmRenderFrameCulling.recordMachineRendererSubmission(blockEntity, 0,
                                MachineRenderRoute.PROFILE_DIRECT, 0);
                        poseStack.popPose();
                        return;
                    }
                } else {
                    boolean direct = renderProfileDirect(blockEntity, partialTick, definition, model, poseStack, buffer,
                            modelLight, packedLight, packedOverlay, renderMode);
                    if (!direct) {
                        LegacyMachinePartRenderSelection.Selection selection =
                                LegacyMachinePartRenderSelection.world(definition);
                        HbmRenderFrameCulling.recordMachineRendererSubmission(blockEntity, 0,
                                MachineRenderRoute.PROFILE_FALLBACK_PARTS, partRunCount(selection));
                        renderParts(definition, selection, model, poseStack, buffer, modelLight, packedOverlay);
                    } else {
                        HbmRenderFrameCulling.recordMachineRendererSubmission(blockEntity, 0,
                                MachineRenderRoute.PROFILE_DIRECT, 0);
                    }
                }
            }

            renderFelBeam(blockEntity, poseStack, buffer);

            poseStack.popPose();
        }
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
        int randomStart = (int) (gameTime % 1_000L / 2L);
        LegacyMachineEffectPresenter.enqueueFelBeams(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer, length,
                color, randomStart);
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
        if (!boiler.hasSolarMirrorBeamTargets()) {
            return;
        }
        int[] offsets = solarBeamOffsets(boiler, ClientConfig.renderHeliostatBeamLimit());
        if (offsets.length == 0) {
            return;
        }
        double halfWidth = LegacyTileRenderPlans.SOLAR_BOILER_BEAM_HALF_WIDTH;
        double startY = LegacyTileRenderPlans.SOLAR_BOILER_BEAM_START_Y;
        int nearAlpha = colorByte(LegacyTileRenderPlans.SOLAR_BOILER_BEAM_MAX_ALPHA);
        int farAlpha = colorByte(LegacyTileRenderPlans.SOLAR_BOILER_BEAM_MIN_ALPHA);
        LegacyMachineEffectPresenter.enqueueSolarBoilerBeams(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, farAlpha, halfWidth, startY, nearAlpha, farAlpha,
                offsets);
    }

    private static int[] solarBeamOffsets(SolarBoilerBlockEntity boiler, int limit) {
        int safeLimit = Math.max(0, limit);
        if (safeLimit == 0) {
            return NO_SOLAR_BEAM_OFFSETS;
        }
        Set<BlockPos> targets = boiler.getSolarMirrorBeamTargets();
        int count = Math.min(targets.size(), safeLimit);
        if (count == 0) {
            return NO_SOLAR_BEAM_OFFSETS;
        }
        int[] offsets = new int[count * 3];
        BlockPos boilerPos = boiler.getBlockPos();
        int index = 0;
        for (BlockPos mirrorPos : targets) {
            offsets[index++] = boilerPos.getX() - mirrorPos.getX();
            offsets[index++] = boilerPos.getY() - mirrorPos.getY();
            offsets[index++] = boilerPos.getZ() - mirrorPos.getZ();
            if (index >= offsets.length) {
                break;
            }
        }
        return offsets;
    }

    private static boolean fancyGraphics() {
        GraphicsStatus graphics = Minecraft.getInstance().options.graphicsMode().get();
        return graphics == GraphicsStatus.FANCY || graphics == GraphicsStatus.FABULOUS;
    }

    private static void renderUntexturedQuad(UntexturedQuadGroup group,
            double x0, double y0, double z0, int color0, int alpha0,
            double x1, double y1, double z1, int color1, int alpha1,
            double x2, double y2, double z2, int color2, int alpha2,
            double x3, double y3, double z3, int color3, int alpha3) {
        group.add(
                x0, y0, z0, color0, alpha0,
                x1, y1, z1, color1, alpha1,
                x2, y2, z2, color2, alpha2,
                x3, y3, z3, color3, alpha3);
    }

    private static int colorByte(double value) {
        return Mth.clamp((int) (value * 255.0D), 0, 255);
    }

    private static void renderParts(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyMachinePartRenderSelection.Selection selection = LegacyMachinePartRenderSelection.world(definition);
        renderParts(definition, selection, model, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderParts(LegacyMachineDefinition definition,
            LegacyMachinePartRenderSelection.Selection selection, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderParts(selection.opaqueRuns(), model, poseStack, buffer, packedLight, packedOverlay,
                LegacyMachinePartRenderContexts.renderMode(definition.renderMode()));
        renderParts(selection.translucentRuns(), model, poseStack, buffer, packedLight, packedOverlay,
                LegacyMachinePartRenderContexts.renderMode(definition.renderMode()));
    }

    private static int partRunCount(LegacyMachinePartRenderSelection.Selection selection) {
        return selection.opaqueRuns().size() + selection.translucentRuns().size();
    }

    public static VisibleMachineRouteCoverage routeCoverageSnapshot() {
        Set<LegacyMachineDefinition> seenDefinitions =
                Collections.newSetFromMap(new IdentityHashMap<>());
        int blocks = 0;
        int defaultRenderAll = 0;
        int defaultParts = 0;
        int profileRenderAll = 0;
        int profileParts = 0;
        int profileDirect = 0;
        int profileFallback = 0;
        int itemPartDefinitions = 0;
        int partPropertyDefinitions = 0;
        for (var block : ForgeRegistries.BLOCKS.getValues()) {
            if (!(block instanceof LegacyVisibleMachineBlock visibleBlock)) {
                continue;
            }
            blocks++;
            LegacyMachineDefinition definition = visibleBlock.definition();
            if (definition == null || !seenDefinitions.add(definition)) {
                continue;
            }
            boolean defaultProfile = definition.renderProfile() == LegacyMachineRenderProfile.DEFAULT;
            if (defaultProfile) {
                if (definition.renderAll()) {
                    defaultRenderAll++;
                } else {
                    defaultParts++;
                }
            } else {
                if (definition.renderAll()) {
                    profileRenderAll++;
                } else {
                    profileParts++;
                }
                if (profileHasDirectRoute(definition.renderProfile())) {
                    profileDirect++;
                } else {
                    profileFallback++;
                }
            }
            if (!definition.itemRenderAll()) {
                itemPartDefinitions++;
            }
            if (!definition.partRenderProperties().isEmpty() || !definition.itemPartRenderProperties().isEmpty()) {
                partPropertyDefinitions++;
            }
        }
        return new VisibleMachineRouteCoverage(blocks, seenDefinitions.size(), defaultRenderAll, defaultParts,
                profileRenderAll, profileParts, profileDirect, profileFallback, itemPartDefinitions,
                partPropertyDefinitions);
    }

    private static boolean profileHasDirectRoute(LegacyMachineRenderProfile profile) {
        return switch (profile) {
            case ANNIHILATOR_UV_SCROLL,
                    RADGEN_STATIC_SPECIAL,
                    BATTERY_REDD_STATIC_SPECIAL,
                    CRYSTALLIZER_STATIC_SPECIAL,
                    CRYSTALLIZER_RUNNING_PARTS,
                    FURNACE_IRON_BURN_STATE,
                    ARC_FURNACE_STATIC_PREVIEW,
                    COMPRESSOR_RUNNING_PARTS,
                    COMPRESSOR_COMPACT_RUNNING_FANS,
                    POWERED_CONDENSER_FANS,
                    PUMP_RUNNING_PARTS,
                    DIESEL_GENERATOR_RUNNING_PARTS,
                    CYCLOTRON_PLUGS,
                    AMMO_PRESS_RUNNING_PARTS,
                    ROTARY_FURNACE_PISTON,
                    RTG_CONNECTORS,
                    INTAKE_FAN,
                    STIRLING_RUNNING_PARTS,
                    SAWMILL_RUNNING_PARTS,
                    COMBUSTION_ENGINE_DOOR_CANISTER,
                    ASHPIT_DOOR_INNER,
                    ARC_WELDER_DISPLAY_OUTPUT,
                    FURNACE_STEEL_FIRE,
                    COMBINATION_OVEN_FIRE,
                    MIXER_RUNNING_PARTS,
                    STRAND_CASTER_MOLTEN,
                    CRUCIBLE_MOLTEN,
                    FIREBOX_HEATER,
                    PRECASS_RUNNING_PARTS,
                    PUREX_RUNNING_PARTS,
                    HEAT_BOILER,
                    HEPHAESTUS_RUNNING_CORE,
                    REFINERY_DAMAGE_STATE,
                    BLAST_FURNACE_TILTED_STATE,
                    GAS_FLARE_TILTED_STATE -> true;
            default -> false;
        };
    }

    private static LegacyWavefrontModel createModel(LegacyMachineDefinition definition) {
        if (definition.renderProfile() == LegacyMachineRenderProfile.CRUCIBLE_MOLTEN) {
            // AdvancedModelLoader's WavefrontObject path in 1.7.10 derives face normals for the crucible shell.
            return ObjMachineModels.CRUCIBLE_LEGACY;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.BLAST_FURNACE_TILTED_STATE) {
            // ResourceManager.blast_furnace is explicitly noSmooth() in 1.7.10. Reusing the prepared
            // source-equivalent model keeps both normal and tilted world states on face normals.
            return ObjMachineModels.BLAST_FURNACE_LEGACY;
        }
        return new LegacyWavefrontModel(definition.modelLocation(), definition.textureLocation()).asVBO();
    }

    private static void renderParts(List<LegacyMachinePartRenderSelection.Run> parts, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode defaultRenderMode) {
        LegacyMachinePartBatchRenderer.renderRuns(parts, model, poseStack, buffer, packedLight, packedOverlay,
                defaultRenderMode);
    }

    private static DirectPartRenderState partRenderState(LegacyMachineDefinition definition, String part,
            int packedLight, LegacyTexturedRenderMode defaultRenderMode, boolean forceFullBright) {
        LegacyMachinePartRenderProperties properties = definition.partRenderProperties().get(part);
        if (properties == null) {
            return new DirectPartRenderState(forceFullBright ? LightTexture.FULL_BRIGHT : packedLight,
                    255, 255, 255, 255, defaultRenderMode);
        }
        LegacyTexturedRenderMode renderMode = LegacyMachinePartRenderContexts.renderMode(properties.mode());
        int color = properties.hasColor() ? properties.color() : 0xFFFFFF;
        int alpha = properties.hasColor() || properties.alpha() < 255 ? properties.alpha() : 255;
        int light = properties.fullBright() || forceFullBright ? LightTexture.FULL_BRIGHT : packedLight;
        return new DirectPartRenderState(light, color >> 16 & 255, color >> 8 & 255, color & 255,
                alpha, renderMode);
    }

    private static boolean renderProfileDirect(BlockEntity blockEntity, float partialTick,
            LegacyMachineDefinition definition, LegacyWavefrontModel model, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        switch (definition.renderProfile()) {
            case ANNIHILATOR_UV_SCROLL -> {
                renderAnnihilatorDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay,
                        renderMode, blockEntity);
                return true;
            }
            case RADGEN_STATIC_SPECIAL -> {
                renderRadGenDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay, renderMode,
                        blockEntity);
                return true;
            }
            case BATTERY_REDD_STATIC_SPECIAL -> {
                renderBatteryReddDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay, renderMode,
                        blockEntity, partialTick);
                return true;
            }
            case CRYSTALLIZER_STATIC_SPECIAL -> {
                renderCrystallizerStaticDirect(definition, model, poseStack, buffer, packedLight, packedOverlay,
                        renderMode, renderChunkBakedStaticsInBer());
                return true;
            }
            case CRYSTALLIZER_RUNNING_PARTS -> {
                renderCrystallizerDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay,
                        renderMode, blockEntity, partialTick);
                return true;
            }
            case FURNACE_IRON_BURN_STATE -> {
                renderFurnaceIronDirect(definition, model, poseStack, buffer, packedLight, packedOverlay,
                        renderMode, blockEntity);
                return true;
            }
            case ARC_FURNACE_STATIC_PREVIEW -> {
                LegacyArcFurnaceRenderHelper.renderStaticPreview(model, poseStack, buffer, packedLight,
                        packedOverlay, renderMode);
                return true;
            }
            case COMPRESSOR_RUNNING_PARTS -> {
                float lift = blockEntity instanceof CompressorBlockEntity compressor
                        ? compressor.getPiston(partialTick)
                        : 0.0F;
                float fan = blockEntity instanceof CompressorBlockEntity compressor
                        ? compressor.getFanSpin(partialTick)
                        : 0.0F;
                renderCompressorDirect(definition, model, lift, fan, poseStack, buffer, packedLight, activityLight,
                        packedOverlay, renderMode, blockEntity,
                        true);
                return true;
            }
            case COMPRESSOR_COMPACT_RUNNING_FANS -> {
                float fan = blockEntity instanceof CompressorBlockEntity compressor
                        ? compressor.getFanSpin(partialTick)
                        : 0.0F;
                renderCompressorCompactDirect(definition, model, fan, poseStack, buffer, packedLight, activityLight,
                        packedOverlay, renderMode, blockEntity,
                        true);
                return true;
            }
            case POWERED_CONDENSER_FANS -> {
                float fan = blockEntity instanceof PoweredCondenserBlockEntity condenser
                        ? condenser.getFanSpin(partialTick)
                        : 0.0F;
                renderCompressorCompactDirect(definition, model, fan, poseStack, buffer, packedLight, activityLight,
                        packedOverlay, renderMode, blockEntity,
                        true);
                return true;
            }
            case PUMP_RUNNING_PARTS -> {
                double rotor = blockEntity instanceof WaterPumpBlockEntity pump ? pump.getRotor(partialTick) : 0.0D;
                renderPumpDirect(definition, model, rotor, poseStack, buffer, packedLight, activityLight, packedOverlay,
                        renderMode, blockEntity,
                        true);
                return true;
            }
            case DIESEL_GENERATOR_RUNNING_PARTS -> {
                renderDieselGeneratorDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay,
                        renderMode, blockEntity);
                return true;
            }
            case CYCLOTRON_PLUGS -> {
                renderCyclotronDirect(definition, model, poseStack, buffer, packedLight, packedOverlay, renderMode,
                        blockEntity);
                return true;
            }
            case AMMO_PRESS_RUNNING_PARTS -> {
                renderAmmoPressDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay, renderMode,
                        blockEntity, partialTick);
                return true;
            }
            case ROTARY_FURNACE_PISTON -> {
                renderRotaryFurnaceDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay,
                        renderMode, blockEntity, partialTick);
                return true;
            }
            case RTG_CONNECTORS -> {
                renderRtgDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay,
                        renderMode, blockEntity);
                return true;
            }
            case INTAKE_FAN -> {
                renderIntakeDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay, renderMode,
                        blockEntity, partialTick);
                return true;
            }
            case STIRLING_RUNNING_PARTS -> {
                renderStirlingDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay, renderMode,
                        blockEntity, partialTick);
                return true;
            }
            case SAWMILL_RUNNING_PARTS -> {
                renderSawmillDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay, renderMode,
                        blockEntity, partialTick);
                return true;
            }
            case COMBUSTION_ENGINE_DOOR_CANISTER -> {
                renderCombustionEngineDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay,
                        renderMode, blockEntity, partialTick);
                return true;
            }
            case ASHPIT_DOOR_INNER -> {
                renderAshpitDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay, renderMode,
                        blockEntity, partialTick);
                return true;
            }
            case ARC_WELDER_DISPLAY_OUTPUT -> {
                renderArcWelderDirect(definition, model, poseStack, buffer, packedLight, activityLight,
                        packedOverlay, renderMode, blockEntity);
                return true;
            }
            case FURNACE_STEEL_FIRE -> {
                renderFurnaceSteelDirect(definition, model, poseStack, buffer, packedLight, packedOverlay,
                        renderMode, blockEntity);
                return true;
            }
            case COMBINATION_OVEN_FIRE -> {
                renderCombinationOvenDirect(definition, model, poseStack, buffer, packedLight, packedOverlay,
                        renderMode, blockEntity);
                return true;
            }
            case MIXER_RUNNING_PARTS -> {
                renderMixerDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay, renderMode,
                        blockEntity, partialTick);
                return true;
            }
            case STRAND_CASTER_MOLTEN -> {
                renderStrandCasterDirect(definition, model, poseStack, buffer, packedLight, activityLight,
                        packedOverlay, renderMode, blockEntity);
                return true;
            }
            case CRUCIBLE_MOLTEN -> {
                renderCrucibleDirect(definition, model, poseStack, buffer, packedLight, packedOverlay, renderMode,
                        blockEntity);
                return true;
            }
            case FIREBOX_HEATER -> {
                renderFireboxHeaterDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay,
                        renderMode, blockEntity, partialTick);
                return true;
            }
            case PRECASS_RUNNING_PARTS -> {
                renderPrecassDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay, renderMode,
                        blockEntity, partialTick);
                return true;
            }
            case PUREX_RUNNING_PARTS -> {
                renderPurexDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay, renderMode,
                        blockEntity, partialTick);
                return true;
            }
            case HEAT_BOILER -> {
                renderHeatBoilerDirect(definition, model, poseStack, buffer, packedLight, packedOverlay,
                        renderMode, blockEntity);
                return true;
            }
            case HEPHAESTUS_RUNNING_CORE -> {
                renderHephaestusDirect(definition, model, poseStack, buffer, packedLight, activityLight, packedOverlay,
                        renderMode, blockEntity, partialTick);
                return true;
            }
            case REFINERY_DAMAGE_STATE -> {
                renderRefineryDirect(definition, model, poseStack, buffer, packedLight, packedOverlay,
                        renderMode, blockEntity);
                return true;
            }
            case BLAST_FURNACE_TILTED_STATE -> {
                renderBlastFurnaceDirect(definition, model, poseStack, buffer, packedLight, packedOverlay,
                        renderMode, blockEntity);
                return true;
            }
            case GAS_FLARE_TILTED_STATE -> {
                renderGasFlareDirect(definition, model, poseStack, buffer, packedLight, packedOverlay,
                        renderMode, blockEntity);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static void withAnimatedModelFade(BlockEntity blockEntity, Runnable action) {
        if (blockEntity == null) {
            action.run();
            return;
        }
        try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
            action.run();
        }
    }

    private static boolean renderChunkBakedStaticsInBer() {
        return LegacyMachineRenderShapes.renderChunkBakedStaticsInBer();
    }

    private static boolean rendersChunkBakedStaticBodyOnly(BlockState state) {
        if (renderChunkBakedStaticsInBer()) {
            return false;
        }
        if (state.getBlock() instanceof BlastFurnaceBlock
                && state.hasProperty(BlastFurnaceBlock.TILTED)
                && !state.getValue(BlastFurnaceBlock.TILTED)) {
            return true;
        }
        if (state.getBlock() instanceof GasFlareBlock
                && state.hasProperty(GasFlareBlock.TILTED)
                && !state.getValue(GasFlareBlock.TILTED)) {
            return true;
        }
        if (state.getBlock() instanceof RefineryBlock
                && state.hasProperty(RefineryBlock.EXPLODED)
                && state.hasProperty(RefineryBlock.TILTED)
                && !state.getValue(RefineryBlock.EXPLODED)
                && !state.getValue(RefineryBlock.TILTED)) {
            return true;
        }
        if (state.getBlock() instanceof CoolingTowerBlock tower) {
            return tower.usesChunkBakedStaticModel();
        }
        if (state.getBlock() instanceof WoodBurnerBlock
                || state.getBlock() instanceof HeaterHeatexBlock
                || state.getBlock() instanceof OilburnerBlock
                || state.getBlock() instanceof ElectricHeaterBlock
                || state.getBlock() instanceof ChimneyBlock
                || state.getBlock() instanceof DrainBlock
                || state.getBlock() instanceof RadiolysisBlock
                || state.getBlock() instanceof GasCentBlock
                || state.getBlock() instanceof SilexBlock
                || state.getBlock() instanceof DeuteriumTowerBlock
                || (state.getBlock() instanceof RemoteFluidMachineBlock machine
                        && machine.usesChunkBakedStaticModel())
                || state.getBlock() instanceof FractionSpacerBlock
                || state.getBlock() instanceof SatelliteDockBlock
                || (state.getBlock() instanceof ProcessingMachineBlock machine
                        && machine.usesChunkBakedStaticModel())
                || (state.getBlock() instanceof OilDrillBlock drill
                        && drill.usesChunkBakedStaticModel())) {
            return true;
        }
        LegacyMachineDefinition definition = visibleDefinition(state);
        return state.getBlock() instanceof HeatBoilerBlock
                && definition != null
                && definition.renderProfile() == LegacyMachineRenderProfile.DEFAULT;
    }

    private static boolean rendersChunkBakedHeatBoilerNormal(BlockState state) {
        if (renderChunkBakedStaticsInBer()
                || !(state.getBlock() instanceof HeatBoilerBlock)
                || !state.hasProperty(HeatBoilerBlock.VISUAL)) {
            return false;
        }
        LegacyMachineDefinition definition = visibleDefinition(state);
        return definition != null
                && definition.renderProfile() == LegacyMachineRenderProfile.HEAT_BOILER
                && state.getValue(HeatBoilerBlock.VISUAL) == HeatBoilerBlock.BoilerVisualState.NORMAL;
    }

    private static boolean rendersChunkBakedDefaultRenderAllBodyOnly(BlockState state) {
        return !renderChunkBakedStaticsInBer()
                && state.getBlock() instanceof FelBlock;
    }

    private static boolean rendersChunkBakedDefaultPartsOnly(BlockState state) {
        return !renderChunkBakedStaticsInBer()
                && (state.getBlock() instanceof SolarBoilerBlock
                || state.getBlock() instanceof GasCentBlock);
    }

    private static boolean rendersEmptyDynamicProfile(BlockEntity blockEntity, BlockState state) {
        if (!isEmptyDynamicProfileCandidate(blockEntity)) {
            return false;
        }
        LegacyMachineDefinition definition = visibleDefinition(state);
        if (definition == null) {
            return false;
        }
        if (rendersChunkBakedDefaultPartsOnly(state) && blockEntity instanceof SolarBoilerBlockEntity boiler) {
            return !hasVisibleSolarBoilerBeams(boiler);
        }
        if (rendersChunkBakedDefaultRenderAllBodyOnly(state) && blockEntity instanceof FelBlockEntity fel) {
            return !hasVisibleFelBeam(fel);
        }
        return switch (definition.renderProfile()) {
            case RTG_CONNECTORS -> false;
            case FURNACE_STEEL_FIRE -> !renderChunkBakedStaticsInBer()
                    && blockEntity instanceof LegacyFurnaceBlockEntity furnace && !furnace.wasOn();
            case COMBINATION_OVEN_FIRE -> !renderChunkBakedStaticsInBer()
                    && blockEntity instanceof CombinationOvenBlockEntity oven && !oven.wasOn();
            case ARC_WELDER_DISPLAY_OUTPUT -> !renderChunkBakedStaticsInBer()
                    && blockEntity instanceof ArcWelderBlockEntity welder
                    && welder.getDisplayOutput().isEmpty();
            case STRAND_CASTER_MOLTEN -> false;
            case CRUCIBLE_MOLTEN -> !renderChunkBakedStaticsInBer()
                    && blockEntity instanceof CrucibleBlockEntity crucible
                    && crucible.getTotalMaterialAmount() <= 0;
            default -> false;
        };
    }

    private static boolean isEmptyDynamicProfileCandidate(BlockEntity blockEntity) {
        return blockEntity instanceof RtgBlockEntity
                || blockEntity instanceof LegacyFurnaceBlockEntity
                || blockEntity instanceof CombinationOvenBlockEntity
                || blockEntity instanceof ArcWelderBlockEntity
                || blockEntity instanceof StrandCasterBlockEntity
                || blockEntity instanceof CrucibleBlockEntity
                || blockEntity instanceof SolarBoilerBlockEntity
                || blockEntity instanceof FelBlockEntity;
    }

    private static boolean hasVisibleSolarBoilerBeams(SolarBoilerBlockEntity boiler) {
        return fancyGraphics() && boiler.hasSolarMirrorBeamTargets();
    }

    private static boolean hasVisibleFelBeam(FelBlockEntity fel) {
        return fel.isBeamActive();
    }

    private static void renderRtgDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        renderRtgPart(model, "Gen", definition.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay, renderMode);
        if (!(blockEntity instanceof HbmEnergyConnector connector) || blockEntity.getLevel() == null) {
            return;
        }
        renderRtgConnectorDirect(definition, model, poseStack, buffer, activityLight, packedOverlay, renderMode,
                blockEntity, connector, Direction.EAST, 0.0F);
        renderRtgConnectorDirect(definition, model, poseStack, buffer, activityLight, packedOverlay, renderMode,
                blockEntity, connector, Direction.WEST, 180.0F);
        renderRtgConnectorDirect(definition, model, poseStack, buffer, activityLight, packedOverlay, renderMode,
                blockEntity, connector, Direction.NORTH, 90.0F);
        renderRtgConnectorDirect(definition, model, poseStack, buffer, activityLight, packedOverlay, renderMode,
                blockEntity, connector, Direction.SOUTH, -90.0F);
    }

    private static boolean hasVisibleRtgConnector(BlockEntity blockEntity) {
        if (!(blockEntity instanceof HbmEnergyConnector connector) || blockEntity.getLevel() == null) {
            return false;
        }
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        return HbmEnergyConnectionUtil.canConnect(level, pos, connector, Direction.EAST)
                || HbmEnergyConnectionUtil.canConnect(level, pos, connector, Direction.WEST)
                || HbmEnergyConnectionUtil.canConnect(level, pos, connector, Direction.NORTH)
                || HbmEnergyConnectionUtil.canConnect(level, pos, connector, Direction.SOUTH);
    }

    private static void renderRtgConnectorDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, HbmEnergyConnector connector,
            Direction direction, float yawDegrees) {
        if (!HbmEnergyConnectionUtil.canConnect(blockEntity.getLevel(), blockEntity.getBlockPos(),
                connector, direction)) {
            return;
        }
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, yawDegrees);
        renderRtgPart(model, "Connector", definition.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderRtgPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.RTG_LEGACY) ? rtgHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.RTG_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        renderTexturedPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    private static LegacyWavefrontModel.SelectionHandle rtgHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Gen" -> RTG_GEN;
            case "Connector" -> RTG_CONNECTOR;
            default -> null;
        };
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

    private static void renderCyclotronDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        // RenderCyclotron emits Body before its stateful plugs.  The normal path owns Body in the
        // chunk-baked model; when baked statics are disabled the BER must restore that shell.
        if (renderChunkBakedStaticsInBer()) {
            renderCyclotronPart(model, "Body", definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        boolean plugged = true;
        for (int index = 0; index < 4; index++) {
            boolean filled = blockEntity instanceof CyclotronBlockEntity cyclotron && cyclotron.getPlug(index);
            plugged &= filled;
            ResourceLocation texture = filled ? CYCLOTRON_FILLED_TEXTURES[index] : CYCLOTRON_EMPTY_TEXTURES[index];
            renderCyclotronPart(model, "B" + (index + 1), texture, poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        if (plugged) {
            renderCyclotronGlyphRing(poseStack, buffer);
        }
    }

    static void renderCyclotronItemParts(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderCyclotronPart(model, "Body", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        for (int index = 0; index < 4; index++) {
            renderCyclotronPart(model, "B" + (index + 1), CYCLOTRON_EMPTY_TEXTURES[index], poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
    }

    private static void renderCyclotronPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.CYCLOTRON) ? cyclotronHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.CYCLOTRON.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
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
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) spin);
        poseStack.translate(0.0D, 2.0D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);

        float rotation = 0.0F;
        for (int index = 0; index < CYCLOTRON_GLYPH_MESSAGE.length(); index++) {
            String glyph = CYCLOTRON_GLYPH_MESSAGE.substring(index, index + 1);
            poseStack.pushPose();
            LegacyPoseRotations.rotateYDegrees(poseStack, rotation);
            rotation -= font.width(glyph) * 2.0F;
            poseStack.translate(2.75D, 0.0D, 0.0D);
            LegacyPoseRotations.rotateYDegrees(poseStack, -(90.0F));
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

    private static void renderStrandCasterDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        ObjMachineModels.STRAND_CASTER.renderOnlyInCallOrder(definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, STRAND_CASTER_CASTER, renderMode);
        if (!(blockEntity instanceof StrandCasterBlockEntity caster)) {
            return;
        }
        var mold = caster.getInstalledMold();
        int amount = caster.getMoltenAmount();
        if (mold == null || amount <= 0) {
            return;
        }
        int capacity = caster.getCapacity();
        int moldCost = mold.cost();
        if (capacity <= 0 || moldCost <= 0) {
            return;
        }
        int moltenColor = caster.getMoltenColor();
        int red = moltenColor >> 16 & 255;
        int green = moltenColor >> 8 & 255;
        int blue = moltenColor & 255;
        double level = (double) amount / (double) capacity * LegacyTileRenderPlans.STRAND_CASTER_FLUID_HEIGHT;
        double offset = (double) amount / (double) moldCost * LegacyTileRenderPlans.STRAND_CASTER_PLATE_TRAVEL;
        double plateZ = Math.max(-offset + LegacyTileRenderPlans.STRAND_CASTER_PLATE_BASE_Z, 0.0D);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, plateZ);
        model.renderPartClipped("plate", definition.textureLocation(), poseStack, buffer,
                activityLight, packedOverlay, red, green, blue, 255, false,
                LegacyTexturedRenderMode.LIGHTMAP_ONLY_CUTOUT_NO_CULL,
                LegacyWavefrontModel.UvTransform.DEFAULT, 0.0D, 0.0D, -1.0D, 0.5D - plateZ);
        poseStack.popPose();

        double lavaY = LegacyTileRenderPlans.STRAND_CASTER_LAVA_BASE_Y + level;
        int color = moltenColor & 0xFFFFFF;
        LegacyMachineEffectPresenter.enqueueTexturedQuad(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                STRAND_CASTER_LAVA_TEXTURE, renderMode, LightTexture.FULL_BRIGHT, packedOverlay,
                0.0F, 1.0F, 0.0F,
                -LegacyTileRenderPlans.STRAND_CASTER_LAVA_HALF_WIDTH, lavaY,
                -LegacyTileRenderPlans.STRAND_CASTER_LAVA_HALF_DEPTH, 0.0D, 0.0D,
                -LegacyTileRenderPlans.STRAND_CASTER_LAVA_HALF_WIDTH, lavaY,
                LegacyTileRenderPlans.STRAND_CASTER_LAVA_HALF_DEPTH, 0.0D, 1.0D,
                LegacyTileRenderPlans.STRAND_CASTER_LAVA_HALF_WIDTH, lavaY,
                LegacyTileRenderPlans.STRAND_CASTER_LAVA_HALF_DEPTH, 1.0D, 1.0D,
                LegacyTileRenderPlans.STRAND_CASTER_LAVA_HALF_WIDTH, lavaY,
                -LegacyTileRenderPlans.STRAND_CASTER_LAVA_HALF_DEPTH, 1.0D, 0.0D,
                color, 255);
    }

    private static void renderStrandCasterPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.STRAND_CASTER) ? strandCasterHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.STRAND_CASTER.renderOnlyInCallOrder(texture, poseStack, buffer,
                    packedLight, packedOverlay, handle, renderMode);
            return;
        }
        renderTexturedPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    private static LegacyWavefrontModel.SelectionHandle strandCasterHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "caster" -> STRAND_CASTER_CASTER;
            default -> null;
        };
    }

    private static void renderRotaryFurnaceDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        // RotaryFurnaceBlock intentionally stays ENTITYBLOCK_ANIMATED: its source-sized OBJ spans
        // well beyond the core block and chunk baking writes invalid zero light into distant faces.
        // Consequently no chunk copy exists even when the global static-bake option is enabled.
        // Submit the fixed body exactly once through the prepared/VBO BER path with modelLight;
        // the moving piston below keeps the core's ordinary packed world light.
        renderRotaryFurnacePart(model, "Furnace", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        float offset = blockEntity instanceof RotaryFurnaceBlockEntity furnace
                ? furnace.getPistonOffset(partialTick)
                : 0.0F;
        withAnimatedModelFade(blockEntity, () -> renderRotaryFurnaceTranslatedPart(model, "Piston",
                true, 0.0D, offset, 0.0D, definition.textureLocation(), poseStack, buffer,
                activityLight, packedOverlay, renderMode));
    }

    private static void renderRotaryFurnaceTranslatedPart(LegacyWavefrontModel model, String partName,
            boolean active, double translateX, double translateY, double translateZ, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        if (!active) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        renderRotaryFurnacePart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderRotaryFurnacePart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.ROTARY_FURNACE) ? rotaryFurnaceHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.ROTARY_FURNACE.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        renderTexturedPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
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

    private static void renderCrucibleDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        // RenderCrucible always submits crucible_heat.renderAll() before the optional molten top. The chunk
        // model owns that body normally; the BER-static fallback must restore it even when the crucible is empty.
        if (renderChunkBakedStaticsInBer()) {
            model.renderAll(definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay, renderMode);
        }
        if (!(blockEntity instanceof CrucibleBlockEntity crucible) || crucible.getTotalMaterialAmount() <= 0) {
            return;
        }
        double y = 0.5D + crucible.getMoltenLevel();
        LegacyMachineEffectPresenter.enqueueTexturedQuad(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                CRUCIBLE_LAVA_TEXTURE, renderMode, LightTexture.FULL_BRIGHT, packedOverlay,
                0.0F, 1.0F, 0.0F,
                -1.0D, y, -1.0D, 0.0D, 0.0D,
                -1.0D, y, 1.0D, 0.0D, 1.0D,
                1.0D, y, 1.0D, 1.0D, 1.0D,
                1.0D, y, -1.0D, 1.0D, 0.0D,
                0xFFFFFF, 255);
    }

    static void renderCompressorPlan(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            LegacyTileRenderPlans.CompressorPlan plan, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        renderCompressorPlan(definition, model, plan, poseStack, buffer, packedLight, packedOverlay,
                renderMode, null, true);
    }

    private static void renderCompressorPlan(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            LegacyTileRenderPlans.CompressorPlan plan, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode, BlockEntity blockEntity,
            boolean renderBody) {
        if (renderBody) {
            renderCompressorPart(model, plan.bodyPartName(), definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        withAnimatedModelFade(blockEntity, () -> {
            LegacyTileRenderPlans.TranslatedModelPartPlan pump = plan.pump();
            if (pump != null && pump.active()) {
                poseStack.pushPose();
                poseStack.translate(pump.translateX(), pump.translateY(), pump.translateZ());
                renderCompressorPart(model, pump.partName(), definition.textureLocation(), poseStack, buffer,
                        packedLight, packedOverlay, renderMode);
                poseStack.popPose();
            }
            for (LegacyTileRenderPlans.RotatingModelPartPlan fan : plan.fans()) {
                renderCompressorRotatingPart(model, fan, definition.textureLocation(), poseStack, buffer,
                        packedLight, packedOverlay, renderMode);
            }
        });
    }

    private static void renderCompressorDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            double piston, double fanDegrees, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int activityLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            BlockEntity blockEntity, boolean renderBody) {
        if (renderBody) {
            renderCompressorPart(model, "Compressor", definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        withAnimatedModelFade(blockEntity, () -> {
            renderCompressorTranslatedPart(model, "Pump", 0.0D,
                    piston * LegacyTileRenderPlans.COMPRESSOR_PUMP_TRAVEL
                            + LegacyTileRenderPlans.COMPRESSOR_PUMP_BASE_Y,
                    0.0D, definition.textureLocation(), poseStack, buffer, activityLight, packedOverlay, renderMode);
            renderCompressorRotatingPart(model, "Fan", 0.0D, LegacyTileRenderPlans.COMPRESSOR_FAN_PIVOT_Y,
                    0.0D, 1.0F, 0.0F, 0.0F, fanDegrees, definition.textureLocation(), poseStack, buffer,
                    activityLight, packedOverlay, renderMode);
        });
    }

    private static void renderCompressorCompactDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            double fanDegrees, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight,
            int packedOverlay, LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, boolean renderBody) {
        if (renderBody) {
            renderCompressorPart(model, "Condenser", definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        withAnimatedModelFade(blockEntity, () -> {
            renderCompressorRotatingPart(model, "Fan1", 0.0D, LegacyTileRenderPlans.COMPRESSOR_FAN_PIVOT_Y,
                    0.0D, 1.0F, 0.0F, 0.0F, fanDegrees, definition.textureLocation(), poseStack, buffer,
                    activityLight, packedOverlay, renderMode);
            renderCompressorRotatingPart(model, "Fan2", 0.0D, LegacyTileRenderPlans.COMPRESSOR_FAN_PIVOT_Y,
                    0.0D, -1.0F, 0.0F, 0.0F, fanDegrees, definition.textureLocation(), poseStack, buffer,
                    activityLight, packedOverlay, renderMode);
        });
    }

    private static void renderCompressorTranslatedPart(LegacyWavefrontModel model, String partName,
            double translateX, double translateY, double translateZ, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        renderCompressorPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderCompressorRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderCompressorRotatingPart(model, part.partName(), part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), texture, poseStack, buffer,
                packedLight, packedOverlay, renderMode);
    }

    private static void renderCompressorRotatingPart(LegacyWavefrontModel model, String partName,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        rotateAround(poseStack, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees);
        renderCompressorPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    static void renderCompressorPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        if (sameModel(model, ObjMachineModels.COMPRESSOR)) {
            LegacyWavefrontModel.SelectionHandle handle = compressorHandle(partName);
            if (handle != null) {
                ObjMachineModels.COMPRESSOR.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                        packedOverlay, handle, renderMode);
                return;
            }
        }
        if (sameModel(model, ObjMachineModels.CONDENSER)) {
            LegacyWavefrontModel.SelectionHandle handle = condenserHandle(partName);
            if (handle != null) {
                ObjMachineModels.CONDENSER.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                        packedOverlay, handle, renderMode);
                return;
            }
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
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

    private static void renderAnnihilatorDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        long currentMillis = System.currentTimeMillis();
        if (renderChunkBakedStaticsInBer()) {
            renderAnnihilatorPart(model, "Annihilator", definition.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, renderMode);
        }
        withAnimatedModelFade(blockEntity, () -> {
            renderAnnihilatorRotatingPart(model, "Roller", 0.0D,
                    LegacyTileRenderPlans.ANNIHILATOR_ROLLER_PIVOT_Y, 0.0D, 0.0F, 0.0F, -1.0F,
                    currentMillis * LegacyTileRenderPlans.ANNIHILATOR_ROLLER_ROTATION_SPEED % 360.0D,
                    definition.textureLocation(), poseStack, buffer, activityLight, packedOverlay, renderMode);
            renderAnnihilatorPart(model, "Belt",
                    definition.partTextures().getOrDefault("Belt", definition.textureLocation()),
                    poseStack, buffer, activityLight, packedOverlay, renderMode,
                    legacyTextureMatrixUvTransform(1.0F, 1.0F,
                            (float) LegacyUvAnimation.annihilatorBeltU(currentMillis), 0.0F));
        });
    }

    private static void renderAnnihilatorRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderAnnihilatorRotatingPart(model, part.partName(), part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), texture, poseStack, buffer,
                packedLight, packedOverlay, renderMode);
    }

    private static void renderAnnihilatorRotatingPart(LegacyWavefrontModel model, String partName,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        rotateAround(poseStack, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees);
        renderAnnihilatorPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    static void renderAnnihilatorPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderAnnihilatorPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay,
                renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
    }

    private static void renderAnnihilatorPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, LegacyWavefrontModel.UvTransform uvTransform) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.ANNIHILATOR) ? annihilatorHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.ANNIHILATOR.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, 255, 255, 255, 255, false, renderMode, uvTransform, handle);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, uvTransform);
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

    private static void renderRadGenDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        if (renderChunkBakedStaticsInBer()) {
            renderRadGenPart(model, "Base", definition.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, renderMode);
        }
        boolean on = blockEntity instanceof RadGenBlockEntity radGen && radGen.isOn();
        double rotorAngle = on ? (System.currentTimeMillis() % 3600L) * -0.1D : 0.0D;
        renderRadGenRotatingPart(model, "Rotor", 0.0D, 1.5D, 0.0D,
                1.0F, 0.0F, 0.0F, rotorAngle, definition.textureLocation(), poseStack,
                buffer, activityLight, packedOverlay, renderMode);
        renderRadGenPart(model, "Glass", definition.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay, renderMode);
        if (on) {
            renderRadGenUntexturedPart(model, "Light", poseStack, buffer, RADGEN_LIGHT_ON_RED,
                    RADGEN_LIGHT_ON_GREEN, RADGEN_LIGHT_ON_BLUE, 255, renderMode);
        } else {
            renderRadGenUntexturedPart(model, "Light", poseStack, buffer, RADGEN_LIGHT_OFF_RED,
                    RADGEN_LIGHT_OFF_GREEN, RADGEN_LIGHT_OFF_BLUE, 255, renderMode);
        }
        renderRadGenTintedPart(model, "Glass", WHITE_TEXTURE, poseStack, buffer, packedLight, packedOverlay,
                RADGEN_GLASS_RED, RADGEN_GLASS_GREEN, RADGEN_GLASS_BLUE, RADGEN_GLASS_ALPHA,
                LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE);
    }

    private static void renderRadGenRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderRadGenRotatingPart(model, part.partName(), part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), texture, poseStack, buffer,
                packedLight, packedOverlay, renderMode);
    }

    private static void renderRadGenRotatingPart(LegacyWavefrontModel model, String partName,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        rotateAround(poseStack, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees);
        renderRadGenPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderRadGenPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.RADGEN) ? radGenHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.RADGEN.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        renderTexturedPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    private static void renderRadGenUntexturedPart(LegacyWavefrontModel model, String partName,
            PoseStack poseStack, MultiBufferSource buffer, int red, int green, int blue, int alpha,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.RADGEN) ? radGenHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.RADGEN.renderOnlyUntextured(poseStack, buffer, red, green, blue, alpha,
                    renderMode, handle);
            return;
        }
        model.renderPartUntextured(partName, poseStack, buffer, red, green, blue, alpha, renderMode);
    }

    private static void renderRadGenTintedPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.RADGEN) ? radGenHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.RADGEN.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, red, green, blue, alpha, false, renderMode,
                    LegacyWavefrontModel.UvTransform.DEFAULT, handle);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
    }

    private static LegacyWavefrontModel.SelectionHandle radGenHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> RADGEN_BASE;
            case "Rotor" -> RADGEN_ROTOR;
            case "Light" -> RADGEN_LIGHT;
            case "Glass" -> RADGEN_GLASS;
            default -> null;
        };
    }

    private static void renderBatteryReddDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        if (renderChunkBakedStaticsInBer()) {
            renderBatteryReddPart(model, "Base", definition.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, 255, 255, 255, 255, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
        }
        BatteryReddBlockEntity battery = blockEntity instanceof BatteryReddBlockEntity redd ? redd : null;
        float rotation = battery != null ? battery.getInterpolatedRotation(partialTick) : 0.0F;
        float speed = battery != null ? battery.getSpeed() : 0.0F;
        poseStack.pushPose();
        poseStack.translate(0.0D, 5.5D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, rotation);
        poseStack.translate(0.0D, -5.5D, 0.0D);
        renderBatteryReddPart(model, "Wheel", definition.textureLocation(), poseStack, buffer, activityLight,
                packedOverlay, 255, 255, 255, 255, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
        renderBatteryReddPart(model, "Lights", definition.textureLocation(), poseStack, buffer,
                LightTexture.FULL_BRIGHT, packedOverlay, 255, 255, 255, 255, renderMode,
                LegacyWavefrontModel.UvTransform.DEFAULT);
        enqueueBatteryReddTrail(speed, poseStack, buffer);
        renderBatteryReddPlasma(definition, model, poseStack, buffer, packedOverlay, blockEntity, speed);
        poseStack.popPose();

        Level level = blockEntity.getLevel();
        if (level != null && speed > 0.0F) {
            renderBatteryReddZaps(level.getGameTime(), System.currentTimeMillis(), poseStack, buffer);
        }
    }

    private static void enqueueBatteryReddTrail(double speed, PoseStack poseStack, MultiBufferSource buffer) {
        double span = Math.max(0.0D, speed) * LegacyTileRenderPlans.BATTERY_REDD_TRAIL_SPAN_SCALE;
        if (span <= 0.0D) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 5.5D, 0.0D);
        LegacyMachineEffectPresenter.enqueueUntexturedQuadGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 0,
                group -> renderBatteryReddTrail(group, span));
        poseStack.popPose();
    }

    private static void renderBatteryReddTrail(UntexturedQuadGroup group, double span) {
        for (int sign = -1; sign <= 1; sign += 2) {
            double xOffset = LegacyTileRenderPlans.BATTERY_REDD_TRAIL_X_OFFSET * sign;
            for (int spoke = 0; spoke < LegacyTileRenderPlans.BATTERY_REDD_TRAIL_SPOKES; spoke++) {
                double startAngle = spoke * 45.0D;
                renderBatteryReddTrailSegment(group, xOffset,
                        startAngle, startAngle + span, 0.75F, 0.5F);
                renderBatteryReddTrailSegment(group, xOffset,
                        startAngle + span, startAngle + span * 2.0D, 0.5F, 0.25F);
                renderBatteryReddTrailSegment(group, xOffset,
                        startAngle + span * 2.0D, startAngle + span * 3.0D, 0.25F, 0.0F);
            }
        }
    }

    private static void renderBatteryReddTrailSegment(UntexturedQuadGroup group,
            double x, double startDegrees, double endDegrees, float startAlpha, float endAlpha) {
        double startRadians = startDegrees * LegacyTileRenderPlans.DEG_TO_RAD;
        double endRadians = endDegrees * LegacyTileRenderPlans.DEG_TO_RAD;
        double startY = Math.cos(startRadians);
        double startZ = Math.sin(startRadians);
        double endY = Math.cos(endRadians);
        double endZ = Math.sin(endRadians);
        double length = LegacyTileRenderPlans.BATTERY_REDD_TRAIL_LENGTH;
        double width = LegacyTileRenderPlans.BATTERY_REDD_TRAIL_WIDTH;
        int startAlphaByte = colorByte(startAlpha);
        int endAlphaByte = colorByte(endAlpha);
        renderUntexturedQuad(group,
                x, startY * length - startY * width, startZ * length - startZ * width,
                0xFFFF00, startAlphaByte,
                x, startY * length + startY * width, startZ * length + startZ * width,
                0xFFFF00, startAlphaByte,
                x, endY * length + endY * width, endZ * length + endZ * width,
                0xFFFF00, endAlphaByte,
                x, endY * length - endY * width, endZ * length - endZ * width,
                0xFFFF00, endAlphaByte);
    }

    private static void renderBatteryReddPlasma(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedOverlay, BlockEntity blockEntity, float speed) {
        if (speed <= 0.0F) {
            return;
        }
        boolean extraLayers = isPlayerWithin(blockEntity, 100.0D);
        long currentMillis = System.currentTimeMillis();
        double time = (double) currentMillis;
        float alpha = 0.45F + (float) (Math.sin(time / LegacyTileRenderPlans.BATTERY_REDD_PLASMA_PERIOD) * 0.15D);
        float alphaMultiplier = (float) Math.max(0.0D, speed) / 15.0F;
        double mainOsc = LegacyObjTransforms.softPeakSine(
                time / LegacyTileRenderPlans.BATTERY_REDD_PLASMA_PERIOD) % 1.0D;
        double sparkleSpin = time / LegacyTileRenderPlans.BATTERY_REDD_SPARKLE_SPIN_PERIOD * -1.0D % 1.0D;
        double sparkleOsc = Math.sin(time / LegacyTileRenderPlans.BATTERY_REDD_PLASMA_PERIOD) * 0.5D % 1.0D;
        int plasmaLight = LegacyTexturedQuadRenderer.legacyLightmap(
                LegacyTileRenderPlans.LEGACY_FULLBRIGHT_LIGHTMAP_X,
                LegacyTileRenderPlans.LEGACY_FULLBRIGHT_LIGHTMAP_Y);
        int plasmaAlpha = colorByte(alpha * alphaMultiplier);
        int sparkleAlpha = colorByte(0.75F * alphaMultiplier);
        LegacyMachineEffectPresenter.enqueueTexturedObjPartGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                buffer, group -> {
            renderBatteryReddPart(group, model, "Plasma", BATTERY_REDD_PLASMA_TEXTURE, plasmaLight,
                    packedOverlay, colorByte(1.0F), colorByte(0.25F), colorByte(0.75F), plasmaAlpha,
                    LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                    legacyTextureMatrixUvTransform(1.0F, 1.0F, 0.0F, (float) mainOsc));
            if (extraLayers) {
                renderBatteryReddPart(group, model, "Plasma", BATTERY_REDD_PLASMA_SPARKLE_TEXTURE,
                        plasmaLight, packedOverlay,
                        colorByte(2.0F), colorByte(0.5F), colorByte(1.5F), sparkleAlpha,
                        LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                        legacyTextureMatrixUvTransform(1.0F, 1.0F,
                                (float) sparkleSpin, (float) sparkleOsc));
            }
        });
    }

    private static void renderBatteryReddPart(TexturedObjPartGroup group, LegacyWavefrontModel model,
            String partName, ResourceLocation texture, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode,
            LegacyWavefrontModel.UvTransform uvTransform) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.BATTERY_REDD_LEGACY) ? batteryReddHandle(partName) : null;
        if (handle != null) {
            group.add(ObjMachineModels.BATTERY_REDD_LEGACY, handle, texture, packedLight, packedOverlay,
                    red, green, blue, alpha, false, renderMode, uvTransform);
        }
    }

    private static void renderBatteryReddPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode,
            LegacyWavefrontModel.UvTransform uvTransform) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.BATTERY_REDD_LEGACY) ? batteryReddHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.BATTERY_REDD_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer,
                    packedLight, packedOverlay, red, green, blue, alpha, false, renderMode, uvTransform, handle);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, false, renderMode, uvTransform);
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
        return LegacyRenderDistanceGates.isPlayerWithin(blockEntity, 2.5D, range);
    }

    private static void renderBatteryReddZaps(long worldTime, long currentMillis,
            PoseStack poseStack, MultiBufferSource buffer) {
        Random random = LegacyRenderRandom.seeded(worldTime / 5L);
        random.nextBoolean();
        int start = (int) (currentMillis % LegacyTileRenderPlans.BATTERY_REDD_ZAP_PERIOD_MILLIS)
                / LegacyTileRenderPlans.BATTERY_REDD_ZAP_START_DIVISOR;
        enqueueBatteryReddZap(random.nextBoolean(), poseStack, buffer, 3.125D, 5.5D, 0.0D,
                -1.375D, -2.625D, 3.75D, start);
        enqueueBatteryReddZap(random.nextBoolean(), poseStack, buffer, -3.125D, 5.5D, 0.0D,
                1.375D, -2.625D, 3.75D, start);
        enqueueBatteryReddZap(random.nextBoolean(), poseStack, buffer, 3.125D, 5.5D, 0.0D,
                -1.375D, -2.625D, -3.75D, start);
        enqueueBatteryReddZap(random.nextBoolean(), poseStack, buffer, -3.125D, 5.5D, 0.0D,
                1.375D, -2.625D, -3.75D, start);
    }

    private static void enqueueBatteryReddZap(boolean active, PoseStack poseStack, MultiBufferSource buffer,
            double translateX, double translateY, double translateZ, double x, double y, double z, int start) {
        if (!active) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        LegacyMachineEffectPresenter.enqueueDualRandomSolidBeam(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                false, x, y, z, LegacyTileRenderPlans.BATTERY_REDD_ZAP_OUTER_COLOR,
                LegacyTileRenderPlans.BATTERY_REDD_ZAP_INNER_COLOR, start,
                LegacyTileRenderPlans.BATTERY_REDD_ZAP_LONG_SEGMENTS, LegacyTileRenderPlans.BATTERY_REDD_ZAP_LONG_SIZE,
                LegacyTileRenderPlans.BATTERY_REDD_ZAP_SHORT_SEGMENTS,
                LegacyTileRenderPlans.BATTERY_REDD_ZAP_SHORT_SIZE, LegacyTileRenderPlans.BATTERY_REDD_ZAP_LAYERS,
                LegacyTileRenderPlans.BATTERY_REDD_ZAP_THICKNESS);
        poseStack.popPose();
    }

    static void renderPumpPlan(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            LegacyTileRenderPlans.PumpPlan plan, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        renderPumpPlan(definition, model, plan, poseStack, buffer, packedLight, packedOverlay, renderMode, null);
    }

    private static void renderPumpPlan(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            LegacyTileRenderPlans.PumpPlan plan, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        renderPumpPlan(definition, model, plan, poseStack, buffer, packedLight, packedOverlay, renderMode,
                blockEntity, true);
    }

    private static void renderPumpPlan(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            LegacyTileRenderPlans.PumpPlan plan, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode, BlockEntity blockEntity,
            boolean renderBase) {
        if (renderBase) {
            renderPumpPart(model, plan.basePartName(), definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        withAnimatedModelFade(blockEntity, () -> {
            renderPumpRotatingPart(model, plan.rotor(), definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
            renderPumpPivotedPart(model, plan.arms(), definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
            renderPumpTranslatedPart(model, plan.piston(), definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        });
    }

    private static void renderPumpDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            double rotorDegrees, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int activityLight, int packedOverlay, LegacyTexturedRenderMode renderMode, BlockEntity blockEntity,
            boolean renderBase) {
        if (renderBase) {
            renderPumpPart(model, "Base", definition.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, renderMode);
        }
        double radians = rotorDegrees * LegacyTileRenderPlans.DEG_TO_RAD;
        double sin = Math.sin(radians) * LegacyTileRenderPlans.PUMP_CRANK_RADIUS
                + LegacyTileRenderPlans.PUMP_CRANK_SIN_OFFSET;
        double cos = Math.cos(radians) * LegacyTileRenderPlans.PUMP_CRANK_RADIUS;
        double armAngle = Math.acos(cos / LegacyTileRenderPlans.PUMP_ARM_ROD_LENGTH)
                * LegacyTileRenderPlans.RAD_TO_DEG - 90.0D;
        double cath = Math.sqrt(1.0D + (cos * cos) / 2.0D);
        double vertical = 1.0D - cath + sin;
        withAnimatedModelFade(blockEntity, () -> {
            renderPumpRotatingPart(model, "Rotor", 0.0D, LegacyTileRenderPlans.PUMP_ROTOR_PIVOT_Y, 0.0D,
                    0.0F, 0.0F, 1.0F, rotorDegrees - 90.0D, definition.textureLocation(), poseStack, buffer,
                    activityLight, packedOverlay, renderMode);
            renderPumpPivotedPart(model, "Arms", 0.0D, vertical, 0.0D, 0.0D,
                    LegacyTileRenderPlans.PUMP_ARM_PIVOT_Y, 0.0D, 0.0F, 0.0F, -1.0F, armAngle,
                    definition.textureLocation(), poseStack, buffer, activityLight, packedOverlay, renderMode);
            renderPumpTranslatedPart(model, "Piston", 0.0D, vertical, 0.0D, definition.textureLocation(),
                    poseStack, buffer, activityLight, packedOverlay, renderMode);
        });
    }

    private static void renderPumpRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderPumpRotatingPart(model, part.partName(), part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), texture, poseStack, buffer,
                packedLight, packedOverlay, renderMode);
    }

    private static void renderPumpRotatingPart(LegacyWavefrontModel model, String partName,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        rotateAround(poseStack, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees);
        renderPumpPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderPumpPivotedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.PivotedModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderPumpPivotedPart(model, part.partName(), part.translateX(), part.translateY(), part.translateZ(),
                part.pivotX(), part.pivotY(), part.pivotZ(), part.axisX(), part.axisY(), part.axisZ(),
                part.angleDegrees(), texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    private static void renderPumpPivotedPart(LegacyWavefrontModel model, String partName,
            double translateX, double translateY, double translateZ, double pivotX, double pivotY, double pivotZ,
            float axisX, float axisY, float axisZ, double angleDegrees, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        rotateAround(poseStack, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees);
        renderPumpPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderPumpTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        if (!part.active()) {
            return;
        }
        renderPumpTranslatedPart(model, part.partName(), part.translateX(), part.translateY(), part.translateZ(),
                texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    private static void renderPumpTranslatedPart(LegacyWavefrontModel model, String partName,
            double translateX, double translateY, double translateZ, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        renderPumpPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    static void renderPumpPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.PUMP) ? pumpHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.PUMP.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
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

    private static void renderCombustionEngineDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        // The Forge OBJ block model owns Engine while chunk-baked statics are enabled.  Re-submitting it here
        // puts identical coplanar faces in the world mesh and BER, which presents as large z-fighting cracks.
        // The direct-render fallback has no baked body, so it must retain the original all-parts sequence.
        if (renderChunkBakedStaticsInBer()) {
            renderCombustionEnginePart(model, "Engine", definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        int canisterColor = blockEntity instanceof CombustionEngineBlockEntity engine
                ? engine.getCanisterColor()
                : 0xFFFFFF;
        renderCombustionEnginePart(model, "Canister", definition.textureLocation(), poseStack, buffer,
                activityLight, packedOverlay, renderMode, canisterColor >>> 16 & 255,
                canisterColor >>> 8 & 255, canisterColor & 255, 255);
        float doorAngle = blockEntity instanceof CombustionEngineBlockEntity engine
                ? engine.getDoorAngle(partialTick)
                : 0.0F;
        renderCombustionEngineRotatingPart(model, "Hatch", 1.0D, 0.0D, -2.6875D,
                0.0F, -1.0F, 0.0F, doorAngle, definition.textureLocation(), poseStack, buffer,
                activityLight, packedOverlay, renderMode);
    }

    private static void renderCombustionEngineRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderCombustionEngineRotatingPart(model, part.partName(), part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), texture, poseStack, buffer,
                packedLight, packedOverlay, renderMode);
    }

    private static void renderCombustionEngineRotatingPart(LegacyWavefrontModel model, String partName,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        rotateAround(poseStack, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees);
        renderCombustionEnginePart(model, partName, texture, poseStack, buffer, packedLight,
                packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderCombustionEnginePart(LegacyWavefrontModel model, String partName,
            ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyTexturedRenderMode renderMode) {
        renderCombustionEnginePart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay,
                renderMode, 255, 255, 255, 255);
    }

    private static void renderCombustionEnginePart(LegacyWavefrontModel model, String partName,
            ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyTexturedRenderMode renderMode, int red, int green, int blue, int alpha) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.COMBUSTION_ENGINE) ? combustionEngineHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.COMBUSTION_ENGINE.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, red, green, blue, alpha, false, renderMode,
                    LegacyWavefrontModel.UvTransform.DEFAULT, handle);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
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

    private static void renderDieselGeneratorDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        // RenderDieselGen submits Generator before its optionally vibrating Engine.  Generator is static baked
        // in the normal path and must be restored by this renderer only when static baking falls back to BER.
        if (renderChunkBakedStaticsInBer()) {
            renderDieselGeneratorPart(model, "Generator", definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        // RenderDieselGen gates the Engine vibration on acceptable fuel plus a non-empty tank.  It does not
        // consult isOn/wasOn; those fields only gate generation, the GUI flame and the engine loop.
        if (blockEntity instanceof DieselGeneratorBlockEntity diesel
                && diesel.hasAcceptableFuel() && diesel.getTank().getFill() > 0) {
            long currentMillis = System.currentTimeMillis();
            double swingSide = Math.sin(currentMillis / 50.0D) * 0.005D;
            double swingFront = Math.sin(currentMillis / 25.0D) * 0.005D;
            poseStack.pushPose();
            poseStack.translate(swingFront, 0.0D, swingSide);
            renderDieselGeneratorPart(model, "Engine", definition.textureLocation(), poseStack, buffer,
                    activityLight, packedOverlay, renderMode);
            poseStack.popPose();
            return;
        }
        renderDieselGeneratorPart(model, "Engine", definition.textureLocation(), poseStack, buffer,
                activityLight, packedOverlay, renderMode);
    }

    static void renderDieselGeneratorPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.DIESELGEN_LEGACY) ? dieselGeneratorHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.DIESELGEN_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
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

    private static void renderHeatBoilerDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        if (rendersChunkBakedHeatBoilerNormal(blockEntity.getBlockState())) {
            return;
        }
        boolean hasExploded = blockEntity instanceof BoilerBlockEntity boiler && boiler.hasExploded();
        int steamFill = blockEntity instanceof BoilerBlockEntity boiler ? boiler.getSteamFill() : 0;
        int steamMaxFill = blockEntity instanceof BoilerBlockEntity boiler ? boiler.getSteamMaxFill() : 0;
        LegacyTileRenderPlans.BoilerPlan plan = LegacyTileRenderPlans.boilerPlan(
                hasExploded, steamFill, steamMaxFill, System.currentTimeMillis());
        poseStack.pushPose();
        if (plan.overpressure()) {
            poseStack.scale((float) plan.scaleX(), (float) plan.scaleY(), (float) plan.scaleZ());
        }
        LegacyWavefrontModel selected = plan.exploded()
                ? EXTRA_MODELS.computeIfAbsent(BOILER_BURST_MODEL,
                        key -> new LegacyWavefrontModel(key, definition.textureLocation()).asVBO())
                : model;
        LegacyTexturedRenderMode boilerRenderMode = plan.cullEnabled()
                ? LegacyTexturedRenderMode.CUTOUT_CULL
                : LegacyTexturedRenderMode.CUTOUT_NO_CULL;
        renderTexturedAll(selected, definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                boilerRenderMode);
        if (HeatBoilerRenderAuditEvidence.enabled()
                && blockEntity.getBlockState().hasProperty(HeatBoilerBlock.VISUAL)) {
            HeatBoilerRenderAuditEvidence.record(
                    blockEntity.getBlockPos(),
                    blockEntity.getBlockState().getValue(HeatBoilerBlock.VISUAL).getSerializedName(),
                    plan.exploded() ? BOILER_BURST_MODEL : definition.modelLocation(),
                    packedLight,
                    false,
                    plan.cullEnabled());
        }
        poseStack.popPose();
    }

    private static void renderHephaestusDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        if (renderChunkBakedStaticsInBer()) {
            renderHephaestusPart(model, "Main", definition.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, 255, 255, 255, 255, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
        }
        float rotor = blockEntity instanceof HephaestusBlockEntity hephaestus ? hephaestus.getRotor(partialTick) : 0.0F;
        boolean active = blockEntity instanceof HephaestusBlockEntity hephaestus && hephaestus.isActive();
        withAnimatedModelFade(blockEntity, () -> {
            poseStack.pushPose();
            LegacyPoseRotations.rotateYDegrees(poseStack, rotor);
            for (int i = 0; i < 3; i++) {
                renderHephaestusPart(model, "Rotor", definition.textureLocation(), poseStack, buffer, activityLight,
                        packedOverlay, 255, 255, 255, 255, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
                LegacyPoseRotations.rotateYDegrees(poseStack, 120.0F);
            }
            poseStack.popPose();
        });

        LegacyWavefrontModel.UvTransform coreUv =
                legacyTextureMatrixUvTransform(0.5F, 0.5F, 0.0F, rotor / 10.0F);
        int coreLight = active ? LightTexture.FULL_BRIGHT : packedLight;
        int coreColor = active ? 255 : 128;
        renderHephaestusPart(model, "Core",
                active ? HEPHAESTUS_CORE_ACTIVE_TEXTURE : HEPHAESTUS_CORE_IDLE_TEXTURE,
                poseStack, buffer, coreLight, packedOverlay, coreColor, coreColor, coreColor, 255,
                renderMode, coreUv);
    }

    private static void renderHephaestusPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode,
            LegacyWavefrontModel.UvTransform uvTransform) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.HEPHAESTUS_LEGACY) ? hephaestusHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.HEPHAESTUS_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer,
                    packedLight, packedOverlay, red, green, blue, alpha, false, renderMode, uvTransform, handle);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, false, renderMode, uvTransform);
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

    private static void renderRefineryDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        boolean exploded = blockEntity instanceof RefineryBlockEntity refinery && refinery.isExploded();
        boolean tilted = blockEntity instanceof RefineryBlockEntity refinery && refinery.isTilted();
        LegacyWavefrontModel selected = exploded
                ? EXTRA_MODELS.computeIfAbsent(REFINERY_EXPLODED_MODEL,
                        key -> new LegacyWavefrontModel(key, definition.textureLocation()).asVBO())
                : model;

        poseStack.pushPose();
        if (tilted) {
            applyLegacyTilt(poseStack);
        }
        renderTexturedAll(selected, definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                renderMode);
        poseStack.popPose();
    }

    private static void renderGasFlareDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        boolean tilted = blockEntity instanceof HbmLegacyLoadedTile loadedTile && loadedTile.isTilted();
        poseStack.pushPose();
        if (tilted) {
            applyLegacyTilt(poseStack);
        }
        renderTexturedAll(model, definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                renderMode);
        poseStack.popPose();
    }

    private static void renderBlastFurnaceDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        boolean tilted = blockEntity instanceof HbmLegacyLoadedTile loadedTile && loadedTile.isTilted();
        poseStack.pushPose();
        if (tilted) {
            applyLegacyTilt(poseStack);
        }
        renderTexturedAll(model, definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                renderMode);
        poseStack.popPose();
    }

    private static void renderIntakeDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        // RenderIntake draws its static Base before the fan.  Base is normally baked with the block model and
        // has to be submitted by this renderer only in the BER-static fallback.
        if (renderChunkBakedStaticsInBer()) {
            renderIntakePart(model, "Base", definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        float fan = blockEntity instanceof IntakeBlockEntity intake ? intake.getFanSpin(partialTick) : 0.0F;
        withAnimatedModelFade(blockEntity, () -> renderIntakeRotatingPart(model, "Fan",
                0.0D, 0.0D, 0.0D, 0.0F, -1.0F, 0.0F, fan, definition.textureLocation(),
                poseStack, buffer, activityLight, packedOverlay, renderMode));
    }

    private static void renderIntakeRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderIntakeRotatingPart(model, part.partName(), part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), texture, poseStack, buffer,
                packedLight, packedOverlay, renderMode);
    }

    private static void renderIntakeRotatingPart(LegacyWavefrontModel model, String partName,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        rotateAround(poseStack, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees);
        renderIntakePart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderIntakePart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.INTAKE_LEGACY) ? intakeHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.INTAKE_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        renderTexturedPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
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

    private static void renderCrystallizerDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        if (renderChunkBakedStaticsInBer()) {
            renderCrystallizerPart(model, "Body", definition.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, renderMode);
        }
        boolean on = blockEntity instanceof ProcessingMachineBlockEntity processing && processing.isOn();
        float angle = blockEntity instanceof ProcessingMachineBlockEntity processing
                ? processing.getAngle(partialTick)
                : 0.0F;
        withAnimatedModelFade(blockEntity, () -> renderCrystallizerRotatingPart(model, "Spinner",
                0.0D, 0.0D, 0.0D, 0.0F, 1.0F, 0.0F, angle, definition.textureLocation(),
                poseStack, buffer, activityLight, packedOverlay, renderMode));
        ResourceLocation fluidTexture = blockEntity instanceof ProcessingMachineBlockEntity processing
                ? processing.getCrystallizerTank().getTankType().getTexture()
                : definition.textureLocation();
        if (on) {
            renderCrystallizerPart(model, "Fluid", fluidTexture, poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE);
        }
    }

    private static void renderCrystallizerRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderCrystallizerRotatingPart(model, part.partName(), part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), texture, poseStack, buffer,
                packedLight, packedOverlay, renderMode);
    }

    private static void renderCrystallizerRotatingPart(LegacyWavefrontModel model, String partName,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        rotateAround(poseStack, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees);
        renderCrystallizerPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay,
                renderMode);
        poseStack.popPose();
    }

    private static void renderCrystallizerPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.ACIDIZER) ? crystallizerHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.ACIDIZER.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        renderTexturedPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
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

    private static void renderCrystallizerStaticDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, boolean renderFluid) {
        renderCrystallizerPart(model, "Body", definition.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay, renderMode);
        renderCrystallizerPart(model, "Spinner", definition.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay, renderMode);
        if (renderFluid) {
            renderCrystallizerPart(model, "Fluid", definition.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE);
        }
    }

    private static void applyLegacyTilt(PoseStack poseStack) {
        poseStack.translate(0.0D, LEGACY_TILTED_TRANSLATE_Y, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, LEGACY_TILTED_ROTATION_Z);
        LegacyPoseRotations.rotateYDegrees(poseStack, LEGACY_TILTED_ROTATION_Y);
    }

    private static void renderAshpitDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        // RenderAshpit always draws Main before its moving door and state-selected inner.  With the default
        // chunk-baked static model Main is already present; the BER fallback owns that static part itself.
        if (renderChunkBakedStaticsInBer()) {
            renderFireboxHeaterPart(model, "Main", definition.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, renderMode);
        }
        float door = blockEntity instanceof AshpitBlockEntity ashpit ? ashpit.getDoorAngle(partialTick) : 0.0F;
        boolean full = blockEntity instanceof AshpitBlockEntity ashpit && ashpit.isFull();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, door * 0.75D / 135.0D);
        renderFireboxHeaterPart(model, "Door", definition.textureLocation(), poseStack, buffer, activityLight,
                packedOverlay, renderMode);
        poseStack.popPose();
        renderFireboxHeaterPart(model, full ? "InnerBurning" : "Inner", definition.textureLocation(),
                poseStack, buffer, activityLight, packedOverlay, renderMode);
    }

    private static void renderArcWelderDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight,
            int packedOverlay, LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        // RenderArcWelder always submits its complete OBJ before the optional display item.  The chunk model
        // owns that body normally; the prepared BER model restores it only while baked statics are disabled.
        if (renderChunkBakedStaticsInBer()) {
            model.renderAll(definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay, renderMode);
        }
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
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, -90.0F);
        poseStack.scale(1.5F, 1.5F, 1.5F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED,
                activityLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, blockEntity.getLevel(), 0);
        poseStack.popPose();
    }

    private static void renderAmmoPressDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        // RenderAmmoPress submits its static Frame before the translated press, shells, and bullets.  Frame is
        // normally baked into the block model and belongs to this renderer only in the BER-static fallback.
        if (renderChunkBakedStaticsInBer()) {
            renderAmmoPressPart(model, "Frame", definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        float press = blockEntity instanceof AmmoPressBlockEntity ammoPress ? ammoPress.getPress(partialTick) : 0.0F;
        float lift = blockEntity instanceof AmmoPressBlockEntity ammoPress ? ammoPress.getLift(partialTick) : 0.0F;
        boolean bullets = blockEntity instanceof AmmoPressBlockEntity ammoPress && ammoPress.shouldRenderBullets();

        renderAmmoPressTranslatedPart(model, "Press", true, 0.0D, -press * 0.25D, 0.0D,
                definition.textureLocation(), poseStack, buffer, activityLight, packedOverlay, renderMode);
        renderAmmoPressTranslatedPart(model, "Shells", true, 0.0D, lift * 0.5D - 0.5D, 0.0D,
                definition.textureLocation(), poseStack, buffer, activityLight, packedOverlay, renderMode);
        renderAmmoPressTranslatedPart(model, "Bullets", bullets, 0.0D, lift * 0.5D - 0.5D, 0.0D,
                definition.textureLocation(), poseStack, buffer, activityLight, packedOverlay, renderMode);
    }

    private static void renderAmmoPressTranslatedPart(LegacyWavefrontModel model, String partName,
            boolean active, double translateX, double translateY, double translateZ, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        if (!active) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        renderAmmoPressPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderAmmoPressPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.AMMO_PRESS) ? ammoPressHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.AMMO_PRESS.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        renderTexturedPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
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

    private static void renderMixerDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        // RenderMixer submits Main before the rotating mixer and optional translucent fluid.  Main remains baked
        // during normal rendering and is owned here only when static rendering falls back to the BER.
        if (renderChunkBakedStaticsInBer()) {
            renderMixerPart(model, "Main", definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        float rotation = blockEntity instanceof MixerBlockEntity mixer ? mixer.getRotation(partialTick) : 0.0F;
        withAnimatedModelFade(blockEntity, () -> renderMixerRotatingPart(model, "Mixer",
                0.0D, 0.0D, 0.0D, 0.0F, -1.0F, 0.0F, rotation, definition.textureLocation(),
                poseStack, buffer, activityLight, packedOverlay, renderMode));

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
        LegacyMachineEffectPresenter.enqueueTexturedObjPartGroup(PresentStage.AFTER_BLOCK_ENTITIES,
                poseStack, buffer, parts -> parts.add(ObjMachineModels.MIXER, MIXER_FLUID, WHITE_TEXTURE,
                        packedLight, packedOverlay, color >>> 16 & 255, color >>> 8 & 255, color & 255, 191,
                        false, LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE,
                        LegacyWavefrontModel.UvTransform.DEFAULT));
        poseStack.popPose();
    }

    private static void renderMixerRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderMixerRotatingPart(model, part.partName(), part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), texture, poseStack, buffer,
                packedLight, packedOverlay, renderMode);
    }

    private static void renderMixerRotatingPart(LegacyWavefrontModel model, String partName,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        rotateAround(poseStack, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees);
        renderMixerPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderMixerPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.MIXER) ? mixerHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.MIXER.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        renderTexturedPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
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

    private static void renderCombinationOvenDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        // RenderFurnaceCombination always submits the complete static oven before the optional fire billboard.
        // The chunk model owns that body normally; the BER-static fallback must restore it even while idle.
        if (renderChunkBakedStaticsInBer()) {
            model.renderAll(definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay, renderMode);
        }
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
        LegacyPoseRotations.rotateYDegrees(poseStack, -yRot);
        LegacyMachineEffectPresenter.enqueueTexturedQuad(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                COMBINATION_FIRE_TEXTURE, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                LightTexture.FULL_BRIGHT, packedOverlay, 0.0F, 1.0F, 0.0F,
                -1.0D, 0.0D, 0.0D, uMax, 1.0D,
                -1.0D, 3.0D, 0.0D, uMax, 0.0D,
                1.0D, 3.0D, 0.0D, uMin, 0.0D,
                1.0D, 0.0D, 0.0D, uMin, 1.0D,
                0xFFFFFF, 255);
        poseStack.popPose();
    }

    private static void renderFurnaceIronDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        // RenderFurnaceIron submits the static shell before its mutually-exclusive On/Off indicator.
        // MODEL rendering owns Main normally; the BER-static fallback must restore the legacy sequence itself.
        if (renderChunkBakedStaticsInBer()) {
            renderFurnaceIronPart(model, "Main", definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        if (blockEntity instanceof LegacyFurnaceBlockEntity furnace && furnace.wasOn()) {
            renderFurnaceIronPart(model, "On", definition.textureLocation(), poseStack, buffer,
                    LightTexture.FULL_BRIGHT, packedOverlay, renderMode);
            return;
        }
        renderFurnaceIronPart(model, "Off", definition.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay, renderMode);
    }

    private static void renderFurnaceIronPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.FURNACE_IRON_LEGACY) ? furnaceIronHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.FURNACE_IRON_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer,
                    packedLight, packedOverlay, handle, renderMode);
            return;
        }
        renderTexturedPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    private static LegacyWavefrontModel.SelectionHandle furnaceIronHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Main" -> FURNACE_IRON_MAIN;
            case "On" -> FURNACE_IRON_ON;
            case "Off" -> FURNACE_IRON_OFF;
            default -> null;
        };
    }

    private static void renderFurnaceSteelDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity) {
        // RenderFurnaceSteel always submits its complete static shell before the optional additive fire glow.
        // Keep the shell baked normally and restore it from the prepared BER model only in fallback mode.
        if (renderChunkBakedStaticsInBer()) {
            model.renderAll(definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay, renderMode);
        }
        if (!(blockEntity instanceof LegacyFurnaceBlockEntity furnace) || !furnace.wasOn()) {
            return;
        }

        float col = (float) Math.sin(System.currentTimeMillis() * 0.001D);
        int red = Math.round((0.875F + col * 0.125F) * 255.0F);
        int green = Math.round((0.625F + col * 0.375F) * 255.0F);
        int color = red << 16 | green << 8;
        LegacyMachineEffectPresenter.enqueueUntexturedQuadGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 128, group -> {
            for (int i = 0; i < 4; i++) {
                double x = 1.0D + i * 0.0625D;
                group.add(
                        x, 1.0D, -1.0D,
                        x, 1.0D, 1.0D,
                        x, 0.5D, 1.0D,
                        x, 0.5D, -1.0D,
                        color, 128, 128, 128, 128);
            }
        });
    }

    private static void renderSawmillDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        // RenderSawmill submits Main before its blade and gear rotations.  Keep Main with the baked OBJ normally,
        // but restore it when the client uses the BER-static fallback.
        if (renderChunkBakedStaticsInBer()) {
            renderSawmillPart(model, "Main", definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        float rot = blockEntity instanceof SawmillBlockEntity sawmill ? sawmill.getSpin(partialTick) : 0.0F;
        boolean hasBlade = !(blockEntity instanceof SawmillBlockEntity sawmill) || sawmill.hasBlade();

        withAnimatedModelFade(blockEntity, () -> {
            if (hasBlade) {
                renderSawmillRotatingPart(model, "Blade", 0.0D, 1.375D, 0.0D,
                        0.0F, 0.0F, 1.0F, -rot * 2.0F, definition.textureLocation(), poseStack,
                        buffer, activityLight, packedOverlay, renderMode);
            }
            renderSawmillRotatingPart(model, "GearLeft", 0.5625D, 1.375D, 0.0D,
                    0.0F, 0.0F, 1.0F, rot, definition.textureLocation(), poseStack, buffer,
                    activityLight, packedOverlay, renderMode);
            renderSawmillRotatingPart(model, "GearRight", -0.5625D, 1.375D, 0.0D,
                    0.0F, 0.0F, 1.0F, -rot, definition.textureLocation(), poseStack, buffer,
                    activityLight, packedOverlay, renderMode);
        });
    }

    private static void renderSawmillRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderSawmillRotatingPart(model, part.partName(), part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), texture, poseStack, buffer,
                packedLight, packedOverlay, renderMode);
    }

    private static void renderSawmillRotatingPart(LegacyWavefrontModel model, String partName,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        rotateAround(poseStack, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees);
        renderSawmillPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderStirlingDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        // RenderStirling always renders its static Base before cog and piston motion.  Base normally belongs to
        // the baked OBJ; restore it here only when the client selects the BER-static fallback.
        if (renderChunkBakedStaticsInBer()) {
            renderStirlingPart(model, "Base", definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        float rot = blockEntity instanceof StirlingBlockEntity stirling ? stirling.getSpin(partialTick) : 0.0F;
        boolean hasCog = !(blockEntity instanceof StirlingBlockEntity stirling) || stirling.hasCog();

        withAnimatedModelFade(blockEntity, () -> {
            if (hasCog) {
                renderStirlingRotatingPart(model, "Cog", 0.0D, 1.375D, 0.0D,
                        0.0F, 0.0F, -1.0F, rot, definition.textureLocation(), poseStack, buffer,
                        activityLight, packedOverlay, renderMode);
            }
            renderStirlingRotatingPart(model, "CogSmall", 0.0D, 1.375D, 0.25D,
                    1.0F, 0.0F, 0.0F, rot * 2.0F + 3.0F, definition.textureLocation(), poseStack,
                    buffer, activityLight, packedOverlay, renderMode);
            renderStirlingTranslatedPart(model, "Piston", true,
                    Math.sin(rot * Math.PI / 90.0D) * 0.25D + 0.125D, 0.0D, 0.0D,
                    definition.textureLocation(), poseStack, buffer, activityLight, packedOverlay, renderMode);
        });
    }

    private static void renderStirlingRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderStirlingRotatingPart(model, part.partName(), part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), texture, poseStack, buffer,
                packedLight, packedOverlay, renderMode);
    }

    private static void renderStirlingRotatingPart(LegacyWavefrontModel model, String partName,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        rotateAround(poseStack, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees);
        renderStirlingPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderStirlingTranslatedPart(LegacyWavefrontModel model, String partName,
            boolean active, double translateX, double translateY, double translateZ, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        if (!active) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        renderStirlingPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderSawmillPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.SAWMILL) ? sawmillHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.SAWMILL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        renderTexturedPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
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
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.STIRLING) ? stirlingHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.STIRLING.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        renderTexturedPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
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

    private static void renderPrecassDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        boolean frame = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                && machine.shouldRenderFrame();
        double ring = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                ? machine.getPrecassRing(partialTick)
                : 0.0D;
        double[] arm = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                ? machine.getPrecassArm(partialTick)
                : new double[] { 45.0D, -30.0D, 45.0D };

        AssemblyMachineRenderer.renderModelPart(model, "Base", definition.textureLocation(), poseStack,
                buffer, packedLight, packedOverlay, renderMode);
        if (frame) {
            AssemblyMachineRenderer.renderModelPart(model, "Frame", definition.textureLocation(), poseStack,
                    buffer, packedLight, packedOverlay, renderMode);
        }

        withAnimatedModelFade(blockEntity, () -> {
            poseStack.pushPose();
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) ring);
            AssemblyMachineRenderer.renderModelPart(model, "Ring", definition.textureLocation(), poseStack, buffer,
                    activityLight, packedOverlay, renderMode);
            AssemblyMachineRenderer.renderModelPart(model, "Ring2", definition.textureLocation(), poseStack, buffer,
                    activityLight, packedOverlay, renderMode);
            for (int i = 0; i < 4; i++) {
                double striker = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                        ? machine.getPrecassStriker(i, partialTick)
                        : 0.0D;
                renderPrecassArmDirect(definition, model, poseStack, buffer, activityLight, packedOverlay, renderMode,
                        arm, striker);
                LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
            }
            poseStack.popPose();
        });

        if (blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine) {
            double iconDistanceSq = LegacyRecipeIconRenderer.playerDistanceSq(machine);
            if (LegacyRecipeIconRenderer.shouldRenderAtDistance(iconDistanceSq)) {
                LegacyRecipeIconRenderer.renderInLegacyMachineSpace(machine.getSelectedRecipeDefinition(),
                        machine.getLevel(), poseStack, buffer, activityLight);
            }
        }
    }

    private static void renderPrecassArmDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, double[] arm, double striker) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.625D, 0.9375D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) arm[0]);
        poseStack.translate(0.0D, -1.625D, -0.9375D);
        AssemblyMachineRenderer.renderModelPart(model, "ArmLower1", definition.textureLocation(), poseStack,
                buffer, packedLight, packedOverlay, renderMode);

        poseStack.translate(0.0D, 2.375D, 0.9375D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) arm[1]);
        poseStack.translate(0.0D, -2.375D, -0.9375D);
        AssemblyMachineRenderer.renderModelPart(model, "ArmUpper1", definition.textureLocation(), poseStack,
                buffer, packedLight, packedOverlay, renderMode);

        poseStack.translate(0.0D, 2.375D, 0.4375D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) arm[2]);
        poseStack.translate(0.0D, -2.375D, -0.4375D);
        AssemblyMachineRenderer.renderModelPart(model, "Head1", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        poseStack.translate(0.0D, striker, 0.0D);
        AssemblyMachineRenderer.renderModelPart(model, "Spike1", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderPurexDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        boolean frame = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                && machine.shouldRenderFrame();
        float anim = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                ? machine.getPurexAnim(partialTick)
                : 0.0F;

        renderPurexPart(model, "Base", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        if (frame) {
            renderPurexPart(model, "Frame", definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
        withAnimatedModelFade(blockEntity, () -> {
            renderPurexRotatingPart(model, "Fan", 1.5D, 1.25D, 0.0D,
                    0.0F, 0.0F, 1.0F, anim * 45.0F, definition.textureLocation(), poseStack,
                    buffer, activityLight, packedOverlay, renderMode);
            renderPurexTranslatedPart(model, "Pump", true, BobMathUtil.sps(anim * 0.25D) * 0.5D,
                    0.0D, 0.0D, definition.textureLocation(), poseStack, buffer, activityLight,
                    packedOverlay, renderMode);
        });
    }

    private static void renderPurexRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderPurexRotatingPart(model, part.partName(), part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), texture, poseStack, buffer,
                packedLight, packedOverlay, renderMode);
    }

    private static void renderPurexRotatingPart(LegacyWavefrontModel model, String partName,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        rotateAround(poseStack, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees);
        renderPurexPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderPurexTranslatedPart(LegacyWavefrontModel model, String partName,
            boolean active, double translateX, double translateY, double translateZ, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        if (!active) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        renderPurexPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    static void renderPurexPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, ObjMachineModels.PUREX) ? purexHandle(partName) : null;
        if (handle != null) {
            ObjMachineModels.PUREX.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                    packedOverlay, handle, renderMode);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
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

    private static void renderFireboxHeaterDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int activityLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, BlockEntity blockEntity, float partialTick) {
        // Both legacy RenderFirebox and RenderAshpit-style oven rendering submit Main before dynamic parts.
        // Main comes from the chunk-baked OBJ normally, but belongs to this renderer in the BER fallback.
        if (renderChunkBakedStaticsInBer()) {
            renderFireboxHeaterPart(model, "Main", definition.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, renderMode);
        }
        FireboxHeaterBlockEntity.Kind kind = blockEntity instanceof FireboxHeaterBlockEntity firebox
                ? firebox.kind()
                : FireboxHeaterBlockEntity.Kind.FIREBOX;
        float door = blockEntity instanceof FireboxHeaterBlockEntity firebox
                ? firebox.getDoorAngle(partialTick)
                : 0.0F;
        boolean burning = blockEntity instanceof FireboxHeaterBlockEntity firebox && firebox.wasOn();

        if (kind == FireboxHeaterBlockEntity.Kind.OVEN) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, door * 0.75D / 135.0D);
            renderFireboxHeaterPart(model, "Door", definition.textureLocation(), poseStack, buffer,
                    packedOverlay, partRenderState(definition, "Door", activityLight, renderMode, false));
            poseStack.popPose();
            String innerPart = burning ? "InnerBurning" : "Inner";
            renderFireboxHeaterPart(model, innerPart, definition.textureLocation(), poseStack, buffer,
                    packedOverlay, partRenderState(definition, innerPart, activityLight, renderMode, burning));
            return;
        }

        poseStack.pushPose();
        poseStack.translate(1.375D, 0.0D, 0.375D);
        LegacyPoseRotations.rotateYDegrees(poseStack, -(door));
        poseStack.translate(-1.375D, 0.0D, -0.375D);
        renderFireboxHeaterPart(model, "Door", definition.textureLocation(), poseStack, buffer,
                packedOverlay, partRenderState(definition, "Door", activityLight, renderMode, false));
        poseStack.popPose();
        String innerPart = burning ? "InnerBurning" : "InnerEmpty";
        renderFireboxHeaterPart(model, innerPart, definition.textureLocation(), poseStack, buffer,
                packedOverlay, partRenderState(definition, innerPart, activityLight, renderMode, burning));
    }

    private static void renderFireboxHeaterPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        if (sameModel(model, ObjMachineModels.FIREBOX_LEGACY)) {
            LegacyWavefrontModel.SelectionHandle handle = fireboxHandle(partName);
            if (handle != null) {
                ObjMachineModels.FIREBOX_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight,
                        packedOverlay, handle, renderMode);
                return;
            }
        }
        if (sameModel(model, ObjMachineModels.HEATING_OVEN_LEGACY)) {
            LegacyWavefrontModel.SelectionHandle handle = heatingOvenHandle(partName);
            if (handle != null) {
                ObjMachineModels.HEATING_OVEN_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer,
                        packedLight, packedOverlay, handle, renderMode);
                return;
            }
        }
        renderTexturedPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    private static void renderFireboxHeaterPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedOverlay, DirectPartRenderState state) {
        if (sameModel(model, ObjMachineModels.FIREBOX_LEGACY)) {
            LegacyWavefrontModel.SelectionHandle handle = fireboxHandle(partName);
            if (handle != null) {
                ObjMachineModels.FIREBOX_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer,
                        state.packedLight(), packedOverlay, state.red(), state.green(), state.blue(), state.alpha(),
                        false, state.renderMode(), LegacyWavefrontModel.UvTransform.DEFAULT, handle);
                return;
            }
        }
        if (sameModel(model, ObjMachineModels.HEATING_OVEN_LEGACY)) {
            LegacyWavefrontModel.SelectionHandle handle = heatingOvenHandle(partName);
            if (handle != null) {
                ObjMachineModels.HEATING_OVEN_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer,
                        state.packedLight(), packedOverlay, state.red(), state.green(), state.blue(), state.alpha(),
                        false, state.renderMode(), LegacyWavefrontModel.UvTransform.DEFAULT, handle);
                return;
            }
        }
        model.renderPart(partName, texture, poseStack, buffer, state.packedLight(), packedOverlay,
                state.red(), state.green(), state.blue(), state.alpha(), false, state.renderMode(),
                LegacyWavefrontModel.UvTransform.DEFAULT);
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
            LegacyTileRenderPlans.VisibleMachineStaticPlan plan, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        renderVisibleMachineStaticPlanPass(definition, model, plan, poseStack, buffer,
                packedLight, packedOverlay, renderMode, false);
        renderVisibleMachineStaticPlanPass(definition, model, plan, poseStack, buffer,
                packedLight, packedOverlay, renderMode, true);
    }

    private static void renderVisibleMachineStaticPlanPass(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            LegacyTileRenderPlans.VisibleMachineStaticPlan plan, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode, boolean translucentPass) {
        for (LegacyTileRenderPlans.ModelPartTintPlan part : plan.parts()) {
            if (plannedPartTranslucent(part) != translucentPass) {
                continue;
            }
            renderPlannedPart(model, definition.textureLocation(), part, poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
        }
    }

    private static boolean plannedPartTranslucent(LegacyTileRenderPlans.ModelPartTintPlan plan) {
        return plan != null
                && plan.blend() != null
                && plan.blend().modernRenderMode().translucent();
    }

    private static void renderPlannedPart(LegacyWavefrontModel model, net.minecraft.resources.ResourceLocation texture,
            LegacyTileRenderPlans.ModelPartTintPlan plan, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        renderPreparedPlannedPart(model, texture, plan, poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    private static void renderPreparedPlannedPart(LegacyWavefrontModel model,
            net.minecraft.resources.ResourceLocation texture, LegacyTileRenderPlans.ModelPartTintPlan plan,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        if (!plan.active()) {
            return;
        }
        LegacyTileRenderPlans.RgbaPlan color = plan.color();
        int red = color != null ? color.redByte() : 255;
        int green = color != null ? color.greenByte() : 255;
        int blue = color != null ? color.blueByte() : 255;
        int alpha = color != null ? color.alphaByte() : 255;
        int resolvedLight = plan.fullbright() != null
                ? LegacyTexturedQuadRenderer.legacyLightmap(plan.fullbright().lightmapX(), plan.fullbright().lightmapY())
                : packedLight;
        LegacyTexturedRenderMode resolvedRenderMode = plan.blend() != null
                ? plan.blend().modernRenderMode()
                : renderMode;
        if (tryRenderPreparedPlannedPart(model, texture, plan, poseStack, buffer, resolvedLight, packedOverlay,
                red, green, blue, alpha, resolvedRenderMode)) {
            return;
        }
        if (plan.textured()) {
            model.renderPart(plan.partName(), texture, poseStack, buffer, resolvedLight, packedOverlay,
                    red, green, blue, alpha, false, resolvedRenderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
        } else {
            model.renderPartUntextured(plan.partName(), poseStack, buffer, red, green, blue, alpha,
                    resolvedRenderMode);
        }
    }

    private static boolean tryRenderPreparedPlannedPart(LegacyWavefrontModel model, ResourceLocation texture,
            LegacyTileRenderPlans.ModelPartTintPlan plan, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            LegacyTexturedRenderMode renderMode) {
        if (plan.textured() && sameModel(model, ObjMachineModels.BATTERY_REDD_LEGACY)) {
            LegacyWavefrontModel.SelectionHandle handle = batteryReddHandle(plan.partName());
            if (handle != null) {
                ObjMachineModels.BATTERY_REDD_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer,
                        packedLight, packedOverlay, red, green, blue, alpha, false, renderMode,
                        LegacyWavefrontModel.UvTransform.DEFAULT, handle);
                return true;
            }
        }
        if (sameModel(model, ObjMachineModels.ACIDIZER)) {
            LegacyWavefrontModel.SelectionHandle handle = crystallizerHandle(plan.partName());
            if (handle != null) {
                if (plan.textured()) {
                    ObjMachineModels.ACIDIZER.renderOnlyInCallOrder(texture, poseStack, buffer,
                            packedLight, packedOverlay, red, green, blue, alpha, false, renderMode,
                            LegacyWavefrontModel.UvTransform.DEFAULT, handle);
                } else {
                    ObjMachineModels.ACIDIZER.renderOnlyUntextured(poseStack, buffer,
                            red, green, blue, alpha, renderMode, handle);
                }
                return true;
            }
        }
        if (sameModel(model, ObjMachineModels.RADGEN)) {
            LegacyWavefrontModel.SelectionHandle handle = radGenHandle(plan.partName());
            if (handle != null) {
                if (plan.textured()) {
                    ObjMachineModels.RADGEN.renderOnlyInCallOrder(texture, poseStack, buffer,
                            packedLight, packedOverlay, red, green, blue, alpha, false, renderMode,
                            LegacyWavefrontModel.UvTransform.DEFAULT, handle);
                } else if ("Glass".equals(plan.partName())) {
                    ObjMachineModels.RADGEN.renderOnlyInCallOrder(WHITE_TEXTURE, poseStack, buffer,
                            packedLight, packedOverlay, red, green, blue, alpha, false, renderMode,
                            LegacyWavefrontModel.UvTransform.DEFAULT, handle);
                } else {
                    ObjMachineModels.RADGEN.renderOnlyUntextured(poseStack, buffer,
                            red, green, blue, alpha, renderMode, handle);
                }
                return true;
            }
        }
        return false;
    }

    static void renderTexturedAll(LegacyWavefrontModel model, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        model.renderAll(texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
    }

    static void renderTexturedPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
    }

    private static LegacyWavefrontModel.UvTransform legacyTextureMatrixUvTransform(float uScale, float vScale,
            float uTranslate, float vTranslate) {
        return legacyTextureMatrixUvTransform(uScale, vScale, 0.0F, uTranslate, vTranslate);
    }

    private static LegacyWavefrontModel.UvTransform legacyTextureMatrixUvTransform(float uScale, float vScale,
            float rotationDegrees, float uTranslate, float vTranslate) {
        float radians = (float) (rotationDegrees * LegacyTileRenderPlans.DEG_TO_RAD);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        return LegacyWavefrontModel.UvTransform.dynamic(
                uScale * cos,
                -uScale * sin,
                vScale * sin,
                vScale * cos,
                uScale * (cos * uTranslate - sin * vTranslate),
                vScale * (sin * uTranslate + cos * vTranslate),
                0.0F);
    }

    private static boolean sameModel(LegacyWavefrontModel model, LegacyWavefrontModel expected) {
        return model == expected || model.modelLocation().equals(expected.modelLocation());
    }

    private record DirectPartRenderState(int packedLight, int red, int green, int blue, int alpha,
            LegacyTexturedRenderMode renderMode) {
    }

    public record VisibleMachineRouteCoverage(
            int blockCount,
            int definitionCount,
            int defaultRenderAllDefinitions,
            int defaultPartDefinitions,
            int profileRenderAllDefinitions,
            int profilePartDefinitions,
            int profileDirectDefinitions,
            int profileFallbackDefinitions,
            int itemPartDefinitions,
            int partPropertyDefinitions) {
    }

    private static void renderNormalTexturedQuad(ResourceLocation texture,
            LegacyTileRenderPlans.NormalTexturedQuadPlan quad, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            LegacyTexturedRenderMode renderMode) {
        if (quad.vertices().size() != 4) {
            return;
        }
        int color = red << 16 | green << 8 | blue;
        LegacyTileRenderPlans.NormalTexturedVertexPlan v0 = quad.vertices().get(0);
        LegacyTileRenderPlans.NormalTexturedVertexPlan v1 = quad.vertices().get(1);
        LegacyTileRenderPlans.NormalTexturedVertexPlan v2 = quad.vertices().get(2);
        LegacyTileRenderPlans.NormalTexturedVertexPlan v3 = quad.vertices().get(3);
        LegacyTexturedQuadRenderer.quadDirect(texture, poseStack, buffer, packedLight, packedOverlay, renderMode,
                quad.normalX(), quad.normalY(), quad.normalZ(),
                v0.x(), v0.y(), v0.z(), v0.u(), v0.v(),
                v1.x(), v1.y(), v1.z(), v1.u(), v1.v(),
                v2.x(), v2.y(), v2.z(), v2.u(), v2.v(),
                v3.x(), v3.y(), v3.z(), v3.u(), v3.v(),
                color, alpha);
    }

    private static void rotate(PoseStack poseStack, float axisX, float axisY, float axisZ, double degrees) {
        if (axisX != 0.0F) {
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) (degrees * axisX));
        }
        if (axisY != 0.0F) {
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) (degrees * axisY));
        }
        if (axisZ != 0.0F) {
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) (degrees * axisZ));
        }
    }

    private static void rotateAround(PoseStack poseStack, double pivotX, double pivotY, double pivotZ,
            float axisX, float axisY, float axisZ, double degrees) {
        poseStack.translate(pivotX, pivotY, pivotZ);
        rotate(poseStack, axisX, axisY, axisZ, degrees);
        poseStack.translate(-pivotX, -pivotY, -pivotZ);
    }
}
