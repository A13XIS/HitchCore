package com.hitchh1k3rsguide.$CORE_REPLACE$.hitchcore;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class HitchCommand extends CommandBase
{

    public static final int OP_LEVEL = 2;

    @Override
    public String getName()
    {
        return "hitch";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.hitch.usage";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 0)
        {
            if ("help".equals(args[0]))
            {
                ChatComponentTranslation header = new ChatComponentTranslation("commands.hitch.help");
                header.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
                sender.addChatMessage(header);
                sender.addChatMessage(new ChatComponentTranslation("commands.hitch.help.help"));
                sender.addChatMessage(new ChatComponentTranslation("commands.hitch.help.config"));
                sender.addChatMessage(new ChatComponentTranslation("commands.hitch.help.changes"));
                return;
            }
            else if ("config".equals(args[0]))
            {
                if (args.length > 2)
                {
                    boolean isClient = false;
                    boolean isServer = false;
                    if ("server".equals(args[1]))
                    {
                        isServer = true;
                    }
                    else if ("client".equals(args[1]))
                    {
                        isClient = true;
                    }
                    if (!isClient || sender instanceof EntityPlayerMP)
                    {
                        if ((isServer || isClient) && args.length < 4)
                        {
                            throw new WrongUsageException("commands.hitch.help.config");
                        }
                        for (CoreConfig.ModConfigOption configOption : CoreConfig.configOptions)
                        {
                            if ((configOption.category + ":" + configOption.name).equalsIgnoreCase(args[1 + ((isClient || isServer) ? 1 : 0)]))
                            {
                                if ((configOption.configType == CoreConfig.CONFIG_COMMON) ||
                                    (!isServer && configOption.configType == CoreConfig.CONFIG_CLIENT) ||
                                    (!isClient && configOption.configType == CoreConfig.CONFIG_SYNCHED))
                                {
                                    Object value = deserializeString(configOption.type, args[2 + ((isClient || isServer) ? 1 : 0)]);
                                    if (value == null)
                                    {
                                        throw new CommandException("commands.hitch.config.valueError", args[1 + ((isClient || isServer) ? 1 : 0)]);
                                    }
                                    if ((isClient || (!isServer && configOption.configType != CoreConfig.CONFIG_SYNCHED)) && sender instanceof EntityPlayerMP)
                                    {
                                        HitchCore.netWrapper.sendTo(new MessageCoreClientConfig(configOption.category, configOption.name, configOption.type.getID(), value), (EntityPlayerMP) sender);
                                        sender.addChatMessage(new ChatComponentTranslation("commands.hitch.config.notify", args[1 + (isClient ? 1 : 0)], stringify(value)));
                                        return;
                                    }
                                    else if (sender.canUseCommand(OP_LEVEL, "hitch"))
                                    {
                                        CoreConfig.setProperty(configOption.category, configOption.name, value);
                                        notifyOperators(sender, this, "commands.hitch.config.notify", args[1 + (isServer ? 1 : 0)], stringify(value));
                                        return;
                                    }
                                    else
                                    {
                                        throw new CommandException("commands.hitch.config.operatorError");
                                    }
                                }
                            }
                        }
                    }
                    throw new CommandException("commands.hitch.config.propertyError");
                }
                else
                {
                    throw new WrongUsageException("commands.hitch.help.config");
                }
            }
            else if ("changes".equals(args[0]))
            {
                if (sender instanceof EntityPlayerMP)
                {
                    HitchCore.netWrapper.sendTo(new MessageCoreGeneric(MessageCoreGeneric.MESSAGE_TYPE.PRINT_CHANGES), (EntityPlayerMP) sender);
                }
                else
                {
                    CoreUtils.printChanges(sender);
                }
                return;
            }
        }

        throw new WrongUsageException("commands.hitch.usage");
    }

    private String stringify(Object value)
    {
        if (value instanceof boolean[])
        {
            return Arrays.toString((boolean[]) value);
        }
        else if (value instanceof double[])
        {
            return Arrays.toString((double[]) value);
        }
        else if (value instanceof int[])
        {
            return Arrays.toString((int[]) value);
        }
        else if (value instanceof String[])
        {
            return Arrays.toString((String[]) value);
        }
        else
        {
            return value.toString();
        }
    }

    private Object deserializeString(CoreConfig.ConfigType type, String data)
    {
        Object r = null;
        try
        {
            String[] chunks;
            switch (type)
            {
                case BOOLEAN:
                    if (data.equalsIgnoreCase("true") || data.equalsIgnoreCase("1"))
                    {
                        r = true;
                    }
                    else if (data.equalsIgnoreCase("false") || data.equalsIgnoreCase("0"))
                    {
                        r = false;
                    }
                    break;
                case BOOLEAN_ARRAY:
                    chunks = data.split(",");
                    r = new boolean[chunks.length];
                    for (int i = 0; i < chunks.length && r != null; ++i)
                    {
                        if (chunks[i].equalsIgnoreCase("true") || chunks[i].equalsIgnoreCase("1"))
                        {
                            ((boolean[]) r)[i] = true;
                        }
                        else if (chunks[i].equalsIgnoreCase("false") || chunks[i].equalsIgnoreCase("0"))
                        {
                            ((boolean[]) r)[i] = false;
                        }
                        else
                        {
                            r = null;
                        }
                    }
                    break;
                case DOUBLE:
                    r = Double.valueOf(data);
                    break;
                case DOUBLE_ARRAY:
                    chunks = data.split(",");
                    r = new double[chunks.length];
                    for (int i = 0; i < chunks.length; ++i)
                    {
                        ((double[]) r)[i] = Double.valueOf(chunks[i]);
                    }
                    break;
                case INTEGER:
                    r = Integer.valueOf(data);
                    break;
                case INTEGER_ARRAY:
                    chunks = data.split(",");
                    r = new int[chunks.length];
                    for (int i = 0; i < chunks.length; ++i)
                    {
                        ((int[]) r)[i] = Integer.valueOf(chunks[i]);
                    }
                    break;
                case STRING:
                    r = String.valueOf(data);
                    break;
                case STRING_ARRAY:
                    chunks = data.split(",");
                    r = new String[chunks.length];
                    for (int i = 0; i < chunks.length; ++i)
                    {
                        ((String[]) r)[i] = String.valueOf(chunks[i]);
                    }
                    break;
            }
        }
        catch (Exception ignored) {}
        return r;
    }

    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "help", "config", "changes");
        }
        else if ("config".equals(args[0]))
        {
            if (args.length == 2)
            {
                return getListOfStringsMatchingLastWord(args, CoreConfig.getConfigOptions(null, sender, "client", "server"));
            }
            else if (args.length == 3 && ("client".equals(args[1]) || "server".equals(args[1])))
            {
                return getListOfStringsMatchingLastWord(args, CoreConfig.getConfigOptions(args[1], sender));
            }
            else
            {
                CoreConfig.ModConfigOption option = null;
                if (args.length == 4 && ("client".equals(args[1]) || "server".equals(args[1])))
                {
                    option = CoreConfig.getOption(args[2]);
                }
                else if (args.length == 3)
                {
                    option = CoreConfig.getOption(args[1]);
                }
                if (option != null && (option.type == CoreConfig.ConfigType.BOOLEAN || option.type == CoreConfig.ConfigType.BOOLEAN_ARRAY))
                {
                    return getListOfStringsMatchingLastWord(args, "true", "false");
                }
            }
        }

        return null;

    }

}