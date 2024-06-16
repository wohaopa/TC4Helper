package com.github.wohaopa.tc4helper;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import com.github.wohaopa.tc4helper.autoplay.AutoPlayButton;

public class Command extends CommandBase {

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "TC4Helper";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "TC4Helper Enable?";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayer) {
            if (args.length == 0) {
                if (TC4Helper.enabled) {
                    sender.addChatMessage(new ChatComponentText("TC4Helper is already disabled!"));
                    TC4Helper.enabled = false;
                } else {
                    sender.addChatMessage(new ChatComponentText("TC4Helper is now enabled!"));
                    TC4Helper.enabled = true;
                }
            }
            if (args.length == 1 && args[0].equals("Restart")) {
                AutoPlayButton.restart();
            }
        }

    }
}
