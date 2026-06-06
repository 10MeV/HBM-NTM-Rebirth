package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjPheoDoorModels {
    public static final LegacyWavefrontModel FIRE_DOOR = model("fire_door").asVBO();
    public static final LegacyWavefrontModel AIRLOCK_DOOR = model("airlock_door").asVBO();
    public static final LegacyWavefrontModel BLAST_DOOR = model("blast_door").asVBO();
    public static final LegacyWavefrontModel CONTAINMENT_DOOR = model("containment_door").asVBO();
    public static final LegacyWavefrontModel SEAL_DOOR = model("seal_door").asVBO();
    public static final LegacyWavefrontModel SECURE_DOOR = model("secure_door").asVBO();
    public static final LegacyWavefrontModel SLIDING_DOOR = model("sliding_door").asVBO();
    public static final LegacyWavefrontModel VEHICLE_DOOR = model("vehicle_door").asVBO();
    public static final LegacyWavefrontModel WATER_DOOR = model("water_door").asVBO();
    public static final LegacyWavefrontModel VAULT_DOOR = model("vault_door", vaultTexture("vault_door_3")).asVBO();

    public static final ResourceLocation FIRE_DOOR_TEXTURE = texture("fire_door");
    public static final ResourceLocation FIRE_DOOR_BLACK_TEXTURE = texture("fire_door_black");
    public static final ResourceLocation FIRE_DOOR_ORANGE_TEXTURE = texture("fire_door_orange");
    public static final ResourceLocation FIRE_DOOR_YELLOW_TEXTURE = texture("fire_door_yellow");
    public static final ResourceLocation FIRE_DOOR_TREFOIL_TEXTURE = texture("fire_door_trefoil");
    public static final ResourceLocation AIRLOCK_DOOR_TEXTURE = texture("airlock_door");
    public static final ResourceLocation AIRLOCK_DOOR_CLEAN_TEXTURE = texture("airlock_door_clean");
    public static final ResourceLocation AIRLOCK_DOOR_GREEN_TEXTURE = texture("airlock_door_green");
    public static final ResourceLocation BLAST_DOOR_TEXTURE = texture("blast_door");
    public static final ResourceLocation CONTAINMENT_DOOR_TEXTURE = texture("containment_door");
    public static final ResourceLocation CONTAINMENT_DOOR_TREFOIL_TEXTURE = texture("containment_door_trefoil");
    public static final ResourceLocation CONTAINMENT_DOOR_TREFOIL_YELLOW_TEXTURE = texture("containment_door_trefoil_yellow");
    public static final ResourceLocation SEAL_DOOR_TEXTURE = texture("seal_door");
    public static final ResourceLocation SECURE_DOOR_TEXTURE = texture("secure_door");
    public static final ResourceLocation SECURE_DOOR_GREY_TEXTURE = texture("secure_door_grey");
    public static final ResourceLocation SECURE_DOOR_BLACK_TEXTURE = texture("secure_door_black");
    public static final ResourceLocation SECURE_DOOR_YELLOW_TEXTURE = texture("secure_door_yellow");
    public static final ResourceLocation SLIDING_DOOR_TEXTURE = texture("sliding_door");
    public static final ResourceLocation VEHICLE_DOOR_TEXTURE = texture("vehicle_door");
    public static final ResourceLocation WATER_DOOR_TEXTURE = texture("water_door");
    public static final ResourceLocation WATER_DOOR_CLEAN_TEXTURE = texture("water_door_clean");
    public static final ResourceLocation VAULT_DOOR_3_TEXTURE = vaultTexture("vault_door_3");
    public static final ResourceLocation VAULT_DOOR_4_TEXTURE = vaultTexture("vault_door_4");
    public static final ResourceLocation VAULT_DOOR_S_TEXTURE = vaultTexture("vault_door_s");
    public static final ResourceLocation LABEL_2_TEXTURE = vaultTexture("label_2");
    public static final ResourceLocation LABEL_81_TEXTURE = vaultTexture("label_81");
    public static final ResourceLocation LABEL_87_TEXTURE = vaultTexture("label_87");
    public static final ResourceLocation LABEL_99_TEXTURE = vaultTexture("label_99");
    public static final ResourceLocation LABEL_101_TEXTURE = vaultTexture("label_101");
    public static final ResourceLocation LABEL_106_TEXTURE = vaultTexture("label_106");
    public static final ResourceLocation LABEL_111_TEXTURE = vaultTexture("label_111");

    public static LegacyWavefrontModel model(String name) {
        return model(name, texture(name));
    }

    public static LegacyWavefrontModel model(String name, ResourceLocation texture) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/pheodoors/" + name + ".obj"),
                texture);
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/pheodoors/" + name + ".png");
    }

    public static ResourceLocation vaultTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/pheodoors/vault/" + name + ".png");
    }

    private ObjPheoDoorModels() {
    }
}
