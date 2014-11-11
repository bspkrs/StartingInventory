package bspkrs.startinginventory;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.SaveHandler;

import org.apache.logging.log4j.Level;

import bspkrs.util.CommonUtils;
import bspkrs.util.Const;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameData;

public class StartingInventory
{
    public static final String     VERSION_NUMBER = Const.MCVERSION + ".r02";

    boolean                        canGiveItems;
    private static String          fileName       = "startingInventory.txt";
    private static String          configPath     = "/config/";
    private static File            file           = new File(new File(CommonUtils.getMinecraftDir()), configPath + fileName);
    private static Scanner         scan;
    private static List<ItemStack> itemsList      = new ArrayList<ItemStack>();
    private final static String[]  defaultItems   = { "minecraft:stone_pickaxe, 1", "minecraft:stone_shovel, 1", "minecraft:stone_sword, 1", "minecraft:stone_axe, 1", "minecraft:apple, 16", "minecraft:torch, 16" };

    public static void init()
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
        return !dir.exists() || !(new File(dir, player.getGameProfile().getName() + ".si")).exists();
    }

    public static boolean isPlayerInventoryEmpty(InventoryPlayer inv)
    {
        for (int i = 0; i < inv.mainInventory.length; i++)
            if (inv.mainInventory[i] != null)
                return false;

        for (int i = 0; i < inv.armorInventory.length; i++)
            if (inv.armorInventory[i] != null)
                return false;

        return true;
    }

    public static boolean createPlayerFile(MinecraftServer server, EntityPlayer player)
    {
        SaveHandler saveHandler = (SaveHandler) server.worldServerForDimension(0).getSaveHandler();
        File dir = new File(saveHandler.getWorldDirectory(), "/StartingInv");

        if (!dir.exists() && !dir.mkdir())
            return false;

        File pFile = new File(dir, player.getGameProfile().getName() + ".si");

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
        for (int i = 0; i < Math.min(player.inventory.getSizeInventory(), itemsList.size()); i++)
        {
            addItemToInv(itemsList.get(i), player);
        }
        return true;
    }

    private static String[] parseLine(String entry)
    {
        String[] r = { "", "1", "0", "" };
        int d1 = entry.indexOf(',');
        int d2 = entry.indexOf(',', d1 + 1);
        int d3 = entry.indexOf(',', d2 + 1);

        if (d1 != -1)
        {
            r[0] = entry.substring(0, d1);
            if (d2 != -1)
            {
                r[1] = entry.substring(d1 + 1, d2);
                if (d3 != -1)
                {
                    r[2] = entry.substring(d2 + 1, d3);
                    r[3] = entry.substring(d3 + 1);
                }
                else
                    r[2] = entry.substring(d2 + 1);
            }
            else
                r[1] = entry.substring(d1 + 1);
        }
        else
            r[0] = entry;

        return r;
    }

    private static void addItemToInv(ItemStack itemStack, EntityPlayer player)
    {
        if (itemStack != null)
        {
            player.inventory.addItemStackToInventory(itemStack.copy());
        }
    }

    private static void readItems()
    {
        if (scan != null)
        {
            itemsList.clear();

            while (scan.hasNextLine())
            {
                String[] item = parseLine(scan.nextLine());
                if (GameData.getItemRegistry().getObject(item[0]) != null)
                {
                    ItemStack itemStack = new ItemStack(GameData.getItemRegistry().getObject(item[0]), CommonUtils.parseInt(item[1]), CommonUtils.parseInt(item[2]));
                    if (!item[3].isEmpty())
                    {
                        try
                        {
                            NBTTagCompound nbt = (NBTTagCompound) JsonToNBT.func_150315_a(item[3].replace("\\n", "\n"));
                            itemStack.stackTagCompound = nbt;
                        }
                        catch (Throwable e)
                        {
                            FMLLog.log("StartingInventory", Level.ERROR, "Error parsing NBT JSON: %s", item[3]);
                            e.printStackTrace();
                        }
                    }
                    itemsList.add(itemStack);
                }
            }

            scan.close();
        }
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
        itemsList.clear();
        try
        {
            if (file.exists())
                file.delete();

            file.createNewFile();

            PrintWriter out = new PrintWriter(new FileWriter(file));

            for (ItemStack itemStack : player.inventory.mainInventory)
            {
                if (itemStack != null)
                {
                    itemsList.add(itemStack);
                }
            }

            for (ItemStack itemStack : itemsList)
            {
                String name = GameData.getItemRegistry().getNameForObject(itemStack.getItem());

                if (name != null && !name.isEmpty())
                {
                    String line = name + "," + itemStack.stackSize + "," + itemStack.getMetadata();

                    if (itemStack.hasTagCompound())
                        line += "," + itemStack.stackTagCompound.toString().replace("\n", "\\n");

                    out.println(line);
                }
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
        player.inventory.clearInventory(null, -1);
        init();
        addItems(player);
    }
}
