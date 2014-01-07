package bspkrs.startinginventory;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;

public class CommandStartingInv extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "startinginv";
    }
    
    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "commands.startinginv.usage";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }
    
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        if (sender instanceof EntityPlayer)
            return sender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
        
        return false;
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
            if (sender instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) sender;
                if (args[0].equalsIgnoreCase("load"))
                {
                    StartingInventory.loadInventoryFromConfigFile(player);
                    notifyAdmins(sender, "commands.startinginv.load.success");
                    return;
                }
                else if (args[0].equalsIgnoreCase("save"))
                {
                    StartingInventory.writeConfigFileFromInventory(player);
                    notifyAdmins(sender, "commands.startinginv.save.success");
                    return;
                }
            }
        }
        throw new WrongUsageException("commands.startinginv.usage");
    }
    
    @Override
    public int compareTo(Object o)
    {
        return 0;
    }
}
