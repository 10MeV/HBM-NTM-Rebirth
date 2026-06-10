package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.util.HbmShadyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Shared formulas from the old RenderAccessoryUtility wearable renderers.
 */
public final class LegacyAccessoryRenderHelper {
    public static final float BIPED_MODEL_SCALE = 0.0625F;
    public static final int WING_MODEL_COUNT = 10;
    public static final int HB_MINECRAFT_WING_MODE = 2;
    public static final int NCR_WING_MODE = 3;
    public static final int POLAROID_CAPE_VARIANT_ID = 11;

    private static final List<CapeRule> CAPE_RULES = createCapeRules();

    public static AccessoryAngles accessoryAngles(LivingEntity entity, float partialTick) {
        float headYaw = Mth.lerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float bodyYaw = Mth.lerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float yaw = headYaw - bodyYaw;
        float wrappedYaw = Mth.wrapDegrees(yaw);
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        return new AccessoryAngles(headYaw, bodyYaw, yaw, wrappedYaw, pitch);
    }

    public static boolean shouldSneakModel(LivingEntity entity) {
        return entity != null && entity.isShiftKeyDown();
    }

    public static boolean validWingMode(int mode) {
        return mode >= 0 && mode < WING_MODEL_COUNT;
    }

    public static Optional<CapePlan> capePlanFor(Player player, boolean polaroidVariant) {
        if (player == null) {
            return Optional.empty();
        }
        String name = player.getGameProfile() == null ? "" : player.getGameProfile().getName();
        return capePlan(player.getUUID().toString(), name, polaroidVariant);
    }

    public static Optional<CapePlan> capePlan(String uuid, String name, boolean polaroidVariant) {
        String safeUuid = safe(uuid);
        for (CapeRule rule : CAPE_RULES) {
            if (uuidMatches(safeUuid, rule.uuid())) {
                String textureName = polaroidVariant && rule.polaroidTextureName() != null
                        ? rule.polaroidTextureName()
                        : rule.textureName();
                return Optional.of(new CapePlan(rule.legacyName(), capeTexture(textureName), textureName,
                        polaroidVariant && rule.polaroidTextureName() != null, false, false));
            }
        }

        if (HbmShadyUtil.CONTRIBUTORS.contains(safeUuid.toLowerCase(Locale.ROOT))) {
            return Optional.of(new CapePlan("contributors", capeTexture("CapeWiki"), "CapeWiki", false, true, false));
        }
        if (safe(name).startsWith("Player")) {
            return Optional.of(new CapePlan("Player*", capeTexture("CapeTest"), "CapeTest", false, false, true));
        }
        return Optional.empty();
    }

    public static Optional<AccessoryRenderPlan> specialAccessoryPlan(Player player, float partialTick) {
        if (player == null) {
            return Optional.empty();
        }
        String uuid = player.getUUID().toString();
        String name = player.getGameProfile() == null ? "" : player.getGameProfile().getName();
        if (matches(uuid, name, HbmShadyUtil.HB_MINECRAFT, "HbMinecraft")) {
            return Optional.of(accessoryPlan(AccessoryKind.WINGS, HB_MINECRAFT_WING_MODE, player, partialTick));
        }
        if (matches(uuid, name, HbmShadyUtil.THE_NCR, "the_NCR")) {
            return Optional.of(accessoryPlan(AccessoryKind.WINGS, NCR_WING_MODE, player, partialTick));
        }
        if (matches(uuid, name, HbmShadyUtil.BARNABY99_X, "pheo7")) {
            return Optional.of(accessoryPlan(AccessoryKind.AXE_PACK, -1, player, partialTick));
        }
        if (matches(uuid, name, HbmShadyUtil.LE_PEEPER_SAUVAGE, "LePeeperSauvage")) {
            return Optional.of(accessoryPlan(AccessoryKind.TAIL, -1, player, partialTick));
        }
        return Optional.empty();
    }

    public static AccessoryRenderPlan accessoryPlan(AccessoryKind kind, int wingMode, LivingEntity entity, float partialTick) {
        AccessoryKind safeKind = kind == null ? AccessoryKind.NONE : kind;
        int safeWingMode = safeKind == AccessoryKind.WINGS && validWingMode(wingMode) ? wingMode : -1;
        return new AccessoryRenderPlan(safeKind, safeWingMode, accessoryAngles(entity, partialTick),
                shouldSneakModel(entity), BIPED_MODEL_SCALE, safeKind.legacyModelClass());
    }

