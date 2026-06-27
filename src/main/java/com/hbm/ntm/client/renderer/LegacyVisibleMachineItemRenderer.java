package com.hbm.ntm.client.renderer;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.CargoElevatorBlock;
import com.hbm.ntm.block.CraneSplitterBlock;
import com.hbm.ntm.block.CustomMissileLauncherBlock;
import com.hbm.ntm.block.ElectricPressBlock;
import com.hbm.ntm.block.FusionMachineBlock;
import com.hbm.ntm.block.LegacyConnectorBlock;
import com.hbm.ntm.block.LegacyLargePylonBlock;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachineRenderProfile;
import com.hbm.ntm.block.LegacyMediumPylonBlock;
import com.hbm.ntm.block.LegacySmallPylonBlock;
import com.hbm.ntm.block.LegacySubstationBlock;
import com.hbm.ntm.block.LegacyVisibleMachineBlock;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.block.LargeLaunchPadBlock;
import com.hbm.ntm.block.LaunchPadBlock;
import com.hbm.ntm.block.RadioAutocalBlock;
import com.hbm.ntm.block.RadioTelexBlock;
import com.hbm.ntm.block.RustedLaunchPadBlock;
import com.hbm.ntm.block.AssemblyMachineBlock;
import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.block.MachineLpw2Block;
import com.hbm.ntm.block.MissileAssemblyBlock;
import com.hbm.ntm.block.ParticleAcceleratorBlock;
import com.hbm.ntm.block.PressMachineBlock;
import com.hbm.ntm.block.RBMKAutoloaderBlock;
import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.block.RBMKConsoleBlock;
import com.hbm.ntm.block.RBMKCraneConsoleBlock;
import com.hbm.ntm.block.AutosawBlock;
import com.hbm.ntm.block.RadioboxBlock;
import com.hbm.ntm.block.RadioReceiverBlock;
import com.hbm.ntm.block.RefuelerBlock;
import com.hbm.ntm.block.StorageDrumBlock;
import com.hbm.ntm.block.ThresherBlock;
import com.hbm.ntm.block.VendingMachineBlock;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.LegacyStateMultiblockBlockItem;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.hbm.ntm.client.obj.ObjRbmkModels;
import com.hbm.ntm.client.obj.ObjLaunchModels;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjNetworkModels;
import com.hbm.ntm.client.obj.ObjParticleAcceleratorModels;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.neutron.RBMKStructureDimensions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class LegacyVisibleMachineItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float LEGACY_GUI_SLOT_PIXELS = 16.0F;
    private static final float LEGACY_GUI_MAX_OCCUPANCY = 0.86F;
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();

    public static final LegacyVisibleMachineItemRenderer INSTANCE = new LegacyVisibleMachineItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private LegacyVisibleMachineItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof LegacyVisibleMultiblockMachineBlock
                || blockItem.getBlock() instanceof LegacyVisibleMachineBlock
                || blockItem.getBlock() instanceof AssemblyMachineBlock
                || blockItem.getBlock() instanceof CustomMissileLauncherBlock
                || blockItem.getBlock() instanceof LaunchPadBlock
                || blockItem.getBlock() instanceof LargeLaunchPadBlock
                || blockItem.getBlock() instanceof RustedLaunchPadBlock
                || blockItem.getBlock() instanceof MissileAssemblyBlock
                || blockItem.getBlock() instanceof MachineBatterySocketBlock
                || blockItem.getBlock() instanceof MachineLpw2Block
                || blockItem.getBlock() instanceof ParticleAcceleratorBlock
                || blockItem.getBlock() instanceof CargoElevatorBlock
                || blockItem.getBlock() instanceof CraneSplitterBlock
                || blockItem.getBlock() instanceof PressMachineBlock
                || blockItem.getBlock() instanceof ElectricPressBlock
                || blockItem.getBlock() instanceof RBMKColumnBlock
                || blockItem.getBlock() instanceof RBMKAutoloaderBlock
                || blockItem.getBlock() instanceof RBMKConsoleBlock
                || blockItem.getBlock() instanceof RBMKCraneConsoleBlock
                || blockItem.getBlock() instanceof AutosawBlock
                || blockItem.getBlock() instanceof RadioboxBlock
                || blockItem.getBlock() instanceof RadioReceiverBlock
                || blockItem.getBlock() instanceof RefuelerBlock
                || blockItem.getBlock() instanceof StorageDrumBlock
                || blockItem.getBlock() instanceof ThresherBlock
                || blockItem.getBlock() instanceof VendingMachineBlock
                || blockItem.getBlock() instanceof LegacyConnectorBlock
                || blockItem.getBlock() instanceof LegacySmallPylonBlock
                || blockItem.getBlock() instanceof LegacyMediumPylonBlock
                || blockItem.getBlock() instanceof LegacyLargePylonBlock
                || blockItem.getBlock() instanceof LegacySubstationBlock
                || blockItem.getBlock() instanceof RadioAutocalBlock
                || blockItem.getBlock() instanceof RadioTelexBlock)) {
            return;
        }

        if (blockItem.getBlock() instanceof FusionMachineBlock block) {
            renderFusionMachineItem(block.kind(), block.definition(), displayContext, poseStack, buffer,
                    packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block) {
            renderVisibleMachineItem(stack, block.definition(), itemState(block.defaultBlockState()), displayContext,
                    poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LegacyVisibleMachineBlock block) {
            renderVisibleMachineItem(stack, block.definition(), itemState(block.defaultBlockState()), displayContext,
                    poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof AssemblyMachineBlock) {
            renderAssemblyMachineItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof CustomMissileLauncherBlock launcher) {
            renderCustomMissileLauncherItem(launcher.kind(), displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LaunchPadBlock) {
            renderLaunchPadItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LargeLaunchPadBlock) {
            renderLargeLaunchPadItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof RustedLaunchPadBlock) {
            renderRustedLaunchPadItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof MissileAssemblyBlock) {
            renderMissileAssemblyItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof MachineBatterySocketBlock) {
            renderBatterySocketItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof MachineLpw2Block) {
            renderLpw2Item(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof ParticleAcceleratorBlock block) {
            renderParticleAcceleratorItem(block.variant(), displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof CargoElevatorBlock) {
            renderCargoElevatorItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof CraneSplitterBlock block) {
            CraneSplitterRenderer.renderItem(displayContext, block.defaultBlockState(), poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof PressMachineBlock) {
            renderPressItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof ElectricPressBlock) {
            renderElectricPressItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof RBMKColumnBlock block) {
            renderRbmkColumnItem(block, displayContext, poseStack, buffer, packedLight);
        } else if (blockItem.getBlock() instanceof RBMKAutoloaderBlock) {
            renderRbmkAutoloaderItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof RBMKConsoleBlock) {
            renderRbmkConsoleItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof RBMKCraneConsoleBlock) {
            renderRbmkCraneConsoleItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof AutosawBlock) {
            renderAutosawItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof RadioboxBlock || blockItem.getBlock() instanceof RadioReceiverBlock) {
            RadioDecoRenderer.renderItem(itemState(blockItem.getBlock().defaultBlockState()), displayContext, poseStack,
                    buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof RefuelerBlock block) {
            RefuelerRenderer.renderItem(itemState(block.defaultBlockState()), displayContext, poseStack, buffer,
                    packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof StorageDrumBlock) {
            StorageDrumRenderer.renderItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof ThresherBlock) {
            renderThresherItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof VendingMachineBlock block) {
            renderVendingMachineItem(block, stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LegacyConnectorBlock connector) {
            renderConnectorItem(connector.kind(), displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LegacySmallPylonBlock) {
            renderSmallPylonItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LegacyMediumPylonBlock pylon) {
            renderMediumPylonItem(pylon.kind(), displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LegacyLargePylonBlock) {
            renderLargePylonItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LegacySubstationBlock) {
            renderSubstationItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof RadioAutocalBlock) {
            renderAutocalItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof RadioTelexBlock) {
            renderTelexItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderRustedLaunchPadItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(ObjLaunchModels.MISSILE_PAD.boundsAll(), point -> point.scale(3.0D));

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyItemBaseInventoryTransform(poseStack, bounds);
            poseStack.translate(0.0D, -1.0D, 0.0D);
            poseStack.scale(3.0F, 3.0F, 3.0F);
        } else {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.35F, 0.35F, 0.35F);
        }
        ObjLaunchModels.MISSILE_PAD.renderAll(ObjLaunchModels.MISSILE_PAD_RUSTED_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderLaunchPadItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(ObjLaunchModels.MISSILE_PAD.boundsAll(), point -> point.scale(3.0D));

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyItemBaseInventoryTransform(poseStack, bounds);
            poseStack.translate(0.0D, -1.0D, 0.0D);
            poseStack.scale(3.0F, 3.0F, 3.0F);
        } else {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.35F, 0.35F, 0.35F);
        }
        ObjLaunchModels.MISSILE_PAD.renderAll(ObjLaunchModels.MISSILE_PAD_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderLargeLaunchPadItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = ObjLaunchModels.MISSILE_ERECTOR.boundsOnly(
                "Pad", "Atlas_Pad", "Atlas_Erector", "Atlas_Pivot");

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyInventoryObjTransform(poseStack, 0.0D, -3.75D, 0.0D, 1.625D);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        } else {
            applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 0.0F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
        ObjLaunchModels.renderMissileErectorPart("Pad", ObjLaunchModels.MISSILE_ERECTOR_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        ObjLaunchModels.renderMissileErectorPart("Atlas_Pad", ObjLaunchModels.MISSILE_ERECTOR_ATLAS_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        ObjLaunchModels.renderMissileErectorPart("Atlas_Erector", ObjLaunchModels.MISSILE_ERECTOR_ATLAS_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        ObjLaunchModels.renderMissileErectorPart("Atlas_Pivot", ObjLaunchModels.MISSILE_ERECTOR_ATLAS_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
    }

    private static void renderMissileAssemblyItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(ObjLaunchModels.MISSILE_ASSEMBLY.boundsAll(),
                point -> point.scale(10.0D).add(0.0D, -2.5D, 0.0D));

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyItemBaseInventoryTransform(poseStack, bounds);
            poseStack.translate(0.0D, -2.5D, 0.0D);
            poseStack.scale(10.0F, 10.0F, 10.0F);
        } else {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.25F, 0.25F, 0.25F);
        }
        ObjLaunchModels.MISSILE_ASSEMBLY.renderAll(ObjLaunchModels.MISSILE_ASSEMBLY_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderCustomMissileLauncherItem(CustomMissileLauncherBlock.Kind kind,
            ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        AABB bounds = kind == CustomMissileLauncherBlock.Kind.COMPACT_LAUNCHER
                ? ObjLaunchModels.COMPACT_LAUNCHER.boundsAll()
                : union(ObjLaunchModels.LAUNCH_TABLE_BASE_LEGACY.boundsAll(),
                        ObjLaunchModels.LAUNCH_TABLE_SMALL_PAD_LEGACY.boundsAll());

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds,
                kind == CustomMissileLauncherBlock.Kind.COMPACT_LAUNCHER ? 0.68F : 0.84F, 0.0F);
        if (kind == CustomMissileLauncherBlock.Kind.COMPACT_LAUNCHER) {
            ObjLaunchModels.COMPACT_LAUNCHER.renderAll(ObjLaunchModels.COMPACT_LAUNCHER_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
        } else {
            ObjLaunchModels.LAUNCH_TABLE_BASE_LEGACY.renderAll(ObjLaunchModels.LAUNCH_TABLE_BASE_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
            ObjLaunchModels.LAUNCH_TABLE_SMALL_PAD_LEGACY.renderAll(ObjLaunchModels.LAUNCH_TABLE_SMALL_PAD_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderVisibleMachineItem(ItemStack stack, LegacyMachineDefinition definition, BlockState state,
            ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()).asVBO());
        LegacyMachinePartRenderSelection.Selection selection = LegacyMachinePartRenderSelection.item(definition);
        AABB rawBounds = definition.itemRenderAll()
                ? model.boundsAll()
                : model.boundsOnly(selection.partNames());
        AABB bounds = transformVisibleBounds(rawBounds, definition, state);

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI && renderLegacyVisibleInventoryItem(definition, state, model,
                stack, poseStack, buffer, packedLight, packedOverlay)) {
            poseStack.popPose();
            return;
        }
        applyDisplayTransform(displayContext, poseStack, bounds, definition.itemFitSize(), definition.legacyItemScale());
        if (displayContext == ItemDisplayContext.GUI && definition.legacyInventoryTranslation() != Vec3.ZERO) {
            Vec3 translation = definition.legacyInventoryTranslation();
            poseStack.translate(translation.x, translation.y, translation.z);
        }
        renderMachine(definition, state, model, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderFusionMachineItem(FusionMachineBlock.Kind kind, LegacyMachineDefinition definition,
            ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()).asVBO());
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            Vec3 translation = fusionInventoryTranslation(kind);
            applyLegacyInventoryObjTransform(poseStack, translation.x, translation.y, translation.z,
                    fusionInventoryScale(kind));
            if (fusionInventoryRotates(kind)) {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        } else {
            AABB bounds = transformBounds(fusionItemBounds(kind, model),
                    point -> rotateY(point, 90.0F).add(0.5D, 0.0D, 0.5D));
            applyDisplayTransform(displayContext, poseStack, bounds, definition.itemFitSize(), fusionLegacyItemScale(kind));
            poseStack.translate(0.5D, 0.0D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
        renderFusionItemParts(kind, definition, model, poseStack, buffer, packedLight, packedOverlay,
                System.currentTimeMillis());
        poseStack.popPose();
    }

    private static AABB fusionItemBounds(FusionMachineBlock.Kind kind, LegacyWavefrontModel model) {
        return switch (kind) {
            case TORUS -> model.boundsOnly("Torus", "Magnet");
            case KLYSTRON, KLYSTRON_CREATIVE -> model.boundsOnly("Klystron", "Rotor");
            case BREEDER -> model.boundsOnly("Breeder");
            case MHDT -> model.boundsOnly("Turbine", "Coils");
            case PLASMA_FORGE -> model.boundsAll();
            case COLLECTOR, BOILER, COUPLER -> model.boundsAll();
        };
    }

    private static float fusionLegacyItemScale(FusionMachineBlock.Kind kind) {
        return switch (kind) {
            case TORUS -> 1.0F;
            case KLYSTRON, KLYSTRON_CREATIVE, BOILER -> 1.75F;
            case BREEDER, COLLECTOR -> 2.5F;
            case COUPLER -> 3.0F;
            case MHDT -> 1.25F;
            case PLASMA_FORGE -> 1.375F;
        };
    }

    private static double fusionInventoryScale(FusionMachineBlock.Kind kind) {
        return switch (kind) {
            case TORUS -> 2.0D;
            case KLYSTRON, KLYSTRON_CREATIVE, BOILER -> 3.5D;
            case BREEDER, COLLECTOR -> 5.0D;
            case COUPLER -> 6.0D;
            case MHDT -> 2.5D;
            case PLASMA_FORGE -> 2.75D;
        };
    }

    private static boolean fusionInventoryRotates(FusionMachineBlock.Kind kind) {
        return kind != FusionMachineBlock.Kind.TORUS;
    }

    private static Vec3 fusionInventoryTranslation(FusionMachineBlock.Kind kind) {
        return switch (kind) {
            case KLYSTRON, KLYSTRON_CREATIVE -> new Vec3(0.0D, -3.0D, 1.0D);
            case BREEDER, COUPLER -> new Vec3(0.0D, -3.0D, 0.0D);
            case COLLECTOR -> new Vec3(0.0D, -2.0D, 0.0D);
            case BOILER, PLASMA_FORGE -> new Vec3(0.0D, -1.0D, 0.0D);
            case TORUS, MHDT -> Vec3.ZERO;
        };
    }

    private static void renderFusionItemParts(FusionMachineBlock.Kind kind, LegacyMachineDefinition definition,
            LegacyWavefrontModel model, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, long currentMillis) {
        switch (kind) {
            case TORUS -> {
                ObjFusionModels.renderTorusPart(model, definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Torus");
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees((float) (currentMillis / 5.0D % 360.0D)));
                ObjFusionModels.renderTorusPart(model, definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Magnet");
                poseStack.popPose();
            }
            case KLYSTRON, KLYSTRON_CREATIVE -> {
                ObjFusionModels.renderKlystronPart(model, definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Klystron");
                poseStack.pushPose();
                poseStack.translate(0.0D, 2.5D, 0.0D);
                poseStack.mulPose(Axis.XP.rotationDegrees((float) (currentMillis / 10.0D % 360.0D)));
                poseStack.translate(0.0D, -2.5D, 0.0D);
                ObjFusionModels.renderKlystronPart(model, definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Rotor");
                poseStack.popPose();
            }
            case BREEDER -> ObjFusionModels.renderBreederPart(model, definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Breeder");
            case COLLECTOR, BOILER, COUPLER -> model.renderAll(definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
            case MHDT -> {
                ObjFusionModels.renderMhdtPart(model, definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Turbine");
                double rotor = currentMillis / 5.0D % 30.0D - 15.0D;
                poseStack.pushPose();
                poseStack.translate(0.0D, 1.5D, 0.0D);
                poseStack.mulPose(Axis.XP.rotationDegrees((float) rotor));
                poseStack.translate(0.0D, -1.5D, 0.0D);
                ObjFusionModels.renderMhdtPart(model, definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Coils");
                poseStack.popPose();
            }
            case PLASMA_FORGE -> {
                ObjFusionModels.renderPlasmaForgeItemBody(definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
                ObjFusionModels.renderPlasmaForgePartUntextured(poseStack, buffer, 0, 0, 0, 255,
                        LegacyTexturedRenderMode.CUTOUT_CULL, "Plasma");
            }
        }
    }

    private static boolean renderLegacyVisibleInventoryItem(LegacyMachineDefinition definition, BlockState state,
            LegacyWavefrontModel model, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        LegacyVisibleInventoryProfile profile = legacyVisibleInventoryProfile(definition);
        if (profile == null) {
            return false;
        }

        applyLegacyInventoryObjTransform(poseStack, profile.yOffsetPixels(), profile.inventoryScale());
        if (profile.commonScale() != 1.0D) {
            poseStack.scale((float) profile.commonScale(), (float) profile.commonScale(), (float) profile.commonScale());
        }
        if (profile.commonYRotationDegrees() != 0.0D) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) profile.commonYRotationDegrees()));
        }

        if (isLegacyFluidTankModel(definition)) {
            renderLegacyFluidTankInventoryItem(stack, state, poseStack, buffer, packedLight, packedOverlay);
        } else if (definition.itemRenderAll()) {
            model.renderAll(definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                    LegacyMachinePartRenderContexts.renderMode(definition.renderMode()));
        } else {
            renderMachineParts(definition, model, poseStack, buffer, packedLight, packedOverlay);
        }
        return true;
    }

    private static LegacyVisibleInventoryProfile legacyVisibleInventoryProfile(LegacyMachineDefinition definition) {
        String path = definition.modelLocation().getPath();
        return switch (path) {
            case "models/machines/fluidtank.obj" -> new LegacyVisibleInventoryProfile(-2.0D, 3.5D, 0.75D, 90.0D);
            case "models/reactors/watz.obj" -> new LegacyVisibleInventoryProfile(-1.0D, 2.0D, 1.0D, 0.0D);
            case "models/machines/watz_pump.obj" -> new LegacyVisibleInventoryProfile(-1.5D, 5.0D, 1.0D, 0.0D);
            case "models/zirnox.obj" -> new LegacyVisibleInventoryProfile(-2.0D, 2.8D, 0.75D, 0.0D);
            case "models/reactors/icf.obj" -> new LegacyVisibleInventoryProfile(-1.5D, 2.125D, 0.5D, 90.0D);
            default -> null;
        };
    }

    private static boolean isLegacyFluidTankModel(LegacyMachineDefinition definition) {
        return "models/machines/fluidtank.obj".equals(definition.modelLocation().getPath());
    }

    private static void renderLegacyFluidTankInventoryItem(ItemStack stack, BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        HbmFluidTank tank = new HbmFluidTank(HbmFluids.NONE, 0);
        boolean exploded = false;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(HbmPersistentBlockState.TAG_PERSISTENT, Tag.TAG_COMPOUND)) {
            CompoundTag persistent = tag.getCompound(HbmPersistentBlockState.TAG_PERSISTENT);
            tank.readFromNbt(persistent, "tank");
            exploded = persistent.getBoolean("hasExploded");
        }
        LegacyFluidTankRenderHelper.renderSmallTank(ObjMachineModels.FLUIDTANK, ObjMachineModels.FLUIDTANK_EXPLODED,
                tank, exploded, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderPressItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyInventoryObjTransform(poseStack, -4.0D, 4.5D);
        } else {
            applyLegacyItemBaseNonInventoryTransform(displayContext, poseStack);
        }
        ObjMachineModels.PRESS_BODY_LEGACY.renderAll(ObjMachineModels.PRESS_BODY_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, 0.5D, 0.0D);
        ObjMachineModels.PRESS_HEAD_LEGACY.renderAll(ObjMachineModels.PRESS_HEAD_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderAssemblyMachineItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(AssemblyMachineRenderer.MODEL.boundsAll(),
                point -> rotateY(point.scale(0.75D), 90.0F).add(0.5D, 0.0D, 0.5D));

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 4.5F);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(0.75F, 0.75F, 0.75F);
        AssemblyMachineRenderer.MODEL.renderAll(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderBatterySocketItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = MachineBatterySocketRenderer.MODEL.boundsOnly("Socket");

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 5.0F);
        MachineBatterySocketRenderer.renderModelPart("Socket", MachineBatterySocketRenderer.SOCKET_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderElectricPressItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bodyBounds = ObjMachineModels.EPRESS_BODY.boundsAll();
        AABB headBounds = transformBounds(ObjMachineModels.EPRESS_HEAD.boundsAll(), point -> point.add(0.0D, 1.5D, 0.0D));
        AABB bounds = union(bodyBounds, headBounds);

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 4.5F);
        ObjMachineModels.EPRESS_BODY.renderAll(ObjMachineModels.EPRESS_BODY_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, 1.5D, 0.0D);
        ObjMachineModels.EPRESS_HEAD.renderAll(ObjMachineModels.EPRESS_HEAD_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderAutosawItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(ObjMachineModels.AUTOSAW.boundsAll(),
                point -> rotateY(point.scale(0.5D), -90.0F));
        LegacyTileRenderPlans.AutosawPlan plan =
                LegacyTileRenderPlans.autosawItemPlan(System.currentTimeMillis());

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 5.0F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
        AutosawRenderer.renderModelPart("Base", poseStack, buffer, packedLight, packedOverlay);
        poseStack.mulPose(Axis.YN.rotationDegrees((float) plan.turnDegrees()));
        AutosawRenderer.renderModelPart("Main", poseStack, buffer, packedLight, packedOverlay);
        AutosawRenderer.renderModelPart("Engine", poseStack, buffer, packedLight, packedOverlay);
        renderAutosawPivotedPart(plan.armUpper(), poseStack, buffer, packedLight, packedOverlay);
        renderAutosawPivotedPart(plan.armLower(), poseStack, buffer, packedLight, packedOverlay);
        renderAutosawPivotedPart(plan.armTip(), poseStack, buffer, packedLight, packedOverlay);
        renderAutosawPivotedPart(plan.sawBlade(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderThresherItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(ObjMachineModels.THRESHER.boundsAll(),
                point -> rotateY(point.scale(0.5D), -90.0F));
        LegacyTileRenderPlans.ThresherPlan plan =
                LegacyTileRenderPlans.thresherItemPlan(System.currentTimeMillis());

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 4.5F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
        ThresherRenderer.renderModelPart("Base", poseStack, buffer, packedLight, packedOverlay);
        ThresherRenderer.renderModelPart("Engine", poseStack, buffer, packedLight, packedOverlay);
        renderThresherPivotedPart(plan.armUpper(), poseStack, buffer, packedLight, packedOverlay);
        renderThresherPivotedPart(plan.armLower(), poseStack, buffer, packedLight, packedOverlay);
        renderThresherPivotedPart(plan.front(), poseStack, buffer, packedLight, packedOverlay);
        renderThresherPivotedPart(plan.wheel(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderRbmkColumnItem(RBMKColumnBlock block, ItemDisplayContext displayContext,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        int heightAbove = RBMKStructureDimensions.columnHeightAboveCore();
        BlockState state = block.defaultBlockState().setValue(RBMKColumnBlock.LID, RBMKColumnBlock.LidType.NONE);
        double topHeight = RBMKColumnRenderer.hasLegacyTopPipePads(block.kind(), RBMKColumnBlock.LidType.NONE)
                ? 1.125D
                : 1.0D;
        AABB bounds = new AABB(0.0D, 0.0D, 0.0D, 1.0D, heightAbove + topHeight, 1.0D);
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 0.0F);
        for (int y = 0; y <= heightAbove; y++) {
            poseStack.pushPose();
            poseStack.translate(0.0D, y, 0.0D);
            RBMKColumnRenderer.renderStaticSegment(dispatcher, state, y, heightAbove, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
        if (block.kind().rod()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            ObjRbmkModels.renderFuelChannelRods(0x304825, heightAbove, poseStack, buffer, packedLight,
                    OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderRbmkAutoloaderItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(ObjRbmkModels.AUTOLOADER.boundsAll(), point -> point.add(0.5D, 0.0D, 0.5D));
        BlockState state = com.hbm.ntm.registry.ModBlocks.RBMK_AUTOLOADER.get().defaultBlockState();

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 0.0F);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        ObjRbmkModels.renderAutoloaderPart("Base", poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        ObjRbmkModels.renderAutoloaderPart("Piston", poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
    }

    private static void renderRbmkConsoleItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float yaw = 270.0F;
        AABB bounds = transformBounds(ObjRbmkModels.CONSOLE.boundsAll(),
                point -> rotateY(point.add(0.5D, 0.0D, 0.0D), yaw).add(0.5D, 0.0D, 0.5D));
        BlockState state = com.hbm.ntm.registry.ModBlocks.RBMK_CONSOLE.get().defaultBlockState()
                .setValue(RBMKConsoleBlock.FACING, Direction.SOUTH);

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 0.0F);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.translate(0.5D, 0.0D, 0.0D);
        ObjRbmkModels.CONSOLE.renderAll(ObjRbmkModels.CONSOLE_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderRbmkCraneConsoleItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float yaw = 270.0F;
        AABB bounds = transformBounds(ObjRbmkModels.CRANE_CONSOLE.boundsOnly("Console_Coonsole", "Joystick",
                "Meter1", "Meter2", "Lamp1", "Lamp2"), point -> rotateY(point, yaw).add(0.5D, 0.0D, 0.5D));

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 0.0F);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        LegacyRbmkMachineRenderer.renderCraneConsole(poseStack, buffer, packedLight, packedOverlay,
                LegacyRbmkMachineRenderer.CraneConsoleState.EMPTY, 0.0F, System.currentTimeMillis());
        poseStack.popPose();
    }

    private static void renderLpw2Item(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = com.hbm.ntm.client.obj.ObjReactorModels.LPW2.boundsAll();

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 0.0F);
        com.hbm.ntm.client.obj.ObjReactorModels.LPW2.renderAll(com.hbm.ntm.client.obj.ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderParticleAcceleratorItem(ParticleAcceleratorBlock.Variant variant,
            ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        LegacyWavefrontModel model = particleAcceleratorModel(variant);

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyInventoryObjTransform(poseStack, particleAcceleratorInventoryYOffset(variant),
                    particleAcceleratorInventoryScale(variant));
        } else {
            applyLegacyItemBaseNonInventoryTransform(displayContext, poseStack);
        }
        double commonScale = particleAcceleratorCommonScale(variant);
        if (commonScale != 1.0D) {
            poseStack.scale((float) commonScale, (float) commonScale, (float) commonScale);
        }
        if (variant != ParticleAcceleratorBlock.Variant.DIPOLE) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }

        if (variant == ParticleAcceleratorBlock.Variant.BEAMLINE) {
            ObjParticleAcceleratorModels.renderBeamlinePart("Beamline", particleAcceleratorTexture(variant),
                    poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
        } else {
            model.renderAll(particleAcceleratorTexture(variant), poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_CULL);
        }
        poseStack.popPose();
    }

    private static void applyLegacyItemBaseNonInventoryTransform(ItemDisplayContext displayContext,
            PoseStack poseStack) {
        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.scale(1.5F, 1.5F, 1.5F);
        } else {
            poseStack.translate(0.5D, 0.25D, 0.0D);
        }
        poseStack.scale(0.25F, 0.25F, 0.25F);
        if (displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
    }

    private static double particleAcceleratorInventoryYOffset(ParticleAcceleratorBlock.Variant variant) {
        return switch (variant) {
            case SOURCE, RFC, DETECTOR -> -1.0D;
            case QUADRUPOLE -> -3.5D;
            case DIPOLE -> -3.0D;
            case BEAMLINE -> 0.0D;
        };
    }

    private static double particleAcceleratorInventoryScale(ParticleAcceleratorBlock.Variant variant) {
        return switch (variant) {
            case SOURCE, BEAMLINE, RFC, QUADRUPOLE -> 4.0D;
            case DIPOLE -> 3.5D;
            case DETECTOR -> 3.0D;
        };
    }

    private static double particleAcceleratorCommonScale(ParticleAcceleratorBlock.Variant variant) {
        return switch (variant) {
            case SOURCE, RFC, DETECTOR -> 0.5D;
            default -> 1.0D;
        };
    }

    private static LegacyWavefrontModel particleAcceleratorModel(ParticleAcceleratorBlock.Variant variant) {
        return switch (variant) {
            case SOURCE -> ObjParticleAcceleratorModels.SOURCE;
            case BEAMLINE -> ObjParticleAcceleratorModels.BEAMLINE;
            case RFC -> ObjParticleAcceleratorModels.RFC;
            case QUADRUPOLE -> ObjParticleAcceleratorModels.QUADRUPOLE;
            case DIPOLE -> ObjParticleAcceleratorModels.DIPOLE;
            case DETECTOR -> ObjParticleAcceleratorModels.DETECTOR;
        };
    }

    private static ResourceLocation particleAcceleratorTexture(ParticleAcceleratorBlock.Variant variant) {
        return switch (variant) {
            case SOURCE -> ObjParticleAcceleratorModels.SOURCE_TEXTURE;
            case BEAMLINE -> ObjParticleAcceleratorModels.BEAMLINE_TEXTURE;
            case RFC -> ObjParticleAcceleratorModels.RFC_TEXTURE;
            case QUADRUPOLE -> ObjParticleAcceleratorModels.QUADRUPOLE_TEXTURE;
            case DIPOLE -> ObjParticleAcceleratorModels.DIPOLE_TEXTURE;
            case DETECTOR -> ObjParticleAcceleratorModels.DETECTOR_TEXTURE;
        };
    }

    private static void renderCargoElevatorItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB rawBounds = ObjMachineModels.ELEVATOR_LEGACY.boundsOnly("Base", "Piston", "Guides", "Platform");
        AABB piston2 = transformBounds(ObjMachineModels.ELEVATOR_LEGACY.boundsOnly("Piston", "Guides", "Platform"),
                point -> point.add(0.0D, 1.0D, 0.0D));
        AABB guides3 = transformBounds(ObjMachineModels.ELEVATOR_LEGACY.boundsOnly("Guides"),
                point -> point.add(0.0D, 2.0D, 0.0D));
        AABB bounds = union(union(rawBounds, piston2), guides3);

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 3.25F);
        CargoElevatorRenderer.renderModelPart("Base", poseStack, buffer, packedLight, packedOverlay);
        CargoElevatorRenderer.renderModelPart("Piston", poseStack, buffer, packedLight, packedOverlay);
        CargoElevatorRenderer.renderModelPart("Guides", poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, 1.0D, 0.0D);
        CargoElevatorRenderer.renderModelPart("Piston", poseStack, buffer, packedLight, packedOverlay);
        CargoElevatorRenderer.renderModelPart("Guides", poseStack, buffer, packedLight, packedOverlay);
        CargoElevatorRenderer.renderModelPart("Platform", poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, 1.0D, 0.0D);
        CargoElevatorRenderer.renderModelPart("Guides", poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderVendingMachineItem(VendingMachineBlock block, ItemStack stack,
            ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        int variant = 0;
        if (stack.getItem() instanceof LegacyStateBlockItem item) {
            variant = item.getVariant(stack);
        } else if (stack.getItem() instanceof LegacyStateMultiblockBlockItem item) {
            variant = item.getVariant(stack);
        }
        BlockState state = block.defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH)
                .setValue(VendingMachineBlock.VARIANT, variant);
        String part = variant == 0 ? "Soda" : "Obamna";
        AABB bounds = VendingMachineRenderer.MODEL.boundsOnly(part);

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 6.25F);
        VendingMachineRenderer.render(state, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderMediumPylonItem(LegacyMediumPylonBlock.Kind kind, ItemDisplayContext displayContext,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB rawBounds = kind.transformer()
                ? ObjNetworkModels.PYLON_MEDIUM_LEGACY.boundsOnly("Pylon", "Transformer")
                : ObjNetworkModels.PYLON_MEDIUM_LEGACY.boundsOnly("Pylon");
        AABB bounds = transformBounds(rawBounds,
                point -> rotateY(point.scale(0.5D).add(0.75D, 0.0D, 0.0D), 90.0F).add(0.5D, 0.0D, 0.5D));

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 4.5F);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.translate(0.75D, 0.0D, 0.0D);
        ResourceLocation texture = kind.steel()
                ? LegacyPylonRenderer.PYLON_MEDIUM_STEEL_TEXTURE
                : LegacyPylonRenderer.PYLON_MEDIUM_TEXTURE;
        LegacyPylonRenderer.renderMediumPylonPart("Pylon", texture, poseStack, buffer, packedLight, packedOverlay);
        if (kind.transformer()) {
            LegacyPylonRenderer.renderMediumPylonPart("Transformer", texture, poseStack, buffer,
                    packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderSmallPylonItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, LegacySmallPylonModel.LEGACY_RENDER_BOUNDS, 0.58F, 4.5F);
        LegacySmallPylonModel.render(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderConnectorItem(LegacyConnectorBlock.Kind kind, ItemDisplayContext displayContext,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB rawBounds = (kind == LegacyConnectorBlock.Kind.SUPER
                ? ObjNetworkModels.CONNECTOR_SUPER_LEGACY
                : ObjNetworkModels.CONNECTOR_LEGACY).boundsAll();
        AABB bounds = transformBounds(rawBounds, point -> point.scale(2.0D));

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyInventoryObjTransform(poseStack,
                    kind == LegacyConnectorBlock.Kind.SUPER ? -5.0D : -3.5D, 7.0D);
        } else {
            applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 7.0F);
        }
        poseStack.scale(2.0F, 2.0F, 2.0F);
        if (kind == LegacyConnectorBlock.Kind.SUPER) {
            ObjNetworkModels.CONNECTOR_SUPER_LEGACY.renderAll(LegacyPylonRenderer.CONNECTOR_SUPER_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
        } else {
            ObjNetworkModels.CONNECTOR_LEGACY.renderAll(LegacyPylonRenderer.CONNECTOR_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderLargePylonItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(ObjNetworkModels.PYLON_LARGE_LEGACY.boundsAll(),
                point -> point.scale(0.5D));

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyInventoryObjTransform(poseStack, -5.0D, 2.25D);
        } else {
            applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 2.25F);
        }
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjNetworkModels.PYLON_LARGE_LEGACY.renderAll(LegacyPylonRenderer.PYLON_LARGE_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderSubstationItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(ObjNetworkModels.SUBSTATION_LEGACY.boundsAll(), point -> point.scale(0.5D));

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 4.5F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjNetworkModels.SUBSTATION_LEGACY.renderAll(LegacyPylonRenderer.SUBSTATION_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderAutocalItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, RadioAutocalRenderer.MODEL.boundsAll(), 0.58F, 6.25F);
        RadioAutocalRenderer.MODEL.renderAll(RadioAutocalRenderer.TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderTelexItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(RadioTelexRenderer.MODEL.boundsAll(), point -> point.add(0.0D, 0.0D, -0.5D));

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 6.0F);
        poseStack.translate(0.0D, 0.0D, -0.5D);
        RadioTelexRenderer.MODEL.renderAll(RadioTelexRenderer.TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static BlockState itemState(BlockState state) {
        if (state.hasProperty(HorizontalMachineBlock.FACING)) {
            state = state.setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        }
        return state;
    }

    private static void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack, AABB bounds,
            float targetSize, float legacyItemScale) {
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        float resolvedTargetSize = targetSize;
        if (legacyItemScale > 0.0F) {
            resolvedTargetSize = (float) Math.min(LEGACY_GUI_MAX_OCCUPANCY,
                    maxSize * legacyItemScale / LEGACY_GUI_SLOT_PIXELS);
        }
        float fitScale = (float) Math.max(0.035D,
                Math.min(0.32D, resolvedTargetSize / Math.max(1.0D, maxSize)));

        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(fitScale, fitScale, fitScale);
            poseStack.translate(-center.x, -center.y, -center.z);
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float worldScale = fitScale * 0.82F;
        poseStack.scale(worldScale, worldScale, worldScale);
        poseStack.translate(-center.x, -center.y, -center.z);

        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.translate(0.0D, -0.25D, 0.0D);
            poseStack.scale(0.8F, 0.8F, 0.8F);
        } else if (displayContext.firstPerson()) {
            poseStack.translate(0.0D, 0.1D, 0.0D);
            poseStack.scale(0.85F, 0.85F, 0.85F);
        }
    }

    private static void applyLegacyItemBaseInventoryTransform(PoseStack poseStack, AABB bounds) {
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        float fitScale = (float) Math.max(0.025D,
                Math.min(0.32D, LEGACY_GUI_MAX_OCCUPANCY / Math.max(1.0D, maxSize)));

        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.scale(fitScale, fitScale, fitScale);
        poseStack.translate(-center.x, -center.y, -center.z);
    }

    private static void applyLegacyInventoryObjTransform(PoseStack poseStack, double yOffsetPixels,
            double inventoryScale) {
        applyLegacyInventoryObjTransform(poseStack, 0.0D, yOffsetPixels, 0.0D, inventoryScale);
    }

    private static void applyLegacyInventoryObjTransform(PoseStack poseStack, double xOffsetPixels,
            double yOffsetPixels, double zOffsetPixels, double inventoryScale) {
        poseStack.translate(0.5D, 0.625D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        poseStack.translate(xOffsetPixels, yOffsetPixels, zOffsetPixels);
        poseStack.scale((float) inventoryScale, (float) inventoryScale, (float) inventoryScale);
    }

    private static AABB transformVisibleBounds(AABB bounds, LegacyMachineDefinition definition, BlockState state) {
        Vec3 translation = definition.modelTranslation(state);
        float yRotation = definition.yRotation(state);
        float postModelYRotation = definition.postModelYRotation(state);
        return transformBounds(bounds, point -> rotateY(
                rotateY(point, postModelYRotation).add(translation), yRotation).add(0.5D, 0.0D, 0.5D));
    }

    private static AABB transformBounds(AABB bounds, PointTransform transform) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (double x : new double[] { bounds.minX, bounds.maxX }) {
            for (double y : new double[] { bounds.minY, bounds.maxY }) {
                for (double z : new double[] { bounds.minZ, bounds.maxZ }) {
                    Vec3 point = transform.apply(new Vec3(x, y, z));
                    minX = Math.min(minX, point.x);
                    minY = Math.min(minY, point.y);
                    minZ = Math.min(minZ, point.z);
                    maxX = Math.max(maxX, point.x);
                    maxY = Math.max(maxY, point.y);
                    maxZ = Math.max(maxZ, point.z);
                }
            }
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static AABB union(AABB first, AABB second) {
        return new AABB(
                Math.min(first.minX, second.minX),
                Math.min(first.minY, second.minY),
                Math.min(first.minZ, second.minZ),
                Math.max(first.maxX, second.maxX),
                Math.max(first.maxY, second.maxY),
                Math.max(first.maxZ, second.maxZ));
    }

    private static Vec3 rotateY(Vec3 point, float degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        return new Vec3(point.x * cos + point.z * sin, point.y, point.z * cos - point.x * sin);
    }

    private static void renderMachine(LegacyMachineDefinition definition, BlockState state, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        if (definition.renderProfile() == LegacyMachineRenderProfile.DEFAULT) {
            if (definition.itemRenderAll()) {
                model.renderAll(definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                        LegacyMachinePartRenderContexts.renderMode(definition.renderMode()));
            } else {
                renderMachineParts(definition, model, poseStack, buffer, packedLight, packedOverlay);
            }
        } else {
            if (renderMachineProfileDirect(definition, model, poseStack, buffer, packedLight, packedOverlay)) {
                poseStack.popPose();
                return;
            }
            if (definition.itemRenderAll()) {
                model.renderAll(definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                        LegacyMachinePartRenderContexts.renderMode(definition.renderMode()));
            } else {
                renderMachineParts(definition, model, poseStack, buffer, packedLight, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    private static boolean renderMachineProfileDirect(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyTexturedRenderMode renderMode = LegacyMachinePartRenderContexts.renderMode(definition.renderMode());
        long currentMillis = System.currentTimeMillis();
        if (definition.renderProfile() == LegacyMachineRenderProfile.STEAM_ENGINE_ITEM_PREVIEW) {
            SteamEngineRenderer.renderPlan(model,
                    LegacyTileRenderPlans.steamEngineItemPlan(true, currentMillis), poseStack, buffer,
                    packedLight, packedOverlay);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.INDUSTRIAL_TURBINE_ITEM_PREVIEW) {
            IndustrialSteamTurbineRenderer.renderPlan(model,
                    LegacyTileRenderPlans.industrialTurbineItemPlan(currentMillis), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.ARC_FURNACE_STATIC_PREVIEW) {
            LegacyArcFurnaceRenderHelper.renderPlan(model,
                    LegacyTileRenderPlans.arcFurnaceStaticPreviewPlan(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.MINING_LASER_ITEM_PREVIEW) {
            renderMiningLaserItem(model, poseStack, buffer, packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.TURBOFAN_ITEM_PREVIEW) {
            renderTurbofanItem(definition, model, poseStack, buffer, packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.LEGACY_LARGE_TURBINE_ITEM_PREVIEW) {
            renderLegacyLargeTurbineItem(definition, model, poseStack, buffer, packedLight, packedOverlay,
                    renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.RADGEN_STATIC_SPECIAL) {
            LegacyVisibleMachineRenderer.renderVisibleMachineStaticPlan(definition, model,
                    LegacyTileRenderPlans.radgenStaticPlan(false), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.BATTERY_REDD_STATIC_SPECIAL) {
            LegacyVisibleMachineRenderer.renderVisibleMachineStaticPlan(definition, model,
                    LegacyTileRenderPlans.batteryReddStaticPlan(), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.CYCLOTRON_PLUGS) {
            LegacyVisibleMachineRenderer.renderCyclotronItemParts(definition, model, poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.PRECASS_RUNNING_PARTS) {
            renderPrecassItem(definition, model, poseStack, buffer, packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.PUREX_RUNNING_PARTS) {
            renderPurexItem(definition, model, poseStack, buffer, packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.CRYSTALLIZER_STATIC_SPECIAL) {
            LegacyVisibleMachineRenderer.renderVisibleMachineStaticPlan(definition, model,
                    LegacyTileRenderPlans.crystallizerStaticPlan(false), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.COMPRESSOR_RUNNING_PARTS) {
            LegacyVisibleMachineRenderer.renderCompressorPlan(definition, model,
                    LegacyTileRenderPlans.compressorItemPlan(currentMillis), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.PUMP_RUNNING_PARTS) {
            LegacyVisibleMachineRenderer.renderPumpPlan(definition, model,
                    LegacyTileRenderPlans.pumpItemPlan(currentMillis), poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.DIESEL_GENERATOR_RUNNING_PARTS) {
            LegacyVisibleMachineRenderer.renderDieselGeneratorPart(model, "Generator", definition.textureLocation(),
                    poseStack, buffer, packedLight, packedOverlay, renderMode);
            LegacyVisibleMachineRenderer.renderDieselGeneratorPart(model, "Engine", definition.textureLocation(),
                    poseStack, buffer, packedLight, packedOverlay, renderMode);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.ANNIHILATOR_UV_SCROLL) {
            renderAnnihilatorItem(definition, model, poseStack, buffer, packedLight, packedOverlay, renderMode);
            return true;
        }
        return false;
    }

    private static void renderAnnihilatorItem(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyVisibleMachineRenderer.renderAnnihilatorPart(model, "Annihilator", definition.textureLocation(),
                poseStack, buffer, packedLight, packedOverlay, renderMode);
        LegacyVisibleMachineRenderer.renderAnnihilatorPart(model, "Roller", definition.textureLocation(),
                poseStack, buffer, packedLight, packedOverlay, renderMode);
        LegacyVisibleMachineRenderer.renderAnnihilatorPart(model, "Belt",
                definition.itemPartTextures().getOrDefault("Belt",
                        definition.partTextures().getOrDefault("Belt", definition.textureLocation())),
                poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    private static void renderPrecassItem(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        AssemblyMachineRenderer.renderModelPart(model, "Base", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        AssemblyMachineRenderer.renderModelPart(model, "Frame", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        AssemblyMachineRenderer.renderModelPart(model, "Ring", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        AssemblyMachineRenderer.renderModelPart(model, "Ring2", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);

        double[] arm = new double[] { 45.0D, -30.0D, 45.0D };
        for (int i = 0; i < 4; i++) {
            renderPrecassArm(definition, model, poseStack, buffer, packedLight, packedOverlay, renderMode,
                    arm, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
    }

    private static void renderPrecassArm(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, double[] arm, double striker) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.625D, 0.9375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) arm[0]));
        poseStack.translate(0.0D, -1.625D, -0.9375D);
        AssemblyMachineRenderer.renderModelPart(model, "ArmLower1", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);

        poseStack.translate(0.0D, 2.375D, 0.9375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) arm[1]));
        poseStack.translate(0.0D, -2.375D, -0.9375D);
        AssemblyMachineRenderer.renderModelPart(model, "ArmUpper1", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);

        poseStack.translate(0.0D, 2.375D, 0.4375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) arm[2]));
        poseStack.translate(0.0D, -2.375D, -0.4375D);
        AssemblyMachineRenderer.renderModelPart(model, "Head1", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        poseStack.translate(0.0D, striker, 0.0D);
        AssemblyMachineRenderer.renderModelPart(model, "Spike1", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderPurexItem(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyVisibleMachineRenderer.renderPurexPart(model, "Base", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        LegacyVisibleMachineRenderer.renderPurexPart(model, "Frame", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        LegacyVisibleMachineRenderer.renderPurexPart(model, "Fan", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
        LegacyVisibleMachineRenderer.renderPurexPart(model, "Pump", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);
    }

    private static void renderMiningLaserItem(LegacyWavefrontModel model, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        MiningLaserRenderer.renderModelPart(model, "Base", ObjMachineModels.MINING_LASER_BASE_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, renderMode);
        MiningLaserRenderer.renderModelPart(model, "Pivot", ObjMachineModels.MINING_LASER_PIVOT_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.pushPose();
        poseStack.translate(0.0D, -1.0D, 0.75D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        MiningLaserRenderer.renderModelPart(model, "Laser", ObjMachineModels.MINING_LASER_LASER_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderTurbofanItem(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        TurbofanRenderer.renderModelPart(model, "Body", definition.textureLocation(),
                poseStack, buffer, packedLight, packedOverlay, renderMode);
        TurbofanRenderer.renderModelPart(model, "Blades", definition.textureLocation(),
                poseStack, buffer, packedLight, packedOverlay, renderMode);
        TurbofanRenderer.renderModelPart(model, "Afterburner", definition.itemPartTextures().getOrDefault("Afterburner",
                definition.partTextures().getOrDefault("Afterburner", definition.textureLocation())),
                poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    private static void renderLegacyLargeTurbineItem(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyLargeTurbineRenderer.renderModelPart(model, "Body", definition.textureLocation(),
                poseStack, buffer, packedLight, packedOverlay, renderMode);
        LegacyLargeTurbineRenderer.renderModelPart(model, "Blades",
                definition.itemPartTextures().getOrDefault("Blades",
                        definition.partTextures().getOrDefault("Blades", definition.textureLocation())),
                poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay, renderMode);
    }

    private static void renderMachineParts(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyMachinePartRenderSelection.Selection selection = LegacyMachinePartRenderSelection.item(definition);
        renderMachineParts(selection.opaqueRuns(), model, poseStack, buffer, packedLight, packedOverlay,
                LegacyMachinePartRenderContexts.renderMode(definition.renderMode()));
        renderMachineParts(selection.translucentRuns(), model, poseStack, buffer, packedLight, packedOverlay,
                LegacyMachinePartRenderContexts.renderMode(definition.renderMode()));
    }

    private static void renderMachineParts(List<LegacyMachinePartRenderSelection.Run> parts,
            LegacyWavefrontModel model, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyTexturedRenderMode defaultRenderMode) {
        LegacyMachinePartBatchRenderer.renderRuns(parts, model, poseStack, buffer, packedLight, packedOverlay,
                defaultRenderMode);
    }

    private static void renderAutosawPivotedPart(LegacyTileRenderPlans.PivotedModelPartPlan part,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        AutosawRenderer.renderModelPart(part.partName(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderThresherPivotedPart(LegacyTileRenderPlans.PivotedModelPartPlan part,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        ThresherRenderer.renderModelPart(part.partName(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void rotate(PoseStack poseStack, float axisX, float axisY, float axisZ, double degrees) {
        if (axisX != 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) (degrees * axisX)));
        }
        if (axisY != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (degrees * axisY)));
        }
        if (axisZ != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (degrees * axisZ)));
        }
    }

    private record LegacyVisibleInventoryProfile(double yOffsetPixels, double inventoryScale,
            double commonScale, double commonYRotationDegrees) {
    }

    private interface PointTransform {
        Vec3 apply(Vec3 point);
    }
}
