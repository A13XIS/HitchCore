package com.hitchh1k3rsguide.$CORE_REPLACE$.hitchcore;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.ArrayList;

public class CoreConfig
{

    // Property for both Client and Server, but seperate (valid for no op's - client side)
    public static final int CONFIG_COMMON = 0;

    // Property for Client only (valid for no op's)
    public static final int CONFIG_CLIENT = 1;

    // Property for both Client and Server, and synched (must be op)
    public static final int CONFIG_SYNCHED = 3;

    public static Configuration config;

    public static boolean debugMode   = false;
    public static boolean updateCheck = true;

    public static ArrayList<ModConfigOption> configOptions = new ArrayList<ModConfigOption>();

    public static void init(File configFile)
    {
        config = new Configuration(configFile);
        config.load();

        ModConfigOption u = new ModConfigOption(null, "general", "updateCheck", HitchCore.MODID + ".configgui.general.updateCheck.tooltip", ConfigType.BOOLEAN.getID(), true, CONFIG_COMMON);
        ModConfigOption d = new ModConfigOption(null, "advanced", "debugMode", HitchCore.MODID + ".configgui.advanced.debugMode.tooltip", ConfigType.BOOLEAN.getID(), false, CONFIG_COMMON);
        u.coreOption = true;
        d.coreOption = true;
        configOptions.add(u);
        configOptions.add(d);

        update(false);
    }

    public static void update(boolean forceSave)
    {
        for (ModConfigOption configOption : configOptions)
        {
            if (!CoreUtils.DEDICATED_SERVER || configOption.configType != CONFIG_CLIENT)
            {
                switch (configOption.type)
                {
                    case BOOLEAN:
                        configOption.value = config.get(configOption.category, configOption.name, (Boolean) configOption.defaultValue, CoreUtils.localize(configOption.desc)).getBoolean();
                        break;
                    case BOOLEAN_ARRAY:
                        configOption.value = config.get(configOption.category, configOption.name, (boolean[]) configOption.defaultValue, CoreUtils.localize(configOption.desc)).getBooleanList();
                        break;
                    case DOUBLE:
                        configOption.value = config.get(configOption.category, configOption.name, (Double) configOption.defaultValue, CoreUtils.localize(configOption.desc)).getDouble();
                        break;
                    case DOUBLE_ARRAY:
                        configOption.value = config.get(configOption.category, configOption.name, (double[]) configOption.defaultValue, CoreUtils.localize(configOption.desc)).getDoubleList();
                        break;
                    case INTEGER:
                        configOption.value = config.get(configOption.category, configOption.name, (Integer) configOption.defaultValue, CoreUtils.localize(configOption.desc)).getInt();
                        break;
                    case INTEGER_ARRAY:
                        configOption.value = config.get(configOption.category, configOption.name, (int[]) configOption.defaultValue, CoreUtils.localize(configOption.desc)).getIntList();
                        break;
                    case STRING:
                        configOption.value = config.get(configOption.category, configOption.name, (String) configOption.defaultValue, CoreUtils.localize(configOption.desc)).getString();
                        break;
                    case STRING_ARRAY:
                        configOption.value = config.get(configOption.category, configOption.name, (String[]) configOption.defaultValue, CoreUtils.localize(configOption.desc)).getStringList();
                        break;
                }
                if (configOption.coreOption)
                {
                    CoreUtils.tellMods(CoreUtils.MESSAGE_CORE_CONFIG, new Object[]{ configOption.name, configOption.value }, null);
                }
                else
                {
                    CoreUtils.tellMod(configOption.owner, CoreUtils.MESSAGE_SET_CONFIG, new Object[]{ configOption.name, configOption.value });
                }
            }
        }


        if (config.hasChanged() || forceSave)
        {
            config.save();
        }
    }

    public static void setProperty(String category, String name, Object value)
    {
        if (value instanceof Boolean)
        {
            config.getCategory(category).get(name).set((Boolean) value);
        }
        else if (value instanceof boolean[])
        {
            config.getCategory(category).get(name).set((boolean[]) value);
        }
        else if (value instanceof Double)
        {
            config.getCategory(category).get(name).set((Double) value);
        }
        else if (value instanceof double[])
        {
            config.getCategory(category).get(name).set((double[]) value);
        }
        else if (value instanceof Integer)
        {
            config.getCategory(category).get(name).set((Integer) value);
        }
        else if (value instanceof int[])
        {
            config.getCategory(category).get(name).set((int[]) value);
        }
        else if (value instanceof String)
        {
            config.getCategory(category).get(name).set((String) value);
        }
        else if (value instanceof String[])
        {
            config.getCategory(category).get(name).set((String[]) value);
        }
        update(false);
    }

    public static void setCoreConfig(String name, Object value)
    {
        if ("debugMode".equals(name))
        {
            debugMode = (Boolean) value;
        }
        else if ("updateCheck".equals(name))
        {
            updateCheck = (Boolean) value;
        }
    }

    public static void addOption(Object[] data)
    {
        configOptions.add(new ModConfigOption(data[0], (String) data[1], (String) data[2], (String) data[3], (Integer) data[4], data[5], (Integer) data[6]));
    }

    public static String[] getConfigOptions(String scope, ICommandSender sender, String... extras)
    {
        ArrayList<String> list = new ArrayList<String>();
        for (String extra : extras)
        {
            list.add(extra);
        }
        boolean isClient = "client".equals(scope);
        boolean isServer = "server".equals(scope);
        for (ModConfigOption option : configOptions)
        {
            if ((option.configType == CONFIG_COMMON && (!isServer || sender.canUseCommand(HitchCommand.OP_LEVEL, "hitch"))) ||
                (!isServer && option.configType == CONFIG_CLIENT) ||
                (!isClient && option.configType == CONFIG_SYNCHED && sender.canUseCommand(HitchCommand.OP_LEVEL, "hitch")))
            {
                list.add(option.category + ":" + option.name);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static ModConfigOption getOption(String key)
    {
        for (ModConfigOption option : configOptions)
        {
            if ((option.category + ":" + option.name).equalsIgnoreCase(key))
            {
                return option;
            }
        }
        return null;
    }

    public static class ModConfigOption
    {

        public Object     owner;
        public String     category;
        public String     name;
        public String     desc;
        public ConfigType type;
        public Object     value;
        public Object     defaultValue;
        public int        configType;
        public boolean    coreOption;

        public ModConfigOption(Object o, String c, String n, String d, Integer t, Object v, int p)
        {
            this.owner = o;
            this.category = c;
            this.name = n;
            this.desc = d;
            this.type = ConfigType.byID(t);
            this.value = v;
            this.defaultValue = v;
            this.configType = p;
            this.coreOption = false;
        }

    }

    public enum ConfigType
    {

        BOOLEAN(0), BOOLEAN_ARRAY(1), DOUBLE(2), DOUBLE_ARRAY(3), INTEGER(4), INTEGER_ARRAY(5), STRING(6), STRING_ARRAY(7);

        private int id;

        private ConfigType(int id)
        {
            this.id = id;
        }

        public int getID()
        {
            return id;
        }

        public static ConfigType byID(int id)
        {
            for (ConfigType type : ConfigType.values())
            {
                if (type.id == id)
                {
                    return type;
                }
            }
            return null;
        }

    }

}
