package com.hbm.ntm.fluid;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class HbmForgeFluidType extends net.minecraftforge.fluids.FluidType {
    private final FluidType hbmType;
    private final ResourceLocation sprite;

    public HbmForgeFluidType(FluidType hbmType) {
        super(propertiesFor(hbmType));
        this.hbmType = hbmType;
        this.sprite = new ResourceLocation(HbmNtm.MOD_ID, "block/fluid/" + hbmType.toPath());
    }

    public FluidType hbmType() {
        return hbmType;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return sprite;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return sprite;
            }

            @Override
            public int getTintColor() {
                return 0xFFFFFFFF;
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
