package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class HbmForgeFluidType extends net.minecraftforge.fluids.FluidType {
    private static final ResourceLocation STILL_TEXTURE = new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation("minecraft", "block/water_flow");

    private final FluidType hbmType;

    public HbmForgeFluidType(FluidType hbmType) {
        super(propertiesFor(hbmType));
        this.hbmType = hbmType;
    }

    public FluidType hbmType() {
        return hbmType;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING_TEXTURE;
            }

            @Override
            public int getTintColor() {
                return 0xFF000000 | (hbmType.getGuiTint() & 0xFFFFFF);
            }
        });
    }

    private static Properties propertiesFor(FluidType hbmType) {
        Properties properties = Properties.create()
                .descriptionId(hbmType.getTranslationKey())
                .temperature(Math.max(0, hbmType.getTemperature() + 273));

        if (hbmType.hasTrait(SimpleFluidTraits.Gaseous.class)) {
            properties.density(-100).viscosity(100).canSwim(false).canDrown(false);
        } else if (hbmType.hasTrait(SimpleFluidTraits.Viscous.class)) {
            properties.viscosity(4_000);
        }

        if (hbmType == HbmFluids.LAVA || hbmType.isHot()) {
            properties.lightLevel(6);
        }

        return properties;
    }
}
