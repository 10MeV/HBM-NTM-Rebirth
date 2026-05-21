package com.hbm.ntm.client.anim;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class LegacyBusAnimationLoader {
    private static final Gson GSON = new Gson();

    public static Map<String, LegacyBusAnimation> load(ResourceLocation file) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(file);
        if (resource.isEmpty()) {
            return Map.of();
        }

        try (Reader reader = resource.get().openAsReader()) {
            return load(GSON.fromJson(reader, JsonObject.class));
        } catch (IOException | RuntimeException ex) {
            return Map.of();
        }
    }

    public static Map<String, LegacyBusAnimation> load(JsonObject json) {
        Map<String, LegacyBusAnimation> animations = new LinkedHashMap<>();
        Map<String, double[]> offsets = loadVec3Map(json.getAsJsonObject("offset"));
        Map<String, double[]> rotModes = loadRotModes(json.has("rotmode") ? json.getAsJsonObject("rotmode") : null);

        JsonObject animRoot = json.getAsJsonObject("anim");
        if (animRoot == null) {
            return animations;
        }

        for (Map.Entry<String, JsonElement> root : animRoot.entrySet()) {
            LegacyBusAnimation animation = new LegacyBusAnimation();
            JsonObject entryObject = root.getValue().getAsJsonObject();
            for (Map.Entry<String, JsonElement> model : entryObject.entrySet()) {
                String modelName = model.getKey();
                double[] offset = offsets.getOrDefault(modelName, new double[3]);
                double[] rotMode = rotModes.getOrDefault(modelName, new double[] { 0.0D, 1.0D, 2.0D });
                animation.addBus(modelName, loadSequence(model.getValue().getAsJsonObject(), offset, rotMode));
            }
            animations.put(root.getKey(), animation);
        }

        return animations;
    }

    private static Map<String, double[]> loadVec3Map(JsonObject root) {
        Map<String, double[]> values = new LinkedHashMap<>();
        if (root == null) {
            return values;
        }
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            JsonArray array = entry.getValue().getAsJsonArray();
            values.put(entry.getKey(), new double[] {
                    array.get(0).getAsDouble(),
                    array.get(1).getAsDouble(),
                    array.get(2).getAsDouble()
            });
        }
        return values;
    }

    private static Map<String, double[]> loadRotModes(JsonObject root) {
        Map<String, double[]> values = new LinkedHashMap<>();
        if (root == null) {
            return values;
        }
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String mode = entry.getValue().getAsString();
            values.put(entry.getKey(), new double[] {
                    getRot(mode.charAt(2)),
                    getRot(mode.charAt(0)),
                    getRot(mode.charAt(1))
            });
        }
        return values;
    }

    private static double getRot(char value) {
        return switch (value) {
            case 'X' -> 0.0D;
            case 'Y' -> 1.0D;
            case 'Z' -> 2.0D;
            default -> 0.0D;
        };
    }

    private static LegacyBusAnimationSequence loadSequence(JsonObject json, double[] offset, double[] rotMode) {
        LegacyBusAnimationSequence sequence = new LegacyBusAnimationSequence();
        if (json.has("location")) {
            JsonObject location = json.getAsJsonObject("location");
            if (location.has("x")) {
                addToSequence(sequence, LegacyBusAnimationSequence.Dimension.TX, location.getAsJsonArray("x"));
            }
            if (location.has("y")) {
                addToSequence(sequence, LegacyBusAnimationSequence.Dimension.TY, location.getAsJsonArray("y"));
            }
            if (location.has("z")) {
                addToSequence(sequence, LegacyBusAnimationSequence.Dimension.TZ, location.getAsJsonArray("z"));
            }
        }
        if (json.has("rotation_euler")) {
            JsonObject rotation = json.getAsJsonObject("rotation_euler");
            if (rotation.has("x")) {
                addToSequence(sequence, LegacyBusAnimationSequence.Dimension.RX, rotation.getAsJsonArray("x"));
            }
            if (rotation.has("y")) {
                addToSequence(sequence, LegacyBusAnimationSequence.Dimension.RY, rotation.getAsJsonArray("y"));
            }
            if (rotation.has("z")) {
                addToSequence(sequence, LegacyBusAnimationSequence.Dimension.RZ, rotation.getAsJsonArray("z"));
            }
        }
        if (json.has("scale")) {
            JsonObject scale = json.getAsJsonObject("scale");
            if (scale.has("x")) {
                addToSequence(sequence, LegacyBusAnimationSequence.Dimension.SX, scale.getAsJsonArray("x"));
            }
            if (scale.has("y")) {
                addToSequence(sequence, LegacyBusAnimationSequence.Dimension.SY, scale.getAsJsonArray("y"));
            }
            if (scale.has("z")) {
                addToSequence(sequence, LegacyBusAnimationSequence.Dimension.SZ, scale.getAsJsonArray("z"));
            }
        }
        sequence.offset = offset;
        sequence.rotMode = rotMode;
        return sequence;
    }

    private static void addToSequence(LegacyBusAnimationSequence sequence, LegacyBusAnimationSequence.Dimension dimension, JsonArray array) {
        LegacyBusAnimationKeyframe.IType previousInterpolation = null;
        for (JsonElement element : array) {
            LegacyBusAnimationKeyframe keyframe = loadKeyframe(element, previousInterpolation);
            previousInterpolation = keyframe.interpolationType;
            sequence.addKeyframe(dimension, keyframe);
        }
    }

    private static LegacyBusAnimationKeyframe loadKeyframe(JsonElement element, LegacyBusAnimationKeyframe.IType previousInterpolation) {
        JsonArray array = element.getAsJsonArray();
        double value = array.get(0).getAsDouble();
        int duration = array.get(1).getAsInt();
        LegacyBusAnimationKeyframe.IType interpolation = array.size() >= 3
                ? LegacyBusAnimationKeyframe.IType.valueOf(array.get(2).getAsString())
                : LegacyBusAnimationKeyframe.IType.LINEAR;
        LegacyBusAnimationKeyframe.EType easing = array.size() >= 4
                ? LegacyBusAnimationKeyframe.EType.valueOf(array.get(3).getAsString())
                : LegacyBusAnimationKeyframe.EType.AUTO;

        LegacyBusAnimationKeyframe keyframe = new LegacyBusAnimationKeyframe(value, duration, interpolation, easing);
        int i = 4;
        if (previousInterpolation == LegacyBusAnimationKeyframe.IType.BEZIER && array.size() >= i + 3) {
            keyframe.leftX = array.get(i++).getAsDouble();
            keyframe.leftY = array.get(i++).getAsDouble();
            keyframe.leftType = LegacyBusAnimationKeyframe.HType.valueOf(array.get(i++).getAsString());
        }
        if (interpolation == LegacyBusAnimationKeyframe.IType.BEZIER && array.size() >= i + 3) {
            keyframe.rightX = array.get(i++).getAsDouble();
            keyframe.rightY = array.get(i++).getAsDouble();
            keyframe.rightType = LegacyBusAnimationKeyframe.HType.valueOf(array.get(i++).getAsString());
        }
        if (interpolation == LegacyBusAnimationKeyframe.IType.ELASTIC && array.size() >= i + 2) {
            keyframe.amplitude = array.get(i++).getAsDouble();
            keyframe.period = array.get(i).getAsDouble();
        } else if (interpolation == LegacyBusAnimationKeyframe.IType.BACK && array.size() > i) {
            keyframe.back = array.get(i).getAsDouble();
        }
        return keyframe;
    }

    private LegacyBusAnimationLoader() {
    }
}
