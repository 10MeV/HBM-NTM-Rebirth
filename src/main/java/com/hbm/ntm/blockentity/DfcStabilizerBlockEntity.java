package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.item.AmsLensItem;
import com.hbm.ntm.menu.DfcStabilizerMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DfcStabilizerBlockEntity extends HbmEnergyBlockEntity
        implements MenuProvider, HbmLegacyButtonReceiver {
    public static final int SLOT_LENS = 0;
    public static final int SLOT_COUNT = 1;
    public static final long MAX_POWER = 2_500_000_000L;
    public static final int RANGE = 15;
    public static final int CONTROL_SET_WATTS = 0;

    private static final String TAG_ITEMS = "items";
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof AmsLensItem;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private int watts = 1;
    private int beam;

    public DfcStabilizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DFC_STABILIZER.get(), pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DfcStabilizerBlockEntity stabilizer) {
        stabilizer.subscribeEnergyReceiverToAllSides();
        stabilizer.tickServer(level, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DfcStabilizerBlockEntity stabilizer) {
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        watts = Mth.clamp(watts, 1, 100);
        long demand = (long) Math.pow(watts, 4);
        beam = 0;
        ItemStack lens = items.getStackInSlot(SLOT_LENS);
        if (energy.getPower() >= demand && lens.getItem() instanceof AmsLensItem lensItem
                && AmsLensItem.getLensDamage(lens) < lensItem.maxLensDamage()) {
            Direction direction = facing();
            for (int i = 1; i <= RANGE; i++) {
                BlockPos target = pos.relative(direction, i);
                BlockEntity targetEntity = level.getBlockEntity(target);
                if (targetEntity instanceof DfcCoreBlockEntity core) {
                    core.applyField(watts);
                    energy.setPower(energy.getPower() - demand);
                    beam = i;
                    long damage = AmsLensItem.getLensDamage(lens) + watts;
                    if (damage >= lensItem.maxLensDamage()) {
                        items.setStackInSlot(SLOT_LENS, ItemStack.EMPTY);
                    } else {
                        AmsLensItem.setLensDamage(lens, damage);
                    }
                    break;
                }
                if (!level.getBlockState(target).isAir()) {
                    break;
                }
            }
        }
        networkPackNT(250);
        setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    private Direction facing() {
        return getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_SET_WATTS) {
            watts = Mth.clamp(value, 1, 100);
            setChanged();
        }
    }

    public ItemStackHandler getItems() { return items; }
    public int getWatts() { return watts; }
    public int getBeam() { return beam; }
    public int getWattsScaled(int width) { return watts * width / 100; }
    public List<ItemStack> getDrops() { return HbmInventoryMenuHelper.clearToDrops(items); }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.dfcStabilizer", "DFC Stabilizer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DfcStabilizerMenu(containerId, inventory, this);
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-RANGE, -RANGE, -RANGE), worldPosition.offset(RANGE + 1, RANGE + 1, RANGE + 1));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putInt("watts", watts);
        tag.putInt("beam", beam);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        watts = tag.getInt("watts");
        beam = tag.getInt("beam");
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
}
