package com.hbm.ntm.client.anim;

import java.util.ArrayList;
import java.util.List;

public class LegacyBusAnimationSequence {
    public enum Dimension {
        TX,
        TY,
        TZ,
        RX,
        RY,
        RZ,
        SX,
        SY,
        SZ
    }

    private final List<List<LegacyBusAnimationKeyframe>> transformKeyframes = new ArrayList<>(9);
    public double[] offset = new double[3];
    public double[] rotMode = new double[] { 0.0D, 1.0D, 2.0D };

    public LegacyBusAnimationSequence() {
        for (int i = 0; i < 9; i++) {
            transformKeyframes.add(new ArrayList<>());
        }
    }

    public LegacyBusAnimationSequence addKeyframe(Dimension dimension, LegacyBusAnimationKeyframe keyframe) {
        transformKeyframes.get(dimension.ordinal()).add(keyframe);
        return this;
    }

    public LegacyBusAnimationSequence addKeyframe(Dimension dimension, double value, int duration) {
        return addKeyframe(dimension, new LegacyBusAnimationKeyframe(value, duration));
    }

    public LegacyBusAnimationSequence setPos(double x, double y, double z) {
        return addPos(x, y, z, 0, LegacyBusAnimationKeyframe.IType.LINEAR);
    }

    public LegacyBusAnimationSequence addPos(double x, double y, double z, int duration) {
        return addPos(x, y, z, duration, LegacyBusAnimationKeyframe.IType.LINEAR);
    }

    public LegacyBusAnimationSequence addPos(double x, double y, double z, int duration, LegacyBusAnimationKeyframe.IType type) {
        addKeyframe(Dimension.TX, new LegacyBusAnimationKeyframe(x, duration, type));
        addKeyframe(Dimension.TY, new LegacyBusAnimationKeyframe(y, duration, type));
        addKeyframe(Dimension.TZ, new LegacyBusAnimationKeyframe(z, duration, type));
        return this;
    }

    public LegacyBusAnimationSequence addRot(double x, double y, double z, int duration) {
        addKeyframe(Dimension.RX, new LegacyBusAnimationKeyframe(x, duration));
        addKeyframe(Dimension.RY, new LegacyBusAnimationKeyframe(y, duration));
        addKeyframe(Dimension.RZ, new LegacyBusAnimationKeyframe(z, duration));
        return this;
    }

    public LegacyBusAnimationSequence hold(int duration) {
        addKeyframe(Dimension.TX, new LegacyBusAnimationKeyframe(getLast(Dimension.TX), duration));
        addKeyframe(Dimension.TY, new LegacyBusAnimationKeyframe(getLast(Dimension.TY), duration));
        addKeyframe(Dimension.TZ, new LegacyBusAnimationKeyframe(getLast(Dimension.TZ), duration));
        return this;
    }

    public LegacyBusAnimationSequence holdUntil(int end) {
        return hold(end - getTotalTime());
    }

    public LegacyBusAnimationSequence multiplyTime(double mult) {
        for (Dimension dim : Dimension.values()) {
            for (LegacyBusAnimationKeyframe keyframe : transformKeyframes.get(dim.ordinal())) {
                keyframe.duration = (int) (keyframe.originalDuration * mult);
            }
        }
        return this;
    }

    public double[] getTransformation(int millis) {
        double[] transform = LegacyBusAnimationTransforms.identity();
        for (int i = 0; i < 9; i++) {
            List<LegacyBusAnimationKeyframe> keyframes = transformKeyframes.get(i);
            LegacyBusAnimationKeyframe currentFrame = null;
            LegacyBusAnimationKeyframe previousFrame = null;
            int startTime = 0;
            int endTime = 0;
            for (LegacyBusAnimationKeyframe keyframe : keyframes) {
                startTime = endTime;
                endTime += keyframe.duration;
                previousFrame = currentFrame;
                currentFrame = keyframe;
                if (millis < endTime) {
                    break;
                }
            }

            if (currentFrame == null) {
                continue;
            }
            if (millis >= endTime || currentFrame.duration == 0) {
                transform[i] = currentFrame.value;
                continue;
            }
            if (previousFrame != null && previousFrame.interpolationType == LegacyBusAnimationKeyframe.IType.CONSTANT) {
                transform[i] = previousFrame.value;
                continue;
            }
            transform[i] = currentFrame.interpolate(startTime, millis, previousFrame);
        }

        transform[9] = offset[0];
        transform[10] = offset[1];
        transform[11] = offset[2];
        transform[12] = rotMode[0];
        transform[13] = rotMode[1];
        transform[14] = rotMode[2];
        return transform;
    }

    public int getTotalTime() {
        int highestTime = 0;
        for (List<LegacyBusAnimationKeyframe> keyframes : transformKeyframes) {
            int time = 0;
            for (LegacyBusAnimationKeyframe frame : keyframes) {
                time += frame.duration;
            }
            highestTime = Math.max(time, highestTime);
        }
        return highestTime;
    }

    private double getLast(Dimension dim) {
        LegacyBusAnimationKeyframe frame = getLastFrame(dim);
        return frame == null ? 0.0D : frame.value;
    }

    private LegacyBusAnimationKeyframe getLastFrame(Dimension dim) {
        List<LegacyBusAnimationKeyframe> keyframes = transformKeyframes.get(dim.ordinal());
        return keyframes.isEmpty() ? null : keyframes.get(keyframes.size() - 1);
    }
}
