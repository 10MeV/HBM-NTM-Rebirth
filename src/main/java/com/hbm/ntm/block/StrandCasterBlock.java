package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.StrandCasterBlockEntity;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class StrandCasterBlock extends LegacyVisibleMultiblockMachineBlock implements Toolable {
    public StrandCasterBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StrandCasterBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && resolveCoreBlockEntity(level, pos) instanceof StrandCasterBlockEntity caster) {
            ItemStack held = player.getItemInHand(hand);
            if (FoundryMoldItem.isMold(held) && caster.getMold().isEmpty()) {
                caster.setMold(held.split(1));
                LegacySoundPlayer.playLegacyUpgradePlug(level, pos, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.CONSUME;
            }
            if (held.getItem() instanceof ShovelItem && caster.getMoltenAmount() > 0) {
                HbmInventoryMenuHelper.giveOrDrop(player, caster.drainMoltenAsScrap());
                return InteractionResult.CONSUME;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, caster, caster.getBlockPos());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.STRAND_CASTER.get()) {
            return null;
        }
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) -> StrandCasterBlockEntity.serverTick(
                        tickLevel, tickPos, tickState, (StrandCasterBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof StrandCasterBlockEntity caster) {
            for (ItemStack stack : caster.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }
        if (!level.isClientSide
                && MultiblockHelper.resolveCoreBlockEntity(level, pos) instanceof StrandCasterBlockEntity caster) {
            ItemStack mold = caster.removeMold();
            if (!mold.isEmpty()) {
                HbmInventoryMenuHelper.giveOrDrop(player, mold);
            }
        }
        return true;
    }
}
