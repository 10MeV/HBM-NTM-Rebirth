package com.hbm.ntm.item;

import com.hbm.ntm.entity.train.CargoTramEntity;
import com.hbm.ntm.entity.train.CargoTramTrailerEntity;
import com.hbm.ntm.entity.train.LegacyRailCarEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.rail.HbmRail;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.Function;

/** Modern NBT-variant carrier for the old metadata {@code ItemTrain}. */
public final class LegacyTrainItem extends Item {
    public static final String VARIANT_TAG = "hbmLegacyVariant";

    public LegacyTrainItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static TrainType getType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getInt(VARIANT_TAG) == TrainType.CARGO_TRAM_TRAILER.ordinal()
                ? TrainType.CARGO_TRAM_TRAILER : TrainType.CARGO_TRAM;
    }

    public static ItemStack createStack(LegacyTrainItem item, TrainType type) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt(VARIANT_TAG, type.ordinal());
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockPos railPos = MultiblockHelper.resolveCorePos(level, clicked);
        BlockState railState = MultiblockHelper.resolveCoreState(level, clicked);
        TrainType type = getType(context.getItemInHand());
        if (!(railState.getBlock() instanceof HbmRail rail) || rail.getGauge(level, railPos) != type.gauge) {
            return InteractionResult.PASS;
        }
        LegacyRailCarEntity train = type.factory.apply(level);
        Vec3 hit = context.getClickLocation();
        train.setPos(hit.x, hit.y, hit.z);
        Player player = context.getPlayer();
        train.setYRot(player == null ? 0.0F : player.getYRot());
        BlockPos anchor = train.getCurrentAnchorPos();
        Vec3 core = train.getRelPosAlongRail(anchor, 0.0D,
                new HbmRail.MoveContext(HbmRail.RailCheckType.CORE, 0.0D));
        if (core == null) {
            return InteractionResult.PASS;
        }
        train.setPos(core);
        Vec3 front = train.getRelPosAlongRail(anchor, train.getLengthSpan(), new HbmRail.MoveContext(
                HbmRail.RailCheckType.FRONT, train.getCollisionSpan() - train.getLengthSpan()));
        Vec3 back = train.getRelPosAlongRail(anchor, -train.getLengthSpan(), new HbmRail.MoveContext(
                HbmRail.RailCheckType.BACK, train.getCollisionSpan() - train.getLengthSpan()));
        if (front == null || back == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            train.snapRailPose(front, back);
            level.addFreshEntity(train);
        }
        context.getItemInHand().shrink(1); // legacy ItemTrain consumed even in creative mode
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getType(stack) == TrainType.CARGO_TRAM
                ? "item.train.cargo_tram.name" : "item.train.cargo_tram_trailer.name");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        TrainType type = getType(stack);
        if (type.engine != null) tooltip.add(line("Engine", type.engine));
        tooltip.add(line("Gauge", type.gauge == HbmRail.TrackGauge.STANDARD ? "Standard Gauge" : "Narrow Gauge"));
        if (type.maxSpeed != null) tooltip.add(line("Max Speed", type.maxSpeed));
        if (type.acceleration != null) tooltip.add(line("Acceleration", type.acceleration));
        if (type.engineBrake != null) tooltip.add(line("Engine Brake Threshold", type.engineBrake));
        if (type.parkingBrake != null) tooltip.add(line("Parking Brake", type.parkingBrake));
    }

    private static Component line(String key, String value) {
        return Component.literal(key + ": ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(value).withStyle(ChatFormatting.RESET));
    }

    public enum TrainType {
        CARGO_TRAM(CargoTramEntity::new, "Electric", HbmRail.TrackGauge.STANDARD, "10m/s", "0.2m/s²", "<1m/s", "Yes"),
        CARGO_TRAM_TRAILER(CargoTramTrailerEntity::new, null, HbmRail.TrackGauge.STANDARD, "Yes", null, null, "No");

        private final Function<Level, LegacyRailCarEntity> factory;
        private final String engine;
        private final HbmRail.TrackGauge gauge;
        private final String maxSpeed;
        private final String acceleration;
        private final String engineBrake;
        private final String parkingBrake;

        TrainType(Function<Level, LegacyRailCarEntity> factory, String engine, HbmRail.TrackGauge gauge,
                String maxSpeed, String acceleration, String engineBrake, String parkingBrake) {
            this.factory = factory;
            this.engine = engine;
            this.gauge = gauge;
            this.maxSpeed = maxSpeed;
            this.acceleration = acceleration;
            this.engineBrake = engineBrake;
            this.parkingBrake = parkingBrake;
        }
    }
}
