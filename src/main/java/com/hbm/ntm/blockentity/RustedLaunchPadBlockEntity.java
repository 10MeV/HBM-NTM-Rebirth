package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.item.DesignatorItem;
import com.hbm.ntm.block.RustedLaunchPadBlock;
import com.hbm.ntm.entity.missile.MissileEntity;
import com.hbm.ntm.menu.RustedLaunchPadMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RustedLaunchPadBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyButtonReceiver {
    public static final int SLOT_RELEASED_MISSILE = 0;
    public static final int SLOT_CODE = 1;
    public static final int SLOT_KEY = 2;
    public static final int SLOT_DESIGNATOR = 3;
    public static final int SLOT_COUNT = 4;
    public static final int BUTTON_RELEASE = 0;

    private static final String TAG_MISSILE_LOADED = "missileLoaded";
    private static final String TAG_REDSTONE = "redstonePower";
    private static final String TAG_PREV_REDSTONE = "prevRedstonePower";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_RELEASED_MISSILE -> false;
                case SLOT_CODE -> stack.is(ModItems.LAUNCH_CODE.get());
                case SLOT_KEY -> stack.is(ModItems.LAUNCH_KEY.get());
                case SLOT_DESIGNATOR -> stack.getItem() instanceof DesignatorItem || hasLegacyDesignatorCoords(stack);
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private boolean missileLoaded;
    private boolean redstonePowered;
    private boolean prevRedstonePowered;

    public RustedLaunchPadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCH_PAD_RUSTED.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RustedLaunchPadBlockEntity launchPad) {
        if (level.isClientSide) {
            return;
        }
        boolean oldRedstone = launchPad.redstonePowered;
        launchPad.redstonePowered = launchPad.isPowered(level, pos, state);
        if (launchPad.redstonePowered && !launchPad.prevRedstonePowered) {
            launchPad.launch();
        }
        launchPad.prevRedstonePowered = launchPad.redstonePowered;
        if (oldRedstone != launchPad.redstonePowered) {
            launchPad.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        launchPad.networkPackNT();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, RustedLaunchPadBlockEntity launchPad) {
        if (!level.isClientSide) {
            return;
        }
        if (level.getEntitiesOfClass(MissileEntity.class,
                        new net.minecraft.world.phys.AABB(pos.getX() - 0.5D, pos.getY(), pos.getZ() - 0.5D,
                                pos.getX() + 1.5D, pos.getY() + 10.0D, pos.getZ() + 1.5D)).isEmpty()) {
            return;
        }
        Direction facing = state.hasProperty(RustedLaunchPadBlock.FACING)
                ? state.getValue(RustedLaunchPadBlock.FACING)
                : Direction.NORTH;
        ParticleUtil.spawnLaunchPadSmokeBurst(level, pos, facing, false);
    }

    private boolean isPowered(Level level, BlockPos pos, BlockState state) {
        if (level.hasNeighborSignal(pos)) {
            return true;
        }
        if (state.getBlock() instanceof RustedLaunchPadBlock block) {
            for (BlockPos offset : block.getMultiblockLayout(state, level, pos).offsets()) {
                if (!offset.equals(BlockPos.ZERO) && level.hasNeighborSignal(pos.offset(offset))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean launch() {
        if (!(level instanceof ServerLevel serverLevel) || !canLaunch()) {
            return false;
        }
        BlockPos target = designatorTarget(items.getStackInSlot(SLOT_DESIGNATOR));
        if (target == null) {
            return false;
        }
        MissileEntity missile = new MissileEntity(ModEntityTypes.MISSILE_DOOMSDAY_RUSTED.get(), serverLevel,
                MissileEntity.Variant.DOOMSDAY_RUSTED);
        missile.configureLaunch(worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D,
                worldPosition.getZ() + 0.5D, target.getX(), target.getZ());
        serverLevel.addFreshEntity(missile);
        LegacySoundPlayer.playSoundEffect(serverLevel, worldPosition.getX() + 0.5D, worldPosition.getY(),
                worldPosition.getZ() + 0.5D, "hbm:weapon.missileTakeOff", SoundSource.PLAYERS, 2.0F, 1.0F);
        missileLoaded = false;
        items.extractItem(SLOT_CODE, 1, false);
        setChangedAndUpdate();
        return true;
    }

    public boolean canLaunch() {
        return missileLoaded
                && items.getStackInSlot(SLOT_CODE).is(ModItems.LAUNCH_CODE.get())
                && items.getStackInSlot(SLOT_KEY).is(ModItems.LAUNCH_KEY.get())
                && designatorTarget(items.getStackInSlot(SLOT_DESIGNATOR)) != null;
    }

    public void releaseMissile() {
        if (!missileLoaded || !items.getStackInSlot(SLOT_RELEASED_MISSILE).isEmpty()) {
            return;
        }
        missileLoaded = false;
        items.setStackInSlot(SLOT_RELEASED_MISSILE, new ItemStack(ModItems.MISSILE_DOOMSDAY_RUSTED.get()));
        setChangedAndUpdate();
    }

    @Nullable
    private BlockPos designatorTarget(ItemStack stack) {
        if (level != null && stack.getItem() instanceof DesignatorItem designator
                && designator.isReady(level, stack, worldPosition)) {
            Vec3 coords = designator.getCoords(level, stack, worldPosition);
            return BlockPos.containing(coords.x, worldPosition.getY(), coords.z);
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("xCoord") && tag.contains("zCoord")) {
            return new BlockPos(tag.getInt("xCoord"), worldPosition.getY(), tag.getInt("zCoord"));
        }
        return null;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public boolean isMissileLoaded() {
        return missileLoaded;
    }

    public void setMissileLoaded(boolean missileLoaded) {
        this.missileLoaded = missileLoaded;
        setChangedAndUpdate();
    }

    public boolean hasLaunchCode() {
        return items.getStackInSlot(SLOT_CODE).is(ModItems.LAUNCH_CODE.get());
    }

    public boolean hasLaunchKey() {
        return items.getStackInSlot(SLOT_KEY).is(ModItems.LAUNCH_KEY.get());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.hbm_ntm_rebirth.launch_pad_rusted", "Old Launch Pad");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RustedLaunchPadMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == BUTTON_RELEASE;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == BUTTON_RELEASE) {
            releaseMissile();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        tag.putBoolean(TAG_MISSILE_LOADED, missileLoaded);
        tag.putBoolean(TAG_REDSTONE, redstonePowered);
        tag.putBoolean(TAG_PREV_REDSTONE, prevRedstonePowered);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        missileLoaded = tag.getBoolean(TAG_MISSILE_LOADED);
        redstonePowered = tag.getBoolean(TAG_REDSTONE);
        prevRedstonePowered = tag.getBoolean(TAG_PREV_REDSTONE);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(worldPosition).inflate(16.0D);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private void networkPackNT() {
        if (level != null && level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static boolean hasLegacyDesignatorCoords(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("xCoord") && tag.contains("zCoord");
    }
}
