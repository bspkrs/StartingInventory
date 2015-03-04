package bspkrs.startinginventory;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.GameData;

import org.apache.logging.log4j.Level;

import bspkrs.util.CommonUtils;

public class StartingInventory
{
    private static String                  fileName         = "startingInventory.txt";
    private static String                  configPath       = "/config/";
    private static final String            NEW_LINE_STR     = "<_newline_>";
    private static final String            NEW_LINE_ALT_STR = ">newline<";
    private static File                    file             = new File(new File(CommonUtils.getMinecraftDir()), configPath + fileName);
    private static Scanner                 scan;
    private static Map<Integer, ItemStack> itemMap          = new TreeMap<Integer, ItemStack>();
    private static List<ItemStack>         itemList         = new ArrayList<ItemStack>();
    private final static String[]          defaultItems     = {
                                                            "# Lines that begin with [<index>] will be loaded into the inventory slot at the given index.",
                                                            "# Indexes for the vanilla inventory are:",
                                                            "#     0-8  : player hotbar",
                                                            "#     9-35 : main inventory",
                                                            "#     36-39: armor inventory in the order boots, leggings, chestplate, helmet",
                                                            "# If a given index is out of range the mod will attempt to load that item into the first available inventory slot after adding all other items.",
                                                            "[36]minecraft:leather_boots, 1", "[37]minecraft:leather_leggings, 1", "[38]minecraft:leather_chestplate, 1",
                                                            "[39]minecraft:leather_chestplate, 1", "minecraft:stone_pickaxe, 1", "minecraft:stone_shovel, 1",
                                                            "minecraft:stone_sword, 1", "minecraft:stone_axe, 1", "minecraft:apple, 16", "minecraft:torch, 16"
                                                            };

    public static void init(EntityPlayer player)
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
        readItems(player);
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
        for (Entry<Integer, ItemStack> e : itemMap.entrySet())
        {
            player.inventory.setInventorySlotContents(e.getKey(), e.getValue());
        }
        for (ItemStack itemStack : itemList)
        {
            player.inventory.addItemStackToInventory(itemStack);
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

    private static void readItems(EntityPlayer player)
    {
        if (scan != null)
        {
            itemList.clear();
            itemMap.clear();

            while (scan.hasNextLine())
            {
                int slot = -1;
                String line = scan.nextLine();

                if (line.startsWith("#"))
                    continue;

                if (line.startsWith("["))
                {
                    try
                    {
                        slot = Integer.parseInt(line.substring(1, line.indexOf("]")).trim());
                        line = line.substring(line.indexOf("]") + 1);
                    }
                    catch (Throwable e)
                    {
                        FMLLog.log("StartingInventory", Level.ERROR, "Error parsing item entry: %s", line);
                        e.printStackTrace();
                        continue;
                    }
                }

                String[] item = parseLine(line);
                if (GameData.getItemRegistry().getObject(item[0]) != null)
                {
                    ItemStack itemStack = new ItemStack(GameData.getItemRegistry().getObject(item[0]), CommonUtils.parseInt(item[1]), CommonUtils.parseInt(item[2]));
                    if (!item[3].isEmpty())
                    {
                        try
                        {
                            NBTTagCompound nbt = JsonToNBT.getTagFromJson(item[3].replace("\n", "\\n").replace(NEW_LINE_STR, "\n").replace(NEW_LINE_ALT_STR, NEW_LINE_STR));
                            itemStack.setTagCompound(nbt);
                        }
                        catch (Throwable e)
                        {
                            FMLLog.log("StartingInventory", Level.ERROR, "Error parsing NBT JSON: %s", item[3]);
                            e.printStackTrace();
                            continue;
                        }
                    }
                    if ((slot > -1) && (slot < (player != null ? player.inventory.getSizeInventory() : 40)))
                        itemMap.put(slot, itemStack);
                    else
                        itemList.add(itemStack);
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
        itemMap.clear();
        itemList.clear();
        try
        {
            if (file.exists())
                file.delete();

            file.createNewFile();

            PrintWriter out = new PrintWriter(new FileWriter(file));

            for (int i = 0; i < player.inventory.getSizeInventory(); i++)
            {
                ItemStack itemStack = player.inventory.getStackInSlot(i);
                if (itemStack != null)
                {
                    itemMap.put(i, itemStack);

                    String name = GameData.getItemRegistry().getNameForObject(itemStack.getItem()).toString();

                    if ((name != null) && !name.isEmpty())
                    {
                        String line = "[" + i + "]" + name + "," + itemStack.stackSize + "," + itemStack.getMetadata();

                        if (itemStack.hasTagCompound())
                            line += "," + itemStack.getTagCompound().toString().replace(NEW_LINE_STR, NEW_LINE_ALT_STR).replace("\n", NEW_LINE_STR);

                        out.println(line);
                    }
                }
            }

            out.close();

            scan = new Scanner(file);

        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        readItems(player);
    }

    protected static void loadInventoryFromConfigFile(EntityPlayer player)
    {
        player.inventory.clear();
        init(player);
        addItems(player);
    }
}
