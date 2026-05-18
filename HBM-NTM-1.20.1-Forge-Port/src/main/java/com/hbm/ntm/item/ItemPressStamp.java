package com.hbm.ntm.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPressStamp extends Item {
    private final StampType stampType;

    public ItemPressStamp(Properties properties, StampType stampType) {
        super(properties);
        this.stampType = stampType;
    }

    public StampType getStampType() {
        return stampType;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    public enum StampType {
        PLATE("plate");

        private final String serializedName;

        StampType(String serializedName) {
            this.serializedName = serializedName;
        }

        public String getSerializedName() {
            return serializedName;
        }

        public static StampType byName(String name) {
            for (StampType type : values()) {
                if (type.serializedName.equals(name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown press stamp type: " + name);
        }
    }
}
