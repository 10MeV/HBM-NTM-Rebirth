package com.hbm.ntm.radiation;

import com.hbm.ntm.api.item.GasMask;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmTuple.Pair;
import com.hbm.ntm.util.HbmWorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ArmorUtil {
    @Deprecated public static final List<Pair<Item, HazardClass[]>> external = new ExternalProtectionList();
    public static final String FILTER_KEY = "hfrFilter";
    public static final String FILTERK_KEY = FILTER_KEY;
    public static final int ASH_EXPOSURE_LIMIT_ASH_GLASSES = 64;
    public static final int ASH_EXPOSURE_LIMIT_SAND_OR_LIGHT = 192;
    public static final int ASH_EXPOSURE_LIMIT_UNPROTECTED = 243;
    public static final HazardClass[] FULL_NO_LIGHT = new HazardClass[] {
            HazardClass.PARTICLE_COARSE,
            HazardClass.PARTICLE_FINE,
            HazardClass.GAS_LUNG,
            HazardClass.BACTERIA,
            HazardClass.GAS_BLISTERING,
            HazardClass.GAS_MONOXIDE,
            HazardClass.SAND
    };
    public static final HazardClass[] FULL_PACKAGE = new HazardClass[] {
            HazardClass.PARTICLE_COARSE,
            HazardClass.PARTICLE_FINE,
            HazardClass.GAS_LUNG,
            HazardClass.BACTERIA,
            HazardClass.GAS_BLISTERING,
            HazardClass.GAS_MONOXIDE,
            HazardClass.LIGHT,
            HazardClass.SAND
    };

    private static final Set<String> FARADAY_KEYWORDS = Set.of(
            "chainmail", "iron", "gold", "netherite", "steel", "titanium", "alloy",
            "lead", "copper", "hazmat", "rubber", "schrabidium", "silver",
            "platinum", "tin", "liquidator", "euphemium", "cmb", "bronze",
            "electrum", "t45", "t51", "bj", "starmetal", "hev", "ajr",
            "rpa", "spacesuit", "paa", "security", "cobalt");

    public static void register() {
        HazmatRegistry.registerDefaultProtections();
    }

    public static boolean checkForHazmat(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (entity.hasEffect(ModEffects.MUTATION.get())) {
            return true;
        }
        return checkLegacyArmorSet(entity, "hazmat")
                || checkLegacyArmorSet(entity, "hazmat_helmet_red", "hazmat_plate_red", "hazmat_legs_red",
                "hazmat_boots_red")
                || checkLegacyArmorSet(entity, "hazmat_helmet_grey", "hazmat_plate_grey", "hazmat_legs_grey",
                "hazmat_boots_grey")
                || checkLegacyArmorSet(entity, "schrabidium")
                || checkForHaz2(entity);
    }

    public static boolean checkForHaz2(LivingEntity entity) {
        return checkLegacyArmorSet(entity, "hazmat_paa")
                || checkLegacyArmorSet(entity, "liquidator")
                || checkLegacyArmorSet(entity, "euphemium")
                || checkLegacyArmorSet(entity, "rpa")
                || checkLegacyArmorSet(entity, "fau")
                || checkLegacyArmorSet(entity, "dns");
    }

    public static boolean hasFineParticleProtection(LivingEntity entity) {
        return hasProtection(entity, HazardClass.PARTICLE_FINE);
    }

    public static boolean hasCoarseParticleProtection(LivingEntity entity) {
        return hasProtection(entity, HazardClass.PARTICLE_COARSE);
    }

    public static boolean hasMonoxideGasProtection(LivingEntity entity) {
        return hasProtection(entity, HazardClass.GAS_MONOXIDE);
    }

    public static boolean hasLungGasProtection(LivingEntity entity) {
        return hasProtection(entity, HazardClass.GAS_LUNG);
    }

    public static boolean hasBacteriaProtection(LivingEntity entity) {
        return hasProtection(entity, HazardClass.BACTERIA);
    }

    public static boolean hasBlisteringGasProtection(LivingEntity entity) {
        return hasProtection(entity, HazardClass.GAS_BLISTERING);
    }

    public static boolean hasLightProtection(LivingEntity entity) {
        return hasProtection(entity, HazardClass.LIGHT);
    }

    public static boolean hasSandProtection(LivingEntity entity) {
        return hasProtection(entity, HazardClass.SAND);
    }

    public static boolean hasPollutionPoisonProtection(LivingEntity entity) {
        return hasBlisteringGasProtection(entity);
    }

    public static boolean hasPollutionLeadProtection(LivingEntity entity) {
        return hasFineParticleProtection(entity);
    }

    public static boolean hasSootLungProtection(LivingEntity entity) {
        return hasCoarseParticleProtection(entity);
    }

    public static boolean hasBlindingProtection(LivingEntity entity) {
        return hasLightProtection(entity);
    }

    public static boolean hasAshVisionPartialProtection(LivingEntity entity) {
        return hasAnyProtection(entity, 3, HazardClass.SAND, HazardClass.LIGHT);
    }

    public static boolean checkForAshGlasses(LivingEntity entity) {
        return checkArmorPiece(entity, legacyItem("ashglasses"), 3);
    }

    public static int getAshExposureLimit(LivingEntity entity) {
        if (checkForAshGlasses(entity)) {
            return ASH_EXPOSURE_LIMIT_ASH_GLASSES;
        }
        if (hasAshVisionPartialProtection(entity)) {
            return ASH_EXPOSURE_LIMIT_SAND_OR_LIGHT;
        }
        return ASH_EXPOSURE_LIMIT_UNPROTECTED;
    }

    public static boolean hasProtection(LivingEntity entity, HazardClass hazardClass) {
        return HazmatRegistry.getProtectionFromItem(getHelmet(entity), entity).contains(hazardClass);
    }

    public static boolean hasProtection(LivingEntity entity, EquipmentSlot slot, HazardClass hazardClass) {
        return HazmatRegistry.hasProtection(entity, slot, hazardClass);
    }

    public static boolean hasProtection(LivingEntity entity, int legacyArmorSlot, HazardClass hazardClass) {
        EquipmentSlot slot = tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && hasProtection(entity, slot, hazardClass);
    }

    public static boolean hasAllProtection(LivingEntity entity, HazardClass... hazardClasses) {
        Set<HazardClass> protections = HazmatRegistry.getProtectionFromItem(getHelmet(entity), entity);
        for (HazardClass hazardClass : hazardClasses) {
            if (!protections.contains(hazardClass)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasAllProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazardClasses) {
        return HazmatRegistry.hasAllProtection(entity, slot, hazardClasses);
    }

    public static boolean hasAllProtection(LivingEntity entity, int legacyArmorSlot, HazardClass... hazardClasses) {
        EquipmentSlot slot = tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && hasAllProtection(entity, slot, hazardClasses);
    }

    public static boolean hasAnyProtection(LivingEntity entity, HazardClass... hazardClasses) {
        Set<HazardClass> protections = HazmatRegistry.getProtectionFromItem(getHelmet(entity), entity);
        for (HazardClass hazardClass : hazardClasses) {
            if (protections.contains(hazardClass)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnyProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazardClasses) {
        return HazmatRegistry.hasAnyProtection(entity, slot, hazardClasses);
    }

    public static boolean hasAnyProtection(LivingEntity entity, int legacyArmorSlot, HazardClass... hazardClasses) {
        EquipmentSlot slot = tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && hasAnyProtection(entity, slot, hazardClasses);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, HazardClass hazardClass,
                                                       int filterDamage) {
        boolean protectedByArmor = hasProtection(entity, hazardClass);
        damageFilterIfProtected(entity, protectedByArmor, filterDamage);
        return protectedByArmor;
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot,
                                                       HazardClass hazardClass, int filterDamage) {
        boolean protectedByArmor = hasProtection(entity, slot, hazardClass);
        damageFilterIfProtected(entity, protectedByArmor, filterDamage);
        return protectedByArmor;
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, int legacyArmorSlot,
                                                       HazardClass hazardClass, int filterDamage) {
        EquipmentSlot slot = tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && hasProtectionAndDamageFilter(entity, slot, hazardClass, filterDamage);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
                                                          HazardClass... hazardClasses) {
        boolean protectedByArmor = hasAllProtection(entity, hazardClasses);
        damageFilterIfProtected(entity, protectedByArmor, filterDamage);
        return protectedByArmor;
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot,
                                                          int filterDamage, HazardClass... hazardClasses) {
        boolean protectedByArmor = hasAllProtection(entity, slot, hazardClasses);
        damageFilterIfProtected(entity, protectedByArmor, filterDamage);
        return protectedByArmor;
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int legacyArmorSlot,
                                                          int filterDamage, HazardClass... hazardClasses) {
        EquipmentSlot slot = tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && hasAllProtectionAndDamageFilter(entity, slot, filterDamage, hazardClasses);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
                                                          HazardClass... hazardClasses) {
        boolean protectedByArmor = hasAnyProtection(entity, hazardClasses);
        damageFilterIfProtected(entity, protectedByArmor, filterDamage);
        return protectedByArmor;
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot,
                                                          int filterDamage, HazardClass... hazardClasses) {
        boolean protectedByArmor = hasAnyProtection(entity, slot, hazardClasses);
        damageFilterIfProtected(entity, protectedByArmor, filterDamage);
        return protectedByArmor;
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int legacyArmorSlot,
                                                          int filterDamage, HazardClass... hazardClasses) {
        EquipmentSlot slot = tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && hasAnyProtectionAndDamageFilter(entity, slot, filterDamage, hazardClasses);
    }

    public static boolean hasFineParticleProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return hasProtectionAndDamageFilter(entity, HazardClass.PARTICLE_FINE, filterDamage);
    }

    public static boolean hasCoarseParticleProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return hasProtectionAndDamageFilter(entity, HazardClass.PARTICLE_COARSE, filterDamage);
    }

    public static boolean hasMonoxideGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return hasProtectionAndDamageFilter(entity, HazardClass.GAS_MONOXIDE, filterDamage);
    }

    public static boolean hasLungGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return hasProtectionAndDamageFilter(entity, HazardClass.GAS_LUNG, filterDamage);
    }

    public static boolean hasBacteriaProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return hasProtectionAndDamageFilter(entity, HazardClass.BACTERIA, filterDamage);
    }

    public static boolean hasBlisteringGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return hasProtectionAndDamageFilter(entity, HazardClass.GAS_BLISTERING, filterDamage);
    }

    public static boolean hasToxinProtection(LivingEntity entity, HazardClass hazardClass,
                                             boolean requiresFullBodyProtection, boolean apply) {
        return hasToxinProtection(entity, hazardClass, requiresFullBodyProtection, apply ? 1 : 0);
    }

    public static boolean hasToxinProtection(LivingEntity entity, HazardClass hazardClass,
                                             boolean requiresFullBodyProtection, int filterDamage) {
        boolean hasMask = hazardClass == null;
        boolean hasSuit = !requiresFullBodyProtection;

        if (hazardClass != null && hasAllProtection(entity, 3, hazardClass)) {
            damageFilterIfProtected(entity, true, filterDamage);
            hasMask = true;
        }

        if (requiresFullBodyProtection && checkForHazmat(entity)) {
            hasSuit = true;
        }

        return hasMask && hasSuit;
    }

    public static boolean checkArmor(LivingEntity entity, Item... armor) {
        if (entity == null || armor == null || armor.length < 4) {
            return false;
        }
        for (int index = 0; index < 4; index++) {
            if (!checkArmorPiece(entity, armor[index], 3 - index)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkArmorPiece(LivingEntity entity, Item armor, int legacyArmorSlot) {
        if (entity == null || armor == null || checkArmorNull(entity, legacyArmorSlot)) {
            return false;
        }
        EquipmentSlot slot = tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && entity.getItemBySlot(slot).getItem() == armor;
    }

    public static boolean checkArmorNull(LivingEntity entity, int legacyArmorSlot) {
        EquipmentSlot slot = tryLegacyEquipmentSlot(legacyArmorSlot);
        return entity == null || slot == null || entity.getItemBySlot(slot).isEmpty();
    }

    private static void damageFilterIfProtected(LivingEntity entity, boolean protectedByArmor, int filterDamage) {
        if (protectedByArmor && filterDamage > 0) {
            damageGasMaskFilter(entity, filterDamage);
        }
    }

    public static void damageSuit(LivingEntity entity, int legacyArmorSlot, int amount) {
        EquipmentSlot slot = tryLegacyEquipmentSlot(legacyArmorSlot);
        if (slot != null) {
            damageSuit(entity, slot, amount);
        }
    }

    public static void damageSuitAll(LivingEntity entity, int amount) {
        for (int slot = 0; slot < 4; slot++) {
            damageSuit(entity, slot, amount);
        }
    }

    public static void damageSuit(LivingEntity entity, EquipmentSlot slot, int amount) {
        if (entity == null || slot == null || amount <= 0) {
            return;
        }
        ItemStack stack = entity.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return;
        }
        stack.hurtAndBreak(amount, entity, brokenEntity -> brokenEntity.broadcastBreakEvent(slot));
    }

    public static void damageGasMaskFilter(LivingEntity entity, int damage) {
        WornGasMask wornMask = getWornGasMask(entity);
        if (!wornMask.isPresent()) {
            return;
        }
        wornMask.mask().damageFilter(wornMask.maskStack(), entity, Math.max(0, damage));
        wornMask.persist();
    }

    public static WornGasMask getWornGasMask(LivingEntity entity) {
        ItemStack helmet = getHelmet(entity);
        if (helmet.isEmpty()) {
            return WornGasMask.EMPTY;
        }
        if (helmet.getItem() instanceof GasMask mask) {
            return new WornGasMask(helmet, helmet, mask, false);
        }
        if (ArmorModHandler.hasMods(helmet)) {
            ItemStack mod = ArmorModHandler.pryMod(helmet, ArmorModHandler.helmet_only);
            if (!mod.isEmpty() && mod.getItem() instanceof GasMask mask) {
                return new WornGasMask(helmet, mod, mask, true);
            }
        }
        return WornGasMask.EMPTY;
    }

    public static ItemStack getWornGasMaskFilter(LivingEntity entity) {
        WornGasMask wornMask = getWornGasMask(entity);
        return wornMask.isPresent() ? wornMask.mask().getFilter(wornMask.maskStack(), entity) : ItemStack.EMPTY;
    }

    public static boolean installWornGasMaskFilter(LivingEntity entity, ItemStack filter) {
        WornGasMask wornMask = getWornGasMask(entity);
        if (!wornMask.isPresent() || filter == null || filter.isEmpty()
                || !wornMask.mask().isFilterApplicable(wornMask.maskStack(), entity, filter)) {
            return false;
        }
        wornMask.mask().installFilter(wornMask.maskStack(), entity, filter.copyWithCount(1));
        wornMask.persist();
        return true;
    }

    public static GasMaskFilterInstallResult installWornGasMaskFilter(Player player, ItemStack heldFilter) {
        WornGasMask wornMask = getWornGasMask(player);
        if (!wornMask.isPresent() || heldFilter == null || heldFilter.isEmpty()
                || !wornMask.mask().isFilterApplicable(wornMask.maskStack(), player, heldFilter)) {
            return GasMaskFilterInstallResult.pass(heldFilter);
        }

        ItemStack current = wornMask.mask().getFilter(wornMask.maskStack(), player);
        wornMask.mask().installFilter(wornMask.maskStack(), player, heldFilter.copyWithCount(1));
        wornMask.persist();

        if (current != null && !current.isEmpty()) {
            return GasMaskFilterInstallResult.installed(current);
        }
        if (!player.getAbilities().instabuild) {
            heldFilter.shrink(1);
        }
        return GasMaskFilterInstallResult.installed(heldFilter);
    }

    public static ItemStack removeWornGasMaskFilter(LivingEntity entity) {
        WornGasMask wornMask = getWornGasMask(entity);
        if (!wornMask.isPresent()) {
            return ItemStack.EMPTY;
        }
        ItemStack filter = wornMask.mask().getFilter(wornMask.maskStack(), entity);
        if (filter == null || filter.isEmpty()) {
            return ItemStack.EMPTY;
        }
        removeFilter(wornMask.maskStack());
        wornMask.persist();
        return filter;
    }

    public static ItemStack getGasMaskFilterRecursively(ItemStack mask, LivingEntity entity) {
        ItemStack filter = getGasMaskFilter(mask);
        if (filter.isEmpty() && ArmorModHandler.hasMods(mask)) {
            ItemStack mod = ArmorModHandler.pryMod(mask, ArmorModHandler.helmet_only);
            if (!mod.isEmpty() && mod.getItem() instanceof GasMask gasMask) {
                filter = gasMask.getFilter(mod, entity);
            }
        }
        return filter;
    }

    public static ItemStack getGasMaskFilter(ItemStack mask) {
        if (mask == null || mask.isEmpty() || !mask.hasTag()) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = mask.getTag();
        if (tag == null || !tag.contains(FILTER_KEY, Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag.getCompound(FILTER_KEY));
    }

    public static void installGasMaskFilter(ItemStack mask, ItemStack filter) {
        if (mask == null || mask.isEmpty() || filter == null || filter.isEmpty()) {
            return;
        }
        mask.getOrCreateTag().put(FILTER_KEY, filter.copyWithCount(1).save(new CompoundTag()));
    }

    public static void removeFilter(ItemStack mask) {
        if (mask == null || mask.isEmpty() || !mask.hasTag()) {
            return;
        }
        mask.getOrCreateTag().remove(FILTER_KEY);
    }

    public static void damageGasMaskFilter(ItemStack mask, int damage) {
        ItemStack filter = getGasMaskFilter(mask);
        if (filter.isEmpty() && ArmorModHandler.hasMods(mask)) {
            ItemStack mod = ArmorModHandler.pryMod(mask, ArmorModHandler.helmet_only);
            if (!mod.isEmpty() && mod.getItem() instanceof GasMask) {
                damageGasMaskFilter(mod, damage);
                ArmorModHandler.applyMod(mask, mod);
            }
            return;
        }
        if (filter.isEmpty() || !filter.isDamageableItem()) {
            return;
        }
        filter.setDamageValue(filter.getDamageValue() + Math.max(0, damage));
        if (filter.getDamageValue() > filter.getMaxDamage()) {
            removeFilter(mask);
        } else {
            installGasMaskFilter(mask, filter);
        }
    }

    public static void addGasMaskTooltip(ItemStack maskStack, @Nullable LivingEntity entity,
                                         List<Component> tooltip, TooltipFlag flag) {
        if (maskStack == null || maskStack.isEmpty() || !(maskStack.getItem() instanceof GasMask mask)) {
            return;
        }
        ItemStack filter = mask.getFilter(maskStack, entity);
        if (filter == null || filter.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.gasmask.no_filter")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.gasmask.installed_filter")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("  ")
                .append(filter.getHoverName())
                .append(filterDurabilitySuffix(filter)));

        List<Component> filterLore = new ArrayList<>();
        filter.getItem().appendHoverText(filter, null, filterLore, flag);
        for (Component line : filterLore) {
            tooltip.add(Component.literal("  ").withStyle(ChatFormatting.YELLOW).append(line));
        }
    }

    public static void addGasMaskBlacklistTooltip(ItemStack maskStack, @Nullable LivingEntity entity,
                                                  List<Component> tooltip) {
        if (maskStack == null || maskStack.isEmpty() || !(maskStack.getItem() instanceof GasMask mask)) {
            return;
        }
        List<HazardClass> blacklist = mask.getBlacklist(maskStack, entity);
        if (blacklist.isEmpty()) {
            return;
        }
        tooltip.add(Component.translatable("hazard.neverProtects").withStyle(ChatFormatting.RED));
        for (HazardClass hazardClass : blacklist) {
            tooltip.add(Component.literal(" -")
                    .append(Component.translatable(hazardClass.translationKey()))
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }

    public static boolean isWearingEmptyMask(Player player) {
        WornGasMask wornMask = getWornGasMask(player);
        if (!wornMask.isPresent()) {
            return false;
        }
        ItemStack filter = wornMask.mask().getFilter(wornMask.maskStack(), player);
        return filter == null || filter.isEmpty();
    }

    private static Component filterDurabilitySuffix(ItemStack filter) {
        if (!filter.isDamageableItem() || filter.getMaxDamage() <= 0) {
            return Component.empty();
        }
        int remaining = Math.max(0, filter.getMaxDamage() - filter.getDamageValue());
        int percent = remaining * 100 / filter.getMaxDamage();
        return Component.literal(" (" + percent + "%)");
    }

    public static boolean checkForMkuProtection(LivingEntity entity) {
        return checkForHaz2(entity) && hasBacteriaProtection(entity);
    }

    public static boolean checkForAsbestos(LivingEntity entity) {
        return checkLegacyArmorSet(entity, "asbestos");
    }

    public static boolean checkForFaraday(Player player) {
        if (player == null) {
            return false;
        }
        int pieces = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            if (!stack.isEmpty() && isFaradayArmor(stack)) {
                pieces++;
            }
        }
        return pieces == 4;
    }

    public static boolean checkForDigamma(Player player) {
        if (player == null) {
            return false;
        }
        return player.hasEffect(ModEffects.STABILITY.get())
                || checkLegacyArmorSet(player, "fau")
                || checkLegacyArmorSet(player, "dns");
    }

    public static boolean checkForDigamma2(Player player) {
        if (player == null) {
            return false;
        }
        return checkLegacyArmorSet(player, "robes")
                && player.hasEffect(ModEffects.STABILITY.get())
                && hasIronCladdingOnEveryArmorPiece(player)
                && player.getMaxHealth() < 3.0F;
    }

    public static boolean checkForFiend(Player player) {
        return checkArmorPiece(player, legacyItem("jackt"), 2)
                && HbmWorldUtil.checkForHeld(player, legacyItem("shimmer_sledge"));
    }

    public static boolean checkForFiend2(Player player) {
        return checkArmorPiece(player, legacyItem("jackt2"), 2)
                && HbmWorldUtil.checkForHeld(player, legacyItem("shimmer_axe"));
    }

    public static boolean isFaradayArmor(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String key = stack.getItem().getDescriptionId().toLowerCase(Locale.US);
        for (String metal : FARADAY_KEYWORDS) {
            if (key.contains(metal)) {
                return true;
            }
        }
        return HazmatRegistry.getCladding(stack) > 0.0D;
    }

    private static ItemStack getHelmet(LivingEntity entity) {
        if (entity == null) {
            return ItemStack.EMPTY;
        }
        return entity.getItemBySlot(EquipmentSlot.HEAD);
    }

    public static EquipmentSlot legacyEquipmentSlot(int legacyArmorSlot) {
        return switch (legacyArmorSlot) {
            case 0 -> EquipmentSlot.FEET;
            case 1 -> EquipmentSlot.LEGS;
            case 2 -> EquipmentSlot.CHEST;
            case 3 -> EquipmentSlot.HEAD;
            default -> throw new IllegalArgumentException("Unknown legacy armor slot: " + legacyArmorSlot);
        };
    }

    @Nullable
    public static EquipmentSlot tryLegacyEquipmentSlot(int legacyArmorSlot) {
        return switch (legacyArmorSlot) {
            case 0 -> EquipmentSlot.FEET;
            case 1 -> EquipmentSlot.LEGS;
            case 2 -> EquipmentSlot.CHEST;
            case 3 -> EquipmentSlot.HEAD;
            default -> null;
        };
    }

    private static boolean checkLegacyArmorSet(LivingEntity entity, String prefix) {
        return checkLegacyArmorSet(entity, prefix + "_helmet", prefix + "_plate", prefix + "_legs",
                prefix + "_boots");
    }

    private static boolean checkLegacyArmorSet(LivingEntity entity, String helmetName, String chestName,
                                               String legsName, String bootsName) {
        Item helmet = legacyItem(helmetName);
        Item chest = legacyItem(chestName);
        Item legs = legacyItem(legsName);
        Item boots = legacyItem(bootsName);
        if (helmet == null || chest == null || legs == null || boots == null) {
            return false;
        }
        return checkArmor(entity, helmet, chest, legs, boots);
    }

    private static boolean hasIronCladdingOnEveryArmorPiece(Player player) {
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.isEmpty() || !ArmorModHandler.hasMods(armor)) {
                return false;
            }
            ItemStack cladding = ArmorModHandler.pryMod(armor, ArmorModHandler.cladding);
            if (cladding.isEmpty() || cladding.getItem() != ModItems.CLADDING_IRON.get()) {
                return false;
            }
        }
        return true;
    }

    private static Item legacyItem(String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        return item != null && item.isPresent() ? item.get() : null;
    }

    private static final class ExternalProtectionList extends AbstractList<Pair<Item, HazardClass[]>> {
        private final List<Pair<Item, HazardClass[]>> entries = new ArrayList<>();

        @Override
        public Pair<Item, HazardClass[]> get(int index) {
            Pair<Item, HazardClass[]> entry = entries.get(index);
            return new Pair<>(entry.getKey(), entry.getValue().clone());
        }

        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public void add(int index, Pair<Item, HazardClass[]> element) {
            if (element == null || element.getKey() == null || element.getValue() == null) {
                return;
            }
            Pair<Item, HazardClass[]> copy = new Pair<>(element.getKey(), element.getValue().clone());
            entries.add(index, copy);
            HazmatRegistry.registerExternalProtection(copy.getKey(), copy.getValue());
        }

        @Override
        public Pair<Item, HazardClass[]> set(int index, Pair<Item, HazardClass[]> element) {
            Pair<Item, HazardClass[]> previous = entries.get(index);
            if (element == null || element.getKey() == null || element.getValue() == null) {
                return new Pair<>(previous.getKey(), previous.getValue().clone());
            }
            Pair<Item, HazardClass[]> copy = new Pair<>(element.getKey(), element.getValue().clone());
            entries.set(index, copy);
            HazmatRegistry.registerExternalProtection(copy.getKey(), copy.getValue());
            return new Pair<>(previous.getKey(), previous.getValue().clone());
        }

        @Override
        public Pair<Item, HazardClass[]> remove(int index) {
            Pair<Item, HazardClass[]> previous = entries.remove(index);
            return new Pair<>(previous.getKey(), previous.getValue().clone());
        }
    }

    private ArmorUtil() {
    }

    public record WornGasMask(ItemStack carrier, ItemStack maskStack, GasMask mask, boolean attachedMod) {
        private static final WornGasMask EMPTY = new WornGasMask(ItemStack.EMPTY, ItemStack.EMPTY, null, false);

        public boolean isPresent() {
            return mask != null && !maskStack.isEmpty();
        }

        public void persist() {
            if (attachedMod && !carrier.isEmpty() && !maskStack.isEmpty()) {
                ArmorModHandler.applyMod(carrier, maskStack);
            }
        }
    }

    public record GasMaskFilterInstallResult(boolean installed, ItemStack replacement) {
        private static GasMaskFilterInstallResult pass(ItemStack replacement) {
            return new GasMaskFilterInstallResult(false, replacement == null ? ItemStack.EMPTY : replacement);
        }

        private static GasMaskFilterInstallResult installed(ItemStack replacement) {
            return new GasMaskFilterInstallResult(true, replacement == null ? ItemStack.EMPTY : replacement);
        }
    }
}
