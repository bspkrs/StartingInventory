package bspkrs.startinginventory.fml;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import bspkrs.bspkrscore.fml.bspkrsCoreMod;
import bspkrs.helpers.entity.player.EntityPlayerHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class SIGameTicker
{
    private Minecraft      mcClient;
    private static boolean isRegistered = false;
    
    public SIGameTicker()
    {
        mcClient = FMLClientHandler.instance().getClient();
        isRegistered = true;
    }
    
    @SubscribeEvent
    public void onTick(ClientTickEvent event)
    {
        if (event.phase.equals(Phase.START))
            return;
        
        boolean keepTicking = !(mcClient != null && mcClient.thePlayer != null && mcClient.theWorld != null);
        
        if (bspkrsCoreMod.instance.allowUpdateCheck && !keepTicking)
        {
            if (StartingInventoryMod.versionChecker != null)
                if (!StartingInventoryMod.versionChecker.isCurrentVersion())
                    for (String msg : StartingInventoryMod.versionChecker.getInGameMessage())
                        EntityPlayerHelper.addChatMessage(mcClient.thePlayer, new ChatComponentText(msg));
            
            if (!keepTicking)
            {
                FMLCommonHandler.instance().bus().unregister(this);
                isRegistered = false;
            }
        }
    }
    
    public static boolean isRegistered()
    {
        return isRegistered;
    }
}
