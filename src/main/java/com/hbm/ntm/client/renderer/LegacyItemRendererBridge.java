package com.hbm.ntm.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class LegacyItemRendererBridge {
    private static final Map<BlockEntityWithoutLevelRenderer, BlockEntityWithoutLevelRenderer> CONTEXTUAL_RENDERERS =
            new IdentityHashMap<>();

    public static void accept(Consumer<IClientItemExtensions> consumer, BlockEntityWithoutLevelRenderer renderer) {
        accept(consumer, () -> renderer);
    }

    public static void accept(Consumer<IClientItemExtensions> consumer, Supplier<? extends BlockEntityWithoutLevelRenderer> rendererSupplier) {
        consumer.accept(extensions(rendererSupplier));
    }

    public static IClientItemExtensions extensions(BlockEntityWithoutLevelRenderer renderer) {
        return extensions(() -> renderer);
    }

    public static IClientItemExtensions extensions(Supplier<? extends BlockEntityWithoutLevelRenderer> rendererSupplier) {
        return new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer contextualRenderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (contextualRenderer == null) {
                    contextualRenderer = withDisplayContext(rendererSupplier.get());
                }
                return contextualRenderer;
            }
        };
    }

    public static BlockEntityWithoutLevelRenderer withDisplayContext(BlockEntityWithoutLevelRenderer renderer) {
        synchronized (CONTEXTUAL_RENDERERS) {
            return CONTEXTUAL_RENDERERS.computeIfAbsent(renderer, ContextualRenderer::new);
        }
    }

    /**
     * Verifies the production contextual wrapper/delegate identity without exposing the
     * private wrapper implementation or allowing callers to replace its delegate.
     */
    public static boolean wraps(BlockEntityWithoutLevelRenderer contextualRenderer,
            BlockEntityWithoutLevelRenderer expectedDelegate) {
        synchronized (CONTEXTUAL_RENDERERS) {
            return CONTEXTUAL_RENDERERS.get(expectedDelegate) == contextualRenderer;
        }
    }

    private static final class ContextualRenderer extends BlockEntityWithoutLevelRenderer {
        private final BlockEntityWithoutLevelRenderer delegate;

        private ContextualRenderer(BlockEntityWithoutLevelRenderer delegate) {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
            this.delegate = delegate;
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay) {
            ItemDisplayContext previous = LegacyItemRenderContext.enter(displayContext);
            try {
                delegate.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
            } finally {
                LegacyItemRenderContext.restore(previous);
            }
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            delegate.onResourceManagerReload(resourceManager);
        }
    }

    private LegacyItemRendererBridge() {
    }
}
