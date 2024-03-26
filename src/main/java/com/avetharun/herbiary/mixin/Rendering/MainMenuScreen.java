package com.avetharun.herbiary.mixin.Rendering;

import com.avetharun.herbiary.Herbiary;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.IconWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MainMenuScreen extends Screen{

    protected MainMenuScreen(Text title) {
        super(title);
    }

    @Inject(method="init", at= @At(value = "TAIL"))
    void renderMixin(CallbackInfo ci) {
        MutableText herbiaryText = Text.literal("Mod Credits");
        int i = this.textRenderer.getWidth(herbiaryText);
        int j = this.width - i - 2;
        int l = this.height / 4 + 48;
        var icon = this.addDrawableChild(IconWidget.create(16, 16, new Identifier("minecraft:textures/block/dark_oak_sapling.png"), 16, 16));
        var icon1 = this.addDrawableChild(IconWidget.create(100, 6, new Identifier("minecraft:textures/block/grass_block_side.png"), 16, 16));

        var textBtn = this.addDrawableChild(new PressableTextWidget(j, this.height - (2 * this.textRenderer.fontHeight) - 30, i, 10, herbiaryText, (button) -> {
            assert this.client != null;
            this.client.setScreen(new Herbiary.CreditsScreen());
        }, this.textRenderer));

        icon.setPosition(j - 20, this.height - (2 * this.textRenderer.fontHeight) - 26);
        icon1.setPosition(j - 20, this.height - (2 * this.textRenderer.fontHeight) - 10);
    }
}
