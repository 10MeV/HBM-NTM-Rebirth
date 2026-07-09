package com.hbm.items.special;

import com.hbm.ntm.item.NuclearWasteItem;
import net.minecraft.world.item.Item;

/**
 * Legacy 1.7.10 package bridge for long-lived nuclear waste metadata.
 */
@Deprecated(forRemoval = false)
public class ItemWasteLong extends NuclearWasteItem {
    public ItemWasteLong() {
        this(new Item.Properties());
    }

    public ItemWasteLong(Item.Properties properties) {
        super(properties, WasteFamily.LONG);
    }

    public static int rectify(int meta) {
        return Math.abs(meta) % WasteClass.values().length;
    }

    public enum WasteClass {
        URANIUM235("Uranium-235", 0, 0),
        URANIUM233("Uranium-233", 0, 50),
        NEPTUNIUM("Neptunium-237", 0, 100),
        THORIUM("Thorium-232", 0, 0),
        SCHRABIDIUM("Schrabidium-326", 0, 250);

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
