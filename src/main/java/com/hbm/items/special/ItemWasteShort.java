package com.hbm.items.special;

import com.hbm.ntm.item.NuclearWasteItem;
import net.minecraft.world.item.Item;

/**
 * Legacy 1.7.10 package bridge for short-lived nuclear waste metadata.
 */
@Deprecated(forRemoval = false)
public class ItemWasteShort extends NuclearWasteItem {
    public ItemWasteShort() {
        this(new Item.Properties());
    }

    public ItemWasteShort(Item.Properties properties) {
        super(properties, WasteFamily.SHORT);
    }

    public static int rectify(int meta) {
        return Math.abs(meta) % WasteClass.values().length;
    }

    public enum WasteClass {
        URANIUM235("Uranium-235", 0, 100),
        URANIUM233("Uranium-233", 50, 100),
        NEPTUNIUM("Neptunium-237", 150, 500),
        PLUTONIUM239("Plutonium-239", 250, 1000),
        PLUTONIUM240("Plutonium-240", 350, 1000),
        PLUTONIUM241("Plutonium-241", 500, 1000),
        AMERICIUM242("Americium-242", 750, 1000),
        SCHRABIDIUM("Schrabidium-326", 1000, 1000);

        public String name;
        public int liquid;
        public int gas;

        WasteClass(String name, int liquid, int gas) {
            this.name = name;
            this.liquid = liquid;
            this.gas = gas;
        }
    }
}
