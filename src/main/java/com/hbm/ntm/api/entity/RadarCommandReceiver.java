package com.hbm.ntm.api.entity;

import net.minecraft.world.entity.Entity;

public interface RadarCommandReceiver {
    boolean sendCommandPosition(int x, int y, int z);

    boolean sendCommandEntity(Entity target);
}
