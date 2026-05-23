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

