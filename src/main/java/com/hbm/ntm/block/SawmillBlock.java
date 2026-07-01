package com.hbm.ntm.block;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.blockentity.SawmillBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class SawmillBlock extends LegacyVisibleMultiblockMachineBlock {
    public SawmillBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SawmillBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(resolveCoreBlockEntity(level, pos) instanceof SawmillBlockEntity sawmill)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (!sawmill.hasBlade() && held.is(ModItems.SAWBLADE.get()) && sawmill.installBlade()) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            LegacySoundPlayer.playSoundEffect(level, pos, "hbm:item.upgradePlug",
                    SoundSource.BLOCKS, 1.5F, 0.75F);
            return InteractionResult.CONSUME;
        }
        if (sawmill.takeOutputs(player)) {
            return InteractionResult.CONSUME;
        }
        if (sawmill.insertHeldInput(player, held)) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.SAWMILL.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        SawmillBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (SawmillBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        SawmillBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (SawmillBlockEntity) blockEntity);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HbmPersistentBlockState persistent) {
            persistent.readPersistentStateFromStack(stack);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getLevel() instanceof ServerLevel
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof SawmillBlockEntity sawmill) {
            ItemStack stack = new ItemStack(this);
            sawmill.writePersistentStateToStack(stack);
            return List.of(stack);
        }
        return super.getDrops(state, builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(HbmPersistentBlockState.TAG_PERSISTENT, Tag.TAG_COMPOUND)
                && tag.getCompound(HbmPersistentBlockState.TAG_PERSISTENT).getBoolean("missingBlade")) {
            tooltip.add(Component.literal("Blade missing!").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof SawmillBlockEntity sawmill) {
            for (ItemStack stack : sawmill.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
