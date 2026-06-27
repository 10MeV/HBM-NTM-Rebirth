package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.menu.DfcInjectorMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class DfcInjectorBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver {
    public static final int SLOT_TANK0_ID = 0;
    public static final int SLOT_TANK0_OUT = 1;
    public static final int SLOT_TANK1_ID = 2;
    public static final int SLOT_TANK1_OUT = 3;
    public static final int SLOT_COUNT = 4;
    public static final int RANGE = 15;

    private static final String TAG_ITEMS = "items";
    private final HbmFluidTank fuel1;
    private final HbmFluidTank fuel2;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private int beam;

    public DfcInjectorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.DEUTERIUM, DfcCoreBlockEntity.TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.TRITIUM, DfcCoreBlockEntity.TANK_CAPACITY));
    }

    private DfcInjectorBlockEntity(BlockPos pos, BlockState state, HbmFluidTank fuel1, HbmFluidTank fuel2) {
        super(ModBlockEntities.DFC_INJECTOR.get(), pos, state, List.of(fuel1, fuel2));
        this.fuel1 = fuel1;
        this.fuel2 = fuel2;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DfcInjectorBlockEntity injector) {
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, injector);
        injector.tickServer(level, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DfcInjectorBlockEntity injector) {
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        HbmFluidItemTransfer.setTankTypeFromIdentifierSlot(items, SLOT_TANK0_ID, SLOT_TANK0_OUT, fuel1, level, pos);
        HbmFluidItemTransfer.setTankTypeFromIdentifierSlot(items, SLOT_TANK1_ID, SLOT_TANK1_OUT, fuel2, level, pos);
        beam = 0;
        Direction direction = facing();
        for (int i = 1; i <= RANGE; i++) {
            BlockPos target = pos.relative(direction, i);
            BlockEntity targetEntity = level.getBlockEntity(target);
            if (targetEntity instanceof DfcCoreBlockEntity core) {
                transferToCore(core.getFuel1(), fuel1);
                transferToCore(core.getFuel2(), fuel2);
                beam = i;
                break;
            }
            if (!level.getBlockState(target).isAir()) {
                break;
            }
        }
        networkPackNT(250);
        setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    private static void transferToCore(HbmFluidTank coreTank, HbmFluidTank injectorTank) {
        if (injectorTank.getTankType() == HbmFluids.NONE) {
            return;
        }
        if (coreTank.getTankType() == injectorTank.getTankType() || coreTank.getFill() == 0) {
            if (coreTank.getFill() == 0) {
                coreTank.setTankType(injectorTank.getTankType());
            }
            int moved = Math.min(injectorTank.getFill(), coreTank.getMaxFill() - coreTank.getFill());
            injectorTank.setFill(injectorTank.getFill() - moved);
            coreTank.setFill(coreTank.getFill() + moved);
        }
    }

    private Direction facing() {
        return getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
    }

    public ItemStackHandler getItems() { return items; }
    public HbmFluidTank getFuel1() { return fuel1; }
    public HbmFluidTank getFuel2() { return fuel2; }
    public int getBeam() { return beam; }
    public List<ItemStack> getDrops() { return HbmInventoryMenuHelper.clearToDrops(items); }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.dfcInjector", "DFC Injector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DfcInjectorMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(fuel1, fuel2);
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-RANGE, -RANGE, -RANGE), worldPosition.offset(RANGE + 1, RANGE + 1, RANGE + 1));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putInt("beam", beam);
        fuel1.writeToNbt(tag, "fuel1");
        fuel2.writeToNbt(tag, "fuel2");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        beam = tag.getInt("beam");
        fuel1.readFromNbt(tag, "fuel1");
        fuel2.readFromNbt(tag, "fuel2");
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
