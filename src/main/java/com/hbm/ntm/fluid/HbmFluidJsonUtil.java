package com.hbm.ntm.fluid;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

public final class HbmFluidJsonUtil {
    public static FluidType readFluidReference(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return HbmFluids.NONE;
        }
        if (element.isJsonObject()) {
            return readFluidReference(element.getAsJsonObject());
        }
        return readFluidReference(element.getAsString());
    }

    public static FluidType readFluidReference(JsonObject object) {
        if (object == null) {
            return HbmFluids.NONE;
        }
        if (object.has("hbm")) {
            return readNamedField(object, "hbm");
        }
        if (object.has("fluid")) {
            return readNamedField(object, "fluid");
        }
        if (object.has("type")) {
            return readNamedField(object, "type");
        }
        if (object.has("forge")) {
            return readForgeField(object, "forge");
        }
        if (object.has("forgeFluid")) {
            return readForgeField(object, "forgeFluid");
        }
        return object.has("tag") ? readTagField(object, "tag") : HbmFluids.NONE;
    }

    public static FluidType readFluidReference(String value) {
        if (value == null || value.isBlank()) {
            return HbmFluids.NONE;
        }
        String trimmed = value.trim();
        ResourceLocation id = ResourceLocation.tryParse(trimmed.contains(":") ? trimmed : "forge:" + trimmed);
        if (!trimmed.contains(":")) {
            FluidType type = HbmFluids.fromName(trimmed);
            if (type != HbmFluids.NONE) {
                return type;
            }
        } else if (isHbmNamespace(id)) {
            FluidType type = HbmFluids.fromName(id.getPath());
            if (type != HbmFluids.NONE) {
                return type;
            }
        }
        if (id == null) {
            return HbmFluids.NONE;
        }
        FluidType type = HbmFluidForgeMappings.fromTagAlias(id);
        if (type != HbmFluids.NONE) {
            return type;
        }
        return readForgeFluid(id);
    }

    public static FluidType requireFluidReference(JsonElement element, String name) {
        FluidType type = readFluidReference(element);
        if (type == HbmFluids.NONE) {
            throw unknownFluidReference(name, element);
        }
        return type;
    }

    public static UnknownFluidReferenceException unknownFluidReference(String name, JsonElement element) {
        return new UnknownFluidReferenceException("Unknown HBM fluid reference in " + name + ": " + element);
    }

    public static boolean isExplicitNoneReference(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return false;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            return isExplicitNoneField(object, "hbm")
                    || isExplicitNoneField(object, "fluid")
                    || isExplicitNoneField(object, "type");
        }
        return isExplicitNoneReference(element.getAsString());
    }

    public static boolean isExplicitNoneReference(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.contains(":")) {
            ResourceLocation id = ResourceLocation.tryParse(trimmed);
            if (id == null || !isHbmNamespace(id)) {
                return false;
            }
            trimmed = id.getPath();
        }
        return HbmFluids.NONE.getName().equalsIgnoreCase(trimmed);
    }

    public static HbmFluidStack readFluidStack(JsonObject object, String name) {
        FluidType type = requireFluidReference(object.get("fluid"), name + " fluid");
        int amount = GsonHelper.getAsInt(object, "amount");
        if (amount <= 0) {
            throw new JsonSyntaxException("Invalid fluid amount " + amount + " in " + name);
        }
        int pressure = GsonHelper.getAsInt(object, "pressure", 0);
        return new HbmFluidStack(type, amount, pressure);
    }

    private static FluidType readNamedField(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element == null ? HbmFluids.NONE : readFluidReference(element);
    }

    private static boolean isExplicitNoneField(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && isExplicitNoneReference(element);
    }

    private static FluidType readForgeField(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return HbmFluids.NONE;
        }
        ResourceLocation id = ResourceLocation.tryParse(element.getAsString());
        return id == null ? HbmFluids.NONE : readForgeFluid(id);
    }

    private static FluidType readTagField(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return HbmFluids.NONE;
        }
        ResourceLocation id = ResourceLocation.tryParse(element.getAsString().contains(":")
                ? element.getAsString()
                : "forge:" + element.getAsString());
        return id == null ? HbmFluids.NONE : HbmFluidForgeMappings.fromTagAlias(id);
    }

    private static boolean isHbmNamespace(ResourceLocation id) {
        return id != null && ("hbm".equals(id.getNamespace()) || HbmNtm.MOD_ID.equals(id.getNamespace()));
    }

    private static FluidType readForgeFluid(ResourceLocation id) {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        return fluid == null || fluid == Fluids.EMPTY ? HbmFluids.NONE : HbmFluidForgeMappings.fromForge(fluid);
    }

    private HbmFluidJsonUtil() {
    }

    public static final class UnknownFluidReferenceException extends RuntimeException {
        public UnknownFluidReferenceException(String message) {
            super(message);
        }
    }
}
