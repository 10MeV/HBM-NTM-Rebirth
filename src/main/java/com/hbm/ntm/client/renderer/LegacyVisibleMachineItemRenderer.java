package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.block.ChargerBlock;
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
import com.hbm.ntm.block.RadioTorchBlock;
import com.hbm.ntm.block.RadioTorchControllerBlock;
import com.hbm.ntm.block.RadioTorchCounterBlock;
import com.hbm.ntm.block.RadioTorchLogicBlock;
import com.hbm.ntm.block.RadioTorchReaderBlock;
import com.hbm.ntm.block.RadioTorchReceiverBlock;
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
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.hbm.ntm.client.obj.ObjRbmkModels;
import com.hbm.ntm.client.obj.ObjLaunchModels;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjNetworkModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.client.obj.ObjParticleAcceleratorModels;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.neutron.RBMKStructureDimensions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LegacyVisibleMachineItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float LEGACY_GUI_SLOT_PIXELS = 16.0F;
    private static final float LEGACY_GUI_MAX_OCCUPANCY = 0.86F;
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();
    private static final LegacyWavefrontModel.SelectionHandle CHARGER_ITEM_PARTS =
            ObjMachineModels.CHARGER.prepareRenderOnlyInCallOrder("Base", "Slide");
    private static final Vec3 FUSION_INV_KLYSTRON_TRANSLATION = new Vec3(0.0D, -3.0D, 1.0D);
    private static final Vec3 FUSION_INV_LOW_TRANSLATION = new Vec3(0.0D, -3.0D, 0.0D);
    private static final Vec3 FUSION_INV_COLLECTOR_TRANSLATION = new Vec3(0.0D, -2.0D, 0.0D);
    private static final Vec3 FUSION_INV_BOILER_TRANSLATION = new Vec3(0.0D, -1.0D, 0.0D);
    private static final double FUSION_INVENTORY_PIXEL_SCALE = 0.0625D;
    private static final double FUSION_COMMON_SCALE = 0.5D;
    private static final double FUSION_CENTERED_GUI_Y = 0.375D;
    private static final double FUSION_KLYSTRON_GUI_Y = 0.5D;
    private static final List<FusionItemAuditContract> FUSION_ITEM_AUDIT_CONTRACTS = List.of(
            fusionItemAuditContract(FusionMachineBlock.Kind.TORUS, "fusion_torus", FUSION_CENTERED_GUI_Y,
                    Vec3.ZERO, 2.0D, false,
                    List.of("Torus", "Magnet"),
                    List.of("Plasma", "Bolts1", "Bolts2", "Bolts3", "Bolts4"), List.of()),
            fusionItemAuditContract(FusionMachineBlock.Kind.KLYSTRON, "fusion_klystron", FUSION_KLYSTRON_GUI_Y,
                    FUSION_INV_KLYSTRON_TRANSLATION, 3.5D, true,
                    List.of("Klystron", "Rotor"), List.of("Pipes"),
                    List.of("inventory_translation_y=-3.0")),
            fusionItemAuditContract(FusionMachineBlock.Kind.KLYSTRON_CREATIVE, "fusion_klystron_creative",
                    FUSION_KLYSTRON_GUI_Y, FUSION_INV_KLYSTRON_TRANSLATION, 3.5D, true,
                    List.of("Klystron", "Rotor"), List.of("Pipes"),
                    List.of("inventory_translation_y=-3.0")),
            fusionItemAuditContract(FusionMachineBlock.Kind.BREEDER, "fusion_breeder", FUSION_CENTERED_GUI_Y,
                    FUSION_INV_LOW_TRANSLATION, 5.0D, true,
                    List.of("Breeder"), List.of("BreederAlt"),
                    List.of("inventory_translation_y=-3.0")),
            fusionItemAuditContract(FusionMachineBlock.Kind.COLLECTOR, "fusion_collector", FUSION_CENTERED_GUI_Y,
                    FUSION_INV_COLLECTOR_TRANSLATION, 5.0D, true,
                    List.of("Collector"), List.of(), List.of("inventory_translation_y=-2.0")),
            fusionItemAuditContract(FusionMachineBlock.Kind.BOILER, "fusion_boiler", FUSION_CENTERED_GUI_Y,
                    FUSION_INV_BOILER_TRANSLATION, 3.5D, true,
                    List.of("Boiler"), List.of(), List.of("inventory_translation_y=-1.0")),
            fusionItemAuditContract(FusionMachineBlock.Kind.COUPLER, "fusion_coupler", FUSION_CENTERED_GUI_Y,
                    FUSION_INV_LOW_TRANSLATION, 6.0D, true,
                    List.of("Coupler"), List.of(), List.of("inventory_translation_y=-3.0")),
            fusionItemAuditContract(FusionMachineBlock.Kind.MHDT, "fusion_mhdt", FUSION_CENTERED_GUI_Y,
                    Vec3.ZERO, 2.5D, true,
                    List.of("Turbine", "Coils"), List.of(),
                    List.of("rotor_degrees=currentMillis/5%30-15 => [-15,15)")),
            fusionItemAuditContract(FusionMachineBlock.Kind.PLASMA_FORGE, "fusion_plasma_forge",
                    FUSION_CENTERED_GUI_Y, FUSION_INV_BOILER_TRANSLATION, 2.75D, true,
                    List.of("Body", "SliderStriker", "ArmLowerStriker", "ArmUpperStriker", "StrikerMount",
                            "StrikerLeft", "StrikerRight", "PistonLeft", "PistonRight", "SliderJet",
                            "ArmLowerJet", "ArmUpperJet", "Jet", "Plasma"),
                    List.of(), List.of("inventory_translation_y=-1.0")));
    private static final BaseInventorySpec MISSILE_PAD_INVENTORY_SPEC =
            baseInventorySpec(scaleBounds(ObjLaunchModels.MISSILE_PAD.boundsAll(), 3.0D));
    private static final DisplaySpec LARGE_LAUNCH_PAD_DISPLAY_SPEC = displaySpec(
            ObjLaunchModels.MISSILE_ERECTOR.boundsOnly("Pad", "Atlas_Pad", "Atlas_Erector", "Atlas_Pivot"),
            0.58F, 0.0F);
    private static final BaseInventorySpec MISSILE_ASSEMBLY_INVENTORY_SPEC = baseInventorySpec(
            scaleTranslateBounds(ObjLaunchModels.MISSILE_ASSEMBLY.boundsAll(), 10.0D, 0.0D, -2.5D, 0.0D));
    private static final DisplaySpec COMPACT_LAUNCHER_DISPLAY_SPEC =
            displaySpec(ObjLaunchModels.COMPACT_LAUNCHER.boundsAll(), 0.68F, 0.0F);
    private static final DisplaySpec LAUNCH_TABLE_DISPLAY_SPEC = displaySpec(
            union(ObjLaunchModels.LAUNCH_TABLE_BASE_LEGACY.boundsAll(),
                    ObjLaunchModels.LAUNCH_TABLE_SMALL_PAD_LEGACY.boundsAll()),
            0.84F, 0.0F);
    private static final DisplaySpec ASSEMBLY_MACHINE_DISPLAY_SPEC = displaySpec(
            rotateYBounds(AssemblyMachineRenderer.MODEL.boundsAll(), 90.0F, 0.75D,
                    0.0D, 0.0D, 0.0D, 0.5D, 0.0D, 0.5D),
            0.58F, 4.5F);
    private static final DisplaySpec BATTERY_SOCKET_DISPLAY_SPEC =
            displaySpec(MachineBatterySocketRenderer.MODEL.boundsOnly("Socket"), 0.58F, 5.0F);
    private static final DisplaySpec CHARGER_DISPLAY_SPEC = displaySpec(
            scaleTranslateBounds(ObjMachineModels.CHARGER.boundsOnly("Base", "Slide"), 2.0D, 1.0D, 0.0D, 0.0D),
            0.58F, 0.0F);
    private static final DisplaySpec ELECTRIC_PRESS_DISPLAY_SPEC = displaySpec(
            union(ObjMachineModels.EPRESS_BODY.boundsAll(),
                    translateBounds(ObjMachineModels.EPRESS_HEAD.boundsAll(), 0.0D, 1.5D, 0.0D)),
            0.58F, 4.5F);
    private static final DisplaySpec AUTOSAW_DISPLAY_SPEC = displaySpec(
            rotateYBounds(ObjMachineModels.AUTOSAW.boundsAll(), -90.0F, 0.5D,
                    0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
            0.58F, 5.0F);
    private static final DisplaySpec THRESHER_DISPLAY_SPEC = displaySpec(
            rotateYBounds(ObjMachineModels.THRESHER.boundsAll(), -90.0F, 0.5D,
                    0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
            0.58F, 4.5F);
    private static final DisplaySpec RBMK_AUTOLOADER_DISPLAY_SPEC =
            displaySpec(translateBounds(ObjRbmkModels.AUTOLOADER.boundsAll(), 0.5D, 0.0D, 0.5D), 0.58F, 0.0F);
    private static final DisplaySpec RBMK_CONSOLE_DISPLAY_SPEC = displaySpec(
            rotateYBounds(ObjRbmkModels.CONSOLE.boundsAll(), 270.0F, 1.0D,
                    0.5D, 0.0D, 0.0D, 0.5D, 0.0D, 0.5D),
            0.58F, 0.0F);
    private static final DisplaySpec RBMK_CRANE_CONSOLE_DISPLAY_SPEC = displaySpec(
            rotateYBounds(ObjRbmkModels.CRANE_CONSOLE.boundsOnly("Console_Coonsole", "Joystick",
                    "Meter1", "Meter2", "Lamp1", "Lamp2"), 270.0F, 1.0D,
                    0.0D, 0.0D, 0.0D, 0.5D, 0.0D, 0.5D),
            0.58F, 0.0F);
    private static final DisplaySpec LPW2_DISPLAY_SPEC =
            displaySpec(com.hbm.ntm.client.obj.ObjReactorModels.LPW2.boundsAll(), 0.58F, 0.0F);
    private static final DisplaySpec CARGO_ELEVATOR_DISPLAY_SPEC = displaySpec(cargoElevatorBounds(), 0.58F, 3.25F);
    private static final DisplaySpec VENDING_SODA_DISPLAY_SPEC =
            displaySpec(VendingMachineRenderer.MODEL.boundsOnly("Soda"), 0.58F, 6.25F);
    private static final DisplaySpec VENDING_OBAMNA_DISPLAY_SPEC =
            displaySpec(VendingMachineRenderer.MODEL.boundsOnly("Obamna"), 0.58F, 6.25F);
    private static final DisplaySpec CONNECTOR_DISPLAY_SPEC =
            displaySpec(scaleBounds(ObjNetworkModels.CONNECTOR_LEGACY.boundsAll(), 2.0D), 0.58F, 7.0F);
    private static final DisplaySpec CONNECTOR_SUPER_DISPLAY_SPEC =
            displaySpec(scaleBounds(ObjNetworkModels.CONNECTOR_SUPER_LEGACY.boundsAll(), 2.0D), 0.58F, 7.0F);
    private static final DisplaySpec LARGE_PYLON_DISPLAY_SPEC =
            displaySpec(scaleBounds(ObjNetworkModels.PYLON_LARGE_LEGACY.boundsAll(), 0.5D), 0.58F, 2.25F);
    private static final DisplaySpec SUBSTATION_DISPLAY_SPEC =
            displaySpec(scaleBounds(ObjNetworkModels.SUBSTATION_LEGACY.boundsAll(), 0.5D), 0.58F, 4.5F);
    private static final DisplaySpec AUTOCAL_DISPLAY_SPEC =
            displaySpec(RadioAutocalRenderer.MODEL.boundsAll(), 0.58F, 6.25F);
    private static final DisplaySpec TELEX_DISPLAY_SPEC =
            displaySpec(translateBounds(RadioTelexRenderer.MODEL.boundsAll(), 0.0D, 0.0D, -0.5D), 0.58F, 6.0F);
    private static final LegacyWavefrontModel RTTY_MODEL = ObjBlockModels.RTTY.asVBO();
    private static final DisplaySpec RTTY_DISPLAY_SPEC = displaySpec(RTTY_MODEL.boundsAll(), 0.58F, 0.0F);

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
                || blockItem.getBlock() instanceof ChargerBlock
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
                || blockItem.getBlock() instanceof RadioTelexBlock
                || blockItem.getBlock() instanceof RadioTorchBlock)) {
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
        } else if (blockItem.getBlock() instanceof ChargerBlock) {
            renderChargerItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
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
        } else if (blockItem.getBlock() instanceof RadioTorchBlock torch) {
            renderRadioTorchItem(torch, displayContext, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderRustedLaunchPadItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyItemBaseInventoryTransform(poseStack, MISSILE_PAD_INVENTORY_SPEC);
            poseStack.translate(0.0D, -1.0D, 0.0D);
            poseStack.scale(3.0F, 3.0F, 3.0F);
        } else {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            poseStack.scale(0.35F, 0.35F, 0.35F);
        }
        ObjLaunchModels.MISSILE_PAD.renderAll(ObjLaunchModels.MISSILE_PAD_RUSTED_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderLaunchPadItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyItemBaseInventoryTransform(poseStack, MISSILE_PAD_INVENTORY_SPEC);
            poseStack.translate(0.0D, -1.0D, 0.0D);
            poseStack.scale(3.0F, 3.0F, 3.0F);
        } else {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            poseStack.scale(0.35F, 0.35F, 0.35F);
        }
        ObjLaunchModels.MISSILE_PAD.renderAll(ObjLaunchModels.MISSILE_PAD_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderLargeLaunchPadItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyCenteredLegacyInventoryObjTransform(poseStack, 0.0D, -3.75D, 0.0D, 1.625D);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        } else {
            applyDisplayTransform(displayContext, poseStack, LARGE_LAUNCH_PAD_DISPLAY_SPEC);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
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
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyItemBaseInventoryTransform(poseStack, MISSILE_ASSEMBLY_INVENTORY_SPEC);
            poseStack.translate(0.0D, -2.5D, 0.0D);
            poseStack.scale(10.0F, 10.0F, 10.0F);
        } else {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            poseStack.scale(0.25F, 0.25F, 0.25F);
        }
        ObjLaunchModels.MISSILE_ASSEMBLY.renderAll(ObjLaunchModels.MISSILE_ASSEMBLY_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderCustomMissileLauncherItem(CustomMissileLauncherBlock.Kind kind,
            ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack,
                kind == CustomMissileLauncherBlock.Kind.COMPACT_LAUNCHER
                        ? COMPACT_LAUNCHER_DISPLAY_SPEC
                        : LAUNCH_TABLE_DISPLAY_SPEC);
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

    private static LegacyWavefrontModel createVisibleMachineModel(LegacyMachineDefinition definition) {
        if (definition.renderProfile() == LegacyMachineRenderProfile.CRUCIBLE_MOLTEN) {
            // The 1.7.10 AdvancedModelLoader crucible path uses face normals in world and inventory rendering.
            return ObjMachineModels.CRUCIBLE_LEGACY;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.BLAST_FURNACE_TILTED_STATE) {
            // The legacy ItemRenderBase path uses the same ResourceManager.blast_furnace.noSmooth()
            // model as the TESR, so inventory/hand previews must keep face normals as well.
            return ObjMachineModels.BLAST_FURNACE_LEGACY;
        }
        if (definition.modelLocation().equals(ObjMachineModels.HEATING_OVEN_LEGACY.modelLocation())) {
            // Ashpit and Heating Oven both use ResourceManager.heater_oven.noSmooth() in 1.7.10.
            return ObjMachineModels.HEATING_OVEN_LEGACY;
        }
        return new LegacyWavefrontModel(definition.modelLocation(), definition.textureLocation()).asVBO();
    }

    private static void renderVisibleMachineItem(ItemStack stack, LegacyMachineDefinition definition, BlockState state,
            ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                LegacyVisibleMachineItemRenderer::createVisibleMachineModel);
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
        if (renderLegacyIronFurnaceNonInventoryItem(definition, model, displayContext, poseStack, buffer,
                packedLight, packedOverlay)) {
            poseStack.popPose();
            return;
        }
        if (renderLegacySteelFurnaceNonInventoryItem(definition, model, displayContext, poseStack, buffer,
                packedLight, packedOverlay)) {
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

    /**
     * Read-only, source-backed Fusion inventory contracts used by the opt-in runtime audit.
     *
     * <p>Negative translations and the signed MHDT rotor interval are deliberately retained as
     * documented legacy values. Only local scale components are required to stay strictly positive;
     * the GUI's inherited one-axis reflection is a screen-space input, not machine state.</p>
     */
    public static List<FusionItemAuditContract> fusionItemAuditContracts() {
        return FUSION_ITEM_AUDIT_CONTRACTS;
    }

    private static FusionItemAuditContract fusionItemAuditContract(FusionMachineBlock.Kind kind) {
        FusionItemAuditContract contract = FUSION_ITEM_AUDIT_CONTRACTS.get(kind.ordinal());
        if (contract.kind() != kind) {
            throw new IllegalStateException("Fusion item audit contract order drift for " + kind);
        }
        return contract;
    }

    private static FusionItemAuditContract fusionItemAuditContract(FusionMachineBlock.Kind kind, String id,
            double guiAnchorY, Vec3 translation, double inventoryScale,
            boolean inventoryRotates, List<String> includedParts, List<String> excludedParts,
            List<String> legalSignedFields) {
        return new FusionItemAuditContract(kind, id, guiAnchorY, translation.x, translation.y, translation.z,
                FUSION_INVENTORY_PIXEL_SCALE, inventoryScale, FUSION_COMMON_SCALE, inventoryRotates,
                LegacyTexturedRenderMode.CUTOUT_REVERSED_CULL,
                LegacyTexturedRenderMode.CUTOUT_CULL, LegacyTexturedRenderMode.CUTOUT_CULL, true,
                includedParts, excludedParts, legalSignedFields);
    }

    private static LegacyWavefrontModel fusionAuditSourceModel(FusionMachineBlock.Kind kind) {
        return switch (kind) {
            case TORUS -> ObjFusionModels.TORUS_LEGACY;
            case KLYSTRON, KLYSTRON_CREATIVE -> ObjFusionModels.KLYSTRON_LEGACY;
            case BREEDER -> ObjFusionModels.BREEDER_LEGACY;
            case COLLECTOR -> ObjFusionModels.COLLECTOR_LEGACY;
            case BOILER -> ObjFusionModels.BOILER_LEGACY;
            case COUPLER -> ObjFusionModels.COUPLER_LEGACY;
            case MHDT -> ObjFusionModels.MHDT_LEGACY;
            case PLASMA_FORGE -> ObjFusionModels.PLASMA_FORGE_LEGACY;
        };
    }

    private static void renderFusionMachineItem(FusionMachineBlock.Kind kind, LegacyMachineDefinition definition,
            ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()).asVBO());
        FusionItemAuditContract contract = fusionItemAuditContract(kind);
        FusionLegacyItemContext legacyContext = fusionLegacyItemContext(displayContext);
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyInventoryObjTransformAtGuiY(poseStack, contract.guiAnchorY(),
                    contract.translationX(), contract.translationY(), contract.translationZ(),
                    contract.inventoryScale());
            if (contract.inventoryRotates()) {
                LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
            }
            poseStack.scale((float) contract.commonScale(), (float) contract.commonScale(),
                    (float) contract.commonScale());
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        } else if (legacyContext != null) {
            /*
             * All nine 1.7.10 Fusion providers inherit ItemRenderBase and leave renderNonInv()
             * empty. Their per-machine inventory transforms therefore do not participate here:
             * ForgeHooksClient's caller matrix must run before the ItemRenderBase
             * ENTITY/EQUIPPED/EQUIPPED_FIRST_PERSON matrix, followed by the shared Fusion
             * common S(.5) -> Ry(90). FIXED is the modern carrier for the old item-frame
             * ENTITY path.
             */
            applyFusionLegacyNonGuiTransform(legacyContext, poseStack);
            poseStack.scale((float) contract.commonScale(), (float) contract.commonScale(),
                    (float) contract.commonScale());
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        } else {
            // HEAD/NONE have no 1.7.10 IItemRenderer equivalent and retain the bounded modern fallback.
            applyFusionModernFallbackTransform(kind, definition, model, displayContext, poseStack);
        }
        renderFusionItemParts(contract, definition, model, poseStack, buffer, packedLight, packedOverlay,
                fusionItemCullMode(contract, displayContext), System.currentTimeMillis());
        poseStack.popPose();
    }

    private enum FusionLegacyItemContext { ENTITY, EQUIPPED, EQUIPPED_FIRST_PERSON }

    private static FusionLegacyItemContext fusionLegacyItemContext(ItemDisplayContext displayContext) {
        return switch (displayContext) {
            case GROUND, FIXED -> FusionLegacyItemContext.ENTITY;
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> FusionLegacyItemContext.EQUIPPED;
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> FusionLegacyItemContext.EQUIPPED_FIRST_PERSON;
            case GUI, HEAD, NONE -> null;
        };
    }

    private static void applyFusionLegacyNonGuiTransform(FusionLegacyItemContext legacyContext,
            PoseStack poseStack) {
        /*
         * ItemRenderer.render(...) translates (-0.5,-0.5,-0.5) immediately before dispatching
         * to a builtin/entity BEWLR. Cancel that modern cube-origin convention first; the old
         * custom item renderer received the Forge carrier matrix without this translation.
         *
         * Forge 1.7.10 applies these transforms before invoking IItemRenderer. Fusion blocks
         * inherit BlockDummyable#getRenderType() == -1 and ItemRenderBase does not request
         * BLOCK_3D/EQUIPPED_BLOCK, so they always take the non-3D ENTITY branch or the default
         * equipped branch in ForgeHooksClient.
         */
        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (legacyContext == FusionLegacyItemContext.ENTITY) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.scale(1.5F, 1.5F, 1.5F);
        } else {
            poseStack.translate(0.0D, -0.3D, 0.0D);
            poseStack.scale(1.5F, 1.5F, 1.5F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 50.0F);
            LegacyPoseRotations.rotateZDegrees(poseStack, 335.0F);
            poseStack.translate(-0.9375D, -0.0625D, 0.0D);
            poseStack.translate(0.5D, 0.25D, 0.0D);
        }
        poseStack.scale(0.25F, 0.25F, 0.25F);
        if (legacyContext != FusionLegacyItemContext.EQUIPPED) {
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        }
    }

    private static void applyFusionModernFallbackTransform(FusionMachineBlock.Kind kind,
            LegacyMachineDefinition definition, LegacyWavefrontModel model, ItemDisplayContext displayContext,
            PoseStack poseStack) {
        AABB bounds = rotateYBounds(fusionItemBounds(kind, model), 90.0F, 1.0D,
                0.0D, 0.0D, 0.0D, 0.5D, 0.0D, 0.5D);
        applyDisplayTransform(displayContext, poseStack, bounds, definition.itemFitSize(), 0.0F);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
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

    private static LegacyTexturedRenderMode fusionItemCullMode(FusionItemAuditContract contract,
            ItemDisplayContext displayContext) {
        /*
         * GuiGraphics supplies BEWLRs with an inherited (1,-1,1) screen-space reflection.
         * LegacyWavefrontModel correctly reverses culling for an object-local mirrored pose,
         * but that inherited GUI reflection is cancelled by the GUI projection and must not
         * turn these positive-scale legacy inventory matrices inside-out. Supplying the paired
         * mode here lets the shared determinant resolver cancel only the GUI reflection; world,
         * ground, fixed and hand contexts continue to resolve ordinary CUTOUT_CULL.
         */
        return displayContext == ItemDisplayContext.GUI
                ? contract.requestedGuiCullMode()
                : contract.requestedNonGuiCullMode();
    }

    private static void renderFusionItemParts(FusionItemAuditContract contract, LegacyMachineDefinition definition,
            LegacyWavefrontModel model, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyTexturedRenderMode opaqueRenderMode, long currentMillis) {
        List<String> includedParts = contract.includedParts();
        switch (contract.kind()) {
            case TORUS -> {
                ObjFusionModels.renderTorusPart(model, definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, opaqueRenderMode, includedParts.get(0));
                poseStack.pushPose();
                LegacyPoseRotations.rotateYDegrees(poseStack, (float) (currentMillis / 5.0D % 360.0D));
                ObjFusionModels.renderTorusPart(model, definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, opaqueRenderMode, includedParts.get(1));
                poseStack.popPose();
            }
            case KLYSTRON, KLYSTRON_CREATIVE -> {
                ObjFusionModels.renderKlystronPart(model, definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, opaqueRenderMode, includedParts.get(0));
                poseStack.pushPose();
                poseStack.translate(0.0D, 2.5D, 0.0D);
                LegacyPoseRotations.rotateXDegrees(poseStack, (float) (currentMillis / 10.0D % 360.0D));
                poseStack.translate(0.0D, -2.5D, 0.0D);
                ObjFusionModels.renderKlystronPart(model, definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, opaqueRenderMode, includedParts.get(1));
                poseStack.popPose();
            }
            case BREEDER -> ObjFusionModels.renderBreederPart(model, definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, opaqueRenderMode, includedParts.get(0));
            case COLLECTOR, BOILER, COUPLER -> model.renderAll(definition.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, opaqueRenderMode);
            case MHDT -> {
                ObjFusionModels.renderMhdtPart(model, definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, opaqueRenderMode, includedParts.get(0));
                double rotor = currentMillis / 5.0D % 30.0D - 15.0D;
                poseStack.pushPose();
                poseStack.translate(0.0D, 1.5D, 0.0D);
                LegacyPoseRotations.rotateXDegrees(poseStack, (float) rotor);
                poseStack.translate(0.0D, -1.5D, 0.0D);
                ObjFusionModels.renderMhdtPart(model, definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, opaqueRenderMode, includedParts.get(1));
                poseStack.popPose();
            }
            case PLASMA_FORGE -> {
                ObjFusionModels.renderPlasmaForgeItemBody(definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, opaqueRenderMode);
                ObjFusionModels.renderPlasmaForgePartUntextured(poseStack, buffer, 0, 0, 0, 255,
                        opaqueRenderMode, includedParts.get(includedParts.size() - 1));
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

        if (profile.centeredInGui()) {
            applyCenteredLegacyInventoryObjTransform(poseStack, profile.yOffsetPixels(), profile.inventoryScale());
        } else {
            applyLegacyInventoryObjTransform(poseStack, profile.yOffsetPixels(), profile.inventoryScale());
        }
        if (profile.commonScale() != 1.0D) {
            poseStack.scale((float) profile.commonScale(), (float) profile.commonScale(), (float) profile.commonScale());
        }
        if (profile.commonYRotationDegrees() != 0.0D) {
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) profile.commonYRotationDegrees());
        }

        if (isLegacyFluidTankModel(definition)) {
            renderLegacyFluidTankInventoryItem(stack, state, poseStack, buffer, packedLight, packedOverlay);
        } else if (definition.itemRenderAll()) {
            model.renderAll(definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                    legacyVisibleItemCullMode(definition, ItemDisplayContext.GUI));
        } else {
            renderMachineParts(definition, model, poseStack, buffer, packedLight, packedOverlay,
                    legacyVisibleItemCullMode(definition, ItemDisplayContext.GUI));
        }
        return true;
    }

    private static boolean renderLegacyIronFurnaceNonInventoryItem(LegacyMachineDefinition definition,
            LegacyWavefrontModel model, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        FusionLegacyItemContext legacyContext = fusionLegacyItemContext(displayContext);
        if (!"models/machines/furnace_iron.obj".equals(definition.modelLocation().getPath())
                || legacyContext == null) {
            return false;
        }

        // Replay the complete 1.7.10 Forge caller + ItemRenderBase matrix. Applying only
        // ItemRenderBase here leaves the modern BEWLR cube-origin translation in place and
        // is the source of the hand/entity offset that made the OBJ appear detached.
        applyFusionLegacyNonGuiTransform(legacyContext, poseStack);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        renderMachineParts(definition, model, poseStack, buffer, packedLight, packedOverlay,
                legacyVisibleItemCullMode(definition, displayContext));
        return true;
    }

    private static boolean renderLegacySteelFurnaceNonInventoryItem(LegacyMachineDefinition definition,
            LegacyWavefrontModel model, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        FusionLegacyItemContext legacyContext = fusionLegacyItemContext(displayContext);
        if (!"models/machines/furnace_steel.obj".equals(definition.modelLocation().getPath())
                || legacyContext == null) {
            return false;
        }

        applyFusionLegacyNonGuiTransform(legacyContext, poseStack);
        model.renderAll(definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                legacyVisibleItemCullMode(definition, displayContext));
        return true;
    }

    private static LegacyTexturedRenderMode legacyVisibleItemCullMode(LegacyMachineDefinition definition,
            ItemDisplayContext displayContext) {
        LegacyTexturedRenderMode renderMode = LegacyMachinePartRenderContexts.renderMode(definition.renderMode());
        if (displayContext != ItemDisplayContext.GUI) {
            return renderMode;
        }
        String modelPath = definition.modelLocation().getPath();
        if (("models/machines/furnace_iron.obj".equals(modelPath)
                || "models/machines/furnace_steel.obj".equals(modelPath))
                && renderMode == LegacyTexturedRenderMode.CUTOUT_CULL) {
            /*
             * GuiGraphics contributes a (1,-1,1) carrier reflection. The shared OBJ backend
             * normally reverses culling for a reflected pose, but 1.7.10 ItemRenderBase's
             * inventory projection and local (-1,-1,-1) transform already form the matching
             * front-face convention. Request the paired mode so the determinant resolver emits
             * ordinary CUTOUT_CULL instead of exposing the inside of these closed furnaces.
             */
            return LegacyTexturedRenderMode.CUTOUT_REVERSED_CULL;
        }
        return renderMode;
    }

    private static LegacyVisibleInventoryProfile legacyVisibleInventoryProfile(LegacyMachineDefinition definition) {
        String path = definition.modelLocation().getPath();
        return switch (path) {
            case "models/machines/furnace_iron.obj" -> new LegacyVisibleInventoryProfile(-2.0D, 5.0D, 1.0D,
                    90.0D, false);
            case "models/machines/furnace_steel.obj" -> new LegacyVisibleInventoryProfile(-1.5D, 3.25D, 1.0D,
                    0.0D, false);
            case "models/fluidtank.obj" -> new LegacyVisibleInventoryProfile(-2.0D, 3.5D, 0.75D, 90.0D,
                    false);
            case "models/reactors/watz.obj" -> new LegacyVisibleInventoryProfile(-1.0D, 2.0D, 1.0D, 0.0D, true);
            case "models/machines/watz_pump.obj" -> new LegacyVisibleInventoryProfile(-1.5D, 5.0D, 1.0D, 0.0D,
                    true);
            case "models/zirnox.obj" -> new LegacyVisibleInventoryProfile(-2.0D, 2.8D, 0.75D, 0.0D, true);
            case "models/reactors/icf.obj" -> new LegacyVisibleInventoryProfile(-1.5D, 2.125D, 0.5D, 90.0D, true);
            default -> null;
        };
    }

    private static boolean isLegacyFluidTankModel(LegacyMachineDefinition definition) {
        return "models/fluidtank.obj".equals(definition.modelLocation().getPath());
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
        LegacyFluidTankRenderHelper.renderSmallTankBody(ObjMachineModels.FLUIDTANK,
                ObjMachineModels.FLUIDTANK_EXPLODED,
                tank, exploded, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderPressItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyCenteredLegacyInventoryObjTransform(poseStack, -4.0D, 4.5D);
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
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, ASSEMBLY_MACHINE_DISPLAY_SPEC);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        AssemblyMachineRenderer.MODEL.renderAll(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderBatterySocketItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, BATTERY_SOCKET_DISPLAY_SPEC);
        MachineBatterySocketRenderer.renderModelPart("Socket", MachineBatterySocketRenderer.SOCKET_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderChargerItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyCenteredLegacyInventoryObjTransform(poseStack, -7.0D, 10.0D);
        } else {
            applyDisplayTransform(displayContext, poseStack, CHARGER_DISPLAY_SPEC);
        }
        poseStack.scale(2.0F, 2.0F, 2.0F);
        poseStack.translate(0.5D, 0.0D, 0.0D);
        ObjMachineModels.CHARGER.renderOnlyInCallOrder(ObjMachineModels.CHARGER_TEXTURE, poseStack, buffer,
                packedLight, packedOverlay, CHARGER_ITEM_PARTS);
        poseStack.popPose();
    }

    private static void renderElectricPressItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, ELECTRIC_PRESS_DISPLAY_SPEC);
        ObjMachineModels.EPRESS_BODY.renderAll(ObjMachineModels.EPRESS_BODY_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, 1.5D, 0.0D);
        ObjMachineModels.EPRESS_HEAD.renderAll(ObjMachineModels.EPRESS_HEAD_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderAutosawItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyTileRenderPlans.AutosawPlan plan =
                LegacyTileRenderPlans.autosawItemPlan(System.currentTimeMillis());

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, AUTOSAW_DISPLAY_SPEC);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        LegacyPoseRotations.rotateYDegrees(poseStack, -(90.0F));
        AutosawRenderer.renderModelPart("Base", poseStack, buffer, packedLight, packedOverlay);
        LegacyPoseRotations.rotateYDegrees(poseStack, -((float) plan.turnDegrees()));
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
        LegacyTileRenderPlans.ThresherPlan plan =
                LegacyTileRenderPlans.thresherItemPlan(System.currentTimeMillis());

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, THRESHER_DISPLAY_SPEC);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        LegacyPoseRotations.rotateYDegrees(poseStack, -(90.0F));
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
        double topHeight = block.kind().control()
                ? 1.25D
                : RBMKColumnRenderer.hasLegacyTopPipePads(block.kind(), RBMKColumnBlock.LidType.NONE)
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
        } else if (block.kind().control()) {
            // RenderRBMKControl#renderInventoryBlock leaves the transform at
            // the fourth section, then renders the complete moving Lid OBJ.
            // At the default zero insertion level its connector bars meet the
            // four pipe pads and its plate reaches heightAbove + 1.25.
            poseStack.pushPose();
            poseStack.translate(0.5D, heightAbove, 0.5D);
            ObjRbmkModels.renderControlLid(block.kind().automatic()
                            ? ObjRbmkModels.CONTROL_AUTO_TEXTURE
                            : ObjRbmkModels.CONTROL_STANDARD_TEXTURE,
                    poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderRbmkAutoloaderItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, RBMK_AUTOLOADER_DISPLAY_SPEC);
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

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, RBMK_CONSOLE_DISPLAY_SPEC);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, yaw);
        poseStack.translate(0.5D, 0.0D, 0.0D);
        ObjRbmkModels.CONSOLE.renderAll(ObjRbmkModels.CONSOLE_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderRbmkCraneConsoleItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float yaw = 270.0F;

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, RBMK_CRANE_CONSOLE_DISPLAY_SPEC);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, yaw);
        LegacyRbmkMachineRenderer.renderCraneConsole(poseStack, buffer, packedLight, packedOverlay,
                LegacyRbmkMachineRenderer.CraneConsoleState.EMPTY, 0.0F, System.currentTimeMillis());
        poseStack.popPose();
    }

    private static void renderLpw2Item(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, LPW2_DISPLAY_SPEC);
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
            applyLegacyInventoryObjTransformAtGuiY(poseStack, 0.375D, 0.0D,
                    particleAcceleratorInventoryYOffset(variant), 0.0D,
                    particleAcceleratorInventoryScale(variant));
        } else {
            applyLegacyItemBaseNonInventoryTransform(displayContext, poseStack);
        }
        double commonScale = particleAcceleratorCommonScale(variant);
        if (commonScale != 1.0D) {
            poseStack.scale((float) commonScale, (float) commonScale, (float) commonScale);
        }
        if (variant != ParticleAcceleratorBlock.Variant.DIPOLE) {
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
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
        if (displayContext == ItemDisplayContext.GROUND || displayContext == ItemDisplayContext.FIXED) {
            poseStack.scale(1.5F, 1.5F, 1.5F);
        } else {
            poseStack.translate(0.5D, 0.25D, 0.0D);
        }
        poseStack.scale(0.25F, 0.25F, 0.25F);
        if (displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
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
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, CARGO_ELEVATOR_DISPLAY_SPEC);
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

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack,
                variant == 0 ? VENDING_SODA_DISPLAY_SPEC : VENDING_OBAMNA_DISPLAY_SPEC);
        VendingMachineRenderer.render(state, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderMediumPylonItem(LegacyMediumPylonBlock.Kind kind, ItemDisplayContext displayContext,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB rawBounds = kind.transformer()
                ? ObjNetworkModels.PYLON_MEDIUM_LEGACY.boundsOnly("Pylon", "Transformer")
                : ObjNetworkModels.PYLON_MEDIUM_LEGACY.boundsOnly("Pylon");
        AABB bounds = rotateYBounds(rawBounds, 90.0F, 0.5D,
                0.75D, 0.0D, 0.0D, 0.5D, 0.0D, 0.5D);

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 4.5F);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
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
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyCenteredLegacyInventoryObjTransform(poseStack,
                    kind == LegacyConnectorBlock.Kind.SUPER ? -5.0D : -3.5D, 7.0D);
        } else {
            applyDisplayTransform(displayContext, poseStack,
                    kind == LegacyConnectorBlock.Kind.SUPER
                            ? CONNECTOR_SUPER_DISPLAY_SPEC
                            : CONNECTOR_DISPLAY_SPEC);
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
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyCenteredLegacyInventoryObjTransform(poseStack, -5.0D, 2.25D);
        } else {
            applyDisplayTransform(displayContext, poseStack, LARGE_PYLON_DISPLAY_SPEC);
        }
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjNetworkModels.PYLON_LARGE_LEGACY.renderAll(LegacyPylonRenderer.PYLON_LARGE_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderSubstationItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, SUBSTATION_DISPLAY_SPEC);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjNetworkModels.SUBSTATION_LEGACY.renderAll(LegacyPylonRenderer.SUBSTATION_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderAutocalItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, AUTOCAL_DISPLAY_SPEC);
        RadioAutocalRenderer.MODEL.renderAll(RadioAutocalRenderer.TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderTelexItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, TELEX_DISPLAY_SPEC);
        poseStack.translate(0.0D, 0.0D, -0.5D);
        RadioTelexRenderer.MODEL.renderAll(RadioTelexRenderer.TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    /**
     * The old RenderRTTY deliberately skipped inventory rendering. The modern item has a
     * visible model, so all display contexts use that exact shared RTTY OBJ and its legacy
     * inactive texture instead of the temporary cuboid item model.
     */
    private static void renderRadioTorchItem(RadioTorchBlock torch, ItemDisplayContext displayContext,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, RTTY_DISPLAY_SPEC);
        RTTY_MODEL.renderWithSprite(rttyItemSprite(torch), poseStack, buffer, packedLight, packedOverlay,
                0.0F, 0.0F, 0.0F, false);
        poseStack.popPose();
    }

    private static TextureAtlasSprite rttyItemSprite(RadioTorchBlock torch) {
        String texture = torch instanceof RadioTorchReceiverBlock ? "rtty_rec_off"
                : torch instanceof RadioTorchLogicBlock ? "rtty_logic_off"
                : torch instanceof RadioTorchReaderBlock ? "rtty_reader"
                : torch instanceof RadioTorchCounterBlock ? "rtty_counter"
                : torch instanceof RadioTorchControllerBlock ? "rtty_controller"
                : "rtty_sender_off";
        return LegacyTexturedQuadRenderer.blockSprite(HbmNtm.MOD_ID, "block/" + texture);
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
            LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.scale(fitScale, fitScale, fitScale);
            poseStack.translate(-center.x, -center.y, -center.z);
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
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

    private static void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack,
            DisplaySpec spec) {
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.scale(spec.fitScale(), spec.fitScale(), spec.fitScale());
            poseStack.translate(-spec.centerX(), -spec.centerY(), -spec.centerZ());
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        float worldScale = spec.fitScale() * 0.82F;
        poseStack.scale(worldScale, worldScale, worldScale);
        poseStack.translate(-spec.centerX(), -spec.centerY(), -spec.centerZ());

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
        LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.scale(fitScale, fitScale, fitScale);
        poseStack.translate(-center.x, -center.y, -center.z);
    }

    private static void applyLegacyItemBaseInventoryTransform(PoseStack poseStack, BaseInventorySpec spec) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.scale(spec.fitScale(), spec.fitScale(), spec.fitScale());
        poseStack.translate(-spec.centerX(), -spec.centerY(), -spec.centerZ());
    }

    private static void applyLegacyInventoryObjTransform(PoseStack poseStack, double yOffsetPixels,
            double inventoryScale) {
        applyLegacyInventoryObjTransform(poseStack, 0.0D, yOffsetPixels, 0.0D, inventoryScale);
    }

    /**
     * Applies the legacy OBJ rotation and pixel-space offsets while anchoring the model to the modern GUI slot
     * center.  Kept opt-in because most legacy item previews intentionally retain their original 10px baseline.
     */
    private static void applyCenteredLegacyInventoryObjTransform(PoseStack poseStack, double yOffsetPixels,
            double inventoryScale) {
        applyCenteredLegacyInventoryObjTransform(poseStack, 0.0D, yOffsetPixels, 0.0D, inventoryScale);
    }

    private static void applyCenteredLegacyInventoryObjTransform(PoseStack poseStack, double xOffsetPixels,
            double yOffsetPixels, double zOffsetPixels, double inventoryScale) {
        applyLegacyInventoryObjTransformAtGuiY(poseStack, 0.375D, xOffsetPixels, yOffsetPixels, zOffsetPixels,
                inventoryScale);
    }

    private static void applyLegacyInventoryObjTransformAtGuiY(PoseStack poseStack, double guiY,
            double xOffsetPixels, double yOffsetPixels, double zOffsetPixels, double inventoryScale) {
        poseStack.translate(0.5D, guiY, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        poseStack.translate(xOffsetPixels, yOffsetPixels, zOffsetPixels);
        poseStack.scale((float) inventoryScale, (float) inventoryScale, (float) inventoryScale);
    }

    private static void applyLegacyInventoryObjTransform(PoseStack poseStack, double xOffsetPixels,
            double yOffsetPixels, double zOffsetPixels, double inventoryScale) {
        poseStack.translate(0.5D, 0.625D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        poseStack.translate(xOffsetPixels, yOffsetPixels, zOffsetPixels);
        poseStack.scale((float) inventoryScale, (float) inventoryScale, (float) inventoryScale);
    }

    private static AABB transformVisibleBounds(AABB bounds, LegacyMachineDefinition definition, BlockState state) {
        Vec3 translation = definition.modelTranslation(state);
        float yRotation = definition.yRotation(state);
        float postModelYRotation = definition.postModelYRotation(state);
        double postSin = LegacyTransformedBounds.sinDeg(postModelYRotation);
        double postCos = LegacyTransformedBounds.cosDeg(postModelYRotation);
        double sin = LegacyTransformedBounds.sinDeg(yRotation);
        double cos = LegacyTransformedBounds.cosDeg(yRotation);
        return LegacyTransformedBounds.transform(bounds, (x, y, z, accumulator) -> {
            double postX = LegacyTransformedBounds.rotateYX(x, z, postSin, postCos) + translation.x;
            double postZ = LegacyTransformedBounds.rotateYZ(x, z, postSin, postCos) + translation.z;
            double finalX = LegacyTransformedBounds.rotateYX(postX, postZ, sin, cos) + 0.5D;
            double finalZ = LegacyTransformedBounds.rotateYZ(postX, postZ, sin, cos) + 0.5D;
            accumulator.include(finalX, y + translation.y, finalZ);
        });
    }

    private static AABB scaleBounds(AABB bounds, double scale) {
        return scaleTranslateBounds(bounds, scale, 0.0D, 0.0D, 0.0D);
    }

    private static AABB translateBounds(AABB bounds, double x, double y, double z) {
        return scaleTranslateBounds(bounds, 1.0D, x, y, z);
    }

    private static AABB scaleTranslateBounds(AABB bounds, double scale, double x, double y, double z) {
        return LegacyTransformedBounds.transform(bounds, (cornerX, cornerY, cornerZ, accumulator) ->
                accumulator.include(cornerX * scale + x, cornerY * scale + y, cornerZ * scale + z));
    }

    private static AABB cargoElevatorBounds() {
        AABB rawBounds = ObjMachineModels.ELEVATOR_LEGACY.boundsOnly("Base", "Piston", "Guides", "Platform");
        AABB piston2 = translateBounds(ObjMachineModels.ELEVATOR_LEGACY.boundsOnly("Piston", "Guides", "Platform"),
                0.0D, 1.0D, 0.0D);
        AABB guides3 = translateBounds(ObjMachineModels.ELEVATOR_LEGACY.boundsOnly("Guides"),
                0.0D, 2.0D, 0.0D);
        return union(union(rawBounds, piston2), guides3);
    }

    private static DisplaySpec displaySpec(AABB bounds, float targetSize, float legacyItemScale) {
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        float resolvedTargetSize = targetSize;
        if (legacyItemScale > 0.0F) {
            resolvedTargetSize = (float) Math.min(LEGACY_GUI_MAX_OCCUPANCY,
                    maxSize * legacyItemScale / LEGACY_GUI_SLOT_PIXELS);
        }
        return new DisplaySpec(
                (bounds.minX + bounds.maxX) * 0.5D,
                (bounds.minY + bounds.maxY) * 0.5D,
                (bounds.minZ + bounds.maxZ) * 0.5D,
                (float) Math.max(0.035D,
                        Math.min(0.32D, resolvedTargetSize / Math.max(1.0D, maxSize))));
    }

    private static BaseInventorySpec baseInventorySpec(AABB bounds) {
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        return new BaseInventorySpec(
                (bounds.minX + bounds.maxX) * 0.5D,
                (bounds.minY + bounds.maxY) * 0.5D,
                (bounds.minZ + bounds.maxZ) * 0.5D,
                (float) Math.max(0.025D,
                        Math.min(0.32D, LEGACY_GUI_MAX_OCCUPANCY / Math.max(1.0D, maxSize))));
    }

    private static AABB rotateYBounds(AABB bounds, float degrees, double scale,
            double preX, double preY, double preZ, double postX, double postY, double postZ) {
        double sin = LegacyTransformedBounds.sinDeg(degrees);
        double cos = LegacyTransformedBounds.cosDeg(degrees);
        return LegacyTransformedBounds.transform(bounds, (x, y, z, accumulator) -> {
            double scaledX = x * scale + preX;
            double scaledY = y * scale + preY;
            double scaledZ = z * scale + preZ;
            double rotatedX = LegacyTransformedBounds.rotateYX(scaledX, scaledZ, sin, cos) + postX;
            double rotatedZ = LegacyTransformedBounds.rotateYZ(scaledX, scaledZ, sin, cos) + postZ;
            accumulator.include(rotatedX, scaledY + postY, rotatedZ);
        });
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

    private record DisplaySpec(double centerX, double centerY, double centerZ, float fitScale) {
    }

    private record BaseInventorySpec(double centerX, double centerY, double centerZ, float fitScale) {
    }

    private static void renderMachine(LegacyMachineDefinition definition, BlockState state, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, definition.yRotation(state));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        LegacyPoseRotations.rotateYDegrees(poseStack, definition.postModelYRotation(state));

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

        for (int i = 0; i < 4; i++) {
            renderPrecassArm(definition, model, poseStack, buffer, packedLight, packedOverlay, renderMode,
                    45.0D, -30.0D, 45.0D, 0.0D);
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        }
    }

    private static void renderPrecassArm(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, double lowerAngle, double upperAngle, double headAngle,
            double striker) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.625D, 0.9375D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) lowerAngle);
        poseStack.translate(0.0D, -1.625D, -0.9375D);
        AssemblyMachineRenderer.renderModelPart(model, "ArmLower1", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);

        poseStack.translate(0.0D, 2.375D, 0.9375D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) upperAngle);
        poseStack.translate(0.0D, -2.375D, -0.9375D);
        AssemblyMachineRenderer.renderModelPart(model, "ArmUpper1", definition.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, renderMode);

        poseStack.translate(0.0D, 2.375D, 0.4375D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) headAngle);
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
        LegacyPoseRotations.rotateXDegrees(poseStack, 90.0F);
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
                poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    private static void renderMachineParts(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderMachineParts(definition, model, poseStack, buffer, packedLight, packedOverlay,
                LegacyMachinePartRenderContexts.renderMode(definition.renderMode()));
    }

    private static void renderMachineParts(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode defaultRenderMode) {
        LegacyMachinePartRenderSelection.Selection selection = LegacyMachinePartRenderSelection.item(definition);
        renderMachineParts(selection.opaqueRuns(), model, poseStack, buffer, packedLight, packedOverlay,
                defaultRenderMode);
        renderMachineParts(selection.translucentRuns(), model, poseStack, buffer, packedLight, packedOverlay,
                defaultRenderMode);
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
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) (degrees * axisX));
        }
        if (axisY != 0.0F) {
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) (degrees * axisY));
        }
        if (axisZ != 0.0F) {
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) (degrees * axisZ));
        }
    }

    private record LegacyVisibleInventoryProfile(double yOffsetPixels, double inventoryScale,
            double commonScale, double commonYRotationDegrees, boolean centeredInGui) {
    }

    public record FusionItemAuditContract(FusionMachineBlock.Kind kind, String id, double guiAnchorY,
            double translationX, double translationY, double translationZ, double legacyPixelScale,
            double inventoryScale, double commonScale, boolean inventoryRotates,
            LegacyTexturedRenderMode requestedGuiCullMode, LegacyTexturedRenderMode requestedNonGuiCullMode,
            LegacyTexturedRenderMode expectedEffectiveGuiCullMode, boolean inheritedGuiReflectionExpected,
            List<String> includedParts, List<String> excludedParts, List<String> legalSignedFields) {

        public FusionItemAuditContract {
            includedParts = List.copyOf(includedParts);
            excludedParts = List.copyOf(excludedParts);
            legalSignedFields = List.copyOf(legalSignedFields);
        }

        public boolean allLocalScalesStrictlyPositive() {
            return Double.isFinite(legacyPixelScale) && legacyPixelScale > 0.0D
                    && Double.isFinite(inventoryScale) && inventoryScale > 0.0D
                    && Double.isFinite(commonScale) && commonScale > 0.0D;
        }

        public int negativeLocalScaleCount() {
            int count = 0;
            count += legacyPixelScale < 0.0D ? 1 : 0;
            count += inventoryScale < 0.0D ? 1 : 0;
            count += commonScale < 0.0D ? 1 : 0;
            return count;
        }

        public double guiLocalScaleDeterminant() {
            return Math.pow(legacyPixelScale, 3.0D)
                    * Math.pow(inventoryScale, 3.0D)
                    * Math.pow(commonScale, 3.0D);
        }

        public int negativeTranslationComponentCount() {
            int count = 0;
            count += translationX < 0.0D ? 1 : 0;
            count += translationY < 0.0D ? 1 : 0;
            count += translationZ < 0.0D ? 1 : 0;
            return count;
        }

        public String sourceModelLocation() {
            return fusionAuditSourceModel(kind).modelLocation().toString();
        }

        public List<String> missingSourceModelParts() {
            LegacyWavefrontModel model = fusionAuditSourceModel(kind);
            List<String> missing = new ArrayList<>();
            includedParts.stream().filter(part -> !model.hasPart(part)).forEach(missing::add);
            excludedParts.stream().filter(part -> !model.hasPart(part)).forEach(missing::add);
            return List.copyOf(missing);
        }
    }

}
