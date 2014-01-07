package bspkrs.startinginventory.fml;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public void registerClientTicker()
    {
        if (!SIGameTicker.isRegistered())
            FMLCommonHandler.instance().bus().register(new SIGameTicker());
    }
}
