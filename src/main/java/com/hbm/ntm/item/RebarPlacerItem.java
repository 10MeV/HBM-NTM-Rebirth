package com.hbm.ntm.item;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RebarBlockEntity;
import com.hbm.ntm.menu.RebarPlacerMenu;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.HbmItemStackUtil;
import com.hbm.ntm.util.HbmRegistryUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

public class RebarPlacerItem extends Item {
    private static final String TAG_POS = "pos";
    private static final Set<String> VALID_CONCRETE_IDS = Set.of(
            "concrete",
            "concrete_rebar",
            "concrete_smooth",
            "concrete_pillar",
            "concrete_colored",
            "concrete_colored_ext");

    public RebarPlacerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                Component title = stack.hasCustomHoverName()
                        ? stack.getHoverName()
                        : Component.translatable("container.rebar");
                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider((containerId, inventory, owner) ->
                                new RebarPlacerMenu(containerId, inventory, hand), title),
                        buffer -> buffer.writeEnum(hand));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player == null) {
            return InteractionResult.PASS;
        }

        ensureDefaultConcrete(stack);
        ItemStack concreteStack = concretePattern(stack);
        if (!isValidConcrete(concreteStack)) {
            sendStatus(player, "No valid concrete type set!", ChatFormatting.RED);
            return InteractionResult.SUCCESS;
        }

        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_POS, Tag.TAG_INT_ARRAY)) {
            tag.putIntArray(TAG_POS, new int[] {target.getX(), target.getY(), target.getZ()});
            return InteractionResult.SUCCESS;
        }

        int rebarLeft = countRebar(player.getInventory());
        if (rebarLeft <= 0) {
            sendStatus(player, "Out of rebar!", ChatFormatting.RED);
            tag.remove(TAG_POS);
            return InteractionResult.SUCCESS;
        }

        int[] first = tag.getIntArray(TAG_POS);
        if (first.length < 3) {
            tag.remove(TAG_POS);
            return InteractionResult.SUCCESS;
        }

        BlockItem concreteItem = (BlockItem) concreteStack.getItem();
        Block targetConcrete = concreteItem.getBlock();
        BlockState targetConcreteState = concreteItem instanceof LegacyStateBlockItem stateItem
                ? stateItem.stateForStack(concreteStack)
                : targetConcrete.defaultBlockState();
        BlockPos min = new BlockPos(
                Math.min(first[0], target.getX()),
                Math.min(first[1], target.getY()),
                Math.min(first[2], target.getZ()));
        BlockPos max = new BlockPos(
                Math.max(first[0], target.getX()),
                Math.max(first[1], target.getY()),
                Math.max(first[2], target.getZ()));

        int placed = 0;
        outer:
        for (int y = min.getY(); y <= max.getY(); y++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int x = min.getX(); x <= max.getX(); x++) {
                    if (rebarLeft <= 0) {
                        break outer;
                    }
                    BlockPos cursor = new BlockPos(x, y, z);
                    if (level.isOutsideBuildHeight(cursor)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(cursor);
                    if ((state.isAir() || state.canBeReplaced())
                            && player.mayUseItemAt(cursor, context.getClickedFace(), stack)) {
                        level.setBlock(cursor, ModBlocks.legacyBlock("rebar").get().defaultBlockState(),
                                Block.UPDATE_ALL);
                        if (level.getBlockEntity(cursor) instanceof RebarBlockEntity rebar) {
                            rebar.setup(targetConcreteState);
                        }
                        placed++;
                        rebarLeft--;
                    }
                }
            }
        }

        consumeRebar(player.getInventory(), placed);
        sendStatus(player, "Placed " + placed + " rebar!", ChatFormatting.GREEN);
        tag.remove(TAG_POS);
        player.inventoryMenu.broadcastChanges();
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_POS, Tag.TAG_INT_ARRAY)) {
            return;
        }
        if (!isSelected || !isValidConcrete(concretePattern(stack))) {
            stack.getTag().remove(TAG_POS);
        }
    }

    public static boolean isValidConcrete(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        return isValidConcrete(blockItem.getBlock().defaultBlockState());
    }

    public static boolean isValidConcrete(BlockState state) {
        if (state == null) {
            return false;
        }
        var key = HbmRegistryUtil.blockKey(state.getBlock());
        return key != null && HbmNtm.MOD_ID.equals(key.getNamespace()) && VALID_CONCRETE_IDS.contains(key.getPath());
    }

    public static List<ItemStack> acceptableConcreteStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        addConcreteStack(stacks, "concrete");
        addConcreteStack(stacks, "concrete_rebar");
        addConcreteStack(stacks, "concrete_smooth");
        addConcreteStack(stacks, "concrete_pillar");
        addConcreteStack(stacks, "concrete_colored");
        addConcreteStack(stacks, "concrete_colored_ext");
        return List.copyOf(stacks);
    }

    private static void addConcreteStack(List<ItemStack> stacks, String name) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        if (block != null && block.isPresent()) {
            if (block.get().asItem() instanceof LegacyStateBlockItem stateItem) {
                for (int variant = 0; variant < stateItem.getVariants(); variant++) {
                    stacks.add(LegacyStateBlockItem.createStack(stateItem, variant));
                }
            } else {
                stacks.add(new ItemStack(block.get()));
            }
        }
    }

    private static void ensureDefaultConcrete(ItemStack stack) {
        if (!HbmItemStackUtil.hasLegacyItemsTag(stack)) {
            HbmItemStackUtil.addStacksToNbt(stack,
                    new ItemStack(ModBlocks.legacyBlock("concrete_rebar").get()));
        }
    }

    private static ItemStack concretePattern(ItemStack stack) {
        NonNullList<ItemStack> slots = HbmItemStackUtil.readStacksFromNbt(stack, 1);
        return slots.isEmpty() ? ItemStack.EMPTY : slots.get(0);
    }

    private static int countRebar(Inventory inventory) {
        int count = 0;
        Item rebar = ModBlocks.legacyBlock("rebar").get().asItem();
        for (ItemStack slot : inventory.items) {
            if (!slot.isEmpty() && slot.is(rebar)) {
                count += slot.getCount();
            }
        }
        return count;
    }

    private static void consumeRebar(Inventory inventory, int amount) {
        if (amount <= 0) {
            return;
        }
        Item rebar = ModBlocks.legacyBlock("rebar").get().asItem();
        int remaining = amount;
        for (int slot = 0; slot < inventory.items.size() && remaining > 0; slot++) {
            ItemStack stack = inventory.items.get(slot);
            if (stack.isEmpty() || !stack.is(rebar)) {
                continue;
            }
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            if (stack.isEmpty()) {
                inventory.items.set(slot, ItemStack.EMPTY);
            }
            remaining -= consumed;
        }
        inventory.setChanged();
    }

    private void sendStatus(Player player, String message, ChatFormatting color) {
        player.displayClientMessage(Component.literal("[")
                .withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.translatable(getDescriptionId()).withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal(message).withStyle(color)), false);
    }
}
