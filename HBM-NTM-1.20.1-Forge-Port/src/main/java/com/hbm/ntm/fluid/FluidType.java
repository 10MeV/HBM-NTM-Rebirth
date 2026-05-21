package com.hbm.ntm.fluid;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.trait.FluidTrait;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class FluidType {
    public static final int ROOM_TEMPERATURE = 20;

    private final int id;
    private final String name;
    private final ResourceLocation texture;
    private final int color;
    private final int guiTint;
    private final int poison;
    private final int flammability;
    private final int reactivity;
    private final FluidSymbol symbol;
    private final Map<Class<? extends FluidTrait>, FluidTrait> traits = new LinkedHashMap<>();
    private int temperature = ROOM_TEMPERATURE;

    FluidType(int id, String name, int color, int poison, int flammability, int reactivity, FluidSymbol symbol) {
        this.id = id;
        this.name = name.toUpperCase(Locale.US);
        this.color = color;
        this.guiTint = color;
        this.poison = poison;
        this.flammability = flammability;
        this.reactivity = reactivity;
        this.symbol = symbol;
        this.texture = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/fluids/" + toPath() + ".png");
    }

    public FluidType setTemperature(int temperature) {
        this.temperature = temperature;
        return this;
    }

    public FluidType addTraits(FluidTrait... traits) {
        for (FluidTrait trait : traits) {
            this.traits.put(trait.getClass(), trait);
        }
        return this;
    }

    public boolean hasTrait(Class<? extends FluidTrait> trait) {
        return traits.containsKey(trait);
    }

    @SuppressWarnings("unchecked")
    public <T extends FluidTrait> T getTrait(Class<T> trait) {
        return (T) traits.get(trait);
    }

    public Collection<FluidTrait> getTraits() {
        return Collections.unmodifiableCollection(traits.values());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String toPath() {
        return name.toLowerCase(Locale.US);
    }

    public int getColor() {
        return color;
    }

    public int getGuiTint() {
        return guiTint;
    }

    public int getPoison() {
        return poison;
    }

    public int getFlammability() {
        return flammability;
    }

    public int getReactivity() {
        return reactivity;
    }

    public FluidSymbol getSymbol() {
        return symbol;
    }

    public int getTemperature() {
        return temperature;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public boolean isHot() {
        return temperature >= 100;
    }

    public boolean isAntimatter() {
        return hasTrait(SimpleFluidTraits.Antimatter.class);
    }

    public boolean hasNoContainer() {
        return hasTrait(SimpleFluidTraits.NoContainer.class);
    }

    public boolean hasNoId() {
        return hasTrait(SimpleFluidTraits.NoId.class);
    }

    public boolean needsLeadContainer() {
        return hasTrait(SimpleFluidTraits.LeadContainer.class);
    }

    public boolean isDispersible() {
        return !hasTrait(SimpleFluidTraits.Antimatter.class)
                && !hasTrait(SimpleFluidTraits.NoContainer.class)
                && !hasTrait(SimpleFluidTraits.Viscous.class);
    }
}
