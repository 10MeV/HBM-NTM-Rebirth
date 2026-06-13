package com.hbm.ntm.item;

import com.hbm.ntm.player.HbmPlayerProperties;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NcrpaArmorItem extends FsbPoweredArmorItem {
    private static final UUID SPRINT_SPEED_UUID = UUID.fromString("6ab858ba-d712-485c-bae9-e5e765fc555a");
    private static final String SPRINT_SPEED_NAME = "NCRPA SPEED";

    public NcrpaArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain,
            FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain, fullSetTraits);
    }

    @Override
    public void tickEquippedArmor(ItemStack stack, Level level, Player player) {
        super.tickEquippedArmor(stack, level, player);
        if (!level.isClientSide && getType() == Type.CHESTPLATE && hasFullSet(player)
                && level.getGameTime() % 20L == 0L && HbmPlayerProperties.isHudEnabled(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
        }
    }

    public static void reconcileSprintBoost(Player player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasNcrpaChest = chest.getItem() instanceof NcrpaArmorItem ncrpa
                && ncrpa.getType() == Type.CHESTPLATE;
        AttributeModifier existing = speed.getModifier(SPRINT_SPEED_UUID);
        if (existing != null && (SPRINT_SPEED_NAME.equals(existing.getName()) || hasNcrpaChest)) {
            speed.removeModifier(SPRINT_SPEED_UUID);
        }
        if (hasNcrpaChest && player.isSprinting()) {
            speed.addTransientModifier(new AttributeModifier(SPRINT_SPEED_UUID, SPRINT_SPEED_NAME,
                    0.1D, AttributeModifier.Operation.ADDITION));
        }
    }
}
