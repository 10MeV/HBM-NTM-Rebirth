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
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjRenderContext;
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

    public FoundryRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state,
                LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight), packedOverlay);

        if (blockEntity instanceof FoundryCastingBlockEntity casting) {
            renderCasting(casting, context, poseStack, buffer, packedLight);
        } else if (blockEntity instanceof FoundryChannelBlockEntity channel) {
            renderChannel(channel, context);
        } else if (blockEntity instanceof FoundryTankBlockEntity tank) {
            renderTank(tank, context);
        } else if (blockEntity instanceof FoundryOutletBlockEntity outlet) {
            renderOutlet(outlet, context);
        } else if (blockEntity instanceof FoundrySlagBlockEntity slag) {
            renderSlag(slag, context);
        }
    }

    private static void renderCasting(FoundryCastingBlockEntity casting, ObjRenderContext context, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        LegacyIsbrhBlockPlans.FoundryOpenVesselRenderPlan plan = casting.getMoldSize() == 0
                ? LegacyIsbrhBlockPlans.foundryMoldWorldPlan(0xFFFFFF, false)
                : LegacyIsbrhBlockPlans.foundryBasinWorldPlan(0xFFFFFF, false);
        renderFaces(plan.faces(), plan.colorPlan(), textureSet(plan.kind()), context);
        renderCastingFluid(casting, context);
        renderCastingItems(casting, poseStack, buffer, packedLight);
    }

    private static void renderChannel(FoundryChannelBlockEntity channel, ObjRenderContext context) {
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
        renderFaces(plan.shellFaces(), plan.colorPlan(), textures, context);
        renderFluidSurfaces(plan.fluidSurfaces(), textures, context);
    }

    private static void renderTank(FoundryTankBlockEntity tank, ObjRenderContext context) {
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
        renderFaces(plan.shellFaces(), plan.colorPlan(), textures, context);
        renderFluidSurfaces(plan.fluidSurfaces(), textures, context);
    }

    private static void renderOutlet(FoundryOutletBlockEntity outlet, ObjRenderContext context) {
        BlockState state = outlet.getBlockState();
        Direction facing = state.hasProperty(FoundryOutletBlock.FACING)
                ? state.getValue(FoundryOutletBlock.FACING)
                : Direction.NORTH;
        String kind = state.is(ModBlocks.FOUNDRY_SLAGTAP.get()) ? "slagtap" : "outlet";
        LegacyIsbrhBlockPlans.FoundryOutletRenderPlan plan = LegacyIsbrhBlockPlans.foundryOutletWorldPlan(
                legacyOutletMetadata(facing), outlet.getFilter() != null, outlet.isClosed(), 0xFFFFFF, false);
        renderFaces(plan.faces(), plan.colorPlan(), textureSet(kind), context);
    }

    private static void renderSlag(FoundrySlagBlockEntity slag, ObjRenderContext context) {
        int color = slag.getMaterialType() == null ? 0xFFFFFF : slag.getMaterialType().moltenColor;
        double height = Math.max(0.0625D, Math.min(1.0D, slag.getFillLevel()));
        LegacyAtlasCuboidRenderer.croppedCuboid(SLAG, context.fullBright().withColor(color),
                0.0D, 0.0D, 0.0D, 1.0D, height, 1.0D);
    }

    private static void renderCastingFluid(FoundryCastingBlockEntity casting, ObjRenderContext context) {
        if (casting.getMaterialType() == null || casting.getAmount() <= 0) {
            return;
        }
        double y = casting.getMoltenLevel();
        if (y <= 0.0D) {
            return;
        }
        double min = 0.125D;
        double max = 0.875D;
        ObjRenderContext fluidContext = context.fullBright().withColor(casting.getMaterialType().moltenColor);
        LegacyTexturedQuadRenderer.spriteQuad(LAVA, fluidContext, 0.0F, 1.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(min, y, min, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(min, y, max, 16.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(max, y, max, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(max, y, min, 0.0D, 0.0D));
        LegacyTexturedQuadRenderer.spriteQuad(LAVA, fluidContext.withAdditiveTranslucency().withAlpha(77),
                0.0F, 1.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(min, y + 0.001D, min, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(min, y + 0.001D, max, 16.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(max, y + 0.001D, max, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(max, y + 0.001D, min, 0.0D, 0.0D));
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
            LegacyIsbrhBlockPlans.FoundryColorPlan colors, TextureSet textures, ObjRenderContext context) {
        for (LegacyIsbrhBlockPlans.FoundryFaceDrawPlan face : faces) {
            ObjRenderContext faceContext = context.withColor(faceColor(colors, face.colorRole()));
            if (face.fullBright()) {
                faceContext = faceContext.fullBright();
            }
            renderFace(face, textures.sprite(face.iconRole()), faceContext);
        }
    }

    private static void renderFluidSurfaces(Iterable<LegacyIsbrhBlockPlans.FoundryFluidSurfacePlan> surfaces,
            TextureSet textures, ObjRenderContext context) {
        for (LegacyIsbrhBlockPlans.FoundryFluidSurfacePlan surface : surfaces) {
            ObjRenderContext fluidContext = context.withColor(surface.color());
            if (surface.fullBright()) {
                fluidContext = fluidContext.fullBright();
            }
            LegacyAtlasCuboidRenderer.croppedCuboid(textures.sprite(surface.iconRole()), fluidContext, surface.bounds());
        }
    }

    private static void renderFace(LegacyIsbrhBlockPlans.FoundryFaceDrawPlan face, TextureAtlasSprite sprite,
            ObjRenderContext context) {
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
            case UP -> LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, 1.0F, 0.0F,
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, minZ, 16.0D, 0.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, minZ, 0.0D, 0.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, maxZ, 0.0D, 16.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, maxZ, 16.0D, 16.0D));
            case DOWN -> LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, -1.0F, 0.0F,
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, minZ, 16.0D, 0.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, minZ, 0.0D, 0.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, maxZ, 0.0D, 16.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, maxZ, 16.0D, 16.0D));
            case SOUTH -> LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, 0.0F, 1.0F,
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, maxZ, 16.0D, 0.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, maxZ, 0.0D, 0.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, maxZ, 0.0D, 16.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, maxZ, 16.0D, 16.0D));
            case NORTH -> LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, 0.0F, -1.0F,
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, minZ, 16.0D, 0.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, minZ, 0.0D, 0.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, minZ, 0.0D, 16.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, minZ, 16.0D, 16.0D));
            case EAST -> LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 1.0F, 0.0F, 0.0F,
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, maxZ, 16.0D, 0.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, minZ, 0.0D, 0.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, minZ, 0.0D, 16.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, maxZ, 16.0D, 16.0D));
            case WEST -> LegacyTexturedQuadRenderer.spriteQuad(sprite, context, -1.0F, 0.0F, 0.0F,
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, minZ, 16.0D, 0.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, maxZ, 0.0D, 0.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, maxZ, 0.0D, 16.0D),
                    LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, minZ, 16.0D, 16.0D));
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
        return new TextureSet(kind);
    }

    private static TextureAtlasSprite sprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID, "block/" + name));
    }

    private record TextureSet(String kind) {
        TextureAtlasSprite sprite(String role) {
            return switch (role) {
                case "iconBottom" -> sprite("foundry_" + kind + "_bottom");
                case "iconInner" -> sprite("foundry_" + kind + "_inner");
                case "iconSide" -> sprite("foundry_" + kind + "_side");
                case "iconSideOutlet" -> sprite("foundry_" + kind + "_side_outlet");
                case "iconSideUpper" -> sprite("foundry_" + kind + "_upper");
                case "iconSideUpperOutlet" -> sprite("foundry_" + kind + "_upper_outlet");
                case "iconFront" -> sprite("foundry_" + kind + "_front");
                case "iconFilter" -> sprite("foundry_" + kind + "_filter");
                case "iconLock" -> sprite("foundry_" + kind + "_lock");
                case "iconLava" -> LAVA;
                default -> sprite("foundry_" + kind + "_top");
            };
        }
    }
}
