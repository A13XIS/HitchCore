package com.hitchh1k3rsguide.$CORE_REPLACE$.hitchcore;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GUIConfig extends GuiConfig
{


    public GUIConfig(GuiScreen parentScreen)
    {
        super(parentScreen, getConfigElements(), HitchCore.MODID, false, false, StatCollector.translateToLocal(HitchCore.MODID + ".title"));
    }

    private static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> list = new ArrayList<IConfigElement>();
        list.add(new DummyConfigElement.DummyCategoryElement("general", HitchCore.MODID + ".configgui.general", ConfigList.class));
        for (String cat : CoreConfig.config.getCategoryNames())
        {
            if (!"general".equals(cat) && !"advanced".equals(cat))
            {
                final String toolName = CoreUtils.localize("itemGroup." + cat);
                list.add(new DummyConfigElement.DummyCategoryElement(cat, "itemGroup." + cat, ConfigList.class)
                {
                    @Override
                    public String getComment()
                    {
                        return StatCollector.translateToLocalFormatted(HitchCore.MODID + ".configgui.mod.tooltip", toolName);
                    }
                });
            }
        }
        list.add(new DummyConfigElement.DummyCategoryElement("advanced", HitchCore.MODID + ".configgui.advanced", ConfigList.class));
        return list;
    }

    public static class ConfigList extends GuiConfigEntries.CategoryEntry
    {

        public ConfigList(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
        {
            super(owningScreen, owningEntryList, configElement);
        }

        @Override
        protected GuiScreen buildChildScreen()
        {
            return new GuiConfig(this.owningScreen, getChildElements(CoreConfig.config.getCategory(configElement.getName())),
                                 this.owningScreen.modID, Configuration.CATEGORY_GENERAL, this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                                 this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart, StatCollector.translateToLocal(HitchCore.MODID + ".title"), this.name)
            {
                @Override
                public void onGuiClosed()
                {
                    super.onGuiClosed();
                    CoreConfig.update(false);
                }
            };
        }

        private List<IConfigElement> getChildElements(ConfigCategory catagory)
        {
            ConfigElement config = new ConfigElement(catagory);
            final String zone;
            if ("general".equals(catagory.getName()) || "advanced".equals(catagory.getName()))
            {
                zone = HitchCore.MODID + ".configgui." + catagory.getName() + ".";
            }
            else
            {
                zone = catagory.getName() + ".configgui.";
            }
            if (!config.isProperty())
            {
                List<IConfigElement> elements = new ArrayList<IConfigElement>();
                Iterator<Property> pI = catagory.getOrderedValues().iterator();

                while (pI.hasNext())
                {
                    ConfigElement temp = new ConfigElement(pI.next())
                    {
                        @Override
                        public String getName()
                        {
                            return I18n.format(zone + super.getName());
                        }

                        @Override
                        public String getComment()
                        {
                            return I18n.format(zone + super.getName() + ".tooltip");
                        }
                    };
                    if (temp.showInGui())
                    {
                        elements.add(temp);
                    }
                }

                return elements;
            }
            return null;
        }
    }

}
