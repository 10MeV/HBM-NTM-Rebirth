package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidThermalExchange;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingType;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BoilerBlockEntity extends HbmFluidNetworkBlockEntity implements HbmStandardFluidReceiver, HbmStandardFluidSender {
    public static final int FEED_TANK = 0;
    public static final int STEAM_TANK = 1;

    private final HbmFluidTank feedTank;
    private final HbmFluidTank steamTank;
    private int heat;

    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        this(
                pos,
                state,
                new HbmFluidTank(HbmFluids.WATER, 16_000),
                new HbmFluidTank(HbmFluids.STEAM, 16_000)
        );
    }

    private BoilerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank feedTank, HbmFluidTank steamTank) {
        super(ModBlockEntities.BOILER.get(), pos, state, List.of(feedTank, steamTank));
        this.feedTank = feedTank;
        this.steamTank = steamTank;
        this.feedTank.conform(new HbmFluidStack(HbmFluids.WATER, 0));
        this.steamTank.conform(new HbmFluidStack(HbmFluids.STEAM, 0));
    }

    public HbmFluidTank getFeedTank() {
        return feedTank;
    }

    public HbmFluidTank getSteamTank() {
        return steamTank;
    }

    public int getHeat() {
        return heat;
    }

    public void addHeat(int heat) {
        this.heat = Math.max(0, this.heat + Math.max(0, heat));
    }

    public HbmFluidThermalExchange.ThermalResult previewBoiling() {
        return HbmFluidThermalExchange.heat(feedTank, steamTank, HeatingType.BOILER, heat, true);
    }

    public HbmFluidThermalExchange.ThermalResult tryBoil(int availableHeat, boolean simulate) {
        HbmFluidThermalExchange.ThermalResult result = HbmFluidThermalExchange.heat(feedTank, steamTank, HeatingType.BOILER, availableHeat, simulate);
        if (!simulate && result.converted()) {
            heat = Math.max(0, heat - result.heatUsed());
            onFluidContentsChanged();
        }
        return result;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BoilerBlockEntity blockEntity) {
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, blockEntity);
        HbmFluidThermalExchange.ThermalResult result = blockEntity.tryBoil(blockEntity.heat, false);
        if (result.converted()) {
        } else if (blockEntity.heat > 0) {
            blockEntity.heat = Math.max(blockEntity.heat - Math.max(blockEntity.heat / 1000, 1), 0);
        }
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(feedTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(steamTank);
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == HbmFluids.WATER;
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == HbmFluids.STEAM;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(feedTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(steamTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        if (side == null) {
            return HbmFluidSideMode.BOTH;
        }
        return side == Direction.DOWN ? HbmFluidSideMode.OUTPUT : HbmFluidSideMode.INPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("heat", heat);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        heat = Math.max(0, tag.getInt("heat"));
    }
}
