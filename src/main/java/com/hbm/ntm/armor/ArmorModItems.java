package com.hbm.ntm.armor;

import com.google.common.collect.Multimap;
import com.hbm.ntm.api.item.ArmorDashProvider;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import javax.annotation.Nullable;
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
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            if (armor.getDamageValue() > 0 && event.getEntity().getRandom().nextInt(5) != 0) {
                armor.setDamageValue(armor.getDamageValue() - 1);
            }
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
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            if (event.getSource().is(DamageTypes.FALL)) {
                event.setAmount(event.getAmount() * damageMod);
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
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (HbmLivingProperties.getDigamma(entity) < 5.0F) {
                return;
            }
            ArmorModHandler.removeMod(armor, ArmorModHandler.extra);
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.TOOL_SYRINGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
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
        public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
            if (entity.level().isClientSide || entity.tickCount % 50 != 0 || entity.getMaxHealth() <= 2.0F) {
                return;
            }

            AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth == null) {
                return;
            }

            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.ENTITY_SLICER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

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
            tooltip.add(Component.literal("Attack hook pending").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
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
                tooltip.add(Component.literal("Ammo consumption hook pending").withStyle(ChatFormatting.DARK_GRAY));
            }
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
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
        private final boolean meteor;

        public Charm(Item.Properties properties, boolean meteor) {
            super(properties, ArmorModHandler.ArmorModSlot.HELMET_ONLY, true, true, false, false);
            this.meteor = meteor;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("You feel blessed.").withStyle(ChatFormatting.AQUA));
            if (meteor) {
                tooltip.add(Component.literal("Negates broadcaster damage").withStyle(ChatFormatting.AQUA));
            } else {
                tooltip.add(Component.literal("Halves broadcaster damage").withStyle(ChatFormatting.AQUA));
            }
            tooltip.add(Component.empty());
            super.appendHoverText(stack, level, tooltip, flag);
        }

        @Override
        public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
            if (!event.getSource().is(ModDamageSources.BROADCAST)) {
                return;
            }
            event.setAmount(meteor ? 0.0F : event.getAmount() * 0.5F);
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
                            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                    ModSounds.TOOL_TECH_BOOP.get(), SoundSource.PLAYERS, 2.0F, 1.5F);
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

    private ArmorModItems() {
    }
}
