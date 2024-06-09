package com.github.wohaopa.tc4helper.mixins;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.github.wohaopa.tc4helper.autoplay.AutoPlay;
import com.github.wohaopa.tc4helper.autoplay.AutoPlayButton;
import com.github.wohaopa.tc4helper.autoplay.GuiResearchTableHelperInterface;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.gui.GuiResearchTable;
import thaumcraft.common.lib.HexUtils;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketAspectCombinationToServer;
import thaumcraft.common.lib.network.playerdata.PacketAspectPlaceToServer;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.tiles.TileResearchTable;

@Mixin(value = GuiResearchTable.class, remap = false)
@Implements(@Interface(iface = GuiResearchTableHelperInterface.class, prefix = "ident$"))
public abstract class GuiResearchTableMixin extends GuiContainer {

    private GuiResearchTableMixin(Container p_i1072_1_) {
        super(p_i1072_1_);
    }

    @Shadow
    EntityPlayer player;
    @Shadow
    public ResearchNoteData note = null;
    @Shadow
    private TileResearchTable tileEntity;

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new AutoPlayButton(101, width / 2 - 25, 20, 50, 20));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 101) {
            AutoPlayButton autoPlayButton = ((AutoPlayButton) button);

            if (autoPlayButton.canAborted()) {
                autoPlayButton.abort();
            } else if (autoPlayButton.canExecute()) {
                autoPlayButton.execute();
            } else if (autoPlayButton.canStart()) {
                if (note != null) {
                    AutoPlay autoPlay = new AutoPlay((GuiResearchTableHelperInterface) this, player, note);
                    autoPlay.start();
                    autoPlayButton.setAutoPlay(autoPlay);
                }
            } else if (autoPlayButton.complete()) {
                autoPlayButton.reset();
            }
        }
    }

    public void ident$place(HexUtils.Hex hex, Aspect aspect) {
        PacketHandler.INSTANCE.sendToServer(
            new PacketAspectPlaceToServer(
                this.player,
                (byte) hex.q,
                (byte) hex.r,
                this.tileEntity.xCoord,
                this.tileEntity.yCoord,
                this.tileEntity.zCoord,
                aspect));
    }

    public void ident$combine(Aspect aspect1, Aspect aspect2) {
        PacketHandler.INSTANCE.sendToServer(
            new PacketAspectCombinationToServer(
                this.player,
                this.tileEntity.xCoord,
                this.tileEntity.yCoord,
                this.tileEntity.zCoord,
                aspect1,
                aspect2,
                this.tileEntity.bonusAspects.getAmount(aspect1) > 0,
                this.tileEntity.bonusAspects.getAmount(aspect2) > 0,
                true));
    }

}
