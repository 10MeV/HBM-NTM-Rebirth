package com.hbm.ntm.client.render;

import com.google.common.collect.ImmutableMap;
import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public final class HbmOptimizedRenderShaders {
    private static final ResourceLocation BLOCK_LIT_INSTANCED =
            new ResourceLocation(HbmNtm.MOD_ID, "block_lit_instanced");
    private static final ResourceLocation BLOCK_LIT_STATIC =
            new ResourceLocation(HbmNtm.MOD_ID, "block_lit_static");
    private static final ResourceLocation LEGACY_STANDARD_ITEM_LIT_STATIC =
            new ResourceLocation(HbmNtm.MOD_ID, "legacy_standard_item_lit_static");
    private static final ResourceLocation LEGACY_STANDARD_ITEM_LIT_SOLID =
            new ResourceLocation(HbmNtm.MOD_ID, "legacy_standard_item_lit_solid");
    private static final ResourceLocation LEGACY_TEXTURED_ADDITIVE =
            new ResourceLocation(HbmNtm.MOD_ID, "legacy_textured_additive");
    private static final ResourceLocation BLOCK_UNTEXTURED_INSTANCED =
            new ResourceLocation(HbmNtm.MOD_ID, "block_untextured_instanced");

    private static ShaderInstance blockLitInstancedShader;
    private static ShaderInstance blockLitStaticShader;
    private static ShaderInstance legacyStandardItemLitStaticShader;
    private static ShaderInstance legacyStandardItemLitSolidShader;
    private static ShaderInstance legacyTexturedAdditiveShader;
    private static ShaderInstance blockUntexturedInstancedShader;

    private HbmOptimizedRenderShaders() {
    }

    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        blockLitInstancedShader = null;
        blockLitStaticShader = null;
        legacyStandardItemLitStaticShader = null;
        legacyStandardItemLitSolidShader = null;
        legacyTexturedAdditiveShader = null;
        blockUntexturedInstancedShader = null;
        event.registerShader(new ShaderInstance(event.getResourceProvider(), BLOCK_LIT_INSTANCED,
                blockLitInstancedFormat()), shader -> {
            blockLitInstancedShader = shader;
            HbmNtm.LOGGER.info("Registered HBM optimized block_lit_instanced shader.");
        });
        event.registerShader(new ShaderInstance(event.getResourceProvider(), BLOCK_LIT_STATIC,
                DefaultVertexFormat.NEW_ENTITY), shader -> {
            blockLitStaticShader = shader;
            HbmNtm.LOGGER.info("Registered HBM optimized block_lit_static shader.");
        });
        event.registerShader(new ShaderInstance(event.getResourceProvider(), LEGACY_STANDARD_ITEM_LIT_STATIC,
                DefaultVertexFormat.NEW_ENTITY), shader -> {
            legacyStandardItemLitStaticShader = shader;
            HbmNtm.LOGGER.info("Registered HBM legacy standard-item-light static shader.");
        });
        event.registerShader(new ShaderInstance(event.getResourceProvider(), LEGACY_STANDARD_ITEM_LIT_SOLID,
                DefaultVertexFormat.NEW_ENTITY), shader -> {
            legacyStandardItemLitSolidShader = shader;
            HbmNtm.LOGGER.info("Registered HBM legacy standard-item-light solid shader.");
        });
        event.registerShader(new ShaderInstance(event.getResourceProvider(), LEGACY_TEXTURED_ADDITIVE,
                DefaultVertexFormat.NEW_ENTITY), shader -> {
            legacyTexturedAdditiveShader = shader;
            HbmNtm.LOGGER.info("Registered HBM legacy textured additive shader.");
        });
        event.registerShader(new ShaderInstance(event.getResourceProvider(), BLOCK_UNTEXTURED_INSTANCED,
                blockLitInstancedFormat()), shader -> {
            blockUntexturedInstancedShader = shader;
            HbmNtm.LOGGER.info("Registered HBM optimized block_untextured_instanced shader.");
        });
    }

    public static ShaderInstance blockLitInstancedShader() {
        return blockLitInstancedShader;
    }

    public static ShaderInstance blockLitStaticShader() {
        return blockLitStaticShader;
    }

    /**
     * Fixed-function equivalent of 1.7.10 RenderHelper#enableStandardItemLighting for shared static OBJ draws.
     */
    public static ShaderInstance legacyStandardItemLitStaticShader() {
        return legacyStandardItemLitStaticShader;
    }

    /** Solid-item variant matching vanilla entity-solid's no-alpha-test contract. */
    public static ShaderInstance legacyStandardItemLitSolidShader() {
        return legacyStandardItemLitSolidShader;
    }

    /**
     * NEW_ENTITY-compatible shader whose program blend is SRC_ALPHA/ONE.  The shader program must own the same
     * blend function as the RenderType because ShaderInstance#apply runs after RenderType state setup.
     */
    public static ShaderInstance legacyTexturedAdditiveShader() {
        return legacyTexturedAdditiveShader;
    }

    public static ShaderInstance blockUntexturedInstancedShader() {
        return blockUntexturedInstancedShader;
    }

    public static boolean instancingShaderReady() {
        return blockLitInstancedShader != null;
    }

    private static VertexFormat blockLitInstancedFormat() {
        return new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
                .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
                .put("Normal", DefaultVertexFormat.ELEMENT_NORMAL)
                .put("UV0", DefaultVertexFormat.ELEMENT_UV0)
                .put("InstModel0", new VertexFormatElement(0, VertexFormatElement.Type.FLOAT,
                        VertexFormatElement.Usage.GENERIC, 4))
                .put("InstModel1", new VertexFormatElement(0, VertexFormatElement.Type.FLOAT,
                        VertexFormatElement.Usage.GENERIC, 4))
                .put("InstModel2", new VertexFormatElement(0, VertexFormatElement.Type.FLOAT,
                        VertexFormatElement.Usage.GENERIC, 4))
                .put("InstModel3", new VertexFormatElement(0, VertexFormatElement.Type.FLOAT,
                        VertexFormatElement.Usage.GENERIC, 4))
                .put("InstLightC01", new VertexFormatElement(0, VertexFormatElement.Type.FLOAT,
                        VertexFormatElement.Usage.GENERIC, 4))
                .put("InstLightC23", new VertexFormatElement(0, VertexFormatElement.Type.FLOAT,
                        VertexFormatElement.Usage.GENERIC, 4))
                .put("InstLightC45", new VertexFormatElement(0, VertexFormatElement.Type.FLOAT,
                        VertexFormatElement.Usage.GENERIC, 4))
                .put("InstLightC67", new VertexFormatElement(0, VertexFormatElement.Type.FLOAT,
                        VertexFormatElement.Usage.GENERIC, 4))
                .put("InstColor", new VertexFormatElement(0, VertexFormatElement.Type.FLOAT,
                        VertexFormatElement.Usage.GENERIC, 4))
                .put("InstOverlay", new VertexFormatElement(0, VertexFormatElement.Type.FLOAT,
                        VertexFormatElement.Usage.GENERIC, 4))
                .put("LocalLightWeight", new VertexFormatElement(0, VertexFormatElement.Type.FLOAT,
                        VertexFormatElement.Usage.GENERIC, 3))
                .build());
    }
}
