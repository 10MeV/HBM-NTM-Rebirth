package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.CustomMissileLauncherBlock;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.item.missile.CustomMissilePartProfile.PartSize;
import com.hbm.ntm.menu.LaunchTableMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LaunchTableBlockEntity extends CustomMissileLauncherBlockEntity implements HbmLegacyButtonReceiver {
    private static final int TANK_CAPACITY = 100_000;
    private static final int MAX_SOLID = 100_000;
    private static final List<FluidPort> FLUID_PORTS = ports(5, false);
    private static final List<EnergyPort> ENERGY_PORTS = energyPorts(5, false);

    public LaunchTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCH_TABLE.get(), pos, state, TANK_CAPACITY, PartSize.SIZE_10);
    }

    @Override
    protected CustomMissileLauncherBlock.Kind kind() {
        return CustomMissileLauncherBlock.Kind.LAUNCH_TABLE;
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
        return 4;
    }

    @Override
    protected PartSize defaultPadSize() {
        return PartSize.SIZE_10;
    }

    @Override
    protected boolean requiresDesignatorForCanLaunch() {
        return false;
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
        return Component.translatableWithFallback("container.hbm_ntm_rebirth.launch_table", "Custom Launch Pad");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new LaunchTableMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == 0 && value >= PartSize.SIZE_10.ordinal() && value <= PartSize.SIZE_20.ordinal()
                && player.distanceToSqr(worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        setPadSize(PartSize.values()[value]);
    }

    private static List<FluidPort> ports(int offset, boolean includeBottom) {
        List<FluidPort> ports = new ArrayList<>();
        for (int i = -4; i <= 4; i++) {
            ports.add(new FluidPort(new BlockPos(i, 0, offset), Direction.SOUTH));
            ports.add(new FluidPort(new BlockPos(i, 0, -offset), Direction.NORTH));
            ports.add(new FluidPort(new BlockPos(offset, 0, i), Direction.EAST));
            ports.add(new FluidPort(new BlockPos(-offset, 0, i), Direction.WEST));
        }
        return ports;
    }

    private static List<EnergyPort> energyPorts(int offset, boolean includeBottom) {
        List<EnergyPort> ports = new ArrayList<>();
        for (FluidPort port : ports(offset, includeBottom)) {
            ports.add(new EnergyPort(port.offset(), port.direction()));
        }
        return ports;
    }
}
