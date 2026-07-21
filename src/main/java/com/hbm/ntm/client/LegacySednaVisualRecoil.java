package com.hbm.ntm.client;

import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.bullet.SednaReceiverConfig;
import com.hbm.ntm.bullet.SednaWeaponModEvaluator;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.item.SednaGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Client counterpart of ItemGunBaseNT.setupRecoil and ModEventHandlerClient's END client tick; packets stay local-player only. */
public final class LegacySednaVisualRecoil {
    private static final float DEFAULT_DECAY = 0.75F;
    private static final float DEFAULT_REBOUND = 0.25F;

    private static float recoilVertical;
    private static float recoilHorizontal;
    private static float recoilDecay = DEFAULT_DECAY;
    private static float recoilRebound = DEFAULT_REBOUND;
    private static float offsetVertical;
    private static float offsetHorizontal;

    /** Exact CYCLE-only receiver callback from HbmAnimationPacket.handleSedna. */
    public static void onCycle(SednaGunItem gun, int receiverIndex, int itemIndex) {
        SednaGunConfig.GunModeConfig mode = mode(gun, itemIndex);
        if (mode == null) {
            return;
        }
        SednaReceiverConfig receiver = mode.receivers().stream()
                .filter(candidate -> candidate.receiverIndex() == receiverIndex)
                .findFirst()
                .orElse(null);
        if (receiver == null) {
            return;
        }
        String handler = receiver.recoilHandlerName();
        ItemStack stack = Minecraft.getInstance().player == null ? ItemStack.EMPTY
                : Minecraft.getInstance().player.getMainHandItem();
        // WeaponModPolymerFurniture overrides Receiver.CON_ONRECOIL for either furniture variant.
        if (SednaWeaponModEvaluator.hasUpgrade(stack, itemIndex, SednaWeaponModEvaluator.ID_FURNITURE_GREEN)
                || SednaWeaponModEvaluator.hasUpgrade(stack, itemIndex, SednaWeaponModEvaluator.ID_FURNITURE_BLACK)) {
            handler = "WeaponModPolymerFurniture.LAMBDA_RECOIL_G3";
        }
        RecoilSpec spec = RecoilSpec.forHandler(handler);
        if (spec != null) {
            setupRecoil(spec.vertical(), spec.horizontal());
        }
    }

    /** Exact END tick recurrence from legacy ModEventHandlerClient. */
    public static void tick(Minecraft minecraft) {
        Player player = minecraft.player;
        if (!HbmClientConfig.visualRecoil() || player == null) {
            clear();
            return;
        }
        offsetVertical += recoilVertical;
        offsetHorizontal += recoilHorizontal;
        player.setXRot(player.getXRot() - recoilVertical);
        player.setYRot(player.getYRot() - recoilHorizontal);
        recoilVertical *= recoilDecay;
        recoilHorizontal *= recoilDecay;
        float verticalRebound = offsetVertical * recoilRebound;
        float horizontalRebound = offsetHorizontal * recoilRebound;
        offsetVertical -= verticalRebound;
        offsetHorizontal -= horizontalRebound;
        player.setXRot(player.getXRot() + verticalRebound);
        player.setYRot(player.getYRot() + horizontalRebound);
    }

    private static SednaGunConfig.GunModeConfig mode(SednaGunItem gun, int itemIndex) {
        return gun.gunConfig().configs().stream()
                .filter(candidate -> candidate.configIndex() == itemIndex)
                .findFirst()
                .orElse(null);
    }

    private static void setupRecoil(float vertical, float horizontal) {
        recoilVertical += vertical;
        recoilHorizontal += horizontal;
        recoilDecay = DEFAULT_DECAY;
        recoilRebound = DEFAULT_REBOUND;
    }

    private static void clear() {
        recoilVertical = 0.0F;
        recoilHorizontal = 0.0F;
        offsetVertical = 0.0F;
        offsetHorizontal = 0.0F;
    }

