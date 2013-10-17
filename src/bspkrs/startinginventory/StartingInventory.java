package bspkrs.startinginventory;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.SaveHandler;
import bspkrs.util.CommonUtils;
import bspkrs.util.Const;

public class StartingInventory
{
    public static final String    VERSION_NUMBER = Const.MCVERSION + ".r02";
    
    boolean                       canGiveItems;
    private static String         fileName       = "startingInventory.txt";
    private static String         configPath     = "/config/StartingInventory/";
    private static File           file           = new File(new File(CommonUtils.getMinecraftDir()), configPath + fileName);
    private static Scanner        scan;
    private static List<String>   list           = new ArrayList<String>();
    private final static String[] defaultItems   = { "274, 1", "273, 1", "272, 1", "275, 1", "260, 16", "50, 16" };
    
    static
    {
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
    }
    
    public static boolean isPlayerNewToWorld(MinecraftServer server, EntityPlayer player)
    {
        SaveHandler saveHandler = (SaveHandler) server.worldServerForDimension(0).getSaveHandler();
        File dir = new File(saveHandler.getWorldDirectory(), "/StartingInv");
        return !dir.exists() || !(new File(dir, player.username + ".si")).exists();
    }
    
    public static boolean createPlayerFile(MinecraftServer server, EntityPlayer player)
    {
        SaveHandler saveHandler = (SaveHandler) server.worldServerForDimension(0).getSaveHandler();
        File dir = new File(saveHandler.getWorldDirectory(), "/StartingInv");
        
        if (!dir.exists() && !dir.mkdir())
            return false;
        
        File pFile = new File(dir, player.username + ".si");
        
        try
        {
            pFile.createNewFile();
            PrintWriter out = new PrintWriter(new FileWriter(pFile));
            out.println("I was here!");
            out.close();
            return true;
            
        }
        catch (Exception exception)
        {
            return false;
        }
    }
    
    public static boolean addItems(EntityPlayer player)
    {
        for (int i = 0; i < Math.min(player.inventory.getSizeInventory(), list.size()); i++)
        {
            addItemToInv(list.get(i), player);
        }
        return true;
    }
    
    private static int[] parseLine(String entry)
    {
        int[] r = { 0, 1, 0 };
        int d1 = entry.indexOf(',');
        int d2 = entry.indexOf(',', d1 + 1);
        
        if (d1 != -1)
        {
            r[0] = CommonUtils.parseInt(entry.substring(0, d1));
            if (d2 != -1)
            {
                r[1] = CommonUtils.parseInt(entry.substring(d1 + 1, d2));
                r[2] = CommonUtils.parseInt(entry.substring(d2 + 1));
            }
            else
                r[1] = CommonUtils.parseInt(entry.substring(d1 + 1));
        }
        else
            r[0] = CommonUtils.parseInt(entry);
        
        return r;
    }
    
    private static void addItemToInv(String entry, EntityPlayer player)
    {
        int[] item = parseLine(entry);
        if (Item.itemsList[item[0]] != null)
            player.inventory.addItemStackToInventory(new ItemStack(item[0], item[1], item[2]));
    }
    
    private static void readItems()
    {
        list.clear();
        if (scan != null)
        {
            for (; scan.hasNextLine(); list.add(scan.nextLine()))
            {}
        }
        scan.close();
    }
    
    private static void createFile()
    {
        File dir = new File(new File(CommonUtils.getMinecraftDir()), configPath);
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
    
    protected static void writeConfigFileFromInventory(EntityPlayer player)
    {
        list.clear();
        try
        {
            if (file.exists())
                file.delete();
            
            file.createNewFile();
            
            PrintWriter out = new PrintWriter(new FileWriter(file));
            
            for (ItemStack itemStack : player.inventory.mainInventory)
            {
                if (itemStack != null)
                    list.add(itemStack.itemID + ", " + itemStack.stackSize + (!itemStack.isItemStackDamageable() ? ", " + itemStack.getItemDamage() : ""));
            }
            
            for (String s : list)
            {
                out.println(s);
            }
            
            out.close();
            
            scan = new Scanner(file);
            
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        
        readItems();
    }
    
    protected static void loadInventoryFromConfigFile(EntityPlayer player)
    {
        player.inventory.clearInventory(-1, -1);
        addItems(player);
    }
}
