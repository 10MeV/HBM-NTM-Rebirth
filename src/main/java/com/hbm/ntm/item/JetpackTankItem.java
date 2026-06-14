package com.hbm.ntm.item;

import com.hbm.ntm.api.fluid.IFillableItem;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class JetpackTankItem extends Item {
    private static final int FILL_AMOUNT = 1_000;

    public JetpackTankItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack tank = player.getItemInHand(hand);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty()) {
            return InteractionResultHolder.pass(tank);
        }

        ItemStack target = chest;
        boolean installedModule = false;
        if (chest.getItem() instanceof ArmorItem && ArmorModHandler.hasMods(chest)) {
            target = ArmorModHandler.pryMod(chest, ArmorModHandler.plate_only);
            installedModule = true;
        }
        if (target.isEmpty() || !(target.getItem() instanceof IFillableItem fillable)) {
            return InteractionResultHolder.pass(tank);
        }
        if (!fillable.acceptsFluid(HbmFluids.KEROSENE, target)) {
            return InteractionResultHolder.pass(tank);
        }

        if (!level.isClientSide) {
            int remainder = fillable.tryFill(HbmFluids.KEROSENE, FILL_AMOUNT, target);
            if (remainder >= FILL_AMOUNT) {
                return InteractionResultHolder.pass(tank);
            }
            if (installedModule) {
                ArmorModHandler.applyMod(chest, target);
            }
            LegacySoundPlayer.playLegacyJetpackTank(player);
            tank.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(tank, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Fills worn jetpack with up to 1000mB of kerosene").withStyle(ChatFormatting.GRAY));
    }
}
