package com.hbm.ntm.client.overlay;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayBlockProvider;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.entity.LegacyLookOverlayEntityProvider;
import com.hbm.ntm.api.item.LegacyLookOverlayItemProvider;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

import java.util.ArrayList;
import java.util.List;

public final class LegacyLookOverlayRenderer {
    public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.screen != null) {
            return;
        }
        if (!HbmClientConfig.LEGACY_LOOK_OVERLAY.get()) {
            return;
        }
        HitResult hitResult = minecraft.hitResult;
        if (hitResult == null) {
            return;
        }

        LegacyLookOverlay overlay = resolveOverlay(minecraft, hitResult);
        if (overlay == null) {
            return;
        }

        renderPanel(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(),
                event.getWindow().getGuiScaledHeight(), overlay);
    }

    private static LegacyLookOverlay resolveOverlay(Minecraft minecraft, HitResult hitResult) {
        if (hitResult instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            BlockState state = minecraft.level.getBlockState(blockHit.getBlockPos());
            if (HbmClientConfig.SHOW_BLOCK_STATE_OVERLAY.get()) {
                return debugBlockStateOverlay(state);
            }
            LegacyLookOverlay overlay = resolveHeldItemOverlay(minecraft, blockHit);
            if (overlay == null) {
                overlay = resolveBlockOverlay(minecraft, blockHit, state);
            }
            if (overlay == null) {
                overlay = resolveBlockEntityOverlay(minecraft, blockHit);
            }
            return overlay;
        }
        if (hitResult instanceof EntityHitResult entityHit && entityHit.getType() == HitResult.Type.ENTITY) {
            return resolveEntityOverlay(minecraft, entityHit);
        }
        return null;
    }

    private static LegacyLookOverlay debugBlockStateOverlay(BlockState state) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(String.valueOf(ForgeRegistries.BLOCKS.getKey(state.getBlock()))));
        state.getValues().forEach((property, value) ->
                lines.add(Component.literal(property.getName() + ": " + value)));
        return LegacyLookOverlay.withTitle(Component.literal("DEBUG"), 0xFFFF00, 0x404000, lines);
    }

    private static LegacyLookOverlay resolveHeldItemOverlay(Minecraft minecraft, BlockHitResult hit) {
        ItemStack stack = minecraft.player.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof LegacyLookOverlayItemProvider provider) {
            return provider.getLookOverlay(minecraft.level, minecraft.player, stack, hit);
        }
        return null;
    }

    private static LegacyLookOverlay resolveBlockOverlay(Minecraft minecraft, BlockHitResult hit, BlockState state) {
        if (state.getBlock() instanceof LegacyLookOverlayBlockProvider provider) {
            return provider.getLookOverlay(minecraft.level, minecraft.player, hit.getBlockPos(), state);
        }
        return null;
    }

    private static LegacyLookOverlay resolveBlockEntityOverlay(Minecraft minecraft, BlockHitResult hit) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(minecraft.level, hit.getBlockPos());
        if (!(blockEntity instanceof LegacyLookOverlayProvider provider)) {
            return null;
        }
        return provider.getLookOverlay(minecraft.level, minecraft.player, hit.getBlockPos());
    }

    private static LegacyLookOverlay resolveEntityOverlay(Minecraft minecraft, EntityHitResult hit) {
        Entity entity = hit.getEntity();
        if (entity instanceof LegacyLookOverlayEntityProvider provider) {
            return provider.getLookOverlay(minecraft.level, minecraft.player, hit);
        }
        return null;
    }

    private static void renderPanel(GuiGraphics graphics, int width, int height, LegacyLookOverlay overlay) {
        Font font = Minecraft.getInstance().font;
        int x = width / 2 + 8;
        int y = height / 2;
        graphics.drawString(font, overlay.title(), x + 1, y - 9, overlay.titleShadowColor(), false);
        graphics.drawString(font, overlay.title(), x, y - 10, overlay.titleColor(), false);
        for (net.minecraft.network.chat.Component line : overlay.lines()) {
            graphics.drawString(font, line, x, y, 0xFFFFFF, true);
            y += 10;
        }
    }

    private LegacyLookOverlayRenderer() {
    }
}
