package com.hbm.ntm.blockentity;

import com.hbm.ntm.entity.missile.BobmazonDeliveryEntity;
import com.hbm.ntm.item.LegacyCustomKitItem;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LanternBehemothBlockEntity extends LegacyLanternBlockEntity {
    public LanternBehemothBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LANTERN_BEHEMOTH.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LanternBehemothBlockEntity blockEntity) {
        if (level.isClientSide) return;
        int timer = blockEntity.getComTimer();
        if (timer == 360) level.playSound(null, pos, ModSounds.BLOCK_HORN_NEAR_SINGLE.get(), SoundSource.BLOCKS, 10.0F, 1.0F);
        if (timer == 280) level.playSound(null, pos, ModSounds.BLOCK_HORN_FAR_SINGLE.get(), SoundSource.BLOCKS, 10000.0F, 1.0F);
        if (timer == 220) level.playSound(null, pos, ModSounds.BLOCK_HORN_NEAR_DUAL.get(), SoundSource.BLOCKS, 10.0F, 1.0F);
        if (timer == 100) level.playSound(null, pos, ModSounds.BLOCK_HORN_FAR_DUAL.get(), SoundSource.BLOCKS, 10000.0F, 1.0F);
        if (timer == 0) deliverSupplies(level, pos);
        if (timer >= 0) blockEntity.setComTimer(timer - 1);
        blockEntity.networkPackNT(250);
    }

    @Override
    public void setRemoved() {
        if (!level.isClientSide) {
            AABB area = new AABB(worldPosition.offset(-50, -50, -50), worldPosition.offset(51, 51, 51));
            for (Player player : level.getEntitiesOfClass(Player.class, area)) {
                HbmPlayerProperties.decrementReputationAbove(player, -25);
            }
        }
        super.setRemoved();
    }

    public void repair() {
        setBroken(false);
        setComTimer(400);
    }

    private static void deliverSupplies(Level level, BlockPos pos) {
        AABB area = new AABB(pos.offset(-10, -10, -10), pos.offset(11, 11, 11));
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        Player first = players.isEmpty() ? null : players.get(0);
        boolean bonus = first != null && HbmPlayerProperties.hasReputationAtLeast(first, 10);
        BobmazonDeliveryEntity shuttle = new BobmazonDeliveryEntity(level);
        shuttle.setPos(pos.getX() + 0.5D + level.random.nextGaussian() * 10.0D, 300.0D,
                pos.getZ() + 0.5D + level.random.nextGaussian() * 10.0D);
        shuttle.setPayload(LegacyCustomKitItem.create("Supplies", null, 0xFFFFFF, 0x008000,
                new ItemStack(ModItems.legacyItem("circuit_basic").get(), 4 + level.random.nextInt(4)),
                new ItemStack(ModItems.legacyItem("circuit_advanced").get(), 4 + level.random.nextInt(2)),
                bonus ? new ItemStack(ModItems.legacyItem("gem_alexandrite").get())
                        : new ItemStack(net.minecraft.world.item.Items.DIAMOND, 6 + level.random.nextInt(6)),
                new ItemStack(net.minecraft.world.level.block.Blocks.POPPY)));
        level.addFreshEntity(shuttle);
    }
}
