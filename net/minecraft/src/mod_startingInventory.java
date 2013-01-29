package net.minecraft.src;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import bspkrs.util.ModVersionChecker;

public class mod_StartingInventory extends BaseMod
{
    private static final String INV              = "inventory";
    private static final String CHEST            = "chest";
    
    @MLProp(info = "Set to true to allow checking for mod updates, false to disable")
    public static boolean       allowUpdateCheck = true;
    @MLProp(info = "Use \"inventory\" to add items to your inventory on starting a world, use \"chest\" to continue using that lame placed chest nonsense.")
    public static String        addItemsTo       = INV;
    @MLProp(info = "Items will be added to the vanilla bonus chest when set to true (meaning you must set bonus chest to ON). If set to false, a separate chest will be placed when addItemsTo=chest")
    public static boolean       useBonusChest    = false;
    @MLProp(info = "The length of time in game time ticks (1/20th of a second) that items have to spawn in a fresh world.  If you are on a slower machine and are having trouble with items not spawning, try setting this a little higher\n\n**ONLY EDIT WHAT IS BELOW THIS**")
    public static int           tickWindow       = 100;
    
    boolean                     canGiveItems;
    String                      fileName;
    String                      configPath;
    File                        mcdir;
    File                        file;
    private Scanner             scan;
    private final List          list;
    private TileEntityChest     chest;
    private final String[]      defaultItems     = { "272, 1", "273, 1", "274, 1", "275, 1", "260, 16", "50, 16" };
    private Minecraft           mc;
    
    private ModVersionChecker   versionChecker;
    private final String        versionURL       = "https://dl.dropbox.com/u/20748481/Minecraft/1.4.6/startingInventory.version";
    private final String        mcfTopic         = "http://www.minecraftforum.net/topic/1009577-";
    
