package com.hbm.ntm.entity.cart;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum NtmMinecartBase {
    VANILLA(0),
    WOOD(1),
    STEEL(2),
    PAINTED(3);

    private final int legacyId;

    NtmMinecartBase(int legacyId) {
        this.legacyId = legacyId;
    }

    public int legacyId() {
        return legacyId;
    }

    public Item cartItem(NtmMinecartType type) {
        return switch (type) {
            case EMPTY -> switch (this) {
                case WOOD -> ModItems.CART_EMPTY_WOOD.get();
                case STEEL -> ModItems.CART_EMPTY_STEEL.get();
                case PAINTED -> ModItems.CART_EMPTY_PAINTED.get();
                case VANILLA -> throw new IllegalArgumentException("Empty custom carts do not support vanilla bases");
            };
            case CRATE -> switch (this) {
                case VANILLA -> ModItems.CART_CRATE.get();
                case WOOD, STEEL, PAINTED -> throw new IllegalArgumentException("Crate carts only support vanilla bases");
            };
            case POWDER -> switch (this) {
                case WOOD -> ModItems.CART_POWDER_WOOD.get();
                case STEEL -> ModItems.CART_POWDER_STEEL.get();
                case PAINTED -> ModItems.CART_POWDER_PAINTED.get();
                case VANILLA -> throw new IllegalArgumentException("Powder carts do not support vanilla bases");
            };
            case SEMTEX -> switch (this) {
                case WOOD -> ModItems.CART_SEMTEX_WOOD.get();
                case STEEL -> ModItems.CART_SEMTEX_STEEL.get();
                case PAINTED -> ModItems.CART_SEMTEX_PAINTED.get();
                case VANILLA -> throw new IllegalArgumentException("Semtex carts do not support vanilla bases");
            };
            case DESTROYER -> switch (this) {
                case STEEL -> ModItems.CART_DESTROYER_STEEL.get();
                case PAINTED -> ModItems.CART_DESTROYER_PAINTED.get();
                case VANILLA, WOOD -> throw new IllegalArgumentException("Destroyer carts only support steel or painted bases");
            };
        };
    }

    public ItemStack cartStack(NtmMinecartType type) {
        return new ItemStack(cartItem(type));
    }

    public Item emptyCartItem() {
        return cartItem(NtmMinecartType.EMPTY);
    }

    public ItemStack emptyCartStack() {
        return cartStack(NtmMinecartType.EMPTY);
    }

    public static NtmMinecartBase byLegacyId(int legacyId) {
        for (NtmMinecartBase base : values()) {
            if (base.legacyId == legacyId) {
                return base;
            }
        }
        return STEEL;
    }
}
