package me.ichun.mods.keygrip.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.keygrip.common.Keygrip;
import me.ichun.mods.keygrip.common.scene.Scene;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PacketStopScene extends AbstractPacket
{
    public String sceneIdent;

    public PacketStopScene() {}

    public PacketStopScene(String s)
    {
        sceneIdent = s;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, sceneIdent);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        sceneIdent = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        for(int i = Keygrip.eventHandlerServer.scenesToPlay.size() - 1; i >= 0; i--)
        {
            Scene scene = Keygrip.eventHandlerServer.scenesToPlay.get(i);
            if(scene.identifier.equals(sceneIdent))
            {
                scene.stop();
                scene.destroy();
                Keygrip.eventHandlerServer.scenesToPlay.remove(i);

                Keygrip.channel.sendToDimension(new PacketSceneStatus(scene.playTime, sceneIdent, false), player.dimension);
            }
        }
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return Side.SERVER;
    }
}
