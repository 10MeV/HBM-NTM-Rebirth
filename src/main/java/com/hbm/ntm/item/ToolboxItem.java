package com.hbm.ntm.item;

import com.hbm.ntm.menu.ToolboxMenu;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ToolboxItem extends Item {
    public static final int SLOT_COUNT = 24;
    public static final String OPEN_TAG = "isOpen";
    public static final String RAND_TAG = "rand";
    private static final int HOTBAR_SIZE = 9;
    private static final int TOOLBOX_ROWS = 3;
    private static final int TOOLBOX_ROW_SIZE = 8;
    private static final int MAX_COMPRESSED_NBT_SIZE = 6000;

    public ToolboxItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            if (player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
                openInventory(stack, serverPlayer, hand);
            } else if (!player.isShiftKeyDown()) {
                moveRows(stack, player);
                player.inventoryMenu.broadcastChanges();
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.toolbox.desc.swap"));
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.toolbox.desc.open"));
    }

    public static boolean isOpen(ItemStack stack) {
        return stack != null && stack.hasTag() && stack.getTag().getBoolean(OPEN_TAG);
    }

    public static void closeInventory(ItemStack stack, Player player) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(OPEN_TAG);
        RandomSource random = player == null ? RandomSource.create() : player.level().random;
        tag.putInt(RAND_TAG, random.nextInt());
        if (player != null) {
            player.inventoryMenu.broadcastChanges();
        }
    }

    private static void openInventory(ItemStack stack, ServerPlayer player, InteractionHand hand) {
        stack.getOrCreateTag().putBoolean(OPEN_TAG, true);
        Component title = stack.hasCustomHoverName() ? stack.getHoverName() : Component.translatable("container.toolBox");
        NetworkHooks.openScreen(player,
                new SimpleMenuProvider((containerId, inventory, owner) ->
                        new ToolboxMenu(containerId, inventory, hand), title),
                buffer -> buffer.writeEnum(hand));
    }

    private static void moveRows(ItemStack box, Player player) {
        ItemStack[] endingHotbar = emptyStacks(HOTBAR_SIZE);
        ItemStack[] stacksToTransferToBox = emptyStacks(TOOLBOX_ROW_SIZE);

        boolean hasToolbox = false;
        int extraToolboxes = 0;
        int selected = player.getInventory().selected;

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (isToolbox(slot) && i != selected) {
                extraToolboxes++;
                player.drop(slot.copy(), true);
                player.getInventory().setItem(i, ItemStack.EMPTY);
            } else if (i == selected) {
                hasToolbox = true;
                endingHotbar[i] = box;
            } else {
                int target = i - (hasToolbox ? 1 : 0);
                if (target >= 0 && target < stacksToTransferToBox.length) {
                    stacksToTransferToBox[target] = slot.copy();
                }
            }
        }

        if (extraToolboxes > 0) {
            player.displayClientMessage(Component.literal(extraToolboxes == 1
                    ? "You can't toolbox a toolbox..."
                    : "You can't toolbox a toolbox... (x" + extraToolboxes + ")").withStyle(ChatFormatting.RED),
                    false);
        }

        ItemStack[] stacks = HbmItemStackUtil.readStacksFromNBT(box, SLOT_COUNT);
        ItemStack[] endingStacks = emptyStacks(SLOT_COUNT);

        List<Integer> activeRows = getActiveRows(stacks);
        int lowestActiveIndex = Integer.MAX_VALUE;
        int lowestInactiveIndex = Integer.MAX_VALUE;

        for (int row = 0; row < TOOLBOX_ROWS; row++) {
            if (activeRows.contains(row)) {
                lowestActiveIndex = Math.min(row, lowestActiveIndex);
            } else {
                lowestInactiveIndex = Math.min(row, lowestInactiveIndex);
            }
        }

        if (lowestInactiveIndex > TOOLBOX_ROWS - 1) {
            lowestInactiveIndex = TOOLBOX_ROWS - 1;
        } else {
            lowestInactiveIndex = Math.max(0, lowestInactiveIndex - 1);
        }

        for (int activeRowIndex : activeRows) {
            int activeIndex = TOOLBOX_ROW_SIZE * activeRowIndex;
            if (activeRowIndex == lowestActiveIndex) {
                hasToolbox = false;
                for (int i = 0; i < HOTBAR_SIZE; i++) {
                    if (i == selected) {
                        hasToolbox = true;
                        continue;
                    }
                    endingHotbar[i] = stacks[activeIndex + i - (hasToolbox ? 1 : 0)].copy();
                }
            } else {
                int targetIndex = TOOLBOX_ROW_SIZE * (activeRowIndex - 1);
                System.arraycopy(stacks, activeIndex, endingStacks, targetIndex, TOOLBOX_ROW_SIZE);
            }
        }

        System.arraycopy(stacksToTransferToBox, 0, endingStacks, lowestInactiveIndex * TOOLBOX_ROW_SIZE, TOOLBOX_ROW_SIZE);

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            player.getInventory().setItem(i, endingHotbar[i]);
        }

        HbmItemStackUtil.setStacksToNbt(box, endingStacks, true);
        ejectIfCompressedNbtTooLarge(box, player);
    }

    private static void ejectIfCompressedNbtTooLarge(ItemStack box, Player player) {
        CompoundTag tag = box.getTag();
        if (tag == null || tag.isEmpty()) {
            return;
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            NbtIo.writeCompressed(tag, output);
            if (output.size() <= MAX_COMPRESSED_NBT_SIZE) {
                return;
            }
        } catch (IOException ignored) {
            return;
        }

        player.displayClientMessage(Component.literal(
                "Warning: Container NBT exceeds 6kB, contents will be ejected!")
                .withStyle(ChatFormatting.RED), false);
        ejectContents(box, player);
        box.setTag(new CompoundTag());
    }

    private static void ejectContents(ItemStack box, Player player) {
        Level level = player.level();
        if (level.isClientSide) {
            return;
        }
        RandomSource random = RandomSource.create();
        ItemStack[] contents = HbmItemStackUtil.readStacksFromNBT(box, SLOT_COUNT);
        Vec3 playerMotion = player.getDeltaMovement();
        for (ItemStack stack : contents) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            float xOffset = random.nextFloat() * 0.8F + 0.1F;
            float yOffset = random.nextFloat() * 0.8F + 0.1F;
            float zOffset = random.nextFloat() * 0.8F + 0.1F;
            ItemStack remainder = stack.copy();
            while (!remainder.isEmpty()) {
                int split = Math.min(remainder.getCount(), random.nextInt(21) + 10);
                ItemEntity entity = new ItemEntity(level,
                        player.getX() + xOffset,
                        player.getY() + yOffset,
                        player.getZ() + zOffset,
                        remainder.split(split));
                float motion = 0.05F;
                entity.setDeltaMovement(random.nextGaussian() * motion + playerMotion.x,
                        random.nextGaussian() * motion + 0.2D + playerMotion.y,
                        random.nextGaussian() * motion + playerMotion.z);
                level.addFreshEntity(entity);
            }
        }
    }

    private static boolean isToolbox(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.TOOLBOX.get());
    }

    private static List<Integer> getActiveRows(ItemStack[] stacks) {
        List<Integer> activeRows = new ArrayList<>();
        for (int row = 0; row < TOOLBOX_ROWS; row++) {
            for (int slot = 0; slot < TOOLBOX_ROW_SIZE; slot++) {
                if (!stacks[row * TOOLBOX_ROW_SIZE + slot].isEmpty()) {
                    activeRows.add(row);
                    break;
                }
            }
        }
        return activeRows;
    }

    private static ItemStack[] emptyStacks(int size) {
        ItemStack[] stacks = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            stacks[i] = ItemStack.EMPTY;
        }
        return stacks;
    }
}
