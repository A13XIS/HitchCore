package com.hitchh1k3rsguide.$CORE_REPLACE$.hitchcore;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

public class ResourceReloadListener implements IResourceManagerReloadListener
{

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        CoreConfig.update(true);
    }

}
