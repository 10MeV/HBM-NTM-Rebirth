package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.TrinketVariant;
import com.hbm.ntm.blockentity.TrinketBlockEntity;
import com.hbm.ntm.client.obj.ObjModelPart;
import com.hbm.ntm.client.obj.ObjTrinketModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TrinketBlockEntityRenderer implements BlockEntityRenderer<TrinketBlockEntity> {
    private static final ObjModelPart BOBBLEHEAD_SOCKET = trinketPart("bobble_socket", RenderType.cutout());
    private static final ObjModelPart BOBBLEHEAD_PELLET = trinketPart("bobble_pellet", RenderType.cutout());
    private static final ObjModelPart BOBBLEHEAD_FUMO = trinketPart("bobble_fumo", RenderType.cutout());
    private static final ObjModelPart BOBBLEHEAD_DRILLGON = trinketPart("bobble_drillgon", RenderType.cutout());
    private static final ObjModelPart SNOWGLOBE_SOCKET = trinketPart("snowglobe_socket", RenderType.cutout());
    private static final ObjModelPart SNOWGLOBE_GLASS = translucentTrinketPart("snowglobe_glass");
    private static final Map<String, ObjModelPart> BOBBLEHEAD_MODELS = new HashMap<>();
    private static final Map<String, ObjModelPart> SNOWGLOBE_FEATURES = new HashMap<>();
    private static final Map<String, ObjModelPart> PLUSHIE_MODELS = new HashMap<>();

    static {
        for (String texture : new String[]{
                "vaultboy", "hbm", "frizzle", "vt", "doctor17ph", "thebluehat", "pheo", "adam29",
                "vaer", "nos", "cirno", "microwave", "peep", "mellowrpg8", "abel"
        }) {
            BOBBLEHEAD_MODELS.put(texture, trinketPart("bobble_classic_" + texture, RenderType.cutout()));
        }
        for (String feature : new String[]{"rivetcity", "tenpennytower", "lucky38", "sierramadre", "prydwen"}) {
            SNOWGLOBE_FEATURES.put(feature, trinketPart("snowglobe_" + feature, RenderType.cutout()));
        }
        PLUSHIE_MODELS.put("yomi", trinketPart("plushie_yomi", RenderType.cutout()));
        PLUSHIE_MODELS.put("numbernine", trinketPart("plushie_numbernine", RenderType.cutout()));
        PLUSHIE_MODELS.put("hundun", trinketPart("plushie_hundun", RenderType.cutout()));
        PLUSHIE_MODELS.put("derg", trinketPart("plushie_derg", RenderType.cutout()));
    }

    public TrinketBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static void registerAdditionalModels() {
        // Loading this class initializes the ObjModelPart registry entries used by ObjModelLibrary.
    }

    @Override
    public void render(TrinketBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        TrinketVariant.Kind kind = blockEntity.kind();
        int variant = blockEntity.variant();
        if (variant <= 0) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YN.rotationDegrees(blockEntity.yawDegrees() + 90.0F));

        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, blockEntity.getBlockState(), packedLight, packedOverlay);
        renderTrinket(kind, variant, blockEntity.squishTimer(), partialTick, context);

        poseStack.popPose();
    }

    public static void renderTrinket(TrinketVariant.Kind kind, int variant, int squishTimer, float partialTick, ObjRenderContext context) {
        switch (kind) {
            case BOBBLEHEAD -> renderBobblehead(variant, context);
            case SNOWGLOBE -> renderSnowglobe(variant, context);
            case PLUSHIE -> renderPlushie(squishTimer, partialTick, variant, context);
        }
    }

    private static void renderBobblehead(int variant, ObjRenderContext context) {
        context.poseStack().pushPose();
        context.poseStack().scale(0.25F, 0.25F, 0.25F);
        BOBBLEHEAD_SOCKET.render(context);
        String type = TrinketVariant.name(TrinketVariant.Kind.BOBBLEHEAD, variant);
        switch (type) {
            case "PU238" -> BOBBLEHEAD_PELLET.render(context);
            case "UFFR" -> BOBBLEHEAD_FUMO.render(context);
            case "DRILLGON" -> BOBBLEHEAD_DRILLGON.render(context);
            default -> {
                ObjModelPart model = BOBBLEHEAD_MODELS.get(TrinketVariant.texture(TrinketVariant.Kind.BOBBLEHEAD, variant));
                if (model != null) {
                    model.render(context);
                }
            }
        }
        context.poseStack().popPose();
    }

    private static void renderSnowglobe(int variant, ObjRenderContext context) {
        context.poseStack().pushPose();
        context.poseStack().scale(0.0625F, 0.0625F, 0.0625F);
        SNOWGLOBE_SOCKET.render(context);
        ObjModelPart feature = SNOWGLOBE_FEATURES.get(TrinketVariant.modelSuffix(TrinketVariant.Kind.SNOWGLOBE, variant));
        if (feature != null) {
            feature.render(context);
        }
        renderSnowglobeLabel(variant, context);
        SNOWGLOBE_GLASS.render(context);
        context.poseStack().popPose();
    }

    private static void renderSnowglobeLabel(int variant, ObjRenderContext context) {
        String label = TrinketVariant.snowglobeLabel(variant);
        if ("NONE".equals(label)) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        context.poseStack().pushPose();
        context.poseStack().translate(4.025D, 0.5D, 0.0D);
        context.poseStack().scale(0.05F, -0.05F, 0.05F);
        context.poseStack().translate(0.0D, -font.lineHeight / 2.0D, font.width(label) * 0.5D);
        context.poseStack().mulPose(Axis.YP.rotationDegrees(90.0F));
        context.poseStack().translate(0.0D, 1.0D, 0.0D);
        font.drawInBatch(label, 0.0F, 0.0F, 0xFFFFFF, false, context.poseStack().last().pose(),
                context.buffer(), Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        context.poseStack().popPose();
    }

    private static void renderPlushie(int squishTimer, float partialTick, int variant, ObjRenderContext context) {
        context.poseStack().pushPose();
        if (squishTimer > 0) {
            double squish = Math.max(0.0D, squishTimer - partialTick);
            context.poseStack().scale(1.0F, (float) (1.0D + (-(Math.sin(squish)) * squish) * 0.025D), 1.0F);
        }

        String suffix = TrinketVariant.modelSuffix(TrinketVariant.Kind.PLUSHIE, variant).toLowerCase(Locale.ROOT);
        float scale = switch (suffix) {
            case "yomi" -> 0.5F;
            case "numbernine" -> 0.75F;
            default -> 1.0F;
        };
        context.poseStack().scale(scale, scale, scale);

        ObjModelPart model = PLUSHIE_MODELS.get(suffix);
        if (model != null) {
            model.render(context);
        }
        context.poseStack().popPose();
    }

    private static ObjModelPart trinketPart(String name, RenderType renderType) {
        return ObjTrinketModels.part(name, renderType);
    }

    private static ObjModelPart translucentTrinketPart(String name) {
        return ObjTrinketModels.part(name, RenderType.translucent(), true);
    }
}
