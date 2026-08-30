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

    /**
     * Returns whether the shared OBJ backend is currently being called from a BEWLR.
     *
     * <p>Every item display context is rendered into a caller-owned buffer lifecycle. This is
     * broader than {@link #isGui()}: dropped items, item frames and both hand views can also be
     * submitted outside the block-entity backend's queued-flush window.</p>
     */
    public static boolean isActive() {
        return CURRENT.get() != null;
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
