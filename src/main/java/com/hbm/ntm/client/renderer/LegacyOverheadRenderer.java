package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyLineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class LegacyOverheadRenderer {
    public static final double LABEL_MAX_DISTANCE = 16.0D;
    public static final double LOOK_APPEND_THRESHOLD = 0.15D;
    public static final double THERMAL_MAX_DISTANCE_SQ = 4096.0D;
    public static final float TAG_SCALE = 0.016666668F * 1.6F;
    public static final int TAG_BACKGROUND = 0x40000000;
    public static final int THERMAL_SKIP_COLOR = -1;
    public static final int THERMAL_MONSTER_COLOR = 0xFF0000;
    public static final int THERMAL_PLAYER_COLOR = 0xFF00FF;
    public static final int THERMAL_LIVING_COLOR = 0x00FF00;
    public static final int THERMAL_ITEM_COLOR = 0xFFFF80;
    public static final int THERMAL_XP_ALT_COLOR = 0x80FF80;
    public static final int THERMAL_DEAD_COLOR = 0x000000;

    public static boolean expired(long expireAt, long now) {
        return expireAt > 0L && now > expireAt;
    }

    public static boolean tooFar(Vec3 observer, Vec3 center, double maxDistance) {
        return maxDistance > 0.0D && observer.distanceToSqr(center) > maxDistance * maxDistance;
    }

    public static Vec3 markerCenter(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    public static Vec3 labelPosition(Vec3 cameraToMarker) {
        double distance = cameraToMarker.length();
        if (distance <= 1.0E-4D) {
            return Vec3.ZERO;
        }
        return cameraToMarker.scale(Math.min(distance, LABEL_MAX_DISTANCE) / distance);
    }

    public static String markerLabel(String label, Vec3 look, Vec3 cameraToMarker) {
        String result = label == null ? "" : label;
        double distance = cameraToMarker.length();
        if (distance <= 1.0E-4D) {
            return result;
        }

        Vec3 markerDirection = cameraToMarker.normalize();
        double lookDelta = Math.abs(look.x - markerDirection.x)
                + Math.abs(look.y - markerDirection.y)
                + Math.abs(look.z - markerDirection.z);
        if (lookDelta < LOOK_APPEND_THRESHOLD) {
            result += (!result.isEmpty() ? " " : "") + ((int) distance) + "m";
        }
        return result;
    }

    public static void markerBox(VertexConsumer consumer, PoseStack.Pose pose, BlockPos pos, int color) {
        markerBox(consumer, pose, pos, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D, color, 255);
    }

    public static void markerBox(VertexConsumer consumer, PoseStack.Pose pose, BlockPos pos,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, int alpha) {
        LegacyLineRenderer.boxPositionColor(consumer, pose,
                pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ,
                color & 0xFFFFFF, alpha);
    }

    public static boolean shouldRenderThermalEntity(Entity entity, Entity observer) {
        return entity != observer && observer.distanceToSqr(entity) <= THERMAL_MAX_DISTANCE_SQ;
    }

    public static int thermalEntityColor(Entity entity, int observerTicks) {
        int color;
        if (entity instanceof Monster) {
            color = THERMAL_MONSTER_COLOR;
        } else if (entity instanceof Player) {
            color = THERMAL_PLAYER_COLOR;
        } else if (entity instanceof LivingEntity) {
            color = THERMAL_LIVING_COLOR;
        } else if (entity instanceof ItemEntity) {
            color = THERMAL_ITEM_COLOR;
        } else if (entity instanceof ExperienceOrb) {
            color = observerTicks % 10 < 5 ? THERMAL_ITEM_COLOR : THERMAL_XP_ALT_COLOR;
        } else {
            return THERMAL_SKIP_COLOR;
        }
        if (entity instanceof LivingEntity living && living.getHealth() <= 0.0F) {
            return THERMAL_DEAD_COLOR;
        }
        return color;
    }

    public static void thermalEntityBoxes(VertexConsumer consumer, PoseStack.Pose pose,
            Iterable<? extends Entity> entities, Entity observer, Vec3 origin) {
        for (Entity entity : entities) {
            if (!shouldRenderThermalEntity(entity, observer)) {
                continue;
            }
            int color = thermalEntityColor(entity, observer.tickCount);
            if (color != THERMAL_SKIP_COLOR) {
                thermalEntityBox(consumer, pose, entity.getBoundingBox(), origin, color, 255);
            }
        }
    }

    public static void thermalEntityBox(VertexConsumer consumer, PoseStack.Pose pose, AABB bounds, Vec3 origin,
            int color, int alpha) {
        LegacyLineRenderer.boxPositionColor(consumer, pose,
                bounds.minX - origin.x, bounds.minY - origin.y, bounds.minZ - origin.z,
                bounds.maxX - origin.x, bounds.maxY - origin.y, bounds.maxZ - origin.z,
                color & 0xFFFFFF, alpha);
    }

    public static void legacyDualPassLabel(Font font, MultiBufferSource buffer, PoseStack poseStack,
            Quaternionf cameraRotation, Vec3 position, String label, int color) {
        legacyDualPassLabel(font, buffer, poseStack, cameraRotation, position, label, color, color, TAG_BACKGROUND);
    }

    public static void legacyDualPassLabel(Font font, MultiBufferSource buffer, PoseStack poseStack,
            Quaternionf cameraRotation, Vec3 position, String label, int color, int seeThroughColor, int backgroundColor) {
        if (label == null || label.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(position.x, position.y, position.z);
        poseStack.mulPose(new Quaternionf(cameraRotation));
        float scale = -TAG_SCALE;
        poseStack.scale(scale, scale, -scale);

        float x = -font.width(label) * 0.5F;
        int opaqueColor = 0xFF000000 | (color & 0xFFFFFF);
        int opaqueSeeThroughColor = 0xFF000000 | (seeThroughColor & 0xFFFFFF);
        font.drawInBatch(label, x, 0.0F, opaqueSeeThroughColor, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH, backgroundColor, LightTexture.FULL_BRIGHT);
        font.drawInBatch(label, x, 0.0F, opaqueColor, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private LegacyOverheadRenderer() {
    }
}
