package com.hbm.ntm.block;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.blockentity.StirlingBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class StirlingBlock extends LegacyVisibleMultiblockMachineBlock {
    private final Kind kind;

    public StirlingBlock(Properties properties, LegacyMachineDefinition definition, Kind kind) {
        super(properties, definition);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StirlingBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!isMatchingCog(held)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && resolveCoreBlockEntity(level, pos) instanceof StirlingBlockEntity stirling
                && !stirling.hasCog()) {
            stirling.installCog();
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            LegacySoundPlayer.playSoundEffect(level, pos, "hbm:item.upgradePlug",
                    SoundSource.BLOCKS, 1.5F, 0.75F);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.STIRLING.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        StirlingBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (StirlingBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        StirlingBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (StirlingBlockEntity) blockEntity);
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
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof StirlingBlockEntity stirling) {
            ItemStack stack = new ItemStack(this);
            stirling.writePersistentStateToStack(stack);
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
                && tag.getCompound(HbmPersistentBlockState.TAG_PERSISTENT).getBoolean("missingCog")) {
            tooltip.add(Component.literal("Gear missing!").withStyle(ChatFormatting.RED));
        }
    }

    private boolean isMatchingCog(ItemStack stack) {
        if (stack.isEmpty() || kind.creative()) {
            return false;
        }
        return switch (kind) {
            case NORMAL -> stack.is(ModItems.GEAR_LARGE.get());
            case STEEL -> stack.is(ModItems.GEAR_LARGE_STEEL.get());
            case CREATIVE -> false;
        };
    }

    public enum Kind {
        NORMAL(0, false),
        STEEL(1, false),
        CREATIVE(2, true);

        private final int gearMeta;
        private final boolean creative;

        Kind(int gearMeta, boolean creative) {
            this.gearMeta = gearMeta;
            this.creative = creative;
        }

        public int gearMeta() {
            return gearMeta;
        }

        public boolean creative() {
            return creative;
        }
    }
}
