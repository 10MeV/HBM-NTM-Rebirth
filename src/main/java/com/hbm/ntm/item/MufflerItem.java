package com.hbm.ntm.item;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.compat.CompatExternal;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class MufflerItem extends Item {
    public MufflerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity core = CompatExternal.getCoreFromPos(level, pos);
        if (level.isClientSide) {
            if (core instanceof HbmLegacyLoadedTile loadedTile) {
                return loadedTile.isMuffled() ? InteractionResult.PASS : InteractionResult.SUCCESS;
            }
            return mayResolveHbmMachine(level, pos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!(core instanceof HbmLegacyLoadedTile loadedTile) || loadedTile.isMuffled()) {
            return InteractionResult.PASS;
        }
        if (!loadedTile.muffle()) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        if (player != null) {
            LegacySoundPlayer.playSoundAtPlayer(player, "hbm:item.upgradePlug", SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            LegacySoundPlayer.playSoundEffect(level, pos, "hbm:item.upgradePlug", SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.CONSUME;
    }

    private static boolean mayResolveHbmMachine(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof MultiblockCoreBlock
                || state.is(ModBlocks.DUMMY_BLOCK.get())
                || state.hasBlockEntity() && isHbmBlock(state);
    }

    private static boolean isHbmBlock(BlockState state) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return id != null && HbmNtm.MOD_ID.equals(id.getNamespace());
    }
}
