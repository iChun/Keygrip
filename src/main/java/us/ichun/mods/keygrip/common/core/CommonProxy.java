package us.ichun.mods.keygrip.common.core;

import net.minecraftforge.fml.common.FMLCommonHandler;
import us.ichun.mods.ichunutil.common.core.network.ChannelHandler;
import us.ichun.mods.ichunutil.common.core.network.PacketChannel;
import us.ichun.mods.keygrip.client.core.ResourceHelper;
import us.ichun.mods.keygrip.client.core.TickHandlerClient;
import us.ichun.mods.keygrip.common.Keygrip;
import us.ichun.mods.keygrip.common.packet.PacketSceneFragment;
import us.ichun.mods.keygrip.common.packet.PacketSceneStatus;
import us.ichun.mods.keygrip.common.packet.PacketStopScene;
import us.ichun.mods.keygrip.common.packet.PacketToggleSleeping;

public class CommonProxy
{
    public void preInit()
    {
        ResourceHelper.init();

        tickHandlerServer = new TickHandlerServer();
        FMLCommonHandler.instance().bus().register(tickHandlerServer);

        Keygrip.channel = ChannelHandler.getChannelHandlers("Keygrip", PacketSceneFragment.class, PacketStopScene.class, PacketSceneStatus.class, PacketToggleSleeping.class);
    }

    public void init(){}
    public void postInit(){}

    public TickHandlerClient tickHandlerClient;
    public TickHandlerServer tickHandlerServer;
}
