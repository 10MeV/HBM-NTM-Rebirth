package com.hbm.ntm.block;

import com.hbm.ntm.api.block.ChainExplodable;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.entity.item.LegacyPrimedExplosiveEntity;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class LegacyTntBaseBlock extends Block implements ChainExplodable, Toolable {
    public static final BooleanProperty IGNITE_ON_BREAK = BooleanProperty.create("ignite_on_break");

    private static final int PRIMED_FUSE = 80;
    private static final int POP_FUSE_WINDOW = 20;

    private final Kind kind;

    public LegacyTntBaseBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(IGNITE_ON_BREAK, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IGNITE_ON_BREAK);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!oldState.is(state.getBlock()) && !level.isClientSide) {
            checkAndIgnite(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (!level.isClientSide) {
            checkAndIgnite(level, pos);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && state.getValue(IGNITE_ON_BREAK)) {
            primeAt(level, pos, player);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(IGNITE_ON_BREAK)) {
            return Collections.emptyList();
        }
        return super.getDrops(state, params);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.FLINT_AND_STEEL)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            primeAt(level, pos, player);
            if (!player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(hand));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof AbstractArrow arrow && arrow.isOnFire() && !level.isClientSide) {
            primeAt(level, pos, arrow.getOwner());
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (projectile.isOnFire() && !level.isClientSide) {
            primeAt(level, hit.getBlockPos(), projectile.getOwner());
        }
        super.onProjectileHit(level, state, hit, projectile);
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face,
            @Nullable LivingEntity igniter) {
        if (!level.isClientSide && shouldIgnite(level, pos)) {
            primeAt(level, pos, igniter);
        }
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        level.removeBlock(pos, false);
        if (!level.isClientSide) {
            level.addFreshEntity(LegacyPrimedExplosiveEntity.create(level,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, this, POP_FUSE_WINDOW, false,
                    explosion.getIndirectSourceEntity()));
        }
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 15;
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool == ToolType.DEFUSER) {
            if (!level.isClientSide) {
                level.removeBlock(pos, false);
                popResource(level, pos, new ItemStack(this));
            }
            return true;
        }
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }
        if (!level.isClientSide) {
            BlockState state = level.getBlockState(pos);
            boolean enabled = !state.getValue(IGNITE_ON_BREAK);
            level.setBlock(pos, state.setValue(IGNITE_ON_BREAK, enabled), 3);
            player.displayClientMessage(Component.literal(enabled
                    ? "[ Ignite On Break: Enabled ]"
                    : "[ Ignite On Break: Disabled ]").withStyle(enabled ? ChatFormatting.RED : ChatFormatting.GOLD), false);
        }
        return true;
    }

    @Override
    public void explodeEntity(Level level, Vec3 position, @Nullable Entity source) {
        if (!level.isClientSide) {
            level.explode(source, position.x, position.y, position.z, kind.power(), false, Level.ExplosionInteraction.BLOCK);
        }
    }

    private void checkAndIgnite(Level level, BlockPos pos) {
        if (level.hasNeighborSignal(pos) || shouldIgnite(level, pos)) {
            primeAt(level, pos, null);
        }
    }

    private boolean shouldIgnite(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(Blocks.FIRE)) {
                return true;
            }
        }
        return false;
    }

    private void primeAt(Level level, BlockPos pos, @Nullable Entity owner) {
        if (level.getBlockState(pos).getBlock() != this) {
            return;
        }
        level.removeBlock(pos, false);
        level.gameEvent(owner, GameEvent.PRIME_FUSE, pos);
        LegacyPrimedExplosiveEntity entity = LegacyPrimedExplosiveEntity.createFixedFuse(level,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, this, PRIMED_FUSE, false,
                owner instanceof LivingEntity livingOwner ? livingOwner : null);
        level.addFreshEntity(entity);
        level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public enum Kind {
        DYNAMITE(8.0F),
        TNT(10.0F),
        SEMTEX(12.0F),
        C4(15.0F);

        private final float power;

        Kind(float power) {
            this.power = power;
        }

        public float power() {
            return power;
        }
    }
}
