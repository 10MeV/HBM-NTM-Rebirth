package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.NavalMineBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class NavalMineBlockEntity extends BlockEntity {
    private boolean primed;
    public boolean waitingForPlayer;

    public NavalMineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NAVAL_MINE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NavalMineBlockEntity mine) {
        if (level.isClientSide() || !(state.getBlock() instanceof NavalMineBlock block)) {
            return;
        }

        double range = block.triggerRange();
        double height = block.triggerHeight();

        if (mine.waitingForPlayer) {
            range = 25.0D;
            height = 25.0D;
        } else if (!mine.primed) {
            range *= 2.0D;
            height *= 2.0D;
        }

        if (!level.getBlockState(pos.above()).isAir()) {
            return;
        }

        AABB bounds = new AABB(pos.getX() - range, pos.getY() - height, pos.getZ() - range,
                pos.getX() + range + 1.0D, pos.getY() + height, pos.getZ() + range + 1.0D);
        for (Entity entity : level.getEntities(null, bounds)) {
            MobCategory category = entity.getType().getCategory();
            if (category == MobCategory.WATER_CREATURE || category == MobCategory.AMBIENT) {
                continue;
            }

            if (mine.waitingForPlayer) {
                if (entity instanceof Player) {
                    mine.waitingForPlayer = false;
                    mine.setChanged();
                    return;
                }
            } else if (entity instanceof LivingEntity) {
                if (mine.primed) {
                    block.explode(level, pos);
                }
                return;
            }
        }

        if (!mine.primed && !mine.waitingForPlayer) {
            LegacySoundPlayer.playLegacyFstbmbStart(level, pos, 3.0F, 1.0F);
            mine.primed = true;
            mine.setChanged();
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        primed = tag.getBoolean("primed");
        waitingForPlayer = tag.getBoolean("waiting");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("primed", primed);
        tag.putBoolean("waiting", waitingForPlayer);
    }
}
