package me.ichun.mods.keygrip.client.core;

import me.ichun.mods.keygrip.common.Keygrip;
import me.ichun.mods.keygrip.common.core.ProxyCommon;
import net.minecraftforge.common.MinecraftForge;

public class ProxyClient extends ProxyCommon
{
    @Override
    public void preInit()
    {
        super.preInit();

        Keygrip.eventHandlerClient = new EventHandlerClient();
        MinecraftForge.EVENT_BUS.register(Keygrip.eventHandlerClient);
    }
}
