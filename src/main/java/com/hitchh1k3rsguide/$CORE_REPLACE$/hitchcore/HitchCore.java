package com.hitchh1k3rsguide.$CORE_REPLACE$.hitchcore;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HitchCore extends DummyModContainer implements IModHandler
{

    public static final String VERSION = "1.0.0";
    public static final String MODID   = "hitchcore";

    public static final String MESSAGE_MOD_VERSION = "mod_version";
    public static final String MESSAGE_MOD_HANDLER = "mod_handler";
    public static final String UPDATE_URL          = "http://bit.ly/hitchmods";

    public static Logger LOGGER = LogManager.getLogger("HitchH1k3r's Mods");

    public static SimpleNetworkWrapper netWrapper;

    public static Map<String, VersionInfo> versions            = new HashMap<String, VersionInfo>();
    public static Map<String, VersionInfo> currentVersions     = new HashMap<String, VersionInfo>();
    public static Map<String, String[]>    changeLogs          = new HashMap<String, String[]>();
    public static boolean                  hasCheckedForUpdate = false;
    public static boolean                  hasUpdates          = false;

    public HitchCore()
    {
        super(ModMeta.instance);
        netWrapper = new SimpleNetworkWrapper(HitchCore.MODID);
    }

    @Override
    public void getMessage(Object sender, String key, Object value)
    {
        if (key.equals(CoreUtils.MESSAGE_ADD_SHARED_ITEM))
        {
            CoreUtils.tellMods(key, value, sender);
        }
        else if (key.equals(CoreUtils.MESSAGE_ADD_CONFIG))
        {
            CoreConfig.addOption((Object[]) value);
        }
        else if (key.equals(CoreUtils.MESSAGE_FIRST_RENDERABLE))
        {
            CoreUtils.updateAlert();
        }
    }

    @Override
    public int hashCode()
    {
        return getModId().hashCode();
    }

    private static class ModMeta extends ModMetadata
    {

        public static ModMeta instance = new ModMeta();

        private ModMeta()
        {
            modId = MODID;
            name = "HitchH1k3r's Mods";
            description = "";
            url = UPDATE_URL;
            updateUrl = "";
            logoFile = "";
            version = VERSION;
            authorList = Lists.newArrayList("HitchH1k3r");
            credits = "";
            parent = "";
            screenshots = new String[]{};
        }

        public String getChildModList()
        {
            return "";
        }

    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        bus.register(this);
        return true;
    }


    @Override
    public String getGuiClassName()
    {
        return "com.hitchh1k3rsguide.$CORE_REPLACE$.hitchcore.GUIFactory";
    }

    private File configFile;

    @Subscribe
    public void construct(FMLConstructionEvent event)
    {
        CoreUtils.DEDICATED_SERVER = event.getSide().isServer();
    }

    @Subscribe
    public void preInit(FMLPreInitializationEvent event)
    {
        configFile = new File(event.getModConfigurationDirectory(), "HitchH1k3rsMods.cfg");

        CoreUtils.setCore(this);

        List<FMLInterModComms.IMCMessage> messages = FMLInterModComms.fetchRuntimeMessages(MODID);
        for (FMLInterModComms.IMCMessage mess : messages)
        {
            if (mess.key.equals(MESSAGE_MOD_VERSION))
            {
                ModContainer mod = FMLCommonHandler.instance().findContainerFor(mess.getSender());
                this.getMetadata().childMods.add(mod);
                mod.getMetadata().parentMod = this;
                versions.put(mess.getSender(), new VersionInfo(mess.getStringValue()));
            }
            else if (mess.key.equals(MESSAGE_MOD_HANDLER))
            {
                CoreUtils.addHandler(mess.getStringValue());
            }
        }

        if (CoreUtils.DEDICATED_SERVER)
        {
            CoreUtils.generateServerLocale(versions.keySet());
        }

        FMLCommonHandler.instance().bus().register(new FirstRenderableTracker());
    }

    @Subscribe
    public void init(FMLInitializationEvent event)
    {
        CoreConfig.init(configFile);

        if (CoreConfig.updateCheck)
        {
            checkForUpdates();
        }

        for (String mod : versions.keySet())
        {
            if (currentVersions.containsKey(mod) && !versions.get(mod).equals(currentVersions.get(mod)))
            {
                ModMeta.instance.description += "  §4" + CoreUtils.localize("itemGroup." + mod) + " §c(" + CoreUtils.localize(HitchCore.MODID + ".update.description", versions.get(mod).verStr, currentVersions.get(mod).verStr) + ")\n";
            }
            else
            {
                ModMeta.instance.description += "  §2" + CoreUtils.localize("itemGroup." + mod) + " §a(" + versions.get(mod).verStr + ")\n";
            }
        }

        netWrapper.registerMessage(MessageCoreGeneric.Handler.class, MessageCoreGeneric.class, 0, Side.CLIENT);
        netWrapper.registerMessage(MessageCoreGeneric.Handler.class, MessageCoreGeneric.class, 1, Side.SERVER);
        netWrapper.registerMessage(MessageCoreClientConfig.Handler.class, MessageCoreClientConfig.class, 2, Side.CLIENT);
    }

    @SideOnly(Side.CLIENT)
    @Subscribe
    public void postInit(FMLPostInitializationEvent event)
    {
        ((net.minecraft.client.resources.IReloadableResourceManager) net.minecraft.client.Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ResourceReloadListener());
    }

    public static void checkForUpdates()
    {
        hasCheckedForUpdate = true;
        for (String mod : versions.keySet())
        {
            VersionInfo currentVersion = CoreUtils.apiGetVersion(mod);
            if (currentVersion.compareTo(versions.get(mod)) > 0)
            {
                LOGGER.info(CoreUtils.localize(HitchCore.MODID + ".update.nag", CoreUtils.localize("itemGroup." + mod), versions.get(mod).verStr, currentVersion.verStr.replaceAll("\\u00A7.", "")));
                currentVersions.put(mod, currentVersion);
                hasUpdates = true;
            }
        }
        if (hasUpdates)
        {
            LOGGER.info(CoreUtils.localize(HitchCore.MODID + ".update.change.prompt").replaceAll("\\u00A7.", ""));
        }
    }

    HitchCommand hitchCommand = new HitchCommand();

    @Subscribe
    public void serverStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(hitchCommand);
    }

}
