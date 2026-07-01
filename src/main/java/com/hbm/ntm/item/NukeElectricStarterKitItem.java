package com.hbm.ntm.item;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.util.HbmInventoryUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class NukeElectricStarterKitItem extends Item {
    public NukeElectricStarterKitItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        giveContents(player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_UNPACK.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        player.getInventory().setChanged();
        stack.shrink(1);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Please empty inventory before opening!").withStyle(ChatFormatting.YELLOW));
    }

    private static void giveContents(Player player) {
        give(player, new ItemStack(ModItems.COPPER_COIL.get(), 16));
        give(player, new ItemStack(ModItems.GOLD_COIL.get(), 8));
        give(player, new ItemStack(ModItems.TUNGSTEN_COIL.get(), 8));
        give(player, new ItemStack(ModItems.MOTOR.get(), 4));
        give(player, legacyItem("circuit_vacuum_tube", 16));
        give(player, legacyItem("circuit_capacitor", 16));
        give(player, legacyItem("circuit_basic", 16));
        give(player, new ItemStack(ModItems.WIRING_RED_COPPER.get(), 1));
        give(player, legacyItem("magnetron", 5));
        give(player, new ItemStack(ModItems.PISTON_SELENIUM.get(), 1));
        give(player, new ItemStack(ModItems.PISTON_SELENIUM.get(), 1));
        give(player, new ItemStack(ModItems.PISTON_SELENIUM.get(), 1));
        give(player, canister(HbmFluids.DIESEL, 16));
        give(player, canister(HbmFluids.BIOFUEL, 16));
        give(player, new ItemStack(ModItems.BATTERY_POTATO.get(), 1));
        give(player, new ItemStack(ModItems.SCREWDRIVER.get(), 1));
        give(player, new ItemStack(ModBlocks.MACHINE_EXCAVATOR.get(), 1));
        give(player, new ItemStack(ModBlocks.MACHINE_DIESEL.get(), 2));
        give(player, new ItemStack(ModBlocks.RED_CABLE.get(), 64));
        give(player, new ItemStack(ModBlocks.RED_WIRE_COATED.get(), 16));
        give(player, new ItemStack(ModBlocks.RED_PYLON.get(), 8));
        give(player, new ItemStack(ModBlocks.MACHINE_BATTERY_SOCKET.get(), 4));
        give(player, new ItemStack(ModItems.BATTERY_LEAD.get(), 4));
    }

    private static ItemStack canister(FluidType fluid, int count) {
        HbmFluidContainerItem item = (HbmFluidContainerItem) ModItems.CANISTER_FULL.get();
        ItemStack stack = item.createFilledStack(fluid);
        stack.setCount(count);
        return stack;
    }

    private static ItemStack legacyItem(String name, int count) {
        return new ItemStack(ModItems.legacyItem(name).get(), count);
    }

    private static void give(Player player, ItemStack stack) {
        ItemStack remainder = HbmInventoryUtil.tryAddItemToInventory(player.getInventory(), stack);
        if (!remainder.isEmpty()) {
            player.drop(remainder, false);
        }
    }
}
