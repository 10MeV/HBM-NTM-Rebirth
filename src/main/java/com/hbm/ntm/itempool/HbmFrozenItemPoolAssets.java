package com.hbm.ntm.itempool;

import com.hbm.ntm.HbmNtm;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/**
 * Repository-only inventory of excluded reward-pool assets.
 *
 * <p>These IDs and JSON files are deliberately retained for a future explicit unfreeze, but this
 * class is not consulted by {@link HbmItemPoolRegistry}: no default command, loot-generator or
 * gameplay hook may roll them.</p>
 */
public final class HbmFrozenItemPoolAssets {
    private static final Map<String, ResourceLocation> TABLES = Map.ofEntries(
            entry("POOL_BLUEPRINTS", "blueprints"),
            entry("POOL_FILING_CABINET", "component/filing_cabinet"),
            entry("POOL_MACHINE_PARTS", "component/machine_parts"),
            entry("POOL_NUKE_FUEL", "component/nuke_fuel"),
            entry("POOL_OFFICE_TRASH", "component/office_trash"),
            entry("POOL_OIL_RIG", "component/oil_rig"),
            entry("POOL_REPAIR_MATERIALS", "component/repair_materials"),
            entry("POOL_RTG", "component/rtg"), entry("POOL_SILO", "component/silo"),
            entry("POOL_SOLID_FUEL", "component/solid_fuel"), entry("POOL_VAULT_LAB", "component/vault_lab"),
            entry("POOL_VAULT_LOCKERS", "component/vault_lockers"),
            entry("POOL_ANTENNA", "legacy/antenna"), entry("POOL_EXPENSIVE", "legacy/expensive"),
            entry("POOL_GENERIC", "legacy/generic"), entry("POOL_NUKE_MISC", "legacy/nuke_misc"),
            entry("POOL_NUKE_TRASH", "legacy/nuke_trash"), entry("POOL_SPACESHIP", "legacy/spaceship"),
            entry("POOL_VERTIBIRD", "legacy/vertibird"), entry("POOL_PILE_BONES", "pile/bones"),
            entry("POOL_PILE_CAPS", "pile/caps"), entry("POOL_PILE_OF_GARBAGE", "pile/garbage"),
            entry("POOL_PILE_GEAR", "pile/gear"), entry("POOL_PILE_HIVE", "pile/hive"),
            entry("POOL_PILE_MAKESHIFT_GUN", "pile/makeshift_gun"),
            entry("POOL_PILE_MAKESHIFT_PLATES", "pile/makeshift_plates"),
            entry("POOL_PILE_MAKESHIFT_WIRE", "pile/makeshift_wire"),
            entry("POOL_PILE_MAKESHIFT_WRENCH", "pile/makeshift_wrench"),
            entry("POOL_PILE_MECHANICAL", "pile/mechanical"), entry("POOL_PILE_MED_PILLS", "pile/med_pills"),
            entry("POOL_PILE_MED_SYRINGE", "pile/med_syringe"),
            entry("POOL_PILE_NUKE_STORAGE", "pile/nuke_storage"), entry("POOL_RED_PEDESTAL", "redroom/red_pedestal"),
            entry("POOL_VAULT_REINFORCED", "vault_reinforced"), entry("POOL_VAULT_RUSTY", "vault_rusty"),
            entry("POOL_VAULT_STANDARD", "vault_standard"), entry("POOL_VAULT_UNBREAKABLE", "vault_unbreakable"));

    public static Map<String, ResourceLocation> tables() {
        return TABLES;
    }

    private static Map.Entry<String, ResourceLocation> entry(String legacyPoolId, String path) {
        return Map.entry(legacyPoolId, new ResourceLocation(HbmNtm.MOD_ID, "item_pool/" + path));
    }

    private HbmFrozenItemPoolAssets() {
    }
}
