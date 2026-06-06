package com.hbm.ntm.item;

import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationData;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class GeigerCounterItem extends Item {
    public GeigerCounterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof LivingEntity living) || level.getGameTime() % 5L != 0L) {
            return;
        }

        float rate = RadiationData.getRadBuf(living);
        if (rate > 1.0E-5F) {
            List<Integer> candidates = new ArrayList<>();
            if (rate < 1.0F) candidates.add(0);
            if (rate < 5.0F) candidates.add(0);
            if (rate < 10.0F) candidates.add(1);
            if (rate > 5.0F && rate < 15.0F) candidates.add(2);
            if (rate > 10.0F && rate < 20.0F) candidates.add(3);
            if (rate > 15.0F && rate < 25.0F) candidates.add(4);
            if (rate > 20.0F && rate < 30.0F) candidates.add(5);
            if (rate > 25.0F) candidates.add(6);

            int sound = candidates.get(living.getRandom().nextInt(candidates.size()));
            if (sound > 0) {
                playGeiger(level, entity, sound);
            }
        } else if (living.getRandom().nextInt(50) == 0) {
            playGeiger(level, entity, 1);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), ModSounds.TOOL_TECH_BOOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            RadiationUtil.printGeigerData(player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void playGeiger(Level level, Entity entity, int sound) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.geiger(sound), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
