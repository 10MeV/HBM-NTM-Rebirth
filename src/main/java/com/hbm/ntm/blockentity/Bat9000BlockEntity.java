package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Bat9000BlockEntity extends LegacyBigTankBlockEntity {
    private static final int TANK_CAPACITY = 2_048_000;
    private static final List<FluidPort> FLUID_PORTS = List.of(
            FluidPort.of(1, 0, 3, Direction.SOUTH),
            FluidPort.of(-1, 0, 3, Direction.SOUTH),
            FluidPort.of(1, 0, -3, Direction.NORTH),
            FluidPort.of(-1, 0, -3, Direction.NORTH),
            FluidPort.of(3, 0, 1, Direction.EAST),
            FluidPort.of(-3, 0, 1, Direction.WEST),
            FluidPort.of(3, 0, -1, Direction.EAST),
            FluidPort.of(-3, 0, -1, Direction.WEST));

    /** Full six-slot inventory for the menu and internal callers (Forge side == null). */
    private final LazyOptional<IItemHandler> internalItemHandler = LazyOptional.of(this::getItems);

    public Bat9000BlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, ModBlockEntities.BAT9000.get(), new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
    }

    @Override
    protected boolean isValidForInternalItemSlot(int slot, ItemStack stack) {
        // TileEntityBarrel#isItemValidForSlot: only the two fluid-container
        // input slots accept insertion; identifier/output slots are locked.
        return getTankContainerAutomationItemHandler().isItemValid(slot, stack);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        internalItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && side == null) {
            return internalItemHandler.cast();
        }
        // Non-null sides use LegacyBigTankBlockEntity's external adapter:
        // insertion is limited to slots 2/4 and extraction to product slots 3/5.
        return super.getCapability(capability, side);
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.bat9000");
    }
}
