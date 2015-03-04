package bspkrs.startinginventory.fml;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class NetworkHandler
{
    @SubscribeEvent
    public void playerLoggedIn(PlayerLoggedInEvent event)
    {
        FMLCommonHandler.instance().bus().register(new SIGiveItemTicker(10, event.player));
    }
}
