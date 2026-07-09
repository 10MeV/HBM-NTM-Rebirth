package com.hbm.inventory.recipes;

import com.hbm.inventory.FluidContainer;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.ntm.energy.HbmLegacyBatteryMaps;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import com.hbm.ntm.registry.ModItems;
import com.hbm.util.Tuple.Triplet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.RegistryObject;

/**
 * Legacy 1.7.10 MachineRecipes facade for NEI/display helper lists.
 */
@Deprecated(forRemoval = false)
public class MachineRecipes {
    public static MachineRecipes instance() {
        return new MachineRecipes();
    }

    public ArrayList<ItemStack> getAlloyFuels() {
        ArrayList<ItemStack> fuels = new ArrayList<>();
        fuels.add(new ItemStack(Items.COAL));
        fuels.add(new ItemStack(Blocks.COAL_BLOCK));
        fuels.add(new ItemStack(Items.LAVA_BUCKET));
        fuels.add(new ItemStack(Items.BLAZE_ROD));
        fuels.add(new ItemStack(Items.BLAZE_POWDER));
        fuels.add(legacyStack("lignite"));
        fuels.add(legacyStack("powder_lignite"));
        fuels.add(new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.BRIQUETTE, 0).get()));
        fuels.add(new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.COKE, 0).get()));
        fuels.add(legacyStack("solid_fuel"));
        fuels.add(legacyStack("powder_coal"));
        return fuels;
    }

    public ArrayList<ItemStack> getBatteries() {
        return new ArrayList<>(HbmLegacyBatteryMaps.legacyMachineRecipeBatteryDisplayStacks());
    }

    public static boolean mODE(ItemStack stack, String name) {
        return stack != null && !stack.isEmpty() && name != null && !name.isBlank()
                && stack.is(LegacyOreDictionaryMappings.itemTag(name));
    }

    public List<Triplet<ItemStack, ItemStack, ItemStack>> getFluidContainers() {
        List<Triplet<ItemStack, ItemStack, ItemStack>> list = new ArrayList<>();
        FluidContainerRegistry.register();
        for (FluidContainer con : FluidContainerRegistry.allContainers) {
            if (con == null || con.type == null || con.fullContainer == null || con.fullContainer.isEmpty()
                    || con.content <= 0) {
                continue;
            }
            ItemStack fluid = ItemFluidIcon.make(con.type, con.content);
            ItemFluidIcon.addQuantity(fluid, con.content);
            ItemStack empty = con.emptyContainer == null ? ItemStack.EMPTY : con.emptyContainer.copy();
            list.add(new Triplet<>(fluid, empty, con.fullContainer.copy()));
        }
        return list;
    }

    private static ItemStack legacyStack(String name) {
        RegistryObject<Item> item = Objects.requireNonNull(ModItems.legacyItem(name),
                () -> "Missing legacy item registration: " + name);
        return new ItemStack(item.get());
    }
}
