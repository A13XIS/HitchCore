package com.hitchh1k3rsguide.$CORE_REPLACE$.hitchcore;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.InjectedModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
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
import java.util.*;
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
                coreMethod.invoke(coreHandler, modHandler, key, value);
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
                if (HitchCore.versions.get(mod) < HitchCore.currentVersions.get(mod))
                {
                    HitchCore.changeLogs.put(mod, apiGetChangeLog(mod));
                }
            }
            for (String mod : HitchCore.currentVersions.keySet())
            {
                if (HitchCore.versions.get(mod) < HitchCore.currentVersions.get(mod))
                {
                    String modName = localize("itemGroup." + mod);
                    lines.add(new Tuple(HitchCore.MODID + ".update.change.list", new Object[]{ modName, intToVersion(HitchCore.versions.get(mod)), intToVersion(HitchCore.currentVersions.get(mod)) }));
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
                    sender.addChatMessage(new ChatComponentText((String) line.getSecond()));
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
        String data = readURL(String.format(URL_API_CHANGELOG, mod, intToVersion(HitchCore.versions.get(mod))));
        if (!"".equals(data) && !"BAD REQUEST".equals(data))
        {
            return data.split("\\n");
        }
        return new String[0];
    }

    public static int apiGetVersion(String mod)
    {
        String data = readURL(String.format(URL_API_VERSION, mod));
        if (!"".equals(data) && !"BAD REQUEST".equals(data))
        {
            try
            {
                return versionToInt(data);
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
                Iterator<String> it = reader.lines().iterator();
                while (it.hasNext())
                {
                    String s = it.next();

                    if (!s.isEmpty() && s.charAt(0) != 35)
                    {
                        String[] astring = Iterables.toArray(splitter.split(s), String.class);

                        if (astring != null && astring.length == 2)
                        {
                            String s1 = astring[0];
                            String s2 = pattern.matcher(astring[1]).replaceAll("%$1s").replaceAll("\\u00A7.", "");
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

    public static int versionToInt(String version)
    {
        String[] v = version.split("\\.");
        int[] vi = new int[3];
        for (int i = 0; i < v.length; ++i)
        {
            vi[i] = Integer.parseInt(v[i]);
        }
        return (vi[0] * 10000) + (vi[1] * 100) + (vi[2]);
    }

    public static String intToVersion(int version)
    {
        int ver = version / 10000;
        int maj = (version - (ver * 10000)) / 100;
        int min = version - (ver * 10000) - (maj * 100);
        return ver + "." + maj + (min > 0 ? "." + min : "");
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
            HitchCore.LOGGER.info(message.getUnformattedText().replaceAll("\\u00A7.", ""));
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
                if (HitchCore.versions.get(mod) < HitchCore.currentVersions.get(mod))
                {
                    tellLocalPlayer(localize(HitchCore.MODID + ".update.nag", localize("itemGroup." + mod), intToVersion(HitchCore.versions.get(mod)), intToVersion(HitchCore.currentVersions.get(mod))));
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
            logger.error("To try running with the failed patches restart the server with the JVM argument: -Dmakersmark.ignorePatchFailure=true");
            if (failureStatus > 1 || !Boolean.parseBoolean(System.getProperty(modid + ".ignorePatchFailure", "false")))
            {
                FMLCommonHandler.instance().exitJava(1, true);
            }
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

}
