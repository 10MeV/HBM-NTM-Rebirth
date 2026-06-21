package com.hbm.ntm.block;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.item.FoundryScrapsItem;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

public class OldBoilerBlock extends HorizontalMachineBlock {
    public OldBoilerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack scrap = FoundryScrapsItem.create(new MaterialStack(Mats.MAT_STEEL, MaterialShapes.INGOT.q(1)));
        scrap.setCount(3 + builder.getLevel().random.nextInt(4));
        return List.of(scrap);
    }
}
