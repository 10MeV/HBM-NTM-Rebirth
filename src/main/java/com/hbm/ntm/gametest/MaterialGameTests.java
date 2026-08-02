package com.hbm.ntm.gametest;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.CrucibleSmeltingRecipeRuntime;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

/** Source-backed coverage for legacy Mats identity and crucible fallback routing. */
@PrefixGameTestTemplate(false)
public final class MaterialGameTests {
    private MaterialGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(MaterialGameTests.class);
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "materialLibrary")
    public static void legacySmeltableMaterialsReachCrucibleFallback(GameTestHelper helper) {
        assertFallback("ingot_meteorite", Mats.MAT_METEORICIRON);
        assertFallback("ingot_u233", Mats.MAT_U233);
        assertFallback("ingot_u235", Mats.MAT_U235);
        assertFallback("ingot_pu_mix", Mats.MAT_RGP);
        assertFallback("ingot_pu238", Mats.MAT_PU238);
        assertFallback("ingot_pu239", Mats.MAT_PU239);
        assertFallback("ingot_pu240", Mats.MAT_PU240);
        assertFallback("ingot_pu241", Mats.MAT_PU241);
        assertFallback("ingot_am_mix", Mats.MAT_RGA);
        assertFallback("ingot_am241", Mats.MAT_AM241);
        assertFallback("ingot_am242", Mats.MAT_AM242);
        assertFallback("ingot_neptunium", Mats.MAT_NEPTUNIUM);
        assertFallback("ingot_actinium", Mats.MAT_ACTINIUM);
        assertFallback("ingot_co60", Mats.MAT_CO60);
        assertFallback("ingot_au198", Mats.MAT_AU198);
        assertFallback("ingot_pb209", Mats.MAT_PB209);
        assertFallback("ingot_solinium", Mats.MAT_SOLINIUM);
        assertFallback("ingot_schrabidate", Mats.MAT_SCHRABIDATE);
        assertFallback("ingot_schraranium", Mats.MAT_SCHRARANIUM);
        assertFallback("ingot_gh336", Mats.MAT_GHIORSIUM);
        assertFallback("ingot_osmiridium", Mats.MAT_OSMIRIDIUM);
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "materialLibrary")
    public static void legacyIngotRawCarriersRemainMaterialMapped(GameTestHelper helper) {
        assertIngotCarrier("ingot_neodymium", Mats.MAT_NEODYMIUM);
        assertIngotCarrier("ingot_borax", Mats.MAT_BORAX);
        assertIngotCarrier("ingot_sodium", Mats.MAT_SODIUM);
        assertIngotCarrier("ingot_strontium", Mats.MAT_STRONTIUM);
        assertIngotCarrier("ingot_slag", Mats.MAT_SLAG);
        helper.succeed();
    }

    private static void assertFallback(String path, NTMMaterial expectedMaterial) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(HbmNtm.MOD_ID, path));
        if (item == null) {
            throw new AssertionError("missing registered material item: " + path);
        }
        ItemStack input = new ItemStack(item);
        assertSingle(path + " raw identity", Mats.getMaterialsFromItem(input), expectedMaterial);
        assertSingle(path + " crucible fallback", CrucibleSmeltingRecipeRuntime.baseMaterials((net.minecraft.world.item.crafting.RecipeManager) null, input), expectedMaterial);
        assertSingle(path + " smelting fallback", CrucibleSmeltingRecipeRuntime.getSmeltingMaterialsFromItem((net.minecraft.world.item.crafting.RecipeManager) null, input), expectedMaterial);
    }

    private static void assertIngotCarrier(String path, NTMMaterial expectedMaterial) {
        if (!expectedMaterial.autogen.contains(MaterialShapes.INGOT)) {
            throw new AssertionError(path + " is missing legacy INGOT autogen shape");
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(HbmNtm.MOD_ID, path));
        if (item == null) {
            throw new AssertionError("missing registered material item: " + path);
        }
        ItemStack input = new ItemStack(item);
        assertSingle(path + " raw identity", Mats.getMaterialsFromItem(input), expectedMaterial, MaterialShapes.INGOT.q(1));
        assertSingle(path + " crucible fallback", CrucibleSmeltingRecipeRuntime.baseMaterials((net.minecraft.world.item.crafting.RecipeManager) null, input), expectedMaterial, MaterialShapes.INGOT.q(1));
        assertSingle(path + " smelting fallback", CrucibleSmeltingRecipeRuntime.getSmeltingMaterialsFromItem((net.minecraft.world.item.crafting.RecipeManager) null, input), expectedMaterial, MaterialShapes.INGOT.q(1));
    }

    private static void assertSingle(String subject, List<MaterialStack> stacks, NTMMaterial expectedMaterial) {
        if (stacks.size() != 1 || stacks.get(0).material != expectedMaterial) {
            throw new AssertionError(subject + " expected " + expectedMaterial.names[0] + " but got " + stacks);
        }
    }

    private static void assertSingle(String subject, List<MaterialStack> stacks, NTMMaterial expectedMaterial, int expectedAmount) {
        if (stacks.size() != 1 || stacks.get(0).material != expectedMaterial || stacks.get(0).amount != expectedAmount) {
            throw new AssertionError(subject + " expected " + expectedMaterial.names[0] + " x" + expectedAmount + " but got " + stacks);
        }
    }
}
