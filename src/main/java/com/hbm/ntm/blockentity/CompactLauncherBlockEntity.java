package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.CustomMissileLauncherBlock;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.item.missile.CustomMissilePartProfile.PartSize;
import com.hbm.ntm.menu.CompactLauncherMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CompactLauncherBlockEntity extends CustomMissileLauncherBlockEntity {
    private static final int TANK_CAPACITY = 25_000;
    private static final int MAX_SOLID = 25_000;
    private static final List<FluidPort> FLUID_PORTS = List.of(
            new FluidPort(new BlockPos(2, 0, 1), Direction.EAST),
            new FluidPort(new BlockPos(2, 0, -1), Direction.EAST),
            new FluidPort(new BlockPos(-2, 0, 1), Direction.WEST),
            new FluidPort(new BlockPos(-2, 0, -1), Direction.WEST),
            new FluidPort(new BlockPos(1, 0, 2), Direction.SOUTH),
            new FluidPort(new BlockPos(-1, 0, 2), Direction.SOUTH),
            new FluidPort(new BlockPos(1, 0, -2), Direction.NORTH),
            new FluidPort(new BlockPos(-1, 0, -2), Direction.NORTH),
            new FluidPort(new BlockPos(1, -1, 1), Direction.DOWN),
            new FluidPort(new BlockPos(1, -1, -1), Direction.DOWN),
            new FluidPort(new BlockPos(-1, -1, 1), Direction.DOWN),
            new FluidPort(new BlockPos(-1, -1, -1), Direction.DOWN));
    private static final List<EnergyPort> ENERGY_PORTS = FLUID_PORTS.stream()
            .map(port -> new EnergyPort(port.offset(), port.direction()))
            .toList();

    public CompactLauncherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPACT_LAUNCHER.get(), pos, state, TANK_CAPACITY, PartSize.SIZE_10);
    }

    @Override
    protected CustomMissileLauncherBlock.Kind kind() {
        return CustomMissileLauncherBlock.Kind.COMPACT_LAUNCHER;
    }

    @Override
    protected int tankCapacity() {
        return TANK_CAPACITY;
    }

    @Override
    protected int maxSolidFuel() {
        return MAX_SOLID;
    }

    @Override
    protected int redstoneRadius() {
        return 1;
    }

    @Override
    protected PartSize defaultPadSize() {
        return PartSize.SIZE_10;
    }

    @Override
    protected List<FluidPort> fluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    protected List<EnergyPort> energyPorts() {
        return ENERGY_PORTS;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.hbm_ntm_rebirth.compact_launcher", "Compact Launcher");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CompactLauncherMenu(containerId, inventory, this);
    }
}
