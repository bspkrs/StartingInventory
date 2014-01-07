package bspkrs.startinginventory.fml;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class NetworkHandler
{
    @SubscribeEvent
    public void playerLoggedIn(PlayerLoggedInEvent event)
    {
        FMLCommonHandler.instance().bus().register(new SIGiveItemTicker(10, event.player));
    }
}
