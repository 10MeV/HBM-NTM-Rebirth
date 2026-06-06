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
        FLAT("flat"),
        PLATE("plate"),
        WIRE("wire"),
        CIRCUIT("circuit"),
        C357("357"),
        C44("44"),
        C50("50"),
        C9("9"),
        PRINTING1("printing1"),
        PRINTING2("printing2"),
        PRINTING3("printing3"),
        PRINTING4("printing4"),
        PRINTING5("printing5"),
        PRINTING6("printing6"),
        PRINTING7("printing7"),
        PRINTING8("printing8");

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