    public static ResourceLocation capeTexture(String legacyTextureName) {
        String textureName = safe(legacyTextureName)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "_");
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/capes/" + textureName + ".png");
    }

    public record AccessoryAngles(float headYaw, float bodyYaw, float yaw, float wrappedYaw, float pitch) {
    }

    public enum AccessoryKind {
        NONE(""),
        WINGS("ModelArmorWings"),
        AXE_PACK("ModelArmorWingsPheo"),
        TAIL("ModelArmorTailPeep");

        private final String legacyModelClass;

        AccessoryKind(String legacyModelClass) {
            this.legacyModelClass = legacyModelClass;
        }

        public String legacyModelClass() {
            return legacyModelClass;
        }
    }

    public record AccessoryRenderPlan(AccessoryKind kind, int wingMode, AccessoryAngles angles, boolean sneak,
                                      float scale, String legacyModelClass) {
        public boolean hasWingMode() {
            return wingMode >= 0;
        }
    }

    public record CapePlan(String matchedRule, ResourceLocation texture, String legacyTextureName,
                           boolean polaroidVariant, boolean contributorFallback, boolean playerNameFallback) {
    }

    public record CapeRule(String legacyName, String uuid, String textureName, String polaroidTextureName) {
        public CapeRule(String legacyName, String uuid, String textureName) {
            this(legacyName, uuid, textureName, null);
        }
    }

    private static List<CapeRule> createCapeRules() {
        List<CapeRule> rules = new ArrayList<>();
        rules.add(new CapeRule("HbMinecraft", HbmShadyUtil.HB_MINECRAFT, "CapeHbm3", "CapeHbm2"));
        rules.add(new CapeRule("Drillgon", HbmShadyUtil.DRILLGON, "CapeDrillgon"));
        rules.add(new CapeRule("Dafnik", HbmShadyUtil.DAFNIK, "CapeDafnik"));
        rules.add(new CapeRule("LPkukin", HbmShadyUtil.LPKUKIN, "CapeShield"));
        rules.add(new CapeRule("LordVertice", HbmShadyUtil.LORD_VERTICE, "CapeVertice_2"));
        rules.add(new CapeRule("CodeRed_", HbmShadyUtil.CODE_RED, "CapeRed"));
        rules.add(new CapeRule("dxmaster769", HbmShadyUtil.DXMASTER769, "CapeAyy"));
        rules.add(new CapeRule("Dr_Nostalgia", HbmShadyUtil.DR_NOSTALGIA, "CapeNostalgia2"));
        rules.add(new CapeRule("Samino2", HbmShadyUtil.SAMINO2, "CapeSam"));
        rules.add(new CapeRule("Hoboy03new", HbmShadyUtil.HOBOY03NEW, "CapeHoboy_mk3"));
        rules.add(new CapeRule("Dragon59MC", HbmShadyUtil.DRAGON59MC, "CapeMaster"));
        rules.add(new CapeRule("Steelcourage", HbmShadyUtil.STEELCOURAGE, "CapeMek"));
        rules.add(new CapeRule("ZippySqrl", HbmShadyUtil.ZIPPY_SQRL, "CapeZippySqrl"));
        rules.add(new CapeRule("Schrabby", HbmShadyUtil.SCHRABBY, "CapeSchrabbyAlt"));
        rules.add(new CapeRule("SweatySwiggs", HbmShadyUtil.SWEATY_SWIGGS, "CapeSweatySwiggs"));
        rules.add(new CapeRule("Doctor17", HbmShadyUtil.DOCTOR17, "CapeDoctor17"));
        rules.add(new CapeRule("Doctor17PH", HbmShadyUtil.DOCTOR17PH, "CapeDoctor17"));
        rules.add(new CapeRule("ShimmeringBlaze", HbmShadyUtil.SHIMMERING_BLAZE, "CapeBlaze", "CapeBlaze2"));
        rules.add(new CapeRule("FifeMiner", HbmShadyUtil.FIFE_MINER, "CapeLeftNugget"));
        rules.add(new CapeRule("lag_add", HbmShadyUtil.LAG_ADD, "CapeRightNugget"));
        rules.add(new CapeRule("Tankish", HbmShadyUtil.TANKISH, "CapeTankish"));
        rules.add(new CapeRule("FrizzleFrazzle", HbmShadyUtil.FRIZZLE_FRAZZLE, "CapeFrizzleFrazzle"));
        rules.add(new CapeRule("Ma118", HbmShadyUtil.MA118, "CapeVaer"));
        rules.add(new CapeRule("Adam29Adam29", HbmShadyUtil.ADAM29ADAM29, "CapeAdam"));
        rules.add(new CapeRule("Alcater", HbmShadyUtil.ALCATER, "CapeAlcater"));
        rules.add(new CapeRule("ege444", HbmShadyUtil.EGE444, "CapeJame"));
        return List.copyOf(rules);
    }

    private static boolean matches(String uuid, String name, String targetUuid, String targetName) {
        return uuidMatches(uuid, targetUuid) || safe(name).equals(targetName);
    }

    private static boolean uuidMatches(String uuid, String targetUuid) {
        return safe(uuid).equalsIgnoreCase(safe(targetUuid));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private LegacyAccessoryRenderHelper() {
    }
}
