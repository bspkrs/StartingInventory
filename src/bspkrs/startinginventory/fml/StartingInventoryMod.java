package bspkrs.startinginventory.fml;

import java.util.EnumSet;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import bspkrs.bspkrscore.fml.bspkrsCoreMod;
import bspkrs.startinginventory.CommandStartingInv;
import bspkrs.startinginventory.StartingInventory;
import bspkrs.util.Const;
import bspkrs.util.ModVersionChecker;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(name = "StartingInventory", modid = "StartingInventory", version = "Forge " + StartingInventory.VERSION_NUMBER, dependencies = "required-after:bspkrsCore", useMetadata = true)
@NetworkMod(clientSideRequired = false, serverSideRequired = false, connectionHandler = StartingInventoryMod.class)
public class StartingInventoryMod implements IConnectionHandler
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
    
    /**
     * 1) Fired when a remote connection is opened CLIENT SIDE
     */
    @Override
    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
    {}
    
    /**
     * 1) Fired when a local connection is opened CLIENT SIDE
     */
    @Override
    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
    {}
    
    /**
     * 2) Called when a player logs into the server SERVER SIDE
     */
    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
    {
        TickRegistry.registerTickHandler(new SIGiveItemTicker(10, netHandler.getPlayer()).addTicks(EnumSet.of(TickType.SERVER)), Side.SERVER);
    }
    
    /**
     * 3) Fired when the client established the connection to the server CLIENT SIDE
     */
    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
    {}
    
    /**
     * If you don't want the connection to continue, return a non-empty string here If you do, you can do other stuff here- note no FML
     * negotiation has occured yet though the client is verified as having FML installed
     * 
     * SERVER SIDE
     */
    @Override
    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
    {
        return null;
    }
    
    /**
     * Fired when a connection closes ALL SIDES
     */
    @Override
    public void connectionClosed(INetworkManager manager)
    {}
}
