package com.hbm.ntm.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.hbm.ntm.client.render.HbmOptimizedRenderShaders;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.RegisterNamedRenderTypesEvent;

/** Named block/item render types for source-backed legacy OBJ lighting. */
public final class LegacyLitObjRenderTypes {
    public static final String SOLID_NAME = "legacy_lit_solid";
    public static final String CUTOUT_NAME = "legacy_lit_cutout";
    public static final String TRANSLUCENT_NAME = "legacy_lit_translucent";

    private static final RenderStateShard.ShaderStateShard LEGACY_STANDARD_ALPHA_LIGHT_SHADER =
            new RenderStateShard.ShaderStateShard(
                    HbmOptimizedRenderShaders::legacyStandardItemLitStaticShader);
    private static final RenderStateShard.ShaderStateShard LEGACY_STANDARD_SOLID_LIGHT_SHADER =
            new RenderStateShard.ShaderStateShard(
                    HbmOptimizedRenderShaders::legacyStandardItemLitSolidShader);
    private static final RenderStateShard.TransparencyStateShard NORMAL_ALPHA_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("hbm_legacy_lit_obj_alpha",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                GlStateManager.SourceFactor.ONE,
                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    });

    private static final RenderType ITEM_SOLID =
            createItemType("hbm_legacy_lit_obj_item_solid", false, false, true);
    private static final RenderType ITEM_CUTOUT =
            createItemType("hbm_legacy_lit_obj_item_cutout", false, false, false);
    private static final RenderType ITEM_TRANSLUCENT =
            createItemType("hbm_legacy_lit_obj_item_translucent", false, true, false);

    private LegacyLitObjRenderTypes() {
    }

    public static void register(RegisterNamedRenderTypesEvent event) {
        event.register(SOLID_NAME, RenderType.solid(), ITEM_SOLID);
        event.register(CUTOUT_NAME, RenderType.cutout(), ITEM_CUTOUT);
        event.register(TRANSLUCENT_NAME, RenderType.translucent(), ITEM_TRANSLUCENT);
    }

    public static boolean isItemRenderType(RenderType renderType) {
        return renderType == ITEM_SOLID || renderType == ITEM_CUTOUT || renderType == ITEM_TRANSLUCENT;
    }

    private static RenderType createItemType(String name, boolean mipmap, boolean translucent, boolean solid) {
        RenderType.CompositeState.CompositeStateBuilder state = RenderType.CompositeState.builder()
                .setShaderState(solid ? LEGACY_STANDARD_SOLID_LIGHT_SHADER : LEGACY_STANDARD_ALPHA_LIGHT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(
                        InventoryMenu.BLOCK_ATLAS, false, mipmap))
                .setCullState(new RenderStateShard.CullStateShard(true))
                .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true));
        if (translucent) {
            state.setTransparencyState(NORMAL_ALPHA_TRANSPARENCY);
        }
        return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1_048_576,
                true, translucent, state.createCompositeState(true));
    }
}
