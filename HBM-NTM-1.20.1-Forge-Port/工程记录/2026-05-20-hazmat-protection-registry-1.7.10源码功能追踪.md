# Hazmat 防护注册 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 防护装备注册与危险抵抗接口。
- 该库与 radiation、hazard、gas mask、防具系统共同工作。

## 1.7.10 源文件

- `src/main/java/com/hbm/handler/HazmatRegistry.java`
- `src/main/java/api/hbm/item/IGasMask.java`
- `src/main/java/api/hbm/entity/IRadiationImmune.java`
- `src/main/java/com/hbm/items/armor`
- `src/main/java/com/hbm/hazard`

## 旧版契约

- Hazmat registry 记录不同护甲套装或单件对危险类型的防护。
- `IGasMask` 表示可过滤空气污染/毒气的头部装备或改装件。
- `IRadiationImmune` 由天然免疫实体实现。
- 防护逻辑被 hazard、radiation、污染、毒气等系统调用。
- 旧版护甲改装也可提供防护，因此与 `ArmorModHandler` 有交叉。

## 迁移计划

- 先建立统一防护查询接口：实体、装备、装备改装、状态效果都能参与。
- 防毒、辐射、防热、防石棉等不要写散在各事件中。
- 与 armor module 文档联动记录 mod slot 对防护的贡献。

## 验证清单

- 完整防护服套装能阻挡对应危险。
- 防毒面具可单独生效。
- 免疫实体不受背包/环境危险影响。
