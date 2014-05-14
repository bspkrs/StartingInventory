package bspkrs.startinginventory.fml;

import net.minecraft.entity.player.EntityPlayer;
import bspkrs.startinginventory.StartingInventory;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class SIGiveItemTicker
{
    private final EntityPlayer player;
    private int                delayTicks;
    
    public SIGiveItemTicker(int delayTicks, EntityPlayer player)
    {
        this.delayTicks = Math.max(delayTicks, 1);
        this.player = player;
    }
    
    @SubscribeEvent
    public void onTick(ServerTickEvent event)
    {
        if (event.phase.equals(Phase.START))
            return;
        
        if (--delayTicks == 0)
        {
            onDelayCompletion();
            FMLCommonHandler.instance().bus().unregister(this);
        }
    }
    
    protected void onDelayCompletion()
    {
        if (StartingInventory.isPlayerNewToWorld(StartingInventoryMod.instance.server, player))
        {
            StartingInventory.createPlayerFile(StartingInventoryMod.instance.server, player);
            
            // if (StartingInventory.isPlayerInventoryEmpty(player.inventory))
            StartingInventory.addItems(player);
        }
    }
    
}
