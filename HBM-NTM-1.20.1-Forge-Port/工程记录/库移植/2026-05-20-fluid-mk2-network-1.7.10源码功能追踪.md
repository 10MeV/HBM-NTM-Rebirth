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
