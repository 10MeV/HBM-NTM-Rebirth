package com.hbm.ntm.item.missile;

import com.hbm.ntm.client.renderer.LegacyItemRendererBridge;
import com.hbm.ntm.client.renderer.MissileItemRenderer;
import com.hbm.ntm.api.entity.LegacyMissileRadarProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class MissileItem extends Item {
    private final FormFactor formFactor;
    private final Tier tier;
    private final Fuel fuel;
    private final int fuelCap;
    private final boolean launchable;

    public MissileItem(Properties properties, FormFactor formFactor, Tier tier) {
        this(properties, formFactor, tier, formFactor.defaultFuel(), formFactor.defaultFuel().defaultCap(), true);
    }

    public MissileItem(Properties properties, FormFactor formFactor, Tier tier, Fuel fuel) {
        this(properties, formFactor, tier, fuel, fuel.defaultCap(), true);
    }

    public MissileItem(Properties properties, FormFactor formFactor, Tier tier, Fuel fuel, int fuelCap, boolean launchable) {
        super(properties);
        this.formFactor = formFactor;
        this.tier = tier;
        this.fuel = fuel;
        this.fuelCap = fuelCap;
        this.launchable = launchable;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.missile.tier." + tier.serializedName())
                .withStyle(ChatFormatting.ITALIC));
        if (!launchable) {
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.missile.not_launchable")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.missile.fuel", fuel.display())
                .withStyle(fuel.color()));
        if (fuelCap > 0) {
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.missile.fuel_capacity", fuelCap));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        if (usesObjItemRenderer()) {
            LegacyItemRendererBridge.accept(consumer, () -> MissileItemRenderer.INSTANCE);
        }
    }

    public FormFactor formFactor() {
        return formFactor;
    }

    public Tier tier() {
        return tier;
    }

    public Fuel fuel() {
        return fuel;
    }

    public int fuelCap() {
        return fuelCap;
    }

    public boolean launchable() {
        return launchable;
    }

    public boolean usesObjItemRenderer() {
        return formFactor != FormFactor.OTHER || launchable;
    }

    public LegacyMissileRadarProfile radarProfile() {
        return tier.radarProfile();
    }

    public enum FormFactor {
        ABM(Fuel.SOLID),
        MICRO(Fuel.SOLID),
        V2(Fuel.ETHANOL_PEROXIDE),
        STRONG(Fuel.KEROSENE_PEROXIDE),
        HUGE(Fuel.KEROSENE_LOXY),
        ATLAS(Fuel.JETFUEL_LOXY),
        OTHER(Fuel.KEROSENE_PEROXIDE);

        private final Fuel defaultFuel;

        FormFactor(Fuel defaultFuel) {
            this.defaultFuel = defaultFuel;
        }

        public Fuel defaultFuel() {
            return defaultFuel;
        }
    }

    public enum Tier {
        TIER0(0, LegacyMissileRadarProfile.TIER0),
        TIER1(1, LegacyMissileRadarProfile.TIER1),
        TIER2(2, LegacyMissileRadarProfile.TIER2),
        TIER3(3, LegacyMissileRadarProfile.TIER3),
        TIER4(4, LegacyMissileRadarProfile.TIER4);

        private final int legacyTier;
        private final LegacyMissileRadarProfile radarProfile;

        Tier(int legacyTier, LegacyMissileRadarProfile radarProfile) {
            this.legacyTier = legacyTier;
            this.radarProfile = radarProfile;
        }

        public int legacyTier() {
            return legacyTier;
        }

        public String serializedName() {
            return name().toLowerCase();
        }

        public LegacyMissileRadarProfile radarProfile() {
            return radarProfile;
        }
    }

    public enum Fuel {
        SOLID("tooltip.hbm_ntm_rebirth.missile.fuel.solid", ChatFormatting.GOLD, 0),
        ETHANOL_PEROXIDE("tooltip.hbm_ntm_rebirth.missile.fuel.ethanol_peroxide", ChatFormatting.AQUA, 4_000),
        KEROSENE_PEROXIDE("tooltip.hbm_ntm_rebirth.missile.fuel.kerosene_peroxide", ChatFormatting.BLUE, 8_000),
        KEROSENE_LOXY("tooltip.hbm_ntm_rebirth.missile.fuel.kerosene_loxy", ChatFormatting.LIGHT_PURPLE, 12_000),
        JETFUEL_LOXY("tooltip.hbm_ntm_rebirth.missile.fuel.jetfuel_loxy", ChatFormatting.RED, 16_000);

        private final String translationKey;
        private final ChatFormatting color;
        private final int defaultCap;

        Fuel(String translationKey, ChatFormatting color, int defaultCap) {
            this.translationKey = translationKey;
            this.color = color;
            this.defaultCap = defaultCap;
        }

        public Component display() {
            return Component.translatable(translationKey);
        }

        public ChatFormatting color() {
            return color;
        }

        public int defaultCap() {
            return defaultCap;
        }
    }
}
