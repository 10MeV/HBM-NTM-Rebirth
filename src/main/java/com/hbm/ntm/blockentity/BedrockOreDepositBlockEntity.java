package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BedrockOreDepositBlockEntity extends BlockEntity {
    private static final String TAG_RESOURCE = "resource";
    private static final String TAG_REQUIRED_FLUID = "required_fluid";
    private static final String TAG_REQUIRED_FLUID_AMOUNT = "required_fluid_amount";
    private static final String TAG_TIER = "tier";
    private static final String TAG_COLOR = "color";
    private static final String TAG_SHAPE = "shape";

    private ItemStack resource = new ItemStack(ModItems.BEDROCK_ORE_BASE.get());
    private FluidType requiredFluid = HbmFluids.NONE;
    private int requiredFluidAmount;
    private int tier = 1;
    private int color = 0xD78A16;
    private int shape;

    public BedrockOreDepositBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BEDROCK_ORE_DEPOSIT.get(), pos, state);
    }

    public void configure(ItemStack resource, @Nullable FluidType requiredFluid, int requiredFluidAmount, int tier,
            int color, int shape) {
        this.resource = resource.copy();
        this.requiredFluid = requiredFluid == null ? HbmFluids.NONE : requiredFluid;
        this.requiredFluidAmount = Math.max(0, requiredFluidAmount);
        this.tier = Math.max(1, tier);
        this.color = color;
        this.shape = Math.floorMod(shape, 10);
        setChanged();
    }

    public ItemStack getResource() {
        return resource.copy();
    }

    public FluidType getRequiredFluid() {
        return requiredFluid;
    }

    public int getRequiredFluidAmount() {
        return requiredFluidAmount;
    }

    public int getTier() {
        return tier;
    }

    public int getColor() {
        return color;
    }

    public int getShape() {
        return shape;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!resource.isEmpty()) {
            tag.put(TAG_RESOURCE, resource.save(new CompoundTag()));
        }
        if (requiredFluid != HbmFluids.NONE && requiredFluidAmount > 0) {
            tag.putString(TAG_REQUIRED_FLUID, requiredFluid.getName());
            tag.putInt(TAG_REQUIRED_FLUID_AMOUNT, requiredFluidAmount);
        }
        tag.putInt(TAG_TIER, tier);
        tag.putInt(TAG_COLOR, color);
        tag.putInt(TAG_SHAPE, shape);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_RESOURCE, Tag.TAG_COMPOUND)) {
            resource = ItemStack.of(tag.getCompound(TAG_RESOURCE));
        }
        if (resource.isEmpty()) {
            resource = new ItemStack(ModItems.BEDROCK_ORE_BASE.get());
        }
        requiredFluid = tag.contains(TAG_REQUIRED_FLUID, Tag.TAG_STRING)
                ? HbmFluidJsonUtil.readFluidReference(tag.getString(TAG_REQUIRED_FLUID))
                : HbmFluids.NONE;
        requiredFluidAmount = tag.getInt(TAG_REQUIRED_FLUID_AMOUNT);
        tier = Math.max(1, tag.getInt(TAG_TIER));
        color = tag.contains(TAG_COLOR) ? tag.getInt(TAG_COLOR) : 0xD78A16;
        shape = Math.floorMod(tag.getInt(TAG_SHAPE), 10);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
