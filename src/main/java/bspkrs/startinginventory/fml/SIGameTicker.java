package bspkrs.startinginventory.fml;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import bspkrs.bspkrscore.fml.bspkrsCoreMod;

public class SIGameTicker
{
    private final Minecraft mcClient;
    private static boolean  isRegistered = false;

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

        boolean keepTicking = !((mcClient != null) && (mcClient.thePlayer != null) && (mcClient.theWorld != null));

        if (!keepTicking && isRegistered)
        {
            if (bspkrsCoreMod.instance.allowUpdateCheck && (StartingInventoryMod.versionChecker != null))
                if (!StartingInventoryMod.versionChecker.isCurrentVersion())
                    for (String msg : StartingInventoryMod.versionChecker.getInGameMessage())
                        mcClient.thePlayer.addChatMessage(new ChatComponentText(msg));

            FMLCommonHandler.instance().bus().unregister(this);
            isRegistered = false;
        }
    }

    public static boolean isRegistered()
    {
        return isRegistered;
    }
}
