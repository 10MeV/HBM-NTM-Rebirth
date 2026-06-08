package com.hbm.ntm.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.ntm.HbmNtm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArmorModEvents {
    private static final List<Attribute> MANAGED_ATTRIBUTES = List.of(
            Attributes.MAX_HEALTH,
            Attributes.MOVEMENT_SPEED,
            Attributes.ATTACK_DAMAGE,
            Attributes.KNOCKBACK_RESISTANCE);

    private static final List<UUID> MANAGED_UUIDS = List.of(
            ArmorModHandler.UUIDs[0],
            ArmorModHandler.UUIDs[1],
            ArmorModHandler.UUIDs[2],
            ArmorModHandler.UUIDs[3],
            ArmorModItems.BottledCloud.SPEED_UUID);

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || entity.isDeadOrDying()) {
            return;
        }
        tickArmorMods(entity);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!ArmorModHandler.hasMods(armor)) {
                continue;
            }
            for (ItemStack mod : ArmorModHandler.pryMods(armor)) {
                if (mod.getItem() instanceof ArmorModItem armorMod) {
                    armorMod.onArmorModHurt(event, armor, mod);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (!(stack.getItem() instanceof ArmorItem) || !ArmorModHandler.hasMods(stack)) {
            return;
        }
        ItemStack cladding = ArmorModHandler.pryMod(stack, ArmorModHandler.cladding);
        if (cladding.getItem() instanceof ArmorModItems.ObsidianCladding) {
            event.getEntity().setInvulnerable(true);
        }
    }

    private static void tickArmorMods(LivingEntity entity) {
        reconcileAttributeModifiers(entity);
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!ArmorModHandler.hasMods(armor)) {
                continue;
            }
            for (ItemStack mod : ArmorModHandler.pryMods(armor)) {
                if (mod.getItem() instanceof ArmorModItem armorMod) {
                    armorMod.onArmorModTick(entity, armor, mod);
                }
            }
        }
    }

    private static void reconcileAttributeModifiers(LivingEntity entity) {
        removeManagedModifiers(entity);
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!ArmorModHandler.hasMods(armor)) {
                continue;
            }
            for (ItemStack mod : ArmorModHandler.pryMods(armor)) {
                if (mod.getItem() instanceof ArmorModItem armorMod) {
                    armorMod.addArmorModAttributeModifiers(armor, mod, modifiers);
                }
            }
        }
        for (var entry : modifiers.entries()) {
            AttributeInstance instance = entity.getAttribute(entry.getKey());
            if (instance != null && instance.getModifier(entry.getValue().getId()) == null) {
                instance.addTransientModifier(entry.getValue());
            }
        }
    }

    private static void removeManagedModifiers(LivingEntity entity) {
        for (Attribute attribute : MANAGED_ATTRIBUTES) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            for (UUID uuid : MANAGED_UUIDS) {
                if (instance.getModifier(uuid) != null) {
                    instance.removeModifier(uuid);
                }
            }
        }
    }

    private ArmorModEvents() {
    }
}
