package us.ichun.mods.keygrip.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import us.ichun.mods.keygrip.common.Keygrip;
import us.ichun.mods.keygrip.common.scene.Scene;

public class PacketStopScene extends AbstractPacket
{
    public String sceneIdent;

    public PacketStopScene(){}

    public PacketStopScene(String s)
    {
        sceneIdent = s;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, sceneIdent);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        sceneIdent = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        for(int i = Keygrip.proxy.tickHandlerServer.scenesToPlay.size() - 1; i >= 0; i--)
        {
            Scene scene = Keygrip.proxy.tickHandlerServer.scenesToPlay.get(i);
            if(scene.identifier.equals(sceneIdent))
            {
                scene.stop();
                scene.destroy();
                Keygrip.proxy.tickHandlerServer.scenesToPlay.remove(i);

                Keygrip.channel.sendToDimension(new PacketSceneStatus(scene.playTime, sceneIdent, false), player.dimension);
            }
        }
    }
}
