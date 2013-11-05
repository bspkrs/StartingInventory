package bspkrs.startinginventory.fml;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import bspkrs.bspkrscore.fml.bspkrsCoreMod;
import bspkrs.fml.util.TickerBase;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.TickType;

public class SIGameTicker extends TickerBase
{
    private Minecraft mcClient;
    private boolean   allowUpdateCheck;
    
    public SIGameTicker(EnumSet<TickType> tickTypes)
    {
        super(tickTypes);
        mcClient = FMLClientHandler.instance().getClient();
        allowUpdateCheck = bspkrsCoreMod.instance.allowUpdateCheck;
    }
    
    @Override
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
    public String getLabel()
    {
        return "SIGameTicker";
    }
    
}
