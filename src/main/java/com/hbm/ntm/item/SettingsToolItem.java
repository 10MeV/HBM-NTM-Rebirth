package com.hbm.ntm.item;

import com.hbm.ntm.api.common.CopiableSettings;
import com.hbm.ntm.fluid.HbmFluidSettingsCopy;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.network.HbmServerKeybinds;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SettingsToolItem extends Item {
    private static final String TAG_COPY_INDEX = "copyIndex";
    private static final String TAG_COPY_KIND = "copyKind";
    private static final String TAG_DISPLAY_INFO = "displayInfo";
    private static final String TAG_INPUT_DELAY = "inputDelay";
    private static final String TAG_TILE_NAME = "tileName";
    private static final String COPY_KIND_GENERIC = "copiable";
    private static final String COPY_KIND_FLUID = "fluid";
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
        BlockPos clickedPos = context.getClickedPos();
        boolean fluidCopiable = HbmFluidSettingsCopy.copiableAt(level, clickedPos).isPresent();
        boolean genericCopiable = copiableAt(level, clickedPos).isPresent();
        if (player == null || (!fluidCopiable && !genericCopiable)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                if (fluidCopiable) {
                    copyFluidSettings(level, clickedPos, stack, player);
                } else {
                    copyGenericSettings(level, clickedPos, stack, player);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            int index = tag.getInt(TAG_COPY_INDEX);
            boolean pasted = COPY_KIND_GENERIC.equals(tag.getString(TAG_COPY_KIND))
                    ? pasteGenericSettings(level, clickedPos, tag, index, player)
                    : pasteFluidSettings(level, clickedPos, tag, index, player);
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

    private static void copyFluidSettings(Level level, BlockPos pos, ItemStack stack, Player player) {
        HbmFluidSettingsCopy.copy(level, pos).ifPresentOrElse(settings -> {
            settings.putString(TAG_COPY_KIND, COPY_KIND_FLUID);
            settings.putString(TAG_TILE_NAME, getSettingsSourceId(level, pos));
            settings.putInt(TAG_COPY_INDEX, 0);
            settings.putInt(TAG_INPUT_DELAY, 0);
            settings.put(TAG_DISPLAY_INFO, makeDisplayInfo(HbmFluidSettingsCopy.displayInfo(level, pos)));
            stack.setTag(settings);
            informCopied(player, Component.translatable(settings.getString(TAG_TILE_NAME)));
        }, () -> informCopyFailed(player));
    }

    private static void copyGenericSettings(Level level, BlockPos pos, ItemStack stack, Player player) {
        copiableAt(level, pos).ifPresentOrElse(copiable -> {
            BlockPos corePos = MultiblockHelper.resolveOperationalCorePos(level, pos);
            CompoundTag settings = copiable.getSettings(level, corePos);
            settings.putString(TAG_COPY_KIND, COPY_KIND_GENERIC);
            settings.putString(TAG_TILE_NAME, copiable.getSettingsSourceDisplay(level, corePos).getString());
            settings.putInt(TAG_COPY_INDEX, 0);
            settings.putInt(TAG_INPUT_DELAY, 0);
            settings.put(TAG_DISPLAY_INFO, makeDisplayInfo(copiable.infoForDisplay(level, corePos)));
            stack.setTag(settings);
            informCopied(player, copiable.getSettingsSourceDisplay(level, corePos));
        }, () -> informCopyFailed(player));
    }

    private static boolean pasteFluidSettings(Level level, BlockPos pos, CompoundTag tag, int index, Player player) {
        boolean recursive = player instanceof ServerPlayer serverPlayer
                && HbmServerKeybinds.isPressed(serverPlayer, HbmKeybind.TOOL_CTRL);
        return HbmFluidSettingsCopy.paste(level, pos, tag, index, player, recursive);
    }

    private static boolean pasteGenericSettings(Level level, BlockPos pos, CompoundTag tag, int index, Player player) {
        return copiableAt(level, pos)
                .map(copiable -> {
                    copiable.pasteSettings(tag, index, level, player,
                            MultiblockHelper.resolveOperationalCorePos(level, pos));
                    return true;
                })
                .orElse(false);
    }

    private static Optional<CopiableSettings> copiableAt(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return Optional.empty();
        }
        BlockEntity blockEntity = level.isClientSide
                ? MultiblockHelper.resolveCoreBlockEntity(level, pos)
                : MultiblockHelper.resolveOperationalCoreBlockEntity(level, pos);
        return blockEntity instanceof CopiableSettings copiable ? Optional.of(copiable) : Optional.empty();
    }

    private static void informCopied(Player player, Component sourceName) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModMessages.informPlayer(serverPlayer,
                    Component.translatable("item.hbm_ntm_rebirth.settings_tool.copied", sourceName),
                    INFORM_ID_BASE - 1, 3000);
        }
    }

    private static void informCopyFailed(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModMessages.informPlayer(serverPlayer,
                    Component.translatable("item.hbm_ntm_rebirth.settings_tool.copy_failed").withStyle(ChatFormatting.RED),
                    INFORM_ID_BASE - 1, 3000);
        }
    }

    private static String getSettingsSourceId(Level level, BlockPos pos) {
        BlockState state = MultiblockHelper.resolveOperationalCoreState(level, pos);
        return (state == null ? level.getBlockState(pos) : state).getBlock().getDescriptionId();
    }
}
