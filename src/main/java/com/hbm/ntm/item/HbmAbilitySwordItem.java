package com.hbm.ntm.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hbm.ntm.ability.AvailableAbilities;
import com.hbm.ntm.ability.IWeaponAbility;
import com.hbm.ntm.ability.WeaponHitContext;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class HbmAbilitySwordItem extends SwordItem {
    private static final UUID MOVEMENT_SPEED_UUID = UUID.fromString("2fe9df8d-52ab-49d0-ace4-4152fc38c3d8");

    protected final AvailableAbilities availableAbilities = new AvailableAbilities();
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    private final float attackDamage;
    private final double movementModifier;
    private boolean playGavelHitSound;

    public HbmAbilitySwordItem(Tier tier, float attackDamageModifier, double movementModifier, Properties properties) {
        super(tier, 0, 0.0F, properties);
        this.attackDamage = attackDamageModifier;
        this.movementModifier = movementModifier;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                "Tool modifier", attackDamageModifier, AttributeModifier.Operation.ADDITION));
        if (movementModifier != 0.0D) {
            builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(MOVEMENT_SPEED_UUID,
                    "Tool movement modifier", movementModifier, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        this.defaultModifiers = builder.build();
    }

    public HbmAbilitySwordItem addAbility(IWeaponAbility ability, int level) {
        availableAbilities.addAbility(ability, level);
        return this;
    }

    public HbmAbilitySwordItem playGavelHitSound() {
        this.playGavelHitSound = true;
        return this;
    }

    public AvailableAbilities availableAbilities() {
        return availableAbilities;
    }

    public float legacyAttackDamage() {
        return attackDamage;
    }

    public double legacyMovementModifier() {
        return movementModifier;
    }

    public boolean canOperate(ItemStack stack) {
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity attacker) {
        if (!attacker.level().isClientSide && attacker instanceof ServerPlayer player && canOperate(stack)) {
            if (playGavelHitSound) {
                LegacySoundPlayer.playLegacyGavelWhack(attacker.level(),
                        victim.getX(), victim.getY(), victim.getZ(), 3.0F, 1.0F);
            }
            WeaponHitContext context = new WeaponHitContext(attacker.level(), player, victim, stack);
            availableAbilities.getWeaponAbilities().forEach((ability, level) -> ability.onHit(level, context));
        }
        return super.hurtEnemy(stack, victim, attacker);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<net.minecraft.network.chat.Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        availableAbilities.addInformation(tooltip);
    }
}
