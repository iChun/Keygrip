package us.ichun.mods.keygrip.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import us.ichun.mods.keygrip.common.Keygrip;

public class PacketToggleSleeping extends AbstractPacket
{
    public int id;
    public boolean state;
    public int face;

    public PacketToggleSleeping(){}

    public PacketToggleSleeping(int id, boolean state, int face)
    {
        this.id = id;
        this.state = state;
        this.face = face;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeInt(id);
        buffer.writeBoolean(state);
        buffer.writeInt(face);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        id = buffer.readInt();
        state = buffer.readBoolean();
        face = buffer.readInt();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        handleClient();
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        Entity ent = Minecraft.getMinecraft().theWorld.getEntityByID(id);
        if(ent instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer)ent;
            player.sleeping = state;
            player.sleepTimer = 0;

            player.renderOffsetX = 0.0F;
            player.renderOffsetZ = 0.0F;

            if(state)
            {
                if(face == EnumFacing.SOUTH.ordinal())
                {
                    player.renderOffsetZ = -1.8F;
                }
                else if(face == EnumFacing.NORTH.ordinal())
                {
                    player.renderOffsetZ = 1.8F;
                }
                else if(face == EnumFacing.WEST.ordinal())
                {
                    player.renderOffsetX = 1.8F;
                }
                else if(face == EnumFacing.EAST.ordinal())
                {
                    player.renderOffsetX = -1.8F;
                }
                if(!Keygrip.proxy.tickHandlerClient.sleepers.contains(player))
                {
                    Keygrip.proxy.tickHandlerClient.sleepers.add(player);
                }
            }
            else
            {
                player.playerLocation = null;
                Keygrip.proxy.tickHandlerClient.sleepers.remove(player);
            }
        }
    }
}
