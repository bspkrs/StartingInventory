package bspkrs.startinginventory.fml;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import bspkrs.bspkrscore.fml.bspkrsCoreMod;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class SIGameTicker implements ITickHandler
{
    private EnumSet<TickType> tickTypes = EnumSet.noneOf(TickType.class);
    private Minecraft         mcClient;
    private boolean           allowUpdateCheck;
    
    public SIGameTicker(EnumSet<TickType> tickTypes)
    {
        this.tickTypes = tickTypes;
        mcClient = FMLClientHandler.instance().getClient();
        allowUpdateCheck = bspkrsCoreMod.instance.allowUpdateCheck;
    }
    
    @Override
    public void tickStart(EnumSet<TickType> tickTypes, Object... tickData)
    {
        tick(tickTypes, true);
    }
    
    @Override
    public void tickEnd(EnumSet<TickType> tickTypes, Object... tickData)
    {
        tick(tickTypes, false);
    }
    
    private void tick(EnumSet<TickType> tickTypes, boolean isStart)
    {
        for (TickType tickType : tickTypes)
        {
            if (!onTick(tickType, isStart))
            {
                this.tickTypes.remove(tickType);
                this.tickTypes.removeAll(tickType.partnerTicks());
            }
        }
    }
    
    public boolean onTick(TickType tick, boolean isStart)
    {
        if (isStart)
        {
            return true;
        }
        
        if (allowUpdateCheck && mcClient != null && mcClient.thePlayer != null)
        {
            if (StartingInventoryMod.versionChecker != null)
                if (!StartingInventoryMod.versionChecker.isCurrentVersion())
                    for (String msg : StartingInventoryMod.versionChecker.getInGameMessage())
                        mcClient.thePlayer.addChatMessage(msg);
            
            return false;
        }
        
        return allowUpdateCheck;
    }
    
    @Override
    public EnumSet<TickType> ticks()
    {
        return tickTypes;
    }
    
    @Override
    public String getLabel()
    {
        return "SIGameTicker";
    }
    
}
