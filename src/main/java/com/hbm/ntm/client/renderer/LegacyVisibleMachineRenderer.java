package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachinePartRenderProperties;
import com.hbm.ntm.block.LegacyMachineRenderProfile;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.BoilerBlockEntity;
import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.blockentity.CombinationOvenBlockEntity;
import com.hbm.ntm.blockentity.CompressorBlockEntity;
import com.hbm.ntm.blockentity.FireboxHeaterBlockEntity;
import com.hbm.ntm.blockentity.AmmoPressBlockEntity;
import com.hbm.ntm.blockentity.BatteryReddBlockEntity;
import com.hbm.ntm.blockentity.IntakeBlockEntity;
import com.hbm.ntm.blockentity.LegacyGenericSelectorMachineBlockEntity;
import com.hbm.ntm.blockentity.MixerBlockEntity;
import com.hbm.ntm.blockentity.PoweredCondenserBlockEntity;
import com.hbm.ntm.blockentity.ProcessingMachineBlockEntity;
import com.hbm.ntm.blockentity.RadGenBlockEntity;
import com.hbm.ntm.blockentity.RefineryBlockEntity;
import com.hbm.ntm.blockentity.SawmillBlockEntity;
import com.hbm.ntm.blockentity.StirlingBlockEntity;
import com.hbm.ntm.blockentity.WaterPumpBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.util.BobMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.Map;

public class LegacyVisibleMachineRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();
    private static final ResourceLocation BOILER_BURST_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/block/machines/boiler_burst.obj");
    private static final ResourceLocation REFINERY_EXPLODED_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/block/machines/refinery_exploded.obj");
    private static final ResourceLocation COMBINATION_FIRE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/rbmk_fire.png");
    private static final ResourceLocation BATTERY_REDD_PLASMA_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/fusion/plasma.png");
    private static final ResourceLocation BATTERY_REDD_PLASMA_SPARKLE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/fusion/plasma_sparkle.png");
    private static final Map<ResourceLocation, LegacyWavefrontModel> EXTRA_MODELS = new IdentityHashMap<>();

    public LegacyVisibleMachineRenderer(BlockEntityRendererProvider.Context context) {
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
    public void render(T blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()));

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        if (definition.renderAll()) {
            ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);
            if (renderProfile(blockEntity, partialTick, definition, model, context, poseStack)) {
                poseStack.popPose();
                return;
            }
            model.renderAll(definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        } else {
            ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);
            if (!renderProfile(blockEntity, partialTick, definition, model, context, poseStack)) {
                renderParts(definition, model, context);
            }
        }

        poseStack.popPose();
    }

    private static void renderParts(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context) {
        for (boolean translucentPass : new boolean[] { false, true }) {
            for (String part : definition.renderParts()) {
                LegacyMachinePartRenderProperties properties = definition.partRenderProperties().get(part);
                if (LegacyMachinePartRenderContexts.translucent(properties) != translucentPass) {
                    continue;
                }
                model.renderPart(part, definition.partTextures().getOrDefault(part, definition.textureLocation()),
                        LegacyMachinePartRenderContexts.apply(context, properties));
            }
        }
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
            case COMBINATION_OVEN_FIRE -> {
                renderCombinationOven(definition, model, context, poseStack, blockEntity);
                return true;
            }
            case MIXER_RUNNING_PARTS -> {
                renderMixer(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case ARC_FURNACE_STATIC_PREVIEW -> {
                LegacyArcFurnaceRenderHelper.renderPlan(model,
                        LegacyTileRenderPlans.arcFurnaceStaticPreviewPlan(), context, poseStack);
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
            case FIREBOX_HEATER -> {
                renderFireboxHeater(definition, model, context, poseStack, blockEntity, partialTick);
                return true;
            }
            case HEAT_BOILER -> {
                renderHeatBoiler(definition, model, context, poseStack, blockEntity);
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
        renderPlannedPart(model, definition.textureLocation(), LegacyTileRenderPlans.radgenLightPlan(on), context);
        renderPlannedPart(model, definition.textureLocation(), LegacyTileRenderPlans.radgenGlassPlan(), context);
        model.renderPart("Glass", definition.textureLocation(), context);
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
                        key -> new LegacyWavefrontModel(key, definition.textureLocation()))
                : model;
        selected.renderAll(definition.textureLocation(), poseStack, context.buffer(), context.packedLight(),
                context.packedOverlay());
        poseStack.popPose();
    }

    private static void renderRefinery(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack, BlockEntity blockEntity) {
        boolean exploded = blockEntity instanceof RefineryBlockEntity refinery && refinery.isExploded();
        boolean tilted = blockEntity instanceof RefineryBlockEntity refinery && refinery.isTilted();
        LegacyTileRenderPlans.RefineryDamagePlan plan =
                LegacyTileRenderPlans.refineryDamagePlan(exploded, tilted);
        LegacyWavefrontModel selected = plan.exploded()
                ? EXTRA_MODELS.computeIfAbsent(REFINERY_EXPLODED_MODEL,
                        key -> new LegacyWavefrontModel(key, definition.textureLocation()))
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
        renderPlannedPart(model, definition.textureLocation(), LegacyTileRenderPlans.crystallizerFluidPlan(on),
                context);
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
    }

    private static void renderPrecassArm(LegacyMachineDefinition definition, LegacyWavefrontModel model,
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

        model.renderPart("Main", definition.textureLocation(), context);
        if (kind == FireboxHeaterBlockEntity.Kind.OVEN) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, door * 0.75D / 135.0D);
            model.renderPart("Door", definition.textureLocation(), context);
            poseStack.popPose();
            model.renderPart(burning ? "InnerBurning" : "Inner", definition.textureLocation(),
                    burning ? context.fullBright() : context);
            return;
        }

        poseStack.pushPose();
        poseStack.translate(1.375D, 0.0D, 0.375D);
        poseStack.mulPose(Axis.YN.rotationDegrees(door));
        poseStack.translate(-1.375D, 0.0D, -0.375D);
        model.renderPart("Door", definition.textureLocation(), context);
        poseStack.popPose();
        model.renderPart(burning ? "InnerBurning" : "InnerEmpty", definition.textureLocation(),
                burning ? context.fullBright() : context);
    }

    static void renderVisibleMachineStaticPlan(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            LegacyTileRenderPlans.VisibleMachineStaticPlan plan, ObjRenderContext context) {
        for (LegacyTileRenderPlans.ModelPartTintPlan part : plan.parts()) {
            renderPlannedPart(model, definition.textureLocation(), part, context);
        }
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
