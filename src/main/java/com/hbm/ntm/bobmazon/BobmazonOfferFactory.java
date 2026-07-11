package com.hbm.ntm.bobmazon;

import com.hbm.ntm.block.ConcreteColoredExtBlock;
import com.hbm.ntm.block.TrinketVariant;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.LegacyStateMultiblockBlockItem;
import com.hbm.ntm.item.TrinketBlockItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.AchievementHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class BobmazonOfferFactory {
    private static List<Offer> standardOffers;

    private BobmazonOfferFactory() {
    }

    public static List<Offer> standardOffers() {
        if (standardOffers == null) {
            standardOffers = List.copyOf(buildStandardOffers());
        }
        return standardOffers;
    }

    public static List<Offer> offersFor(ItemStack stack) {
        if (stack != null && stack.is(ModItems.BOBMAZON.get())) {
            return standardOffers();
        }
        return null;
    }

    private static List<Offer> buildStandardOffers() {
        List<Offer> offers = new ArrayList<>();

        offers.add(new Offer(new ItemStack(Items.TORCH, 64), Requirement.NONE, 2));
        offers.add(new Offer(new ItemStack(ModItems.DEFINITELYFOOD.get(), 16), Requirement.NONE, 4));
        offers.add(new Offer(new ItemStack(item("nitra"), 4), Requirement.CHEMICS, 16));
        offers.add(new Offer(new ItemStack(ModItems.GUN_KIT_1.get()), Requirement.ASSEMBLY, 16));
        offers.add(new Offer(new ItemStack(ModItems.GEIGER_COUNTER.get()), Requirement.NONE, 16));
        offers.add(new Offer(new ItemStack(ModItems.MATCHSTICK.get(), 16), Requirement.STEEL, 2));

        offers.add(new Offer(new ItemStack(ModItems.BLUEPRINT_FOLDER.get()), Requirement.ASSEMBLY, 64));
        offers.add(new Offer(new ItemStack(ModItems.BLUEPRINT_FOLDER_DISCOVER.get()), Requirement.OIL, 256));

        offers.add(new Offer(vendingMachine(0), Requirement.CHEMICS, 64));
        offers.add(new Offer(vendingMachine(1), Requirement.CHEMICS, 64));

        offers.add(new Offer(new ItemStack(Items.JUNGLE_SAPLING), Requirement.STEEL, 12, 9));
        offers.add(new Offer(new ItemStack(ModBlocks.PLANT_FLOWER_FOXGLOVE.get()), Requirement.STEEL, 16, 5));
        offers.add(new Offer(new ItemStack(ModBlocks.PLANT_FLOWER_TOBACCO.get()), Requirement.STEEL, 16, 9));
        offers.add(new Offer(new ItemStack(ModBlocks.PLANT_FLOWER_NIGHTSHADE.get()), Requirement.STEEL, 16, 3));
        offers.add(new Offer(new ItemStack(ModBlocks.PLANT_FLOWER_WEED.get()), Requirement.STEEL, 4, 10));
        offers.add(new Offer(new ItemStack(ModBlocks.PLANT_FLOWER_CD0.get()), Requirement.NUCLEAR, 64, 8));

        for (ConcreteColoredExtBlock.Variant variant : ConcreteColoredExtBlock.Variant.values()) {
            offers.add(new Offer(withCount(concrete(variant.legacyMeta()), 16), Requirement.CHEMICS, 4));
        }

        int snowglobeCount = TrinketVariant.variantCount(TrinketVariant.Kind.SNOWGLOBE);
        for (int variant = 0; variant < snowglobeCount; variant++) {
            offers.add(new Offer(trinket("snowglobe", variant), Requirement.CHEMICS, 128));
        }

        int plushieCount = TrinketVariant.variantCount(TrinketVariant.Kind.PLUSHIE);
        for (int variant = 1; variant < plushieCount; variant++) {
            offers.add(new Offer(trinket("plushie", variant), Requirement.OIL, 16, variant < 3 ? 10 : 0));
        }

        return offers;
    }

    private static Item item(String legacyName) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        if (item == null) {
            throw new IllegalStateException("Missing Bobmazon legacy item: " + legacyName);
        }
        return item.get();
    }

    private static Block block(String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        if (block == null) {
            throw new IllegalStateException("Missing Bobmazon legacy block: " + legacyName);
        }
        return block.get();
    }

    private static ItemStack vendingMachine(int variant) {
        Item item = ModBlocks.VENDING_MACHINE.get().asItem();
        if (item instanceof LegacyStateMultiblockBlockItem stateItem) {
            return LegacyStateMultiblockBlockItem.createStack(stateItem, variant);
        }
        throw new IllegalStateException("vending_machine is not a legacy state multiblock item");
    }

    private static ItemStack concrete(int variant) {
        Item item = ModBlocks.CONCRETE_COLORED_EXT.get().asItem();
        if (item instanceof LegacyStateBlockItem stateItem) {
            return LegacyStateBlockItem.createStack(stateItem, variant);
        }
        throw new IllegalStateException("concrete_colored_ext is not a legacy state block item");
    }

    private static ItemStack trinket(String legacyBlockName, int variant) {
        return TrinketBlockItem.createStack(block(legacyBlockName).asItem(), variant);
    }

    private static ItemStack withCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    public record Offer(ItemStack stack, Requirement requirement, int cost, int ratingBar, String comment,
            String author) {
        public Offer(ItemStack stack, Requirement requirement, int cost) {
            this(stack, requirement, cost, 0);
        }

        public Offer(ItemStack stack, Requirement requirement, int cost, int rating) {
            this(stack, requirement, cost, rating * 4 - 1, "No Ratings", "");
        }

        public Offer {
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
            requirement = requirement == null ? Requirement.NONE : requirement;
            comment = comment == null ? "" : comment;
            author = author == null ? "" : author;
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }

        public int displayedRatingBar() {
            return Math.max(0, Math.min(39, ratingBar));
        }
    }

    public enum Requirement {
        NONE(null, () -> new ItemStack(Items.BOOK)),
        STEEL(AchievementHandler.BLAST_FURNACE, () -> new ItemStack(ModBlocks.MACHINE_BLAST_FURNACE.get())),
        ASSEMBLY(AchievementHandler.ASSEMBLY, () -> new ItemStack(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get())),
        CHEMICS(AchievementHandler.CHEMPLANT, () -> new ItemStack(ModBlocks.MACHINE_CHEMICAL_PLANT.get())),
        OIL(AchievementHandler.DESH, () -> new ItemStack(item("ingot_desh"))),
        NUCLEAR(AchievementHandler.TECHNETIUM, () -> new ItemStack(item("nugget_technetium")));

        private final ResourceLocation advancementId;
        private final Supplier<ItemStack> displayStack;

        Requirement(ResourceLocation advancementId, Supplier<ItemStack> displayStack) {
            this.advancementId = advancementId;
            this.displayStack = displayStack;
        }

        public boolean fulfilledBy(Player player) {
            return advancementId == null || AchievementHandler.has(player, advancementId);
        }

        public ItemStack displayStack() {
            return displayStack.get();
        }

        public ResourceLocation advancementId() {
            return advancementId;
        }
    }
}
