package com.hbm.ntm.item;

import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.LegacyBlueprintPools;
import com.hbm.ntm.util.HbmItemStackUtil;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BlueprintFolderItem extends Item {
    private final Kind kind;

    public BlueprintFolderItem(Properties properties, Kind kind) {
        super(properties.stacksTo(1));
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        List<String> pools = matchingPools(level);
        if (pools.isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }

        stack.shrink(1);
        String chosen = pools.get(player.getRandom().nextInt(pools.size()));
        HbmItemStackUtil.giveOrDrop(player, ItemBlueprints.make(chosen));
        return InteractionResultHolder.success(stack);
    }

    private List<String> matchingPools(Level level) {
        Set<String> pools = new LinkedHashSet<>();
        for (GenericMachineRecipe.Machine machine : GenericMachineRecipe.Machine.values()) {
            for (GenericMachineRecipe recipe : GenericMachineRecipeRuntime.recipes(level, machine)) {
                for (String pool : recipe.getPools()) {
                    if (kind.matches(pool)) {
                        pools.add(pool);
                    }
                }
            }
        }
        return new ArrayList<>(pools);
    }

    public enum Kind {
        ALT(LegacyBlueprintPools.PREFIX_ALT),
        DISCOVER(LegacyBlueprintPools.PREFIX_DISCOVER),
        SECRET(LegacyBlueprintPools.PREFIX_SECRET);

        private final String prefix;

        Kind(String prefix) {
            this.prefix = prefix;
        }

        private boolean matches(String pool) {
            return pool != null && pool.startsWith(prefix);
        }
    }
}
