package com.hbm.ntm.armor;

import com.google.common.collect.Multimap;
import com.hbm.ntm.api.fluid.IFillableItem;
import com.hbm.ntm.api.item.ArmorDashProvider;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFillableItemCapabilityProvider;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.satellite.SatelliteSavedData;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ArmorModItems {
    public static class Cladding extends ArmorModItem {
        private final double radiationResistance;

        public Cladding(Item.Properties properties, double radiationResistance) {
            super(properties, ArmorModHandler.ArmorModSlot.CLADDING, true, true, true, true);
            this.radiationResistance = radiationResistance;
        }

        public double radiationResistance() {
            return radiationResistance;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("+" + radiationResistance + " rad-resistance").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.YELLOW,
                    " (+" + radiationResistance + " radiation resistance)"));
        }
    }

    public static class IronCladding extends ArmorModItem {
        public IronCladding(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.CLADDING, true, true, true, true);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("+0.5 knockback resistance").withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.WHITE, " (+0.5 knockback resistence)"));
        }

        @Override
        public void addArmorModAttributeModifiers(ItemStack armor, ItemStack mod,
                                                  Multimap<Attribute, AttributeModifier> modifiers) {
            if (armor.getItem() instanceof ArmorItem armorItem) {
                modifiers.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(
                        ArmorModHandler.modifierUuidFor(armorItem.getType()),
                        "NTM Armor Mod Knockback",
                        0.5D,
                        AttributeModifier.Operation.ADDITION));
            }
        }
    }

    public static class ObsidianCladding extends ArmorModItem {
        public ObsidianCladding(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.CLADDING, true, true, true, true);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Makes dropped armor indestructible").withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.DARK_PURPLE, " (Item indestructible)"));
        }
    }

    public static class Health extends ArmorModItem {
        private final float health;
        private final boolean nostalgia;

        public Health(Item.Properties properties, float health, boolean nostalgia) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, false, true, false, false);
            this.health = health;
            this.nostalgia = nostalgia;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("+" + Math.round(health * 10.0F) * 0.1F + " health")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.empty());
            if (nostalgia) {
                tooltip.add(Component.literal("Nostalgia").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.empty());
            }
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, blink(ChatFormatting.RED, ChatFormatting.LIGHT_PURPLE),
                    " (+" + Math.round(health * 10.0F) * 0.1F + " health)"));
        }

        @Override
        public void addArmorModAttributeModifiers(ItemStack armor, ItemStack mod,
                                                  Multimap<Attribute, AttributeModifier> modifiers) {
            if (armor.getItem() instanceof ArmorItem armorItem) {
                modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                        ArmorModHandler.modifierUuidFor(armorItem.getType()),
                        "NTM Armor Mod Health",
                        health,
                        AttributeModifier.Operation.ADDITION));
            }
        }
    }

    public static class Servos extends ArmorModItem {
        private final boolean desh;

        public Servos(Item.Properties properties, boolean desh) {
            super(properties, ArmorModHandler.ArmorModSlot.SERVOS, false, true, true, false);
            this.desh = desh;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            if (desh) {
                tooltip.add(Component.literal("Chestplate: Haste III / Damage +150%")
                        .withStyle(ChatFormatting.DARK_PURPLE));
                tooltip.add(Component.literal("Leggings: Speed +50% / Jump III")
                        .withStyle(ChatFormatting.DARK_PURPLE));
            } else {
                tooltip.add(Component.literal("Chestplate: Haste I / Damage +50%")
                        .withStyle(ChatFormatting.DARK_PURPLE));
                tooltip.add(Component.literal("Leggings: Speed +25% / Jump II")
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            if (!(armor.getItem() instanceof ArmorItem armorItem)) {
                return;
            }
            if (armorItem.getType() == ArmorItem.Type.CHESTPLATE) {
                tooltip.add(installedLine(mod, ChatFormatting.DARK_PURPLE,
                        desh ? " (Haste III / Damage +150%)" : " (Haste I / Damage +50%)"));
            } else if (armorItem.getType() == ArmorItem.Type.LEGGINGS) {
                tooltip.add(installedLine(mod, ChatFormatting.DARK_PURPLE,
                        desh ? " (Speed +50% / Jump III)" : " (Speed +25% / Jump II)"));
            }
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (!(armor.getItem() instanceof ArmorItem armorItem)) {
                return;
            }
            if (armorItem.getType() == ArmorItem.Type.CHESTPLATE) {
                entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, desh ? 2 : 0));
            } else if (armorItem.getType() == ArmorItem.Type.LEGGINGS) {
                entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, desh ? 2 : 1));
            }
        }

        @Override
        public void addArmorModAttributeModifiers(ItemStack armor, ItemStack mod,
                                                  Multimap<Attribute, AttributeModifier> modifiers) {
            if (!(armor.getItem() instanceof ArmorItem armorItem)) {
                return;
            }
            UUID id = ArmorModHandler.modifierUuidFor(armorItem.getType());
            if (armorItem.getType() == ArmorItem.Type.CHESTPLATE) {
                modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                        id,
                        "NTM Armor Mod Servos",
                        desh ? 1.5D : 0.5D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            } else if (armorItem.getType() == ArmorItem.Type.LEGGINGS) {
                modifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                        id,
                        "NTM Armor Mod Servos",
                        desh ? 0.5D : 0.25D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }

    public static class Wd40 extends ArmorModItem {
        public Wd40(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, true, true, true);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Highly reduces damage taken by armor, +2 HP")
                    .withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, blink(ChatFormatting.BLUE, ChatFormatting.YELLOW),
                    " (-80% armor wear / +2 HP)"));
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            if (armor.getDamageValue() > 0 && event.getEntity().getRandom().nextInt(5) != 0) {
                armor.setDamageValue(armor.getDamageValue() - 1);
            }
        }

        @Override
        public void onClientArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (entity.hurtTime <= 0) {
                return;
            }
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth() * 2.0D;
            double y = entity.getY() + entity.getRandom().nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth() * 2.0D;
            ParticleUtil.spawnVanillaExtRedDust(entity.level(), x, y, z, 0.01D, 0.5D, 0.8D);
        }

        @Override
        public void addArmorModAttributeModifiers(ItemStack armor, ItemStack mod,
                                                  Multimap<Attribute, AttributeModifier> modifiers) {
            if (armor.getItem() instanceof ArmorItem armorItem) {
                modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                        ArmorModHandler.modifierUuidFor(armorItem.getType()),
                        "NTM Armor Mod Health",
                        4.0D,
                        AttributeModifier.Operation.ADDITION));
            }
        }
    }

    public static class BottledCloud extends ArmorModItem implements ArmorDashProvider {
        public static final UUID SPEED_UUID = UUID.fromString("1d11e63e-28c4-4e14-b09f-fe0bd1be708f");
        public static final int DASHES = 3;

        public BottledCloud(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.PLATE_ONLY, false, true, false, false);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Grants horizontal dashes").withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.RED, " (Dashes)"));
        }

        @Override
        public void addArmorModAttributeModifiers(ItemStack armor, ItemStack mod,
                                                  Multimap<Attribute, AttributeModifier> modifiers) {
            modifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                    SPEED_UUID,
                    "CLOUD SPEED",
                    0.125D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        public int getDashes() {
            return DASHES;
        }
    }

    public static class Jetpack extends ArmorModItem implements IFillableItem {
        private static final String FUEL_TAG = "fuel";

        private final Type type;
        private final FluidType fuelType;
        private final int maxFuel;

        public Jetpack(Item.Properties properties, Type type, FluidType fuelType, int maxFuel) {
            super(properties, ArmorModHandler.ArmorModSlot.PLATE_ONLY, false, true, false, false);
            this.type = type;
            this.fuelType = fuelType;
            this.maxFuel = maxFuel;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            for (Component description : type.descriptions()) {
                tooltip.add(description.copy().withStyle(ChatFormatting.GOLD));
            }
            tooltip.add(fuelType.getDisplayName().copy()
                    .append(": " + getFuel(stack) + "mB / " + maxFuel + "mB")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.literal("Can be worn on its own!").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.RED,
                    " (" + fuelType.getDisplayName().getString() + ": " + getFuel(mod) + "mB / " + maxFuel + "mB)"));
        }

        @Override
        public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
            return new HbmFillableItemCapabilityProvider(stack, this, maxFuel);
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (!(entity instanceof Player player)) {
                return;
            }

            if (tickJetpack(player, mod)) {
                ArmorModHandler.applyMod(armor, mod);
            }
        }

        public boolean tickJetpack(Player player, ItemStack stack) {
            if (getFuel(stack) <= 0) {
                return false;
            }
            boolean changed = switch (type) {
                case REGULAR -> regular(player, stack);
                case HOVER -> hover(player, stack);
                case VECTORED -> vector(player, stack, 2.0D, 0.4D, 0.1D, 0.1D, 3, 1.5F);
                case BOOST -> vector(player, stack, 5.0D, 0.6D, 0.1D, 0.25D, 1, 1.0F);
            };
            return changed && !stack.isEmpty();
        }

        @Override
        public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
            return armorType == EquipmentSlot.CHEST;
        }

        @Override
        public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
            return EquipmentSlot.CHEST;
        }

        public static int getFuel(ItemStack stack) {
            if (stack.isEmpty() || stack.getTag() == null) {
                return 0;
            }
            return stack.getTag().getInt(FUEL_TAG);
        }

        public int getMaxFuel() {
            return maxFuel;
        }

        public int getFuelColor() {
            return fuelType.getColor();
        }

        public float getFuelFraction(ItemStack stack) {
            if (maxFuel <= 0) {
                return 0.0F;
            }
            return Mth.clamp((float) getFuel(stack) / (float) maxFuel, 0.0F, 1.0F);
        }

        public void setFuel(ItemStack stack, int fuel) {
            stack.getOrCreateTag().putInt(FUEL_TAG, Math.max(0, Math.min(maxFuel, fuel)));
        }

        @Override
        public boolean acceptsFluid(FluidType type, ItemStack stack) {
            return type == fuelType && getFuel(stack) < maxFuel;
        }

        @Override
        public int tryFill(FluidType type, int amount, ItemStack stack) {
            if (amount <= 0 || !acceptsFluid(type, stack)) {
                return Math.max(0, amount);
            }
            int moved = Math.min(amount, maxFuel - getFuel(stack));
            setFuel(stack, getFuel(stack) + moved);
            return amount - moved;
        }

        @Override
        public boolean providesFluid(FluidType type, ItemStack stack) {
            return false;
        }

        @Override
        public int tryEmpty(FluidType type, int amount, ItemStack stack) {
            return 0;
        }

        @Override
        public FluidType getFirstFluidType(ItemStack stack) {
            return HbmFluids.NONE;
        }

        @Override
        public int getFill(ItemStack stack) {
            return getFuel(stack);
        }

        private boolean regular(Player player, ItemStack stack) {
            if (!HbmPlayerProperties.isJetpackActive(player)) {
                return false;
            }
            Vec3 movement = player.getDeltaMovement();
            if (movement.y < 0.4D) {
                player.setDeltaMovement(movement.x, movement.y + 0.1D, movement.z);
                player.hasImpulse = true;
            }
            player.fallDistance = 0.0F;
            play(player, 1.5F);
            ParticleUtil.spawnJetpack(player.level(), player, 0);
            useUpFuel(player, stack, 5);
            return true;
        }

        private boolean hover(Player player, ItemStack stack) {
            boolean active = HbmPlayerProperties.isJetpackActive(player);
            boolean triesToHover = player.isShiftKeyDown() && active;
            boolean shouldHover = triesToHover || !player.isShiftKeyDown();
            if (active && !triesToHover) {
                Vec3 movement = player.getDeltaMovement();
                if (movement.y < 0.4D) {
                    player.setDeltaMovement(movement.x, movement.y + 0.1D, movement.z);
                    player.hasImpulse = true;
                }
                player.fallDistance = 0.0F;
                play(player, 1.5F);
                ParticleUtil.spawnJetpack(player.level(), player, 0);
                useUpFuel(player, stack, 5);
                return true;
            }
            if (shouldHover && !player.onGround() && HbmPlayerProperties.isBackpackEnabled(player)) {
                Vec3 movement = player.getDeltaMovement();
                double y = movement.y;
                if (y < -1.0D) {
                    y += 0.2D;
                } else if (y < -0.1D) {
                    y += 0.1D;
                } else if (y < 0.0D) {
                    y = 0.0D;
                }
                player.setDeltaMovement(movement.x * 1.025D, y, movement.z * 1.025D);
                player.hasImpulse = true;
                player.fallDistance = 0.0F;
                play(player, 1.5F);
                ParticleUtil.spawnJetpack(player.level(), player, 0);
                useUpFuel(player, stack, 10);
                return true;
            }
            return false;
        }

        private boolean vector(Player player, ItemStack stack, double speedLimit, double verticalLimit,
                               double verticalThrust, double lookThrust, int fuelRate, float pitch) {
            if (!HbmPlayerProperties.isJetpackActive(player)) {
                return false;
            }
            Vec3 movement = player.getDeltaMovement();
            double y = movement.y;
            if (y < verticalLimit) {
                y += verticalThrust;
            }
            Vec3 look = player.getLookAngle();
            double x = movement.x;
            double z = movement.z;
            if (new Vec3(movement.x, y, movement.z).length() < speedLimit) {
                x += look.x * lookThrust;
                y += look.y * lookThrust;
                z += look.z * lookThrust;
                if (look.y > 0.0D) {
                    player.fallDistance = 0.0F;
                }
            }
            player.setDeltaMovement(x, y, z);
            player.hasImpulse = true;
            play(player, pitch);
            ParticleUtil.spawnJetpack(player.level(), player, 1);
            useUpFuel(player, stack, fuelRate);
            return true;
        }

        private void play(Player player, float pitch) {
            LegacySoundPlayer.playLegacyFlamethrowerShoot(player, 0.25F, pitch);
        }

        private void useUpFuel(Player player, ItemStack stack, int rate) {
            if (player.tickCount % rate == 0) {
                setFuel(stack, getFuel(stack) - 1);
            }
        }

        public enum Type {
            REGULAR(Component.literal("Regular jetpack for simple upwards momentum.")),
            HOVER(Component.literal("Regular jetpack that will automatically hover mid-air."),
                    Component.literal("Sneaking will stop hover mode."),
                    Component.literal("Hover mode will consume less fuel and increase air-mobility.")),
            VECTORED(Component.literal("High-mobility jetpack."),
                    Component.literal("Higher fuel consumption.")),
            BOOST(Component.literal("High-powered vectorized jetpack."),
                    Component.literal("Highly increased fuel consumption."));

            private final List<Component> descriptions;

            Type(Component... descriptions) {
                this.descriptions = List.of(descriptions);
            }

            public List<Component> descriptions() {
                return descriptions;
            }
        }
    }

    public static class Wings extends ArmorModItem {
        private final boolean murky;

        public Wings(Item.Properties properties, boolean murky) {
            super(properties, ArmorModHandler.ArmorModSlot.PLATE_ONLY, false, true, false, false);
            this.murky = murky;
        }

        public boolean isMurky() {
            return murky;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Can be worn on its own!").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (entity instanceof Player player) {
                tickWings(player);
            }
        }

        @Override
        public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
            return armorType == EquipmentSlot.CHEST;
        }

        @Override
        public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
            return EquipmentSlot.CHEST;
        }

        public boolean tickWings(Player player) {
            if (player.onGround()) {
                return false;
            }

            com.hbm.ntm.util.ArmorUtil.resetFlightTime(player);
            if (player.fallDistance > 0.0F) {
                player.fallDistance = 0.0F;
            }

            if (murky) {
                tickMurkyWings(player);
            } else {
                tickLimpWings(player);
            }
            return true;
        }

        private static void tickLimpWings(Player player) {
            Vec3 movement = player.getDeltaMovement();
            double x = movement.x;
            double y = movement.y;
            double z = movement.z;

            if (y < -0.4D) {
                y = -0.4D;
            }
            if (player.isShiftKeyDown() && y < -0.08D) {
                double lift = y * -0.2D;
                Vec3 look = player.getLookAngle().scale(lift);
                x += look.x;
                y += lift + look.y;
                z += look.z;
            }
            player.setDeltaMovement(x, y, z);
            player.hasImpulse = true;
        }

        private static void tickMurkyWings(Player player) {
            Vec3 movement = player.getDeltaMovement();
            double x = movement.x;
            double y = movement.y;
            double z = movement.z;

            if (HbmPlayerProperties.isJetpackActive(player)) {
                if (player.isShiftKeyDown()) {
                    if (y < -1.0D) {
                        y += 0.4D;
                    } else if (y < -0.1D) {
                        y += 0.2D;
                    } else if (y < 0.0D) {
                        y = 0.0D;
                    } else if (y > 1.0D) {
                        y -= 0.4D;
                    } else if (y > 0.1D) {
                        y -= 0.2D;
                    } else if (y > 0.0D) {
                        y = 0.0D;
                    }
                } else if (y < 0.6D) {
                    y += 0.2D;
                } else {
                    y = 0.8D;
                }
            } else if (HbmPlayerProperties.isBackpackEnabled(player) && !player.isShiftKeyDown()) {
                if (y < -1.0D) {
                    y += 0.4D;
                } else if (y < -0.1D) {
                    y += 0.2D;
                } else if (y < 0.0D) {
                    y = 0.0D;
                }
            }

            if (HbmPlayerProperties.isBackpackEnabled(player)) {
                Vec3 look = new Vec3(player.getLookAngle().x, 0.0D, player.getLookAngle().z);
                if (look.lengthSqr() > 1.0E-7D) {
                    look = look.normalize();
                    double modifier = player.isSprinting() ? 1.0D : 0.25D;
                    if (player.zza != 0.0F) {
                        x += look.x * 0.35D * player.zza * modifier;
                        z += look.z * 0.35D * player.zza * modifier;
                    }
                    if (player.xxa != 0.0F) {
                        Vec3 strafe = look.yRot((float) Math.PI * 0.5F);
                        x += strafe.x * 0.15D * player.xxa * modifier;
                        z += strafe.z * 0.15D * player.xxa * modifier;
                    }
                }
            }

            player.setDeltaMovement(x, y, z);
            player.hasImpulse = true;
        }
    }

    public static class Shield extends ArmorModItem {
        private final float shield;

        public Shield(Item.Properties properties, float shield) {
            super(properties, ArmorModHandler.ArmorModSlot.KEVLAR, false, true, false, false);
            this.shield = shield;
        }

        public float shield() {
            return shield;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("+" + Math.round(shield * 10.0F) * 0.1F + " shield")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, blink(ChatFormatting.YELLOW, ChatFormatting.GOLD),
                    " (+" + Math.round(shield * 10.0F) * 0.1F + " health)"));
        }
    }

    public static class Pads extends ArmorModItem {
        private final float damageMod;
        private final boolean staticCharge;

        public Pads(Item.Properties properties, float damageMod, boolean staticCharge) {
            super(properties, ArmorModHandler.ArmorModSlot.BOOTS_ONLY, false, false, false, true);
            this.damageMod = damageMod;
            this.staticCharge = staticCharge;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("-" + Math.round((1.0F - damageMod) * 100.0F) + "% fall damage")
                    .withStyle(ChatFormatting.RED));
            if (staticCharge) {
                tooltip.add(Component.literal("Passively charges electric armor when walking")
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.DARK_PURPLE,
                    " (-" + Math.round((1.0F - damageMod) * 100.0F)
                            + (staticCharge ? "% fall dmg / passive charge)" : "% fall dmg)")));
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            if (event.getSource().is(DamageTypes.FALL)) {
                event.setAmount(event.getAmount() * damageMod);
            }
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (!staticCharge || !(entity instanceof Player player) || !FsbPoweredArmor.hasFullPoweredSetIgnoreCharge(player)) {
                return;
            }
            double dx = player.getX() - player.xo;
            double dz = player.getZ() - player.zo;
            if (!player.onGround() || dx * dx + dz * dz <= 1.0E-6D) {
                return;
            }
            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                ItemStack equipped = player.getItemBySlot(slot);
                if (equipped.getItem() instanceof FsbPoweredArmor powered) {
                    long charge = powered.getDrain(equipped) / 2L;
                    if (charge == 0L) {
                        charge = powered.getConsumption(equipped) / 40L;
                    }
                    if (charge > 0L) {
                        powered.setCharge(equipped, Math.min(powered.getMaxCharge(equipped), powered.getCharge(equipped) + charge));
                    }
                }
            }
        }
    }

    public static class Polish extends ArmorModItem {
        public Polish(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, true, true, true);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("5% chance to nullify damage").withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.BLUE, " (5% chance to nullify damage)"));
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            if (event.getEntity().getRandom().nextInt(20) == 0) {
                event.setAmount(0.0F);
            }
        }
    }

    public static class Bandaid extends ArmorModItem {
        public Bandaid(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, true, true, true);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("3% chance for full heal when damaged").withStyle(ChatFormatting.RED));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.RED, " (3% chance for full heal)"));
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            LivingEntity entity = event.getEntity();
            if (entity.getRandom().nextInt(100) < 3) {
                event.setAmount(0.0F);
                entity.heal(entity.getMaxHealth());
            }
        }
    }

    public static class Serum extends ArmorModItem {
        public Serum(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, true, true, true);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Cures poison and gives strength").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.BLUE, " (replaces poison with strength)"));
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (entity.hasEffect(MobEffects.POISON)) {
                entity.removeEffect(MobEffects.POISON);
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 4));
            }
        }
    }

    public static class Quartz extends ArmorModItem {
        public Quartz(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, true, true, true);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Taking damage removes 10 RAD").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.DARK_GRAY, " (-10 RAD when hit)"));
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            HbmLivingProperties.incrementRadiation(event.getEntity(), -10.0F);
        }
    }

    public static class MorningGlory extends ArmorModItem {
        public MorningGlory(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, true, true, true);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("5% chance to apply resistance when hit, wither immunity")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.LIGHT_PURPLE,
                    " (5% for resistance, wither immunity)"));
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            LivingEntity entity = event.getEntity();
            if (entity.getRandom().nextInt(20) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4));
            }
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (entity.hasEffect(MobEffects.WITHER)) {
                entity.removeEffect(MobEffects.WITHER);
            }
        }
    }

    public static class Lodestone extends ArmorModItem {
        private final int range;

        public Lodestone(Item.Properties properties, int range) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, true, true, true);
            this.range = range;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Attracts nearby items").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal("Item attraction range: " + range).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.DARK_GRAY, " (Magnetic range: " + range + ")"));
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (entity instanceof Player player && !HbmPlayerProperties.isMagnetActive(player)) {
                return;
            }
            List<ItemEntity> items = entity.level().getEntitiesOfClass(ItemEntity.class,
                    entity.getBoundingBox().inflate(range, range, range));
            for (ItemEntity item : items) {
                Vec3 pull = entity.position().subtract(item.position()).normalize();
                Vec3 motion = item.getDeltaMovement().add(pull.scale(0.05D));
                if (pull.y > 0.0D && motion.y < 0.04D) {
                    motion = motion.add(0.0D, 0.2D, 0.0D);
                }
                item.setDeltaMovement(motion);
                item.hasImpulse = true;
            }
        }
    }

    public static class Bathwater extends ArmorModItem {
        private final boolean wither;

        public Bathwater(Item.Properties properties, boolean wither) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, true, true, true);
            this.wither = wither;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal(wither ? "Inflicts wither on the attacker" : "Inflicts poison on the attacker")
                    .withStyle(wither ? ChatFormatting.GREEN : ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod,
                    wither ? blink(ChatFormatting.GREEN, ChatFormatting.YELLOW)
                            : blink(ChatFormatting.BLUE, ChatFormatting.LIGHT_PURPLE),
                    " (Poisons attackers)"));
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            Entity attacker = event.getSource().getEntity();
            if (attacker instanceof LivingEntity livingAttacker) {
                livingAttacker.addEffect(new MobEffectInstance(wither ? MobEffects.WITHER : MobEffects.POISON,
                        200, wither ? 4 : 2));
            }
        }
    }

    public static class Milk extends ArmorModItem {
        public Milk(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, true, true, true);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Removes bad potion effects").withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.WHITE, " (Removes bad potion effects)"));
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            List<MobEffect> harmfulEffects = new ArrayList<>();
            for (MobEffectInstance effect : entity.getActiveEffects()) {
                if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                    harmfulEffects.add(effect.getEffect());
                }
            }
            harmfulEffects.forEach(entity::removeEffect);
        }
    }

    public static class Ink extends ArmorModItem {
        private static final Item[] RED_FLOWERS = new Item[] {
                Items.POPPY,
                Items.BLUE_ORCHID,
                Items.ALLIUM,
                Items.AZURE_BLUET,
                Items.RED_TULIP,
                Items.ORANGE_TULIP,
                Items.WHITE_TULIP,
                Items.PINK_TULIP,
                Items.OXEYE_DAISY
        };

        public Ink(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, true, true, true);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("10% chance to nullify damage").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.literal("Flowers!").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.LIGHT_PURPLE, " (10% chance to nullify damage)"));
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            LivingEntity entity = event.getEntity();
            if (entity.getRandom().nextInt(10) != 0) {
                return;
            }
            event.setAmount(0.0F);
            if (entity.getRandom().nextInt(10) == 0) {
                entity.spawnAtLocation(new ItemStack(Items.DANDELION), 1.0F);
            }
            entity.spawnAtLocation(new ItemStack(RED_FLOWERS[entity.getRandom().nextInt(RED_FLOWERS.length)]), 1.0F);
        }
    }

    public static class AutoInjector extends ArmorModItem {
        public AutoInjector(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, false, true, false, false);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Imported from Japsterdam.").withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.BLUE, ""));
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (HbmLivingProperties.getDigamma(entity) < 5.0F) {
                return;
            }
            ArmorModHandler.removeMod(armor, ArmorModHandler.extra);
            LegacySoundPlayer.playLegacySyringe(entity);
            HbmLivingProperties.incrementDigamma(entity, -5.0F);
            entity.addEffect(new MobEffectInstance(ModEffects.STABILITY.get(), 60 * 20, 0));
            entity.heal(20.0F);
        }
    }

    public static class InjectorKnife extends ArmorModItem {
        public static final UUID TRIGAMMA_UUID = UUID.fromString("86d44ca9-44f1-4ca6-bdbb-d9d33bead251");

        public InjectorKnife(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, false, true, false, false);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Pain.").withStyle(ChatFormatting.RED));
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Hurts, doesn't it?").withStyle(ChatFormatting.RED));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.RED, ""));
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (entity.level().isClientSide || entity.tickCount % 50 != 0 || entity.getMaxHealth() <= 2.0F) {
                return;
            }

            AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth == null) {
                return;
            }

            LegacySoundPlayer.playLegacySlicer(entity);
            ParticleUtil.spawnVomit(entity, ParticleUtil.VOMIT_BLOOD, 25);

            double currentMax = entity.getMaxHealth();
            maxHealth.removeModifier(TRIGAMMA_UUID);
            double restoredMax = entity.getMaxHealth();
            double modifier = -(restoredMax - currentMax + 2.0D);
            maxHealth.addPermanentModifier(new AttributeModifier(TRIGAMMA_UUID, "digamma",
                    modifier, AttributeModifier.Operation.ADDITION));
            if (entity.getHealth() > entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }

            if (entity instanceof ServerPlayer player) {
                CompoundTag data = new CompoundTag();
                data.putString("type", ParticleUtil.TYPE_PROPER_JOLT);
                if (entity.getMaxHealth() > 2.0F) {
                    data.putInt("time", 10_000 + entity.getRandom().nextInt(10_000));
                    data.putInt("maxTime", 10_000);
                } else {
                    data.putInt("time", 0);
                    data.putInt("maxTime", 0);
                }
                ModMessages.sendAuxParticle(player, 0.0D, 0.0D, 0.0D, data);
            }
        }
    }

    public static class Defuser extends ArmorModItem {
        private static final String DEFUSED_TAG = "hfr_defused";
        @Nullable
        private static final Field GOAL_SELECTOR_FIELD = findGoalSelectorField();

        public Defuser(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, true, true, true);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Defuses nearby creepers").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.YELLOW, " (Defuses creepers)"));
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            Level level = entity.level();
            if (level.isClientSide || level.getGameTime() % 20 != 0) {
                return;
            }
            for (Creeper creeper : level.getEntitiesOfClass(Creeper.class, entity.getBoundingBox().inflate(5.0D))) {
                defuse(creeper, entity, true);
            }
        }

        public static boolean defuse(Creeper creeper, LivingEntity entity, boolean dropItem) {
            creeper.setSwellDir(-1);
            if (creeper.level().isClientSide) {
                return false;
            }

            GoalSelector selector = goalSelector(creeper);
            if (selector == null) {
                return false;
            }

            List<Goal> swellGoals = selector.getAvailableGoals().stream()
                    .map(WrappedGoal::getGoal)
                    .filter(SwellGoal.class::isInstance)
                    .toList();
            if (swellGoals.isEmpty()) {
                return false;
            }

            for (Goal goal : swellGoals) {
                selector.removeGoal(goal);
            }

            if (dropItem) {
                LegacySoundPlayer.playLegacyPinBreak(creeper);
                RegistryObject<Item> fuse = ModItems.legacyItem("safety_fuse");
                if (fuse != null) {
                    creeper.spawnAtLocation(new ItemStack(fuse.get()), 0.0F);
                }
                creeper.hurt(creeper.damageSources().mobAttack(entity), 1.0F);
                creeper.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 0, 200));
            }
            creeper.getPersistentData().putBoolean(DEFUSED_TAG, true);
            return true;
        }

        @Nullable
        private static GoalSelector goalSelector(Mob mob) {
            if (GOAL_SELECTOR_FIELD == null) {
                return null;
            }
            try {
                Object value = GOAL_SELECTOR_FIELD.get(mob);
                return value instanceof GoalSelector selector ? selector : null;
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }

        @Nullable
        private static Field findGoalSelectorField() {
            for (String name : List.of("goalSelector", "f_21345_")) {
                try {
                    Field field = Mob.class.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignored) {
                }
            }
            return null;
        }
    }

    public static class NeutrinoLens extends ArmorModItem implements ISatelliteChip {
        private static final int SCANNER_RANGE_CHUNKS = 3;
        private static final int SCANNER_MAX_HITS = 100;
        private static final List<ScannerTarget> SCANNER_TARGETS = List.of(
                ScannerTarget.legacy("ore_alexandrite", 1, "Alexandrite", 0x00ffff),
                ScannerTarget.legacy("ore_oil", 300, "Oil", 0xa0a0a0),
                ScannerTarget.legacy("ore_bedrock_oil", 300, "Bedrock Oil", 0xa0a0a0),
                ScannerTarget.legacy("ore_coltan", 5, "Coltan", 0xa0a000),
                ScannerTarget.legacy("deepslate_ore_coltan", 5, "Coltan", 0xa0a000),
                ScannerTarget.legacy("stone_gneiss", 5000, "Schist", 0x8080ff),
                ScannerTarget.legacy("ore_australium", 1000, "Australium", 0xffff00),
                ScannerTarget.vanilla(Blocks.END_PORTAL_FRAME, 1, "End Portal", 0x40b080),
                ScannerTarget.legacy("bobblehead", 1, "A Treasure!", 0xff0000),
                ScannerTarget.legacy("ore_bedrock", 1, "Bedrock Ore", 0xff0000),
                ScannerTarget.legacy("ore_bedrock_coltan", 1, "Bedrock Coltan", 0xa0a000)
        );

        public NeutrinoLens(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, false, false, false);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("satchip.frequency")
                    .append(": " + getFrequency(stack))
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.AQUA, " (Freq: " + getFrequency(mod) + ")"));
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (!(entity instanceof ServerPlayer player) || !(entity.level() instanceof ServerLevel level)) {
                return;
            }

            Satellite satellite = SatelliteSavedData.get(level).getSatellite(getFrequency(mod));
            if (satellite == null || satellite.type() != LegacySatelliteType.SCANNER) {
                return;
            }

            scanAndMark(level, player);
        }

        private void scanAndMark(ServerLevel level, ServerPlayer player) {
            BlockPos playerPos = player.blockPosition();
            int minY = level.getMinBuildHeight();
            int maxSpan = Math.max(1, level.getMaxBuildHeight() - minY);
            int heightSpan = Mth.clamp(playerPos.getY() - minY + 10, 64, maxSpan);
            int segY = minY + (int) (level.getGameTime() % heightSpan);
            int centerChunkX = playerPos.getX() >> 4;
            int centerChunkZ = playerPos.getZ() >> 4;
            int hits = 0;
            BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();

            for (int chunkX = centerChunkX - SCANNER_RANGE_CHUNKS; chunkX <= centerChunkX + SCANNER_RANGE_CHUNKS; chunkX++) {
                for (int chunkZ = centerChunkZ - SCANNER_RANGE_CHUNKS; chunkZ <= centerChunkZ + SCANNER_RANGE_CHUNKS; chunkZ++) {
                    LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                    for (int ix = 0; ix < 16; ix++) {
                        for (int iz = 0; iz < 16; iz++) {
                            scanPos.set((chunkX << 4) + ix, segY, (chunkZ << 4) + iz);
                            Block block = chunk.getBlockState(scanPos).getBlock();
                            for (ScannerTarget target : SCANNER_TARGETS) {
                                if (target.tryMark(block, scanPos, player)) {
                                    hits++;
                                    if (hits > SCANNER_MAX_HITS) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private record ScannerTarget(@Nullable String legacyName, @Nullable Block vanillaBlock, int chance,
                                     @Nullable String label, int color) {
            private static ScannerTarget legacy(String legacyName, int chance, @Nullable String label, int color) {
                return new ScannerTarget(legacyName, null, chance, label, color);
            }

            private static ScannerTarget vanilla(Block block, int chance, @Nullable String label, int color) {
                return new ScannerTarget(null, block, chance, label, color);
            }

            private boolean tryMark(Block block, BlockPos pos, ServerPlayer player) {
                Block target = targetBlock();
                if (target != block || player.getRandom().nextInt(chance) != 0) {
                    return false;
                }
                CompoundTag data = new CompoundTag();
                data.putString("type", ParticleUtil.TYPE_MARKER);
                data.putInt("color", color);
                data.putInt("expires", 15_000);
                data.putDouble("dist", 300.0D);
                if (label != null) {
                    data.putString("label", label);
                }
                ModMessages.sendAuxParticle(player, pos.getX(), pos.getY(), pos.getZ(), data);
                return true;
            }

            @Nullable
            private Block targetBlock() {
                if (vanillaBlock != null) {
                    return vanillaBlock;
                }
                RegistryObject<? extends Block> object = ModBlocks.legacyBlock(legacyName);
                return object == null ? null : object.get();
            }
        }
    }

    public static class NightVision extends ArmorModItem {
        private static final String NIGHT_VISION_ACTIVE_NBT_KEY = "ITEM_MOD_NV_ACTIVE";

        public NightVision(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.HELMET_ONLY, true, false, false, false);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Grants you night vision (requires full electric set)")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.YELLOW, " (Grants night vision)"));
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (!(entity instanceof Player player) || !(armor.getItem() instanceof FsbPoweredArmor powered)
                    || !FsbPoweredArmor.hasFullPoweredSet(player)) {
                return;
            }

            CompoundTag tag = armor.getOrCreateTag();
            if (HbmPlayerProperties.isHudEnabled(player)) {
                entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 15 * 20, 0));
                tag.putBoolean(NIGHT_VISION_ACTIVE_NBT_KEY, true);
                if (entity.getRandom().nextInt(200) == 0) {
                    powered.applyLegacyDamage(armor, 1);
                }
            } else if (tag.contains(NIGHT_VISION_ACTIVE_NBT_KEY)) {
                entity.removeEffect(MobEffects.NIGHT_VISION);
                tag.remove(NIGHT_VISION_ACTIVE_NBT_KEY);
            }
        }
    }

    public static class BackTesla extends ArmorModItem {
        @Nullable
        private static final EntityDataAccessor<Boolean> CREEPER_POWERED = findCreeperPoweredAccessor();

        public BackTesla(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.PLATE_ONLY, false, true, false, false);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Zaps nearby entities (requires full electric set)")
                    .withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(noIndentInstalledLine(mod, ChatFormatting.YELLOW, " (zaps nearby entities)"));
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (!(entity instanceof Player player) || !(entity.level() instanceof ServerLevel level)
                    || !(armor.getItem() instanceof FsbPoweredArmor powered)
                    || !FsbPoweredArmor.hasFullPoweredSet(player)) {
                return;
            }

            if (zap(level, entity, 5.0D) && entity.getRandom().nextInt(5) == 0) {
                powered.applyLegacyDamage(armor, 1);
            }
        }

        private boolean zap(ServerLevel level, LivingEntity source, double radius) {
            Vec3 origin = new Vec3(source.getX(), source.getY() + 1.25D, source.getZ());
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(origin, origin).inflate(radius), LivingEntity::isAlive);
            boolean zapped = false;
            for (LivingEntity target : nearby) {
                if (target == source || target instanceof Ocelot) {
                    continue;
                }
                Vec3 targetPoint = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
                if (origin.distanceToSqr(targetPoint) > radius * radius || isObstructed(level, origin, targetPoint, target)) {
                    continue;
                }

                if (target instanceof Creeper creeper && chargeCreeper(creeper)) {
                    zapped = true;
                    continue;
                }

                if (!(target instanceof Player player && ArmorUtil.checkForFaraday(player))) {
                    float damage = Mth.clamp(target.getMaxHealth() * 0.5F, 3.0F, 20.0F) / Math.max(1, nearby.size());
                    if (EntityDamageUtil.attackEntityFromNt(target, ModDamageSources.electric(level), damage)) {
                        LegacySoundPlayer.playLegacyTesla(target);
                    }
                }
                zapped = true;
            }
            return zapped;
        }

        private boolean isObstructed(ServerLevel level, Vec3 origin, Vec3 targetPoint, LivingEntity target) {
            BlockHitResult hit = level.clip(new ClipContext(origin, targetPoint,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target));
            return hit.getType() == HitResult.Type.BLOCK
                    && hit.getLocation().distanceToSqr(origin) + 0.25D < targetPoint.distanceToSqr(origin);
        }

        private static boolean chargeCreeper(Creeper creeper) {
            if (CREEPER_POWERED == null) {
                return false;
            }
            creeper.getEntityData().set(CREEPER_POWERED, true);
            return true;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        private static EntityDataAccessor<Boolean> findCreeperPoweredAccessor() {
            for (String name : List.of("DATA_IS_POWERED", "f_32274_")) {
                try {
                    Field field = Creeper.class.getDeclaredField(name);
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value instanceof EntityDataAccessor<?> accessor) {
                        return (EntityDataAccessor<Boolean>) accessor;
                    }
                } catch (IllegalAccessException | NoSuchFieldException ignored) {
                }
            }
            return null;
        }
    }

    public static class Medal extends ArmorModItem {
        public Medal(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.PLATE_ONLY, true, true, false, false);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("-10 RAD/s").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.GOLD, " (-10 RAD/s)"));
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            HbmLivingProperties.incrementRadiation(entity, -0.5F);
        }
    }

    public static class BallisticGauntlet extends ArmorModItem {
        public BallisticGauntlet(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.SERVOS, false, true, false, false);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("\"I've had worse\"").withStyle(ChatFormatting.ITALIC));
            tooltip.add(Component.literal("Punches fire 12 gauge shells").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.YELLOW, " (Shotgun punches)"));
        }
    }

    public static class Card extends ArmorModItem {
        private final boolean queenOfSpades;

        public Card(Item.Properties properties, boolean queenOfSpades) {
            super(properties, ArmorModHandler.ArmorModSlot.HELMET_ONLY, true, true, false, false);
            this.queenOfSpades = queenOfSpades;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            if (queenOfSpades) {
                tooltip.add(Component.literal("Power!").withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal("33% chance to tank damage with no cap").withStyle(ChatFormatting.RED));
            } else {
                tooltip.add(Component.literal("Top of the line!").withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal("Guns now have a 33% chance to not consume ammo.")
                        .withStyle(ChatFormatting.RED));
            }
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(noIndentInstalledLine(mod, ChatFormatting.RED, ""));
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            LivingEntity entity = event.getEntity();
            if (!queenOfSpades || !(entity instanceof Player player) || entity.getRandom().nextInt(3) != 0) {
                return;
            }
            HbmPlayerProperties.plink(player, SoundEvents.ITEM_BREAK, 0.5F,
                    1.0F + entity.getRandom().nextFloat() * 0.5F);
            event.setAmount(0.0F);
            event.setCanceled(true);
        }
    }

    public static class Charm extends ArmorModItem {

        public Charm(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.HELMET_ONLY, true, true, false, false);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("You feel blessed.").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal("Halves broadcaster damage").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.GOLD, ""));
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            if (!event.getSource().is(ModDamageSources.BROADCAST)) {
                return;
            }
            event.setAmount(event.getAmount() * 0.5F);
        }
    }

    public static class GasSensor extends ArmorModItem {
        private static final int RANGE = 3;

        public GasSensor(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, true, true, true, true);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Beeps near hazardous gasses").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal("Works in the inventory or when applied to armor").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.YELLOW, " (Detects gasses)"));
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
            if (entity instanceof LivingEntity living) {
                scanForGas(living);
            }
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            scanForGas(entity);
        }

        private void scanForGas(LivingEntity entity) {
            Level level = entity.level();
            if (level.isClientSide || level.getGameTime() % 20 != 0) {
                return;
            }

            BlockPos center = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
            for (int i = -RANGE; i <= RANGE; i++) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = -RANGE; k <= RANGE; k++) {
                        Block block = level.getBlockState(center.offset(i * 2, j * 2, k * 2)).getBlock();
                        if (isPoisonGas(block)) {
                            LegacySoundPlayer.playLegacyTechBoop(entity, 2.0F, 1.5F);
                            return;
                        }
                    }
                }
            }
        }

        private boolean isPoisonGas(Block block) {
            return block == ModBlocks.GAS_ASBESTOS.get()
                    || block == ModBlocks.GAS_COAL.get()
                    || block == ModBlocks.GAS_RADON.get()
                    || block == ModBlocks.GAS_MONOXIDE.get()
                    || block == ModBlocks.GAS_RADON_DENSE.get()
                    || block == ModBlocks.CHLORINE_GAS.get();
        }
    }

    public static class ArmorBattery extends ArmorModItem {
        private final double multiplier;

        public ArmorBattery(Item.Properties properties, double multiplier) {
            super(properties, ArmorModHandler.ArmorModSlot.BATTERY, true, true, true, true);
            this.multiplier = multiplier;
        }

        public double multiplier() {
            return multiplier;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("Power armor capacity x" + multiplier).withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }
    }

    public static class Revive extends ArmorModItem {
        public Revive(Item.Properties properties, int revives) {
            super(properties.durability(revives), ArmorModHandler.ArmorModSlot.EXTRA,
                    false, false, true, false, false);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal((stack.getMaxDamage() - stack.getDamageValue()) + " revives left")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.GOLD,
                    " (" + (mod.getMaxDamage() - mod.getDamageValue()) + " revives left)"));
        }

        public void handleDeath(LivingDeathEvent event, ItemStack armor, ItemStack mod) {
            damageInstalledRevive(armor, mod);
            revive(event.getEntity());
            event.setCanceled(true);
        }

        private void damageInstalledRevive(ItemStack armor, ItemStack mod) {
            mod.setDamageValue(mod.getDamageValue() + 1);
            if (mod.getDamageValue() >= mod.getMaxDamage()) {
                ArmorModHandler.removeMod(armor, ArmorModHandler.extra);
            } else {
                ArmorModHandler.applyMod(armor, mod);
            }
        }
    }

    public static class Shackles extends ArmorModItem {
        private static final String REVIVE_COUNT_TAG = "hfr_shackle_revives";

        public Shackles(Item.Properties properties) {
            super(properties, ArmorModHandler.ArmorModSlot.EXTRA, false, false, true, false);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("You will die when I allow you to.").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("Infinite revives left").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            tooltip.add(installedLine(mod, ChatFormatting.GOLD, " (infinite revives left)"));
        }

        public void handleDeath(LivingDeathEvent event, ItemStack armor, ItemStack mod) {
            LivingEntity entity = event.getEntity();
            if (HbmLivingProperties.getRadiation(entity) >= 1000.0F) {
                return;
            }
            int count = mod.getOrCreateTag().getInt(REVIVE_COUNT_TAG) + 1;
            mod.getOrCreateTag().putInt(REVIVE_COUNT_TAG, count);
            ArmorModHandler.applyMod(armor, mod);
            revive(entity);
            HbmLivingProperties.incrementRadiation(entity, count * count);
            event.setCanceled(true);
        }
    }

    private static void revive(LivingEntity entity) {
        entity.setHealth(entity.getMaxHealth());
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 99));
    }

    public static class Insert extends ArmorModItem {
        private final float damageMod;
        private final float projectileMod;
        private final float explosionMod;
        private final float speed;
        private final boolean polonium;
        private final boolean explosiveReactiveArmor;

        public Insert(Item.Properties properties, int durability, float damageMod, float projectileMod,
                      float explosionMod, float speed, boolean polonium, boolean explosiveReactiveArmor) {
            super(properties.durability(durability), ArmorModHandler.ArmorModSlot.KEVLAR, false, true, false, false, false);
            this.damageMod = damageMod;
            this.projectileMod = projectileMod;
            this.explosionMod = explosionMod;
            this.speed = speed;
            this.polonium = polonium;
            this.explosiveReactiveArmor = explosiveReactiveArmor;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            if (damageMod != 1.0F) {
                tooltip.add(Component.literal(percentLine("damage", damageMod)).withStyle(ChatFormatting.RED));
            }
            if (projectileMod != 1.0F) {
                tooltip.add(Component.literal(percentLine("projectile damage", projectileMod)).withStyle(ChatFormatting.YELLOW));
            }
            if (explosionMod != 1.0F) {
                tooltip.add(Component.literal(percentLine("explosion damage", explosionMod)).withStyle(ChatFormatting.YELLOW));
            }
            if (speed != 1.0F) {
                tooltip.add(Component.literal(percentLine("speed", speed)).withStyle(ChatFormatting.BLUE));
            }
            if (polonium) {
                tooltip.add(Component.literal("+100 RAD/s").withStyle(ChatFormatting.DARK_RED));
            }
            tooltip.add(Component.literal((stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage() + "HP"));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                                   TooltipFlag flag) {
            List<String> desc = new ArrayList<>();
            if (damageMod != 1.0F) {
                desc.add((damageMod < 1.0F ? "-" : "+")
                        + Math.abs(Math.round((1.0F - damageMod) * 100.0F)) + "% dmg");
            }
            if (projectileMod != 1.0F) {
                desc.add("-" + Math.round((1.0F - projectileMod) * 100.0F) + "% proj");
            }
            if (explosionMod != 1.0F) {
                desc.add("-" + Math.round((1.0F - explosionMod) * 100.0F) + "% exp");
                desc.add("-" + Math.round((1.0F - speed) * 100.0F) + "% speed");
            }
            if (polonium) {
                desc.add("+100 RAD/s");
            }
            tooltip.add(installedLine(mod, ChatFormatting.DARK_PURPLE,
                    " (" + String.join(" / ", desc) + " / "
                            + (mod.getMaxDamage() - mod.getDamageValue()) + "HP)"));
        }

        @Override
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (polonium && !entity.level().isClientSide) {
                HbmLivingProperties.incrementRadiation(entity, 100.0F);
            }
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            float amount = event.getAmount() * damageMod;
            if (event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
                amount *= projectileMod;
            }
            if (event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
                amount *= explosionMod;
            }
            event.setAmount(amount);

            if (!event.getEntity().level().isClientSide && explosiveReactiveArmor) {
                LivingEntity entity = event.getEntity();
                entity.level().explode(event.getSource().getEntity(), entity.getX(),
                        entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                        0.05F, false, Level.ExplosionInteraction.NONE);
            }

            damageInstalledInsert(armor, mod);
        }

        @Override
        public void addArmorModAttributeModifiers(ItemStack armor, ItemStack mod,
                                                  Multimap<Attribute, AttributeModifier> modifiers) {
            if (speed == 1.0F || !(armor.getItem() instanceof ArmorItem armorItem)) {
                return;
            }
            modifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                    ArmorModHandler.modifierUuidFor(armorItem.getType()),
                    "NTM Armor Mod Speed",
                    -1.0F + speed,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        private void damageInstalledInsert(ItemStack armor, ItemStack mod) {
            mod.setDamageValue(mod.getDamageValue() + 1);
            if (mod.getDamageValue() >= mod.getMaxDamage()) {
                ArmorModHandler.removeMod(armor, ArmorModHandler.kevlar);
            } else {
                ArmorModHandler.applyMod(armor, mod);
            }
        }

        private static String percentLine(String label, float modifier) {
            int percent = Math.abs(Math.round((1.0F - modifier) * 100.0F));
            return (modifier < 1.0F ? "-" : "+") + percent + "% " + label;
        }
    }

    private static ChatFormatting blink(ChatFormatting first, ChatFormatting second) {
        return System.currentTimeMillis() % 1000L < 500L ? first : second;
    }

    private static Component installedLine(ItemStack stack, ChatFormatting color, String suffix) {
        return Component.literal("  ")
                .append(stack.getHoverName())
                .append(Component.literal(suffix))
                .withStyle(color);
    }

    private static Component noIndentInstalledLine(ItemStack stack, ChatFormatting color, String suffix) {
        return stack.getHoverName().copy()
                .append(Component.literal(suffix))
                .withStyle(color);
    }

    private ArmorModItems() {
    }
}
