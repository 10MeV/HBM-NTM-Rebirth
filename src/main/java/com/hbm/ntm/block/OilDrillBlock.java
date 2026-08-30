package com.hbm.ntm.block;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.blockentity.OilDrillBlockEntity;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorStandard;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.particle.LegacyParticleCreators;
import com.hbm.ntm.registry.ModBlockEntities;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class OilDrillBlock extends LegacyVisibleMultiblockMachineBlock {
    private static final String DERRICK_MODEL = "models/machines/derrick.obj";
    private static final String PUMPJACK_MODEL = "models/machines/pumpjack.obj";

    public OilDrillBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        String modelPath = definition().modelLocation().getPath();
        if (PUMPJACK_MODEL.equals(modelPath)) {
            return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
        }
        return usesChunkBakedStaticModel()
                ? LegacyMachineRenderShapes.chunkBakedStaticOrEntity()
                : super.getRenderShape(state);
    }

    public boolean usesChunkBakedStaticModel() {
        return DERRICK_MODEL.equals(definition().modelLocation().getPath());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OilDrillBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!player.isShiftKeyDown() && !level.isClientSide && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof OilDrillBlockEntity drill) {
            NetworkHooks.openScreen(serverPlayer, drill, drill.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof OilDrillBlockEntity drill) {
            for (ItemStack stack : drill.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.OIL_DRILL.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) -> {
                    if (blockEntity instanceof OilDrillBlockEntity drill) {
                        OilDrillBlockEntity.clientTick(tickLevel, tickPos, tickState, drill);
                    }
                }
                : (tickLevel, tickPos, tickState, blockEntity) -> {
                    if (blockEntity instanceof OilDrillBlockEntity drill) {
                        OilDrillBlockEntity.serverTick(tickLevel, tickPos, tickState, drill);
                    }
                };
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getLevel() instanceof ServerLevel
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof OilDrillBlockEntity drill) {
            return List.of(drill.createPersistentBlockDrop(asItem()));
        }
        return super.getDrops(state, builder);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if (resolveCoreBlockEntity(level, pos) instanceof OilDrillBlockEntity drill
                && drill.getKind() == OilDrillBlockEntity.Kind.WELL) {
            // MachineOilWell removes the hit core before the secondary VNT pass. This keeps the
            // VNT allocator from re-entering the well's explosion hook while it clears dummies.
            level.removeBlock(pos, false);
            if (drill.hasStoredFluid()) {
                drill.clearStoredFluids();
                BlockPos corePos = drill.getBlockPos();
                double x = corePos.getX() + 0.5D;
                double y = corePos.getY() + 0.5D;
                double z = corePos.getZ() + 0.5D;
                new ExplosionVnt(level, x, y, z, 15.0F)
                        .setBlockAllocator(new BlockAllocatorStandard(24))
                        .setBlockProcessor(new BlockProcessorStandard().setNoDrop())
                        .setEntityProcessor(new EntityProcessorStandard())
                        .setPlayerProcessor(new PlayerProcessorStandard())
                        .explode();
                LegacyParticleCreators.composeEffect(level, x, y, z, 10, 2.0F, 0.5F, 25.0F,
                        5, 8, 20, 0.75F, 1.0F, -2.0F, 150.0F);
            }
            return;
        }
        super.onBlockExploded(state, level, pos, explosion);
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
        tooltip.add(Component.literal(shortPower(persistent.getLong("power")) + "HE").withStyle(ChatFormatting.GREEN));
        for (HbmFluidTank tank : readTooltipTanks(persistent)) {
            // MachineOilWell/MachinePumpjack/MachineFrackingTower all use the
            // IPersistentInfoProvider format directly: no separators around
            // the slash/unit and the fluid name follows the capacity.
            tooltip.add(Component.literal(tank.getFill() + "/" + tank.getMaxFill() + "mB ")
                    .append(tank.getTankType().getDisplayName())
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private static List<HbmFluidTank> readTooltipTanks(CompoundTag persistent) {
        List<HbmFluidTank> tanks = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            HbmFluidTank tank = new HbmFluidTank(HbmFluids.NONE, 0);
            tank.readFromNbt(persistent, "t" + i);
            tanks.add(tank);
        }
        return tanks;
    }

    private static String shortPower(long power) {
        double value;
        String suffix;
        long absolute = Math.abs(power);
        if (absolute >= 1_000_000_000_000_000_000L) {
            value = power / 1_000_000_000_000_000_000.0D;
            suffix = "E";
        } else if (absolute >= 1_000_000_000_000_000L) {
            value = power / 1_000_000_000_000_000.0D;
            suffix = "P";
        } else if (absolute >= 1_000_000_000_000L) {
            value = power / 1_000_000_000_000.0D;
            suffix = "T";
        } else if (absolute >= 1_000_000_000L) {
            value = power / 1_000_000_000.0D;
            suffix = "G";
        } else if (absolute >= 1_000_000L) {
            value = power / 1_000_000.0D;
            suffix = "M";
        } else if (absolute >= 1_000L) {
            value = power / 1_000.0D;
            suffix = "k";
        } else {
            return Long.toString(power);
        }

        // Exact BobMathUtil#getShortNumber rounding used by all three legacy
        // oil-drill-family IPersistentInfoProvider implementations.
        if (value <= -100.0D) {
            value = Math.round(value * 10.0D) / 10.0D;
        } else {
            value = Math.round(value * 100.0D) / 100.0D;
        }
        return value + suffix;
    }
}
