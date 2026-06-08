package com.hbm.ntm.ability;

import com.hbm.ntm.config.ToolConfig;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.Enchantments;

public final class ToolHarvestAbilities {
    public static final IToolHarvestAbility NONE = new BaseHarvestAbility("", 0);
    public static final IToolHarvestAbility SILK = new SilkTouchAbility();
    public static final IToolHarvestAbility LUCK = new LuckAbility();
    public static final IToolHarvestAbility SMELTER = new SmelterAbility();
    public static final IToolHarvestAbility SHREDDER = new ConfigHarvestAbility("tool.ability.shredder", 4, () -> ToolConfig.ABILITY_SHREDDER);
    public static final IToolHarvestAbility CENTRIFUGE = new ConfigHarvestAbility("tool.ability.centrifuge", 5, () -> ToolConfig.ABILITY_CENTRIFUGE);
    public static final IToolHarvestAbility CRYSTALLIZER = new ConfigHarvestAbility("tool.ability.crystallizer", 6, () -> ToolConfig.ABILITY_CRYSTALLIZER);
    public static final IToolHarvestAbility MERCURY = new ConfigHarvestAbility("tool.ability.mercury", 7, () -> ToolConfig.ABILITY_MERCURY);

    public static final IToolHarvestAbility[] ABILITIES = { NONE, SILK, LUCK, SMELTER, SHREDDER, CENTRIFUGE, CRYSTALLIZER, MERCURY };

    public static IToolHarvestAbility getByName(String name) {
        for (IToolHarvestAbility ability : ABILITIES) {
            if (ability.getName().equals(name)) {
                return ability;
            }
        }
        return NONE;
    }

    private static class BaseHarvestAbility implements IToolHarvestAbility {
        private final String name;
        private final int sort;

        private BaseHarvestAbility(String name, int sort) {
            this.name = name;
            this.sort = sort;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int sortOrder() {
            return SORT_ORDER_BASE + sort;
        }
    }

    private static class ConfigHarvestAbility extends BaseHarvestAbility {
        private final Supplier<net.minecraftforge.common.ForgeConfigSpec.BooleanValue> config;

        private ConfigHarvestAbility(String name, int sort, Supplier<net.minecraftforge.common.ForgeConfigSpec.BooleanValue> config) {
            super(name, sort);
            this.config = config;
        }

        @Override
        public boolean isAllowed() {
            return ToolConfig.enabled(config.get());
        }
    }

    private static final class SilkTouchAbility extends ConfigHarvestAbility {
        private SilkTouchAbility() {
            super("tool.ability.silktouch", 1, () -> ToolConfig.ABILITY_SILK);
        }

        @Override
        public void preHarvestAll(int level, ToolHarvestContext context) {
            context.addTemporaryEnchantment(Enchantments.SILK_TOUCH, 1);
        }

        @Override
        public void postHarvestAll(int level, ToolHarvestContext context) {
            context.restoreEnchantments();
        }
    }

    private static final class LuckAbility extends ConfigHarvestAbility {
        private final int[] powerAtLevel = { 1, 2, 3, 4, 5, 9 };

        private LuckAbility() {
            super("tool.ability.luck", 2, () -> ToolConfig.ABILITY_LUCK);
        }

        @Override
        public int levels() {
            return powerAtLevel.length;
        }

        @Override
        public String getExtension(int level) {
            return " (" + powerAtLevel[level] + ")";
        }

        @Override
        public void preHarvestAll(int level, ToolHarvestContext context) {
            context.addTemporaryEnchantment(Enchantments.BLOCK_FORTUNE, powerAtLevel[level]);
        }

        @Override
        public void postHarvestAll(int level, ToolHarvestContext context) {
            context.restoreEnchantments();
        }
    }

    private static final class SmelterAbility extends ConfigHarvestAbility {
        private SmelterAbility() {
            super("tool.ability.smelter", 3, () -> ToolConfig.ABILITY_FURNACE);
        }

        @Override
        public void onHarvestBlock(int level, ToolHarvestContext context) {
            if (!(context.level() instanceof ServerLevel serverLevel)) {
                context.harvestBlock(false);
                return;
            }

            List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(
                    context.state(), serverLevel, context.pos(), context.blockEntity(), context.player(), context.toolStack());
            boolean smeltedAny = false;
            for (int i = 0; i < drops.size(); i++) {
                ItemStack drop = drops.get(i);
                Optional<SmeltingRecipe> recipe = serverLevel.getRecipeManager()
                        .getRecipeFor(RecipeType.SMELTING, new SimpleContainer(drop), serverLevel);
                if (recipe.isPresent()) {
                    ItemStack result = recipe.get().getResultItem(serverLevel.registryAccess()).copy();
                    result.setCount(result.getCount() * drop.getCount());
                    drops.set(i, result);
                    smeltedAny = true;
                }
            }

            context.harvestBlock(smeltedAny);
            if (smeltedAny) {
                drops.forEach(context::drop);
            }
        }
    }

    private ToolHarvestAbilities() {
    }
}
