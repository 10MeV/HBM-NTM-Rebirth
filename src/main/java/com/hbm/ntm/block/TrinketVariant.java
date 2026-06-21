package com.hbm.ntm.block;

import java.util.Locale;

public final class TrinketVariant {
    public static final String TAG_VARIANT = "Variant";

    private static final String[] BOBBLEHEAD = {
            "NONE",
            "STRENGTH",
            "PERCEPTION",
            "ENDURANCE",
            "CHARISMA",
            "INTELLIGENCE",
            "AGILITY",
            "LUCK",
            "BOB",
            "FRIZZLE",
            "PU238",
            "VT",
            "DOC",
            "BLUEHAT",
            "PHEO",
            "ADAM29",
            "UFFR",
            "VAER",
            "NOS",
            "DRILLGON",
            "CIRNO",
            "MICROWAVE",
            "PEEP",
            "MELLOW",
            "ABEL"
    };

    private static final String[] SNOWGLOBE = {
            "NONE",
            "RIVETCITY",
            "TENPENNYTOWER",
            "LUCKY38",
            "SIERRAMADRE",
            "PRYDWEN"
    };

    private static final String[] PLUSHIE = {
            "NONE",
            "YOMI",
            "NUMBERNINE",
            "HUNDUN",
            "DERG"
    };

    public static int variantCount(Kind kind) {
        return names(kind).length;
    }

    public static int firstCreativeVariant(Kind kind) {
        return Math.min(1, variantCount(kind) - 1);
    }

    public static int clamp(Kind kind, int variant) {
        return Math.max(0, Math.min(variant, variantCount(kind) - 1));
    }

    public static String name(Kind kind, int variant) {
        return names(kind)[clamp(kind, variant)];
    }

    public static String texture(Kind kind, int variant) {
        String name = name(kind, variant);
        if (kind == Kind.BOBBLEHEAD) {
            return switch (name) {
                case "STRENGTH", "PERCEPTION", "ENDURANCE", "CHARISMA", "INTELLIGENCE", "AGILITY", "LUCK" -> "vaultboy";
                case "BOB" -> "hbm";
                case "PU238" -> "pellet";
                case "DOC" -> "doctor17ph";
                case "BLUEHAT" -> "thebluehat";
                case "DRILLGON" -> "drillgon200";
                case "MELLOW" -> "mellowrpg8";
                default -> name.toLowerCase(Locale.ROOT);
            };
        }
        if (kind == Kind.SNOWGLOBE) {
            return "snowglobe_features";
        }
        return switch (name) {
            case "HUNDUN" -> "hundun";
            case "DERG" -> "derg";
            default -> "yomi";
        };
    }

    public static String displayKey(Kind kind, int variant) {
        return "tooltip.hbm_ntm_rebirth." + switch (kind) {
            case BOBBLEHEAD -> "bobblehead";
            case SNOWGLOBE -> "snowglobe";
            case PLUSHIE -> "plushie";
        } + "." + name(kind, variant).toLowerCase(Locale.ROOT);
    }

    public static String snowglobeLabel(int variant) {
        return switch (name(Kind.SNOWGLOBE, variant)) {
            case "RIVETCITY" -> "Rivet City";
            case "TENPENNYTOWER" -> "Tenpenny Tower";
            case "LUCKY38" -> "Lucky 38";
            case "SIERRAMADRE" -> "Sierra Madre";
            case "PRYDWEN" -> "The Prydwen";
            default -> "NONE";
        };
    }

    public static String modelSuffix(Kind kind, int variant) {
        return name(kind, variant).toLowerCase(Locale.ROOT);
    }

    private static String[] names(Kind kind) {
        return switch (kind) {
            case BOBBLEHEAD -> BOBBLEHEAD;
            case SNOWGLOBE -> SNOWGLOBE;
            case PLUSHIE -> PLUSHIE;
        };
    }

    public enum Kind {
        BOBBLEHEAD,
        SNOWGLOBE,
        PLUSHIE
    }

    private TrinketVariant() {
    }
}
