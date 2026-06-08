package com.hbm.ntm.neutron;

public class RBMKBoilerState {
    public static final int DEFAULT_FEED_MAX = 10_000;
    public static final int DEFAULT_STEAM_MAX = 1_000_000;

    private int feedFill;
    private int feedMax = DEFAULT_FEED_MAX;
    private int steamFill;
    private int steamMax = DEFAULT_STEAM_MAX;
    private RBMKBoilerRuntime.SteamGrade steamGrade = RBMKBoilerRuntime.SteamGrade.STEAM;
    private int consumption;
    private int output;
    private int ventDelay;

    public int feedFill() {
        return feedFill;
    }

    public void setFeedFill(int feedFill) {
        this.feedFill = clamp(feedFill, 0, feedMax);
    }

    public int feedMax() {
        return feedMax;
    }

    public void setFeedMax(int feedMax) {
        this.feedMax = Math.max(1, feedMax);
        setFeedFill(feedFill);
    }

    public int steamFill() {
        return steamFill;
    }

    public void setSteamFill(int steamFill) {
        this.steamFill = clamp(steamFill, 0, steamMax);
    }

    public int steamMax() {
        return steamMax;
    }

    public void setSteamMax(int steamMax) {
        this.steamMax = Math.max(1, steamMax);
        setSteamFill(steamFill);
    }

    public RBMKBoilerRuntime.SteamGrade steamGrade() {
        return steamGrade;
    }

    public void setSteamGrade(RBMKBoilerRuntime.SteamGrade steamGrade) {
        this.steamGrade = steamGrade == null ? RBMKBoilerRuntime.SteamGrade.STEAM : steamGrade;
    }

    public int consumption() {
        return consumption;
    }

    void setConsumption(int consumption) {
        this.consumption = Math.max(0, consumption);
    }

    public int output() {
        return output;
    }

    void setOutput(int output) {
        this.output = Math.max(0, output);
    }

    public int ventDelay() {
        return ventDelay;
    }

    public void setVentDelay(int ventDelay) {
        this.ventDelay = Math.max(0, ventDelay);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
