package com.hbm.ntm.block;

import com.hbm.ntm.itempool.HbmItemPoolIds;
import com.hbm.ntm.itempool.HbmItemPoolRegistry;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.Vec3;

/**
 * Ground-refresh meteorite treasure from 1.7.10 BlockMeteoriteTreasure.
 * The legacy block makes one to three independent POOL_METEORITE_TREASURE rolls.
 */
public final class MeteoriteTreasureBlock extends Block {
    public MeteoriteTreasureBlock(Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(net.minecraft.world.level.block.state.BlockState state, LootParams.Builder builder) {
        if (!(builder.getLevel() instanceof ServerLevel)) {
            return List.of();
        }
        ServerLevel level = (ServerLevel) builder.getLevel();

        Vec3 origin = builder.getParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN);

        List<ItemStack> drops = new java.util.ArrayList<>();
        int rolls = 1 + level.random.nextInt(3);
        for (int index = 0; index < rolls; index++) {
            drops.addAll(HbmItemPoolRegistry.getStacks(level, HbmItemPoolIds.POOL_METEORITE_TREASURE, origin));
        }
        return List.copyOf(drops);
    }
}
