package com.hbm.ntm.fluid;

import com.google.gson.JsonObject;
import com.hbm.config.GeneralConfig;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.trait.ContainerFluidTrait;
import com.hbm.ntm.fluid.trait.CorrosiveFluidTrait;
import com.hbm.ntm.fluid.trait.FluidTrait;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import com.hbm.render.util.EnumSymbol;
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
import net.minecraft.world.level.block.entity.BlockEntity;

public final class FluidType {
    public static final int ROOM_TEMPERATURE = 20;

    private int id;
    private String name;
    private ResourceLocation texture;
    private int color;
    private int guiTint;
    private String displayNameOverride;
    public int poison;
    public int flammability;
    public int reactivity;
    public EnumSymbol symbol;
    private final Map<Class<? extends FluidTrait>, FluidTrait> traits = new LinkedHashMap<>();
    public int temperature = ROOM_TEMPERATURE;
    public boolean renderWithTint;

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
        this.symbol = EnumSymbol.fromModern(symbol);
        this.texture = texture(textureName == null || textureName.isBlank() ? toPath() : textureName);
        this.renderWithTint = renderTankWithTint;
        return this;
    }

    public FluidType setTemperature(int temperature) {
        this.temperature = temperature;
        return this;
    }

    @Deprecated(forRemoval = false)
    public FluidType setTemp(int temperature) {
        return setTemperature(temperature);
    }

    @Deprecated(forRemoval = false)
    public FluidType addContainers(Object... containers) {
        if (containers == null || containers.length == 0) {
            return this;
        }
        ContainerFluidTrait trait = mutableContainerTrait();
        for (Object container : containers) {
            if (container instanceof Fluids.CD_Canister canister) {
                trait.withCanister(canister.color);
            } else if (container instanceof Fluids.CD_Gastank gasTank) {
                trait.withGasTank(gasTank.bottleColor, gasTank.labelColor);
            }
        }
        return this;
    }

    @Deprecated(forRemoval = false)
    @SuppressWarnings("unchecked")
    public <T> T getContainer(Class<? extends T> container) {
        if (container == null) {
            return null;
        }
        ContainerFluidTrait trait = getTrait(ContainerFluidTrait.class);
        if (trait == null) {
            return null;
        }
        if (container == Fluids.CD_Canister.class && trait.hasCanister()) {
            return (T) new Fluids.CD_Canister(trait.getCanisterColor());
        }
        if (container == Fluids.CD_Gastank.class && trait.hasGasTank()) {
            return (T) new Fluids.CD_Gastank(trait.getGasTankBottleColor(), trait.getGasTankLabelColor());
        }
        return null;
    }

    public FluidType addTraits(FluidTrait... traits) {
        for (FluidTrait trait : traits) {
            this.traits.put(trait.getClass(), trait);
        }
        return this;
    }

    public boolean hasTrait(Class<? extends FluidTrait> trait) {
        return getTrait(trait) != null;
    }

    @SuppressWarnings("unchecked")
    public <T extends FluidTrait> T getTrait(Class<T> trait) {
        FluidTrait exact = traits.get(trait);
        if (exact != null) {
            return (T) exact;
        }
        for (FluidTrait candidate : traits.values()) {
            if (trait.isInstance(candidate)) {
                return (T) candidate;
            }
        }
        return null;
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

    @Deprecated(forRemoval = false)
    public int getID() {
        return getId();
    }

    @Deprecated(forRemoval = false)
    public static FluidType getEnum(int id) {
        return HbmFluids.fromId(id);
    }

    @Deprecated(forRemoval = false)
    public static FluidType getEnumFromName(String name) {
        return HbmFluids.fromName(name);
    }

    public String getName() {
        return name;
    }

    @Deprecated(forRemoval = false)
    public String name() {
        return getName();
    }

    @Deprecated(forRemoval = false)
    public int ordinal() {
        return getId();
    }

    public String toPath() {
        return name.toLowerCase(Locale.US);
    }

    public String getTranslationKey() {
        return "hbmfluid." + toPath();
    }

    @Deprecated(forRemoval = false)
    public String getUnlocalizedName() {
        return getTranslationKey();
    }

    @Deprecated(forRemoval = false)
    public String getConditionalName() {
        return displayNameOverride != null && !displayNameOverride.isBlank()
                ? displayNameOverride
                : getTranslationKey();
    }

    @Deprecated(forRemoval = false)
    public String getLocalizedName() {
        return getDisplayName().getString();
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

    @Deprecated(forRemoval = false)
    public int getTint() {
        return getGuiTint();
    }

    @Deprecated(forRemoval = false)
    public int getMSAColor() {
        return getColor();
    }

    public FluidType renderTankWithTint() {
        this.renderWithTint = true;
        return this;
    }

    FluidType setRenderTankWithTint(boolean renderTankWithTint) {
        this.renderWithTint = renderTankWithTint;
        return this;
    }

    public boolean shouldRenderTankWithTint() {
        return renderWithTint;
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
        return symbol == null ? FluidSymbol.NONE : symbol.modern();
    }

    public int getTemperature() {
        return temperature;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    @Deprecated(forRemoval = false)
    public String getDict(int quantity) {
        String prefix = GeneralConfig.enableFluidContainerCompat() ? "container" : "ntmcontainer";
        return legacyContainerTagName(prefix, quantity);
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
        object.addProperty("symbol", getSymbol().name());
        object.addProperty("texture", texture.toString());
        if (displayNameOverride != null) {
            object.addProperty("displayName", displayNameOverride);
        }
        object.addProperty("temperature", temperature);
        object.addProperty("renderTankWithTint", renderWithTint);

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
                    .withStyle(temperature < 0 ? ChatFormatting.BLUE : ChatFormatting.RED));
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

    @Deprecated(forRemoval = false)
    public void addInfo(List<String> info) {
        if (info == null) {
            return;
        }
        List<Component> components = new ArrayList<>();
        appendInfo(components, false);
        for (Component component : components) {
            info.add(component.getString());
        }
    }

    public boolean isHot() {
        return temperature >= 100;
    }

    public boolean isCorrosive() {
        return hasTrait(CorrosiveFluidTrait.class);
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

    @Deprecated(forRemoval = false)
    public boolean hasNoID() {
        return hasNoId();
    }

    public boolean needsLeadContainer() {
        return hasTrait(SimpleFluidTraits.LeadContainer.class);
    }

    public boolean isDispersible() {
        return !hasTrait(SimpleFluidTraits.Antimatter.class)
                && !hasTrait(SimpleFluidTraits.NoContainer.class)
                && !hasTrait(SimpleFluidTraits.Viscous.class);
    }

    @Deprecated(forRemoval = false)
    public boolean isDispersable() {
        return isDispersible();
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
        return HbmFluidReleaseEffects.applyLegacyTraitRelease(level, pos, this, amountMb, releaseType);
    }

    @Deprecated(forRemoval = false)
    public void onTankBroken(BlockEntity blockEntity, com.hbm.inventory.fluid.tank.FluidTank tank) {
    }

    @Deprecated(forRemoval = false)
    public void onTankUpdate(BlockEntity blockEntity, com.hbm.inventory.fluid.tank.FluidTank tank) {
    }

    @Deprecated(forRemoval = false)
    public HbmFluidReleaseEffects.ReleaseReport onFluidRelease(BlockEntity blockEntity,
            com.hbm.inventory.fluid.tank.FluidTank tank, int overflowAmount) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return HbmFluidReleaseEffects.previewLegacyTraitRelease(this, overflowAmount, FluidReleaseType.SPILL);
        }
        return onFluidRelease(blockEntity.getLevel(), blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(),
                blockEntity.getBlockPos().getZ(), tank, overflowAmount);
    }

    @Deprecated(forRemoval = false)
    public HbmFluidReleaseEffects.ReleaseReport onFluidRelease(Level level, int x, int y, int z,
            com.hbm.inventory.fluid.tank.FluidTank tank, int overflowAmount) {
        if (level == null) {
            return HbmFluidReleaseEffects.previewLegacyTraitRelease(this, overflowAmount, FluidReleaseType.SPILL);
        }
        return onFluidRelease(level, new BlockPos(x, y, z), overflowAmount, FluidReleaseType.SPILL);
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

    private ContainerFluidTrait mutableContainerTrait() {
        ContainerFluidTrait trait = getTrait(ContainerFluidTrait.class);
        if (trait == null) {
            trait = new ContainerFluidTrait();
            addTraits(trait);
        }
        return trait;
    }

    private String legacyContainerTagName(String prefix, int quantity) {
        return prefix + quantity + name.replace("_", "").toLowerCase(Locale.US);
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
