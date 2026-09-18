package com.hbm.ntm.client.render;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Marks OBJ submissions made from {@code ParticleEngine#render}.
 *
 * <p>Particle model poses are camera-relative local transforms. The particle engine keeps the
 * camera rotation in RenderSystem's active model-view matrix, so an immediate OBJ draw must not
 * replace that matrix with the local pose. The shared OBJ backend uses this scope to retain the
 * caller-owned buffer path, where the active particle model-view matrix is applied exactly once
 * when the batch is flushed.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class LegacyParticleModelRenderContext {
    private static final ThreadLocal<Integer> DEPTH = new ThreadLocal<>();
    private static final Scope SCOPE = new Scope();

    public static Scope enter() {
        Integer depth = DEPTH.get();
        DEPTH.set(depth == null ? 1 : depth + 1);
        return SCOPE;
    }

    public static boolean isActive() {
        return DEPTH.get() != null;
    }

    public static final class Scope implements AutoCloseable {
        private Scope() {
        }

        @Override
        public void close() {
            Integer depth = DEPTH.get();
            if (depth == null || depth <= 1) {
                DEPTH.remove();
            } else {
                DEPTH.set(depth - 1);
            }
        }
    }

    private LegacyParticleModelRenderContext() {
    }
}
