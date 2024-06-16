package com.github.wohaopa.tc4helper.autoplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

import thaumcraft.common.lib.research.ResearchNoteData;

public class AutoPlayButton extends GuiButton {

    private static AutoPlay autoPlay = new AutoPlay();

    public static void restart() {
        autoPlay.interrupt();

        autoPlay = new AutoPlay();
    }

    public AutoPlayButton(int buttonId, int x, int y, int width, int height) {
        super(buttonId, x, y, width, height, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        switch (autoPlay.getStatus()) {
            case Done -> displayString = "完成";
            case Leisure -> displayString = "AutoPlay";
            case Searching -> displayString = "搜索";
            case CanExecute -> displayString = "可以执行";
            case Execute -> displayString = "执行";
        }
        super.drawButton(mc, mouseX, mouseY);

    }

    public void onAction(EntityPlayer player, ResearchNoteData note, GuiResearchTableHelperInterface obj) {

        switch (autoPlay.getStatus()) {
            case Leisure, Done -> {
                if (autoPlay.set(obj, player, note)) autoPlay.start();
            }
            case Searching -> {
                autoPlay.abort();
            }
            case CanExecute, Execute -> {
                autoPlay.execute();
            }

        }
    }
}
