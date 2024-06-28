package com.github.wohaopa.tc4helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.wohaopa.tc4helper.proxy.IProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

@Mod(
    modid = TC4Helper.MODID,
    version = Tags.VERSION,
    name = "TC4Helper",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "after:ThaumcraftResearchTweaks")
public class TC4Helper {

    public static final String MODID = "tc4helper";
    public static final Logger LOG = LogManager.getLogger(MODID);
    public static boolean enabled = true;

    @SidedProxy(
        clientSide = "com.github.wohaopa.tc4helper.proxy.ClientProxy",
        serverSide = "com.github.wohaopa.tc4helper.proxy.ServerProxy")
    public static IProxy proxy;

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void complete(FMLLoadCompleteEvent event) {
        proxy.complete(event);
    }

}
