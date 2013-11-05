package bspkrs.startinginventory.fml;

import net.minecraft.entity.player.EntityPlayer;
import bspkrs.fml.util.DelayedActionTicker;
import bspkrs.startinginventory.StartingInventory;

public class SIGiveItemTicker extends DelayedActionTicker
{
    private final EntityPlayer player;
    
    public SIGiveItemTicker(int delayTicks, EntityPlayer player)
    {
        super(delayTicks);
        this.player = player;
    }
    
    @Override
    public String getLabel()
    {
        return "SIGiveItemTicker";
    }
    
    @Override
    protected void onDelayCompletion()
    {
        if (StartingInventory.isPlayerNewToWorld(StartingInventoryMod.instance.server, player))
        {
            StartingInventory.createPlayerFile(StartingInventoryMod.instance.server, player);
            StartingInventory.addItems(player);
        }
    }
    
}
