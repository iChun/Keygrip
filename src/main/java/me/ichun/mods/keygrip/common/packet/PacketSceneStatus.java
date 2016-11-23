package me.ichun.mods.keygrip.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.keygrip.client.gui.GuiWorkspace;
import me.ichun.mods.keygrip.common.Keygrip;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSceneStatus extends AbstractPacket
{
    public int startPoint;
    public String sceneName;
    public boolean playing;

    public PacketSceneStatus() {}

    public PacketSceneStatus(int start, String name, boolean play)
    {
        startPoint = start;
        sceneName = name;
        playing = play;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        buffer.writeInt(startPoint);
        ByteBufUtils.writeUTF8String(buffer, sceneName);
        buffer.writeBoolean(playing);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        startPoint = buffer.readInt();
        sceneName = ByteBufUtils.readUTF8String(buffer);
        playing = buffer.readBoolean();
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        handleClient();
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return Side.CLIENT;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        GuiWorkspace workspace = Keygrip.eventHandlerClient.workspace;
        if(workspace.hasOpenScene() && workspace.getOpenScene().identifier.equals(sceneName))
        {
            workspace.getOpenScene().playing = playing;
            workspace.timeline.timeline.setCurrentPos(startPoint);
            workspace.timeline.timeline.focusOnTicker();
            if(playing && Keygrip.eventHandlerClient.sceneFrom != null && sceneName.equals(Keygrip.eventHandlerClient.sceneFrom.identifier) && Keygrip.config.playbackSceneWhileRecording == 1 && Keygrip.eventHandlerClient.actionToRecord != null)
            {
                Keygrip.eventHandlerClient.startRecord = true;
            }
        }
    }
}
