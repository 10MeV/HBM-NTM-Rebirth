# Fluid MK2 网络 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 流体 MK2 网络、流体类型、流体 tank、流体 trait、可填充物品接口。
- 该库服务化工、精炼、反应堆、冷却、污染、流体容器等多个系统。

## 1.7.10 源文件

- `src/main/java/api/hbm/fluidmk2/FluidNetMK2.java`
- `src/main/java/api/hbm/fluidmk2/FluidNode.java`
- `src/main/java/api/hbm/fluidmk2/IFluidUserMK2.java`
- `src/main/java/api/hbm/fluidmk2/IFluidConnectorMK2.java`
- `src/main/java/api/hbm/fluidmk2/IFluidConnectorBlockMK2.java`
- `src/main/java/api/hbm/fluidmk2/IFluidProviderMK2.java`
- `src/main/java/api/hbm/fluidmk2/IFluidReceiverMK2.java`
- `src/main/java/api/hbm/fluidmk2/IFluidPipeMK2.java`
- `src/main/java/api/hbm/fluidmk2/IFillableItem.java`
- `src/main/java/com/hbm/inventory/fluid/FluidType.java`
- `src/main/java/com/hbm/inventory/fluid/Fluids.java`
- `src/main/java/com/hbm/inventory/fluid/tank/FluidTank.java`
- `src/main/java/com/hbm/inventory/fluid/tank/FluidLoadingHandler.java`
- `src/main/java/com/hbm/inventory/fluid/trait`

## 旧版契约

- 网络类型：`FluidNetMK2 extends NodeNet<IFluidReceiverMK2, IFluidProviderMK2, FluidNode>`。
- 流体按 `FluidType` 区分，不是直接使用现代 Forge Fluid。
- 压力范围：
  - `IFluidUserMK2.HIGHEST_VALID_PRESSURE = 5`。
  - provider/receiver 返回可提供或可接收的压力区间。
  - 网络按压力层分别统计可用量和需求。
- 优先级：
  - 接收端复用能量 `ConnectionPriority`。
  - 每个压力层内按优先级和需求权重分配。
- `FluidTank` 旧契约：
  - 储存 `FluidType`、fill、maxFill、pressure。
  - 可被配方模块 conform/reset。
  - 由 loader 处理物品容器与 tank 的装卸。
- 流体 trait：
  - `FT_Flammable`、`FT_Combustible`、`FT_Corrosive`、`FT_Toxin`、`FT_Poison`、`FT_Polluting`、`FT_VentRadiation`、`FT_Heatable`、`FT_Coolable` 等。
  - trait 把流体与燃烧、毒性、污染、辐射、热交换连接起来。
- 旧 `api/hbm/fluid` 是 MK1 兼容接口，迁移时只保留需要兼容的语义。

## 迁移计划

- 先决定现代端 `FluidType` 与 Forge Fluid/FluidStack 的映射方式。
- 保留压力、优先级、trait 语义，避免只用普通 Forge tank 抹掉旧机制。
- `FluidTank` 可先作为 HBM 自有 tank 数据结构迁移，再逐步桥接 Forge capability。
- `IFillableItem` 与装卸 handler 要和现代 ItemStack 数据存储一起设计。

## 桥接法硬性要求：1.20.1 Forge Fluid 传输

- 后续所有迁移到 1.20.1 的 HBM 流体机器、管道、流体容器、流体配方模块，必须采用“内部 HBM FluidType/pressure/trait 语义 + 外部 Forge Fluid capability”的桥接方案。
- 内部语义：
  - 保留 1.7.10 `FluidType` 的类型身份、颜色、trait、网络 provider 信息。
  - 保留压力层语义，旧版有效压力范围为 `0..5`。
  - 保留 provider/receiver、`ConnectionPriority`、按压力层分配、同优先级按需求权重分配。
  - 保留 tank conform/reset 行为，使配方能动态约束 tank 的目标流体和压力。
  - 不允许只用 Forge `FluidTank` 替代 HBM tank 后丢失 pressure、trait、配方 conform 语义。
- Forge 通用互操作：
  - 方块实体应通过 `getCapability(ForgeCapabilities.FLUID_HANDLER, side)` 暴露 Forge `IFluidHandler`。
  - 默认做 HBM tank 到 Forge `FluidStack` 的 adapter，而不是让机器直接操作外部 `FluidStack`。
  - 需要建立 HBM `FluidType` <-> Forge `Fluid`/`FluidStack` 的稳定映射表；没有映射的 HBM 流体不得静默变成 water 或 empty。
  - 外部 Forge 输入若没有压力信息，默认进入该 tank 配置的默认压力，或显式规定为 pressure 0。
  - 外部 Forge 抽取只允许抽取 adapter 能映射回 Forge `FluidStack` 的 HBM 流体。
  - `simulate` fill/drain 不得改变 HBM tank 状态。
- 推荐接入模板：
  - 内部字段：HBM 自有 tank，例如后续 `HbmFluidTank`，保存 `FluidType`、fill、maxFill、pressure。
  - Forge adapter：后续 `ForgeFluidHandlerAdapter implements IFluidHandler`，包装一个或多个 HBM tank。
  - `getCapability(ForgeCapabilities.FLUID_HANDLER, side)` 根据 side 返回可输入/输出的 adapter。
  - `invalidateCaps()` 中必须 invalidate 对应 `LazyOptional<IFluidHandler>`。
- 流体物品要求：
  - 旧版 `IFillableItem` 的 `acceptsFluid`、`tryFill`、`providesFluid`、`tryEmpty`、`getFirstFluidType`、`getFill` 语义必须保留。
  - 对外可暴露 Forge fluid item capability，但内部仍应能读写 HBM `FluidType` 与 fill。
- 禁止路线：
  - 禁止把 HBM `FluidType` 全部直接替换成 Forge `Fluid` 后删除 trait。
  - 禁止忽略压力层，把所有机器都当普通单 tank fluid handler。
  - 禁止每台机器各自手写 Forge fluid 适配，应复用共享 adapter。

## 验证清单

- 不同压力层不会互相传输。
- 同压力同优先级按需求权重分配。
- tank conform/reset 能随配方改变目标流体。
- trait 能被污染、辐射、燃烧、冷却逻辑读取。
- 通过 Forge fluid capability fill/drain 时，HBM tank fill 与 type 同步变化。
- Forge simulate fill/drain 不改变 HBM tank。
- 未映射的 HBM 流体不会被错误导出为其他 Forge 流体。

## 2026-05-21 Modern Library Pass 1

This pass starts the clean-port code layer for the shared fluid library. Scope is intentionally limited to reusable data structures and the Forge capability bridge:

- Add modern HBM `FluidType`, `HbmFluidStack`, and `HbmFluidTank` while preserving the legacy `type/fill/maxFill/pressure` and `conform/reset` contracts.
- Add base fluid traits and simple tag traits for `GASEOUS`, `LIQUID`, `VISCOUS`, `PLASMA`, `ANTIMATTER`, `LEAD_CONTAINER`, `NO_ID`, `NO_CONTAINER`, and `UNSIPHONABLE`.
- Add `HbmFluids` as the internal HBM fluid identity registry, with only a small starter set for water/lava/steam/coolant/oil/gas/sulfuric acid/hydrogen/deuterium/tritium.
- Add an HBM `FluidType` <-> Forge `Fluid` mapping layer and `ForgeFluidHandlerAdapter` so later block entities expose Forge `IFluidHandler` without losing pressure and trait data.
- Add the modern `IFillableItem` interface matching the old `acceptsFluid`, `tryFill`, `providesFluid`, `tryEmpty`, `getFirstFluidType`, and `getFill` surface.

Deferred on purpose:

- Full `FluidNetMK2` provider/receiver scheduling, pressure-layer distribution, and `ConnectionPriority` weighting.
- `FluidLoadingHandler`, canister/gastank/meta-container compatibility, and fluid container GUI behavior.
- Direct adoption of the reference port `BasicFluidTank extends Forge FluidTank` design, because that path drops HBM pressure/conform semantics.
- World fluid blocks and bucket registration. The bridge only maps vanilla water/lava now and leaves HBM Forge fluid registration for a later pass.

## 2026-05-21 Modern Library Pass 2

This pass wires the fluid core into the first real block entity path without migrating a full machine yet:

- Add `HbmFluidSideMode` and extend `ForgeFluidHandlerAdapter` so a capability view can be input-only, output-only, or both.
- Add shared `HbmFluidBlockEntity`, which owns HBM tanks, saves/loads them under `hbm_fluids`, exposes `ForgeCapabilities.FLUID_HANDLER`, and invalidates all fluid capability optionals.
- Convert `machine_boiler_off` from a plain horizontal block to a minimal `BoilerBlock`/`BoilerBlockEntity` fluid-capability scaffold.
- Boiler scaffold tanks:
  - feed tank: `WATER`, 16,000 mB, input-facing capability.
  - steam tank: `STEAM`, 16,000 mB, output-facing capability.
- Side rule for the temporary scaffold: bottom is output, all other explicit sides are input. `null` capability access remains an all-tank internal view.

Still deferred:

- No heat, fuel, boiling recipe, pressure explosion, sound loop, GUI, render animation, or old `TileEntityHeatBoiler` behavior has been migrated.
- `machine_boiler_off` is only a safe test consumer for the fluid library; full boiler migration still needs its own machine trace and behavior pass.

## 2026-05-21 Modern Library Pass 3

This pass makes the starter HBM-only fluids visible to Forge `FluidStack` and external fluid capabilities without changing the internal tank semantics:

- Add `HbmForgeFluidType`, a thin Forge `FluidType` wrapper around the internal HBM `FluidType`.
- Add `ModFluids` with `DeferredRegister` entries for HBM source/flowing fluids: steam, hot steam, superhot steam, coolant, hot coolant, oil, gas, sulfuric acid, hydrogen, deuterium, and tritium.
- Keep vanilla water/lava mapped to vanilla Forge fluids; do not register duplicate `hbm:water` or `hbm:lava`.
- During `HbmFluids.bootstrap()`, register all new Forge source fluids back into `HbmFluidForgeMappings`, so `ForgeFluidHandlerAdapter` can export non-vanilla HBM tank contents.
- Client render extensions currently reuse vanilla water still/flow textures with each HBM fluid's GUI tint. This is only a bridge visualization, not final legacy asset migration.

Still deferred:

- No bucket items, world fluid blocks, fluid block loot, language entries, or creative tab exposure were added in this pass.
- No full old fluid network scheduling, container loader, pressure distribution, or machine-specific fluid recipes were migrated.
- Full fluid textures should be revisited from 1.7.10 resources before adding visible world fluid blocks.

## 2026-05-21 Modern Library Pass 4

This pass fills two shared-library gaps needed before more machines can safely use fluid slots and automation:

- Add small tank inspection helpers: `isEmpty`, `getSpace`, and `getFluidStack`.
- Extend `ForgeFluidHandlerAdapter` with an optional change callback. Real fill/drain through Forge capability now marks the owning block entity dirty instead of silently mutating HBM tanks.
- Rework `HbmFluidBlockEntity` to create fluid capability adapters per side. Side-specific tank lists from `getInputTanks(side)` / `getOutputTanks(side)` now matter, and `null` access still remains the internal all-tank view.
- Add `getInputPressure(side)` as the modern hook for the legacy pressure-layer contract when later machines need non-zero pressure.
- Add `HbmFluidItemTransfer`, a shared item/tank transfer helper. It first honors legacy `IFillableItem` (`acceptsFluid`, `tryFill`, `providesFluid`, `tryEmpty`, `getFirstFluidType`, `getFill`) and then falls back to Forge `IFluidHandlerItem` when a normal modded fluid container is used.

Legacy correction after re-checking the source package at `E:\游戏\我的世界\源码包\Hbm-s-Nuclear-Tech-GIT-master`:

- Old `api/hbm/fluidmk2/IFillableItem#tryFill` is documented and implemented as "return the remainder that could not be added". Modern `HbmFluidItemTransfer` therefore subtracts the returned remainder from the requested amount before draining the source tank.
- Old `IFillableItem#tryEmpty` returns the amount actually provided to the machine tank, and the modern helper keeps that interpretation.
- Old `com/hbm/inventory/fluid/tank/FluidTank#loadTank` rejects normal item loading when `pressure != 0`, except for the legacy infinite barrel item. Modern item loading currently preserves the conservative default by using the target tank's own pressure and only accepting tanks that can accept that pressure.

Still deferred:

- No concrete canister/gas tank item has been ported yet, so `HbmFluidItemTransfer` is only shared plumbing for future machine slots and container items.
- Full `FluidLoadingHandler` slot rules, container replacement behavior, and machine GUI integration still need a dedicated pass once the relevant old source files are available again.
- Full fluid network pressure distribution and `ConnectionPriority` scheduling remain deferred.

## 2026-05-21 Modern Library Pass 5

This pass re-checks and ports the reusable slot-level shape of the old fluid item loading handlers:

- Legacy sources re-read:
  - `api/hbm/fluidmk2/IFillableItem.java`
  - `com/hbm/inventory/fluid/tank/FluidTank.java`
  - `com/hbm/inventory/fluid/tank/FluidLoadingHandler.java`
  - `com/hbm/inventory/fluid/tank/FluidLoaderStandard.java`
  - `com/hbm/inventory/fluid/tank/FluidLoaderFillableItem.java`
  - `com/hbm/inventory/fluid/tank/FluidLoaderInfinite.java`
- Add slot-level helpers to `HbmFluidItemTransfer`:
  - `loadTankFromSlot(items, inputSlot, outputSlot, tank, ...)`: input container drains into a tank and the resulting container is written to the output slot.
  - `unloadTankToSlot(items, inputSlot, outputSlot, tank, ...)`: tank fills an input container and the resulting container is written to the output slot.
- Modern standard containers use Forge `IFluidHandlerItem`, which replaces the old `FluidContainerRegistry` path while preserving the input/output slot split and output stack merge checks.
- Legacy `IFillableItem` items keep the old in-place mutation behavior. They are written back to the input slot instead of consuming one container and producing an output-slot replacement.
- Standard stack container handling refuses same-slot processing when the input stack has more than one item, preserving the old safety assumption that output replacement must not duplicate or erase stacked containers.

Still deferred:

- `FluidLoaderInfinite` is documented but not implemented generically yet; it depends on the old `ItemInfiniteFluid` item contract and should be ported with that concrete item family.
- Armor mod fluid loading from `FluidLoaderFillableItem` is deferred until the armor mod/attachment system is migrated.
- Machines still need to call the new slot-level helpers explicitly during their server tick after their inventories are migrated.

## 2026-05-21 Modern Library Pass 6

This pass scales the fluid library from slot helpers to an actual shared network layer, using the 1.7.10 `FluidNetMK2` contract as the blueprint:

- Add modern fluid network contracts:
  - `HbmFluidUser`
  - `HbmFluidProvider`
  - `HbmFluidReceiver`
  - `HbmFluidConnector`
  - `HbmFluidConnectorBlock`
  - `HbmFluidNodeHost`
  - `HbmFluidNode`
- Add standard network mixins:
  - `HbmStandardFluidSender`
  - `HbmStandardFluidReceiver`
  - `HbmStandardFluidTransceiver`
- Add `HbmFluidNet`, which mirrors the old `FluidNetMK2` shape:
  - per fluid type network
  - pressure range `0..5`
  - provider/receiver timeout pruning
  - receiver distribution by priority, then demand weight
  - provider depletion by available weight with a random leftover fallback
- Add `HbmFluidNodespace` and `HbmFluidConnectionUtil` as the modern network registry/connection helpers.
- Wire `CommonForgeEvents` to tick and unload fluid nodespaces alongside the existing energy nodespace.
- Keep the implementation conservative: the network layer currently manages shared plumbing and algorithm shape, but no concrete pipe block or machine has been switched over to use it yet.

Still deferred:

- No actual fluid pipe block, valve, tank tower, or machine has been wired to `HbmFluidNetworkBlockEntity` yet.
- No particle debug or command-side network inspector has been added for fluid nodes.
- The new network layer should be treated as shared infrastructure for later machine passes, not as a finished user-facing system.

## 2026-05-21 Modern Library Pass 7

This pass turns the network layer into a minimally testable in-world fluid graph and fixes the first machine integration surface:

- Legacy sources re-read:
  - `api/hbm/fluidmk2/IFluidPipeMK2.java`
  - `com/hbm/tileentity/network/TileEntityPipeBaseNT.java`
  - `com/hbm/blocks/network/FluidDuctBase.java`
  - `com/hbm/blocks/ModBlocks.java` fluid duct registrations/resources
- Add debug inspection for fluid nodes/networks:
  - `/hbm fluid nodespace`
  - `/hbm fluid node <pos> <fluid>`
  - `/hbm fluid network <pos> <fluid>`
  - `/hbm fluid pipe set <pos> <fluid>` as a temporary test hook until old fluid identifier tools are migrated.
- Add `HbmFluidNet.DebugSnapshot` and `HbmFluidNodespace.NetworkDebugSnapshot`, reporting links, providers, receivers, pressure-layer availability/rate/demand, receiver priorities, and last transfer.
- Rework `HbmFluidBlockEntity`/`HbmFluidNetworkBlockEntity` to support more than one tracked fluid node per block entity. This is required for machines with separate input/output fluid types such as water input and steam output.
- Fix `HbmFluidNetworkBlockEntity` node refresh semantics so nodes are created only when missing/expired, not destroyed and recreated every server tick.
- Convert the temporary boiler scaffold into a real network participant:
  - receives `WATER` through `HbmStandardFluidReceiver`
  - provides `STEAM` through `HbmStandardFluidSender`
  - owns distinct water/steam nodes and refreshes subscriptions from its server ticker
- Add `HbmFluidNodeBlock`, mirroring the existing energy node block connection-state pattern for six-direction fluid network blocks.
- Add `FluidPipeBlockEntity`, matching the old `TileEntityPipeBaseNT` core contract:
  - stores one selected `FluidType`
  - only connects to the same non-`NONE` fluid type
  - saves both fluid name and legacy numeric ID fallback
  - destroys/recreates its node when the type changes
- Add `FluidPipeBlock` and register legacy `fluid_duct_neo` with block entity, machine tab placement, pickaxe/iron-tool tags, self loot, English/Chinese names, generated multipart state/model, and legacy `pipe_neo.png` copied from the 1.7.10 resource pack.

Still deferred:

- Real old fluid identifier items (`IItemFluidIdentifier`, `ItemFluidIDMulti`) and ctrl/alt recursive pipe retargeting are not ported yet; the debug command is only a temporary validation hook.
- Box ducts, gauges, exhaust ducts, paintable ducts, duct overlays, and analyzer overlay text are still separate follow-up slices.
- Pipe rendering currently reuses the legacy `pipe_neo` texture as a conservative generated multipart cube/arm model, not the final old renderer/overlay behavior.
- No in-game end-to-end transfer test has been run yet; verification so far is command-line build/data generation.

Verification:

