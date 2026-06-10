package com.hbm.ntm.item;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.item.ArmorDashProvider;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FsbArmorItem extends ArmorItem implements ArmorDashProvider {
    private final ResourceLocation fsbMaterialId;
    private final List<FullSetEffect> fullSetEffects;
    private final boolean noHelmet;
    private final int dashCount;

    public FsbArmorItem(HbmArmorMaterials material, Type type, Properties properties, List<FullSetEffect> fullSetEffects) {
        this(material, type, properties, fullSetEffects, false, 0);
    }

    public FsbArmorItem(HbmArmorMaterials material, Type type, Properties properties, List<FullSetEffect> fullSetEffects,
            boolean noHelmet, int dashCount) {
        super(material, type, properties.stacksTo(1));
        this.fsbMaterialId = ResourceLocation.tryParse(material.getName());
        this.fullSetEffects = List.copyOf(fullSetEffects);
        this.noHelmet = noHelmet;
        this.dashCount = Math.max(0, dashCount);
    }

    public ResourceLocation fsbMaterialId(ItemStack stack) {
        return fsbMaterialId;
    }

    public boolean noHelmetForFsbSet(ItemStack chestplate) {
        return noHelmet;
    }

    public boolean isArmorEnabled(ItemStack stack) {
        return true;
    }

    public boolean hasFullSet(Player player) {
        return hasFullFsbSet(player, false);
    }

    public boolean hasFullSetIgnoreCharge(Player player) {
        return hasFullFsbSet(player, true);
    }

    @Override
    public int getDashes() {
        return dashCount;
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide && getType() == Type.CHESTPLATE && hasFullSet(player)) {
            for (FullSetEffect effect : fullSetEffects) {
                player.addEffect(effect.create());
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!fullSetEffects.isEmpty() || dashCount > 0) {
            tooltip.add(Component.literal("Full set bonus").withStyle(ChatFormatting.GOLD));
            for (FullSetEffect effect : fullSetEffects) {
                tooltip.add(Component.literal("  " + effect.tooltip()).withStyle(ChatFormatting.AQUA));
            }
            if (dashCount > 0) {
                tooltip.add(Component.literal("  Dashes: " + dashCount).withStyle(ChatFormatting.AQUA));
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public static boolean hasFullFsbSet(Player player, boolean ignoreCharge) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof FsbArmorItem chestplate)) {
            return false;
        }

        ResourceLocation material = chestplate.fsbMaterialId(chest);
        if (material == null) {
            return false;
        }

        EquipmentSlot[] slots = chestplate.noHelmetForFsbSet(chest)
                ? new EquipmentSlot[] {EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}
                : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        for (EquipmentSlot slot : slots) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!(armor.getItem() instanceof FsbArmorItem fsb)) {
                return false;
            }
            if (!material.equals(fsb.fsbMaterialId(armor))) {
                return false;
            }
            if (!ignoreCharge && !fsb.isArmorEnabled(armor)) {
                return false;
            }
        }
        return true;
    }

    public static FullSetEffect effect(MobEffect effect, int duration, int amplifier, String tooltip) {
        return effect(() -> effect, duration, amplifier, tooltip);
    }

    public static FullSetEffect effect(Supplier<? extends MobEffect> effect, int duration, int amplifier, String tooltip) {
        return new FullSetEffect(effect, duration, amplifier, tooltip);
    }

    public record FullSetEffect(Supplier<? extends MobEffect> effect, int duration, int amplifier, String tooltip) {
        public FullSetEffect {
            duration = Math.max(1, duration);
            amplifier = Math.max(0, amplifier);
            tooltip = tooltip == null || tooltip.isBlank() ? "Effect" : tooltip;
        }

        MobEffectInstance create() {
            return new MobEffectInstance(effect.get(), duration, amplifier, true, true);
        }
    }

    protected static ResourceLocation legacyMaterialId(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }
}
