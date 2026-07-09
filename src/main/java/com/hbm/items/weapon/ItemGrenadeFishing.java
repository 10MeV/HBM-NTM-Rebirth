package com.hbm.items.weapon;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class ItemGrenadeFishing extends ItemGenericGrenade {
    public ItemGrenadeFishing(int fuse) {
        super(fuse);
    }

    public ItemGrenadeFishing(int fuse, Item.Properties properties) {
        super(fuse, properties);
    }

    @Override
    public void explode(Entity grenade, LivingEntity thrower, Level level, double x, double y, double z) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.explode(null, x, y + 0.25D, z, 3.0F, false, Level.ExplosionInteraction.NONE);

        int iX = (int) Math.floor(x);
        int iY = (int) Math.floor(y);
        int iZ = (int) Math.floor(z);

        for (int i = 0; i < 15; i++) {
            int rX = iX + serverLevel.random.nextInt(15) - 7;
            int rY = iY + serverLevel.random.nextInt(15) - 7;
            int rZ = iZ + serverLevel.random.nextInt(15) - 7;
            BlockPos pos = new BlockPos(rX, rY, rZ);

            if (serverLevel.getBlockState(pos).getBlock() instanceof LiquidBlock
                    && serverLevel.getFluidState(pos).is(FluidTags.WATER)) {
                ItemStack loot = getRandomLoot(serverLevel, grenade, thrower, Vec3.atCenterOf(pos));
                if (!loot.isEmpty()) {
                    ItemEntity item = new ItemEntity(serverLevel, rX + 0.5D, rY + 0.5D, rZ + 0.5D, loot.copy());
                    item.setDeltaMovement(0.0D, 1.0D, 0.0D);
                    serverLevel.addFreshEntity(item);
                }
            }
        }
    }

    public static ItemStack getRandomLoot(ServerLevel level, Entity grenade, LivingEntity thrower, Vec3 origin) {
        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, origin)
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                .withLuck(0.0F);
        if (grenade != null) {
            builder.withOptionalParameter(LootContextParams.THIS_ENTITY, grenade);
        }
        if (thrower != null) {
            builder.withOptionalParameter(LootContextParams.KILLER_ENTITY, thrower);
        }
        LootParams params = builder.create(LootContextParamSets.FISHING);
        LootTable table = level.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING_FISH);
        List<ItemStack> loot = table.getRandomItems(params);
        return loot.stream().filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
    }

    @Override
    public int getMaxTimer() {
        return 60;
    }

    @Override
    public double getBounceMod() {
        return 0.5D;
    }
}
