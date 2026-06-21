package com.hbm.ntm.block;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.NuclearDeviceBlockEntity;
import com.hbm.ntm.explosion.NuclearExplosionUtil;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
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
public class NuclearDeviceBlock extends HorizontalMachineBlock implements EntityBlock, RemoteDetonatableBlock {
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
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (kind == Kind.PROTOTYPE && player.getItemInHand(hand).is(ModItems.legacyItem("igniter").get())) {
            if (!level.isClientSide()) {
                detonateArmed(level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MenuProvider menuProvider = getOrCreateMenuProvider(state, level, pos);
            if (menuProvider != null) {
                HbmNtm.LOGGER.info("Opening nuclear device menu at {} for {} using {}.",
                        pos, serverPlayer.getGameProfile().getName(), menuProvider.getClass().getSimpleName());
                NetworkHooks.openScreen(serverPlayer, menuProvider, buffer -> {
                    buffer.writeBlockPos(pos);
                    buffer.writeVarInt(kind.ordinal());
                });
            } else {
                HbmNtm.LOGGER.warn("Could not open nuclear device menu at {} because no block entity was available for {}.",
                        pos, state);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return getOrCreateMenuProvider(state, level, pos);
    }

    @Nullable
    private MenuProvider getOrCreateMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof NuclearDeviceBlockEntity device) {
            return device;
        }
        if (!level.isClientSide && state.getBlock() == this) {
            BlockEntity created = newBlockEntity(pos, state);
            if (created instanceof NuclearDeviceBlockEntity device) {
                level.setBlockEntity(device);
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                return device;
            }
        }
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof NuclearDeviceBlockEntity device) {
            device.spillDrops(level, pos);
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
        boolean spawned = detonationKind.detonate(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        if (spawned) {
            LegacySoundPlayer.playSoundEffect(level, pos.getX(), pos.getY(), pos.getZ(), "random.explode",
                    1.0F, 0.9F + level.random.nextFloat() * 0.1F);
            level.gameEvent(null, GameEvent.EXPLODE, pos);
        }
        return true;
    }

    @Override
    public BombReturnCode detonateFromRemote(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return BombReturnCode.UNDEFINED;
        }
        if (level.getBlockState(pos).getBlock() != this) {
            return BombReturnCode.ERROR_NO_BOMB;
        }
        if (!(level.getBlockEntity(pos) instanceof NuclearDeviceBlockEntity device) || !device.isReady()) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }
        return detonateArmed(level, pos) ? BombReturnCode.DETONATED : BombReturnCode.ERROR_INCOMPATIBLE;
    }

    public static float legacyRenderYaw(Kind kind, Direction facing) {
        return switch (kind) {
            case GADGET -> switch (facing) {
                case NORTH -> 270.0F;
                case EAST -> 180.0F;
                case SOUTH -> 90.0F;
                case WEST -> 0.0F;
                default -> 90.0F;
            };
            case BOY, TSAR -> switch (facing) {
                case NORTH -> 0.0F;
                case EAST -> 270.0F;
                case SOUTH -> 180.0F;
                case WEST -> 90.0F;
                default -> 180.0F;
            };
            case MAN -> switch (facing) {
                case NORTH -> 180.0F;
                case EAST -> 90.0F;
                case SOUTH -> 0.0F;
                case WEST -> 270.0F;
                default -> 0.0F;
            };
            case MIKE, PROTOTYPE, FLEIJA -> switch (facing) {
                case NORTH -> 90.0F;
                case EAST -> 0.0F;
                case SOUTH -> 270.0F;
                case WEST -> 180.0F;
                default -> 270.0F;
            };
            case SOLINIUM, N2 -> switch (facing) {
                case NORTH -> 90.0F;
                case EAST -> 0.0F;
                case SOUTH -> 270.0F;
                case WEST -> 180.0F;
                default -> 270.0F;
            };
        };
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
