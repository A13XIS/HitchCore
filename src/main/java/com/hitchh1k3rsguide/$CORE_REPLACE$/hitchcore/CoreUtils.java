package com.hitchh1k3rsguide.$CORE_REPLACE$.hitchcore;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.InjectedModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class CoreUtils
{

    public static final String MESSAGE_CORE_HANDLER     = "core_handler";
    public static final String MESSAGE_ADD_SHARED_ITEM  = "add_shared_item";
    public static final String MESSAGE_CORE_CONFIG      = "core_config";
    public static final String MESSAGE_ADD_CONFIG       = "add_config";
    public static final String MESSAGE_SET_CONFIG       = "set_config";
    public static final String MESSAGE_FIRST_RENDERABLE = "first_renderable";

    public static final String URL_API_VERSION   = "http://api.hitchh1k3rsguide.com/minecraft/%s/version";
    public static final String URL_API_CHANGELOG = "http://api.hitchh1k3rsguide.com/minecraft/%s/change/%s";

    public static Map<String, Object> devHandlers = new HashMap<String, Object>();

    private static Object modHandler;
    private static Object coreHandler;
    private static Method coreMethod;

    public static boolean DEDICATED_SERVER = false;

    private static HashMap<Object, Method> handlers = new HashMap<Object, Method>();

    public static void tellMods(String key, Object value, Object exception)
    {
        for (Object handler : handlers.keySet())
        {
            if (exception == null || exception != handler)
            {
                tellMod(handler, key, value);
            }
        }
    }

    public static void tellMod(Object handler, String key, Object value)
    {
        try
        {
            handlers.get(handler).invoke(handler, coreHandler, key, value);
        }
        catch (Exception ignored) {}
    }

    public static void tellCore(String key, Object value)
    {
        if (coreMethod != null)
        {
            try
            {
                coreMethod.invoke(coreHandler, getModHandler(), key, value);
            }
            catch (Exception ignored) {}
        }
    }

    public static void addHandler(String className)
    {
        try
        {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getDeclaredMethod("getMessage", Object.class, String.class, Object.class);
            Object handler = clazz.newInstance();
            handlers.put(handler, method);
            tellMod(handler, MESSAGE_CORE_HANDLER, coreHandler);
        }
        catch (Exception ignored) {}
    }

    public static void setCore(Object core)
    {
        try
        {
            Class<?> clazz = core.getClass();
            Method method = clazz.getDeclaredMethod("getMessage", Object.class, String.class, Object.class);
            coreHandler = core;
            coreMethod = method;
        }
        catch (Exception ignored) {}
    }

    public static void setHandlers(Object core, Object mod)
    {
        if (!FMLForgePlugin.RUNTIME_DEOBF)
        {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            for (StackTraceElement trace : stack)
            {
                String name = trace.getClassName();
                if (name.startsWith("com.hitchh1k3rsguide.") && !name.contains("$CORE_REPLACE$"))
                {
                    devHandlers.put(name.substring(21, name.indexOf('.', 21)), mod);
                    break;
                }
            }
        }
        modHandler = mod;
        try
        {
            Class<?> clazz = core.getClass();
            Method method = clazz.getDeclaredMethod("getMessage", Object.class, String.class, Object.class);
            coreHandler = core;
            coreMethod = method;
        }
        catch (Exception ignored) {}
    }

    public static void addConfigOption(Object handler, String category, String name, String description, CoreConfig.ConfigType type, Object defaultValue, int configType)
    {
        tellCore(MESSAGE_ADD_CONFIG, new Object[]{ handler, category, name, description, type.getID(), defaultValue, configType });
    }

    public static Object getModHandler()
    {
        if (!FMLForgePlugin.RUNTIME_DEOBF)
        {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            for (StackTraceElement trace : stack)
            {
                String name = trace.getClassName();
                if (name.startsWith("com.hitchh1k3rsguide.") && !name.contains("$CORE_REPLACE$"))
                {
                    String mod = name.substring(21, name.indexOf('.', 21));
                    if (devHandlers.containsKey(mod))
                    {
                        return devHandlers.get(mod);
                    }
                }
            }
        }
        return modHandler;
    }

    public static void printChanges(ICommandSender sender)
    {
        if (!HitchCore.hasCheckedForUpdate)
        {
            HitchCore.checkForUpdates();
        }
        ArrayList<Tuple> lines = new ArrayList<Tuple>();
        IChatComponent finalMessage = null;
        if (HitchCore.hasUpdates)
        {
            for (String mod : HitchCore.currentVersions.keySet())
            {
                if (HitchCore.versions.get(mod).compareTo(HitchCore.currentVersions.get(mod)) < 0)
                {
                    HitchCore.changeLogs.put(mod, apiGetChangeLog(mod));
                }
            }
            for (String mod : HitchCore.currentVersions.keySet())
            {
                if (HitchCore.versions.get(mod).compareTo(HitchCore.currentVersions.get(mod)) < 0)
                {
                    String modName = localize("itemGroup." + mod);
                    lines.add(new Tuple(HitchCore.MODID + ".update.change.list", new Object[]{ modName, HitchCore.versions.get(mod).verStr, HitchCore.currentVersions.get(mod).verStr }));
                    for (String line : HitchCore.changeLogs.get(mod))
                    {
                        lines.add(new Tuple(null, "\u00A78 - " + line));
                    }
                    finalMessage = new ChatComponentText(localize(HitchCore.MODID + ".update.change.download") + " ");
                    ChatComponentText link = new ChatComponentText(HitchCore.UPDATE_URL);
                    link.getChatStyle().setColor(EnumChatFormatting.DARK_AQUA);
                    link.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, HitchCore.UPDATE_URL));
                    finalMessage.appendSibling(link);
                }
            }
        }
        else
        {
            lines.add(new Tuple("hitchcore.update.none", new Object[0]));
        }

        if (sender == null)
        {
            for (Tuple line : lines)
            {
                if (line.getFirst() != null)
                {
                    tellLocalPlayer(localize((String) line.getFirst(), (Object[]) line.getSecond()));
                }
                else
                {
                    tellLocalPlayer((String) line.getSecond());
                }
            }
            if (finalMessage != null)
            {
                tellLocalPlayer(finalMessage);
            }
        }
        else
        {
            for (Tuple line : lines)
            {
                if (line.getFirst() != null)
                {
                    sender.addChatMessage(new ChatComponentText(localize((String) line.getFirst(), (Object[]) line.getSecond())));
                }
                else
                {
                    if (DEDICATED_SERVER)
                    {
                        sender.addChatMessage(new ChatComponentText(((String) line.getSecond()).replaceAll("\u00A7.", "")));
                    }
                    else
                    {
                        sender.addChatMessage(new ChatComponentText((String) line.getSecond()));
                    }
                }
            }
            if (finalMessage != null)
            {
                sender.addChatMessage(finalMessage);
            }
        }
    }

    public static String[] apiGetChangeLog(String mod)
    {
        String data = readURL(String.format(URL_API_CHANGELOG, mod, HitchCore.versions.get(mod).verStr));
        if (!"".equals(data) && !"BAD REQUEST".equals(data))
        {
            return data.split("\\n");
        }
        return new String[0];
    }

    public static VersionInfo apiGetVersion(String mod)
    {
        String data = readURL(String.format(URL_API_VERSION, mod));
        if (!"".equals(data) && !"BAD REQUEST".equals(data))
        {
            try
            {
                return new VersionInfo(data);
            }
            catch (Exception ignored) {}
        }
        return HitchCore.versions.get(mod);
    }

    private static Map<String, String> lang;

    public static void generateServerLocale(Set<String> mods)
    {
        Splitter splitter = Splitter.on('=').limit(2);
        Pattern pattern = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");

        lang = new HashMap<String, String>();
        for (String mod : mods)
        {
            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(CoreUtils.class.getClassLoader().getResourceAsStream("assets/" + mod + "/lang/en_US.lang"), "UTF-8"));
                String s;
                while ((s = reader.readLine()) != null)
                {
                    if (!s.isEmpty() && s.charAt(0) != 35)
                    {
                        String[] astring = Iterables.toArray(splitter.split(s), String.class);

                        if (astring != null && astring.length == 2)
                        {
                            String s1 = astring[0];
                            String s2 = pattern.matcher(astring[1]).replaceAll("%$1s").replaceAll("\u00A7.", "");
                            lang.put(s1, s2);
                        }
                    }
                }
            }
            catch (Exception ignored) {}
        }
    }

    public static String localize(String key, Object... inserts)
    {
        if (DEDICATED_SERVER)
        {
            if (lang.containsKey(key))
            {
                return String.format(lang.get(key), inserts);
            }
        }
        else
        {
            return StatCollector.translateToLocalFormatted(key, inserts);
        }
        return "";
    }

    public static void debugErr(String message)
    {
        if (CoreConfig.debugMode)
        {
            HitchCore.LOGGER.error(message);
        }
    }

    public static void debugMsg(String message)
    {
        if (CoreConfig.debugMode)
        {
            HitchCore.LOGGER.info(message);
        }
    }

    public static String readURL(String url)
    {
        String ret = "";
        try
        {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            String preFix = "";
            while ((inputLine = in.readLine()) != null)
            {
                ret += preFix + inputLine;
                preFix = "\n";
            }
            connection.disconnect();
            in.close();
        }
        catch (Exception e)
        {
            if (CoreConfig.debugMode)
            {
                CoreUtils.debugErr("Failed to read URL (" + url + ")");
                e.printStackTrace();
            }
        }
        return ret.replaceAll("\\[color-(.)\\]", "\u00A7$1");
    }

    public static void tellLocalPlayer(IChatComponent message)
    {
        if (!CoreUtils.DEDICATED_SERVER && Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null)
        {
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(message);
        }
        else
        {
            HitchCore.LOGGER.info(message.getUnformattedText().replaceAll("\u00A7.", ""));
        }
    }

    public static void tellLocalPlayer(String message)
    {
        tellLocalPlayer(new ChatComponentText(message));
    }

    public static void updateAlert()
    {
        if (!HitchCore.hasCheckedForUpdate)
        {
            HitchCore.checkForUpdates();
        }
        if (HitchCore.hasUpdates)
        {
            for (String mod : HitchCore.currentVersions.keySet())
            {
                if (HitchCore.versions.get(mod).compareTo(HitchCore.currentVersions.get(mod)) < 0)
                {
                    tellLocalPlayer(localize(HitchCore.MODID + ".update.nag", localize("itemGroup." + mod), HitchCore.versions.get(mod).verStr, HitchCore.currentVersions.get(mod).verStr));
                }
            }
            tellLocalPlayer(localize(HitchCore.MODID + ".update.change.prompt"));
        }
    }

    public static void failedTransform(String modname, String version, String patchFailures, int failureStatus, Logger logger, String modid)
    {
        String mods = "Minecraft\u00A0(" + Loader.MC_VERSION + ")";
        for (ModContainer mod : Loader.instance().getModList())
        {
            if (mod instanceof InjectedModContainer)
            {
                mods += ", " + mod.getName().replace(' ', '\u00A0') + '\u00A0' + "(" + mod.getVersion() + ")";
            }
        }

        String err = "<b>" + modname + " (" + version + ")</b> failed to apply patches.<br />One or more of these are incompatible:<br /><em>" + mods + "</em><br /><br />"
                     + "Remove <b>" + modname + "</b> from your mods folder and check <a href=\"http://bit.ly/hitchmods\">http://bit.ly/hitchmods</a> for an update!<br /><br />"
                     + "<b>Failed Patches:</b>"
                     + patchFailures
                     + "<br /><br />" + (failureStatus == 1 ? "No critical errors detected, proceed at your own risk." : "Critical errors detected, loading cannot proceed.");

        for (String line : err.split("<br \\/>"))
        {
            logger.error(line.replaceAll("<.*?>", ""));
        }

        if (FMLCommonHandler.instance().getSide().isServer())
        {
            if (failureStatus > 1 || !Boolean.parseBoolean(System.getProperty(modid + ".ignorePatchFailure", "false")))
            {
                FMLCommonHandler.instance().exitJava(1, true);
            }
            logger.error("To try running with the failed patches restart the server with the JVM argument: -D" + modid + ".ignorePatchFailure=true");
        }
        else if (!Boolean.parseBoolean(System.getProperty(modid + ".ignorePatchFailure", "false")))
        {
            JEditorPane ep = new JEditorPane(
                    "text/html",
                    "<html><div style=\"width: 640px;\">"
                    + err
                    + "</div></html>");
            ep.setEditable(false);
            ep.setOpaque(false);
            ep.addHyperlinkListener(new HyperlinkListener()
            {

                @Override
                public void hyperlinkUpdate(HyperlinkEvent event)
                {
                    try
                    {
                        if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                        {
                            Desktop.getDesktop().browse(event.getURL().toURI());
                        }
                    }
                    catch (Exception ignored)
                    {
                    }
                }

            });

            String[] options;
            if (failureStatus == 1)
            {
                options = new String[]{ "Quit", "Try Anyway" };
            }
            else
            {
                options = new String[]{ "Quit" };
            }
            JOptionPane optionPane = new JOptionPane(ep, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
            optionPane.selectInitialValue();
            JDialog dialog = optionPane.createDialog("Patching Error");
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
            dialog.dispose();
            if (optionPane.getValue() == options[0])
            {
                FMLCommonHandler.instance().exitJava(1, true);
            }
        }
        logger.error("Ignoring patch failures...");
    }

    public static boolean isOre(ItemStack stack, String oreName)
    {
        int target = OreDictionary.getOreID(oreName);
        for (int i : OreDictionary.getOreIDs(stack))
        {
            if (i == target)
            {
                return true;
            }
        }
        return false;
    }

    public static ItemStack idToStack(int id, int stackSize)
    {
        return new ItemStack(Item.getItemById(id >> 16), stackSize, (id & 65535));
    }

    public static int stackToId(ItemStack stack)
    {
        return (Item.getIdFromItem(stack.getItem()) << 16) + stack.getMetadata();
    }

}
