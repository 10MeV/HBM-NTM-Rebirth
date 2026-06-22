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
                model.renderPart("Annihilator", definition.textureLocation(), context);
                renderRotatingPart(model, LegacyTileRenderPlans.annihilatorRollerPlan(currentMillis), context, poseStack);
                LegacyTileRenderPlans.TextureMatrixPartPlan belt = LegacyTileRenderPlans.annihilatorBeltPlan(currentMillis);
                model.renderPart(belt.partName(), definition.partTextures().getOrDefault(belt.partName(),
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
        model.renderPart("Body", definition.textureLocation(), context);
        boolean plugged = true;
        for (int index = 0; index < 4; index++) {
            boolean filled = blockEntity instanceof CyclotronBlockEntity cyclotron && cyclotron.getPlug(index);
            plugged &= filled;
            ResourceLocation texture = filled ? CYCLOTRON_FILLED_TEXTURES[index] : CYCLOTRON_EMPTY_TEXTURES[index];
            model.renderPart("B" + (index + 1), texture, context);
        }
        if (plugged) {
            renderCyclotronGlyphRing(context.poseStack(), context.buffer());
        }
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
        model.renderPart("Furnace", definition.textureLocation(), context);
        float offset = blockEntity instanceof RotaryFurnaceBlockEntity furnace
                ? furnace.getPistonOffset(partialTick)
                : 0.0F;
        renderTranslatedPart(model, new LegacyTileRenderPlans.TranslatedModelPartPlan(
                "rotary_furnace_piston", "Piston", true, 0.0D, offset, 0.0D), context, poseStack);
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
        model.renderPart(plan.bodyPartName(), definition.textureLocation(), context);
        LegacyTileRenderPlans.TranslatedModelPartPlan pump = plan.pump();
        if (pump != null && pump.active()) {
            poseStack.pushPose();
            poseStack.translate(pump.translateX(), pump.translateY(), pump.translateZ());
            model.renderPart(pump.partName(), definition.textureLocation(), context);
            poseStack.popPose();
        }
        for (LegacyTileRenderPlans.RotatingModelPartPlan fan : plan.fans()) {
            renderRotatingPart(model, fan, context, poseStack);
        }
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
        model.renderPart("Base", definition.textureLocation(), context);
        poseStack.pushPose();
        poseStack.translate(0.0D, 5.5D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.translate(0.0D, -5.5D, 0.0D);
        model.renderPart("Wheel", definition.textureLocation(), context);
        renderPlannedPart(model, definition.textureLocation(),
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
            model.renderPart(layer.partName(), texture, layerContext);
        }
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
        model.renderPart(plan.basePartName(), definition.textureLocation(), context);
        renderRotatingPart(model, plan.rotor(), context, poseStack);
        renderPivotedPart(model, plan.arms(), context, poseStack);
        renderTranslatedPart(model, plan.piston(), context, poseStack);
    }

    private static void renderCombustionEngine(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        model.renderPart("Engine", definition.textureLocation(), context);
        ObjRenderContext canisterContext = context;
        if (blockEntity instanceof CombustionEngineBlockEntity engine) {
            int color = engine.getCanisterColor();
            canisterContext = context.withRgb(color >>> 16 & 255, color >>> 8 & 255, color & 255);
        }
        model.renderPart("Canister", definition.textureLocation(), canisterContext);
        float doorAngle = blockEntity instanceof CombustionEngineBlockEntity engine
                ? engine.getDoorAngle(partialTick)
                : 0.0F;
        renderRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "combustion_engine_hatch", "Hatch", 1.0D, 0.0D, -2.6875D, 0.0F, -1.0F, 0.0F,
                doorAngle), context, poseStack);
    }

    private static void renderDieselGenerator(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        model.renderPart("Generator", definition.textureLocation(), context);
        if (blockEntity instanceof DieselGeneratorBlockEntity diesel && diesel.isBurning()) {
            long currentMillis = System.currentTimeMillis();
            double swingSide = Math.sin(currentMillis / 50.0D) * 0.005D;
            double swingFront = Math.sin(currentMillis / 25.0D) * 0.005D;
            poseStack.pushPose();
            poseStack.translate(swingFront, 0.0D, swingSide);
            model.renderPart("Engine", definition.textureLocation(), context);
            poseStack.popPose();
            return;
        }
        model.renderPart("Engine", definition.textureLocation(), context);
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
        model.renderPart("Main", definition.textureLocation(), context);
        float rotor = blockEntity instanceof HephaestusBlockEntity hephaestus ? hephaestus.getRotor(partialTick) : 0.0F;
        boolean active = blockEntity instanceof HephaestusBlockEntity hephaestus && hephaestus.isActive();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotor));
        for (int i = 0; i < 3; i++) {
            model.renderPart("Rotor", definition.textureLocation(), context);
            poseStack.mulPose(Axis.YP.rotationDegrees(120.0F));
        }
        poseStack.popPose();

        ObjRenderContext coreContext = active
                ? context.fullBright().withLegacyTextureMatrix(0.5F, 0.5F, 0.0F, rotor / 10.0F)
                : context.withColor(0x808080).withLegacyTextureMatrix(0.5F, 0.5F, 0.0F, rotor / 10.0F);
        model.renderPart("Core", active ? HEPHAESTUS_CORE_ACTIVE_TEXTURE : HEPHAESTUS_CORE_IDLE_TEXTURE,
                coreContext);
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
        model.renderPart("Base", definition.textureLocation(), context);
        renderRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "intake_fan", "Fan", 0.0D, 0.0D, 0.0D, 0.0F, -1.0F, 0.0F,
                fan), context, poseStack);
    }

    private static void renderCrystallizer(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        boolean on = blockEntity instanceof ProcessingMachineBlockEntity processing && processing.isOn();
        float angle = blockEntity instanceof ProcessingMachineBlockEntity processing
                ? processing.getAngle(partialTick)
                : 0.0F;
        model.renderPart("Body", definition.textureLocation(), context);
        renderRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "crystallizer_spinner", "Spinner", 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, 0.0F,
                angle), context, poseStack);
        ResourceLocation fluidTexture = blockEntity instanceof ProcessingMachineBlockEntity processing
                ? processing.getCrystallizerTank().getTankType().getTexture()
                : definition.textureLocation();
        renderPlannedPart(model, fluidTexture, LegacyTileRenderPlans.crystallizerFluidPlan(on), context);
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

        model.renderPart("Frame", definition.textureLocation(), context);
        renderTranslatedPart(model, new LegacyTileRenderPlans.TranslatedModelPartPlan(
                "ammo_press_press", "Press", true,
                0.0D, -press * 0.25D, 0.0D), context, poseStack);
        renderTranslatedPart(model, new LegacyTileRenderPlans.TranslatedModelPartPlan(
                "ammo_press_shells", "Shells", true,
                0.0D, lift * 0.5D - 0.5D, 0.0D), context, poseStack);
        renderTranslatedPart(model, new LegacyTileRenderPlans.TranslatedModelPartPlan(
                "ammo_press_bullets", "Bullets", bullets,
                0.0D, lift * 0.5D - 0.5D, 0.0D), context, poseStack);
    }

    private static void renderMixer(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        model.renderPart("Main", definition.textureLocation(), context);
        float rotation = blockEntity instanceof MixerBlockEntity mixer ? mixer.getRotation(partialTick) : 0.0F;
        renderRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "mixer_blade", "Mixer", 0.0D, 0.0D, 0.0D, 0.0F, -1.0F, 0.0F,
                rotation), context, poseStack);

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
        model.renderPartUntextured("Fluid", context.withRgba(color >>> 16 & 255, color >>> 8 & 255,
                color & 255, 191).withTranslucencyNoDepthWrite());
        poseStack.popPose();
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

        model.renderPart("Base", definition.textureLocation(), context);
        if (hasCog) {
            renderRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                    "stirling_cog", "Cog", 0.0D, 1.375D, 0.0D, 0.0F, 0.0F, -1.0F,
                    rot), context, poseStack);
        }
        renderRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "stirling_cog_small", "CogSmall", 0.0D, 1.375D, 0.25D, 1.0F, 0.0F, 0.0F,
                rot * 2.0F + 3.0F), context, poseStack);
        renderTranslatedPart(model, new LegacyTileRenderPlans.TranslatedModelPartPlan(
                "stirling_piston", "Piston", true,
                Math.sin(rot * Math.PI / 90.0D) * 0.25D + 0.125D, 0.0D, 0.0D), context, poseStack);
    }

    private static void renderSawmill(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        float rot = blockEntity instanceof SawmillBlockEntity sawmill ? sawmill.getSpin(partialTick) : 0.0F;
        boolean hasBlade = !(blockEntity instanceof SawmillBlockEntity sawmill) || sawmill.hasBlade();

        model.renderPart("Main", definition.textureLocation(), context);
        if (hasBlade) {
            renderRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                    "sawmill_blade", "Blade", 0.0D, 1.375D, 0.0D, 0.0F, 0.0F, 1.0F,
                    -rot * 2.0F), context, poseStack);
        }
        renderRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "sawmill_gear_left", "GearLeft", 0.5625D, 1.375D, 0.0D, 0.0F, 0.0F, 1.0F,
                rot), context, poseStack);
        renderRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "sawmill_gear_right", "GearRight", -0.5625D, 1.375D, 0.0D, 0.0F, 0.0F, 1.0F,
                -rot), context, poseStack);
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

        model.renderPart("Base", definition.textureLocation(), context);
        if (frame) {
            model.renderPart("Frame", definition.textureLocation(), context);
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) ring));
        model.renderPart("Ring", definition.textureLocation(), context);
        model.renderPart("Ring2", definition.textureLocation(), context);
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
        model.renderPart("ArmLower1", definition.textureLocation(), context);

        poseStack.translate(0.0D, 2.375D, 0.9375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) arm[1]));
        poseStack.translate(0.0D, -2.375D, -0.9375D);
        model.renderPart("ArmUpper1", definition.textureLocation(), context);

        poseStack.translate(0.0D, 2.375D, 0.4375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) arm[2]));
        poseStack.translate(0.0D, -2.375D, -0.4375D);
        model.renderPart("Head1", definition.textureLocation(), context);
        poseStack.translate(0.0D, striker, 0.0D);
        model.renderPart("Spike1", definition.textureLocation(), context);
        poseStack.popPose();
    }

    private static void renderPurex(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity, float partialTick) {
        boolean frame = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                && machine.shouldRenderFrame();
        float anim = blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                ? machine.getPurexAnim(partialTick)
                : 0.0F;

        model.renderPart("Base", definition.textureLocation(), context);
        if (frame) {
            model.renderPart("Frame", definition.textureLocation(), context);
        }
        renderRotatingPart(model, new LegacyTileRenderPlans.RotatingModelPartPlan(
                "purex_fan", "Fan", 1.5D, 1.25D, 0.0D, 0.0F, 0.0F, 1.0F,
                anim * 45.0F), context, poseStack);
        renderTranslatedPart(model, new LegacyTileRenderPlans.TranslatedModelPartPlan(
                "purex_pump", "Pump", true, BobMathUtil.sps(anim * 0.25D) * 0.5D, 0.0D, 0.0D),
                context, poseStack);
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

        model.renderPart("Main", definition.textureLocation(), partContext(definition, "Main", context));
        if (kind == FireboxHeaterBlockEntity.Kind.OVEN) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, door * 0.75D / 135.0D);
            model.renderPart("Door", definition.textureLocation(), partContext(definition, "Door", context));
            poseStack.popPose();
            String innerPart = burning ? "InnerBurning" : "Inner";
            ObjRenderContext innerContext = partContext(definition, innerPart, context);
            model.renderPart(innerPart, definition.textureLocation(),
                    burning && innerContext == context ? innerContext.fullBright() : innerContext);
            return;
        }

        poseStack.pushPose();
        poseStack.translate(1.375D, 0.0D, 0.375D);
        poseStack.mulPose(Axis.YN.rotationDegrees(door));
        poseStack.translate(-1.375D, 0.0D, -0.375D);
        model.renderPart("Door", definition.textureLocation(), partContext(definition, "Door", context));
        poseStack.popPose();
        String innerPart = burning ? "InnerBurning" : "InnerEmpty";
        ObjRenderContext innerContext = partContext(definition, innerPart, context);
        model.renderPart(innerPart, definition.textureLocation(),
                burning && innerContext == context ? innerContext.fullBright() : innerContext);
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
        if (!plan.active()) {
            return;
        }
        ObjRenderContext resolved = applyTintPlan(context, plan);
        if (plan.textured()) {
            model.renderPart(plan.partName(), texture, resolved);
        } else {
            model.renderPartUntextured(plan.partName(), resolved);
        }
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
