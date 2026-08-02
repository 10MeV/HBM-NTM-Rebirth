package com.hbm.ntm.client.renderer;

import net.minecraft.world.item.ItemDisplayContext;

/**
 * Carries the active BEWLR display context into the shared OBJ backend.
 *
 * <p>The OBJ backend is also used by block-entity renderers and therefore
 * cannot infer an item display context from its own arguments. Keep this
 * scope thread-local and nestable because item rendering can be re-entrant.
 */
public final class LegacyItemRenderContext {
    private static final ThreadLocal<ItemDisplayContext> CURRENT = new ThreadLocal<>();

    static ItemDisplayContext enter(ItemDisplayContext displayContext) {
        ItemDisplayContext previous = CURRENT.get();
        CURRENT.set(displayContext);
        return previous;
    }

    public static boolean isGui() {
        return CURRENT.get() == ItemDisplayContext.GUI;
    }

    static void restore(ItemDisplayContext previous) {
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }

    private LegacyItemRenderContext() {
    }
}
