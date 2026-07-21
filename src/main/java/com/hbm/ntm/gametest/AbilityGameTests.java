package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.ItemProcessingRecipeRuntime;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Source-backed regression coverage for harvest abilities that consume stateful block-item recipes. */
@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AbilityGameTests {
    private AbilityGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(AbilityGameTests.class);
    }

    /**
     * 1.7.10 ShredderRecipes maps Sellafield metadata 0..5 to nuclear scrap
     * quantities 1, 2, 3, 5, 7 and 15.  The modern ability bridge carries the
     * same metadata through {@link LegacyStateBlockItem#TAG_VARIANT}.
     */
    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "abilityHarvest")
    public static void sellafieldVariantsResolveToLegacyShredderOutputs(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        if (!(ModBlocks.SELLAFIELD.get().asItem() instanceof LegacyStateBlockItem sellafieldItem)) {
            throw new AssertionError("sellafield must retain LegacyStateBlockItem variant data");
        }

        int[] expectedScrap = {1, 2, 3, 5, 7, 15};
        for (int variant = 0; variant < expectedScrap.length; variant++) {
            ItemStack input = LegacyStateBlockItem.createStack(sellafieldItem, variant);
            ItemProcessingRecipe recipe = ItemProcessingRecipeRuntime.find(level,
                    ItemProcessingRecipe.Machine.SHREDDER, input);
            if (recipe == null) {
                throw new AssertionError("missing sellafield shredder recipe for legacy variant " + variant);
            }
            if (recipe.input().partialNbt().getInt(LegacyStateBlockItem.TAG_VARIANT) != variant) {
                throw new AssertionError("sellafield recipe " + variant + " must retain its legacy variant predicate");
            }
            if (recipe.outputStacks().size() != 1) {
                throw new AssertionError("sellafield recipe " + variant + " must have exactly one output");
            }
            ItemStack output = recipe.outputStacks().get(0);
            if (!output.is(ModItems.legacyItem("scrap_nuclear").get()) || output.getCount() != expectedScrap[variant]) {
                throw new AssertionError("sellafield legacy variant " + variant + " expected scrap_nuclear x"
                        + expectedScrap[variant] + ", got " + output);
            }
        }
        helper.succeed();
    }
}
