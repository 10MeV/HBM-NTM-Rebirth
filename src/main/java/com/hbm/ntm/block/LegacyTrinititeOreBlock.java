package com.hbm.ntm.block;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyTrinititeOreBlock extends Block {
    public LegacyTrinititeOreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            RadiationUtil.addRadiationPoisoning(living, 30 * 20, 0);
        }
    }

    public Item droppedItem() {
        return ModItems.legacyItem("trinitite").get();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        ParticleUtil.spawnTownAuraOnOpenFaces(level, pos, random);
    }
}
