package com.hbm.ntm.item;

import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionSavedData;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PollutionDetectorItem extends Item {
    public PollutionDetectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof ServerPlayer player) || level.getGameTime() % 10L != 0L) {
            return;
        }

        PollutionSavedData.PollutionSample data = PollutionManager.getPollutionData(level, entity.blockPosition());
        ModMessages.informPlayer(player, readout("Soot", data.get(PollutionType.SOOT)), 100, 4_000);
        ModMessages.informPlayer(player, readout("Poison", data.get(PollutionType.POISON)), 101, 4_000);
        ModMessages.informPlayer(player, readout("Heavy metal", data.get(PollutionType.HEAVYMETAL)), 102, 4_000);
    }

    private static Component readout(String label, float value) {
        float rounded = ((int) (value * 100.0F)) / 100.0F;
        return Component.literal(label + ": " + rounded).withStyle(ChatFormatting.YELLOW);
    }
}
