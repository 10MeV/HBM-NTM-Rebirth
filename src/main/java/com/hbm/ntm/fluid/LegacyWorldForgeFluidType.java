package com.hbm.ntm.fluid;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class LegacyWorldForgeFluidType extends net.minecraftforge.fluids.FluidType {
    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;

    public LegacyWorldForgeFluidType(Properties properties, ResourceLocation stillTexture,
            ResourceLocation flowingTexture) {
        super(properties);
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return stillTexture;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flowingTexture;
            }

            @Override
            public int getTintColor() {
                return 0xFFFFFFFF;
            }
        });
    }
}
