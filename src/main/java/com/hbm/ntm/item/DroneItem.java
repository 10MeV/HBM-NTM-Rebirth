package com.hbm.ntm.item;

import com.hbm.ntm.entity.item.DeliveryDroneEntity;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** The legacy metadata drone item, retained as one modern registry ID with explicit NBT variant state. */
public class DroneItem extends Item {
    private static final String TYPE = "droneType";
    public DroneItem(Properties properties) { super(properties); }

    @Override public InteractionResult useOn(UseOnContext context) {
        // ItemDrone#onItemUse returned false on every server path, including after it
        // spawned and consumed a patrol drone.  Only the legacy client top-face branch
        // returned true.  Preserve that asymmetric interaction contract instead of
        // turning a rejected face into a modern FAIL that cuts off later use handlers.
        if (context.getClickedFace() != net.minecraft.core.Direction.UP) return InteractionResult.PASS;
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        DroneType type = typeOf(stack);
        if (!level.isClientSide && type != DroneType.REQUEST) {
            BlockPos pos = context.getClickedPos().above();
            DeliveryDroneEntity drone = new DeliveryDroneEntity(ModEntityTypes.DELIVERY_DRONE.get(), level);
            drone.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
            drone.setExpress(type.express);
            drone.setChunkLoading(type.chunkLoading);
            level.addFreshEntity(drone);
        }
        // ItemDrone decremented every metadata variant after a top-face use, including the
        // request-only variant which deliberately produces no patrol entity.
        if (!level.isClientSide) stack.shrink(1);
        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    public static DroneType typeOf(ItemStack stack) {
        if (!stack.hasTag()) return DroneType.PATROL;
        return DroneType.byName(stack.getTag().getString(TYPE));
    }

    public static ItemStack withType(ItemStack stack, DroneType type) {
        stack.getOrCreateTag().putString(TYPE, type.serializedName);
        return stack;
    }
    @Override public Component getName(ItemStack stack) { return Component.translatable("item.hbm_ntm_rebirth.drone." + typeOf(stack).serializedName() + ".name"); }
    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String key = "item.hbm_ntm_rebirth.drone." + typeOf(stack).serializedName() + ".desc";
        // ItemDrone used Keyboard.KEY_LSHIFT, not a generic "either Shift" test.
        if (isLeftShiftDown()) {
            String text = Component.translatable(key).getString();
            if (!text.equals(key)) for (String line : text.split("\\$")) tooltip.add(Component.literal(line).withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.literal("Hold <")
                    .append(Component.literal("LSHIFT").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC))
                    .append(Component.literal("> to display more info"))
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    /** Keep client input classes behind the project's existing DistExecutor tooltip bridge. */
    private static boolean isLeftShiftDown() {
        return DistExecutor.unsafeRunForDist(
                () -> () -> com.hbm.ntm.client.ClientTooltipState.hasLeftShiftDown(),
                () -> () -> false);
    }

    /** 0..4 are deliberately stable model-predicate values, matching the old metadata order. */
    public static float modelVariant(ItemStack stack) { return (float) typeOf(stack).ordinal(); }

    public enum DroneType {
        PATROL("patrol", false, false),
        PATROL_CHUNKLOADING("patrol_chunkloading", false, true),
        PATROL_EXPRESS("patrol_express", true, false),
        PATROL_EXPRESS_CHUNKLOADING("patrol_express_chunkloading", true, true),
        REQUEST("request", false, false);
        private final String serializedName;
        private final boolean express;
        private final boolean chunkLoading;
        DroneType(String serializedName, boolean express, boolean chunkLoading) {
            this.serializedName = serializedName; this.express = express; this.chunkLoading = chunkLoading;
        }
        public static DroneType byName(String name) {
            for (DroneType type : values()) if (type.serializedName.equals(name)) return type;
            return PATROL;
        }
        public String serializedName() { return serializedName; }
    }
}