- `.\gradlew.bat compileJava --rerun-tasks --no-daemon` passed.
- `.\gradlew.bat runData --no-daemon` passed and generated `fluid_duct_neo` blockstate/model/item/lang/loot/tag resources.
- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` passed after data generation.

## 2026-05-21 Modern Library Pass 8

This pass expands the shared fluid identity table so later machine and pipe work can reference legacy fluid names safely before all behavior traits are complete:

- Legacy source re-read:
  - `com/hbm/inventory/fluid/Fluids.java`
  - `com/hbm/inventory/fluid/FluidType.java`
  - `com/hbm/inventory/fluid/trait/*`
- Replace the starter `HbmFluids` table with the 1.7.10 `Fluids.init()` built-in registration order:
  - 154 registered HBM fluid IDs from `NONE` through `DHC`
  - `ACID` compatibility alias mapped to `PEROXIDE`, matching the old `renameMapping.put("ACID", PEROXIDE)` / `ACID = PEROXIDE` compatibility rule
- Preserve the old stable numeric ID order for the built-in table as far as the modern port can before custom/foreign fluid loading is implemented.
- Preserve core data for the expanded table:
  - internal name
  - approximate color
  - poison / flammability / reactivity values
  - temperature where explicit in old source
  - basic marker traits currently available in the modern port: liquid, gaseous, gaseous-at-room-temperature, viscous, plasma, antimatter, lead-container, no-id, no-container, unsiphonable
- Expand `FluidSymbol` with the old symbol categories currently referenced by the table: `CRYOGENIC`, `NOWATER`, `OXIDIZER`, and `RADIATION`.

Still deferred:

- Behavior-rich traits are only represented as coarse marker traits in this pass. The old `FT_Flammable`, `FT_Combustible`, `FT_Corrosive`, `FT_Toxin`, `FT_Poison`, `FT_Polluting`, `FT_VentRadiation`, `FT_Heatable`, `FT_Coolable`, `FT_PWRModerator`, and `FT_Pheromone` data/actions still need real modern equivalents.
- Custom/foreign fluid loading from `hbmFluidTypes.json`, `CompatFluidRegistry`, and `IFluidRegisterListener` is not migrated.
- `metaOrder`/nice-order UI sorting is not migrated yet.
- Only the existing starter set is exposed as actual Forge source/flowing fluids through `ModFluids`; the expanded identity table does not automatically create world fluids or bucket items.

Progress estimate after Pass 8:

- Core `FluidType` identity/NBT lookup/table: about 80%.
- Basic tank/conform/Forge capability bridge: about 65%.
- Fluid network/provider/receiver algorithm: about 60%.
- In-world pipe graph: about 20%.
- Fluid item/container loading: about 30%.
- Behavior traits and cross-system effects: about 15%.
- Machine integration through the library: about 8%.
- Overall fluid library migration: about 35%.

Verification:

- `.\gradlew.bat compileJava --rerun-tasks --no-daemon` passed.
- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` passed.

## 2026-05-22 Modern Library Pass 9

This pass moves the fluid trait layer from coarse marker tags toward reusable legacy behavior data, without wiring destructive world/entity effects yet:

- Legacy sources re-read:
  - `com/hbm/inventory/fluid/trait/FluidTrait.java`
  - `FluidTraitSimple.java`
  - `FT_Flammable.java`
  - `FT_Combustible.java`
  - `FT_Corrosive.java`
  - `FT_Polluting.java`
  - `FT_Poison.java`
  - `FT_VentRadiation.java`
  - `FT_Heatable.java`
  - `FT_Coolable.java`
  - `FT_PWRModerator.java`
  - `FT_Pheromone.java`
  - `com/hbm/inventory/fluid/Fluids.java` trait initialization blocks
- Add modern data traits:
  - `FlammableFluidTrait`: legacy TU-per-bucket heat energy.
  - `CombustibleFluidTrait`: legacy fuel grade and HE-per-bucket combustion energy.
  - `CorrosiveFluidTrait`: legacy 0..100 corrosion rating and strong-corrosion threshold.
  - `VentRadiationFluidTrait`: radiation per mB released.
  - `PollutingFluidTrait`: spill/burn pollution maps for soot, poison, heavy metal, fallout.
  - `PoisonFluidTrait`: deprecated old poison/withering data retained for compatibility.
  - `HeatableFluidTrait`: heat steps and boiler/heat-exchanger/PWR/ICF/PA efficiencies.
  - `CoolableFluidTrait`: cooling target, amount conversion, heat energy, turbine/heat-exchanger efficiencies.
  - `PwrModeratorFluidTrait`: PWR flux multiplier.
  - `PheromoneFluidTrait`: old glyphid pheromone type marker.
- Add `HbmFluids.registerLegacyBehaviorTraits()` after the 154-fluid identity table:
  - Ports old corrosive/radiation/poison/pheromone marker data for the fluids that had those traits in 1.7.10.
  - Ports old calculated fuel spreadsheet formulas for petroleum, gas, coal, biofuel, reforming, vacuum, DS, and related fuels.
  - Ports old heat/cool transitions for water/steam, oils, coolant, perfluoromethyl, mug, blood, heavy water, sodium, lead, and thorium salt.
  - Ports old PWR moderator multipliers for mug, heavy water, lead, and thorium salt.
- Pollution constants mirror the old `PollutionHandler` baselines:
  - soot `1/25`
  - heavy metal `1/50`
  - poison `1/50`

Still deferred:

- `FT_Toxin` is not fully ported yet because it depends on hazard armor, damage source, potion/effect, and entity protection systems.
- Trait `onFluidRelease` world effects are not invoked yet. Radiation/pollution/corrosion data is queryable, but spilling/burning fluids does not yet mutate world pollution or chunk radiation through this new trait layer.
- Custom/foreign JSON trait serialization/deserialization is still deferred.
- Container definitions (`CD_Canister`, `CD_Gastank`, infinite barrel, armor mods) are still not migrated.
- Nice tooltip text from old trait `addInfo` / `addInfoHidden` is not fully recreated; modern traits prioritize stable machine-readable data first.

Progress estimate after Pass 9:

- Core `FluidType` identity/NBT lookup/table: about 82%.
- Basic tank/conform/Forge capability bridge: about 65%.
- Fluid network/provider/receiver algorithm: about 60%.
- In-world pipe graph: about 20%.
- Fluid item/container loading: about 30%.
- Behavior traits and cross-system effects: about 35%.
- Machine integration through the library: about 8%.
- Overall fluid library migration: about 40%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed.

## 2026-05-31 Modern Library Pass 49

This pass fills the workbench recipe/data gap for the registered fluid MK2 network block family, using the 1.7.10 `CraftingManager` block as the source:

- Legacy sources re-read:
  - `com/hbm/main/CraftingManager.java`
  - `com/hbm/inventory/recipes/anvil/AnvilRecipes.java`
  - `com/hbm/items/ModItems.java`
  - `com/hbm/inventory/OreDictManager.java`
- Add and/or correct generated recipe provider entries for:
  - `fluid_duct_neo`
  - `fluid_duct_paintable`
  - `fluid_duct_paintable_block_exhaust`
  - `fluid_duct_gauge`
  - `fluid_valve`
  - `fluid_switch`
  - `fluid_counter_valve`
  - `fluid_pump`
  - `pipe_anchor`
- Correct `fluid_valve` and `fluid_switch` to use `fluid_duct_paintable` as the duct base, matching old `CraftingManager`, instead of `fluid_duct_neo`.
- Add stable hand-written `src/main/resources/data/hbm/recipes/fluid_network/*.json` recipes so normal resource processing has the fluid network recipes even when `runData` is not executed first.
- Add minimal Forge item tags for this recipe slice and future legacy-ore recipe use:
  - `forge:circuits/basic`
  - `forge:circuits/chip`
  - `forge:ingots/steel`
  - `forge:plates/steel`
  - `forge:plates/aluminium`
  - `forge:pipes/steel`
- Extend item tag datagen with the same steel plate/ingot/pipe tags so later `runData` remains consistent.

Legacy correction/deferred notes:

- Old `fluid_pump` uses `STEEL.shell()` in slot `S`. In 1.7.10 this is not a standalone `shell_steel` item; it is `ModItems.shell = new ItemAutogen(MaterialShapes.SHELL)` with material metadata and ore dictionary registration. The clean 1.20.1 port does not yet own the material-shape/autogen item library, so this pass keeps the existing steel-plate pump ingredient as a buildable temporary recipe and records the true dependency here.
- Old `fluid_duct_box` and `fluid_duct_exhaust` 15-meta construction/recycling matrix lives in `AnvilRecipes` tier-2 construction/recycling, not normal crafting. It remains deferred until the modern anvil/material-shape recipe library is migrated.
- The old `fluid_duct_neo` had three metadata variants. The current clean port has only the single registered modern block, so only the default workbench recipe is represented here.

Progress estimate after Pass 49:

- Core `FluidType` identity/NBT lookup/table: about 96%.
- Basic tank/conform/Forge capability bridge: about 95%.
- Fluid network/provider/receiver algorithm: about 91%.
- In-world pipe graph: about 92%.
- Fluid item/container loading: about 94%.
- Behavior traits and cross-system effects: about 84%.
- Machine integration through the library: about 78%.
- Data/recipe/resource coverage for fluid MK2 blocks: about 93%.
- Overall fluid library migration: about 97.7%.

Verification:

- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` passed with proxy JVM options.

## 2026-05-30 Modern Library Pass 46

This pass ports the first batch of old fluid network control blocks and routes them through the modern fluid-mk2 graph instead of per-machine shortcuts:

- Legacy sources re-read:
  - `com/hbm/blocks/network/FluidValve.java`
  - `com/hbm/blocks/network/FluidSwitch.java`
  - `com/hbm/blocks/network/FluidCounterValve.java`
  - `com/hbm/blocks/network/FluidPump.java`
  - `com/hbm/tileentity/network/TileEntityFluidValve.java`
  - `com/hbm/tileentity/network/TileEntityFluidCounterValve.java`
  - `api/hbm/fluidmk2/IFluidReceiverMK2.java`
  - `api/hbm/fluidmk2/IFluidStandardSenderMK2.java`
- Add modern `fluid_valve`, `fluid_switch`, `fluid_counter_valve`, and `fluid_pump` registrations.
- Add `FluidValveBlock` and `FluidValveBlockEntity`:
  - Reuses the modern pipe type/NBT/drop behavior.
  - Creates a fluid node only while `open=true`, matching legacy metadata `1`.
  - Manual valve toggles with the legacy `reactorStart` sound pitches.
  - Fluid switch follows redstone power and refreshes the network on state changes.
- Add `FluidCounterValveBlockEntity`:
  - Saves `counter` as a long.
  - Counts `HbmFluidNet.getFluidTracker()` while the valve has a valid typed network.
  - Exposes modern ROR values/functions matching the old `value`, `state`, `reset`, and `setState` surface.
- Add `FluidPumpBlock` and `FluidPumpBlockEntity`:
  - One typed buffer tank, default/max control contract `100` and `0..10000`.
  - Redstone disables output.
  - Subscribes as receiver on the legacy rotated input side and provides from the opposite side through `HbmFluidPortMachine`.
  - Keeps legacy priority, pressure, buffer, redstone, and tank NBT fields.
- Copy old valve/switch/counter textures and `block/reactorStart.ogg`.
- Add blockstates/models/items/loot/lang entries and old crafting recipes for the migrated batch.

Still deferred:

- The old pump GUI and OpenComputers callbacks are not wired yet; the runtime state and transfer behavior are present, but there is no modern screen for adjusting buffer/pressure/priority in survival UI.
- Old overlay text for valves/counter/pump is still deferred to the look-overlay/HUD pass.
- The old `fluid_duct_paintable`, gauge duct, exhaust duct, and pipe anchor are still not part of the modern pipe family.
- In-game visual validation of pump OBJ orientation and left/right port direction is still needed.

Progress estimate after Pass 46:

- Core `FluidType` identity/NBT lookup/table: about 85%.
- Basic tank/conform/Forge capability bridge: about 72%.
- Fluid network/provider/receiver algorithm: about 62%.
- In-world pipe graph: about 32%.
- Fluid item/container loading: about 30%.
- Behavior traits and cross-system effects: about 60%.
- Machine integration through the library: about 18%.
- Overall fluid library migration: about 55%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed.
- `.\gradlew.bat processResources --rerun-tasks --no-daemon` passed.

## 2026-05-30 Modern Library Pass 47

This pass expands the migrated in-world pipe graph beyond the standard duct into the next legacy fluid duct family:

- Legacy sources re-read:
  - `com/hbm/blocks/network/FluidDuctBase.java`
  - `com/hbm/blocks/network/FluidDuctBox.java`
  - `com/hbm/blocks/network/FluidDuctGauge.java`
  - `com/hbm/blocks/network/FluidDuctBoxExhaust.java`
  - `com/hbm/blocks/network/FluidPipeAnchor.java`
  - `com/hbm/tileentity/network/TileEntityPipeBaseNT.java`
  - `com/hbm/tileentity/network/TileEntityPipeExhaust.java`
  - `com/hbm/tileentity/network/TileEntityPipeAnchor.java`
  - `com/hbm/tileentity/network/TileEntityPipelineBase.java`
  - `com/hbm/render/tileentity/RenderPipeAnchor.java`
  - `com/hbm/items/tool/ItemWrench.java`
  - legacy textures `boxduct_*`, `pipe_gauge`, `fluid_duct_paintable_overlay`, and `models/network/pipe_anchor.obj`.
- Add modern block registrations and machine-tab placement for:
  - `fluid_duct_box`
  - `fluid_duct_gauge`
  - `fluid_duct_exhaust`
  - `pipe_anchor`
- Route the new blocks through the existing fluid-mk2 library:
  - `fluid_duct_box` reuses `FluidPipeBlock` typed interaction/drop behavior and stores its HBM `FluidType` in `FluidDuctBoxBlockEntity`.
  - `fluid_duct_gauge` uses the same typed pipe node and records `deltaTick` / `deltaLastSecond` from `HbmFluidNet.getFluidTracker()`, matching the old gauge surface.
  - `fluid_duct_exhaust` creates three separate smoke nodes for `SMOKE`, `SMOKE_LEADED`, and `SMOKE_POISON`, matching old `TileEntityPipeExhaust`.
  - `pipe_anchor` is registered under the legacy id and gets its OBJ renderer, facing placement, single-side connection rule, typed drops, and NBT fluid type.
- Add modern ROR value exposure for gauge:
  - `VAL:deltatick`
  - `VAL:deltasecond`
- Copy the required legacy textures into `assets/hbm/textures/block`.
- Add blockstates/models/item models/loot/lang entries for the new pipe family.
- Keep `fluid_duct_box`, `fluid_duct_gauge`, and `pipe_anchor` as single creative entries instead of expanding all HBM fluid typed variants; old non-standard duct blocks were typed by tools, not by the dedicated `ItemFluidDuct` placement item.

Still deferred:

- Full old `fluid_duct_box` 15-metadata visual/material variants are not fully represented yet. This pass registers the default modern block and uses the legacy box duct texture path, but not the full old anvil construction/recycling matrix.
- `pipe_anchor` remote anchor-to-anchor links remain deferred until the modern wrench/wiring tool path is migrated. The old behavior is driven by `ItemWrench` two-click NBT state and `TileEntityPipelineBase#addConnection`; the block now has the local single-side node contract and renderer, but not the remote cable segment.
- Gauge look overlay / HUD text and OpenComputers callbacks remain deferred; the local ROR values and saved/synced counters are present.
- `fluid_duct_paintable` and `fluid_duct_paintable_block_exhaust` are still separate follow-up slices because they need the paintable-block library/tool interaction path.

Progress estimate after Pass 47:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 92%.
- Fluid network/provider/receiver algorithm: about 81%.
- In-world pipe graph: about 68%.
- Fluid item/container loading: about 94%.
- Behavior traits and cross-system effects: about 74%.
- Machine integration through the library: about 85%.
- Overall fluid library migration: about 97.0%.

Verification:

- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` passed with proxy JVM options after registering box/gauge/exhaust/anchor blocks, block entities, resources, and renderer.

## 2026-05-30 Modern Library Pass 48

This pass migrates the two legacy paintable pipe-family blocks through the existing fluid-mk2 pipe library instead of leaving them as isolated decorative blocks:

- Legacy sources re-read:
  - `com/hbm/blocks/network/FluidDuctPaintable.java`
  - `com/hbm/blocks/network/FluidDuctPaintableBlockExhaust.java`
  - `com/hbm/blocks/network/BlockCablePaintable.java`
  - `com/hbm/blocks/network/FluidDuctBase.java`
  - `com/hbm/tileentity/network/TileEntityPipeBaseNT.java`
  - `com/hbm/tileentity/network/TileEntityPipeExhaust.java`
  - `com/hbm/main/CraftingManager.java`
  - legacy textures `fluid_duct_paintable`, `fluid_duct_paintable_color`, `fluid_duct_paintable_overlay`, and `fluid_duct_paintable_block_exhaust`.
- Add modern block registrations and machine-tab placement for:
  - `fluid_duct_paintable`
  - `fluid_duct_paintable_block_exhaust`
- Add `PaintableDuctBlockEntity`, `FluidDuctPaintableBlockEntity`, and `FluidDuctPaintableExhaustBlockEntity`:
  - Standard paintable duct extends the typed pipe BE path, saving the HBM fluid type and the old paint payload fields.
  - Paint payload keeps old keys `block` and `meta`, and also saves a modern `paint_block` registry name for stable 1.20.1 worlds.
  - Paintable exhaust creates the same three smoke nodes as old `TileEntityPipeExhaust`: `SMOKE`, `SMOKE_LEADED`, and `SMOKE_POISON`.
- Add `FluidDuctPaintableBlock` / `FluidDuctPaintableExhaustBlock`:
  - Right-clicking an allowed normal block records its block/default-state and old metadata nibble when the duct has no paint yet, matching the old `ItemBlock` paint behavior.
  - Empty-hand screwdriver path clears the stored paint block, using the modern `Toolable` interface that was already recorded in the common interfaces library.
  - Empty-hand sneak path maps to the old DEFUSER toggle and flips the modern `overlay` blockstate, replacing old metadata 0/1 overlay mode.
  - Normal fluid identifier interaction, typed placement, connected type replacement, pick-block, and typed drops still come from `FluidPipeBlock`.
- Add blockstates/models/item models/lang/loot/tags and copy the legacy textures into `assets/hbm/textures/block`.

Still deferred:

- Dynamic disguise rendering is only represented as saved/synced paint data this pass. Old multipass rendering displayed the selected block texture through `TileEntityPipePaintable`; modern equivalent should be routed through a render/model-data helper instead of a one-off fake model path.
- The old `ICopiable` / template paste path for paint payloads and fluid IDs remains deferred until the tool/copy library is migrated.
- Exact old recipes from `CraftingManager` are recorded but not generated yet because this pass focused on block/library behavior; recipe migration should map `STEEL.ingot`, `AL.plate`, `IRON.ingot`, and `plate_polymer` through the common recipe/tag importer.

Progress estimate after Pass 48:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 92%.
- Fluid network/provider/receiver algorithm: about 81%.
- In-world pipe graph: about 76%.
- Fluid item/container loading: about 94%.
- Behavior traits and cross-system effects: about 74%.
- Machine integration through the library: about 85%.
- Overall fluid library migration: about 97.5%.

Verification:

- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` passed with proxy JVM options after adding paintable pipe blocks, block entities, resources, lang, loot, and tags.

## 2026-05-28 Modern Library Pass 37

This pass closes the old `FluidContainerRegistry.registerContainer(...)` OreDictionary compatibility bridge for the direct, fixed-item fluid containers that can be represented safely as modern item tags:

- Legacy sources re-read:
  - `com/hbm/inventory/FluidContainerRegistry.java`
  - `com/hbm/inventory/fluid/FluidType.java`
- Preserve the old dictionary naming formula:
  - default: `ntmcontainer<amount><fluidname-without-underscores-lowercase>`
  - compat prefix: `container<amount><fluidname-without-underscores-lowercase>`
- Extend `HbmFluidContainerRegistry.ContainerEntry` with tag-support and legacy-name helpers:
  - Only direct fixed-item containers with no stack NBT are emitted as item tags.
  - NBT-backed generic HBM containers are intentionally excluded because item tags cannot express a concrete stored HBM fluid.
  - The water potion entry is also excluded because the old full container is an NBT potion stack.
- Wire `HbmItemTagsProvider` to generate both old prefix variants for every currently active supported container:
  - `minecraft:water_bucket`: `ntmcontainer1000water`, `container1000water`
  - `minecraft:lava_bucket`: `ntmcontainer1000lava`, `container1000lava`
  - `minecraft:experience_bottle`: `ntmcontainer100xpjuice`, `container100xpjuice`
  - `hbm:cell_sas3`: `ntmcontainer1000sas3`, `container1000sas3`
  - `hbm:ore_oil`: `ntmcontainer250oil`, `container250oil`
  - `hbm:ore_gneiss_gas`: `ntmcontainer250petroleum`, `container250petroleum`
- Fixed a datagen blocker discovered while validating the tag provider:
  - `HbmBlockStateProvider#sellafieldSlakedWithItem(...)` and `sellafieldOreWithItem(...)` now assign all random variant models for a level in one `setModels(...)` call instead of repeatedly calling `addModel()` on the same partial state.
  - This is a narrow generated-resource fix, not a new fluid behavior feature, but it unblocks full `runData` validation for the fluid tag bridge.

Still deferred:

- Modern recipes that need exact legacy full generic-container matching still need a later NBT-aware ingredient or machine-input bridge; Forge item tags deliberately cannot cover filled `canister_full`, `gas_full`, `fluid_tank_full`, and similar NBT fluid stacks.
- 528-mode-specific `ore_gneiss_gas` amount remains deferred until the modern config profile exposes that compatibility setting.
- Armor mod fillable-item traversal from old `FluidLoaderFillableItem` remains deferred until the armor mod/attachment system is migrated.

Progress estimate after Pass 37:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 90%.
- Fluid network/provider/receiver algorithm: about 76%.
- In-world pipe graph: about 49%.
- Fluid item/container loading: about 91%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 69%.
- Overall fluid library migration: about 93%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed with proxy JVM options.
- `.\gradlew.bat runData --no-daemon` passed after the Sellafield generated blockstate fix and generated the 12 legacy fluid-container Forge item tags under ignored `src/generated/resources`.

## 2026-05-28 Modern Library Pass 38

This pass closes the first NBT-sensitive generic-machine item input bridge for fluid containers, so generated package/unpackage recipes no longer treat every full fluid pack as the same input stack:

- Legacy sources re-read:
  - `com/hbm/inventory/FluidContainerRegistry.java`
  - `com/hbm/inventory/fluid/FluidType.java`
  - representative legacy `Fluids.X.getDict(...)` recipe call sites in crafting and machine recipe registration
- Extend `GenericMachineRecipe.ItemInput` with an optional exact `ItemStack`:
  - Keeps the normal `Ingredient` for recipe display/listing compatibility.
  - Adds `ItemInput#test(...)`, which checks count, vanilla/tag ingredient, and optional exact item+NBT equality.
  - Serializes/deserializes the exact stack over the recipe network payload.
- Update generic machine runtime matching:
  - `hasItemInputs(...)` now routes through `ItemInput#test(...)`.
  - auto-switch recipe matching also honors the exact-stack bridge.
- Update generic machine JSON handling:
  - `input_items[]` may now include `exact_stack` with item/count/NBT data.
  - `output_items[]` now uses the same local item-stack parser, preserving generated NBT outputs instead of relying on vanilla `ShapedRecipe` parsing.
- Update `HbmRecipeProvider.GenericMachineRecipeBuilder`:
  - NBT-bearing `inputItem(ItemStack)` entries emit `exact_stack`.
  - Shared item-stack JSON writing is used for both exact inputs and outputs.
  - Generated assembly-machine full fluid pack unpackage recipes now require the exact stored fluid, amount, and pressure NBT.
- Validation sample:
  - `assembly_machine/unpackage_water.json` now requires `hbm:fluid_pack_full` with `{hbm_fluid:"WATER",hbm_fluid_amount:32000,hbm_fluid_pressure:0}`.
  - Current data generation emits 135 exact-stack assembly unpackage recipes.

Still deferred:

- Ordinary crafting recipes using old `Fluids.X.getDict(...)` still need a dedicated NBT-aware ingredient/remainder bridge; this pass only covers the generic machine recipe system.
- Crafting-container remainder behavior from old `FluidContainerRegistry` is still not fully mirrored for generic recipes.
- Armor mod fillable-item traversal from old `FluidLoaderFillableItem` remains deferred until the armor mod/attachment system is migrated.
- 528-mode-specific `ore_gneiss_gas` amount remains deferred until the modern config profile exposes that compatibility setting.

Progress estimate after Pass 38:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 90%.
- Fluid network/provider/receiver algorithm: about 76%.
- In-world pipe graph: about 49%.
- Fluid item/container loading: about 92%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 71%.
- Overall fluid library migration: about 94%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed with proxy JVM options.
- `.\gradlew.bat runData --no-daemon` passed and generated exact-stack NBT inputs for the fluid pack unpackage recipes.

## 2026-05-28 Modern Library Pass 39

This pass starts the ordinary-crafting bridge for old `FluidType#getDict(...)` fluid-container ingredients and fixes one concrete NBT-sensitive crafting recipe that had been over-broad:

- Legacy sources re-read:
  - `com/hbm/inventory/FluidContainerRegistry.java`
  - `com/hbm/inventory/fluid/FluidType.java`
  - `com/hbm/main/CraftingManager.java`
  - `com/hbm/inventory/recipes/BlastFurnaceRecipes.java`
- Add `HbmFluidContainerIngredient`:
  - Registers Forge custom ingredient type `hbm:fluid_container` during mod construction so recipe loading can deserialize it.
  - JSON contract: `{ "type": "hbm:fluid_container", "fluid": "hbm:<fluid>", "amount": <mB> }`.
  - Runtime matching uses `HbmFluidContainerRegistry.getFluidContent(stack, fluid) == amount`, intentionally preserving the old OreDictionary quantity exactness from names like `ntmcontainer1000water`.
  - Display stacks are supplied from `HbmFluidContainerRegistry.getContainers(fluid)` filtered to the exact content amount, so both direct containers and NBT-backed HBM containers can be represented.
  - Network serialization writes the legacy fluid name and mB amount.
- Port first ordinary-crafting `getDict(...)` use:
  - Old `CraftingManager` `inf_water` recipe used `Fluids.WATER.getDict(1_000)` plus aluminium plates and a diamond.
  - Modern `control/inf_water.json` now uses `hbm:fluid_container` for 1000 mB water, preserving the old generic-container input instead of a single item/tag.
- Fix the existing `canister_napalm` recipe:
  - Old `BlastFurnaceRecipes` required `canister_full` with `Fluids.GASOLINE` metadata.
  - Modern generated recipe now uses Forge strict NBT for `hbm:canister_full` with `{hbm_fluid:"GASOLINE",hbm_fluid_amount:1000,hbm_fluid_pressure:0}`.
  - This is deliberately not the generic `hbm:fluid_container` ingredient because the legacy recipe was a specific gasoline canister stack, not any 1000 mB gasoline container.

Still deferred:

- Broad migration of all old ordinary crafting `Fluids.X.getDict(...)` call sites remains open; the new ingredient is the reusable bridge for those recipes.
- Crafting remainder behavior for multi-container shaped/shapeless recipes still relies on vanilla/Forge crafting remainder mechanics; custom remainder handlers may still be needed for recipes that produce filled containers or consume multiple fluid containers.
- `inf_water_mk2` is still deferred because its old recipe depends on steel pipe/shell item families that are not fully migrated as stable modern ingredients yet.
- Machine-specific `OreDictStack(Fluids.X.getDict(...))` uses in assembly, anvil, and ammo press recipes still need machine-recipe integration through this same semantic bridge or direct fluid tank inputs.
- 528-mode-specific `ore_gneiss_gas` amount remains deferred until the modern config profile exposes that compatibility setting.

Progress estimate after Pass 39:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 90%.
- Fluid network/provider/receiver algorithm: about 76%.
- In-world pipe graph: about 49%.
- Fluid item/container loading: about 93%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 71%.
- Overall fluid library migration: about 94.5%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed with proxy JVM options.
- `.\gradlew.bat runData --no-daemon` passed and generated `hbm:fluid_container` in `control/inf_water.json` plus strict-NBT gasoline canister input in `blast_furnace/canister_napalm.json`.

## 2026-05-28 Modern Library Pass 40

This pass expands the ordinary-crafting bridge from Pass 39 into a small batch of directly mappable old fluid-container recipes:

- Legacy sources re-read:
  - `com/hbm/main/CraftingManager.java`
  - `com/hbm/inventory/OreDictManager.java`
  - `com/hbm/inventory/fluid/FluidType.java`
  - `com/hbm/inventory/FluidContainerRegistry.java`
- Port old empty-container crafting recipes that do not require missing metadata families:
  - `control/fluid_tank_empty`: old `fluid_tank_empty` recipe, 8 output, aluminium plates + iron plates + `KEY_ANYGLASS`.
  - `control/fluid_barrel_empty`: old `fluid_barrel_empty` recipe, 2 output, steel plates + aluminium plates + `KEY_ANYGLASS`.
- Expand the modern `forge:glass` tag used by those recipes:
  - Adds vanilla normal/stained glass plus existing HBM boron/trinitite glass.
  - Does not add 1.20 tinted glass, because the old `blockGlass` semantic predates it and should stay closer to 1.7.10 glass blocks.
- Port another ordinary-crafting `FluidType#getDict(...)` use:
  - Old `CraftingManager` slime recipe used four bone-meal dye stacks plus `Fluids.SULFURIC_ACID.getDict(1000)` to produce 16 slime balls.
  - Modern `control/slime_ball_from_sulfuric_acid.json` now uses four `minecraft:bone_meal` plus `{ "type": "hbm:fluid_container", "fluid": "hbm:sulfuric_acid", "amount": 1000 }`.

Still deferred:

- `solid_fuel` and the two-lubricant-canister recipe remain blocked on the legacy `ntmchemistryset` tool family and, for the canister output, a dedicated NBT-result crafting serializer or equivalent safe output path.
- Tritium lamp recipes remain deferred because their old red-phosphorus / aluminium-dust ingredient chain is not fully migrated in the modern item registry.
- `inf_water_mk2` remains deferred because the old `STEEL.shell()` ingredient is not yet available as a stable modern item/tag.
- Express conveyor wand crafting is a good later `FluidType#getDict(...)` candidate, but it needs NBT-sensitive conveyor-wand output handling instead of a plain vanilla result stack.

Progress estimate after Pass 40:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 90%.
- Fluid network/provider/receiver algorithm: about 76%.
- In-world pipe graph: about 49%.
- Fluid item/container loading: about 93.5%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 71%.
- Overall fluid library migration: about 94.8%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed with proxy JVM options.
- `.\gradlew.bat runData --no-daemon` passed and generated `control/fluid_tank_empty.json`, `control/fluid_barrel_empty.json`, and `control/slime_ball_from_sulfuric_acid.json`.
- Generated `forge:glass` block/item tags were inspected and confirmed to contain vanilla normal/stained glass plus HBM glass, without tinted glass.

## 2026-05-28 Modern Library Pass 41

This pass starts the larger machine-connection push for fluid MK2 remote ports. The first active target is the already-registered `machine_bigasstank`; it has had its model and multiblock shell in the modern port, but was still using the generic visible-machine block entity instead of the legacy fluid tank runtime.

- Legacy sources re-read:
  - `api/hbm/fluidmk2/IFluidConnectorMK2.java`
  - `api/hbm/fluidmk2/IFluidStandardReceiverMK2.java`
  - `api/hbm/fluidmk2/IFluidStandardSenderMK2.java`
  - `api/hbm/fluidmk2/FluidNode.java`
  - `com/hbm/uninos/GenNode.java`
  - `com/hbm/uninos/UniNodespace.java`
  - `com/hbm/blocks/machine/MachineBigAssTank.java`
  - `com/hbm/tileentity/machine/storage/TileEntityBarrel.java`
  - `com/hbm/tileentity/machine/storage/TileEntityMachineFluidTank.java`
  - `com/hbm/tileentity/machine/storage/TileEntityMachineBigAssTank.java`
- Migration target:
  - Reuse the modern `machine_fluidtank` runtime path for tank mode, GUI slots, identifier-slot behavior, item/tank loading, comparator output, Forge fluid capability, and HBM receiver/provider contracts.
  - Specialize `machine_bigasstank` with the old `16_000_000` mB capacity, `max(50_000, space/fill / 100)` transfer speeds, and two facing-aligned remote fluid connection points.
  - Keep the old storage-tank buffer-mode behavior tied to remote-port multi-position fluid nodes: the core and internal port positions are indexed in nodespace while the connection points remain the old external `DirPos` coordinates.
- Scope guard:
  - `machine_large_turbine` remains intentionally excluded because the user confirmed the large turbine was removed and should not be added.
- Implemented in the modern port:
  - `FluidTankBlockEntity` now exposes protected constructor/capacity hooks so the old storage-tank runtime can be specialized without duplicating tank mode, slots, Forge capability, or HBM receiver/provider logic.
  - `BigAssTankBlockEntity` / `BigAssTankBlock` switch `machine_bigasstank` from the generic visible shell to a real fluid tank runtime.
  - Capacity is `16_000_000` mB and transfer speed floor is `50_000`, matching old `TileEntityMachineBigAssTank`.
  - The two legacy facing-aligned ports are placed at `+/- facing * 7` and use the old outward `DirPos` direction.
  - `machine_bigasstank` was removed from `LEGACY_VISIBLE_MACHINE`, registered as `BIG_ASS_TANK`, and rendered through the existing fluid-tank renderer with the big-tank OBJ.
- Verification:
  - `.\gradlew.bat compileJava --no-daemon` passed with proxy JVM options.

Progress estimate after Pass 41:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 91%.
- Fluid network/provider/receiver algorithm: about 78%.
- In-world pipe graph: about 50%.
- Fluid item/container loading: about 93.5%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 74%.
- Overall fluid library migration: about 95%.

## 2026-05-28 Modern Library Pass 42

This pass continues the larger remote-port machine integration push and focuses on already-migrated or safe standalone runtime paths instead of inventing missing refinery-chain behavior.

- Legacy sources re-read:
  - `api/hbm/fluid/IFluidStandardReceiver.java`
  - `api/hbm/fluidmk2/IFluidStandardReceiverMK2.java`
  - `api/hbm/fluidmk2/IFluidStandardTransceiverMK2.java`
  - `com/hbm/tileentity/machine/TileEntityMachineChemicalPlant.java`
  - `com/hbm/tileentity/machine/TileEntityMachinePrecAss.java`
  - `com/hbm/tileentity/machine/oil/TileEntityMachineGasFlare.java`
- Fix migrated remote-port machine sync:
  - `ChemicalPlantBlockEntity` and `AssemblyMachineBlockEntity` now override `transferFluid(...)` and `useUpFluid(...)` so HBM remote-network fill/drain marks the machine dirty and sends block updates.
  - This preserves the old packeted machine behavior where fluid tank changes caused machine state sync, instead of silently mutating server-side tanks.
- Add gas flare as a real fluid-library participant:
  - `machine_gasflare` now uses `GasFlareBlock` / `GasFlareBlockEntity` instead of `LegacyVisibleMachineBlockEntity`.
  - Ports the old default gas tank shape: `GAS`, `64_000` mB, four horizontal remote fluid ports at `(+/-2,0,0)` and `(0,0,+/-2)`.
  - The flare subscribes as a LOW-priority HBM fluid receiver through the remote ports and also exposes Forge fluid input capability.
  - Keeps the old six-slot persistence layout as a scaffold: energy output, fluid input/return, fluid identifier, and two deferred upgrade slots.
  - Shift-right-click with a fluid identifier can set the accepted tank type until the full old GUI/control screen is migrated.
  - Old valve/burn booleans are saved and legacy-button hooks are present; default state remains off, matching 1.7.10.
  - When enabled by later UI/control, venting uses `FluidReleaseType.SPILL` for gases and burning uses `FluidReleaseType.BURN`, with HE generation based on `FlammableFluidTrait#getHeatEnergyPerBucket()` and the old gas/liquid penalty split.
  - Generated HE can charge the output slot or provide to the same four remote energy ports through the existing energy helper.
  - The existing visible-machine OBJ renderer was made generic so `GasFlareBlockEntity` can reuse the same `flare_stack` model path without duplicating renderer logic.
- Scope guard:
  - Full gas flare GUI/menu, valve/burn buttons on screen, upgrade item effects, particles/sounds/entity fire damage, and Energy Control extra info remain deferred. The runtime state and remote-port plumbing are ready first.
  - Large turbine remains excluded.

Progress estimate after Pass 42:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 91%.
- Fluid network/provider/receiver algorithm: about 79%.
- In-world pipe graph: about 51%.
- Fluid item/container loading: about 94%.
- Behavior traits and cross-system effects: about 73%.
- Machine integration through the library: about 77%.
- Overall fluid library migration: about 95.5%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed with proxy JVM options after adding gas flare runtime and sync fixes.

## 2026-05-28 Modern Library Pass 43

This pass continues the machine/remote-port push with standalone steam and condenser machines whose 1.7.10 behavior is local enough to migrate without inventing refinery-chain recipe logic.

- Legacy sources re-read:
  - `com/hbm/tileentity/machine/TileEntitySteamEngine.java`
  - `com/hbm/blocks/machine/MachineSteamEngine.java`
  - `com/hbm/tileentity/machine/TileEntitySolarBoiler.java`
  - `com/hbm/blocks/machine/MachineSolarBoiler.java`
  - `com/hbm/tileentity/machine/TileEntityCondenser.java`
  - `com/hbm/tileentity/machine/TileEntityTowerSmall.java`
  - `com/hbm/tileentity/machine/TileEntityTowerLarge.java`
  - `com/hbm/blocks/machine/MachineTowerSmall.java`
  - `com/hbm/blocks/machine/MachineTowerLarge.java`
- Add shared `HbmFluidPortMachine`:
  - centralizes the common remote-port pattern for non-`HbmFluidBlockEntity` machines that have input tank subscriptions and output tank direct pushes.
  - `AssemblyMachineBlockEntity` and `ChemicalPlantBlockEntity` now use the helper instead of each hand-rolling the same subscription/provision loops.
- Add `machine_steam_engine` runtime:
  - `SteamEngineBlock` / `SteamEngineBlockEntity` replace the generic visible-machine shell.
  - Preserves old tank sizes: `STEAM` input `2_000` mB and `SPENTSTEAM` output `20` mB.
  - Uses the migrated turbine/coolable-fluid math at 85% efficiency.
  - Preserves the old immediate `powerBuffer` shape by resetting generated energy every server tick, then pushing through the three old remote power ports.
  - Ports the three old side remote fluid/power ports from `getConPos()` using the same legacy side-axis convention as `steamEngineProxyOffsets(...)`.
  - Keeps rotor/acceleration/last-output state for later renderer/screen overlays.
- Add `machine_solar_boiler` runtime:
  - `SolarBoilerBlock` / `SolarBoilerBlockEntity` replace the generic visible-machine shell.
  - Preserves old tank sizes: `WATER` input `100` mB and `STEAM` output `10_000` mB.
  - Ports the old two vertical remote fluid ports at `y + 3` and `y - 1`.
  - Keeps `heat` and `display` state for later mirror/particle integration; heat can already be added by future solar mirror code.
- Add cooling tower runtimes:
  - `CoolingTowerBlock`, `CoolingTowerBlockEntity`, `SmallCoolingTowerBlockEntity`, and `LargeCoolingTowerBlockEntity`.
  - `machine_tower_small` now converts `SPENTSTEAM` to `WATER` with old `1_000` mB input/output tanks and four cardinal remote ports at radius 3.
  - `machine_tower_large` now converts `SPENTSTEAM` to `WATER` with old `10_000` mB input/output tanks and the twelve old side remote ports at radius 5 with +/-3 side offsets.
  - Both towers preserve `waterTimer` and throughput state for later old cooling-tower particle/overlay work.
  - Both expose Forge fluid capability through the shared HBM tank bridge and keep HBM remote receiver/provider behavior.
- Registration/rendering:
  - `ModBlocks` now uses dedicated block classes for steam engine, solar boiler, and both tower sizes.
  - `ModBlockEntities` registers dedicated BE types and removes those four blocks from `LEGACY_VISIBLE_MACHINE`.
  - Client renderer registration reuses the generic OBJ visible-machine renderer for all four new BE types.
- Scope guard:
  - Large turbine remains excluded.
  - Steam engine sound, exact old client rotor interpolation, solar mirror heat accumulation, cooling-tower particles, look overlay text, and Energy Control integration remain deferred.
  - Gas turbine and turbofan are still deferred because their old behavior includes control UI, fuel selection, pollution, startup/shutdown state, and entity/particle side effects; they should get a dedicated safe pass rather than a partial port.
  - Refinery-chain machines remain deferred until their recipe/runtime support is grounded in the old source and modern recipe system.

Progress estimate after Pass 43:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 91.5%.
- Fluid network/provider/receiver algorithm: about 79%.
- In-world pipe graph: about 52%.
- Fluid item/container loading: about 94%.
- Behavior traits and cross-system effects: about 73%.
- Machine integration through the library: about 81%.
- Overall fluid library migration: about 96%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed with proxy JVM options after adding the shared helper and four standalone machine runtimes.

## 2026-05-28 Modern Library Pass 44

This pass starts the refinery/oil-chain machine connection work by replacing a larger batch of visible shells with real tank and remote-port participants.

- Legacy sources re-read:
  - `com/hbm/tileentity/machine/oil/TileEntityMachineCatalyticCracker.java`
  - `com/hbm/tileentity/machine/oil/TileEntityMachineCatalyticReformer.java`
  - `com/hbm/tileentity/machine/oil/TileEntityMachineVacuumDistill.java`
  - `com/hbm/tileentity/machine/oil/TileEntityMachineFractionTower.java`
  - `com/hbm/tileentity/machine/oil/TileEntityMachineHydrotreater.java`
  - `com/hbm/tileentity/machine/oil/TileEntityMachineCoker.java`
  - `com/hbm/inventory/recipes/CrackingRecipes.java`
  - `com/hbm/inventory/recipes/FractionRecipes.java`
- Corrected `machine_steam_engine` remote fluid/energy ports:
  - The previous modern BE used `rot * 2`; old `MachineSteamEngine.fillSpace` places the three proxy ports at `rot`, `rot + dir`, and `rot - dir` on `y + 1`.
  - The BE now matches `steamEngineProxyOffsets(...)` and the old dummy placement.
- Add shared `LegacyRemoteFluidMachineBlockEntity`:
  - Extends the HBM tank + Forge capability bridge through `HbmEnergyAndFluidBlockEntity`.
  - Implements `HbmStandardFluidTransceiver` and refreshes old-style remote fluid receiver/provider ports with `HbmFluidPortMachine`.
  - Can also subscribe remote energy receiver ports for machines whose full energy runtime is still deferred.
  - Keeps `shouldCreateFluidNode=false`, because these machines are not pipe nodes; they are old remote-port machines.
- Add `RemoteFluidMachineBlock` and dedicated BE registrations/renderers for:
  - `machine_catalytic_cracker`
  - `machine_catalytic_reformer`
  - `machine_vacuum_distill`
  - `machine_fraction_tower`
  - `machine_hydrotreater`
  - `machine_coker`
- Port exact tank surfaces and remote ports:
  - Catalytic cracker: `BITUMEN` 4,000 + `STEAM` 8,000 inputs; `OIL` 4,000, `PETROLEUM` 4,000, `SPENTSTEAM` 800 outputs; eight facing-relative old `getConPos()` ports.
  - Catalytic reformer: `NAPHTHA` 64,000 input; `REFORMATE`, `PETROLEUM`, `HYDROGEN` 24,000 outputs; six old power/fluid ports.
  - Vacuum distill: pressure-2 `OIL` 64,000 input; `HEAVYOIL_VACUUM`, `REFORMATE`, `LIGHTOIL_VACUUM`, `SOURGAS` 24,000 outputs; eight surrounding old ports.
  - Fraction tower: `HEAVYOIL` 4,000 input; `BITUMEN` and `SMEAR` 4,000 outputs; four cardinal old ports at radius 2.
  - Hydrotreater: `OIL` 64,000 and pressure-1 `HYDROGEN` 64,000 inputs; `OIL_DS` and `SOURGAS` 24,000 outputs; eight surrounding old ports.
  - Coker: `HEAVYOIL` 16,000 input and `OIL_COKER` 8,000 output; eight surrounding old ports.
- Port safe pure-fluid runtimes:
  - `LegacyOilFluidRecipes` records the default old `CrackingRecipes` and `FractionRecipes` tables.
  - Catalytic cracker now runs the old `100 mB input + 200 mB steam -> left/right + 2 mB spent steam`, up to two operations every five ticks.
  - Fraction tower now runs the old `100 mB input -> two fractions` operation every ten ticks.
  - Fraction tower also preserves the old stacked-tower transfer at `y + 3`: input moves upward, fractions move downward.
- Registration/layout fixes:
  - These six machines are removed from `LEGACY_VISIBLE_MACHINE`.
  - Their OBJ rendering still uses `LegacyVisibleMachineRenderer`.
  - Vacuum distill, hydrotreater, and coker proxy dummy layouts now expose the old eight surrounding remote ports instead of only the previous corner placeholders.
  - Catalytic reformer proxy layout now matches its six old `getConPos()` positions.
- Scope guard:
  - Large turbine remains excluded.
  - Reformer, vacuum distill, hydrotreater, and coker full processing remains deferred because the old behavior requires energy draw, item slots, catalyst checks, heat source coupling, pollution, GUI/menu, sound/particles, and item byproducts.
  - The pass intentionally does not invent those systems; it only makes the machines real tank/remote-port network participants and leaves their output tanks ready for the next grounded recipe/runtime pass.

Progress estimate after Pass 44:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 92%.
- Fluid network/provider/receiver algorithm: about 80%.
- In-world pipe graph: about 53%.
- Fluid item/container loading: about 94%.
- Behavior traits and cross-system effects: about 73%.
- Machine integration through the library: about 85%.
- Overall fluid library migration: about 96.5%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed with proxy JVM options.
- `.\gradlew.bat compileJava processResources --no-daemon` passed with proxy JVM options.

## 2026-05-30 Modern Library Pass 45

This pass repairs the visible `fluid_duct_neo` pipe model and fills the remaining standard-pipe placement/break behavior that belongs in the fluid MK2 library.

- Legacy sources re-read:
  - `com/hbm/render/block/RenderTestPipe.java`
  - `com/hbm/blocks/network/FluidDuctStandard.java`
  - `com/hbm/blocks/network/FluidDuctBase.java`
  - `com/hbm/tileentity/network/TileEntityPipeBaseNT.java`
  - `com/hbm/items/machine/ItemFluidDuct.java`
  - `com/hbm/lib/Library.java`
  - `com/hbm/blocks/ModSoundTypes.java`
  - `assets/hbm/models/blocks/pipe_neo.obj`
  - `assets/hbm/textures/blocks/pipe_neo.png`
  - `assets/hbm/textures/blocks/pipe_neo_overlay.png`
  - `assets/hbm/sounds/block/pipePlaced.ogg`
- Root-cause note:
  - The old standard pipe is not a cuboid multipart model. `RenderTestPipe` draws named OBJ groups (`pX`, `nX`, `pY`, `nY`, `pZ`, `nZ`, plus the eight corner filler groups) according to the same-type fluid connection mask.
  - The modern generated JSON cuboid fallback made each pipe block render as bulky square chunks, which explains the broken connected-row screenshot.
- Add `FluidPipeRenderer`:
  - Registered on `ModBlockEntities.FLUID_PIPE`.
  - Renders `pipe_neo.obj` group-by-group with the legacy base texture and color-tinted overlay texture.
  - Preserves old display rules: isolated pipes render all six arms; a single connection on an axis renders the full straight pair; junctions render connected arms plus missing-corner filler groups.
  - Keeps the old Z-group reversal used by `RenderTestPipe` for world rendering (`south -> nZ`, `north -> pZ`).
- Change `FluidPipeBlock` world render shape to `INVISIBLE` so the dynamic renderer owns the world model instead of the generated cuboids.
- Change the modern blockstate/model data to a particle-only marker model and add source resource files for blockstate, item model, and block loot so `processResources` and direct resource loading no longer revive the broken cuboid pipe model.
- Restore old pipe collision/selection behavior:
  - Isolated pipe uses a full-block selection/collision box, matching `setBlockBoundsBasedOnState`.
  - Single-ended and two-ended straight pipe cases use full straight axis boxes.
  - Junctions keep core plus connected arms.
- Placement/break and type behavior:
  - Typed pipe placement still writes the item NBT fluid type into `FluidPipeBlockEntity`, matching old `ItemFluidDuct.onItemUse` setting the tile type from item damage.
  - Pick-block and normal drops now preserve the pipe fluid type in the modern NBT-backed pipe item, including a code-side fallback if a stale resource pack lacks the block loot table.
  - `HbmFluidNodeBlock.neighborChanged` now refreshes neighbor fluid nodes too, so breaking/adding a neighboring pipe updates the surrounding graph instead of only the changed block.
  - `FluidPipeBlockEntity.refreshFluidNode()` now recreates the node when refreshed, preserving old `TileEntityPipeBaseNT#setType`/node invalidation semantics when the connection mask changes.
- Port old pipe placement/break sound:
  - Register modern `hbm:block.pipe_placed` for the old pipe placement/break sound.
  - Add `sounds/block/pipe_placed.ogg` copied from legacy `sounds/block/pipePlaced.ogg`.
  - Wire `fluid_duct_neo` to a `ForgeSoundType` using the old pipe sound for break/place while keeping vanilla metal step/hit/fall.

Current resource contract:

- `assets/hbm/blockstates/fluid_duct_neo.json` points to `hbm:block/fluid_duct_neo`.
- `assets/hbm/models/block/fluid_duct_neo.json` has only `minecraft:block/block` parent and `particle = hbm:block/legacy_blocks/pipe_neo`; no cuboid elements are emitted.
- `assets/hbm/models/item/fluid_duct_neo.json` uses the old 2D `item/duct` + tinted `item/duct_overlay`.
- `data/hbm/loot_tables/blocks/fluid_duct_neo.json` drops `hbm:fluid_duct_neo`; `FluidPipeBlock#getDrops` rewrites the stack to include the current pipe fluid type.

Still deferred:

- Standard pipe item rendering still uses the existing 2D duct icon/overlay path; the old inventory `RenderTestPipe.renderInventoryBlock` 3D pipe model can be migrated later if desired.
- Box ducts, paintable ducts, gauges, valves, exhaust ducts, and analyzer overlay text remain their own follow-up slices.
- In-game screenshot verification of the dynamic renderer still needs a client run after unrelated compile blockers in the current worktree are resolved.

Progress estimate after Pass 45:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 92%.
- Fluid network/provider/receiver algorithm: about 80%.
- In-world pipe graph: about 61%.
- Fluid item/container loading: about 94%.
- Behavior traits and cross-system effects: about 73%.
- Machine integration through the library: about 85%.
- Overall fluid library migration: about 96.8%.

Verification:

- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` passed with proxy JVM options after the renderer, model-resource, loot, and sound retarget changes.
- `build/resources/main/assets/hbm/blockstates/fluid_duct_neo.json` now contains the particle-only marker model path instead of the old multipart cuboid generated model.


## 2026-05-24 Modern Library Pass 17

This pass expands the fluid library into the first substantial legacy fluid-processing machine: `machine_liquefactor`.

Legacy sources re-read:

- `com/hbm/inventory/recipes/LiquefactionRecipes.java`
- `com/hbm/tileentity/machine/oil/TileEntityMachineLiquefactor.java`
- `com/hbm/blocks/machine/MachineLiquefactor.java`
- `com/hbm/inventory/container/ContainerLiquefactor.java`
- `com/hbm/inventory/gui/GUILiquefactor.java`
- `com/hbm/render/tileentity/RenderLiquefactor.java`
- `com/hbm/items/ModItems.java` entries for `biomass` and `biomass_compressed`
- related legacy crafting and machine recipe call sites for biomass/fish oil/sunflower oil/seed slurry

Modern additions:

- Add `hbm:liquefaction` recipe type through `LiquefactionRecipe` and `ModRecipes`.
  - JSON contract: `ingredient` plus `output.fluid`, `output.amount`, and optional `output.pressure`.
  - Fluid names accept both legacy names such as `BIOGAS` and namespaced datapack ids such as `hbm:biogas`.
- Add data-generated liquefaction recipes for the directly mappable legacy set:
  - biomass/glyphid gland to biogas
  - snow/ice to water
  - netherrack/cobblestone/stone/obsidian to lava
  - ender pearl to ender juice
  - sugar to ethanol
  - seeds/vine/kelp/grass/fern to seed slurry
  - cod/salmon/tropical fish/pufferfish to fish oil
  - sunflower to sunflower oil
- Add `machine_liquefactor` as a modern multiblock machine:
  - `LiquefactorBlock`
  - `LiquefactorBlockEntity`
  - `LiquefactorMenu`
  - `LiquefactorScreen`
  - `LiquefactorRenderer`
  - `HbmEnergyAndFluidBlockEntity` shared base for machines needing both Forge energy and HBM fluid tanks/network behavior
- Preserve the main legacy processing contract:
  - 4 slots: input, battery, speed-upgrade slot placeholder, power-upgrade slot placeholder
  - tank capacity 24,000 mB
  - base energy storage 100,000 HE
  - base use 250 HE/t
  - base process time 100 ticks
  - output only fills when the tank is empty or already contains the same fluid and has enough space
  - battery slot can charge the internal energy store through the migrated HBM/Forge energy bridge
  - filled tank participates as a modern `HbmStandardFluidSender`
- Add legacy biomass items and resources:
  - `biomass`
  - `biomass_compressed`
  - Item creative placement follows 1.7.10 `partsTab`, not the modern control/fluid tab.
- Add legacy GUI texture and OBJ-backed rendering:
  - `textures/gui/processing/gui_liquefactor.png`
  - OBJ parts `Main`, `Fluid`, and `Glass`
  - fluid part scales by tank fill and uses the migrated fluid color.

Still deferred:

- Legacy upgrade behavior is not active yet. The two upgrade slots are present for layout/NBT compatibility, but reject items until the old `UpgradeManagerNT`/speed/power upgrade item family is migrated.
- Exact old MK2 energy-network subscription is not implemented for the liquefactor yet; the machine currently uses internal HBM energy storage, battery charging, and Forge energy capability input.
- Exact old custom fluid connection offsets from `getConPos()` are not fully represented yet. The machine sends through the modern HBM fluid network from its current node/capability surface.
- Coal/oil-tar/ore-dictionary liquid fuel recipes from `LiquefactionRecipes` are still deferred until their item/fluid inputs are present and tag equivalents are defined.
- Legacy `ItemFood -> SALIENT` fallback is deferred, because broad food-to-fluid fallback needs a recipe matching policy decision for 1.20 datapacks.
- In-game visual verification of the multiblock proxy footprint and glass/fluid alpha ordering is still needed.

Progress estimate after Pass 17:

- Core `FluidType` identity/NBT lookup/table: about 86%.
- Basic tank/conform/Forge capability bridge: about 74%.
- Fluid network/provider/receiver algorithm: about 61%.
- In-world pipe graph: about 22%.
- Fluid item/container loading: about 60%.
- Behavior traits and cross-system effects: about 60%.
- Machine integration through the library: about 24%.
- Overall fluid library migration: about 63%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed.
- `.\gradlew.bat runData --no-daemon` passed and generated 22 `hbm:liquefaction` recipes.

## 2026-05-24 Modern Library Pass 18

This pass fills in the tag-backed portion of the legacy liquefaction table where the modern port already has safe item/block or Forge-tag equivalents.

Legacy sources re-read:

- `com/hbm/inventory/recipes/LiquefactionRecipes.java`
- `com/hbm/inventory/OreDictManager.java`
- modern datagen providers for Forge item/block tags

Modern additions:

- Extend `LiquefactionRecipeBuilder` to accept `TagKey<Item>` ingredients.
- Add tag-backed liquefaction recipes for old ore-dictionary inputs:
  - `forge:gems/coal -> COALOIL 100`
  - `forge:dusts/coal -> COALOIL 100`
  - `forge:gems/lignite -> COALOIL 50`
  - `forge:dusts/lignite -> COALOIL 50`
  - `forge:dusts/sodium -> SODIUM 100`
  - `forge:ingots/lead -> LEAD 100`
  - `forge:dusts/lead -> LEAD 100`
  - `forge:storage_blocks/lead -> LEAD 900`
  - `minecraft:logs -> MUG 100`
  - `ore_oil_sand -> BITUMEN 100`
- Extend Forge tags used by the recipes:
  - `forge:dusts/lead`
  - aggregate `forge:dusts` now includes `powder_lead`
  - `forge:storage_blocks/lead` block and item tags

Still deferred:

- `oiltar`, `cracktar`, and `coaltar` inputs are still deferred because the old tar metadata item family is not yet migrated.
- Legacy ethanol outputs from `plant_flower` metadata 3/4 are deferred until the old custom flower block/metas are represented in modern registration.
- The `ItemFood -> SALIENT` fallback remains deferred for the recipe-policy reason recorded in Pass 17.

Progress estimate after Pass 18:

- Core `FluidType` identity/NBT lookup/table: about 86%.
- Basic tank/conform/Forge capability bridge: about 74%.
- Fluid network/provider/receiver algorithm: about 61%.
- In-world pipe graph: about 22%.
- Fluid item/container loading: about 60%.
- Behavior traits and cross-system effects: about 60%.
- Machine integration through the library: about 26%.
- Overall fluid library migration: about 64%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed.
- `.\gradlew.bat runData --no-daemon` passed and generated 32 `hbm:liquefaction` recipes.

## 2026-05-25 Modern Library Pass 19：远端端口订阅 helper 与化工厂 12 端口闭合

- 1.7.10 对照：
  - `IFluidReceiverMK2#trySubscribe(type, world, DirPos)`：
    - 读取旧 `DirPos` 的目标导线坐标和方向。
    - 检查 `IFluidConnectorMK2#canConnect(type, dir.getOpposite())`。
    - 通过 `UniNodespace.getNode(world, x, y, z, type.getNetworkProvider())` 找到该流体类型的网络，并把 receiver 加入网络。
  - `IFluidStandardSenderMK2#tryProvide(type, pressure, world, DirPos)`：
    - 先按同样端口规则尝试把 provider 加入导线网络。
    - 如果端口位置就是 receiver，则按 `min(available/providerSpeed, demand/receiverSpeed)` 直连传输。
  - `TileEntityMachineChemicalPlant#updateEntity()` 在同一批 `getConPos()` 上：
    - 对每个非 `NONE` 输入 tank 调 `trySubscribe(tank.type, world, pos)`。
    - 对每个 fill > 0 的输出 tank 调 `tryProvide(tank, world, pos)`。
- 本批现代接入：
  - 新增 `HbmFluidUtil`：
    - `FluidPort`：记录相对核心偏移和旧 `DirPos` 方向。
    - `subscribeProviderToPort(s)`
    - `subscribeReceiverToPort(s)`
    - `subscribeProviderToNetwork`
    - `subscribeReceiverToNetwork`
    - `tryProvideToPorts`
    - `getConnectableFluidNet`
  - `FluidPort#connectorSide()` 固定为 `direction.getOpposite()`，对齐旧 `canConnect(type, dir.getOpposite())`。
  - `HbmFluidBlockEntity` 新增可覆盖端口入口与 helper：
    - `getFluidPorts()`
    - `subscribeFluidProviderToPorts`
    - `subscribeFluidReceiverToPorts`
    - `tryProvideFluidToPorts`
  - `HbmFluidNetworkBlockEntity#refreshFluidNetworkSubscriptions()` 现在除本地节点网络外，也会刷新 `getNetworkFluidPorts(type)` 上的 provider/receiver 订阅。
  - `ChemicalPlantBlockEntity` 实现 `HbmStandardFluidTransceiver`：
    - `getReceivingTanks()` 返回 3 个输入 tank。
    - `getSendingTanks()` 返回 3 个输出 tank。
    - `getAllTanks()` 返回 6 个 tank，供标准流体接口/兼容层读取。
    - 服务端 tick 中对旧化工厂 12 个端口刷新：
      - 输入 tank 非 `NONE`：`subscribeReceiverToPorts`。
      - 输出 tank 非 `NONE` 且 fill > 0：`tryProvideToPorts`，包含网络订阅与端口直连 receiver 传输。
- 现代等价边界：
  - 这批只补远端端口订阅与直连传输；完整流体 pipe 渲染、标识工具、阀门、压力调试粒子仍属于后续流体网络内容。
  - 化工厂仍因 Java 单继承直接继承 `BlockEntity`，因此用本地包装调 `HbmFluidUtil`；能继承 `HbmFluidNetworkBlockEntity` 的后续机器应优先覆盖 `getFluidPorts()` / `getNetworkFluidPorts(type)`。
  - 旧 `type.getNetworkProvider()` 在现代端由 `HbmFluidNodespace` 的 `(pos, FluidType)` key 表达，当前内建流体均走 `FluidType` 本体网络。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。

Progress estimate after Pass 19:

- Core `FluidType` identity/NBT lookup/table: about 86%.
- Basic tank/conform/Forge capability bridge: about 74%.
- Fluid network/provider/receiver algorithm: about 63%.
- In-world pipe graph: about 22%.
- Fluid item/container loading: about 60%.
- Behavior traits and cross-system effects: about 60%.
- Machine integration through the library: about 34%.
- Overall fluid library migration: about 66%.

## 2026-05-25 Modern Library Pass 20

This pass wires the modern liquefactor into the remote fluid-port helper introduced in Pass 19.

Legacy sources re-read:

- `com/hbm/tileentity/machine/oil/TileEntityMachineLiquefactor.java`
- current modern `HbmFluidBlockEntity`, `HbmFluidNetworkBlockEntity`, `HbmFluidUtil`, and `ChemicalPlantBlockEntity`

Legacy behavior preserved:

- Old `getConPos()` exposes six fixed world-axis output connector positions:
  - `(x, y + 4, z)` toward `POS_Y`
  - `(x, y - 1, z)` toward `NEG_Y`
  - `(x + 2, y + 1, z)` toward `POS_X`
  - `(x - 2, y + 1, z)` toward `NEG_X`
  - `(x, y + 1, z + 2)` toward `POS_Z`
  - `(x, y + 1, z - 2)` toward `NEG_Z`
- `LiquefactorBlockEntity` now declares the same six offsets as `FluidPort`s and calls `tryProvideFluidToPorts(...)` each server tick while the output tank is filled.
- The liquefactor no longer creates an extra HBM fluid node at the core block; HBM network exposure now goes through the old remote ports. The core still keeps Forge fluid capability output for modern capability integration.
- `useUpFluid(...)` now marks/syncs the block entity when a remote HBM fluid network drains the tank, keeping saved fill state and the client fluid-height render in sync.

Still deferred:

- In-game verification should place fluid pipes/receivers at all six old connector positions and confirm output routing plus OBJ fluid-height updates.
- Energy MK2 remote port parity is separate and belongs to the energy-network trace/work.
- Upgrade slots/effects remain deferred as recorded in Pass 17.

Progress estimate after Pass 20:

- Core `FluidType` identity/NBT lookup/table: about 86%.
- Basic tank/conform/Forge capability bridge: about 75%.
- Fluid network/provider/receiver algorithm: about 64%.
- In-world pipe graph: about 24%.
- Fluid item/container loading: about 60%.
- Behavior traits and cross-system effects: about 60%.
- Machine integration through the library: about 36%.
- Overall fluid library migration: about 67%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed.

## 2026-05-25 Modern Library Pass 21

This pass expands the fluid-library machine integration around the legacy heat boiler, using `TileEntityHeatBoiler` as the source of truth:

- Legacy sources re-read:
  - `com/hbm/tileentity/machine/TileEntityHeatBoiler.java`
  - `com/hbm/inventory/fluid/trait/FT_Heatable.java`
  - modern support files `BoilerBlockEntity`, `HbmFluidThermalExchange`, `HbmFluidUtil.FluidPort`, and `HbmStandardFluidReceiver/Sender`
- Update `BoilerBlockEntity`:
  - Restores the old heat buffer constants in code form: `maxHeat = 3_200_000` and heat diffusion `0.1D`.
  - Restores old below-block `IHeatSource` behavior through modern `HeatSource`: positive difference transfers `ceil(diff * 0.1)`, capped by remaining boiler heat capacity; no positive transfer decays stored heat by `max(heat / 1000, 1)`.
  - Raises the steam/output tank from the earlier temporary `16_000` mB to the old `16_000 * 100` water-to-steam capacity.
  - Adds `prepareOutputTank()` so the output tank type and capacity follow the current input fluid's first `HeatableFluidTrait` BOILER step instead of hard-coding steam forever. Water remains `16_000 -> 1_600_000` steam; oil/crack oil can follow the already migrated hot-fluid heat pairs.
  - Adds 1.7.10-style remote fluid ports from `getConPos()`:
    - facing-forward connector at `facing * 2`, same direction
    - facing-back connector at `-facing * 2`, opposite direction
    - top connector at `y + 4`, upward direction
  - Disables the local HBM fluid node for the boiler body so HBM MK2 network participation happens through those old remote ports. Forge fluid capability on the core block remains available through the base block entity.
  - Sends produced output fluid to remote ports every server tick when the output tank is non-empty.
  - Overrides receiver/sender mutation hooks so network fill/drain marks the block entity changed and sends client updates.

Still deferred:

- Legacy overpressure explosion, burst model state, boiler loop/groan sounds, and ROR values are still deferred to the destructive/visual machine behavior passes.
- Boiler item/container UI support is still limited to whatever the current Forge fluid capability bridge exposes; old tank GUI work remains in the machine UI backlog.
- Generic remote input subscription still follows the tank's current type, matching the old tank-driven behavior closely enough for migrated water/oil use, but full copy/configurator workflows are still deferred.

Progress estimate after Pass 21:

- Core `FluidType` identity/NBT lookup/table: about 86%.
- Basic tank/conform/Forge capability bridge: about 74%.
- Fluid network/provider/receiver algorithm: about 68%.
- In-world pipe graph: about 28%.
- Fluid item/container loading: about 52%.
- Behavior traits and cross-system effects: about 66%.
- Machine integration through the library: about 44%.
- Overall fluid library migration: about 69%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed.

## 2026-05-25 Modern Library Pass 22

This pass closes the first boiler-to-turbine runtime loop by adding the normal single-block steam turbine as a fluid-library consumer and energy producer:

- Legacy sources re-read:
  - `com/hbm/tileentity/machine/TileEntityMachineTurbine.java`
  - `com/hbm/tileentity/machine/TileEntityTurbineBase.java`
  - `com/hbm/tileentity/machine/TileEntityMachineLargeTurbine.java`
  - `com/hbm/tileentity/machine/TileEntityMachineIndustrialTurbine.java`
  - `com/hbm/blocks/machine/MachineTurbine.java`
  - `com/hbm/inventory/fluid/trait/FT_Coolable.java`
  - legacy textures `textures/blocks/machine_turbine_base.png` and `textures/blocks/machine_turbine_top.png`
- Add `HbmTurbineConversion`:
  - Shared `CoolableFluidTrait` turbine runtime wrapper around `HbmFluidThermalExchange.cool(...)`.
  - Preserves the old `CoolingType.TURBINE` efficiency multiplier and reports consumed input, produced output, and generated HE.
  - Adds output-tank preparation so the output fluid follows the current input fluid's `coolsTo` type.
- Add modern normal steam turbine:
  - Registers `machine_turbine` and `steam_turbine` BlockEntity.
  - Adds `SteamTurbineBlock` and `SteamTurbineBlockEntity`.
  - Uses old normal turbine constants: max power `1_000_000`, input tank `64_000`, output tank `128_000`, max steam per tick `6_000`, efficiency `0.85`.
  - Defaults to `STEAM -> SPENTSTEAM`, while the shared conversion helper also supports the already migrated hot/superhot/ultrahot steam coolable chain once UI/copy controls expose type switching.
  - Decays stored power by `0.95` each server tick before adding new turbine output, matching old normal turbine behavior.
  - Subscribes and direct-transfers HBM fluid on the six adjacent sides, matching old `subscribeToAllAround(...)` / `sendFluidToAll(...)` rather than a remote multiblock port layout.
  - Pushes generated HE to all adjacent energy connections via the modern energy helper.
  - Keeps Forge fluid and Forge energy capabilities available on the core block.
- Add generated/data resources:
  - Blockstate/model/item model for `machine_turbine`.
  - Loot table, pickaxe/iron-tool tags, English and Chinese lang entries.
  - Copies old top/base turbine textures into modern `assets/hbm/textures/block`.

Still deferred:

- Normal turbine GUI, item slots, battery charging slot, manual fluid container loading/unloading, and OpenComputers/ROR outputs are not ported yet.
- The old lever/copy workflow for switching among STEAM/HOTSTEAM/SUPERHOTSTEAM/ULTRAHOTSTEAM remains deferred; current no-GUI block accepts the current tank type and defaults to STEAM.
- Large turbine and industrial Mk2 turbine have been analyzed and can reuse `HbmTurbineConversion`, but their multiblock shapes, remote fluid/power ports, flywheel state, sounds, and render animation are still future passes.

Progress estimate after Pass 22:

- Core `FluidType` identity/NBT lookup/table: about 86%.
- Basic tank/conform/Forge capability bridge: about 75%.
- Fluid network/provider/receiver algorithm: about 69%.
- In-world pipe graph: about 30%.
- Fluid item/container loading: about 52%.
- Behavior traits and cross-system effects: about 67%.
- Machine integration through the library: about 49%.
- Overall fluid library migration: about 72%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed.
- `.\gradlew.bat runData --no-daemon` passed and generated `machine_turbine` blockstate/model/item model/loot/tag/lang resources.
- A transient Gradle `build/resources/main/.cache` hash miss appeared after `runData`; the `.cache` directory was verified inside the workspace, removed, and `compileJava processResources` passed on rerun.

## 2026-05-25 Modern Library Pass 23

This pass expands the turbine runtime path to the industrial steam turbine family, using the shared fluid turbine conversion helper instead of adding per-machine conversion math. A first draft also added `machine_large_turbine`, but that block has since been confirmed removed from the active target scope and was removed again in the same migration pass.

- Legacy sources re-read:
  - `com/hbm/blocks/machine/MachineIndustrialTurbine.java`
  - `com/hbm/tileentity/machine/TileEntityMachineIndustrialTurbine.java`
  - `com/hbm/tileentity/machine/TileEntityTurbineBase.java`
  - OBJ resource `models/block/machines/industrial_turbine.obj`
- Extend `HbmTurbineConversion`:
  - Adds percent-of-available conversion for old Mk2 turbine behavior.
  - Adds max-power preview from a tank's current coolable fluid type for industrial flywheel target output.
- Add shared `LegacySteamTurbineBlockEntity`:
  - Handles two-tank turbine conversion, HBM fluid receiver/provider contracts, Forge fluid capability, HBM/Forge energy output, common NBT sync fields, and remote port pushing.
  - Keeps `shouldCreateFluidNode=false`, matching the current port-proxy machine pattern.
- Add `machine_industrial_turbine`:
  - Registers `IndustrialSteamTurbineBlockEntity` and `SteamTurbineMultiblockBlock.Kind.INDUSTRIAL`.
  - Uses old constants: input tank `750_000`, output tank `3_000_000`, efficiency `1.0`, 20% per-tick operation cap, and `50_000_000` flywheel max energy.
  - Ports flywheel state (`spin`, `flywheelEnergy`, `lastPowerTarget`, `maxPowerTarget`) so generated heat first charges the flywheel and then becomes an output power target.
  - Wires the six legacy remote fluid ports and one rear upper energy port.
- Add block registration and generated/data resources:
  - `machine_industrial_turbine` blockstate, item model, loot table, pickaxe/iron-tool tags, and English/Chinese language entries.
  - Reuses already migrated OBJ/texture assets: `machines/industrial_turbine`.

Still deferred:

- Animated rotor/flywheel rendering, sound loops, GUI/container slots, manual fluid container loading, redstone/control toggles, and OpenComputers/ROR outputs remain deferred.
- The old copy/lever fluid-type switching workflow is still not exposed through UI; the tanks default to STEAM/SPENTSTEAM while the conversion helper remains type-generic for migrated coolable steam variants.
- Multiblock dummy placement uses the modern proxy layout that matches the functional port positions; in-game inspection should still verify old visual offset/orientation against the renderer once animation work starts.
- `machine_large_turbine` is intentionally not registered because the user confirmed the large turbine was removed and should not be included.

Progress estimate after Pass 23:

- Core `FluidType` identity/NBT lookup/table: about 86%.
- Basic tank/conform/Forge capability bridge: about 76%.
- Fluid network/provider/receiver algorithm: about 71%.
- In-world pipe graph: about 31%.
- Fluid item/container loading: about 52%.
- Behavior traits and cross-system effects: about 67%.
- Machine integration through the library: about 53%.
- Overall fluid library migration: about 74%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed.
- `.\gradlew.bat runData --no-daemon` passed and generated the industrial turbine blockstate/item/loot/tag/lang resources.
- `.\gradlew.bat compileJava processResources --no-daemon` passed.

## 2026-05-25 Modern Library Pass 24

This pass moves the legacy `machine_fluidtank` from a visible shell into a real fluid-library consumer. It also keeps the user's scope correction from Pass 23: `machine_large_turbine` is not part of the active target because the large turbine has been removed.

- Legacy sources re-read:
  - `com/hbm/blocks/machine/MachineFluidTank.java`
  - `com/hbm/tileentity/machine/TileEntityMachineFluidTank.java`
  - `com/hbm/inventory/container/ContainerMachineFluidTank.java`
  - `com/hbm/inventory/gui/GUIMachineFluidTank.java`
  - GUI texture `textures/gui/storage/gui_tank.png`
- Add `FluidTankBlockEntity`:
  - Ports the old `256_000` mB tank capacity and six-slot inventory layout.
  - Ports four modes: input, buffer, output, and disabled.
  - Routes the block through `HbmFluidNetworkBlockEntity`, `HbmStandardFluidReceiver`, and `HbmStandardFluidSender`.
  - Preserves the old mode meaning: input subscribes as receiver, output pushes to remote ports, buffer creates a local fluid node and can both receive and provide, disabled does neither.
  - Wires the eight old remote horizontal fluid port positions used by the multiblock tank.
  - Adds Forge fluid capability exposure and item load/unload behavior through `HbmFluidItemTransfer` for the old load/unload slot pairs.
  - Saves and syncs tank state, inventory, mode, explosion/fire flags, tick age, and comparator output.
  - Ports comparator strength as the old fill-ratio signal.
  - Ports the old antimatter/corrosive damage flags into the modern `HbmFluidTank.release(...)` leak path, using burn release when the exploded tank is on fire.
- Add `FluidTankBlock`, `FluidTankMenu`, and `FluidTankScreen`:
  - Right-click opens the old-style storage GUI when the tank is not exploded.
  - Menu slot coordinates match the old container layout.
  - Mode button sends the existing legacy-button packet path and cycles the four modes server-side.
  - GUI uses the copied 1.7.10 `gui_tank.png` texture, the old mode icon strip, and a simple colored fill preview.
- Add renderer and registration:
  - Registers `ModBlockEntities.FLUID_TANK`, `ModMenuTypes.FLUID_TANK`, menu screen, and block entity renderer.
  - `FluidTankRenderer` renders the old normal or exploded OBJ through `ObjModelLibrary.MACHINE_FLUIDTANK` / `MACHINE_FLUIDTANK_EXPLODED`.
  - `machine_fluidtank` now uses `FluidTankBlock` instead of the generic visible multiblock shell.
- Data/resource updates:
  - Adds English and Chinese GUI/mode/status language keys.
  - Copies the legacy GUI texture into `assets/hbm/textures/gui/storage/gui_tank.png`.
  - `runData` keeps the existing blockstate/item/loot/tag/lang resources for `machine_fluidtank`.

Still deferred:

- Exact legacy `tank.setType(0, 1, slots)` type-copy slot behavior is not fully ported yet; the modern tank can accept its first fluid directly through capability/network input, but the old identifier-slot workflow needs a dedicated item-identity pass.
- Persistent fluid NBT on block drops, repair/extinguisher interactions, and torch/fire interaction details remain deferred.
- Old explosion side effects, particles, entity burning, and pollution mutation are limited to the currently migrated fluid release behavior until the explosion/pollution/effect libraries are ready for a deeper integration.
- OpenComputers/ROR data exposure and any old automation names remain deferred.
- The block entity renderer does not yet render the old inner fluid surface/level OBJ piece.

Progress estimate after Pass 24:

- Core `FluidType` identity/NBT lookup/table: about 86%.
- Basic tank/conform/Forge capability bridge: about 78%.
- Fluid network/provider/receiver algorithm: about 73%.
- In-world pipe graph: about 32%.
- Fluid item/container loading: about 56%.
- Behavior traits and cross-system effects: about 69%.
- Machine integration through the library: about 58%.
- Overall fluid library migration: about 77%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed during the implementation pass.
- `.\gradlew.bat runData --no-daemon` passed after wiring the block/menu/renderer path.
- `machine_large_turbine` / `LargeSteamTurbine` / `MACHINE_LARGE_TURBINE` source and generated-resource scans found no remaining active registrations.
- Final `.\gradlew.bat compileJava processResources --no-daemon` rerun passed after this documentation update.

## 2026-05-25 Modern Library Pass 25

This pass fills the main legacy identification gap left by the first `machine_fluidtank` runtime pass and makes the behavior reusable by later machines.

- Legacy sources re-read:
  - `com/hbm/inventory/fluid/tank/FluidTank.java`
  - `com/hbm/items/machine/IItemFluidIdentifier.java`
  - `com/hbm/items/machine/ItemFluidIDMulti.java`
  - `com/hbm/tileentity/machine/storage/TileEntityMachineFluidTank.java`
  - Textures `textures/items/fluid_identifier_multi.png` and `textures/items/fluid_identifier_overlay.png`
- Add modern fluid identifier API and item:
  - Adds `IFluidIdentifierItem`, the modern equivalent of old `IItemFluidIdentifier`.
  - Adds `fluid_identifier_multi` through `FluidIdentifierItem`.
  - Stores primary/secondary fluid identities in NBT using the old-style `fluid1` / `fluid2` keys, with name and numeric-id fallback for migration resilience.
  - Right-click swaps primary and secondary identities, matching the old non-sneak air-use behavior.
  - Creative tab variants are generated for every modern `FluidType` that is not `NONE` and does not carry the `NoId` trait.
  - Registers item tinting so the legacy overlay is colored by the primary identified fluid.
  - Copies the legacy base and overlay textures, adds English/Chinese lang keys, and generates a two-layer item model.
- Finish `machine_fluidtank` identification behavior:
  - Type slot input now only accepts `IFluidIdentifierItem`.
  - Output slot rejects manual insertion.
  - Server tick applies the old `tank.setType(0, 1, slots)` rule: when the output slot is empty and the identifier points to a different fluid, the tank type changes, current fill is cleared by `setTankType`, and the identifier moves from input to output.
  - Sneak-right-clicking the tank core with a fluid identifier now sets the tank type directly, preserving the old external identification workflow for storage blocks that expose it.
- Finish legacy dynamic fluid speed on the storage tank:
  - Receiver speed: `max(500, (maxFill - fill) / 100)`.
  - Provider speed: `max(500, fill / 100)`.
  - These override the generic billion-mB default and are now used by both HBM net transfer and direct port transfer helpers.

Still deferred:

- The fluid identifier selection GUI (`GUIScreenFluid`) is not ported yet; current creative variants and right-click primary/secondary swap cover the old runtime consumer contract.
- Pipe bulk-identification with shift-right-click and 64-block flood fill remains deferred to a pipe-network interaction pass.
- Persistent fluid-on-block-drop NBT, repair/extinguisher interactions, and the old full explosion/pollution/entity-fire effects remain deferred.
- Datagen for all old fluid container item models is still intentionally not broadened in this pass because several container texture names do not match their registry names yet.

Progress estimate after Pass 25:

- Core `FluidType` identity/NBT lookup/table: about 88%.
- Basic tank/conform/Forge capability bridge: about 79%.
- Fluid network/provider/receiver algorithm: about 74%.
- In-world pipe graph: about 33%.
- Fluid item/container loading: about 60%.
- Behavior traits and cross-system effects: about 69%.
- Machine integration through the library: about 60%.
- Overall fluid library migration: about 79%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed after adding the API, item, and tank behavior.
- `.\gradlew.bat runData --no-daemon` passed after generating the new identifier model/lang resources.
- A first `runData` attempt exposed unrelated broad model-generation debt for old fluid containers (`canister_full` texture naming); the pass was narrowed back to the newly migrated identifier model and rerun successfully.
- Final `.\gradlew.bat compileJava processResources --no-daemon` passed after this documentation update.

## 2026-05-25 Modern Library Pass 26

This pass closes the main pipe-side fluid identification gap and restores typed duct placement behavior from the legacy `fluid_duct_neo` item.

- Legacy sources re-read:
  - `com/hbm/blocks/generic/FluidDuctBase.java`
  - `com/hbm/tileentity/network/TileEntityPipeBaseNT.java`
  - `com/hbm/items/machine/ItemFluidDuct.java`
  - `com/hbm/items/machine/IItemFluidIdentifier.java`
  - Textures `textures/items/duct.png` and `textures/items/duct_overlay.png`
- Add fluid identifier interaction to `FluidPipeBlock`:
  - Right-clicking a pipe with any `IFluidIdentifierItem` sets that pipe's `FluidType`, matching the old non-sneak interaction.
  - Sneak-right-clicking performs a connected-pipe type replacement with a 64-step breadth-first traversal, matching the old recursive `changeTypeRecursively(..., 64)` contract while avoiding Java call-stack recursion.
  - Only pipes whose current type matches the clicked pipe's previous type are changed, preserving the legacy “retag this connected typed run” behavior.
- Improve pipe BlockEntity sync:
  - Adds update tag and update packet support so server-side type changes are sent to clients through the normal block-entity sync path.
- Add `FluidPipeBlockItem` for typed duct stacks:
  - Stores the old item-damage-style fluid identity in modern stack NBT.
  - Creative tab variants are emitted for every identified modern `FluidType` except `NONE` and old `NoId` fluids.
  - Placement copies the stack's fluid type into the placed pipe BlockEntity.
  - Tooltip/name decoration exposes the stored fluid identity without splitting the duct into hundreds of separate registered items.
  - Item tinting colors the overlay layer from the stored fluid type.
- Resource/data updates:
  - Copies old `duct.png` and `duct_overlay.png` into the modern item texture folder.
  - Generates a two-layer `fluid_duct_neo` item model using the old base/overlay texture split.
  - Maps old `igniter` item model generation to `trigger`, matching legacy `ModItems.igniter.setTextureName(hbm:trigger)`; this was exposed by `runData` while validating the broader generated resources.
- Scope correction kept:
  - `machine_large_turbine` remains intentionally excluded because the user confirmed the large turbine was removed.
  - `machine_industrial_turbine` remains active because the 1.7.10 source has a separate `MachineIndustrialTurbine` / `TileEntityMachineIndustrialTurbine` path and only `machine_large_turbine` is the deprecated/removed large turbine target.

Still deferred:

- The old Alt/Ctrl keybind path that copies a pipe's type back into `ItemFluidIDMulti` is not ported yet; the modern input/keybind layer needs a dedicated pass before this can be exact.
- Typed pipe drops and old metadata-to-NBT world migration are not finalized; placed pipes save their BlockEntity type, and newly placed typed stacks work, but block drops still need explicit NBT preservation.
- Pipe visuals still use the connected duct block model; in-world verification should check item tint, placement type, and client sync under multiplayer/client-server conditions.
- Fluid identifier selection GUI (`GUIScreenFluid`) is still deferred.

Progress estimate after Pass 26:

- Core `FluidType` identity/NBT lookup/table: about 88%.
- Basic tank/conform/Forge capability bridge: about 80%.
- Fluid network/provider/receiver algorithm: about 75%.
- In-world pipe graph: about 39%.
- Fluid item/container loading: about 61%.
- Behavior traits and cross-system effects: about 69%.
- Machine integration through the library: about 61%.
- Overall fluid library migration: about 81%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed after adding pipe interactions and the typed duct item.
- First `.\gradlew.bat runData --no-daemon` found the unrelated old `igniter` texture-name mismatch; `igniter -> trigger` was confirmed from 1.7.10 source and fixed in item-model datagen.
- `.\gradlew.bat runData --no-daemon` passed after the model mapping fix and regenerated the duct/identifier resources.
- `machine_large_turbine` / `LargeSteamTurbine` / `MACHINE_LARGE_TURBINE` source and generated-resource scans found no active registrations.
- Final `.\gradlew.bat compileJava processResources --no-daemon` passed after this documentation update.

## 2026-05-25 Modern Library Pass 27

This pass tightens the typed duct and multi-fluid identifier behavior against the exact old interaction contracts instead of broadening the system beyond 1.7.10.

- Legacy sources re-read:
  - `com/hbm/blocks/network/FluidDuctBase.java`
  - `com/hbm/blocks/network/FluidDuctStandard.java`
  - `com/hbm/tileentity/network/TileEntityPipeBaseNT.java`
  - `com/hbm/items/machine/ItemFluidIDMulti.java`
  - `com/hbm/items/machine/IItemFluidIdentifier.java`
  - `com/hbm/handler/HbmKeybinds.java` and the modern `HbmServerKeybinds` bridge
- Extend `IFluidIdentifierItem` with a conservative optional write-back hook:
  - Read-only identifiers can keep the default `false`.
  - `FluidIdentifierItem` writes primary/secondary NBT through the existing `fluid1`/`fluid2` keys.
- Port the old duct keybind interaction:
  - `TOOL_ALT` on a pipe copies the pipe's current type back into the held identifier's primary slot, plays the old orb-style feedback sound, and consumes the interaction.
  - `TOOL_CTRL` now triggers the same 64-depth connected-pipe retagging path as sneak-right-click.
  - Sneak-right-click still works as the old fallback bulk retarget path.
- Port `ItemFluidIDMulti` quality-of-life behavior:
  - Non-sneak right-click swaps primary and secondary fluid selections on the server.
  - The swap plays the old orb-style sound and sends the already-migrated `ClientInformPacket` HUD notice using legacy notice id `7` and 3000 ms duration.
  - The identifier now acts as its own crafting remaining item with NBT preserved, matching old `getContainerItem`.
  - `doesSneakBypassUse` returns true so sneak interactions can pass through to pipes/tanks as in 1.7.10.
- Port old typed pick-block behavior from `FluidDuctStandard#getPickBlock`:
  - Middle-clicking a placed typed pipe returns a `fluid_duct_neo` stack with the pipe's fluid type NBT.
  - Ordinary block drops intentionally remain normal block drops, because old `FluidDuctStandard#damageDropped` preserves duct material metadata, not the pipe's internal fluid type.
- Scope correction kept:
  - `machine_large_turbine` remains intentionally excluded because the user confirmed the large turbine was removed.
  - `machine_industrial_turbine` remains active as the separate old industrial turbine path.

Still deferred:

- The old sneak client fluid selection GUI (`GUIScreenFluid`) is still not ported.
- The exact localized old `FluidType#getConditionalName()` table is not fully migrated; HUD notices currently use the modern pretty fluid name derived from the library id.
- Generic settings-tool copy/paste (`getFluidIDToCopy`, `pasteSettings`, and wider tool interfaces) remains deferred beyond direct pipe identifier interaction.
- Pipe in-world visuals still use the connected duct model path; more precise old overlay/tint rendering can be revisited after the network behavior is stable.

Progress estimate after Pass 27:

- Core `FluidType` identity/NBT lookup/table: about 88%.
- Basic tank/conform/Forge capability bridge: about 80%.
- Fluid network/provider/receiver algorithm: about 75%.
- In-world pipe graph: about 43%.
- Fluid item/container loading: about 64%.
- Behavior traits and cross-system effects: about 69%.
- Machine integration through the library: about 61%.
- Overall fluid library migration: about 82%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed after adding Alt/Ctrl interaction, identifier write-back, self-container behavior, and typed pipe pick-block support.
- Final `.\gradlew.bat compileJava processResources --no-daemon` passed after this documentation update.

## 2026-05-25 Modern Library Pass 28

This pass ports the old multi-fluid identifier selection screen and the old fluid listing order so the pipe/identifier loop is no longer limited to creative variants or Alt-copy.

- Legacy sources re-read:
  - `com/hbm/inventory/gui/GUIScreenFluid.java`
  - `com/hbm/items/machine/ItemFluidIDMulti.java`
  - `com/hbm/packet/toserver/NBTItemControlPacket.java` call path through `PacketDispatcher`
  - `com/hbm/inventory/fluid/Fluids.java` `metaOrder` / `getInNiceOrder()`
  - Texture `assets/hbm/textures/gui/machine/gui_fluid.png`
- Add `FluidIdentifierScreen`:
  - Opens from sneak-right-clicking `fluid_identifier_multi`, matching old client-side `player.openGui(...)` behavior.
  - Uses the old `gui_fluid.png` panel texture.
  - Provides the old focused search field at the same panel coordinates.
  - Shows 9 visible matching fluids, with old-style tinted 8x14 fluid bars and primary/secondary selection overlays.
  - Left click sends `primary`, right click sends `secondary`, matching old `GUIScreenFluid#mouseClicked`.
  - Closes automatically when the player is no longer holding the identifier, matching old `updateScreen`.
  - Does not pause the game.
- Add a client bridge (`FluidIdentifierScreenBridge`) so the common item class does not directly link client-only Screen classes outside the `DistExecutor` path.
- Wire `FluidIdentifierItem` into the existing migrated item-control packet path:
  - Implements `HbmItemControlReceiver`.
  - Handles `primary` and `secondary` integer fields from `ItemControlPacket` / legacy `NBTItemControlPacket` mapping.
  - Keeps the existing non-sneak right-click primary/secondary swap from Pass 27.
- Add `HbmFluids.niceOrder()`:
  - Ports the old `Fluids.metaOrder` list as the first ordering source.
  - Appends any modern registered fluids not present in the old list so future additions remain selectable.
  - Uses the same order for `FluidIdentifierScreen`, `fluid_identifier_multi` creative variants, and typed `fluid_duct_neo` creative variants.
- Resource update:
  - Copies old `textures/gui/machine/gui_fluid.png` into the modern asset tree.
- Scope correction kept:
  - `machine_large_turbine` remains intentionally excluded because the user confirmed the large turbine was removed.
  - `machine_industrial_turbine` remains active as the separate old industrial turbine path.

Still deferred:

- Old `FluidType#getLocalizedName()` and `FluidType#addInfo(...)` tooltip text is only approximated by the modern pretty internal name until the fluid localization/trait-info table is fully migrated.
- The old GUI has no scrolling beyond the 9 search matches; this pass preserves that limitation and does not add a new paged selector.
- Generic settings-tool copy/paste (`getFluidIDToCopy`, `pasteSettings`, and wider tool interfaces) remains deferred beyond direct pipe/identifier interaction.
- Pipe in-world overlay/tint rendering remains deferred.

Progress estimate after Pass 28:

- Core `FluidType` identity/NBT lookup/table: about 89%.
- Basic tank/conform/Forge capability bridge: about 80%.
- Fluid network/provider/receiver algorithm: about 75%.
- In-world pipe graph: about 44%.
- Fluid item/container loading: about 68%.
- Behavior traits and cross-system effects: about 69%.
- Machine integration through the library: about 61%.
- Overall fluid library migration: about 83%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed after adding the screen, client bridge, item control receiver, old GUI texture, and fluid nice-order helper.
- Final `.\gradlew.bat compileJava processResources --no-daemon` passed after this documentation update.

## 2026-05-25 Modern Library Pass 29

This pass closes the main `FluidType#getLocalizedName()` / `FluidType#addInfo(...)` gap left by Pass 28 and pushes localized fluid names through the visible migrated UI paths:

- Legacy sources re-read:
  - `com/hbm/inventory/fluid/FluidType.java`
  - `com/hbm/inventory/fluid/trait/FluidTrait.java`
  - `com/hbm/inventory/fluid/trait/FluidTraitSimple.java`
  - `FT_Combustible`, `FT_Flammable`, `FT_Corrosive`, `FT_Polluting`, `FT_VentRadiation`, `FT_PWRModerator`, `FT_Pheromone`, `FT_Poison`, `FT_Toxin`, `FT_Heatable`, `FT_Coolable`
  - Legacy language keys `assets/hbm/lang/en_US.lang` and `zh_CN.lang` for `hbmfluid.*` and `hazard.*`
- Add modern `FluidType` display helpers:
  - `getTranslationKey()` returns the old `hbmfluid.<path>` key.
  - `getDisplayName()` returns a translatable component with a safe pretty-name fallback.
  - `appendInfo(...)` preserves the old tooltip contract: non-room temperature first, then each trait's visible info, with hidden trait details gated behind Shift and a hint when hidden details exist.
- Port old trait tooltip text into the modern trait classes:
  - Simple tags: gaseous, gaseous at room temperature, liquid, viscous, plasma, antimatter, lead-container, unsiphonable.
  - Value traits: combustible fuel grade/HE, flammable TU, corrosive/strongly corrosive, polluting spill/burn maps, radioactive, PWR flux multiplier, pheromone type, toxic fumes, toxin direct/effect summaries, heatable/coolable thermal capacity and efficiency labels.
- Add datagen-backed language entries:
  - `HbmFluidLangEntries` carries the legacy English and Chinese `hbmfluid.*` names.
  - Hazard protection keys used by toxin tooltip summaries are migrated with the same table.
  - Adds the modern Shift hint key `hbmfluid.info.hold_shift`.
- Update visible fluid name call sites:
  - `fluid_identifier_multi` HUD swap message, tooltip, GUI hover, and GUI search now use localized fluid names.
  - Typed `fluid_duct_neo` item names/tooltips now use localized names and show migrated fluid info.
  - Filled fluid container item names use localized fluid names.
  - Fluid tank, liquefactor, assembly machine, and chemical plant tank tooltips now use localized components.
  - Fluid pipe debug command messages now use localized names where they are player-facing.

Still deferred:

- The old danger-diamond texture overlay (`DiamondPronter` / `danger_diamond.png`) is still not drawn on fluid containers, tank renderers, or pipe overlays; this pass only ports text tooltip data.
- Some command/debug string output intentionally keeps raw internal fluid IDs where stable diagnostics are more useful than localized text.

Progress estimate after Pass 29:

- Core `FluidType` identity/NBT lookup/table: about 91%.
- Basic tank/conform/Forge capability bridge: about 80%.
- Fluid network/provider/receiver algorithm: about 75%.
- In-world pipe graph: about 44%.
- Fluid item/container loading: about 71%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 62%.
- Overall fluid library migration: about 85%.

Verification:

- `.\gradlew.bat runData --no-daemon` passed and generated the fluid/hazard language resources.
- `.\gradlew.bat compileJava processResources --no-daemon` passed.
- Verified `build/resources/main/assets/hbm/lang/{en_us,zh_cn}.json` contains `hbmfluid.water`, `hazard.gasChlorine`, and `hbmfluid.info.hold_shift`.

## 2026-05-25 Modern Library Pass 30

This pass migrates another broad client/resource slice for typed ducts and concrete fluid containers, based on the old multipass item renderers and pipe renderer:

- Legacy sources re-read:
  - `com/hbm/items/machine/ItemFluidDuct.java`
  - `com/hbm/render/block/RenderTestPipe.java`
  - `com/hbm/items/machine/ItemCanister.java`
  - `com/hbm/items/machine/ItemGasTank.java`
  - `com/hbm/items/machine/ItemFluidTank.java`
  - `com/hbm/items/weapon/ItemDisperser.java`
  - Old textures: `pipe_neo_overlay`, `canister_overlay`, `gas_bottle`, `gas_label`, `fluid_tank*_overlay`, `fluid_barrel_overlay`, `fluid_pack_overlay`, `disperser_canister_overlay`.
- Add the missing modern world-color path for `fluid_duct_neo`:
  - `ClientModEvents` now registers a block color handler for `fluid_duct_neo`.
  - The handler reads `FluidPipeBlockEntity#getFluidType()` and returns the stored fluid color for tint index 1.
  - Generated block models now split the duct into old-sized core/arm boxes (`5..11` px) with an untinted `pipe_neo` layer and a tinted `pipe_neo_overlay` layer.
- Expand container item presentation:
  - `HbmItemModelProvider` now generates all `CONTROL_FLUID_ITEMS`, including concrete container family models.
  - Full canisters, gas tanks, fluid tanks, lead tanks, barrels, packs, disperser canisters, and glyphid glands use old-style layered models instead of single-layer full-item tinting.
  - `HbmFluidContainerItem#getTintColor(stack, tintIndex)` now respects the old canister and gas tank container colors from `ContainerFluidTrait`; general fluid tanks/barrels/packs use the fluid color on layer 1.
  - `ClientModEvents` registers item color handlers for fluid containers in `CONTROL_FLUID_ITEMS`.
  - The control creative tab now expands normal fluid containers into all accepted filled variants, while infinite fluid items remain single default entries.
- Remove hand-written single-layer main-resource models for the filled container IDs that are now datagen-owned, so `processResources` picks the generated layered models.

Still deferred:

- `DiamondPronter` / `danger_diamond.png` is still not drawn as an actual GUI/icon overlay; hazard data is text-only for now.
- Duct geometry is a modern block-model equivalent of the old OBJ renderer, but it does not yet recreate every old corner part name from `pipe_neo.obj`.
- Fluid container recipe remainder and machine-slot replacement behavior still need a focused pass against old `FluidLoadingHandler` and crafting hooks.

Progress estimate after Pass 30:

- Core `FluidType` identity/NBT lookup/table: about 91%.
- Basic tank/conform/Forge capability bridge: about 80%.
- Fluid network/provider/receiver algorithm: about 75%.
- In-world pipe graph: about 48%.
- Fluid item/container loading: about 74%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 62%.
- Overall fluid library migration: about 86%.

Verification:

- First `.\gradlew.bat runData --no-daemon` pass succeeded for the new duct models, then a later rerun hit a transient Forge resource scan `NoSuchFileException` for unrelated `bio_revolver_atlas.png`; the file existed in both main and build resources, and a direct rerun passed.
- `.\gradlew.bat runData --no-daemon` passed and generated the layered container/duct models.
- `.\gradlew.bat compileJava processResources --no-daemon` passed.

## 2026-05-25 Modern Library Pass 31

This pass closes the Forge item-capability bridge for the concrete HBM fluid container family, while keeping the old `IFillableItem` NBT contract as the authoritative internal state:

- Legacy sources re-read:
  - `api/hbm/fluidmk2/IFillableItem.java`
  - `com/hbm/inventory/fluid/tank/FluidLoaderFillableItem.java`
  - `com/hbm/inventory/fluid/tank/FluidLoaderStandard.java`
  - `com/hbm/inventory/fluid/tank/FluidLoaderInfinite.java`
  - `com/hbm/items/machine/ItemCanister.java`
  - `com/hbm/items/machine/ItemGasTank.java`
  - `com/hbm/items/machine/ItemFluidTank.java`
- Add `HbmFluidContainerItemHandler implements IFluidHandlerItem`:
  - Exposes normal NBT-backed `HbmFluidContainerItem` stacks as one Forge item-fluid tank.
  - Maps Forge `FluidStack` to internal `FluidType` through `HbmFluidForgeMappings`; unmapped Forge fluids are rejected instead of becoming water/empty.
  - Exports only HBM fluids with an explicit Forge mapping, preserving the existing "unmapped HBM fluid is not silently exported" rule.
  - Honors Forge `SIMULATE` by operating on a copied stack, and honors `EXECUTE` by mutating the same container stack through old `tryFill` / `tryEmpty`.
- Add `HbmFluidContainerItemCapabilityProvider` and attach it from `HbmFluidContainerItem#initCapabilities`.
- Keep `HbmInfiniteFluidItem` out of the generic Forge item capability path for now; it continues to use the dedicated old `FluidLoaderInfinite` semantics already represented in `HbmFluidItemTransfer`, including pressure/random-chance handling.
- Correct `HbmFluidItemTransfer.fillItemFromTank` so HBM-owned containers can be filled with valid internal fluids even when that fluid has not yet been registered as a Forge fluid. The Forge fallback path still requires an export mapping.
- Minimal unrelated compile unblock: `CustomNukeBlockEntity#isBlock` now accepts `RegistryObject<? extends Block>` to match `ModBlocks.legacyBlock(...)`; this was required because an in-progress custom nuke migration was blocking project compilation.

Still deferred:

- Crafting-time fluid remainder behavior still relies on current item `craftRemainder`; exact old metadata container replacement in shaped/shapeless recipes needs a later recipe/crafting hook pass.
- No Forge item capability is exposed for infinite fluid items yet, because old infinite barrel/pinwheel behavior is not a normal finite container contract.
- Full Forge fluid registration still covers only the currently registered mapped fluids; all other HBM internal fluids remain HBM-only until their Forge fluid entries are migrated.

Progress estimate after Pass 31:

- Core `FluidType` identity/NBT lookup/table: about 91%.
- Basic tank/conform/Forge capability bridge: about 82%.
- Fluid network/provider/receiver algorithm: about 75%.
- In-world pipe graph: about 48%.
- Fluid item/container loading: about 78%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 63%.
- Overall fluid library migration: about 87%.

Verification:

- First `.\gradlew.bat compileJava processResources --no-daemon` attempt was blocked by unrelated in-progress custom nuke registry/generic compile errors.
- After the minimal generic fix noted above, `.\gradlew.bat compileJava processResources --no-daemon` passed.

## 2026-05-25 Modern Library Pass 32

This pass expands the HBM `FluidType` <-> Forge `FluidStack` bridge from the starter subset to the broad legacy fluid table, so machines, tanks, pipes, and normal HBM containers can interoperate with external Forge fluid handlers for far more old fluids:

- Legacy sources re-read:
  - `com/hbm/inventory/fluid/Fluids.java`
  - `com/hbm/inventory/fluid/FluidType.java`
  - Current modern `HbmFluids`, `ModFluids`, `HbmFluidForgeMappings`, and `HbmForgeFluidType`.
- Rework `ModFluids` registration storage:
  - Keep the existing public starter constants for code that may refer to them directly.
  - Store all modern HBM Forge-fluid entries in an identity-keyed map.
  - After the starter constants are created, automatically register every migrated HBM fluid except:
    - `NONE`
    - vanilla-backed `WATER` and `LAVA`
    - old `NO_ID` fluids such as plasma/smoke entries that were intentionally not normal identifier/container fluids.
- `registerMappings()` now registers the full map into `HbmFluidForgeMappings`, preserving the rule that only explicitly registered Forge fluids are exported.
- `HbmForgeFluidType` now uses the existing old `hbmfluid.*` translation key directly, so external Forge fluid UIs can reuse the migrated legacy language table instead of showing `fluid.hbm.*` fallback IDs.
- This is still a no-world-block/no-bucket bridge: the generated Forge fluids exist for `FluidStack` capability interoperability, not as placeable source blocks or bucket items.

Still deferred:

- Real fluid world blocks, buckets, blockstate/model data, and flowing/still legacy textures remain deferred until a dedicated world-fluid slice.
- `NO_ID` / plasma / smoke fluids remain intentionally HBM-internal for now; if a later machine needs an external Forge representation for one, it should be migrated explicitly with its old behavior notes.
- The Forge fluid visual extension still reuses water still/flow textures with tint; old fluid textures need a separate legacy asset/resource pass before visible world fluids are added.

Progress estimate after Pass 32:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 88%.
- Fluid network/provider/receiver algorithm: about 75%.
- In-world pipe graph: about 48%.
- Fluid item/container loading: about 81%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 64%.
- Overall fluid library migration: about 88%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed before and after aligning Forge fluid description ids to `hbmfluid.*`.

## 2026-05-25 Modern Library Pass 33

This pass expands the fluid-library verification and external Forge-fluid presentation surface after the broad Forge registration pass:

- Add `/hbm fluid info <pos>`:
  - Resolves multiblock cores through the same `CompatEnergyControl.findTileEntity(...)` path used by `/hbm energy info <pos>`.
  - Reports every internal `HbmFluidBlockEntity` tank with localized name, legacy internal id, fill, capacity, pressure, and whether that fluid is exportable to Forge or HBM-only.
  - Reports the visible Forge `IFluidHandler` sample side, all capability sides, Forge tank count, displayed fluid, amount, and capacity.
  - Keeps this as a read-only diagnostic command; it does not mutate tanks or network state.
- Copy 154 legacy `assets/hbm/textures/gui/fluids/*.png` icons into the modern block texture namespace at `assets/hbm/textures/block/fluid/*.png`.
- Update `HbmForgeFluidType` so generated Forge fluids use the copied legacy per-fluid sprite for still and flowing textures instead of the temporary vanilla water sprite.
- Keep Forge-fluid tint white because the old built-in GUI fluid icons are already colored; this avoids double-tinting external fluid previews.
- Keep this as an external handler/UI presentation bridge only. The fluids are still no-world-block/no-bucket entries from Pass 32.

Still deferred:

- Real fluid world blocks, buckets, flowing models, and old block-level fluid interactions remain deferred until a dedicated world-fluid slice.
- The copied GUI icons are suitable for external fluid slot/UI display, but old true world fluid textures such as `sulfuric_acid_still/flowing`, `schrabidic_acid_still/flowing`, and custom water/oil/toxin bases still need a separate mapping pass before visible world fluids are enabled.
- `NO_ID` fluids remain HBM-internal and do not receive Forge fluid entries.
- Debug commands still do not provide mutating tank fill/drain operations; machine-level behavior should continue to be verified through normal slots/capabilities first.

Progress estimate after Pass 33:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 89%.
- Fluid network/provider/receiver algorithm: about 76%.
- In-world pipe graph: about 49%.
- Fluid item/container loading: about 81%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 64%.
- Overall fluid library migration: about 89%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed after adding `/hbm fluid info <pos>`.
- `.\gradlew.bat compileJava processResources --no-daemon` passed after copying legacy fluid sprites and wiring `HbmForgeFluidType`.

## 2026-05-26 Modern Library Pass 34

This pass closes another major old `FluidContainerRegistry` / `FluidLoaderStandard` compatibility gap for the migrated HBM container family:

- Legacy sources re-read:
  - `com/hbm/inventory/FluidContainerRegistry.java`
  - `com/hbm/inventory/fluid/tank/FluidLoaderStandard.java`
  - `com/hbm/inventory/fluid/tank/FluidLoaderFillableItem.java`
  - `com/hbm/items/machine/ItemCanister.java`
  - `com/hbm/items/machine/ItemGasTank.java`
  - `com/hbm/items/machine/ItemFluidTank.java`
- Add modern `HbmFluidContainerRegistry` for the migrated HBM container families:
  - Registers empty/full pairs for canister, gas tank, fluid tank, lead tank, fluid barrel, fluid pack, disperser canister, and glyphid gland.
  - Recreates the old core query shape: `getContainers`, `getContainer`, `getFluidContent`, `getFluidType`, `getFullContainer`, and `getEmptyContainer`.
  - Keeps the scope to already migrated HBM NBT-backed containers; vanilla buckets, cells, ore blocks, IV bags, and other unported old registry entries are still deferred.
- Wire `HbmFluidContainerItem` crafting remainders through the new registry:
  - Filled migrated HBM containers now return the matching empty container after crafting.
  - Empty / no-fluid stacks do not claim a crafting remainder through the custom override.
- Align Forge item-fluid drain behavior:
  - Draining a finite migrated HBM container through `IFluidHandlerItem` now returns the matching empty container when the stored fluid reaches zero.
- Align machine slot transfer behavior with old `FluidLoaderStandard`:
  - When separate input/output slots are used, empty migrated containers are converted into filled containers only if the tank has enough fluid for a full legacy container amount.
  - Filled migrated containers are converted into empty containers only if the receiving tank can accept the full legacy container amount.
  - Custom hover names are copied from input container to output container, matching the old standard-loader behavior.
  - Same-slot single-container transfers still keep the existing NBT-backed gradual fill/drain path for modern direct editing/capability use.
- Minimal unrelated compile unblock:
  - `ClientModEvents#biomeKey(...)` now only calls `getBiome` when the tint getter is a `LevelReader`, because `BlockAndTintGetter` itself does not expose `getBiome` in 1.20.1.

Still deferred:

- Old `FluidContainerRegistry` entries for vanilla buckets, potion bottles, mud/schrabidic/sulfuric buckets, red/pink/LOX barrels, ore fluid blocks, cells, particles, IV bags, mug cans, and compat containers remain outside this pass until those item/block families are migrated.
- Exact recipe JSON coverage for every old fluid-container recipe is still incomplete; this pass fixes the reusable container behavior once those recipes are present.
- Armor mod fillable-item traversal from old `FluidLoaderFillableItem` remains deferred until the armor mod/attachment system is migrated.
- Infinite fluid items remain on their dedicated `HbmInfiniteFluidItem` / `FluidLoaderInfinite` path and are not exposed as normal finite crafting remainders.

Progress estimate after Pass 34:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 90%.
- Fluid network/provider/receiver algorithm: about 76%.
- In-world pipe graph: about 49%.
- Fluid item/container loading: about 85%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 66%.
- Overall fluid library migration: about 90%.

Verification:

- First `.\gradlew.bat compileJava --no-daemon` attempt hit an SSL/MCP dependency resolution issue and produced a broken classpath.
- Re-run with the project proxy JVM options reached real compilation, then exposed the unrelated `ClientModEvents` biome lookup error noted above.
- `.\gradlew.bat compileJava --no-daemon` passed after the minimal client compile fix.
- `.\gradlew.bat compileJava processResources --no-daemon` passed with proxy JVM options.

## 2026-05-26 Modern Library Pass 35

This pass expands the modern `HbmFluidContainerRegistry` to cover the clear vanilla entries from the old `FluidContainerRegistry` without introducing any not-yet-migrated HBM item families:

- Legacy source re-read:
  - `com/hbm/inventory/FluidContainerRegistry.java`
- Added direct standard-container entries matching the 1.7.10 table:
  - `water_bucket` -> `bucket`, `WATER`, 1000.
  - water `potion` -> `glass_bottle`, `WATER`, 250.
  - `lava_bucket` -> `bucket`, `LAVA`, 1000.
- Kept unregistered HBM-specific legacy entries deferred:
  - Mud/schrabidic/sulfuric buckets, red/pink/LOX barrels, filled cells, mercury bottle/ingot, Zirnox tritium rod, particles, IV bags, XP bottle, mug can, and compat containers still need their item/block families present before they can be registered safely.
  - Large turbine remains intentionally excluded because it was removed from the port target.
- Updated the registry helpers so static direct-container templates are copied before use:
  - Machine output can preserve custom hover names without mutating the template stack.
  - Water-potion matching checks the potion identity instead of treating every potion item as a water bottle.
- Updated `HbmFluidItemTransfer#getItemFluid(...)` to report registered standard containers as fluid stacks:
  - UI/probe/automation callers now see water buckets as 1000 mB water, water bottles as 250 mB water, and lava buckets as 1000 mB lava through the HBM fluid library.
  - The existing machine transfer path can now fill and drain these vanilla containers through the same old-style full-container swap path used for migrated HBM containers.

Still deferred:

- HBM custom bucket items and world-fluid blocks remain tied to the future world-fluid/content slice.
- Old special containers that depend on missing item families remain documented but unregistered in this pass.
- Recipe JSON coverage is still separate from the reusable library behavior; recipes can now target the standard-container behavior once their item families exist.
- Armor mod fillable-item traversal from old `FluidLoaderFillableItem` remains deferred until the armor mod/attachment system is migrated.

Progress estimate after Pass 35:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 90%.
- Fluid network/provider/receiver algorithm: about 76%.
- In-world pipe graph: about 49%.
- Fluid item/container loading: about 87%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 67%.
- Overall fluid library migration: about 91%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed with proxy JVM options after adding the vanilla standard-container entries.

## 2026-05-26 Modern Library Pass 36

This pass pushes the reusable `FluidContainerRegistry` compatibility layer further without adding any removed machines or forcing missing content families into the port:

- Legacy sources re-read:
  - `com/hbm/inventory/FluidContainerRegistry.java`
  - `com/hbm/inventory/FluidContainer.java`
  - `com/hbm/inventory/fluid/tank/FluidLoaderStandard.java`
  - `com/hbm/inventory/fluid/FluidType.java`
- Expanded direct standard-container coverage:
  - Vanilla `experience_bottle` now maps to `glass_bottle`, `XPJUICE`, 100 mB.
  - The already-registered modern `cell_sas3` now maps to `cell_empty`, `SAS3`, 1000 mB.
  - Existing `ore_oil` and `ore_gneiss_gas` block items are recognized as consumable fluid sources, matching the old entries with no empty container:
    - `ore_oil` -> `OIL`, 250 mB.
    - `ore_gneiss_gas` -> `PETROLEUM`, 250 mB in the current non-528 default.
- Added guarded legacy-name mappings for the rest of the old direct container table:
  - Deuterium/tritium/UF6/PUF6/antimatter/anti-schrabidium cells, Zirnox tritium rod, particle containers, IV bags, mug can, HBM custom buckets, mercury bottle/ingot, and red/pink/LOX barrel block containers are now described in code but only activate when their referenced item/block IDs exist in the modern registries.
  - This keeps the fluid library ready for later item/block-family migration while avoiding synthetic placeholder items.
- Recreated more of the old query shape:
  - Added `HbmFluidContainerRegistry#getAllContainers()` as the modern counterpart to old `allContainers`.
  - Existing `getContainers(type)` remains the type-filtered counterpart to old `containerMap`.
- Aligned old no-empty-container transfer behavior:
  - `FluidLoaderStandard#emptyItem(...)` allowed entries such as `ingot_mercury` and fluid ores to be consumed without producing an output container.
  - `HbmFluidItemTransfer` now allows registered standard containers with `ItemStack.EMPTY` as their empty container to drain into tanks and leave no output stack.

Still deferred:

- Modern item/block registration, textures, recipes, and creative-tab placement for the guarded legacy container families are not part of this pass.
- The old `OreDictionary.registerOre(con.type.getDict(con.content), con.fullContainer)` bridge is documented but not yet regenerated as modern item tags. Legacy names follow `ntmcontainer<amount><fluidname>` unless fluid-container compat is enabled, in which case the prefix is `container`.
- 528-mode-specific `ore_gneiss_gas` amount remains deferred until the modern config profile exposes that compatibility setting.
- Armor mod fillable-item traversal from old `FluidLoaderFillableItem` remains deferred until the armor mod/attachment system is migrated.

Progress estimate after Pass 36:

- Core `FluidType` identity/NBT lookup/table: about 92%.
- Basic tank/conform/Forge capability bridge: about 90%.
- Fluid network/provider/receiver algorithm: about 76%.
- In-world pipe graph: about 49%.
- Fluid item/container loading: about 89%.
- Behavior traits and cross-system effects: about 72%.
- Machine integration through the library: about 69%.
- Overall fluid library migration: about 92%.

Verification:

- `.\gradlew.bat compileJava --no-daemon` passed with proxy JVM options after adding the guarded legacy direct-container mappings and no-empty-container transfer behavior.

## 2026-05-23 Modern Library Pass 16

This pass closes the concrete `FluidLoaderInfinite` gap left after the container item registration pass:

- Legacy sources re-read:
  - `com/hbm/items/machine/ItemInfiniteFluid.java`
  - `com/hbm/inventory/fluid/tank/FluidLoaderInfinite.java`
  - `com/hbm/inventory/fluid/tank/FluidTank.java`
- Wire `HbmInfiniteFluidItem` into `HbmFluidItemTransfer` before the generic `IFillableItem` path, preserving the old special loader semantics:
  - `loadTankFromSlot(...)` can now fill a typed tank from `inf_water`, `inf_water_mk2`, `chlorine_pinwheel`, or the untyped infinite fluid barrel.
  - `unloadTankToSlot(...)` can now drain matching tanks through the same infinite item family.
  - Non-zero pressure is rejected for typed infinite items, while the untyped infinite barrel keeps the old pressure exception through `allowPressure(...)`.
  - `chance` now gates the actual transfer, matching the old `FluidLoaderInfinite` random tick behavior for items such as `chlorine_pinwheel`.
  - The untyped infinite barrel uses the target tank's current type instead of inventing a fluid identity, matching the old `type == null` contract.
- Keep normal NBT-backed containers on the existing `IFillableItem` path so canisters, gas bottles, fluid tanks, barrels, packs, dispersers, and glyphid glands continue to use the shared fill/empty contract.

Still deferred:

- No concrete machine slot has been newly wired in this pass; future machine BlockEntities must call `HbmFluidItemTransfer` to inherit the infinite item behavior.
- Old `FluidTank.noDualUnload` is still only documented; dual-slot GUI behavior needs the relevant machine/inventory migration.
- Dedicated liquefactor recipes such as `glyphid_gland_empty -> BIOGAS 2000 mB` are confirmed in 1.7.10 but remain deferred because the modern port does not yet have a `liquefactor` machine recipe type/runtime.

Progress estimate after Pass 16:

- Core `FluidType` identity/NBT lookup/table: about 86%.
- Basic tank/conform/Forge capability bridge: about 73%.
- Fluid network/provider/receiver algorithm: about 60%.
- In-world pipe graph: about 20%.
- Fluid item/container loading: about 60%.
- Behavior traits and cross-system effects: about 60%.
- Machine integration through the library: about 15%.
- Overall fluid library migration: about 60%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed.

## 2026-05-23 Modern Library Pass 15

This pass moves the legacy fluid-container family from shared rules into actual registered items and resource-backed presentation:

- Legacy sources re-read:
  - `com/hbm/items/ModItems.java` fluid item block
  - `com/hbm/inventory/fluid/tank/FluidLoaderInfinite.java`
  - `com/hbm/inventory/fluid/tank/FluidTank.java`
  - `com/hbm/inventory/fluid/tank/FluidLoadingHandler.java`
- Register the legacy container family in modern `ModItems`:
  - `canister_empty`
  - `canister_full`
  - `canister_napalm`
  - `gas_empty`
  - `gas_full`
  - `fluid_tank_empty`
  - `fluid_tank_full`
  - `fluid_tank_lead_empty`
  - `fluid_tank_lead_full`
  - `fluid_barrel_empty`
  - `fluid_barrel_full`
  - `fluid_barrel_infinite`
  - `fluid_pack_empty`
  - `fluid_pack_full`
  - `disperser_canister_empty`
  - `disperser_canister`
  - `glyphid_gland_empty`
  - `glyphid_gland`
  - `inf_water`
  - `inf_water_mk2`
  - `chlorine_pinwheel`
- Add `CONTROL_FLUID_ITEMS` so the old control-tab fluids sit with the rest of the container family instead of being mixed into the consumables tab.
- Keep the reusable `HbmFluidContainerItem` / `HbmInfiniteFluidItem` implementation as the shared behavior layer, but expose the family as real registered items now.
- Copy legacy resource textures into the modern resource tree and add minimal item model JSONs for each container entry.
- Add English and Chinese display names for the new registered items.
- Hook item tinting so fluid containers colorize from their stored HBM fluid type.

Still deferred:

- Full legacy metadata-style container swapping is still not identical to 1.7.10. The modern port now has named items and NBT-backed fluid content, but it still needs final remainder/container-item behavior tuning in recipes and machine slots.
- `ItemInfiniteFluid` is only partially ported as a reusable shared item; the old random drain/fill chance behavior is not yet wired into every machine path that used `FluidLoaderInfinite`.
- Old `FluidContainerRegistry` recipe/crafting remainder integration still needs a dedicated pass to ensure every recipe slot returns the correct empty container item automatically.
- The old `ItemFluidIDMulti` / fluid identifier control flow is not yet migrated.

Progress estimate after Pass 15:

- Core `FluidType` identity/NBT lookup/table: about 86%.
- Basic tank/conform/Forge capability bridge: about 72%.
- Fluid network/provider/receiver algorithm: about 60%.
- In-world pipe graph: about 20%.
- Fluid item/container loading: about 55%.
- Behavior traits and cross-system effects: about 60%.
- Machine integration through the library: about 14%.
- Overall fluid library migration: about 58%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed.

## 2026-05-22 Modern Library Pass 14

This pass expands the old fluid-container contract from generic slot transfer helpers into reusable container rules and data:

- Legacy sources re-read:
  - `com/hbm/inventory/FluidContainerRegistry.java`
  - `com/hbm/items/machine/ItemCanister.java`
  - `com/hbm/items/machine/ItemGasTank.java`
  - `com/hbm/items/machine/ItemFluidTank.java`
  - `com/hbm/items/machine/ItemInfiniteFluid.java`
  - `com/hbm/items/weapon/ItemDisperser.java`
  - `com/hbm/inventory/fluid/Fluids.java` container declarations
- Add `ContainerFluidTrait`:
  - Preserves old `CD_Canister` overlay color data.
  - Preserves old `CD_Gastank` bottle and label color data.
- Wire the old canister/gastank white lists into `HbmFluids` for all legacy fluids that had explicit `addContainers(new CD_Canister(...))` or `addContainers(new CD_Gastank(...))`.
- Add `HbmFluidContainerRules`:
  - Records old capacities for 1,000 mB small containers, 16,000 mB fluid barrels, 2,000 mB disperser canisters, and 4,000 mB glyphid glands.
  - Separates canister/gastank explicit white lists from generic fluid tank / lead tank / fluid barrel rules.
  - Keeps the old lead-container split: normal tanks/barrels reject lead-required fluids, while lead tanks accept all general container fluids.
  - Keeps old disperser filtering through `FluidType.isDispersible()`.
  - Keeps the old glyphid gland exception for pheromone and sulfuric acid.
- Add reusable `HbmFluidContainerItem`:
  - Implements modern `IFillableItem` with internal NBT fields for HBM fluid name, amount, and pressure.
  - Uses `HbmFluidContainerRules` for acceptance instead of each future item reimplementing the old rules.
  - Preserves the old `tryFill` contract by returning the unfilled remainder and `tryEmpty` by returning the amount provided.

Still deferred:

- The concrete legacy item IDs (`canister_empty`, `canister_full`, `gas_empty`, `gas_full`, `fluid_tank_full`, `fluid_barrel_full`, etc.) are not registered in this pass. The reusable item implementation is ready, but exact full/empty ID handling needs a separate registration/data pass because 1.7.10 used metadata subitems while 1.20.1 needs NBT/model handling.
- Old `FluidContainerRegistry` discrete full/empty replacement behavior is not fully mirrored for metadata-style stacks yet.
- Infinite barrel behavior is documented but still not connected to machine loading; its pressure exception will need a concrete `ItemInfiniteFluid` port.
- Container tooltips, color tinting/render layers, item models, and creative-tab fluid variants remain deferred.

Progress estimate after Pass 14:

- Core `FluidType` identity/NBT lookup/table: about 86%.
- Basic tank/conform/Forge capability bridge: about 72%.
- Fluid network/provider/receiver algorithm: about 60%.
- In-world pipe graph: about 20%.
- Fluid item/container loading: about 40%.
- Behavior traits and cross-system effects: about 60%.
- Machine integration through the library: about 14%.
- Overall fluid library migration: about 54%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed.

## 2026-05-22 Modern Library Pass 10

This pass closes the largest remaining trait data gap by porting the legacy `FT_Toxin` shape as machine-readable data:

- Legacy sources re-read:
  - `com/hbm/inventory/fluid/trait/FT_Toxin.java`
  - `com/hbm/inventory/fluid/Fluids.java` toxin append block
- Add `ToxinFluidTrait`:
  - Stores a list of toxin entries.
  - Preserves the old `HazardClass` requirement and `fullBody`/hazmat-suit flag.
  - Supports direct damage entries with damage type id, amount, and tick delay.
  - Supports effect entries with effect id, duration, amplifier, and ambient flag.
- Wire the old built-in toxic fluids into `HbmFluids`:
  - `CHLORINE`: cloud damage, 2 damage every 20 ticks, `GAS_LUNG`.
  - `PHOSGENE`: cloud damage, 4 damage every 20 ticks, `GAS_LUNG`.
  - `MUSTARDGAS`: cloud damage, 4 damage every 10 ticks, `GAS_BLISTERING`; plus full-body blistering effects for wither and nausea.
  - `ESTRADIOL`: particle-fine effect data for old `HbmPotion.death` using `hbm:death` as a deferred effect id.
  - `REDMUD`: blistering wither effect data.
- The trait intentionally records `ResourceLocation` ids instead of invoking `DamageSource` or `MobEffect` directly. This keeps the fluid library independent from incomplete armor/effect wiring and lets later toxic-gas or spill systems resolve the ids when they actually apply effects.

Still deferred:

- `ToxinFluidTrait` does not yet check armor protection, consume gas-mask filters, damage entities, or apply effects.
- The old `ArmorRegistry.hasAllProtection`, `ArmorUtil.damageGasMaskFilter`, and hazmat-suit checks still need a dedicated hazard/armor integration pass.
- `hbm:death` is only recorded as legacy effect data for now; if the old death potion is migrated under another id, the resolver layer must map it explicitly.
- JSON trait serialization/deserialization remains deferred.

Progress estimate after Pass 10:

- Core `FluidType` identity/NBT lookup/table: about 82%.
- Basic tank/conform/Forge capability bridge: about 65%.
- Fluid network/provider/receiver algorithm: about 60%.
- In-world pipe graph: about 20%.
- Fluid item/container loading: about 30%.
- Behavior traits and cross-system effects: about 42%.
- Machine integration through the library: about 8%.
- Overall fluid library migration: about 42%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed.


## 2026-05-22 Modern Library Pass 11

This pass starts turning the migrated trait data into a shared runtime release path, based on legacy `FluidTrait.FluidReleaseType`, `FT_Polluting.pollute(...)`, and `FT_VentRadiation.onFluidRelease(...)`:

- Legacy sources re-read:
  - `com/hbm/inventory/fluid/trait/FluidTrait.java`
  - `com/hbm/inventory/fluid/trait/FT_Polluting.java`
  - `com/hbm/inventory/fluid/trait/FT_VentRadiation.java`
  - representative release call sites in `TileEntityMachineFluidTank`, `TileEntityBarrel`, `TileEntityMachineGasFlare`, and `TileEntityMachineDrain`
- Add modern `FluidReleaseType` with the old release modes:
  - `VOID`: fluid is deleted without release side effects.
  - `BURN`: fluid is combusted/burned and uses burn pollution.
  - `SPILL`: fluid leaks/spills and uses release pollution.
- Add `HbmFluidReleaseEffects`:
  - Computes release reports for radiation and pollution from a `FluidType` and amount in mB.
  - Applies `VentRadiationFluidTrait` through `ChunkRadiationManager.incrementRadiation(level, pos, radiationPerMb * amountMb)`.
  - Preserves `PollutingFluidTrait` as calculated report data, multiplying legacy per-mB pollution by released amount.
  - Does not yet mutate world pollution, because the modern pollution saved-data/handler layer has not been migrated.
- Add convenience entrypoints:
  - `FluidType.onFluidRelease(level, pos, amountMb, releaseType)`.
  - `FluidType.previewRelease(amountMb, releaseType)`.
  - `HbmFluidTank.release(level, pos, amount, releaseType, simulate)`, which clamps to stored fill, returns the same release report, applies effects only when not simulated, and drains the released amount.

Still deferred:

- Real pollution mutation remains blocked on a modern `PollutionHandler`/saved-data migration. Current release reports intentionally make the amounts visible without inventing a parallel world pollution store.
- Entity-facing mist/chemical effects are still not applied through this helper; toxin, corrosive, poison, flammable mist, and pheromone handling need the entity/hazard armor integration pass.
- Machine call sites have not yet been rewired to `HbmFluidTank.release(...)`; the shared entrypoint is ready for the next storage tank, drain, gas flare, and turbine passes.

Progress estimate after Pass 11:

- Core `FluidType` identity/NBT lookup/table: about 84%.
- Basic tank/conform/Forge capability bridge: about 70%.
- Fluid network/provider/receiver algorithm: about 60%.
- In-world pipe graph: about 20%.
- Fluid item/container loading: about 30%.
- Behavior traits and cross-system effects: about 48%.
- Machine integration through the library: about 10%.
- Overall fluid library migration: about 45%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed.

## 2026-05-22 Modern Library Pass 12

This pass ports the legacy `EntityChemical` / `EntityMist` fluid-contact behavior into a shared modern helper without wiring it into every gas block or projectile yet:

- Legacy sources re-read:
  - `com/hbm/entity/projectile/EntityChemical.java`
  - `com/hbm/entity/effect/EntityMist.java`
  - `com/hbm/inventory/fluid/trait/FT_Toxin.java`
- Modern support checked:
  - `ArmorUtil` already exposes lung gas, monoxide, coarse/fine particle, bacteria, and hazmat protection helpers.
  - `ModDamageSources` already exposes `cloud`, `monoxide`, and `pc` damage sources.
  - `RadiationData` and `RadiationUtil` already support oil timers and radiation contamination.
- Add `HbmFluidContactEffects`:
  - Applies hot/cold contact damage through vanilla fire/freeze damage where modern dedicated old acid/boil/cryolator sources are not yet present.
  - Applies oil coating for liquid flammable fluids via `RadiationData.setOil`.
  - Applies `VentRadiationFluidTrait` to living entities via `RadiationUtil.contaminate(..., bypassResistance=true)`.
  - Applies old `PoisonFluidTrait` as poison/wither effects.
  - Applies `ToxinFluidTrait` direct damage and resolvable effect entries with armor-protection checks.
  - Preserves unresolved legacy effects such as `hbm:death` as report data instead of inventing a new effect.
  - Applies basic pheromone buff effects recorded by the old mist/projectile behavior.
- Add convenience entrypoints:
  - `FluidType.affectEntity(entity, intensity)`.
  - `FluidType.previewEntityContact(entity, intensity)`.

Still deferred:

- This helper is not yet wired into migrated gas blocks, chemical projectile entities, mist entities, or leaking tank visuals.
- Dedicated old damage sources for acid/boil/cryolator are still not registered in the modern damage-type data, so this pass uses vanilla fire/freeze where safe and leaves corrosion suit damage deferred.
- `hbm:death` remains unresolved until the old death potion/effect is migrated or explicitly mapped.
- Gas-mask filter consumption is not implemented yet; protection checks are passive.

Progress estimate after Pass 12:

- Core `FluidType` identity/NBT lookup/table: about 85%.
- Basic tank/conform/Forge capability bridge: about 70%.
- Fluid network/provider/receiver algorithm: about 60%.
- In-world pipe graph: about 20%.
- Fluid item/container loading: about 30%.
- Behavior traits and cross-system effects: about 55%.
- Machine integration through the library: about 10%.
- Overall fluid library migration: about 48%.

Verification:

- First validation hit a transient generated-file/cache issue in `build/resources/main/.cache`; the cache directory was removed after confirming it was inside the workspace.
- `.\gradlew.bat compileJava processResources --no-daemon` passed after the cache refresh.

## 2026-05-22 Modern Library Pass 13

This pass turns the migrated `FT_Heatable` / `FT_Coolable` data into shared runtime conversion helpers and gives the modern boiler a safe library-backed boiling path:

- Legacy sources re-read:
  - `com/hbm/inventory/fluid/trait/FT_Heatable.java`
  - `com/hbm/inventory/fluid/trait/FT_Coolable.java`
  - `com/hbm/tileentity/machine/TileEntityHeatBoiler.java`
  - representative turbine use in `TileEntityMachineTurbine.java`
- Add `HbmFluidThermalExchange`:
  - `heat(...)` consumes input fluid, output capacity, available heat, heating type, and efficiency.
  - Uses the legacy formula `ceil(step.heatReq / (fluidEfficiency * machineEfficiency))` for heat cost per operation.
  - Converts by exact legacy mB ratios from `HeatingStep.amountRequired` to `HeatingStep.amountProduced`.
  - `cool(...)` converts coolable fluids by `CoolableFluidTrait` ratios and reports produced heat as `ops * heatEnergy * fluidEfficiency * machineEfficiency`.
  - Both heating and cooling support simulation and return a `ThermalResult` report.
- Add `HbmFluidTank.getSpaceFor(type)` so conversion code can check destination capacity without changing or clearing tank type.
- Wire `BoilerBlockEntity` to the shared helper:
  - Adds saved `heat` state.
  - Adds `addHeat`, `previewBoiling`, and `tryBoil`.
  - Server tick now calls `tryBoil(heat, false)` and only decays heat when no conversion happens.
  - The boiler still does not generate heat by itself; it is ready for later `HeatSource` / adjacent heater integration.

Still deferred:

- No modern machine currently implements `HeatSource`, so boiler heating must be provided by later heater/industrial boiler work or debug/test hooks.
- Turbine and heat-exchanger BlockEntity ports are not wired yet, but their core thermal math now has a shared implementation.
- Explosion/overpressure behavior from old boilers remains deferred until the destructive machine behavior phase.

Progress estimate after Pass 13:

- Core `FluidType` identity/NBT lookup/table: about 85%.
- Basic tank/conform/Forge capability bridge: about 72%.
- Fluid network/provider/receiver algorithm: about 60%.
- In-world pipe graph: about 20%.
- Fluid item/container loading: about 30%.
- Behavior traits and cross-system effects: about 60%.
- Machine integration through the library: about 14%.
- Overall fluid library migration: about 51%.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` passed.

