package us.ichun.mods.keygrip.common.scene.action;

import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.RandomStringUtils;
import us.ichun.mods.ichunutil.common.core.EntityHelperBase;
import us.ichun.mods.ichunutil.common.core.util.IOUtil;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.ichunutil.common.network.FakeNetHandlerPlayServer;
import us.ichun.mods.keygrip.common.Keygrip;
import us.ichun.mods.keygrip.common.packet.PacketToggleSleeping;
import us.ichun.mods.keygrip.common.scene.Scene;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Action
        implements Comparable
{
    public static final int VERSION = 1;
    //Used names = ac, al, ap, i, k, n, nbt, p, s, t, v

    @SerializedName("n")
    public String name;
    @SerializedName("i")
    public String identifier;
    @SerializedName("t")
    public String entityType;
    @SerializedName("k")
    public int startKey;
    @SerializedName("nbt")
    public byte[] nbtToRead;
    @SerializedName("v")
    public int version = VERSION;
    @SerializedName("p")
    public int precreateEntity;
    @SerializedName("P")
    public int persistEntity;
    @SerializedName("h")
    public int hidden;

    @SerializedName("s")
    public int[] offsetPos = new int[3];
    @SerializedName("r")
    public int[] rotation = new int[2];

    @SerializedName("ac")
    public TreeMap<Integer, ArrayList<ActionComponent>> actionComponents = new TreeMap<Integer, ArrayList<ActionComponent>>(Ordering.natural());
    @SerializedName("al")
    public TreeMap<Integer, LimbComponent> lookComponents = new TreeMap<Integer, LimbComponent>(Ordering.natural());
    @SerializedName("ap")
    public TreeMap<Integer, LimbComponent> posComponents = new TreeMap<Integer, LimbComponent>(Ordering.natural());

    public transient EntityState state;

    public Action(String name, String type, int startKey, NBTTagCompound tag, boolean preCreate, boolean persist) // name of action, entity type (player::<NAME> or entity class name), start key for action, NBT Tag if player, pre-create the player.
    {
        this.identifier = RandomStringUtils.randomAscii(ProjectInfo.IDENTIFIER_LENGTH);
        update(name, type, startKey, tag, preCreate, persist);
    }

    public void update(String name, String type, int startKey, NBTTagCompound tag, boolean preCreate, boolean persist)
    {
        this.name = name;
        this.entityType = type;
        this.startKey = startKey;
        if(tag != null)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try
            {
                CompressedStreamTools.writeCompressed(tag, baos);
                nbtToRead = baos.toByteArray();
            }
            catch(IOException ioexception)
            {
            }
        }
        precreateEntity = preCreate ? 1 : 0;
        persistEntity = persist ? 1 : 0;
    }

    public int getLength()
    {
        int l = 0;
        for(Map.Entry<Integer, ArrayList<ActionComponent>> e : actionComponents.entrySet())
        {
            if(l < e.getKey())
            {
                l = e.getKey();
            }
        }
        for(Map.Entry<Integer, LimbComponent> e : posComponents.entrySet())
        {
            if(l < e.getKey())
            {
                l = e.getKey();
            }
        }
        for(Map.Entry<Integer, LimbComponent> e : lookComponents.entrySet())
        {
            if(l < e.getKey())
            {
                l = e.getKey();
            }
        }
        return l;
    }

    public void doAction(Scene scene, int time)
    {
        if(state != null && state.ent != null)
        {
//            state.ent.worldObj.setBlockState(new BlockPos(state.ent.posX, state.ent.posY + state.ent.getEyeHeight(), state.ent.posZ), Blocks.torch.getDefaultState(), 3);
            ArrayList<ActionComponent> act = actionComponents.get(time);
            if(act != null)
            {
                for(ActionComponent comp : act)
                {
                    switch(comp.toggleType)
                    {
                        case 1:
                        {
                            state.useItem = !state.useItem;
                            if(state.ent instanceof EntityPlayer)
                            {
                                EntityPlayer player = (EntityPlayer)state.ent;
                                if(state.useItem && state.ent.getHeldItem() != null)
                                {
                                    player.setItemInUse(state.ent.getHeldItem(), state.ent.getHeldItem().getMaxItemUseDuration());
                                }
                                else
                                {
                                    player.stopUsingItem();
                                }
                            }
                            break;
                        }
                        case 2:
                        {
                            state.sprinting = !state.sprinting;
                            state.ent.setSprinting(state.sprinting);
                            break;
                        }
                        case 3:
                        {
                            state.sneaking = !state.sneaking;
                            state.ent.setSneaking(state.sneaking);
                            break;
                        }
                        case 4:
                        {
                            state.ent.swingItem();
                            MovingObjectPosition mop = EntityHelperBase.getEntityLook(state.ent, 4);
                            if(mop != null && mop.typeOfHit.equals(MovingObjectPosition.MovingObjectType.ENTITY))
                            {
                                EntityHelperBase.attackEntityWithItem(state.ent, mop.entityHit);
                            }
                            break;
                        }
                        case 5:
                        {
                            state.health = comp.itemAction / (float)Scene.PRECISION;
                            state.ent.setHealth(state.health);
                            state.ent.hurtTime = comp.itemNBT[0];
                            state.ent.deathTime = comp.itemNBT[1];
                            if(state.ent.hurtTime > 0)
                            {
                                if(state.ent instanceof EntityPlayer)
                                {
                                    state.ent.playSound(state.ent.deathTime > 0 ? "game.player.die" : "game.player.hurt", 1.0F, (state.ent.getRNG().nextFloat() - state.ent.getRNG().nextFloat()) * 0.2F + 1.0F);
                                }
                                FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayersInDimension(new S19PacketEntityStatus(state.ent, (byte)(state.ent.deathTime > 0 ? 3 : 2)), state.ent.dimension);
                            }
                            break;
                        }
                        case 6:
                        {
                            state.fire = !state.fire;
                            if(state.fire)
                            {
                                state.ent.setFire(24000);
                                state.ent.getDataWatcher().updateObject(0, Byte.valueOf((byte)(state.ent.getDataWatcher().getWatchableObjectByte(0) | 1 << 0)));
                            }
                            else
                            {
                                state.ent.extinguish();
                                state.ent.getDataWatcher().updateObject(0, Byte.valueOf((byte)(state.ent.getDataWatcher().getWatchableObjectByte(0) & ~(1 << 0))));
                            }
                            break;
                        }
                        case 7:
                        {
                            state.sleeping = !state.sleeping;
                            if(state.ent instanceof EntityPlayer)
                            {
                                EntityPlayer player = (EntityPlayer)state.ent;
                                if(state.sleeping)
                                {
                                    player.setSize(0.2F, 0.2F);

                                    player.sleeping = true;
                                    player.sleepTimer = 0;
                                    player.playerLocation = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
                                    player.motionX = player.motionZ = player.motionY = 0.0D;
                                }
                                else
                                {
                                    player.setSize(0.6F, 1.8F);

                                    player.sleeping = false;
                                    player.sleepTimer = 0;
                                }
                                Keygrip.channel.sendToDimension(new PacketToggleSleeping(player.getEntityId(), state.sleeping, comp.itemAction), player.dimension);
                            }
                            break;
                        }
                        case 0:
                        {
                            if(comp.itemAction > 0)
                            {
                                if(comp.itemAction <= 5)
                                {
                                    if(comp.itemNBT != null)
                                    {
                                        try
                                        {
                                            state.ent.setCurrentItemOrArmor(comp.itemAction - 1, ItemStack.loadItemStackFromNBT(CompressedStreamTools.readCompressed(new ByteArrayInputStream(comp.itemNBT))));
                                        }
                                        catch(IOException ignored)
                                        {
                                        }
                                    }
                                    else
                                    {
                                        state.ent.setCurrentItemOrArmor(comp.itemAction - 1, null);
                                    }
                                    if(state.ent instanceof EntityPlayer)
                                    {
                                        FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayersInDimension(new S04PacketEntityEquipment(state.ent.getEntityId(), comp.itemAction - 1, state.ent.getEquipmentInSlot(comp.itemAction - 1)), ((EntityPlayer)state.ent).dimension);
                                    }
                                }
                                else if(comp.itemAction == 6 && comp.itemNBT != null)
                                {
                                    ItemStack stack = null;
                                    try
                                    {
                                        stack = ItemStack.loadItemStackFromNBT(CompressedStreamTools.readCompressed(new ByteArrayInputStream(comp.itemNBT)));
                                    }
                                    catch(IOException ignored)
                                    {

                                    }
                                    if(stack != null)
                                    {
                                        double d0 = state.ent.posY - 0.30000001192092896D + (double)state.ent.getEyeHeight();
                                        EntityItem entityitem = new EntityItem(state.ent.worldObj, state.ent.posX, d0, state.ent.posZ, stack);
                                        entityitem.setPickupDelay(40);

                                        entityitem.setThrower(state.ent.getCommandSenderName());

                                        float f;
                                        float f1;

                                        f = 0.3F;
                                        entityitem.motionX = (double)(-MathHelper.sin(state.ent.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(state.ent.rotationPitch / 180.0F * (float)Math.PI) * f);
                                        entityitem.motionZ = (double)(MathHelper.cos(state.ent.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(state.ent.rotationPitch / 180.0F * (float)Math.PI) * f);
                                        entityitem.motionY = (double)(-MathHelper.sin(state.ent.rotationPitch / 180.0F * (float)Math.PI) * f + 0.1F);
                                        f1 = state.ent.getRNG().nextFloat() * (float)Math.PI * 2.0F;
                                        f = 0.02F * state.ent.getRNG().nextFloat();
                                        entityitem.motionX += Math.cos((double)f1) * (double)f;
                                        entityitem.motionY += (double)((state.ent.getRNG().nextFloat() - state.ent.getRNG().nextFloat()) * 0.1F);
                                        entityitem.motionZ += Math.sin((double)f1) * (double)f;

                                        state.ent.worldObj.spawnEntityInWorld(entityitem);

                                        state.additionalEnts.add(entityitem);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
            LimbComponent comp = lookComponents.get(time);
            if(comp != null)
            {
                state.rot[0] = comp.actionChange[0] / (double)Scene.PRECISION;
                state.rot[1] = comp.actionChange[1] / (double)Scene.PRECISION;
            }
            LimbComponent comp1 = posComponents.get(time);
            if(comp1 != null)
            {
                for(int i = 0; i < 3; i++)
                {
                    state.pos[i] = (comp1.actionChange[i] + (offsetPos[i] + scene.startPos[i])) / (double)Scene.PRECISION;
                }
            }
            state.ent.motionX = state.ent.motionY = state.ent.motionZ = 0.0D;
            state.ent.setPosition(state.pos[0], state.pos[1], state.pos[2]);
            state.ent.rotationYawHead = state.ent.rotationYaw = (float)state.rot[0];
            state.ent.rotationPitch = (float)state.rot[1];

            if(state.ent instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer)state.ent;
                if (player.getHealth() > 0.0F && !player.isSpectator())
                {
                    AxisAlignedBB axisalignedbb = null;

                    if (player.ridingEntity != null && !player.ridingEntity.isDead)
                    {
                        axisalignedbb = player.getEntityBoundingBox().union(player.ridingEntity.getEntityBoundingBox()).expand(1.0D, 0.0D, 1.0D);
                    }
                    else
                    {
                        axisalignedbb = player.getEntityBoundingBox().expand(1.0D, 0.5D, 1.0D);
                    }

                    List list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, axisalignedbb);

                    for (int i = 0; i < list.size(); ++i)
                    {
                        Entity entity = (Entity)list.get(i);

                        if (!entity.isDead)
                        {
                            entity.onCollideWithPlayer(player);
                        }
                    }
                }

                if(state.useItem)
                {
                    if (player.itemInUse != null)
                    {
                        ItemStack itemstack = player.inventory.getCurrentItem();

                        if (itemstack == player.itemInUse)
                        {
                            player.itemInUseCount = net.minecraftforge.event.ForgeEventFactory.onItemUseTick(player, player.itemInUse, player.itemInUseCount);
                            if (player.itemInUseCount <= 0)
                            {
                                player.onItemUseFinish();
                            }
                            else
                            {
                                player.itemInUse.getItem().onUsingTick(player.itemInUse, player, player.itemInUseCount); //Forge Added
                                if (player.itemInUseCount <= 25 && player.itemInUseCount % 4 == 0)
                                {
                                    player.updateItemUse(itemstack, 5);
                                }

                                if (--player.itemInUseCount == 0 && !player.worldObj.isRemote)
                                {
                                    player.onItemUseFinish();
                                }
                            }
                        }
                        else
                        {
                            player.clearItemInUse();
                        }
                    }
                }
            }
        }
    }

    public boolean createState(WorldServer world, double x, double y, double z)
    {
        if(state == null || state.ent == null)
        {
            try
            {
                EntityPlayerMP playerDummy = new FakePlayer(world, EntityHelperBase.getSimpleGameProfileFromName("ForgeDev"));
                NBTTagCompound tag = CompressedStreamTools.readCompressed(new ByteArrayInputStream(this.nbtToRead));
                playerDummy.readFromNBT(tag);
                playerDummy.setLocationAndAngles(x, y, z, playerDummy.rotationYaw, playerDummy.rotationPitch);
                playerDummy.writeToNBT(tag);
                this.state = new EntityState(playerDummy);

                if(this.entityType.startsWith("player::"))
                {
                    this.state.ent = new FakePlayer(world, EntityHelperBase.getFullGameProfileFromName(this.entityType.substring("player::".length())));
                    this.state.ent.readFromNBT(tag);
                    new FakeNetHandlerPlayServer(FMLCommonHandler.instance().getMinecraftServerInstance(), new NetworkManager(EnumPacketDirection.CLIENTBOUND), (FakePlayer)this.state.ent);
                    FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayers(new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.ADD_PLAYER, (FakePlayer)this.state.ent));
                    state.ent.getDataWatcher().updateObject(10, (byte)127);
                }
                else
                {
                    this.state.ent = (EntityLivingBase)Class.forName(this.entityType).getConstructor(World.class).newInstance(world);
                    this.state.ent.setSprinting(playerDummy.isSprinting());
                    this.state.ent.setSneaking(playerDummy.isSneaking());
                    for(int i = 0; i < state.ent.getInventory().length; i++)
                    {
                        this.state.ent.setCurrentItemOrArmor(i, playerDummy.getEquipmentInSlot(i));
                    }
                }

                if(state.ent instanceof EntityLiving)
                {
                    ((EntityLiving)state.ent).setNoAI(true);
                    ((EntityLiving)state.ent).tasks.taskEntries.clear();
                    ((EntityLiving)state.ent).targetTasks.taskEntries.clear();
                }
                return true;
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public int compareTo(Object arg0)
    {
        if(arg0 instanceof Action)
        {
            Action comp = (Action)arg0;
            return Integer.compare(startKey, comp.startKey);
        }
        return 0;
    }

    public static Action openAction(File file)
    {
        try
        {
            byte[] data = new byte[(int)file.length()];
            FileInputStream stream = new FileInputStream(file);
            stream.read(data);
            stream.close();

            Action scene = (new Gson()).fromJson(IOUtil.decompress(data), Action.class);

            return scene;
        }
        catch(IOException ignored)
        {
            ignored.printStackTrace();
        }
        return null;
    }
}
