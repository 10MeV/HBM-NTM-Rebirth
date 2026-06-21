package com.hbm.ntm.client.screen;

import com.hbm.ntm.network.packet.PWRPrinterSnapshotPacket;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PWRSlicePrinterScreen extends Screen {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private final BlockPos min;
    private final BlockPos max;
    private final Direction direction;
    private final List<BlockState> states;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final String directoryName;
    private int yIndex;
    private boolean closing;

    public PWRSlicePrinterScreen(PWRPrinterSnapshotPacket packet) {
        super(Component.translatableWithFallback("screen.hbm_ntm_rebirth.pwr_printer", "PWR Slice Printer"));
        this.min = new BlockPos(Math.min(packet.min().getX(), packet.max().getX()),
                Math.min(packet.min().getY(), packet.max().getY()),
                Math.min(packet.min().getZ(), packet.max().getZ()));
        this.max = new BlockPos(Math.max(packet.min().getX(), packet.max().getX()),
                Math.max(packet.min().getY(), packet.max().getY()),
                Math.max(packet.min().getZ(), packet.max().getZ()));
        this.direction = packet.direction();
        this.states = packet.states();
        this.sizeX = this.max.getX() - this.min.getX() + 1;
        this.sizeY = this.max.getY() - this.min.getY() + 1;
        this.sizeZ = this.max.getZ() - this.min.getZ() + 1;
        this.directoryName = DATE_FORMAT.format(new Date());
    }

    public static void open(PWRPrinterSnapshotPacket packet) {
        Minecraft.getInstance().setScreen(new PWRSlicePrinterScreen(packet));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFFFF00FF);
        if (closing) {
            return;
        }
        if (yIndex >= sizeY) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.literal("Slices saved to: .minecraft/printer/"
                        + directoryName), false);
            }
            closing = true;
            onClose();
            return;
        }
        renderSlice(graphics);
        graphics.flush();
        saveSlice();
        yIndex++;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderSlice(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        graphics.pose().pushPose();
        graphics.pose().translate(width / 2.0F, height / 2.0F - 36.0F, 400.0F);
        graphics.pose().scale(-24.0F, -24.0F, -12.0F);
        graphics.pose().mulPose(Axis.XP.rotationDegrees(-30.0F));
        graphics.pose().mulPose(Axis.YP.rotationDegrees(225.0F));
        if (direction == Direction.WEST || direction == Direction.EAST) {
            graphics.pose().translate(sizeX / -2.0D, sizeY / -2.0D, sizeZ / -2.0D);
        } else {
            graphics.pose().translate(sizeZ / -2.0D, sizeY / -2.0D, sizeX / -2.0D);
        }

        RenderSystem.enableDepthTest();
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                BlockState state = stateAt(x, yIndex, z);
                if (state.isAir()) {
                    continue;
                }
                int dx = x;
                int dz = z;
                if (direction == Direction.WEST) {
                    dx = sizeZ - 1 - z;
                    dz = x;
                } else if (direction == Direction.SOUTH) {
                    dx = sizeX - 1 - x;
                    dz = sizeZ - 1 - z;
                } else if (direction == Direction.EAST) {
                    dx = z;
                    dz = sizeX - 1 - x;
                }
                graphics.pose().pushPose();
                graphics.pose().translate(dx, 0.0D, dz);
                dispatcher.renderSingleBlock(state, graphics.pose(), graphics.bufferSource(),
                        LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                graphics.pose().popPose();
            }
        }
        graphics.pose().popPose();
    }

    private BlockState stateAt(int x, int y, int z) {
        int index = (x * sizeY + y) * sizeZ + z;
        return index >= 0 && index < states.size() ? states.get(index) : Blocks.AIR.defaultBlockState();
    }

    private void saveSlice() {
        if (minecraft == null) {
            return;
        }
        File directory = new File(new File(minecraft.gameDirectory, "printer"), directoryName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File output = new File(directory, "slice_" + yIndex + ".png");
        try (NativeImage image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
            image.writeToFile(output);
        } catch (IOException exception) {
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.literal("Failed to save PWR slice: "
                        + output.getPath()), false);
            }
        }
    }
}
