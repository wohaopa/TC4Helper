package com.github.wohaopa.tc4helper.autoplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class AutoPlayButton extends GuiButton {

    static AutoPlay autoPlay;

    public AutoPlayButton(int buttonId, int x, int y, int width, int height) {
        super(buttonId, x, y, width, height, "");
    }

    public boolean canAborted() {
        return autoPlay != null && !autoPlay.isCompleted() && !autoPlay.isAborted();
    }

    public boolean canExecute() {
        return autoPlay != null && autoPlay.isResult() && autoPlay.isCompleted();
    }

    public boolean canStart() {
        return autoPlay == null;
    }

    public void abort() {
        autoPlay.abort();
    }

    public void execute() {
        autoPlay.execute();
        autoPlay = null;
    }

    public boolean hasResult() {
        return autoPlay != null && autoPlay.isResult();
    }

    public void setAutoPlay(AutoPlay autoPlay) {
        AutoPlayButton.autoPlay = autoPlay;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {

        if (canStart()) displayString = "AutoPlay";
        else if (hasResult()) displayString = "HasResult";
        else if (canAborted()) displayString = "Play...";
        else if (canExecute()) displayString = "Execute";
        else if (complete()) displayString = "Reset";
        else displayString = "UnKnown";

        super.drawButton(mc, mouseX, mouseY);

    }

    public boolean complete() {
        return autoPlay != null && !autoPlay.isResult() && autoPlay.isCompleted();
    }

    public void reset() {
        autoPlay = null;
    }
}
