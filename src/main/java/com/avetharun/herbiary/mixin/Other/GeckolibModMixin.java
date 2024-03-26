package com.avetharun.herbiary.mixin.Other;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.example.GeckoLibMod;

@Mixin(value = GeckoLibMod.class, remap = false)
public class GeckolibModMixin {
    @ModifyReturnValue(method="shouldRegisterExamples", at=@At("RETURN"), remap = false)
    private static boolean shouldRenderExamplesMixin(boolean original) {
        return false;
    }
}
