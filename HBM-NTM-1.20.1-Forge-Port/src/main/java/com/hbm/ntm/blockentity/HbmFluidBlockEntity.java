package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.ForgeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodeHost;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class HbmFluidBlockEntity extends BlockEntity implements HbmFluidNodeHost {
    private static final String TAG_FLUIDS = "hbm_fluids";
    private static final String TAG_TANK_PREFIX = "tank_";

    private final List<HbmFluidTank> tanks;
    private final Map<Direction, LazyOptional<IFluidHandler>> sidedFluidHandlers = new EnumMap<>(Direction.class);
    private LazyOptional<IFluidHandler> nullSideFluidHandler;
    private final Map<com.hbm.ntm.fluid.FluidType, HbmFluidNode> fluidNodes = new HashMap<>();

    protected HbmFluidBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, List<HbmFluidTank> tanks) {
        super(type, pos, state);
        this.tanks = List.copyOf(tanks);
    }

    public List<HbmFluidTank> getAllTanks() {
        return tanks;
    }

    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return tanks;
    }

    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return tanks;
    }

    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    public HbmFluidNode getFluidNode() {
        return fluidNodes.values().stream().findFirst().orElse(null);
    }

    protected void setFluidNode(HbmFluidNode node) {
        if (node != null) {
            fluidNodes.put(node.getFluidType(), node);
        }
    }

    protected HbmFluidNode getFluidNode(com.hbm.ntm.fluid.FluidType type) {
        return fluidNodes.get(type);
    }

    protected void removeFluidNode(com.hbm.ntm.fluid.FluidType type) {
        fluidNodes.remove(type);
    }

    protected Set<com.hbm.ntm.fluid.FluidType> getTrackedFluidNodeTypes() {
        return new HashSet<>(fluidNodes.keySet());
    }

    @Override
    public void refreshFluidNode() {
    }

    @Override
    public void removeFluidNode() {
        fluidNodes.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag fluids = new CompoundTag();
        for (int i = 0; i < tanks.size(); i++) {
            tanks.get(i).writeToNbt(fluids, TAG_TANK_PREFIX + i);
        }
        tag.put(TAG_FLUIDS, fluids);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        CompoundTag fluids = tag.getCompound(TAG_FLUIDS);
        for (int i = 0; i < tanks.size(); i++) {
            tanks.get(i).readFromNbt(fluids, TAG_TANK_PREFIX + i);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (LazyOptional<IFluidHandler> handler : sidedFluidHandlers.values()) {
            handler.invalidate();
        }
        sidedFluidHandlers.clear();
        if (nullSideFluidHandler != null) {
            nullSideFluidHandler.invalidate();
            nullSideFluidHandler = null;
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return getFluidHandler(side).cast();
        }
        return super.getCapability(capability, side);
    }

    protected void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private LazyOptional<IFluidHandler> getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            if (nullSideFluidHandler == null) {
                nullSideFluidHandler = createFluidHandler(null);
            }
            return nullSideFluidHandler;
        }
        return sidedFluidHandlers.computeIfAbsent(side, this::createFluidHandler);
    }

    private LazyOptional<IFluidHandler> createFluidHandler(@Nullable Direction side) {
        HbmFluidSideMode mode = getFluidSideMode(side);
        if (mode == HbmFluidSideMode.NONE) {
            return LazyOptional.empty();
        }
        List<HbmFluidTank> visibleTanks = mode == HbmFluidSideMode.INPUT ? getInputTanks(side)
                : mode == HbmFluidSideMode.OUTPUT ? getOutputTanks(side)
                : tanks;
        return LazyOptional.of(() -> new ForgeFluidHandlerAdapter(
                visibleTanks,
                getInputPressure(side),
                mode.canFill(),
                mode.canDrain(),
                this::onFluidContentsChanged));
    }

    protected int getInputPressure(@Nullable Direction side) {
        return 0;
    }
}
