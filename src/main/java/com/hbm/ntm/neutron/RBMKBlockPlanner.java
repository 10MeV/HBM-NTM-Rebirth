package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public final class RBMKBlockPlanner {
    public static final int DUMMY_EXTRA_OFFSET = 6;
    public static final int CORE_METADATA_OFFSET = 10;
    public static final int DEFAULT_GUI_ID = 0;
    public static final float LEGACY_HARDNESS = 3.0F;
    public static final float LEGACY_RESISTANCE = 30.0F;
    public static final double LID_COLLISION_HEIGHT = 0.25D;
    public static final double DETAILED_HITBOX_MAX_Y = 0.999D;

    private RBMKBlockPlanner() {
    }

    public static MetadataKind metadataKind(int meta) {
        if (meta >= CORE_METADATA_OFFSET + Direction.NORTH.ordinal()) {
            return MetadataKind.CORE;
        }
        if (hasExtra(meta)) {
            return MetadataKind.EXTRA_DUMMY;
        }
        return MetadataKind.DUMMY;
    }

    public static boolean hasExtra(int meta) {
        return meta > 5 && meta < 12;
    }

    public static int removeExtraFlag(int meta) {
        return hasExtra(meta) ? meta - DUMMY_EXTRA_OFFSET : meta;
    }

    public static int addExtraFlag(int meta) {
        return meta >= 0 && meta <= 5 ? meta + DUMMY_EXTRA_OFFSET : meta;
    }

    public static RBMKColumnLifecyclePlanner.LidType lidFromCoreMeta(int meta) {
        return RBMKColumnLifecyclePlanner.lidFromLegacyMeta(meta);
    }

    public static int coreMetaForLid(RBMKColumnLifecyclePlanner.LidType lidType) {
        return RBMKColumnLifecyclePlanner.legacyMetaForLid(lidType);
    }

    public static ColumnStructurePlan planColumnStructure(BlockPos core, int columnHeight) {
        BlockPos safeCore = core == null ? BlockPos.ZERO : core;
        int height = Math.max(1, columnHeight);
        List<BlockPos> columnBlocks = new ArrayList<>(height + 1);
        for (int y = 0; y <= height; y++) {
            columnBlocks.add(safeCore.above(y));
        }
        return new ColumnStructurePlan(
                safeCore,
                new LegacyDimensions(height, 0, 0, 0, 0, 0),
                List.copyOf(columnBlocks),
                safeCore.above(height),
                true);
    }

    public static OpenGuiPlan planOpenGui(boolean remoteLevel, boolean holdingLid, boolean hasLid, boolean sneaking) {
        if (remoteLevel) {
            return new OpenGuiPlan(true, false, DEFAULT_GUI_ID, null);
        }
        RBMKColumnLifecyclePlanner.ActivationPlan activation =
                RBMKColumnLifecyclePlanner.planOpenActivation(holdingLid, hasLid, sneaking);
        return new OpenGuiPlan(
                activation.handled(),
                activation.openGui(),
                DEFAULT_GUI_ID,
                activation.failure());
    }

    public static BreakLidPlan planBreakLidDrop(BlockPos core, int coreMeta, int columnHeight, boolean dropLids) {
        RBMKColumnLifecyclePlanner.LidType lidType = lidFromCoreMeta(coreMeta);
        RBMKColumnLifecyclePlanner.LidDropPlan drop =
                RBMKColumnLifecyclePlanner.planBreakLidDrop(
                        core == null ? BlockPos.ZERO : core,
                        columnHeight,
                        lidType,
                        dropLids);
        return new BreakLidPlan(coreMeta, lidType, drop);
    }

    public static ScrewdriverPlan planScrewdriverLidRemoval(
            BlockPos core,
            int coreMeta,
            int columnHeight,
            boolean hasLid,
            boolean lidRemovable) {
        RBMKColumnLifecyclePlanner.LidType lidType = hasLid
                ? lidFromCoreMeta(coreMeta)
                : RBMKColumnLifecyclePlanner.LidType.NONE;
        RBMKColumnLifecyclePlanner.LidRemovalPlan removal =
                RBMKColumnLifecyclePlanner.planScrewdriverLidRemoval(
                        core == null ? BlockPos.ZERO : core,
                        columnHeight,
                        lidType,
                        lidRemovable);
        return new ScrewdriverPlan(
                removal.removed(),
                coreMeta,
                removal.newLegacyMeta(),
                removal.removeNeutronNodeLid(),
                removal.suppressExplodeOnBrokenDuringMutation(),
                removal.drop());
    }

    public static AABB collisionBox(AABB baseBox, boolean hasLid) {
        AABB safeBox = baseBox == null ? new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D) : baseBox;
        return new AABB(
                safeBox.minX,
                safeBox.minY,
                safeBox.minZ,
                safeBox.maxX,
                safeBox.maxY + (hasLid ? LID_COLLISION_HEIGHT : 0.0D),
                safeBox.maxZ);
    }

    public static AABB detailedHitboxBounds() {
        return new AABB(0.0D, 0.0D, 0.0D, 1.0D, DETAILED_HITBOX_MAX_Y, 1.0D);
    }

    public static AABB rotateDetailedBox(AABB localBox, BlockPos core, Direction rotation) {
        AABB box = localBox == null ? new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D) : localBox;
        BlockPos safeCore = core == null ? BlockPos.ZERO : core;
        Direction safeRotation = rotation == null ? Direction.NORTH : rotation;

        AABB rotated = switch (safeRotation) {
            case EAST -> new AABB(-box.maxZ, box.minY, box.minX, -box.minZ, box.maxY, box.maxX);
            case SOUTH -> new AABB(-box.maxX, box.minY, -box.maxZ, -box.minX, box.maxY, -box.minZ);
            case WEST -> new AABB(box.minZ, box.minY, -box.maxX, box.maxZ, box.maxY, -box.minX);
            default -> new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
        };
        return rotated.move(safeCore.getX() + 0.5D, safeCore.getY(), safeCore.getZ() + 0.5D);
    }

    public static TexturePlan texturePlan(boolean hasOwnLid, int renderLid, boolean renderPipes, Direction side) {
        Direction safeSide = side == null ? Direction.NORTH : side;
        boolean vertical = safeSide == Direction.DOWN || safeSide == Direction.UP;
        if (renderPipes) {
            return new TexturePlan(vertical ? TextureRole.PIPE_TOP : TextureRole.PIPE_SIDE);
        }
        if (!hasOwnLid) {
            if (renderLid == LegacyRenderLid.STANDARD.id()) {
                return new TexturePlan(vertical ? TextureRole.COVER_TOP : TextureRole.COVER_SIDE);
            }
            if (renderLid == LegacyRenderLid.GLASS.id()) {
                return new TexturePlan(vertical ? TextureRole.GLASS_TOP : TextureRole.GLASS_SIDE);
            }
        }
        return new TexturePlan(vertical ? TextureRole.COLUMN_TOP : TextureRole.COLUMN_SIDE);
    }

    public static BlockContract blockContract(String legacyId) {
        String id = legacyId == null ? "" : legacyId;
        return switch (id) {
            case "rbmk_rod" -> rbmkRod(id, "rbmk/rbmk_element", "TileEntityRBMKRod", false, false);
            case "rbmk_rod_mod" -> rbmkRod(id, "rbmk/rbmk_element_mod", "TileEntityRBMKRod", true, false);
            case "rbmk_rod_reasim" ->
                    rbmkRod(id, "rbmk/rbmk_element_reasim", "TileEntityRBMKRodReaSim", false, true);
            case "rbmk_rod_reasim_mod" ->
                    rbmkRod(id, "rbmk/rbmk_element_reasim_mod", "TileEntityRBMKRodReaSim", true, true);
            case "rbmk_control" ->
                    rbmkControl(id, "rbmk/rbmk_control", "TileEntityRBMKControlManual", false, false, false);
            case "rbmk_control_mod" ->
                    rbmkControl(id, "rbmk/rbmk_control_mod", "TileEntityRBMKControlManual", true, false, false);
            case "rbmk_control_auto" ->
                    rbmkControl(id, "rbmk/rbmk_control_auto", "TileEntityRBMKControlAuto", false, true, false);
            case "rbmk_control_reasim" ->
                    rbmkControl(id, "rbmk/rbmk_control_reasim", "TileEntityRBMKControlManual", false, false, true);
            case "rbmk_control_reasim_auto" ->
                    rbmkControl(id, "rbmk/rbmk_control_reasim_auto", "TileEntityRBMKControlAuto", false, true, true);
            case "rbmk_blank" -> rbmkPassive(id, "rbmk/rbmk_blank", "TileEntityRBMKBlank", ProxyPlan.none());
            case "rbmk_boiler" ->
                    rbmkPiped(id, "rbmk/rbmk_boiler", "TileEntityRBMKBoiler", ProxyPlan.fluidProxy());
            case "rbmk_reflector" ->
                    rbmkPassive(id, "rbmk/rbmk_reflector", "TileEntityRBMKReflector", ProxyPlan.none());
            case "rbmk_absorber" ->
                    rbmkPassive(id, "rbmk/rbmk_absorber", "TileEntityRBMKAbsorber", ProxyPlan.none());
            case "rbmk_moderator" ->
                    rbmkPassive(id, "rbmk/rbmk_moderator", "TileEntityRBMKModerator", ProxyPlan.none());
            case "rbmk_outgasser" ->
                    rbmkPassive(id, "rbmk/rbmk_outgasser", "TileEntityRBMKOutgasser", ProxyPlan.inventoryFluid());
            case "rbmk_storage" ->
                    rbmkPassive(id, "rbmk/rbmk_storage", "TileEntityRBMKStorage", ProxyPlan.inventoryProxy());
            case "rbmk_cooler" ->
                    rbmkPassive(id, "rbmk/rbmk_cooler", "TileEntityRBMKCooler", ProxyPlan.fluidProxy());
            case "rbmk_heater" ->
                    rbmkPiped(id, "rbmk/rbmk_heater", "TileEntityRBMKHeater", ProxyPlan.fluidProxy());
            case "rbmk_console" -> new BlockContract(
                    id,
                    LegacyBlockClass.CONSOLE,
                    "rbmk/rbmk_console",
                    CreativeTabRole.MACHINE,
                    LEGACY_HARDNESS,
                    LEGACY_RESISTANCE,
                    RenderRole.TESR_ONLY,
                    false,
                    false,
                    new LegacyDimensions(3, 0, 0, 0, 2, 2),
                    1,
                    new TileEntityPlan("TileEntityRBMKConsole", TileEntityCreation.CORE_ONLY, ProxyPlan.none()),
                    InteractionContract.console(),
                    IconContract.simple(false, false),
                    List.of("extra footprint {0,0,0,1,2,2}", "guide book click zone handled by RBMKPanelBlockPlanner"));
            case "rbmk_crane_console" -> new BlockContract(
                    id,
                    LegacyBlockClass.CRANE_CONSOLE,
                    "rbmk/rbmk_crane_console",
                    CreativeTabRole.MACHINE,
                    LEGACY_HARDNESS,
                    LEGACY_RESISTANCE,
                    RenderRole.DEFAULT_MODEL,
                    true,
                    true,
                    new LegacyDimensions(1, 0, 0, 0, 1, 1),
                    0,
                    new TileEntityPlan("TileEntityCraneConsole", TileEntityCreation.CORE_ONLY, ProxyPlan.none()),
                    InteractionContract.screwdriverRotates(),
                    IconContract.simple(false, false),
                    List.of("screwdriver cycles crane rotation"));
            case "rbmk_display_blank" ->
                    miniPanel(id, "TileEntity:none", RBMKPanelPlanner.PanelType.DISPLAY, false, false);
            case "rbmk_display" -> miniPanel(id, "TileEntityRBMKDisplay", RBMKPanelPlanner.PanelType.DISPLAY, false, true);
            case "rbmk_key_pad" -> miniPanel(id, "TileEntityRBMKKeyPad", RBMKPanelPlanner.PanelType.KEYPAD, true, false);
            case "rbmk_gauge" -> miniPanel(id, "TileEntityRBMKGauge", RBMKPanelPlanner.PanelType.GAUGE, true, false);
            case "rbmk_numitron" -> miniPanel(id, "TileEntityRBMKNumitron", RBMKPanelPlanner.PanelType.NUMITRON, true, false);
            case "rbmk_graph" -> miniPanel(id, "TileEntityRBMKGraph", RBMKPanelPlanner.PanelType.GRAPH, true, false);
            case "rbmk_lever" -> miniPanel(id, "TileEntityRBMKLever", RBMKPanelPlanner.PanelType.LEVER, true, false);
            case "rbmk_indicator" ->
                    miniPanel(id, "TileEntityRBMKIndicator", RBMKPanelPlanner.PanelType.INDICATOR, true, false);
            case "rbmk_terminal" ->
                    miniPanel(id, "TileEntityRBMKTerminal", RBMKPanelPlanner.PanelType.TERMINAL, false, false);
            case "rbmk_autoloader" -> new BlockContract(
                    id,
                    LegacyBlockClass.AUTOLOADER,
                    "rbmk_autoloader",
                    CreativeTabRole.MACHINE,
                    50.0F,
                    60.0F,
                    RenderRole.TESR_ONLY,
                    false,
                    false,
                    new LegacyDimensions(8, 0, 0, 0, 0, 0),
                    0,
                    new TileEntityPlan("TileEntityRBMKAutoloader", TileEntityCreation.CORE_OR_PROXY,
                            ProxyPlan.inventoryProxy()),
                    InteractionContract.openStandardGui(),
                    IconContract.simple(false, false),
                    List.of("custom collision boxes: narrow stem and upper body"));
            case "rbmk_loader" -> new BlockContract(
                    id,
                    LegacyBlockClass.LOADER,
                    "rbmk_loader",
                    CreativeTabRole.MACHINE,
                    50.0F,
                    60.0F,
                    RenderRole.DEFAULT_MODEL,
                    true,
                    true,
                    new LegacyDimensions(0, 0, 0, 0, 0, 0),
                    0,
                    new TileEntityPlan("", TileEntityCreation.NONE, ProxyPlan.none()),
                    InteractionContract.tooltipOnly(),
                    IconContract.simple(false, false),
                    List.of("Fluid connector: UP accepts heatable, other sides accept coolable or perfluoromethyl"));
            case "rbmk_steam_inlet" -> ioBlock(id, "rbmk_steam_inlet", "TileEntityRBMKInlet");
            case "rbmk_steam_outlet" -> ioBlock(id, "rbmk_steam_outlet", "TileEntityRBMKOutlet");
            case "pribris" -> debris(id, "rbmk/rbmk_debris");
            case "pribris_burning" -> debris(id, "rbmk/rbmk_debris_burning");
            case "pribris_radiating" -> debris(id, "rbmk/rbmk_debris_radiating");
            case "pribris_digamma" -> debris(id, "rbmk/rbmk_debris_digamma");
            case "deco_rbmk" -> decoration(id, "rbmk/rbmk_top");
            case "deco_rbmk_smooth" -> decoration(id, "rbmk/rbmk_blank_top");
            default -> new BlockContract(
                    id,
                    LegacyBlockClass.UNKNOWN,
                    "",
                    CreativeTabRole.NONE,
                    0.0F,
                    0.0F,
                    RenderRole.DEFAULT_MODEL,
                    true,
                    true,
                    new LegacyDimensions(0, 0, 0, 0, 0, 0),
                    0,
                    new TileEntityPlan("", TileEntityCreation.NONE, ProxyPlan.none()),
                    InteractionContract.none(),
                    IconContract.simple(false, false),
                    List.of());
        };
    }

    public static TileEntityPlan tileEntityPlan(String legacyId, int meta) {
        BlockContract contract = blockContract(legacyId);
        if (contract.tileEntity().creation() == TileEntityCreation.ALWAYS) {
            return contract.tileEntity();
        }
        if (contract.tileEntity().creation() == TileEntityCreation.CORE_ONLY && meta >= CORE_METADATA_OFFSET) {
            return contract.tileEntity();
        }
        if (contract.tileEntity().creation() == TileEntityCreation.CORE_OR_PROXY) {
            if (meta >= CORE_METADATA_OFFSET || ("rbmk_autoloader".equals(contract.legacyId()) && meta >= 12)) {
                return contract.tileEntity();
            }
            if (hasExtra(meta) || "rbmk_storage".equals(contract.legacyId()) || "rbmk_autoloader".equals(contract.legacyId())) {
                return new TileEntityPlan("TileEntityProxyCombo", TileEntityCreation.PROXY_ONLY,
                        contract.tileEntity().proxy());
            }
        }
        return new TileEntityPlan("", TileEntityCreation.NONE, ProxyPlan.none());
    }

    public static List<String> iconNames(String legacyId) {
        BlockContract contract = blockContract(legacyId);
        List<String> names = new ArrayList<>();
        String base = contract.textureName();
        if (base.isEmpty()) {
            return List.of();
        }
        if (contract.icon().rbmkBaseIcons()) {
            names.add(base + "_side");
            names.add(base + "_top");
            if (!contract.icon().hasOwnLid()) {
                names.add(base + "_cover_top");
                names.add(base + "_cover_side");
                names.add(base + "_glass_top");
                names.add(base + "_glass_side");
            }
            if (contract.icon().piped()) {
                names.add(base + "_pipe_top");
                names.add(base + "_pipe_side");
            }
            if (contract.icon().rodInterior()) {
                names.add(base + "_inner");
                names.add(base + "_fuel");
            }
            if (contract.icon().reasimBottom()) {
                names.add(base + "_bottom");
            }
            return List.copyOf(names);
        }
        names.add(base);
        return List.copyOf(names);
    }

    private static BlockContract rbmkRod(String id, String texture, String tileEntity, boolean moderated,
            boolean reasim) {
        return new BlockContract(
                id,
                reasim ? LegacyBlockClass.REASIM_ROD : LegacyBlockClass.ROD,
                texture,
                CreativeTabRole.MACHINE,
                LEGACY_HARDNESS,
                LEGACY_RESISTANCE,
                RenderRole.RODS,
                false,
                false,
                new LegacyDimensions(3, 0, 0, 0, 0, 0),
                0,
                new TileEntityPlan(tileEntity, TileEntityCreation.CORE_OR_PROXY, ProxyPlan.inventoryProxy()),
                InteractionContract.rod(),
                new IconContract(true, false, false, true, false, false),
                List.of(moderated ? "moderated=true" : "moderated=false"));
    }

    private static BlockContract rbmkPassive(String id, String texture, String tileEntity, ProxyPlan proxy) {
        TileEntityCreation creation = proxy.hasAny() ? TileEntityCreation.CORE_OR_PROXY : TileEntityCreation.CORE_ONLY;
        return new BlockContract(
                id,
                LegacyBlockClass.RBMK_BASE,
                texture,
                CreativeTabRole.MACHINE,
                LEGACY_HARDNESS,
                LEGACY_RESISTANCE,
                RenderRole.PASSIVE,
                true,
                true,
                new LegacyDimensions(3, 0, 0, 0, 0, 0),
                0,
                new TileEntityPlan(tileEntity, creation, proxy),
                InteractionContract.openGuiWhenNotSneaking(),
                IconContract.rbmkBase(false),
                List.of());
    }

    private static BlockContract rbmkPiped(String id, String texture, String tileEntity, ProxyPlan proxy) {
        return new BlockContract(
                id,
                LegacyBlockClass.PIPED_BASE,
                texture,
                CreativeTabRole.MACHINE,
                LEGACY_HARDNESS,
                LEGACY_RESISTANCE,
                RenderRole.CONTROL,
                true,
                true,
                new LegacyDimensions(3, 0, 0, 0, 0, 0),
                0,
                new TileEntityPlan(tileEntity, TileEntityCreation.CORE_OR_PROXY, proxy),
                InteractionContract.openGuiWhenNotSneaking(),
                new IconContract(true, false, true, false, false, false),
                List.of("renderPipes static flag swaps side/top icons to pipe textures"));
    }

    private static BlockContract rbmkControl(String id, String texture, String tileEntity, boolean moderated,
            boolean automatic, boolean reasim) {
        return new BlockContract(
                id,
                automatic ? LegacyBlockClass.CONTROL_AUTO : LegacyBlockClass.CONTROL_MANUAL,
                texture,
                CreativeTabRole.MACHINE,
                LEGACY_HARDNESS,
                LEGACY_RESISTANCE,
                RenderRole.CONTROL,
                true,
                true,
                new LegacyDimensions(3, 0, 0, 0, 0, 0),
                0,
                new TileEntityPlan(tileEntity, automatic ? TileEntityCreation.CORE_ONLY : TileEntityCreation.CORE_OR_PROXY,
                        automatic ? ProxyPlan.none() : ProxyPlan.emptyProxy()),
                InteractionContract.openGuiWhenNotSneaking(),
                new IconContract(true, true, true, false, reasim, false),
                List.of(moderated ? "moderated=true" : "moderated=false",
                        automatic ? "auto control" : "manual control",
                        reasim ? "bottom icon when no lid" : ""));
    }

    private static BlockContract miniPanel(String id, String tileEntity, RBMKPanelPlanner.PanelType panelType,
            boolean screwdriverGui, boolean screwdriverRotates) {
        return new BlockContract(
                id,
                LegacyBlockClass.MINI_PANEL,
                "rbmk/rbmk_display",
                CreativeTabRole.MACHINE,
                LEGACY_HARDNESS,
                LEGACY_RESISTANCE,
                RenderRole.MINI_PANEL,
                false,
                false,
                new LegacyDimensions(0, 0, 0, 0, 0, 0),
                0,
                new TileEntityPlan(tileEntity, "TileEntity:none".equals(tileEntity)
                        ? TileEntityCreation.NONE : TileEntityCreation.ALWAYS, ProxyPlan.none()),
                screwdriverRotates ? InteractionContract.screwdriverRotates()
                        : screwdriverGui ? InteractionContract.screwdriverOpensGui()
                        : InteractionContract.openGuiWhenNotSneaking(),
                IconContract.simple(false, false),
                List.of("panelType=" + panelType.name(), "uses RBMKMiniPanelBase side bounds and inventory slab render"));
    }

    private static BlockContract ioBlock(String id, String texture, String tileEntity) {
        return new BlockContract(
                id,
                LegacyBlockClass.IO_PORT,
                texture,
                CreativeTabRole.MACHINE,
                50.0F,
                60.0F,
                RenderRole.DEFAULT_MODEL,
                true,
                true,
                new LegacyDimensions(0, 0, 0, 0, 0, 0),
                0,
                new TileEntityPlan(tileEntity, TileEntityCreation.ALWAYS, ProxyPlan.none()),
                InteractionContract.tooltipOnly(),
                IconContract.simple(false, false),
                List.of("standard info tooltip"));
    }

    private static BlockContract debris(String id, String texture) {
        return new BlockContract(
                id,
                LegacyBlockClass.DEBRIS,
                texture,
                CreativeTabRole.MACHINE,
                50.0F,
                600.0F,
                RenderRole.DEBRIS,
                false,
                false,
                new LegacyDimensions(0, 0, 0, 0, 0, 0),
                0,
                new TileEntityPlan("", TileEntityCreation.NONE, ProxyPlan.none()),
                InteractionContract.none(),
                IconContract.simple(false, false),
                List.of());
    }

    private static BlockContract decoration(String id, String texture) {
        return new BlockContract(
                id,
                LegacyBlockClass.DECORATION,
                texture,
                CreativeTabRole.BLOCK,
                5.0F,
                100.0F,
                RenderRole.DEFAULT_MODEL,
                true,
                true,
                new LegacyDimensions(0, 0, 0, 0, 0, 0),
                0,
                new TileEntityPlan("", TileEntityCreation.NONE, ProxyPlan.none()),
                InteractionContract.none(),
                IconContract.simple(false, false),
                List.of());
    }

    public enum MetadataKind {
        DUMMY,
        EXTRA_DUMMY,
        CORE
    }

    public enum LegacyRenderLid {
        NONE(0),
        STANDARD(1),
        GLASS(2);

        private final int id;

        LegacyRenderLid(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }
    }

    public enum TextureRole {
        COLUMN_TOP,
        COLUMN_SIDE,
        COVER_TOP,
        COVER_SIDE,
        GLASS_TOP,
        GLASS_SIDE,
        PIPE_TOP,
        PIPE_SIDE
    }

    public enum LegacyBlockClass {
        UNKNOWN,
        DECORATION,
        RBMK_BASE,
        ROD,
        REASIM_ROD,
        PIPED_BASE,
        CONTROL_MANUAL,
        CONTROL_AUTO,
        CONSOLE,
        CRANE_CONSOLE,
        MINI_PANEL,
        AUTOLOADER,
        LOADER,
        IO_PORT,
        DEBRIS
    }

    public enum CreativeTabRole {
        NONE,
        BLOCK,
        MACHINE
    }

    public enum RenderRole {
        DEFAULT_MODEL,
        NORMAL,
        RODS,
        PASSIVE,
        CONTROL,
        MINI_PANEL,
        TESR_ONLY,
        DEBRIS
    }

    public enum TileEntityCreation {
        NONE,
        ALWAYS,
        CORE_ONLY,
        CORE_OR_PROXY,
        PROXY_ONLY
    }

    public enum ActivationMode {
        NONE,
        OPEN_GUI_WHEN_NOT_SNEAKING,
        STANDARD_OPEN_BEHAVIOR,
        SCREWDRIVER_OPENS_GUI,
        SCREWDRIVER_ROTATES,
        ROD_HAND_LOAD_OR_OPEN_GUI,
        CONSOLE_GUIDE_OR_OPEN_GUI,
        TOOLTIP_ONLY
    }

    public record LegacyDimensions(int up, int down, int forward, int backward, int left, int right) {
    }

    public record BlockContract(
            String legacyId,
            LegacyBlockClass legacyClass,
            String textureName,
            CreativeTabRole creativeTab,
            float hardness,
            float resistance,
            RenderRole renderRole,
            boolean opaqueCube,
            boolean renderAsNormalBlock,
            LegacyDimensions dimensions,
            int offset,
            TileEntityPlan tileEntity,
            InteractionContract interaction,
            IconContract icon,
            List<String> notes) {
        public BlockContract {
            legacyId = legacyId == null ? "" : legacyId;
            textureName = textureName == null ? "" : textureName;
            legacyClass = legacyClass == null ? LegacyBlockClass.UNKNOWN : legacyClass;
            creativeTab = creativeTab == null ? CreativeTabRole.NONE : creativeTab;
            renderRole = renderRole == null ? RenderRole.DEFAULT_MODEL : renderRole;
            dimensions = dimensions == null ? new LegacyDimensions(0, 0, 0, 0, 0, 0) : dimensions;
            tileEntity = tileEntity == null ? new TileEntityPlan("", TileEntityCreation.NONE, ProxyPlan.none()) : tileEntity;
            interaction = interaction == null ? InteractionContract.none() : interaction;
            icon = icon == null ? IconContract.simple(false, false) : icon;
            notes = List.copyOf(notes == null ? List.of() : notes);
        }
    }

    public record TileEntityPlan(String coreTileEntity, TileEntityCreation creation, ProxyPlan proxy) {
        public TileEntityPlan {
            coreTileEntity = coreTileEntity == null ? "" : coreTileEntity;
            creation = creation == null ? TileEntityCreation.NONE : creation;
            proxy = proxy == null ? ProxyPlan.none() : proxy;
        }
    }

    public record ProxyPlan(boolean inventory, boolean power, boolean fluid, boolean emptyProxyCreated) {
        public static ProxyPlan none() {
            return new ProxyPlan(false, false, false, false);
        }

        public static ProxyPlan emptyProxy() {
            return new ProxyPlan(false, false, false, true);
        }

        public static ProxyPlan inventoryProxy() {
            return new ProxyPlan(true, false, false, true);
        }

        public static ProxyPlan fluidProxy() {
            return new ProxyPlan(false, false, true, true);
        }

        public static ProxyPlan inventoryFluid() {
            return new ProxyPlan(true, false, true, true);
        }

        public boolean hasAny() {
            return inventory || power || fluid || emptyProxyCreated;
        }
    }

    public record InteractionContract(
            ActivationMode activation,
            boolean bossFbiMark,
            boolean acceptsLidUse,
            boolean opensGuiClientSideOnly,
            boolean opensGuiServerSideOnly,
            boolean consumesHeldRod,
            boolean standardInfoTooltip) {
        public static InteractionContract none() {
            return new InteractionContract(ActivationMode.NONE, false, false, false, false, false, false);
        }

        public static InteractionContract tooltipOnly() {
            return new InteractionContract(ActivationMode.TOOLTIP_ONLY, false, false, false, false, false, true);
        }

        public static InteractionContract openGuiWhenNotSneaking() {
            return new InteractionContract(ActivationMode.OPEN_GUI_WHEN_NOT_SNEAKING, false, true, false, true, false,
                    false);
        }

        public static InteractionContract openStandardGui() {
            return new InteractionContract(ActivationMode.STANDARD_OPEN_BEHAVIOR, false, false, false, true, false,
                    false);
        }

        public static InteractionContract screwdriverOpensGui() {
            return new InteractionContract(ActivationMode.SCREWDRIVER_OPENS_GUI, false, false, true, false, false,
                    true);
        }

        public static InteractionContract screwdriverRotates() {
            return new InteractionContract(ActivationMode.SCREWDRIVER_ROTATES, false, false, false, true, false,
                    false);
        }

        public static InteractionContract rod() {
            return new InteractionContract(ActivationMode.ROD_HAND_LOAD_OR_OPEN_GUI, true, true, false, true, true,
                    false);
        }

        public static InteractionContract console() {
            return new InteractionContract(ActivationMode.CONSOLE_GUIDE_OR_OPEN_GUI, true, false, true, false, false,
                    false);
        }
    }

    public record IconContract(
            boolean rbmkBaseIcons,
            boolean hasOwnLid,
            boolean piped,
            boolean rodInterior,
            boolean reasimBottom,
            boolean miniPanelBounds) {
        public static IconContract simple(boolean miniPanelBounds, boolean reasimBottom) {
            return new IconContract(false, false, false, false, reasimBottom, miniPanelBounds);
        }

        public static IconContract rbmkBase(boolean hasOwnLid) {
            return new IconContract(true, hasOwnLid, false, false, false, false);
        }
    }

    public record ColumnStructurePlan(
            BlockPos core,
            LegacyDimensions dimensions,
            List<BlockPos> columnBlocks,
            BlockPos extraBlock,
            boolean markExtraBlock) {
    }

    public record OpenGuiPlan(
            boolean handled,
            boolean openGui,
            int guiId,
            RBMKColumnLifecyclePlanner.ActivationFailure failure) {
    }

    public record BreakLidPlan(
            int coreMeta,
            RBMKColumnLifecyclePlanner.LidType lidType,
            RBMKColumnLifecyclePlanner.LidDropPlan drop) {
    }

    public record ScrewdriverPlan(
            boolean removed,
            int oldCoreMeta,
            int newCoreMeta,
            boolean removeNeutronNodeLid,
            boolean suppressExplodeOnBrokenDuringMutation,
            RBMKColumnLifecyclePlanner.LidDropPlan drop) {
    }

    public record TexturePlan(TextureRole textureRole) {
    }
}
