package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.NuclearDeviceBlockEntity;
import com.hbm.ntm.explosion.NuclearExplosionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class NuclearDeviceBlock extends HorizontalMachineBlock implements EntityBlock {
    private final Kind kind;

    public NuclearDeviceBlock(Properties properties, Kind kind) {
        super(properties, false);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NuclearDeviceBlockEntity(pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (!level.isClientSide() && level.hasNeighborSignal(pos)) {
            detonateArmed(level, pos);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!oldState.is(state.getBlock()) && !level.isClientSide() && level.hasNeighborSignal(pos)) {
            detonateArmed(level, pos);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        MenuProvider menuProvider = getMenuProvider(state, level, pos);
        if (!level.isClientSide && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer
                && menuProvider != null) {
            NetworkHooks.openScreen(serverPlayer, menuProvider, pos);
        }
        return player.isShiftKeyDown() ? InteractionResult.PASS : InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof NuclearDeviceBlockEntity device ? device : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof NuclearDeviceBlockEntity device) {
            for (ItemStack stack : device.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public boolean detonateArmed(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() != this) {
            return false;
        }
        if (!(level.getBlockEntity(pos) instanceof NuclearDeviceBlockEntity device) || !device.isReady()) {
            return false;
        }

        Kind detonationKind = device.detonationKind();
        device.clearSlots();
        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                1.0F, 0.9F + level.random.nextFloat() * 0.1F);
        level.gameEvent(null, GameEvent.EXPLODE, pos);
        return detonationKind.detonate(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    public enum Kind {
        GADGET(6) {
            @Override
            boolean detonate(Level level, double x, double y, double z) {
                return NuclearExplosionUtil.spawnGadget(level, x, y, z);
            }
        },
        BOY(5) {
            @Override
            boolean detonate(Level level, double x, double y, double z) {
                return NuclearExplosionUtil.spawnBoy(level, x, y, z);
            }
        },
        MAN(6) {
            @Override
            boolean detonate(Level level, double x, double y, double z) {
                return NuclearExplosionUtil.spawnMan(level, x, y, z);
            }
        },
        TSAR(6) {
            @Override
            boolean detonate(Level level, double x, double y, double z) {
                return NuclearExplosionUtil.spawnTsar(level, x, y, z);
            }
        },
        MIKE(8) {
            @Override
            boolean detonate(Level level, double x, double y, double z) {
                return NuclearExplosionUtil.spawnMike(level, x, y, z);
            }
        },
        PROTOTYPE(14) {
            @Override
            boolean detonate(Level level, double x, double y, double z) {
                return NuclearExplosionUtil.spawnPrototype(level, x, y, z);
            }
        },
        FLEIJA(11) {
            @Override
            boolean detonate(Level level, double x, double y, double z) {
                return NuclearExplosionUtil.spawnFleijaBomb(level, x, y, z);
            }
        },
        SOLINIUM(9) {
            @Override
            boolean detonate(Level level, double x, double y, double z) {
                return NuclearExplosionUtil.spawnSoliniumBomb(level, x, y, z);
            }
        },
        N2(12) {
            @Override
            boolean detonate(Level level, double x, double y, double z) {
                return NuclearExplosionUtil.spawnN2(level, x, y, z);
            }
        };

        private final int slots;

        Kind(int slots) {
            this.slots = slots;
        }

        public int slots() {
            return slots;
        }

        abstract boolean detonate(Level level, double x, double y, double z);
    }
}