    private record RecoilSpec(float verticalBase, float verticalGaussian, float horizontalBase,
                              float horizontalGaussian) {
        float vertical() {
            return verticalBase + (verticalGaussian == 0.0F ? 0.0F : gaussian() * verticalGaussian);
        }

        float horizontal() {
            return horizontalBase + (horizontalGaussian == 0.0F ? 0.0F : gaussian() * horizontalGaussian);
        }

        private static float gaussian() {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.player == null ? 0.0F : (float) minecraft.player.getRandom().nextGaussian();
        }

        static RecoilSpec forHandler(String handler) {
            return switch (handler) {
                case "XFactory10ga.LAMBDA_RECOIL_DOUBLE_BARREL", "XFactory12ga.LAMBDA_RECOIL_MARESLEG",
                        "XFactory357.LAMBDA_RECOIL_ATLAS", "XFactory35800.LAMBDA_RECOIL_ABERRATOR",
                        "XFactory40mm.LAMBDA_RECOIL_GL", "XFactory44.LAMBDA_RECOIL_NOPIP",
                        "XFactoryAccelerator.LAMBDA_RECOIL_COILGUN",
                        "XFactoryBlackPowder.LAMBDA_RECOIL_PEPPERBOX", "XFactoryTool.LAMBDA_RECOIL_CT"
                        -> new RecoilSpec(10.0F, 0.0F, 0.0F, 1.5F);
                case "XFactory12ga.LAMBDA_RECOIL_LIBERATOR" -> new RecoilSpec(5.0F, 0.0F, 0.0F, 1.5F);
                case "XFactory12ga.LAMBDA_RECOIL_AUTOSHOTGUN" -> new RecoilSpec(1.5F, 1.5F, 0.0F, 0.5F);
                case "XFactory12ga.LAMBDA_RECOIL_SEXY", "XFactory50.LAMBDA_RECOIL_M2",
                        "XFactory762mm.LAMBDA_RECOIL_MINIGUN" -> new RecoilSpec(0.0F, 0.5F, 0.0F, 0.5F);
                case "XFactory22lr.LAMBDA_RECOIL_AM180", "XFactory556mm.LAMBDA_RECOIL_G3"
                        -> new RecoilSpec(0.0F, 0.25F, 0.0F, 0.25F);
                case "XFactory22lr.LAMBDA_RECOIL_STAR_F" -> new RecoilSpec(2.5F, 0.0F, 0.0F, 0.5F);
                case "XFactory357.LAMBDA_RECOIL_DANI" -> new RecoilSpec(5.0F, 0.0F, 0.0F, 0.75F);
                case "XFactory40mm.LAMBDA_RECOIL_MK108" -> new RecoilSpec(1.0F, 1.0F, 0.0F, 1.0F);
                case "XFactory44.LAMBDA_RECOIL_HENRY", "XFactory44.LAMBDA_RECOIL_HANGMAN"
                        -> new RecoilSpec(5.0F, 0.0F, 0.0F, 1.0F);
                case "XFactory50.LAMBDA_RECOIL_AMAT" -> new RecoilSpec(12.5F, 0.0F, 0.0F, 1.0F);
                case "XFactory556mm.LAMBDA_RECOIL_ZEBRA", "WeaponModPolymerFurniture.LAMBDA_RECOIL_G3"
                        -> new RecoilSpec(0.0F, 0.125F, 0.0F, 0.125F);
                case "XFactory75Bolt.LAMBDA_RECOIL_BOLT" -> new RecoilSpec(0.0F, 1.5F, 0.0F, 1.5F);
                case "XFactory762mm.LAMBDA_RECOIL_CARBINE" -> new RecoilSpec(5.0F, 0.0F, 0.0F, 0.5F);
                case "XFactory9mm.LAMBDA_RECOIL_GREASEGUN" -> new RecoilSpec(2.0F, 0.0F, 0.0F, 0.5F);
                case "XFactory9mm.LAMBDA_RECOIL_LAG" -> new RecoilSpec(5.0F, 0.0F, 0.0F, 1.5F);
                case "XFactory9mm.LAMBDA_RECOIL_UZI" -> new RecoilSpec(1.0F, 0.0F, 0.0F, 0.25F);
                case "XFactoryFolly.LAMBDA_RECOIL_FOLLY" -> new RecoilSpec(25.0F, 0.0F, 0.0F, 1.5F);
                // These legacy handlers intentionally do not call ItemGunBaseNT.setupRecoil.
                case "", "XFactory556mm.LAMBDA_RECOIL_STG", "XFactory762mm.LAMBDA_RECOIL_LACUNAE",
                        "XFactoryAccelerator.LAMBDA_RECOIL_TAU", "XFactoryCatapult.LAMBDA_RECOIL_FATMAN",
                        "XFactoryEnergy.LAMBDA_RECOIL_ENERGY", "XFactoryRocket.LAMBDA_RECOIL_ROCKET" -> null;
                default -> null;
            };
        }
    }

    private LegacySednaVisualRecoil() {
    }
}
