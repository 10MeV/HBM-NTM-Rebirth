package com.hbm.inventory.material;

import java.util.ArrayList;
import java.util.List;

public class MaterialShapes {
    public static final List<MaterialShapes> allShapes = new ArrayList<>();

    public static final MaterialShapes ANY = new MaterialShapes(0, "any").noAutogen();
    public static final MaterialShapes ONLY_ORE = new MaterialShapes(0, "ore").noAutogen();
    public static final MaterialShapes ORE = new MaterialShapes(0, "ore", "oreNether").noAutogen();
    public static final MaterialShapes ORENETHER = new MaterialShapes(0, "oreNether").noAutogen();
    public static final MaterialShapes QUANTUM = new MaterialShapes(1);
    public static final MaterialShapes NUGGET = new MaterialShapes(8, "nugget", "tiny");
    public static final MaterialShapes TINY = new MaterialShapes(8, "tiny").noAutogen();
    public static final MaterialShapes FRAGMENT = new MaterialShapes(8, "bedrockorefragment");
    public static final MaterialShapes DUSTTINY = new MaterialShapes(NUGGET.quantity, "dustTiny");
    public static final MaterialShapes WIRE = new MaterialShapes(9, "wireFine");
    public static final MaterialShapes BOLT = new MaterialShapes(9, "bolt");
    public static final MaterialShapes BILLET = new MaterialShapes(NUGGET.quantity * 6, "billet");
    public static final MaterialShapes INGOT = new MaterialShapes(NUGGET.quantity * 9, "ingot");
    public static final MaterialShapes GEM = new MaterialShapes(INGOT.quantity, "gem");
    public static final MaterialShapes CRYSTAL = new MaterialShapes(INGOT.quantity, "crystal");
    public static final MaterialShapes DUST = new MaterialShapes(INGOT.quantity, "dust");
    public static final MaterialShapes DENSEWIRE = new MaterialShapes(INGOT.quantity, "wireDense");
    public static final MaterialShapes PLATE = new MaterialShapes(INGOT.quantity, "plate");
    public static final MaterialShapes CASTPLATE = new MaterialShapes(INGOT.quantity * 3, "plateTriple");
    public static final MaterialShapes WELDEDPLATE = new MaterialShapes(INGOT.quantity * 6, "plateSextuple");
    public static final MaterialShapes SHELL = new MaterialShapes(INGOT.quantity * 4, "shell");
    public static final MaterialShapes PIPE = new MaterialShapes(INGOT.quantity * 3, "ntmpipe");
    public static final MaterialShapes QUART = new MaterialShapes(162);
    public static final MaterialShapes BLOCK = new MaterialShapes(INGOT.quantity * 9, "block");
    public static final MaterialShapes LIGHTBARREL = new MaterialShapes(INGOT.quantity * 3, "barrelLight");
    public static final MaterialShapes HEAVYBARREL = new MaterialShapes(INGOT.quantity * 6, "barrelHeavy");
    public static final MaterialShapes LIGHTRECEIVER = new MaterialShapes(INGOT.quantity * 4, "receiverLight");
    public static final MaterialShapes HEAVYRECEIVER = new MaterialShapes(INGOT.quantity * 9, "receiverHeavy");
    public static final MaterialShapes MECHANISM = new MaterialShapes(INGOT.quantity * 4, "gunMechanism");
    public static final MaterialShapes STOCK = new MaterialShapes(INGOT.quantity * 4, "stock");
    public static final MaterialShapes GRIP = new MaterialShapes(INGOT.quantity * 2, "grip");

    public boolean noAutogen;
    private final int quantity;
    public final String[] prefixes;

    private MaterialShapes(int quantity, String... prefixes) {
        this.quantity = quantity;
        this.prefixes = prefixes;
        if (prefixes != null) {
            for (String prefix : prefixes) {
                Mats.prefixByName.put(prefix, this);
            }
        }
        allShapes.add(this);
    }

    public MaterialShapes noAutogen() {
        this.noAutogen = true;
        return this;
    }

    public int q(int amount) {
        return quantity * amount;
    }

    public int q(int unitsUsed, int itemsProduced) {
        return itemsProduced == 0 ? 0 : quantity * unitsUsed / itemsProduced;
    }

    public String name() {
        return prefixes != null && prefixes.length > 0 ? prefixes[0] : "unknown";
    }

    public String make(NTMMaterial material) {
        return name() + (material == null ? "Unknown" : material.names[0]);
    }
}
