package com.hbm.ntm.compat.jei;

import com.hbm.ntm.blockentity.LegacyRemoteFluidMachineBlockEntity.LegacyGuiProfile;
import com.hbm.ntm.client.screen.AnvilScreen;
import com.hbm.ntm.client.screen.ArcWelderScreen;
import com.hbm.ntm.client.screen.BasicMachineScreen;
import com.hbm.ntm.client.screen.BreedingReactorScreen;
import com.hbm.ntm.client.screen.CombinationOvenScreen;
import com.hbm.ntm.client.screen.CyclotronScreen;
import com.hbm.ntm.client.screen.ElectricPressScreen;
import com.hbm.ntm.client.screen.ElectrolyserScreen;
import com.hbm.ntm.client.screen.ExposureChamberScreen;
import com.hbm.ntm.client.screen.GasCentScreen;
import com.hbm.ntm.client.screen.MixerScreen;
import com.hbm.ntm.client.screen.ProcessingMachineScreen;
import com.hbm.ntm.client.screen.PyroOvenScreen;
import com.hbm.ntm.client.screen.RBMKOutgasserScreen;
import com.hbm.ntm.client.screen.RadiolysisScreen;
import com.hbm.ntm.client.screen.RefineryScreen;
import com.hbm.ntm.client.screen.RemoteFluidMachineScreen;
import com.hbm.ntm.client.screen.RotaryFurnaceScreen;
import com.hbm.ntm.client.screen.ShredderScreen;
import com.hbm.ntm.client.screen.SilexScreen;
import com.hbm.ntm.client.screen.SolderingStationScreen;
import java.util.Collection;
import java.util.List;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;

/**
 * Restores the machine-screen recipe entry points registered by legacy NEI handlers.
 *
 * <p>The coordinates target the equivalent active process region in the current screen, not the old
 * 1.7.10 texture coordinates. This keeps the lookup affordance attached to the rendered operation
 * after the menu layouts were modernized.</p>
 */
final class LegacyJeiGuiInteractions {
    private LegacyJeiGuiInteractions() {
    }

    static void register(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(AnvilScreen.class, 11, 42, 36, 18,
                HbmJeiPlugin.ANVIL_CONSTRUCTION, HbmJeiPlugin.ANVIL_SMITHING);
        registration.addRecipeClickArea(AnvilScreen.class, 65, 42, 36, 18,
                HbmJeiPlugin.ANVIL_CONSTRUCTION, HbmJeiPlugin.ANVIL_SMITHING);
        registration.addRecipeClickArea(ArcWelderScreen.class, 72, 37, 33, 14, HbmJeiPlugin.ARC_WELDER);
        registration.addRecipeClickArea(BreedingReactorScreen.class, 53, 32, 70, 20,
                HbmJeiPlugin.BREEDING_REACTOR);
        registration.addGuiContainerHandler(ProcessingMachineScreen.class, processingRecipeClickArea());
        registration.addRecipeClickArea(CombinationOvenScreen.class, 45, 37, 38, 14,
                HbmJeiPlugin.COMBINATION_OVEN);
        registration.addRecipeClickArea(CyclotronScreen.class, 48, 27, 34, 34, HbmJeiPlugin.CYCLOTRON);
        registration.addRecipeClickArea(ElectrolyserScreen.class, 62, 26, 12, 41,
                HbmJeiPlugin.ELECTROLYSER_FLUID);
        registration.addRecipeClickArea(ElectrolyserScreen.class, 7, 45, 22, 26,
                HbmJeiPlugin.ELECTROLYSER_METAL);
        registration.addRecipeClickArea(ExposureChamberScreen.class, 36, 39, 42, 10,
                HbmJeiPlugin.EXPOSURE_CHAMBER);
        registration.addRecipeClickArea(GasCentScreen.class, 70, 35, 36, 13, HbmJeiPlugin.GAS_CENT);
        registration.addRecipeClickArea(MixerScreen.class, 62, 36, 53, 44, HbmJeiPlugin.MIXER);
        registration.addRecipeClickArea(RBMKOutgasserScreen.class, 82, 50, 13, 6, HbmJeiPlugin.OUTGASSER);
        registration.addRecipeClickArea(BasicMachineScreen.class, 79, 35, 18, 16, HbmJeiPlugin.PRESS);
        registration.addRecipeClickArea(ElectricPressScreen.class, 79, 35, 18, 16, HbmJeiPlugin.PRESS);
        registration.addRecipeClickArea(PyroOvenScreen.class, 57, 47, 27, 12, HbmJeiPlugin.PYRO_OVEN);
        registration.addRecipeClickArea(RadiolysisScreen.class, 92, 25, 48, 18, HbmJeiPlugin.RADIOLYSIS);
        registration.addRecipeClickArea(RefineryScreen.class, 36, 16, 122, 102, HbmJeiPlugin.REFINERY);
        registration.addRecipeClickArea(RotaryFurnaceScreen.class, 63, 30, 34, 10,
                HbmJeiPlugin.ROTARY_FURNACE);
        registration.addRecipeClickArea(ShredderScreen.class, 63, 89, 35, 18, HbmJeiPlugin.SHREDDER);
        registration.addRecipeClickArea(SilexScreen.class, 45, 82, 69, 43, HbmJeiPlugin.SILEX);
        registration.addRecipeClickArea(SolderingStationScreen.class, 72, 28, 33, 14,
                HbmJeiPlugin.SOLDERING_STATION);
        registration.addGuiContainerHandler(RemoteFluidMachineScreen.class, cokerRecipeClickArea());
    }

    private static IGuiContainerHandler<RemoteFluidMachineScreen> cokerRecipeClickArea() {
        return new IGuiContainerHandler<>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(RemoteFluidMachineScreen screen,
                    double guiMouseX, double guiMouseY) {
                if (screen.getMenu().getProfile() != LegacyGuiProfile.COKER) {
                    return List.of();
                }
                return List.of(IGuiClickableArea.createBasic(61, 46, 53, 14, HbmJeiPlugin.COKER));
            }
        };
    }

    private static IGuiContainerHandler<ProcessingMachineScreen> processingRecipeClickArea() {
        return new IGuiContainerHandler<>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(ProcessingMachineScreen screen,
                    double guiMouseX, double guiMouseY) {
                return switch (screen.getMenu().getBlockEntity().kind()) {
                    case CENTRIFUGE -> List.of(IGuiClickableArea.createBasic(65, 15, 80, 35,
                            HbmJeiPlugin.CENTRIFUGE));
                    case CRYSTALLIZER -> List.of(IGuiClickableArea.createBasic(80, 47, 28, 12,
                            HbmJeiPlugin.CRYSTALLIZER));
                };
            }
        };
    }
}
