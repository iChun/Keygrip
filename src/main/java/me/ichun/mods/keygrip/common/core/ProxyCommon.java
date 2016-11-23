package me.ichun.mods.keygrip.common.core;

import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.keygrip.client.core.ResourceHelper;
import me.ichun.mods.keygrip.common.Keygrip;
import me.ichun.mods.keygrip.common.packet.PacketSceneFragment;
import me.ichun.mods.keygrip.common.packet.PacketSceneStatus;
import me.ichun.mods.keygrip.common.packet.PacketStopScene;
import me.ichun.mods.keygrip.common.packet.PacketToggleSleeping;
import net.minecraftforge.common.MinecraftForge;

public class ProxyCommon
{
    public void preInit()
    {
        ResourceHelper.init();

        Keygrip.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(Keygrip.eventHandlerServer);

        Keygrip.channel = new PacketChannel("Keygrip", PacketSceneFragment.class, PacketStopScene.class, PacketSceneStatus.class, PacketToggleSleeping.class);
    }
}