    public mod_StartingInventory()
    {
        mc = ModLoader.getMinecraftInstance();
        mcdir = Minecraft.getMinecraftDir();
        fileName = "startingInventory.txt";
        configPath = "/config/StartingInventory/";
        file = new File(mcdir, configPath + fileName);
        list = new ArrayList();
        list.clear();
        if (!file.exists())
            createFile();
        else
            try
            {
                scan = new Scanner(file);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
        readItems();
        
        if (allowUpdateCheck)
            versionChecker = new ModVersionChecker(getName(), getVersion(), versionURL, mcfTopic, ModLoader.getLogger());
    }
    
    @Override
    public String getName()
    {
        return "StartingInventory";
    }
    
    @Override
    public String getVersion()
    {
        return "ML 1.4.6.r03";
    }
    
    @Override
    public void load()
    {
        if (allowUpdateCheck)
            versionChecker.checkVersionWithLogging();
    }
    
    @Override
    public boolean onTickInGame(float f, Minecraft mc)
    {
        if (canGiveItems && mc.isSingleplayer() && isFreshWorld(mc))
            canGiveItems = !addItems(mc.getIntegratedServer().worldServerForDimension(mc.thePlayer.dimension));
        
        if (allowUpdateCheck)
        {
            if (!versionChecker.isCurrentVersion())
                for (String msg : versionChecker.getInGameMessage())
                    mc.thePlayer.addChatMessage(msg);
            allowUpdateCheck = false;
        }
        
        return canGiveItems && mc.isSingleplayer() && isFreshWorld(mc);
    }
    
    @Override
    public void clientConnect(NetClientHandler nch)
    {
        ModLoader.setInGameHook(this, true, true);
        canGiveItems = true;
    }
    
    public TileEntityChest getChest(World world)
    {
        int x = world.getWorldInfo().getSpawnX();
        int z = world.getWorldInfo().getSpawnZ();
        
        if (useBonusChest)
        {
            for (int i = 10; i >= -10; i--)
                for (int j = 10; j >= -10; j--)
                {
                    int y = world.getTopSolidOrLiquidBlock(x + i, z + j) - 1;
                    if (world.getBlockId(x + i, y, z + j) == Block.chest.blockID)
                    {
                        ModLoader.getLogger().log(Level.INFO, "Found bonus chest at " + (x + i) + ", " + y + ", " + (z + j));
                        return (TileEntityChest) world.getBlockTileEntity(x + i, y, z + j);
                    }
                }
            ModLoader.getLogger().log(Level.WARNING, "Unable to find bonus chest.");
        }
        else
        {
            Random random = new Random();
            for (int i = 0; i < 10; i++)
            {
                x = world.getWorldInfo().getSpawnX() + random.nextInt(6) - random.nextInt(6);
                z = world.getWorldInfo().getSpawnZ() + random.nextInt(6) - random.nextInt(6);
                int y = world.getTopSolidOrLiquidBlock(x, z);
                if (world.isAirBlock(x, y, z) && world.doesBlockHaveSolidTopSurface(x, y - 1, z))
                {
                    for (int a = -1; a <= -1; a++)
                        for (int b = -1; b <= 1; b++)
                        {
                            world.setBlockWithNotify(x + a, y, z + b, 0);
                            world.setBlockWithNotify(x + a, y + 1, z + b, 0);
                        }
                    world.setBlockWithNotify(x, y, z, Block.chest.blockID);
                    ModLoader.getLogger().log(Level.INFO, "Chest placed at " + x + ", " + y + ", " + z);
                    return (TileEntityChest) world.getBlockTileEntity(x, y, z);
                }
            }
        }
        
        return null;
        
    }
    
    public boolean isFreshWorld(Minecraft mc)
    {
        try
        {
            return (int) mc.theWorld.getWorldInfo().getWorldTime() < tickWindow;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    public boolean addItems(World world)
    {
        if (this.addItemsTo.equals(CHEST))
        {
            chest = getChest(world);
            if (chest != null)
            {
                for (int i = 0; i < chest.getSizeInventory(); i++)
                    chest.setInventorySlotContents(i, null);
                
                for (int i = 0; i < Math.min(chest.getSizeInventory(), list.size()); i++)
                {
                    addItemToChest(chest, i, (String) list.get(i));
                }
                return true;
            }
            return false;
        }
        else
        {
            for (int i = 0; i < Math.min(chest.getSizeInventory(), list.size()); i++)
            {
                addItemToInv((String) list.get(i));
            }
            return true;
        }
    }
    
    private int[] parseLine(String entry)
    {
        int[] r = { 0, 1, 0 };
        int d1 = entry.indexOf(',');
        int d2 = entry.indexOf(',', d1 + 1);
        
        if (d1 != -1)
        {
            r[0] = parseInt(entry.substring(0, d1));
            if (d2 != -1)
            {
                r[1] = parseInt(entry.substring(d1 + 1, d2));
                r[2] = parseInt(entry.substring(d2 + 1));
            }
            else
                r[1] = parseInt(entry.substring(d1 + 1));
        }
        else
            r[0] = parseInt(entry);
        
        return r;
    }
    
    private void addItemToInv(String entry)
    {
        int[] item = parseLine(entry);
        if (Item.itemsList[item[0]] != null)
            mc.getIntegratedServer().worldServerForDimension(mc.thePlayer.dimension).getPlayerEntityByName(mc.thePlayer.username).inventory.addItemStackToInventory(new ItemStack(item[0], item[1], item[2]));
    }
    
    private void addItemToChest(TileEntityChest chest, int slot, String entry)
    {
        int[] item = parseLine(entry);
        if (Item.itemsList[item[0]] != null)
            chest.setInventorySlotContents(slot, new ItemStack(item[0], item[1], item[2]));
    }
    
    public void readItems()
    {
        if (scan != null)
        {
            for (; scan.hasNextLine(); list.add(scan.nextLine()))
            {}
        }
        scan.close();
    }
    
    public int parseInt(String s)
    {
        try
        {
            return Integer.parseInt(s.trim());
        }
        catch (NumberFormatException numberformatexception)
        {
            return 0;
        }
    }
    
    public void createFile()
    {
        File dir = new File(mcdir, configPath);
        if (!dir.exists() && dir.mkdir())
        {
            file = new File(dir, fileName);
        }
        else
        {
            file = new File(dir, fileName);
        }
        try
        {
            file.createNewFile();
            PrintWriter out = new PrintWriter(new FileWriter(file));
            
            for (String s : defaultItems)
            {
                out.println(s);
            }
            
            out.close();
            
            scan = new Scanner(file);
            
        }
        catch (Exception exception)
        {}
    }
}
