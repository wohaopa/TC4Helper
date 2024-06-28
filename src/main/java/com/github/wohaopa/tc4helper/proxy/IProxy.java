package com.github.wohaopa.tc4helper.proxy;

import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

public interface IProxy {

    default void postInit(FMLPostInitializationEvent event) {}

    default void complete(FMLLoadCompleteEvent event) {}
}
