package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.FoundryOutletBlock;
import com.hbm.ntm.blockentity.FoundryBaseBlockEntity;
import com.hbm.ntm.blockentity.FoundryCastingBlockEntity;
import com.hbm.ntm.blockentity.FoundryChannelBlockEntity;
import com.hbm.ntm.blockentity.FoundryOutletBlockEntity;
import com.hbm.ntm.blockentity.FoundrySlagBlockEntity;
import com.hbm.ntm.blockentity.FoundryTankBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyIsbrhBlockPlans;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.registry.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FoundryRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final TextureAtlasSprite LAVA = sprite("lava_gray");
    private static final TextureAtlasSprite SLAG = sprite("slag");
    private static final TextureSet BASIN_TEXTURES = simpleTextures("basin");
    private static final TextureSet MOLD_TEXTURES = simpleTextures("mold");
    private static final TextureSet CHANNEL_TEXTURES = simpleTextures("channel");
    private static final TextureSet TANK_TEXTURES = tankTextures();
    private static final TextureSet OUTLET_TEXTURES = outletTextures("outlet", null, null);
    private static final TextureSet SLAGTAP_TEXTURES = outletTextures("slagtap",
            OUTLET_TEXTURES.filter(), OUTLET_TEXTURES.lock());

    public FoundryRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        LegacyBlockEntityRenderCulling.recordMachineSubmission(blockEntity);

        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);

        if (blockEntity instanceof FoundryCastingBlockEntity casting) {
            renderCasting(casting, poseStack, buffer, modelLight, packedLight, packedOverlay);
        } else if (blockEntity instanceof FoundryChannelBlockEntity channel) {
            renderChannel(channel, poseStack, buffer, modelLight, packedOverlay);
        } else if (blockEntity instanceof FoundryTankBlockEntity tank) {
            renderTank(tank, poseStack, buffer, modelLight, packedOverlay);
        } else if (blockEntity instanceof FoundryOutletBlockEntity outlet) {
            renderOutlet(outlet, poseStack, buffer, modelLight, packedOverlay);
        } else if (blockEntity instanceof FoundrySlagBlockEntity slag) {
            renderSlag(slag, poseStack, buffer, packedOverlay);
        }
    }

    private static void renderCasting(FoundryCastingBlockEntity casting, PoseStack poseStack,
            MultiBufferSource buffer, int modelLight, int packedLight, int packedOverlay) {
        LegacyIsbrhBlockPlans.FoundryOpenVesselRenderPlan plan = casting.getMoldSize() == 0
                ? LegacyIsbrhBlockPlans.foundryMoldWorldPlan(0xFFFFFF, false)
                : LegacyIsbrhBlockPlans.foundryBasinWorldPlan(0xFFFFFF, false);
        renderFaces(plan.faces(), plan.colorPlan(), textureSet(plan.kind()), poseStack, buffer, modelLight,
                packedOverlay);
        renderCastingFluid(casting, poseStack, buffer, packedOverlay);
        renderCastingItems(casting, poseStack, buffer, packedLight);
    }

    private static void renderChannel(FoundryChannelBlockEntity channel, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        Level level = channel.getLevel();
        BlockPos pos = channel.getBlockPos();
        boolean posX = canChannelConnect(level, pos, Direction.EAST);
        boolean negX = canChannelConnect(level, pos, Direction.WEST);
        boolean posZ = canChannelConnect(level, pos, Direction.SOUTH);
        boolean negZ = canChannelConnect(level, pos, Direction.NORTH);
        LegacyIsbrhBlockPlans.FoundryChannelRenderPlan plan = LegacyIsbrhBlockPlans.foundryChannelWorldPlan(
                0xFFFFFF, false, posX, negX, posZ, negZ,
                channel.getAmount(), channel.getCapacity(), channel.getMoltenColor());
        TextureSet textures = textureSet("channel");
        renderFaces(plan.shellFaces(), plan.colorPlan(), textures, poseStack, buffer, packedLight, packedOverlay);
        renderFluidSurfaces(plan.fluidSurfaces(), textures, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderTank(FoundryTankBlockEntity tank, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        Level level = tank.getLevel();
        BlockPos pos = tank.getBlockPos();
        boolean posX = isTank(level, pos.relative(Direction.EAST));
        boolean negX = isTank(level, pos.relative(Direction.WEST));
        boolean posZ = isTank(level, pos.relative(Direction.SOUTH));
        boolean negZ = isTank(level, pos.relative(Direction.NORTH));
        boolean posY = isTank(level, pos.above());
        boolean negY = isTank(level, pos.below());
        LegacyIsbrhBlockPlans.FoundryTankRenderPlan plan = LegacyIsbrhBlockPlans.foundryTankWorldPlan(
                0xFFFFFF, false, posX, negX, posZ, negZ, posY, negY,
                isOutletFacing(level, pos.relative(Direction.EAST), Direction.EAST),
                isOutletFacing(level, pos.relative(Direction.WEST), Direction.WEST),
                isOutletFacing(level, pos.relative(Direction.SOUTH), Direction.SOUTH),
                isOutletFacing(level, pos.relative(Direction.NORTH), Direction.NORTH),
                tank.getAmount(), tank.getCapacity(), tank.getMoltenColor());
        TextureSet textures = textureSet("tank");
        renderFaces(plan.shellFaces(), plan.colorPlan(), textures, poseStack, buffer, packedLight, packedOverlay);
        renderFluidSurfaces(plan.fluidSurfaces(), textures, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderOutlet(FoundryOutletBlockEntity outlet, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        BlockState state = outlet.getBlockState();
        Direction facing = state.hasProperty(FoundryOutletBlock.FACING)
                ? state.getValue(FoundryOutletBlock.FACING)
                : Direction.NORTH;
        String kind = state.is(ModBlocks.FOUNDRY_SLAGTAP.get()) ? "slagtap" : "outlet";
        LegacyIsbrhBlockPlans.FoundryOutletRenderPlan plan = LegacyIsbrhBlockPlans.foundryOutletWorldPlan(
                legacyOutletMetadata(facing), outlet.getFilter() != null, outlet.isClosed(), 0xFFFFFF, false);
        renderFaces(plan.faces(), plan.colorPlan(), textureSet(kind), poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderSlag(FoundrySlagBlockEntity slag, PoseStack poseStack, MultiBufferSource buffer,
            int packedOverlay) {
        int color = slag.getMaterialType() == null ? 0xFFFFFF : slag.getMaterialType().moltenColor;
        double height = Math.max(0.0625D, Math.min(1.0D, slag.getFillLevel()));
        LegacyAtlasCuboidRenderer.croppedCuboid(SLAG, poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay,
                color, 255, LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                0.0D, 0.0D, 0.0D, 1.0D, height, 1.0D);
    }

    private static void renderCastingFluid(FoundryCastingBlockEntity casting, PoseStack poseStack,
            MultiBufferSource buffer, int packedOverlay) {
        if (casting.getMaterialType() == null || casting.getAmount() <= 0) {
            return;
        }
        double y = casting.getMoltenLevel();
        if (y <= 0.0D) {
            return;
        }
        double min = 0.125D;
        double max = 0.875D;
        int color = casting.getMaterialType().moltenColor;
        LegacyTexturedQuadRenderer.spriteQuad(LAVA, poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, 0.0F, 1.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(min, y, min, 0.0D, 16.0D, color, 255),
                LegacyTexturedQuadRenderer.spritePixelVertex(min, y, max, 16.0D, 16.0D, color, 255),
                LegacyTexturedQuadRenderer.spritePixelVertex(max, y, max, 16.0D, 0.0D, color, 255),
                LegacyTexturedQuadRenderer.spritePixelVertex(max, y, min, 0.0D, 0.0D, color, 255));
        double highlightY = y + 0.001D;
        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                queuedPose -> LegacyTexturedQuadRenderer.spriteQuad(LAVA, queuedPose, buffer,
                        LightTexture.FULL_BRIGHT, packedOverlay,
                        LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 0.0F, 1.0F, 0.0F,
                        LegacyTexturedQuadRenderer.spritePixelVertex(min, highlightY, min, 0.0D, 16.0D, color, 77),
                        LegacyTexturedQuadRenderer.spritePixelVertex(min, highlightY, max, 16.0D, 16.0D, color, 77),
                        LegacyTexturedQuadRenderer.spritePixelVertex(max, highlightY, max, 16.0D, 0.0D, color, 77),
                        LegacyTexturedQuadRenderer.spritePixelVertex(max, highlightY, min, 0.0D, 0.0D, color, 77)));
    }

    private static void renderCastingItems(FoundryCastingBlockEntity casting, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ItemStack mold = casting.getMoldStack();
        if (!mold.isEmpty()) {
            renderFlatItem(mold, 0.13D, poseStack, buffer, packedLight, casting);
        }
        ItemStack output = casting.getOutputStack();
        if (!output.isEmpty()) {
            renderFlatItem(output, casting.getMoldSize() == 0 ? 0.25D : 0.875D, poseStack, buffer, packedLight,
                    casting);
        }
    }

    private static void renderFlatItem(ItemStack stack, double height, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, BlockEntity blockEntity) {
        poseStack.pushPose();
        poseStack.translate(0.5D, height + 0.015D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.375F, 0.375F, 0.375F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED,
                Math.max(packedLight, LightTexture.FULL_BRIGHT), OverlayTexture.NO_OVERLAY, poseStack, buffer,
                blockEntity.getLevel(), 0);
        poseStack.popPose();
    }

    private static void renderFaces(Iterable<LegacyIsbrhBlockPlans.FoundryFaceDrawPlan> faces,
            LegacyIsbrhBlockPlans.FoundryColorPlan colors, TextureSet textures, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (LegacyIsbrhBlockPlans.FoundryFaceDrawPlan face : faces) {
            int light = face.fullBright() ? LightTexture.FULL_BRIGHT : packedLight;
            renderFace(face, textures.sprite(face.iconRole()), poseStack, buffer, light, packedOverlay,
                    faceColor(colors, face.colorRole()));
        }
    }

    private static void renderFluidSurfaces(Iterable<LegacyIsbrhBlockPlans.FoundryFluidSurfacePlan> surfaces,
            TextureSet textures, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (LegacyIsbrhBlockPlans.FoundryFluidSurfacePlan surface : surfaces) {
            int light = surface.fullBright() ? LightTexture.FULL_BRIGHT : packedLight;
            LegacyAtlasCuboidRenderer.CuboidBounds bounds = surface.bounds();
            if (bounds == null) {
                continue;
            }
            LegacyAtlasCuboidRenderer.croppedCuboid(textures.sprite(surface.iconRole()), poseStack, buffer,
                    light, packedOverlay, surface.color(), 255, LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                    bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ());
        }
    }

    private static void renderFace(LegacyIsbrhBlockPlans.FoundryFaceDrawPlan face, TextureAtlasSprite sprite,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, int color) {
        LegacyAtlasCuboidRenderer.CuboidBounds bounds = face.boundsOverride();
        double minX = bounds == null ? 0.0D : bounds.minX();
        double minY = bounds == null ? 0.0D : bounds.minY();
        double minZ = bounds == null ? 0.0D : bounds.minZ();
        double maxX = bounds == null ? 1.0D : bounds.maxX();
        double maxY = bounds == null ? 1.0D : bounds.maxY();
        double maxZ = bounds == null ? 1.0D : bounds.maxZ();
        minX += face.offsetX();
        maxX += face.offsetX();
        minY += face.offsetY();
        maxY += face.offsetY();
        minZ += face.offsetZ();
        maxZ += face.offsetZ();

        switch (face.direction()) {
            case UP -> LegacyTexturedQuadRenderer.spriteQuad(sprite, poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL, 0.0F, 1.0F, 0.0F,
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, minZ, 16.0D, 0.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, minZ, 0.0D, 0.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, maxZ, 0.0D, 16.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, maxZ, 16.0D, 16.0D, color, 255));
            case DOWN -> LegacyTexturedQuadRenderer.spriteQuad(sprite, poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL, 0.0F, -1.0F, 0.0F,
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, minZ, 16.0D, 0.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, minZ, 0.0D, 0.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, maxZ, 0.0D, 16.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, maxZ, 16.0D, 16.0D, color, 255));
            case SOUTH -> LegacyTexturedQuadRenderer.spriteQuad(sprite, poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL, 0.0F, 0.0F, 1.0F,
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, maxZ, 16.0D, 0.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, maxZ, 0.0D, 0.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, maxZ, 0.0D, 16.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, maxZ, 16.0D, 16.0D, color, 255));
            case NORTH -> LegacyTexturedQuadRenderer.spriteQuad(sprite, poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL, 0.0F, 0.0F, -1.0F,
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, minZ, 16.0D, 0.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, minZ, 0.0D, 0.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, minZ, 0.0D, 16.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, minZ, 16.0D, 16.0D, color, 255));
            case EAST -> LegacyTexturedQuadRenderer.spriteQuad(sprite, poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL, 1.0F, 0.0F, 0.0F,
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, maxZ, 16.0D, 0.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, minZ, 0.0D, 0.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, minZ, 0.0D, 16.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, maxZ, 16.0D, 16.0D, color, 255));
            case WEST -> LegacyTexturedQuadRenderer.spriteQuad(sprite, poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL, -1.0F, 0.0F, 0.0F,
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, minZ, 16.0D, 0.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, maxZ, 0.0D, 0.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, maxZ, 0.0D, 16.0D, color, 255),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, minZ, 16.0D, 16.0D, color, 255));
        }
    }

    private static int faceColor(LegacyIsbrhBlockPlans.FoundryColorPlan colors, String role) {
        float multiplier = switch (role) {
            case "bottom" -> colors.bottomMultiplier();
            case "x" -> colors.xMultiplier();
            case "z" -> colors.zMultiplier();
            default -> colors.topMultiplier();
        };
        return clamp(colors.red() * multiplier) << 16
                | clamp(colors.green() * multiplier) << 8
                | clamp(colors.blue() * multiplier);
    }

    private static int clamp(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255.0F)));
    }

    private static boolean canChannelConnect(Level level, BlockPos pos, Direction direction) {
        if (level == null) {
            return false;
        }
        BlockPos target = pos.relative(direction);
        BlockState state = level.getBlockState(target);
        if ((state.is(ModBlocks.FOUNDRY_OUTLET.get()) || state.is(ModBlocks.FOUNDRY_SLAGTAP.get()))
                && state.hasProperty(FoundryOutletBlock.FACING)) {
            return state.getValue(FoundryOutletBlock.FACING) == direction;
        }
        return state.is(ModBlocks.FOUNDRY_CHANNEL.get()) || state.is(ModBlocks.FOUNDRY_MOLD.get());
    }

    private static boolean isTank(Level level, BlockPos pos) {
        return level != null && level.getBlockState(pos).is(ModBlocks.FOUNDRY_TANK.get());
    }

    private static boolean isOutletFacing(Level level, BlockPos pos, Direction direction) {
        if (level == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return (state.is(ModBlocks.FOUNDRY_OUTLET.get()) || state.is(ModBlocks.FOUNDRY_SLAGTAP.get()))
                && state.hasProperty(FoundryOutletBlock.FACING)
                && state.getValue(FoundryOutletBlock.FACING) == direction;
    }

    private static int legacyOutletMetadata(Direction direction) {
        return switch (direction) {
            case EAST -> 4;
            case WEST -> 5;
            case SOUTH -> 2;
            default -> 3;
        };
    }

    private static TextureSet textureSet(String kind) {
        return switch (kind) {
            case "basin" -> BASIN_TEXTURES;
            case "mold" -> MOLD_TEXTURES;
            case "channel" -> CHANNEL_TEXTURES;
            case "tank" -> TANK_TEXTURES;
            case "slagtap" -> SLAGTAP_TEXTURES;
            default -> OUTLET_TEXTURES;
        };
    }

    private static TextureAtlasSprite sprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID, "block/" + name));
    }

    private static TextureSet simpleTextures(String kind) {
        return new TextureSet(
                sprite("foundry_" + kind + "_top"),
                sprite("foundry_" + kind + "_bottom"),
                sprite("foundry_" + kind + "_inner"),
                sprite("foundry_" + kind + "_side"),
                null, null, null, null, null, null, LAVA);
    }

    private static TextureSet tankTextures() {
        return new TextureSet(
                sprite("foundry_tank_top"),
                sprite("foundry_tank_bottom"),
                sprite("foundry_tank_inner"),
                sprite("foundry_tank_side"),
                sprite("foundry_tank_side_outlet"),
                sprite("foundry_tank_upper"),
                sprite("foundry_tank_upper_outlet"),
                null, null, null, LAVA);
    }

    private static TextureSet outletTextures(String kind, TextureAtlasSprite filterFallback,
            TextureAtlasSprite lockFallback) {
        return new TextureSet(
                sprite("foundry_" + kind + "_top"),
                sprite("foundry_" + kind + "_bottom"),
                sprite("foundry_" + kind + "_inner"),
                sprite("foundry_" + kind + "_side"),
                null, null, null,
                sprite("foundry_" + kind + "_front"),
                filterFallback == null ? sprite("foundry_" + kind + "_filter") : filterFallback,
                lockFallback == null ? sprite("foundry_" + kind + "_lock") : lockFallback,
                LAVA);
    }

    private record TextureSet(TextureAtlasSprite top, TextureAtlasSprite bottom, TextureAtlasSprite inner,
            TextureAtlasSprite side, TextureAtlasSprite sideOutlet, TextureAtlasSprite sideUpper,
            TextureAtlasSprite sideUpperOutlet, TextureAtlasSprite front, TextureAtlasSprite filter,
            TextureAtlasSprite lock, TextureAtlasSprite lava) {
        TextureAtlasSprite sprite(String role) {
            return switch (role) {
                case "iconBottom" -> bottom;
                case "iconInner" -> inner;
                case "iconSide" -> side;
                case "iconSideOutlet" -> sideOutlet == null ? top : sideOutlet;
                case "iconSideUpper" -> sideUpper == null ? top : sideUpper;
                case "iconSideUpperOutlet" -> sideUpperOutlet == null ? top : sideUpperOutlet;
                case "iconFront" -> front == null ? top : front;
                case "iconFilter" -> filter == null ? top : filter;
                case "iconLock" -> lock == null ? top : lock;
                case "iconLava" -> lava;
                default -> top;
            };
        }
    }
}
