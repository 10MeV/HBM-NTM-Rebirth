package com.hbm.ntm.item;

import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.blockentity.ZirnoxReactorBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** The 1.7.10 Dyatlov tool: deliberately invokes the legacy reactor failure paths. */
public class MeltdownToolItem extends Item {
    public MeltdownToolItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            BlockEntity core = MultiblockHelper.resolveCoreBlockEntity(level, context.getClickedPos());
            if (core instanceof RBMKColumnBlockEntity rbmk) {
                rbmk.triggerLegacyMeltdown();
            } else if (core instanceof ZirnoxReactorBlockEntity zirnox) {
                zirnox.setLegacyMeltdownHeat();
            }
        }
        return InteractionResult.PASS;
    }
}
