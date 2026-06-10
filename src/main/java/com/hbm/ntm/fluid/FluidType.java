package com.hbm.ntm.fluid;

import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.trait.ContainerFluidTrait;
import com.hbm.ntm.fluid.trait.FluidTrait;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class FluidType {
    public static final int ROOM_TEMPERATURE = 20;

    private int id;
    private String name;
    private ResourceLocation texture;
    private int color;
    private int guiTint;
    private String displayNameOverride;
    private int poison;
    private int flammability;
    private int reactivity;
    private FluidSymbol symbol;
    private final Map<Class<? extends FluidTrait>, FluidTrait> traits = new LinkedHashMap<>();
    private int temperature = ROOM_TEMPERATURE;
    private boolean renderTankWithTint;

    FluidType(int id, String name, int color, int poison, int flammability, int reactivity, FluidSymbol symbol) {
        this(id, name, color, poison, flammability, reactivity, symbol, null, 0xFFFFFF, null, false);
    }

    FluidType(int id, String name, int color, int poison, int flammability, int reactivity, FluidSymbol symbol,
            String textureName, int guiTint, String displayNameOverride, boolean renderTankWithTint) {
        setupExternal(id, name, color, poison, flammability, reactivity, symbol, textureName, guiTint,
                displayNameOverride, renderTankWithTint);
    }

    FluidType setupExternal(int id, String name, int color, int poison, int flammability, int reactivity,
            FluidSymbol symbol, String textureName, int guiTint, String displayNameOverride, boolean renderTankWithTint) {
        this.id = id;
        this.name = name.toUpperCase(Locale.US);
        this.color = color;
        this.guiTint = guiTint;
        this.displayNameOverride = displayNameOverride;
        this.poison = poison;
        this.flammability = flammability;
        this.reactivity = reactivity;
        this.symbol = symbol;
        this.texture = texture(textureName == null || textureName.isBlank() ? toPath() : textureName);
        this.renderTankWithTint = renderTankWithTint;
        return this;
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

    public void setTraits(Collection<? extends FluidTrait> traits) {
        this.traits.clear();
        for (FluidTrait trait : traits) {
            this.traits.put(trait.getClass(), trait);
        }
    }

    public Map<String, JsonObject> getTraitJson() {
        Map<String, JsonObject> result = new LinkedHashMap<>();
        for (FluidTrait trait : traits.values()) {
            result.put(trait.getLegacyName(), trait.toJson());
        }
        return Collections.unmodifiableMap(result);
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

    public String getTranslationKey() {
        return "hbmfluid." + toPath();
    }

    public Component getDisplayName() {
        if (displayNameOverride != null && !displayNameOverride.isBlank()) {
            return Component.literal(displayNameOverride);
        }
        return Component.translatableWithFallback(getTranslationKey(), prettyName(name));
    }

    public Component getFallbackDisplayName() {
        return getDisplayName();
    }

    public Component getDebugName() {
        return getDisplayName().copy().append(Component.literal(" (" + name + ")").withStyle(ChatFormatting.DARK_GRAY));
    }

    public int getColor() {
        return color;
    }

    public int getGuiTint() {
        return guiTint;
    }

    public FluidType renderTankWithTint() {
        this.renderTankWithTint = true;
        return this;
    }

    public boolean shouldRenderTankWithTint() {
        return renderTankWithTint;
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

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("name", name);
        object.addProperty("color", color);
        object.addProperty("tint", guiTint);
        object.addProperty("p", poison);
        object.addProperty("f", flammability);
        object.addProperty("r", reactivity);
        object.addProperty("symbol", symbol.name());
        object.addProperty("texture", texture.toString());
        if (displayNameOverride != null) {
            object.addProperty("displayName", displayNameOverride);
        }
        object.addProperty("temperature", temperature);
        object.addProperty("renderTankWithTint", renderTankWithTint);

        JsonObject traitsJson = new JsonObject();
        for (FluidTrait trait : traits.values()) {
            traitsJson.add(trait.getLegacyName(), trait.toJson());
        }
        object.add("traits", traitsJson);
        return object;
    }

    public void appendInfo(List<Component> info, boolean showHidden) {
        if (temperature != ROOM_TEMPERATURE) {
            info.add(Component.literal(temperature + "\u00b0C")
                    .withStyle(temperature < ROOM_TEMPERATURE ? ChatFormatting.BLUE : ChatFormatting.RED));
        }

        List<Component> hidden = new ArrayList<>();
        for (FluidTrait trait : traits.values()) {
            trait.addInfo(info);
            if (showHidden) {
                trait.addHiddenInfo(info);
            } else {
                trait.addHiddenInfo(hidden);
            }
        }

        if (!hidden.isEmpty() && !showHidden) {
            info.add(Component.translatable("hbmfluid.info.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
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

    public boolean hasCanisterContainer() {
        ContainerFluidTrait trait = getTrait(ContainerFluidTrait.class);
        return trait != null && trait.hasCanister();
    }

    public boolean hasGasTankContainer() {
        ContainerFluidTrait trait = getTrait(ContainerFluidTrait.class);
        return trait != null && trait.hasGasTank();
    }

    public HbmFluidReleaseEffects.ReleaseReport onFluidRelease(Level level, BlockPos pos, int amountMb, FluidReleaseType releaseType) {
        return HbmFluidReleaseEffects.applyRelease(level, pos, this, amountMb, releaseType);
    }

    public HbmFluidReleaseEffects.ReleaseReport previewRelease(int amountMb, FluidReleaseType releaseType) {
        return HbmFluidReleaseEffects.previewRelease(this, amountMb, releaseType);
    }

    public HbmFluidContactEffects.ContactReport affectEntity(Entity entity, float intensity) {
        return HbmFluidContactEffects.affectEntity(this, entity, intensity);
    }

    public HbmFluidContactEffects.ContactReport previewEntityContact(Entity entity, float intensity) {
        return HbmFluidContactEffects.previewContact(this, entity, intensity);
    }

    private static String prettyName(String raw) {
        StringBuilder builder = new StringBuilder();
        for (String part : raw.toLowerCase(Locale.US).split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private static ResourceLocation texture(String name) {
        String safeName = sanitizePath(name);
        ResourceLocation parsed = ResourceLocation.tryParse(safeName);
        if (parsed != null && name.contains(":")) {
            return parsed;
        }
        String path = safeName;
        if (!path.startsWith("textures/")) {
            if (path.endsWith(".png")) {
                path = path.substring(0, path.length() - 4);
            }
            path = "textures/gui/fluids/" + path + ".png";
        } else if (!path.endsWith(".png")) {
            path = path + ".png";
        }
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private static String sanitizePath(String name) {
        return (name == null ? "" : name)
                .replace('\\', '/')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9:/._-]", "_");
    }
}
