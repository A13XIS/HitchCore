package com.hitchh1k3rsguide.$CORE_REPLACE$.hitchcore;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FirstRenderableTracker
{

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void firstRenderable(TickEvent.ClientTickEvent event)
    {
        if (Minecraft.getMinecraft().theWorld != null)
        {
            FMLCommonHandler.instance().bus().unregister(this);
            CoreUtils.tellMods(CoreUtils.MESSAGE_FIRST_RENDERABLE, null, null);
            CoreUtils.tellCore(CoreUtils.MESSAGE_FIRST_RENDERABLE, null);
        }
    }

}
