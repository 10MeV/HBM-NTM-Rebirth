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
        int id = 100;
        for (PollutionType type : PollutionType.legacyDetectorTypes()) {
            ModMessages.informPlayer(player, readout(type, data), id++, 4_000);
        }
    }

    private static Component readout(PollutionType type, PollutionSavedData.PollutionSample data) {
        PollutionSavedData.PollutionSample sample = data == null ? new PollutionSavedData.PollutionSample() : data;
        return Component.literal(type.displayName() + ": " + sample.formatValue(type))
                .withStyle(ChatFormatting.YELLOW);
    }
}
