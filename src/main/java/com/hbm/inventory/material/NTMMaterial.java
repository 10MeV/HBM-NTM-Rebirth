package com.hbm.inventory.material;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class NTMMaterial {
    public final int id;
    public final String[] names;
    public final Set<MaterialShapes> autogen = new HashSet<>();
    public final Set<MatTraits> traits = new HashSet<>();
    public SmeltingBehavior smeltable = SmeltingBehavior.NOT_SMELTABLE;
    public int solidColorLight = 0xFF4A00;
    public int solidColorDark = 0x802000;
    public int moltenColor = 0xFF4A00;
    public NTMMaterial smeltsInto;
    public int convIn = 1;
    public int convOut = 1;

    public NTMMaterial(int id, String... names) {
        this.id = id;
        this.names = names == null || names.length == 0 ? new String[] { "Unknown" } : names;
        this.smeltsInto = this;
        Mats.register(this);
    }

    public String getUnlocalizedName() {
        return "hbmmat." + names[0].toLowerCase(Locale.ROOT);
    }

    public NTMMaterial setConversion(NTMMaterial material, int in, int out) {
        this.smeltsInto = material == null ? this : material;
        this.convIn = Math.max(1, in);
        this.convOut = Math.max(0, out);
        return this;
    }

    public NTMMaterial setAutogen(MaterialShapes... shapes) {
        if (shapes != null) {
            for (MaterialShapes shape : shapes) {
                if (shape != null) {
                    autogen.add(shape);
                }
            }
        }
        return this;
    }

    public NTMMaterial setTraits(MatTraits... traits) {
        if (traits != null) {
            for (MatTraits trait : traits) {
                if (trait != null) {
                    this.traits.add(trait);
                }
            }
        }
        return this;
    }

    public NTMMaterial m() {
        traits.add(MatTraits.METAL);
        return this;
    }

    public NTMMaterial n() {
        traits.add(MatTraits.NONMETAL);
        return this;
    }

    public NTMMaterial smeltable(SmeltingBehavior behavior) {
        this.smeltable = behavior == null ? SmeltingBehavior.NOT_SMELTABLE : behavior;
        return this;
    }

    public NTMMaterial setSolidColor(int colorLight, int colorDark) {
        this.solidColorLight = colorLight;
        this.solidColorDark = colorDark;
        return this;
    }

    public NTMMaterial setMoltenColor(int color) {
        this.moltenColor = color;
        return this;
    }

    public enum SmeltingBehavior {
        NOT_SMELTABLE,
        VAPORIZES,
        BREAKS,
        SMELTABLE,
        ADDITIVE
    }

    public enum MatTraits {
        METAL,
        NONMETAL
    }
}
