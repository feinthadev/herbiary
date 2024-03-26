package com.avetharun.herbiary.client;

import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerStatus {

    public static final TrackedData<Float> COLDNESS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> WETNESS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> HEAT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public float requiredPercentageToDisplay = 10;
    public float percentage;
    public String title;
    public Sprite sprite;
    public PlayerStatus(String title, float requiredPercentage) {
        this.title = title;
        this.requiredPercentageToDisplay = requiredPercentage;
    }
}
