package com.hbm.inventory.fluid.trait;

import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import java.util.List;
import net.minecraft.ChatFormatting;

/**
 * Legacy package bridges for 1.7.10 simple fluid tag traits.
 */
@Deprecated(forRemoval = false)
public final class FluidTraitSimple {
    public static class FT_Gaseous extends SimpleFluidTraits.Gaseous {
        @Override
        public String getLegacyName() {
            return "gaseous";
        }

        public void addInfoHidden(List<String> info) {
            info.add(ChatFormatting.BLUE + "[Gaseous]");
        }
    }

    public static class FT_Gaseous_ART extends SimpleFluidTraits.GaseousAtRoomTemperature {
        @Override
        public String getLegacyName() {
            return "gaseous_art";
        }

        public void addInfoHidden(List<String> info) {
            info.add(ChatFormatting.BLUE + "[Gaseous at Room Temperature]");
        }
    }

    public static class FT_Liquid extends SimpleFluidTraits.Liquid {
        @Override
        public String getLegacyName() {
            return "liquid";
        }

        public void addInfoHidden(List<String> info) {
            info.add(ChatFormatting.BLUE + "[Liquid]");
        }
    }

    public static class FT_Viscous extends SimpleFluidTraits.Viscous {
        @Override
        public String getLegacyName() {
            return "viscous";
        }

        public void addInfoHidden(List<String> info) {
            info.add(ChatFormatting.BLUE + "[Viscous]");
        }
    }

    public static class FT_Plasma extends SimpleFluidTraits.Plasma {
        @Override
        public String getLegacyName() {
            return "plasma";
        }

        public void addInfoHidden(List<String> info) {
            info.add(ChatFormatting.LIGHT_PURPLE + "[Plasma]");
        }
    }

    public static class FT_Amat extends SimpleFluidTraits.Antimatter {
        @Override
        public String getLegacyName() {
            return "amat";
        }
    }

    public static class FT_LeadContainer extends SimpleFluidTraits.LeadContainer {
        @Override
        public String getLegacyName() {
            return "leadcontainer";
        }
    }

    public static class FT_Delicious extends SimpleFluidTraits.Delicious {
        @Override
        public String getLegacyName() {
            return "delicious";
        }

        public void addInfoHidden(List<String> info) {
            info.add(ChatFormatting.DARK_GREEN + "[Delicious]");
        }
    }

    public static class FT_Unsiphonable extends SimpleFluidTraits.Unsiphonable {
        @Override
        public String getLegacyName() {
            return "unsiphonable";
        }

        public void addInfoHidden(List<String> info) {
            info.add(ChatFormatting.BLUE + "[Ignored by siphon]");
        }
    }

    public static class FT_NoID extends SimpleFluidTraits.NoId {
        @Override
        public String getLegacyName() {
            return "noid";
        }
    }

    public static class FT_NoContainer extends SimpleFluidTraits.NoContainer {
        @Override
        public String getLegacyName() {
            return "nocontainer";
        }
    }

    private FluidTraitSimple() {
    }
}
