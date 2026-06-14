package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachinePartRenderProperties;
import com.hbm.ntm.block.LegacyMachineRenderProfile;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.BoilerBlockEntity;
import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.blockentity.CompressorBlockEntity;
import com.hbm.ntm.blockentity.FireboxHeaterBlockEntity;
import com.hbm.ntm.blockentity.PoweredCondenserBlockEntity;
import com.hbm.ntm.blockentity.WaterPumpBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.Map;

public class LegacyVisibleMachineRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();
    private static final ResourceLocation BOILER_BURST_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/block/machines/boiler_burst.obj");
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
                renderVisibleMachineStaticPlan(definition, model,
                        LegacyTileRenderPlans.radgenStaticPlan(false), context);
                return true;
            }
            case BATTERY_REDD_STATIC_SPECIAL -> {
                renderVisibleMachineStaticPlan(definition, model,
                        LegacyTileRenderPlans.batteryReddStaticPlan(), context);
                return true;
            }
            case CRYSTALLIZER_STATIC_SPECIAL -> {
                renderVisibleMachineStaticPlan(definition, model,
                        LegacyTileRenderPlans.crystallizerStaticPlan(false), context);
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
