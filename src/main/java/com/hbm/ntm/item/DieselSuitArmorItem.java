package com.hbm.ntm.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hbm.items.armor.ArmorFSBFueled;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.particle.ParticleUtil;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DieselSuitArmorItem extends ArmorFSBFueled {
    public DieselSuitArmorItem(ArmorMaterial material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, int maxFuel, int fillRate, int consumption, int drain,
            FluidType... acceptedFuelTypes) {
        super(material, type, properties, fullSetEffects, maxFuel, fillRate, consumption, drain, acceptedFuelTypes);
    }

    public DieselSuitArmorItem(ArmorMaterial material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, int maxFuel, int fillRate, int consumption, int drain,
            FullSetTraits fullSetTraits, FluidType... acceptedFuelTypes) {
        super(material, type, properties, fullSetEffects, maxFuel, fillRate, consumption, drain,
                fullSetTraits, acceptedFuelTypes);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != slotForType(getType())) {
            return super.getDefaultAttributeModifiers(slot);
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(super.getDefaultAttributeModifiers(slot));
        int legacySlot = ArmorModHandler.legacyArmorTypeIndex(getType());
        builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(
                ArmorModHandler.fixedUUIDs[legacySlot], "Dieselsuit armor modifier", 0.25D,
                AttributeModifier.Operation.MULTIPLY_BASE));
        return builder.build();
    }

    @Override
    public void tickEquippedArmor(ItemStack stack, Level level, Player player) {
        super.tickEquippedArmor(stack, level, player);
        if (!level.isClientSide && getType() == Type.LEGGINGS && hasFullSet(player) && level.getGameTime() % 3L == 0L) {
            ParticleUtil.spawnBnuuy(level, player);
        }
    }

    private static EquipmentSlot slotForType(Type type) {
        return switch (type) {
            case HELMET -> EquipmentSlot.HEAD;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case LEGGINGS -> EquipmentSlot.LEGS;
            case BOOTS -> EquipmentSlot.FEET;
        };
    }
}
