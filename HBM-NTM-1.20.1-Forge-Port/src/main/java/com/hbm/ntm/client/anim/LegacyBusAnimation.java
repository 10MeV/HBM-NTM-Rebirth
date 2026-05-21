package com.hbm.ntm.client.anim;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LegacyBusAnimation {
    private final Map<String, LegacyBusAnimationSequence> animationBuses = new LinkedHashMap<>();
    private int totalTime;

    public LegacyBusAnimation addBus(String name, LegacyBusAnimationSequence bus) {
        animationBuses.put(name, bus);
        totalTime = Math.max(totalTime, bus.getTotalTime());
        return this;
    }

    public void updateTime() {
        totalTime = 0;
        for (LegacyBusAnimationSequence sequence : animationBuses.values()) {
            totalTime = Math.max(totalTime, sequence.getTotalTime());
        }
    }

    public LegacyBusAnimationSequence getBus(String name) {
        return animationBuses.get(name);
    }

    public Set<String> getBusNames() {
        return Collections.unmodifiableSet(animationBuses.keySet());
    }

    public void setTimeMult(double mult) {
        for (LegacyBusAnimationSequence sequence : animationBuses.values()) {
            sequence.multiplyTime(mult);
        }
        updateTime();
    }

    public double[] getTimedTransformation(String name, int millis) {
        LegacyBusAnimationSequence sequence = animationBuses.get(name);
        return sequence == null ? null : sequence.getTransformation(millis);
    }

    public int getDuration() {
        return totalTime;
    }
}
