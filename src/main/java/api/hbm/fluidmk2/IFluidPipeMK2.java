package api.hbm.fluidmk2;

/**
 * Legacy 1.7.10 package bridge for Fluid MK2 pipe node creation.
 */
@Deprecated(forRemoval = false)
public interface IFluidPipeMK2 extends com.hbm.ntm.api.fluid.IFluidPipeMK2 {
    @Override
    default FluidNode createNode(com.hbm.ntm.fluid.FluidType type) {
        com.hbm.ntm.api.fluid.FluidNode node = com.hbm.ntm.api.fluid.IFluidPipeMK2.super.createNode(type);
        return new FluidNode(node.getFluidType(), node.getPositions(), node.getConnections());
    }

    @Override
    default FluidNode createNode(com.hbm.ntm.fluid.FluidType type, net.minecraft.world.level.Level level,
            net.minecraft.core.BlockPos pos) {
        com.hbm.ntm.api.fluid.FluidNode node =
                com.hbm.ntm.api.fluid.IFluidPipeMK2.super.createNode(type, level, pos);
        return new FluidNode(node.getFluidType(), node.getPositions(), node.getConnections());
    }
}
