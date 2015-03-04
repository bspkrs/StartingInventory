package bspkrs.startinginventory.fml;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
