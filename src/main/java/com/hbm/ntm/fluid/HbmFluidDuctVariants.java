package com.hbm.ntm.fluid;

import java.util.Arrays;

public final class HbmFluidDuctVariants {
    public static final int STANDARD_STYLE_COUNT = 3;
    public static final int BOX_METADATA_COUNT = 15;

    private static final String[] STANDARD_PARTICLE_TEXTURES = {"pipe_neo", "pipe_silver", "pipe_colored"};
    private static final String[] BOX_MATERIAL_TEXTURES = {"silver", "copper", "white"};
    private static final int[] STANDARD_VISIBLE_STYLES = {0, 1, 2};
    private static final int[] BOX_VISIBLE_METADATA = createSequentialMetadata();
    private static final int[] EXHAUST_VISIBLE_METADATA = {0, 3, 6, 9, 12};

    private HbmFluidDuctVariants() {
    }

    public static int clampStandardStyle(int style) {
        return Math.max(0, Math.min(STANDARD_STYLE_COUNT - 1, style));
    }

    public static int clampBoxMetadata(int metadata) {
        return Math.max(0, Math.min(BOX_METADATA_COUNT - 1, metadata));
    }

    public static int[] standardVisibleStyles() {
        return STANDARD_VISIBLE_STYLES.clone();
    }

    public static int[] boxVisibleMetadata() {
        return BOX_VISIBLE_METADATA.clone();
    }

    public static int[] exhaustVisibleMetadata() {
        return EXHAUST_VISIBLE_METADATA.clone();
    }

    public static int boxMaterialIndex(int metadata) {
        return clampBoxMetadata(metadata) % BOX_MATERIAL_TEXTURES.length;
    }

    public static String boxMaterialTexture(int metadata) {
        return BOX_MATERIAL_TEXTURES[boxMaterialIndex(metadata)];
    }

    public static int boxSizeStep(int metadata) {
        return clampBoxMetadata(metadata) / BOX_MATERIAL_TEXTURES.length;
    }

    public static String standardParticleTexture(int style) {
        return STANDARD_PARTICLE_TEXTURES[clampStandardStyle(style)];
    }

    public static String standardOverlayTexture(int style) {
        return standardParticleTexture(style) + "_overlay";
    }

    public static String boxJunctionTexture(int metadata) {
        return "boxduct_" + boxMaterialTexture(metadata) + "_junction_" + boxSizeStep(metadata);
    }

    public static String exhaustJunctionTexture(int metadata) {
        return "boxduct_exhaust_junction_" + boxSizeStep(metadata);
    }

    public static boolean isExhaustVisibleMetadata(int metadata) {
        int clamped = clampBoxMetadata(metadata);
        return Arrays.stream(EXHAUST_VISIBLE_METADATA).anyMatch(value -> value == clamped);
    }

    private static int[] createSequentialMetadata() {
        int[] metadata = new int[BOX_METADATA_COUNT];
        for (int i = 0; i < metadata.length; i++) {
            metadata[i] = i;
        }
        return metadata;
    }
}
