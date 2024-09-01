package com.github.wohaopa.tc4helper.autoplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

import thaumcraft.common.lib.research.ResearchNoteData;

public class AutoPlayButton extends GuiButton {

    private static AutoPlay autoPlay = new AutoPlay();

    public static void restart() {
        autoPlay.interrupt();
        autoPlay.abort();

        autoPlay = new AutoPlay();
    }

    public AutoPlayButton(int buttonId, int x, int y, int width, int height) {
        super(buttonId, x, y, width, height, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        switch (autoPlay.getStatus()) {
            case Done -> displayString = I18n.format("TC4Helper.Done");
            case Leisure -> displayString = I18n.format("TC4Helper.Leisure");
            case Searching -> {
                displayString = I18n.format("TC4Helper.Searching");
            }
            case CanExecute -> displayString = I18n.format("TC4Helper.CanExecute");
            case Execute -> displayString = I18n.format("TC4Helper.Execute");
        }
        super.drawButton(mc, mouseX, mouseY);

    }

    public void onAction(EntityPlayer player, ResearchNoteData note, GuiResearchTableHelperInterface obj) {

        switch (autoPlay.getStatus()) {
            case Leisure, Done -> {
                if (note != null) if (autoPlay.set(obj, player, note)) autoPlay.start();
            }
            case Searching -> autoPlay.abort();
            case CanExecute, Execute -> autoPlay.execute();

        }
    }
}
