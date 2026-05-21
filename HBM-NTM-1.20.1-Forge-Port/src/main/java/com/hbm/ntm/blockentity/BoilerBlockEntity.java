package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BoilerBlockEntity extends HbmFluidBlockEntity {
    public static final int FEED_TANK = 0;
    public static final int STEAM_TANK = 1;

    private final HbmFluidTank feedTank;
    private final HbmFluidTank steamTank;

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
}
