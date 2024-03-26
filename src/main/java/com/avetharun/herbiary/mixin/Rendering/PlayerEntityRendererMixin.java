package com.avetharun.herbiary.mixin.Rendering;

import com.avetharun.herbiary.Herbiary;
import com.avetharun.herbiary.hUtil.iface.PlayerEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Mixin(BipedEntityModel.class)
    public static class PlayerGuiArmRendererMixin<T extends LivingEntity> {
        @Shadow @Final public ModelPart rightArm;

        @Shadow @Final public ModelPart leftArm;


        @Unique
        float herbiary$getOffset(){
            return (float) Math.sin(Herbiary.clientRuntime) * 0.25f;
        }

        @Shadow @Final public ModelPart head;
        @Shadow public BipedEntityModel.ArmPose leftArmPose;

        @Shadow public BipedEntityModel.ArmPose rightArmPose;

        // Unfortunately, this is the easiest method I could think of.
        @Inject(method="positionRightArm", at=@At(value = "HEAD"), cancellable = true)
        void positionRightArmMixin(T entity, CallbackInfo ci){

            if (MinecraftClient.getInstance().player == entity && !MinecraftClient.getInstance().gameRenderer.getCamera().isThirdPerson()) {return;}
            if (rightArmPose == BipedEntityModel.ArmPose.THROW_SPEAR) {
                this.rightArm.yaw = -0.05F + this.head.yaw;
                this.rightArm.pitch = (-1.7707964F + this.head.pitch - (MathHelper.RADIANS_PER_DEGREE * 80)) * 0.85f;
                this.rightArm.roll = -5 * MathHelper.RADIANS_PER_DEGREE;
                ci.cancel();
            }
            if (entity instanceof PlayerEntityAccessor pea) {
                if (pea.isSifting()){
                    this.rightArm.yaw = -0.1F + this.head.yaw * 0.25f;
                    this.leftArm.yaw = 0.1F + this.head.yaw * 0.25f;
                    this.rightArm.pitch = -1.5707964F + this.head.pitch;
                    this.leftArm.pitch = -1.5707964F + this.head.pitch;
                    ci.cancel();
                }
            }
        }
        @Inject(method="positionLeftArm", at=@At(value = "HEAD"), cancellable = true)
        void positionLeftArmMixin(T entity, CallbackInfo ci){
            if (MinecraftClient.getInstance().player == entity && !MinecraftClient.getInstance().gameRenderer.getCamera().isThirdPerson()) {return;}
            if (leftArmPose == BipedEntityModel.ArmPose.THROW_SPEAR) {
                this.leftArm.yaw = 0.05F + this.head.yaw;
                this.leftArm.pitch = (-1.7707964F + this.head.pitch - (MathHelper.RADIANS_PER_DEGREE * 80)) * 0.85f;
                this.leftArm.roll = 5 * MathHelper.RADIANS_PER_DEGREE;
                ci.cancel();
            }
            if (entity instanceof PlayerEntityAccessor pea) {
                if (pea.isSifting()){
                    this.rightArm.yaw = -0.1F + this.head.yaw * 0.25f;
                    this.leftArm.yaw = 0.1F + this.head.yaw * 0.25f;
                    this.rightArm.pitch = -1.5707964F + this.head.pitch;
                    this.leftArm.pitch = -1.5707964F + this.head.pitch;
                    ci.cancel();
                }
            }
        }
    }
}
