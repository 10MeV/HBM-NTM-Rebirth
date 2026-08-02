package com.hbm.ntm.block;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.blockentity.RefineryBlockEntity;
import com.hbm.ntm.entity.projectile.AirstrikeBombletEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.AchievementHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class RefineryBlock extends LegacyVisibleMultiblockMachineBlock {
    public static final BooleanProperty EXPLODED = BooleanProperty.create("exploded");
    public static final BooleanProperty TILTED = BooleanProperty.create("tilted");

    public RefineryBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
        registerDefaultState(defaultBlockState()
                .setValue(EXPLODED, false)
                .setValue(TILTED, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RefineryBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        if ((state.hasProperty(EXPLODED) && state.getValue(EXPLODED))
                || (state.hasProperty(TILTED) && state.getValue(TILTED))) {
            return super.getRenderShape(state);
        }
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (resolveCoreBlockEntity(level, pos) instanceof RefineryBlockEntity refinery) {
            if (refinery.isExploded()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, refinery, refinery.getBlockPos());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.REFINERY.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                RefineryBlockEntity.clientTick(tickLevel, tickPos, tickState, (RefineryBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                RefineryBlockEntity.serverTick(tickLevel, tickPos, tickState, (RefineryBlockEntity) blockEntity);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getLevel() instanceof ServerLevel
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof RefineryBlockEntity refinery) {
            return List.of(refinery.createPersistentBlockDrop(asItem()));
        }
        return super.getDrops(state, builder);
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if (!(resolveCoreBlockEntity(level, pos) instanceof RefineryBlockEntity refinery)) {
            return;
        }
        if (!refinery.markExplosionHandled(explosion)) {
            return;
        }
        if (!refinery.isExploded()) {
            awardInfernoForZetaStrike(level, refinery, explosion);
            refinery.explode();
        } else {
            level.setBlock(refinery.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
        }
    }

    /** MachineRefinery#onBlockExploded awards Inferno for a legacy Zeta hit. */
    private static void awardInfernoForZetaStrike(Level level, RefineryBlockEntity refinery, Explosion explosion) {
        if (!(explosion.getExploder() instanceof AirstrikeBombletEntity)) {
            return;
        }
        AABB range = new AABB(refinery.getBlockPos()).inflate(100.0D);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, range)) {
            AchievementHandler.award(player, AchievementHandler.INFERNO);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(HbmPersistentBlockState.TAG_PERSISTENT, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag persistent = tag.getCompound(HbmPersistentBlockState.TAG_PERSISTENT);
        for (HbmFluidTank tank : readTooltipTanks(persistent)) {
            tooltip.add(HbmFluidGuiHelper.tankInfo(tank, tank.getFill(), tank.getMaxFill())
                    .copy()
                    .withStyle(ChatFormatting.YELLOW));
        }
        if (persistent.getBoolean("hasExploded")) {
            tooltip.add(Component.translatable("container.fluidtank.damaged").withStyle(ChatFormatting.RED));
        }
        if (persistent.getBoolean("onFire")) {
            tooltip.add(Component.translatable("container.fluidtank.burning").withStyle(ChatFormatting.RED));
        }
    }

    private static List<HbmFluidTank> readTooltipTanks(CompoundTag persistent) {
        List<HbmFluidTank> tanks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            HbmFluidTank tank = new HbmFluidTank(HbmFluids.NONE, 0);
            tank.readFromNbt(persistent, Integer.toString(i));
            tanks.add(tank);
        }
        return tanks;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(EXPLODED, TILTED);
    }
}
