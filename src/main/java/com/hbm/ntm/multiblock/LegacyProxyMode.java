package com.hbm.ntm.multiblock;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.Objects;

/**
 * Capability switches for 1.7.10 TileEntityProxyCombo-style dummy blocks.
 */
public final class LegacyProxyMode {
    private static final LegacyProxyMode NONE = new LegacyProxyMode(false, false, false, false, false, false, false, false);
    private static final LegacyProxyMode PASSIVE = new LegacyProxyMode(true, false, false, false, false, false, false, false);
    private static final LegacyProxyMode ALL = new LegacyProxyMode(true, true, true, true, true, true, true, true);

    private final boolean proxy;
    private final boolean inventory;
    private final boolean power;
    private final boolean conductor;
    private final boolean fluid;
    private final boolean heat;
    private final boolean moltenMetal;
    private final boolean allCapabilities;

    private LegacyProxyMode(boolean proxy, boolean inventory, boolean power, boolean conductor, boolean fluid,
            boolean heat, boolean moltenMetal, boolean allCapabilities) {
        this.proxy = proxy;
        this.inventory = inventory;
        this.power = power;
        this.conductor = conductor;
        this.fluid = fluid;
        this.heat = heat;
        this.moltenMetal = moltenMetal;
        this.allCapabilities = allCapabilities;
    }

    public static LegacyProxyMode none() {
        return NONE;
    }

    public static LegacyProxyMode passive() {
        return PASSIVE;
    }

    public static LegacyProxyMode all() {
        return ALL;
    }

    public static LegacyProxyMode fullCombo() {
        return combo(true, true, true);
    }

    public static LegacyProxyMode combo(boolean inventory, boolean power, boolean fluid) {
        return passive().withInventory(inventory).withPower(power).withFluid(fluid);
    }

    public boolean isProxy() {
        return proxy;
    }

    public boolean inventory() {
        return inventory;
    }

    public boolean power() {
        return power;
    }

    public boolean conductor() {
        return conductor;
    }

    public boolean fluid() {
        return fluid;
    }

    public boolean heat() {
        return heat;
    }

    public boolean moltenMetal() {
        return moltenMetal;
    }

    public boolean allCapabilities() {
        return allCapabilities;
    }

    public LegacyProxyMode inventoryProxy() {
        return withInventory(true);
    }

    public LegacyProxyMode powerProxy() {
        return withPower(true);
    }

    public LegacyProxyMode conductorProxy() {
        return withConductor(true);
    }

    public LegacyProxyMode fluidProxy() {
        return withFluid(true);
    }

    public LegacyProxyMode heatProxy() {
        return withHeat(true);
    }

    public LegacyProxyMode moltenMetalProxy() {
        return withMoltenMetal(true);
    }

    public boolean allows(Capability<?> capability) {
        if (!proxy) {
            return false;
        }
        if (allCapabilities) {
            return true;
        }
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return inventory;
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return power || conductor;
        }
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return fluid || moltenMetal;
        }
        return false;
    }

    public LegacyProxyMode withInventory(boolean value) {
        return copy(true, value, power, conductor, fluid, heat, moltenMetal, allCapabilities);
    }

    public LegacyProxyMode withPower(boolean value) {
        return copy(true, inventory, value, conductor, fluid, heat, moltenMetal, allCapabilities);
    }

    public LegacyProxyMode withConductor(boolean value) {
        return copy(true, inventory, power, value, fluid, heat, moltenMetal, allCapabilities);
    }

    public LegacyProxyMode withFluid(boolean value) {
        return copy(true, inventory, power, conductor, value, heat, moltenMetal, allCapabilities);
    }

    public LegacyProxyMode withHeat(boolean value) {
        return copy(true, inventory, power, conductor, fluid, value, moltenMetal, allCapabilities);
    }

    public LegacyProxyMode withMoltenMetal(boolean value) {
        return copy(true, inventory, power, conductor, fluid, heat, value, allCapabilities);
    }

    private LegacyProxyMode copy(boolean proxy, boolean inventory, boolean power, boolean conductor, boolean fluid,
            boolean heat, boolean moltenMetal, boolean allCapabilities) {
        if (!proxy) {
            return NONE;
        }
        if (allCapabilities) {
            return ALL;
        }
        return new LegacyProxyMode(true, inventory, power, conductor, fluid, heat, moltenMetal, false);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LegacyProxyMode other)) {
            return false;
        }
        return proxy == other.proxy
                && inventory == other.inventory
                && power == other.power
                && conductor == other.conductor
                && fluid == other.fluid
                && heat == other.heat
                && moltenMetal == other.moltenMetal
                && allCapabilities == other.allCapabilities;
    }

    @Override
    public int hashCode() {
        return Objects.hash(proxy, inventory, power, conductor, fluid, heat, moltenMetal, allCapabilities);
    }
}
