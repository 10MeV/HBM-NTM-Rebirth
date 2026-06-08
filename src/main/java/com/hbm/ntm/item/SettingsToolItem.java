package com.hbm.ntm.item;

import com.hbm.ntm.fluid.HbmFluidSettingsCopy;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.network.HbmServerKeybinds;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

public class SettingsToolItem extends Item {
    private static final String TAG_COPY_INDEX = "copyIndex";
    private static final String TAG_DISPLAY_INFO = "displayInfo";
    private static final String TAG_INPUT_DELAY = "inputDelay";
    private static final String TAG_TILE_NAME = "tileName";
    private static final int INFORM_ID_BASE = 897;
    private static final int INFORM_MILLIS = 4000;

    public SettingsToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null || !HbmFluidSettingsCopy.copiableAt(level, context.getClickedPos()).isPresent()) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                HbmFluidSettingsCopy.copy(level, context.getClickedPos()).ifPresentOrElse(settings -> {
                    settings.putString(TAG_TILE_NAME, getSettingsSourceId(level, context.getClickedPos()));
                    settings.putInt(TAG_COPY_INDEX, 0);
                    settings.putInt(TAG_INPUT_DELAY, 0);
                    settings.put(TAG_DISPLAY_INFO, makeDisplayInfo(HbmFluidSettingsCopy.displayInfo(level, context.getClickedPos())));
                    stack.setTag(settings);
                    if (player instanceof ServerPlayer serverPlayer) {
                        ModMessages.informPlayer(serverPlayer,
                                Component.translatable("item.hbm_ntm_rebirth.settings_tool.copied",
                                        Component.translatable(settings.getString(TAG_TILE_NAME))),
                                INFORM_ID_BASE - 1, 3000);
                    }
                }, () -> {
                    if (player instanceof ServerPlayer serverPlayer) {
                        ModMessages.informPlayer(serverPlayer,
                                Component.translatable("item.hbm_ntm_rebirth.settings_tool.copy_failed").withStyle(ChatFormatting.RED),
                                INFORM_ID_BASE - 1, 3000);
                    }
                });
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            int index = tag.getInt(TAG_COPY_INDEX);
            boolean recursive = player instanceof ServerPlayer serverPlayer
                    && HbmServerKeybinds.isPressed(serverPlayer, HbmKeybind.TOOL_CTRL);
            boolean pasted = HbmFluidSettingsCopy.paste(level, context.getClickedPos(), tag, index, player, recursive);
            if (player instanceof ServerPlayer serverPlayer) {
                ModMessages.informPlayer(serverPlayer,
                        Component.translatable(pasted
                                ? "item.hbm_ntm_rebirth.settings_tool.pasted"
                                : "item.hbm_ntm_rebirth.settings_tool.paste_failed"),
                        INFORM_ID_BASE - 1, 3000);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !selected || !(entity instanceof ServerPlayer player) || !stack.hasTag()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        ListTag displayInfo = tag.getList(TAG_DISPLAY_INFO, Tag.TAG_STRING);
        if (displayInfo.isEmpty()) {
            return;
        }

        int delay = tag.getInt(TAG_INPUT_DELAY) + 1;
        if (HbmServerKeybinds.isPressed(player, HbmKeybind.TOOL_ALT) && delay > 4) {
            int index = tag.getInt(TAG_COPY_INDEX) + 1;
            if (index > displayInfo.size() - 1) {
                index = 0;
            }
            tag.putInt(TAG_COPY_INDEX, index);
            delay = 0;
        }
        tag.putInt(TAG_INPUT_DELAY, delay);

        if (level.getGameTime() % 5L != 0L) {
            return;
        }
        int selectedIndex = tag.getInt(TAG_COPY_INDEX);
        for (int i = 0; i < displayInfo.size(); i++) {
            ChatFormatting color = selectedIndex == i ? ChatFormatting.AQUA : ChatFormatting.YELLOW;
            ModMessages.informPlayer(player, Component.Serializer.fromJson(displayInfo.getString(i)).withStyle(color),
                    INFORM_ID_BASE + i, INFORM_MILLIS);
        }
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, net.minecraft.core.BlockPos pos, Player player) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.settings_tool.desc1"));
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.settings_tool.desc2"));
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.settings_tool.desc3"));
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_TILE_NAME, Tag.TAG_STRING)) {
            tooltip.add(Component.translatable(tag.getString(TAG_TILE_NAME)).withStyle(ChatFormatting.BLUE));
        } else {
            tooltip.add(Component.translatable("item.hbm_ntm_rebirth.settings_tool.none").withStyle(ChatFormatting.RED));
        }
    }

    private static ListTag makeDisplayInfo(List<Component> lines) {
        ListTag tag = new ListTag();
        for (Component line : lines) {
            tag.add(StringTag.valueOf(Component.Serializer.toJson(line)));
        }
        return tag;
    }

    private static String getSettingsSourceId(Level level, net.minecraft.core.BlockPos pos) {
        return MultiblockHelper.resolveCoreState(level, pos).getBlock().getDescriptionId();
    }
}
