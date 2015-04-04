package us.ichun.mods.keygrip.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import us.ichun.mods.keygrip.client.gui.GuiWorkspace;
import us.ichun.mods.keygrip.common.Keygrip;

public class PacketSceneStatus extends AbstractPacket
{
    public int startPoint;
    public String sceneName;
    public boolean playing;

    public PacketSceneStatus(){}

    public PacketSceneStatus(int start, String name, boolean play)
    {
        startPoint = start;
        sceneName = name;
        playing = play;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeInt(startPoint);
        ByteBufUtils.writeUTF8String(buffer, sceneName);
        buffer.writeBoolean(playing);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        startPoint = buffer.readInt();
        sceneName = ByteBufUtils.readUTF8String(buffer);
        playing = buffer.readBoolean();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        handleClient();
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        GuiWorkspace workspace = Keygrip.proxy.tickHandlerClient.workspace;
        if(workspace.hasOpenScene() && workspace.getOpenScene().identifier.equals(sceneName))
        {
            workspace.getOpenScene().playing = playing;
            workspace.timeline.timeline.setCurrentPos(startPoint);
            workspace.timeline.timeline.focusOnTicker();
            if(playing && Keygrip.proxy.tickHandlerClient.sceneFrom != null && sceneName.equals(Keygrip.proxy.tickHandlerClient.sceneFrom.identifier) && Keygrip.config.playbackSceneWhileRecording == 1 && Keygrip.proxy.tickHandlerClient.actionToRecord != null)
            {
                Keygrip.proxy.tickHandlerClient.startRecord = true;
            }
        }
    }
}
