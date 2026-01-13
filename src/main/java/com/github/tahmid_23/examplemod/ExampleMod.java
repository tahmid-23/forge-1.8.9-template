package com.github.tahmid_23.examplemod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "examplemod", useMetadata = true)
public class ExampleMod {
    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        event.getModLog().info("Hello, ExampleMod!");
    }
}
