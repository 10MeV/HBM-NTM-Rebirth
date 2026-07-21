package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.CrashedBombBlockEntity;
import com.hbm.ntm.entity.logic.BalefireExplosionEntity;
import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorStandard;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorCross;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CrashedBombBlock extends Block implements EntityBlock, RemoteDetonatableBlock {
    public static final EnumProperty<CrashedBombType> TYPE = EnumProperty.create("type", CrashedBombType.class);

    public CrashedBombBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TYPE, CrashedBombType.BALEFIRE));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // The legacy block was always TESR-rendered: each position has a deterministic crash attitude.
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrashedBombBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.CRASHED_BOMB.get() && !level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) -> CrashedBombBlockEntity.serverTick(tickLevel,
                        tickPos, tickState, (CrashedBombBlockEntity) blockEntity)
                : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!player.getItemInHand(hand).is(ModItems.DEFUSER.get())) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            for (ItemStack drop : defuserDrops(state.getValue(TYPE))) {
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        drop));
            }
            level.removeBlock(pos, false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level == null || level.isClientSide || level.getBlockState(pos).getBlock() != this) {
            return BombReturnCode.UNDEFINED;
        }
        CrashedBombType type = level.getBlockState(pos).getValue(TYPE);
        level.removeBlock(pos, false);
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        switch (type) {
            case BALEFIRE -> {
                level.addFreshEntity(BalefireExplosionEntity.create(level, pos.getX(), pos.getY(), pos.getZ(),
                        (int) (com.hbm.ntm.config.BombConfig.fatmanRadius() * 1.25D)));
                spawnMush(level, x, y, z, true);
            }
            case CONVENTIONAL -> {
                new ExplosionVnt(level, x, y, z, 35.0F)
                        .setBlockAllocator(new BlockAllocatorStandard(24))
                        .setBlockProcessor(new BlockProcessorStandard().setNoDrop())
                        .setEntityProcessor(new EntityProcessorCross(5.0D).withRangeMod(1.5F))
                        .setPlayerProcessor(new PlayerProcessorStandard()).explode();
                ParticleUtil.spawnLegacyExplosionLarge(level, x, y, z);
            }
            case NUKE -> {
                level.addFreshEntity(NukeExplosionMk5Entity.statFac(level, 35, x, y, z));
                spawnMush(level, x, y, z, level.random.nextInt(100) == 0);
            }
            case SALTED -> {
                level.addFreshEntity(NukeExplosionMk5Entity.statFac(level, 25, x, y, z).moreFallout(25));
                spawnMush(level, x, y, z, level.random.nextInt(100) == 0);
            }
        }
        return BombReturnCode.DETONATED;
    }

    @Override
    public BombReturnCode detonateFromRemote(Level level, BlockPos pos) {
        return explode(level, pos);
    }

    private static void spawnMush(Level level, double x, double y, double z, boolean balefire) {
        LegacySoundPlayer.playLegacyMukeExplosion(level, x, y, z);
        ParticleUtil.spawnMuke(level, x, y, z, balefire);
    }

    private static ItemStack[] defuserDrops(CrashedBombType type) {
        return switch (type) {
            case BALEFIRE -> new ItemStack[] {new ItemStack(ModItems.legacyItem("egg_balefire_shard").get())};
            case CONVENTIONAL -> new ItemStack[] {new ItemStack(ModItems.legacyItem("ball_tnt").get(), 16)};
            case NUKE -> new ItemStack[] {new ItemStack(ModItems.legacyItem("ball_tnt").get(), 8),
                    new ItemStack(ModItems.legacyItem("billet_plutonium").get(), 4)};
            case SALTED -> new ItemStack[] {new ItemStack(ModItems.legacyItem("ball_tnt").get(), 8),
                    new ItemStack(ModItems.legacyItem("billet_plutonium").get(), 2),
                    new ItemStack(ModItems.COBALT_INGOT.get(), 12)};
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }
}
