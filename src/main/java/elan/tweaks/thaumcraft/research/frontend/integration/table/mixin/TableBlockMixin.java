package elan.tweaks.thaumcraft.research.frontend.integration.table.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.wohaopa.tc4helper.TC4Helper;

import thaumcraft.common.blocks.BlockTable;

@Mixin(BlockTable.class)
public class TableBlockMixin {

    @Redirect(
        method = ("onBlockActivated"),
        at = @At(
            value = "INVOKE",
            target = "net.minecraft.entity.player.EntityPlayer.openGui(Ljava/lang/Object;ILnet/minecraft/world/World;III)V",
            remap = false),
        require = 4)
    private void correctGuiCallFor(EntityPlayer entityPlayer, Object mod, int modGuiId, World world, int x, int y,
        int z) {
        if (modGuiId == 10 && !TC4Helper.enabled) {
            entityPlayer.openGui("ThaumcraftResearchTweaks", 0, world, x, y, z);
        } else {
            entityPlayer.openGui(mod, modGuiId, world, x, y, z);
        }
    }
}
