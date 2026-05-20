# 玩家与生物扩展属性 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 `IExtendedEntityProperties` 风格的玩家/生物持久数据。
- 该库与辐射、污染、磁铁、能力、同步 packet、HUD 状态等系统交叉。

## 1.7.10 源文件

- `src/main/java/com/hbm/extprop/HbmPlayerProps.java`
- `src/main/java/com/hbm/extprop/HbmLivingProps.java`
- `src/main/java/com/hbm/packet/toclient/ExtPropPacket.java`
- `src/main/java/com/hbm/handler/HbmKeybindsServer.java`
- 相关事件处理位于 `src/main/java/com/hbm/main/EventHandler*` 与 `handler`

## 旧版契约

- 旧版使用实体扩展属性保存附加状态。
- 玩家属性通常包含：
  - 辐射/污染相关数值。
  - 磁铁开关或装备辅助状态。
  - 客户端 HUD 需要同步的数据。
- 生物属性通常包含：
  - 辐射、污染或特殊效果附加数据。
  - 与实体伤害/危险系统联动。
- `ExtPropPacket` 负责向客户端同步部分属性。
- 玩家死亡、克隆、维度切换、登录时需要处理数据复制和同步。

## 迁移计划

- 现代端使用 capability 或 attachment 风格数据承载。
- 区分持久数据、临时 tick 状态、仅客户端显示数据。
- 同步包应只发送客户端需要的字段。
- 旧 NBT key 需要在辐射和玩家状态迁移时逐项核对。

## 验证清单

- 玩家死亡后应保留的属性正确复制。
- 登录和维度切换后客户端状态同步。
- 生物卸载/重载后持久数据不丢。
- 客户端不能伪造服务端属性。
