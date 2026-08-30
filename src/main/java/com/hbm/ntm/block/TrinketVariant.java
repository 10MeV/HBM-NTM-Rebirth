package com.hbm.ntm.block;

import java.util.Locale;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.network.chat.Component;

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

    /** Text shown by the old client-only bobblehead/snowglobe panels. */
    public static List<Component> infoLines(Kind kind, int variant) {
        int safeVariant = clamp(kind, variant);
        List<Component> lines = new ArrayList<>();
        if (kind == Kind.BOBBLEHEAD) {
            lines.add(Component.literal("Nuclear Tech Commemorative Bobblehead"));
            lines.add(Component.literal(bobbleTitle(safeVariant)));
            String contribution = bobbleContribution(safeVariant);
            if (contribution != null) {
                lines.add(Component.literal("Has contributed"));
                addSplit(lines, contribution);
            }
            String inscription = bobbleInscription(safeVariant);
            if (inscription != null) {
                lines.add(Component.literal("On the bottom is the following inscription:"));
                addSplit(lines, inscription);
            }
            return lines;
        }
        lines.add(Component.literal("Nuclear Tech Commemorative Snowglobe"));
        lines.add(Component.literal(snowglobeLabel(safeVariant)));
        String inscription = snowglobeInscription(safeVariant);
        if (inscription != null) {
            lines.add(Component.literal("On the bottom is the following inscription:"));
            addSplit(lines, inscription);
        }
        return lines;
    }

    private static void addSplit(List<Component> lines, String value) {
        for (String part : value.split("\\$")) {
            lines.add(Component.literal(part));
        }
    }

    private static String bobbleTitle(int variant) {
        return switch (name(Kind.BOBBLEHEAD, variant)) {
            case "BOB" -> "Robert \"The Bobcat\" Katzinsky";
            case "FRIZZLE" -> "Frooz";
            case "PU238" -> "Pu-238";
            case "VT" -> "VT-6/24";
            case "DOC" -> "The Doctor";
            case "BLUEHAT" -> "The Blue Hat";
            case "PHEO" -> "Pheo";
            case "ADAM29" -> "Adam29";
            case "UFFR" -> "UFFR";
            case "VAER" -> "vaer";
            case "NOS" -> "Dr Nostalgia";
            case "DRILLGON" -> "Drillgon200";
            case "CIRNO" -> "Cirno";
            case "MICROWAVE" -> "Microwave";
            case "PEEP" -> "Peep";
            case "MELLOW" -> animatedMellowTitle();
            case "ABEL" -> "Abel1502";
            default -> name(Kind.BOBBLEHEAD, variant);
        };
    }

    /** Exact 1.7.10 GUIScreenBobble MELLOW title interpolation. */
    private static String animatedMellowTitle() {
        char[] source = "MELLOWARPEGGIATION".toCharArray();
        char[] target = "GEORGEWILLIAMPATON".toCharArray();
        boolean[] paired = new boolean[target.length];
        double t = Math.sin(System.currentTimeMillis() / 1500.0D) * 0.75D + 0.5D;
        List<LetterTarget> letters = new ArrayList<>();
        for (int sourceIndex = 0; sourceIndex < source.length; sourceIndex++) {
            for (int targetIndex = 0; targetIndex < target.length; targetIndex++) {
                if (source[sourceIndex] == target[targetIndex] && !paired[targetIndex]) {
                    letters.add(new LetterTarget(lerp(sourceIndex, targetIndex, t), source[sourceIndex]));
                    paired[targetIndex] = true;
                    break;
                }
            }
        }
        letters.sort(Comparator.comparingDouble(LetterTarget::position));
        StringBuilder title = new StringBuilder(letters.size());
        for (LetterTarget letter : letters) {
            title.append(letter.letter());
        }
        return title.toString();
    }

    private static double lerp(double from, double to, double progress) {
        progress = Math.max(0.0D, Math.min(progress, 1.0D));
        return from * (1.0D - progress) + to * progress;
    }

    private record LetterTarget(double position, char letter) {
    }

    private static String bobbleContribution(int variant) {
        return switch (name(Kind.BOBBLEHEAD, variant)) {
            case "BOB" -> "Hbm's Nuclear Tech Mod";
            case "FRIZZLE" -> "Weapon models";
            case "PU238" -> "Improved Tom impact mechanics";
            case "VT" -> "Balefire warhead model and general texturework";
            case "DOC" -> "Russian localization, lunar miner";
            case "BLUEHAT" -> "Textures";
            case "PHEO" -> "Deuterium machines, tantalium textures, Reliant Rocket";
            case "ADAM29" -> "Ethanol, liquid petroleum gas";
            case "VAER" -> "ZIRNOX";
            case "NOS" -> "SSG and Vortex models";
            case "DRILLGON" -> "1.12 Port";
            case "CIRNO" -> "the only multi layered skin i had";
            case "MICROWAVE" -> "OC Compatibility and massive RBMK/packet optimizations";
            case "PEEP" -> "Coilgun, Leadburster and Congo Lake models, BDCL QC";
            case "MELLOW" -> "NBT Structures, industrial lighting, animation tools";
            default -> null;
        };
    }

    private static String bobbleInscription(int variant) {
        return switch (name(Kind.BOBBLEHEAD, variant)) {
            case "STRENGTH" -> "It's essential to give your arguments impact.";
            case "PERCEPTION" -> "Only through observation will you perceive weakness.";
            case "ENDURANCE" -> "Always be ready to take one for the team.";
            case "CHARISMA" -> "Nothing says pizzaz like a winning smile.";
            case "INTELLIGENCE" -> "It takes the smartest individuals to realize$there's always more to learn.";
            case "AGILITY" -> "Never be afraid to dodge the sensitive issues.";
            case "LUCK" -> "There's only one way to give 110%.";
            case "BOB" -> "I know where you live, " + System.getProperty("user.name");
            case "FRIZZLE" -> "BLOOD IS FUEL";
            case "VT" -> "You cannot unfuck a horse.";
            case "DOC" -> "Perhaps the moon rocks were too expensive";
            case "BLUEHAT" -> "payday 2's deagle freeaim champ of the year 2022";
            case "PHEO" -> "RUN TO THE BEDROOM, ON THE SUITCASE ON THE LEFT,$YOU'LL FIND MY FAVORITE AXE";
            case "ADAM29" -> "You know, nukes are really quite beatiful.$It's like watching a star be born for a split second.";
            case "UFFR" -> "fried shrimp";
            case "VAER" -> "taken de family out to the weekend cigarette festival";
            case "NOS" -> "Take a picture, I'ma pose, paparazzi$I've been drinking, moving like a zombie";
            case "CIRNO" -> "No brain. Head empty.";
            case "MICROWAVE" -> "they call me the food heater$john optimization";
            case "PEEP" -> "Fluffy ears can't hide in ash, nor snow.";
            case "MELLOW" -> "Make something cool now, ask for permission later.";
            case "ABEL" -> "NANTO SUBARASHII";
            default -> null;
        };
    }

    private static String snowglobeInscription(int variant) {
        return switch (name(Kind.SNOWGLOBE, variant)) {
            case "RIVETCITY" -> "Welcome to Rivet City. Please wait while the bridge extends.";
            case "TENPENNYTOWER" -> "Tenpenny Tower is the brainchild of Allistair Tenpenny, a British refugee who came to the Capital Wasteland seeking his fortune.";
            case "LUCKY38" -> "My guess? Leads to a big cashout at some casino - and if the \"38\" on it is any indication... well... Lucky 38 it is.";
            case "SIERRAMADRE" -> "It's the moment you've been waiting for, the reason we're all here - the Gala Event, the Grand Opening of the Sierra Madre Casino.";
            case "PRYDWEN" -> "People of the Commonwealth. Do not interfere. Our intentions are peaceful. We are the Brotherhood of Steel.";
            default -> null;
        };
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
