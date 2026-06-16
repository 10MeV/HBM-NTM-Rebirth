package com.hbm.ntm.client.particle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class SpentCasingDefinition {
    public static final int COLOR_CASE_BRASS = 0xEBC35E;
    public static final int COLOR_CASE_EQUESTRIAN = 0x957BA0;
    public static final int COLOR_CASE_12GA = 0x757575;
    public static final int COLOR_CASE_4GA = 0xD8D8D8;
    public static final int COLOR_CASE_44 = 0x3E3E3E;
    public static final int COLOR_CASE_16INCH = 0xD89128;
    public static final int COLOR_CASE_16INCH_PHOS = 0xC8C8C8;
    public static final int COLOR_CASE_16INCH_NUKE = 0x495443;
    public static final int COLOR_CASE_40MM = 0x515151;
    public static final int COLOR_CASE_CHLOROPHYTE = 0x659750;
    private static final String PLINK_SHELL = "hbm:weapon.casing.shell";
    private static final String PLINK_SMALL = "hbm:weapon.casing.small";
    private static final String PLINK_MEDIUM = "hbm:weapon.casing.medium";
    private static final String PLINK_LARGE = "hbm:weapon.casing.large";

    private static final Map<String, SpentCasingDefinition> DEFINITIONS = new HashMap<>();

    public enum CasingType {
        STRAIGHT("Straight"),
        BOTTLENECK("Bottleneck"),
        SHOTGUN("Shotgun", "ShotgunCase");

        private final String[] partNames;

        CasingType(String... partNames) {
            this.partNames = partNames;
        }

        public String[] partNames() {
            return partNames;
        }
    }

    private final CasingType type;
    private float scaleX = 1.0F;
    private float scaleY = 1.0F;
    private float scaleZ = 1.0F;
    private int[] colors;
    private String bounceSound;
    private boolean largeBounceSound;
    private float bounceYaw = 1.0F;
    private float bouncePitch = 1.0F;
    private int maxAge = 240;

    private SpentCasingDefinition(CasingType type) {
        this.type = type;
        this.bounceSound = type == CasingType.SHOTGUN ? PLINK_SHELL : PLINK_SMALL;
    }

    public static SpentCasingDefinition fromName(String name) {
        if (DEFINITIONS.isEmpty()) {
            registerDefaults();
        }
        SpentCasingDefinition definition = DEFINITIONS.get(key(name));
        return definition == null ? DEFINITIONS.get("default") : definition;
    }

    private static void registerDefaults() {
        SpentCasingDefinition smallBrass = straight().colors(COLOR_CASE_BRASS);
        smallBrass.copy().name("default");
        smallBrass.copy().scale(0.5F).names("p22", "p22fmj", "p22jhp");
        smallBrass.copy().scale(0.5F).colors(COLOR_CASE_44).name("p22ap");
        smallBrass.copy().scale(1.0F, 1.0F, 0.75F).names("p9", "p9fmj", "p9jhp");
        smallBrass.copy().scale(1.0F, 1.0F, 0.75F).colors(COLOR_CASE_44).name("p9ap");
        bottleneck().colors(COLOR_CASE_BRASS).scale(0.8F).names("r556", "r556fmj", "r556jhp");
        bottleneck().colors(COLOR_CASE_44).scale(0.8F).name("r556ap");
        bottleneck().colors(COLOR_CASE_BRASS).names("r762", "r762fmj", "r762jhp");
        bottleneck().colors(COLOR_CASE_44).names("r762ap", "r762du", "r762he");
        straight().colors(COLOR_CASE_BRASS).scale(1.5F, 1.0F, 1.5F).names("DEBUG0", "DEBUG1");
        straight().colors(COLOR_CASE_BRASS).smokeDefaults().names("m44bp", "m44", "m44fmj", "m44jhp", "m44express");
        straight().colors(COLOR_CASE_44).smokeDefaults().name("m44ap");
        straight().colors(COLOR_CASE_EQUESTRIAN).smokeDefaults().names("m44equestrianPip", "m44equestrianMn7");
        straight().colors(COLOR_CASE_BRASS).scale(1.0F, 1.0F, 0.75F).names("p45", "p45fmj", "p45jhp");
        straight().colors(COLOR_CASE_44).scale(1.0F, 1.0F, 0.75F).names("p45ap", "p45du");
        bottleneck().colors(COLOR_CASE_BRASS).scale(1.5F).names("bmg50", "bmg50fmj", "bmg50jhp");
        bottleneck().colors(COLOR_CASE_44).scale(1.5F).names("bmg50ap", "bmg50du", "bmg50he", "bmg50sm");
        bottleneck().colors(COLOR_CASE_EQUESTRIAN).scale(1.5F).names("bmg50black", "bmg50equestrian");
        straight().colors(COLOR_CASE_BRASS).scale(2.0F, 2.0F, 1.5F).names("b75", "b75inc", "b75exp");
        straight().colors(0x9E1616).scale(2.0F).name("g26Flare");
        straight().colors(0x3C80F0).scale(2.0F).name("g26FlareSupply");
        straight().colors(0x278400).scale(2.0F).name("g26FlareWeapon");
        straight().colors(0x777777).scale(2.0F, 2.0F, 1.5F).name("g40");
        straight().colors(0x5E6854).scale(2.0F, 2.0F, 1.5F).name("g40heat");
        straight().colors(0xE30000).scale(2.0F, 2.0F, 1.5F).name("g40demo");
        straight().colors(0xE86F20).scale(2.0F, 2.0F, 1.5F).name("g40inc");
        straight().colors(0xC8C8C8).scale(2.0F, 2.0F, 1.5F).name("g40phos");
        straight().colors(0xCEB78E).name("35-800");
        straight().colors(COLOR_CASE_BRASS).scale(1.5F).bounceMotion(1.0F, 0.5F).age(60).name("DGK");

        shotgun(0xB52B2B, COLOR_CASE_BRASS).scale(0.75F).names("12GA", "12GA_BP", "12GA_BP_MAGNUM", "12GA_BP_SLUG");
        shotgun(0x393939, COLOR_CASE_BRASS).scale(0.75F).name("12GA_SLUG");
        shotgun(0x3C80F0, COLOR_CASE_BRASS).scale(0.75F).name("12GA_FLECHETTE");
        shotgun(0x278400, COLOR_CASE_12GA).scale(0.75F).name("12GA_MAGNUM");
        shotgun(0xDA4127, COLOR_CASE_12GA).scale(0.75F).name("12GA_EXPLOSIVE");
        shotgun(0x910001, COLOR_CASE_12GA).scale(0.75F).name("12GA_PHOSPHORUS");
        shotgun(0xB52B2B, COLOR_CASE_EQUESTRIAN).scale(0.75F).names("12gaEquestrianBJ", "12gaEquestrianTKR");
        shotgun(0xB52B2B, COLOR_CASE_12GA).scale(1.0F).name("10GA");
        shotgun(0xE5DD00, COLOR_CASE_12GA).scale(1.0F).name("10GAShrapnel");
        shotgun(0x538D53, COLOR_CASE_12GA).scale(1.0F).name("10GADU");
        shotgun(0x808080, COLOR_CASE_12GA).scale(1.0F).name("10GASlug");
        shotgun(0xFAC943, COLOR_CASE_12GA).scale(1.0F).name("10GAEXP");

        bottleneck().scale(7.5F).bounceMotion(0.02F, 0.05F).colors(COLOR_CASE_BRASS)
                .names("240standard", "240ext", "240w", "240u", "240n");
        straight().scale(15.0F, 15.0F, 10.0F).age(300).bounceMotion(1.0F, 0.5F)
                .colors(COLOR_CASE_16INCH).names("ammo_arty", "ammo_arty_classic", "ammo_arty_he", "ammo_arty_cargo",
                        "ammo_arty_chlorine");
        straight().scale(15.0F, 15.0F, 10.0F).age(300).bounceMotion(1.0F, 0.5F)
                .colors(COLOR_CASE_16INCH_PHOS).names("ammo_arty_phosphorus", "ammo_arty_phosphorus_multi");
        straight().scale(15.0F, 15.0F, 10.0F).age(300).bounceMotion(1.0F, 0.5F)
                .colors(COLOR_CASE_16INCH_NUKE).names("ammo_arty_mini_nuke", "ammo_arty_nuke",
                        "ammo_arty_mini_nuke_multi", "ammo_arty_phosgene", "ammo_arty_mustard_gas");
        registerChlorophyteVariants();
    }

    private static void registerChlorophyteVariants() {
        Map<String, SpentCasingDefinition> baseDefinitions = new HashMap<>(DEFINITIONS);
        for (Map.Entry<String, SpentCasingDefinition> entry : baseDefinitions.entrySet()) {
            SpentCasingDefinition chlorophyte = entry.getValue().copy();
            chlorophyte.replaceLastColor(COLOR_CASE_CHLOROPHYTE);
            DEFINITIONS.put(entry.getKey() + "cl", chlorophyte);
        }
    }

    private static SpentCasingDefinition straight() {
        return new SpentCasingDefinition(CasingType.STRAIGHT);
    }

    private static SpentCasingDefinition bottleneck() {
        return new SpentCasingDefinition(CasingType.BOTTLENECK);
    }

    private static SpentCasingDefinition shotgun(int plasticColor, int caseColor) {
        return new SpentCasingDefinition(CasingType.SHOTGUN).colors(plasticColor, caseColor);
    }

    private SpentCasingDefinition copy() {
        SpentCasingDefinition copy = new SpentCasingDefinition(type);
        copy.scaleX = scaleX;
        copy.scaleY = scaleY;
        copy.scaleZ = scaleZ;
        copy.colors = colors == null ? null : colors.clone();
        copy.bounceSound = bounceSound;
        copy.largeBounceSound = largeBounceSound;
        copy.bounceYaw = bounceYaw;
        copy.bouncePitch = bouncePitch;
        copy.maxAge = maxAge;
        return copy;
    }

    private SpentCasingDefinition scale(float scale) {
        return scale(scale, scale, scale);
    }

    private SpentCasingDefinition scale(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
        if (x * y * z >= 100.0F && type != CasingType.SHOTGUN) {
            this.bounceSound = PLINK_LARGE;
            this.largeBounceSound = true;
        } else if (x * y * z >= 3.0F && type != CasingType.SHOTGUN) {
            this.bounceSound = PLINK_MEDIUM;
            this.largeBounceSound = false;
        }
        return this;
    }

    private SpentCasingDefinition colors(int... colors) {
        this.colors = colors;
        return this;
    }

    private SpentCasingDefinition replaceLastColor(int color) {
        if (colors == null || colors.length == 0) {
            colors = new int[] { color };
        } else {
            colors = colors.clone();
            colors[colors.length - 1] = color;
        }
        return this;
    }

    private SpentCasingDefinition bounceMotion(float yaw, float pitch) {
        this.bounceYaw = yaw;
        this.bouncePitch = pitch;
        return this;
    }

    private SpentCasingDefinition age(int maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    private SpentCasingDefinition smokeDefaults() {
        return this;
    }

    private void name(String name) {
        DEFINITIONS.put(key(name), copy());
    }

    private void names(String... names) {
        for (String name : names) {
            name(name);
        }
    }

    public CasingType type() {
        return type;
    }

    public float scaleX() {
        return scaleX;
    }

    public float scaleY() {
        return scaleY;
    }

    public float scaleZ() {
        return scaleZ;
    }

    public int color(int index) {
        if (colors == null || colors.length == 0) {
            return COLOR_CASE_BRASS;
        }
        return colors[Math.min(index, colors.length - 1)];
    }

    public String bounceSound() {
        return bounceSound;
    }

    public boolean largeBounceSound() {
        return largeBounceSound;
    }

    public float bounceYaw() {
        return bounceYaw;
    }

    public float bouncePitch() {
        return bouncePitch;
    }

    public int maxAge() {
        return maxAge;
    }

    private static String key(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }
}
