package com.github.wohaopa.tc4helper.proxy;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraftforge.client.ClientCommandHandler;

import com.github.wohaopa.tc4helper.Command;
import com.github.wohaopa.tc4helper.GuiHandler;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;

public class ClientProxy implements IProxy {

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new Command());
    }

    @Override
    public void complete(FMLLoadCompleteEvent event) {
        if (Loader.isModLoaded("ThaumcraftResearchTweaks")) {

            try {
                Class<NetworkRegistry> clazz = NetworkRegistry.class;
                Field field = clazz.getDeclaredField("clientGuiHandlers");
                field.setAccessible(true);
                Map<ModContainer, IGuiHandler> clientGuiHandlers = (Map<ModContainer, IGuiHandler>) field
                    .get(NetworkRegistry.INSTANCE);
                ModContainer mc = Loader.instance()
                    .getIndexedModList()
                    .get("ThaumcraftResearchTweaks");

                NetworkRegistry.INSTANCE
                    .registerGuiHandler("ThaumcraftResearchTweaks", new GuiHandler(clientGuiHandlers.get(mc)));

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }

    }
}
