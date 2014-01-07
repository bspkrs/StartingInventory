package bspkrs.startinginventory.fml;

import net.minecraft.server.MinecraftServer;
import bspkrs.bspkrscore.fml.bspkrsCoreMod;
import bspkrs.startinginventory.CommandStartingInv;
import bspkrs.startinginventory.StartingInventory;
import bspkrs.util.Const;
import bspkrs.util.ModVersionChecker;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(name = "StartingInventory", modid = "StartingInventory", version = "Forge " + StartingInventory.VERSION_NUMBER, dependencies = "required-after:bspkrsCore", useMetadata = true)
public class StartingInventoryMod
{
    protected static ModVersionChecker versionChecker;
    private final String               versionURL = Const.VERSION_URL + "/Minecraft/" + Const.MCVERSION + "/startingInventoryForge.version";
    private final String               mcfTopic   = "http://www.minecraftforum.net/topic/1009577-";
    
    public MinecraftServer             server;
    
    @Metadata(value = "StartingInventory")
    public static ModMetadata          metadata;
    
    @SidedProxy(clientSide = "bspkrs.startinginventory.fml.ClientProxy", serverSide = "bspkrs.startinginventory.fml.CommonProxy")
    public static CommonProxy          proxy;
    
    @Instance(value = "StartingInventory")
    public static StartingInventoryMod instance;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        metadata = event.getModMetadata();
        
        if (bspkrsCoreMod.instance.allowUpdateCheck)
        {
            versionChecker = new ModVersionChecker(metadata.name, metadata.version, versionURL, mcfTopic);
            versionChecker.checkVersionWithLogging();
        }
        
        StartingInventory.init();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.registerClientTicker();
        FMLCommonHandler.instance().bus().register(new NetworkHandler());
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        instance.server = event.getServer();
        event.registerServerCommand(new CommandStartingInv());
    }
    
    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
        instance.server = null;
    }
}
