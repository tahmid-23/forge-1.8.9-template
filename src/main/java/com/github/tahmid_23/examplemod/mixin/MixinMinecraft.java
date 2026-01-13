package com.github.tahmid_23.examplemod.mixin;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.Minecraft.class)
public class MixinMinecraft {

    @Shadow
    @Final
    private static Logger logger;

    @Inject(method = "startGame", at = @At("HEAD"))
    private void onStartGame(CallbackInfo ci) {
        logger.info("Hello, Mixin!");
    }

}
