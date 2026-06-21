package com.hbm.ntm.item;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FoundryMoldItem extends Item {
    private static final String TAG_MOLD = "mold";
    private static final List<Mold> MOLDS = new ArrayList<>();
    private static final Map<Integer, Mold> BY_ID = new LinkedHashMap<>();

    static {
        register(new MoldShape(0, 0, "nugget", MaterialShapes.NUGGET));
        register(new MoldShape(1, 0, "billet", MaterialShapes.BILLET));
        register(new MoldShape(2, 0, "ingot", MaterialShapes.INGOT));
        register(new MoldShape(3, 0, "plate", MaterialShapes.PLATE));
        register(new MoldShape(4, 0, "wire", MaterialShapes.WIRE, 8));
        register(new MoldShape(19, 0, "plate_cast", MaterialShapes.CASTPLATE));
        register(new MoldShape(20, 0, "wire_dense", MaterialShapes.DENSEWIRE));
        register(new MoldShape(8, 0, "shell", MaterialShapes.SHELL));
        register(new MoldShape(9, 0, "pipe", MaterialShapes.PIPE));
        register(new MoldShape(10, 1, "ingots", MaterialShapes.INGOT, 9));
        register(new MoldShape(11, 1, "plates", MaterialShapes.PLATE, 9));
        register(new MoldShape(13, 1, "plates_cast", MaterialShapes.CASTPLATE, 3));
        register(new MoldShape(21, 1, "wires_dense", MaterialShapes.DENSEWIRE, 9));
        register(new MoldShape(12, 1, "block", MaterialShapes.BLOCK));
        register(new MoldShape(22, 0, "barrel_light", MaterialShapes.LIGHTBARREL));
        register(new MoldShape(23, 0, "barrel_heavy", MaterialShapes.HEAVYBARREL));
        register(new MoldShape(24, 0, "receiver_light", MaterialShapes.LIGHTRECEIVER));
        register(new MoldShape(25, 0, "receiver_heavy", MaterialShapes.HEAVYRECEIVER));
        register(new MoldShape(26, 0, "mechanism", MaterialShapes.MECHANISM));
        register(new MoldShape(27, 0, "stock", MaterialShapes.STOCK));
        register(new MoldShape(28, 0, "grip", MaterialShapes.GRIP));
        register(new MoldMapped(5, 0, "blade", MaterialShapes.INGOT.q(3), Map.of(
                "Titanium", "blade_titanium",
                "Tungsten", "blade_tungsten")));
        register(new MoldMapped(6, 0, "blades", MaterialShapes.INGOT.q(4), Map.of(
                "Steel", "blades_steel",
                "Titanium", "blades_titanium")));
        register(new MoldMapped(7, 0, "stamp", MaterialShapes.INGOT.q(4), Map.of(
                "Stone", "stamp_stone_flat",
                "Iron", "stamp_iron_flat",
                "Steel", "stamp_steel_flat",
                "Titanium", "stamp_titanium_flat",
                "Obsidian", "stamp_obsidian_flat")));
        register(new MoldMapped(16, 0, "c9", MaterialShapes.PLATE.q(1, 4), Map.of(
                "Gunmetal", "casing_small",
                "WeaponSteel", "casing_small_steel")));
        register(new MoldMapped(17, 0, "c50", MaterialShapes.PLATE.q(1, 2), Map.of(
                "Gunmetal", "casing_large",
                "WeaponSteel", "casing_large_steel")));
    }

    public FoundryMoldItem(Properties properties) {
        super(properties);
    }

    public static ItemStack stackFor(Mold mold) {
        ItemStack stack = new ItemStack(ModItems.MOLD.get());
        stack.getOrCreateTag().putInt(TAG_MOLD, mold.id());
        return stack;
    }

    public static Mold getMold(ItemStack stack) {
        if (stack.getItem() instanceof FoundryMoldItem) {
            int id = stack.hasTag() ? stack.getTag().getInt(TAG_MOLD) : 0;
            return BY_ID.getOrDefault(id, MOLDS.get(0));
        }
        return null;
    }

    public static boolean isMold(ItemStack stack) {
        return getMold(stack) != null;
    }

    public static void addCreativeStacks(CreativeModeTab.Output output, FoundryMoldItem item) {
        for (Mold mold : MOLDS) {
            output.accept(stackFor(mold));
        }
    }

    private static void register(Mold mold) {
        MOLDS.add(mold);
        BY_ID.put(mold.id(), mold);
    }

    @Override
    public Component getName(ItemStack stack) {
        Mold mold = getMold(stack);
        return mold == null
                ? super.getName(stack)
                : Component.translatable("item.hbm_ntm_rebirth.mold")
                        .append(" - ")
                        .append(mold.title());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Mold mold = getMold(stack);
        if (mold != null) {
            tooltip.add(mold.title().copy().withStyle(ChatFormatting.YELLOW));
        }
    }

    public interface Mold {
        int id();

        int size();

        String name();

        int cost();

        Component title();

        ItemStack getOutput(NTMMaterial material);
    }

    private record MoldShape(int id, int size, String name, MaterialShapes shape, int amount) implements Mold {
        private MoldShape(int id, int size, String name, MaterialShapes shape) {
            this(id, size, name, shape, 1);
        }

        @Override
        public int cost() {
            return shape.q(amount);
        }

        @Override
        public Component title() {
            return Component.translatable("shape." + shape.name()).append(" x" + amount);
        }

        @Override
        public ItemStack getOutput(NTMMaterial material) {
            if (material == null) {
                return ItemStack.EMPTY;
            }
            if (shape == MaterialShapes.BLOCK) {
                ItemStack block = blockOverride(material);
                if (!block.isEmpty()) {
                    return block;
                }
            }
            for (String materialName : material.names) {
                Item item = findItem(shape.name() + "_" + materialName.toLowerCase(Locale.ROOT));
                if (item != Items.AIR) {
                    ItemStack stack = new ItemStack(item, amount);
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }
    }

    private record MoldMapped(int id, int size, String name, int cost, Map<String, String> outputs) implements Mold {
        @Override
        public Component title() {
            return Component.translatable("shape." + name);
        }

        @Override
        public ItemStack getOutput(NTMMaterial material) {
            if (material == null) {
                return ItemStack.EMPTY;
            }
            for (String materialName : material.names) {
                String itemName = outputs.get(materialName);
                if (itemName == null) {
                    continue;
                }
                Item item = findItem(itemName);
                if (item != Items.AIR) {
                    return new ItemStack(item);
                }
            }
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack blockOverride(NTMMaterial material) {
        for (String name : material.names) {
            String lower = name.toLowerCase(Locale.ROOT);
            if ("stone".equals(lower)) {
                return new ItemStack(Items.STONE);
            }
            if ("obsidian".equals(lower)) {
                return new ItemStack(Items.OBSIDIAN);
            }
            Item item = findItem("block_" + lower);
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
        }
        return ItemStack.EMPTY;
    }

    private static Item findItem(String path) {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation("hbm_ntm_rebirth", path));
        return item == null ? Items.AIR : item;
    }
}
