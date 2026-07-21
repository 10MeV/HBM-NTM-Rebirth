package com.hbm.ntm.client.obj;

/**
 * Legacy OpenGL lightmap texture coordinates, deliberately distinct from the
 * modern packed block/sky light value consumed by {@code LightTexture}.
 */
public record LegacyRawLightmapCoordinates(int u, int v) {
    public LegacyRawLightmapCoordinates {
        if (u < 0 || u > 0xFFFF || v < 0 || v > 0xFFFF) {
            throw new IllegalArgumentException("Legacy lightmap coordinates must fit unsigned shorts: " + u + "," + v);
        }
    }

    public static LegacyRawLightmapCoordinates of(int u, int v) {
        return new LegacyRawLightmapCoordinates(u, v);
    }

    /**
     * Vertex-format carrier for the raw-lightmap mode, which also reproduces the source renderer's
     * RenderHelper#enableStandardItemLighting fixed-function state.
     */
    public int vertexUv2() {
        return u | v << 16;
    }
}
