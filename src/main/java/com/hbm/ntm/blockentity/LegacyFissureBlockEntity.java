package com.hbm.ntm.blockentity;

import api.hbm.fluid.IFluidStandardSender;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Direct Fluid MK2 lava sender preserved from {@code BlockFissure.TileEntityFissure}. */
public final class LegacyFissureBlockEntity extends BlockEntity implements IFluidStandardSender {
    public static final int LAVA_CAPACITY = 1_000;
    private final FluidTank lava = new FluidTank(HbmFluids.LAVA, LAVA_CAPACITY);
    private final List<HbmFluidTank> tanks = List.of(lava);

    public LegacyFissureBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_FISSURE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LegacyFissureBlockEntity fissure) {
        fissure.tickServer(level, pos);
    }

    private void tickServer(Level level, BlockPos pos) {
        // The old tile refilled before every send attempt, so its 1,000 mB
        // tank is an infinite one-tick source rather than stored state.
        lava.setFill(LAVA_CAPACITY);
        sendFluid(lava, level, pos.above(), Direction.UP);
    }

    /** 1.7.10 TileEntityFissure#canConnect contract. */
    public boolean canConnect(FluidType type, Direction side) {
        return type == HbmFluids.LAVA && side == Direction.DOWN;
    }

    public FluidTank lavaTank() {
        return lava;
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return tanks;
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return tanks;
    }
}
