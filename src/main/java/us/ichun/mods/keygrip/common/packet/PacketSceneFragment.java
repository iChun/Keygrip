package us.ichun.mods.keygrip.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import us.ichun.mods.ichunutil.common.core.packet.PacketDataFragment;
import us.ichun.mods.keygrip.common.Keygrip;

public class PacketSceneFragment extends PacketDataFragment
{
    public int startPoint = 0;

    public PacketSceneFragment(){}

    public PacketSceneFragment(int startPoint, String fileName, int packetTotal, int packetNumber, int fragmentSize, byte[] data)
    {
        super(fileName, packetTotal, packetNumber, fragmentSize, data);
        this.startPoint = startPoint;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        super.writeTo(buffer, side);
        buffer.writeInt(startPoint);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        super.readFrom(buffer, side);
        startPoint = buffer.readInt();
    }

    @Override
    public void execution(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            Keygrip.proxy.tickHandlerServer.receiveProjectData(player.dimension, startPoint, fileName, packetTotal, packetNumber, data);
        }
    }
}
