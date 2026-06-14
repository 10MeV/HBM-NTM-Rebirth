package com.hbm.ntm.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PlasticScrapItem extends Item {
    public static final String TAG_SCRAP_TYPE = "ScrapType";

    public PlasticScrapItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createStack(Item item, ScrapType type) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt(TAG_SCRAP_TYPE, type.ordinal());
        return stack;
    }

    public static ScrapType getScrapType(ItemStack stack) {
        int id = stack.hasTag() ? stack.getTag().getInt(TAG_SCRAP_TYPE) : 0;
        return ScrapType.byId(id);
    }

    public enum ScrapType {
        BOARD_BLANK,
        BOARD_TRANSISTOR,
        BOARD_CONVERTER,
        BRIDGE_NORTH,
        BRIDGE_SOUTH,
        BRIDGE_IO,
        BRIDGE_BUS,
        BRIDGE_CHIPSET,
        BRIDGE_CMOS,
        BRIDGE_BIOS,
        CPU_REGISTER,
        CPU_CLOCK,
        CPU_LOGIC,
        CPU_CACHE,
        CPU_EXT,
        CPU_SOCKET,
        MEM_SOCKET,
        MEM_16K_A,
        MEM_16K_B,
        MEM_16K_C,
        MEM_16K_D,
        CARD_BOARD,
        CARD_PROCESSOR;

        public static ScrapType byId(int id) {
            ScrapType[] values = values();
            return values[Math.max(0, Math.min(id, values.length - 1))];
        }
    }
}
