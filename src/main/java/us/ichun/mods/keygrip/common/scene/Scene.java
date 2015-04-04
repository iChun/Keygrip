package us.ichun.mods.keygrip.common.scene;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import us.ichun.mods.ichunutil.client.gui.window.WindowPopup;
import us.ichun.mods.ichunutil.common.core.EntityHelperBase;
import us.ichun.mods.ichunutil.common.core.util.IOUtil;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.keygrip.client.core.ResourceHelper;
import us.ichun.mods.keygrip.client.gui.GuiWorkspace;
import us.ichun.mods.keygrip.common.Keygrip;
import us.ichun.mods.keygrip.common.packet.PacketSceneFragment;
import us.ichun.mods.keygrip.common.scene.action.Action;
import us.ichun.mods.keygrip.common.scene.action.EntityState;
import us.ichun.mods.keygrip.common.scene.action.LimbComponent;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class Scene
{
    public static final transient int VERSION = 1;
    public static final transient int PRECISION = 1000;
    //Used names = a, i, n, v

    @SerializedName("n")
    public String name;
    @SerializedName("i")
    public String identifier;
    @SerializedName("v")
    public int version = VERSION;

    @SerializedName("a")
    public ArrayList<Action> actions = new ArrayList<Action>();//sort the list after
    @SerializedName("s")
    public int[] startPos = new int[3];

    public transient File saveFile;
    public transient String saveFileMd5;

    public transient int playTime;
    public transient boolean playing;

    public transient WorldServer server;

    public Scene(String name)
    {
        this.name = name;
        this.identifier = RandomStringUtils.randomAscii(ProjectInfo.IDENTIFIER_LENGTH);
    }

    public void update()
    {
        if(playing)
        {
            if(server != null)
            {
                for(Action a : actions)
                {
                    if(a.getLength() > 0)
                    {
                        if(playTime > a.startKey + a.getLength() || playTime < a.startKey || a.hidden == 1)
                        {
                            if(playTime == 5 && a.precreateEntity == 1 && a.state != null && a.state.ent != null)
                            {
                                FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayersInDimension(new S18PacketEntityTeleport(a.state.ent), a.state.ent.worldObj.provider.getDimensionId());
                            }
                            continue;
                        }
                        if(playTime == a.startKey)
                        {
                            if(a.precreateEntity != 1)
                            {
                                if(a.createState(server, (startPos[0] + a.offsetPos[0]) / (double)PRECISION, (startPos[1] + a.offsetPos[1]) / (double)PRECISION, (startPos[2] + a.offsetPos[2]) / (double)PRECISION))
                                {
                                    if(a.state.ent == null)
                                    {
                                        Keygrip.console("Error initializing action: " + a.name, true);
                                    }
                                    else
                                    {
                                        a.state.ent.setLocationAndAngles((startPos[0] + a.offsetPos[0]) / (double)PRECISION, (startPos[1] + a.offsetPos[1]) / (double)PRECISION, (startPos[2] + a.offsetPos[2]) / (double)PRECISION, a.rotation[0] / (float)PRECISION, a.rotation[1] / (float)PRECISION);
                                        server.spawnEntityInWorld(a.state.ent);
                                        if(a.state.ent instanceof EntityPlayer)
                                        {
                                            server.playerEntities.remove(a.state.ent);
                                            server.updateAllPlayersSleepingFlag();
                                        }
                                    }
                                }
                            }
                        }
                        else if(playTime == a.startKey + a.getLength() && a.persistEntity != 1 && a.state != null && a.state.ent != null)
                        {
                            a.state.ent.setDead();
                            for(Entity ent : a.state.additionalEnts)
                            {
                                ent.setDead();
                            }
                            a.state = null;
                        }
                        a.doAction(this, playTime);
                        if(playTime - a.startKey == 5 && a.state != null && a.state.ent != null && a.precreateEntity != 1)
                        {
                            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayersInDimension(new S18PacketEntityTeleport(a.state.ent), a.state.ent.worldObj.provider.getDimensionId());
                        }
                    }
                }
            }
            playTime++;
        }
    }

    public int getLength()
    {
        int l = 0;
        for(Action a : actions)
        {
            if(a.startKey + a.getLength() > l)
            {
                l = a.startKey + a.getLength();
            }
        }
        return l;
    }

    public void create(WorldServer world)
    {
        server = world;
        for(Action a : actions)
        {
            if(playTime > a.startKey + a.getLength() && a.persistEntity != 1 || a.hidden == 1)
            {
                continue;
            }
            if(playTime < a.startKey && a.precreateEntity == 1 || playTime >= a.startKey)
            {
                //create the related entity
                if(a.createState(world, (startPos[0] + a.offsetPos[0]) / (double)PRECISION, (startPos[1] + a.offsetPos[1]) / (double)PRECISION, (startPos[2] + a.offsetPos[2]) / (double)PRECISION))
                {
                    if(a.state.ent == null)
                    {
                        Keygrip.console("Error initializing action: " + a.name, true);
                    }
                    else
                    {
                        a.state.ent.setLocationAndAngles((startPos[0] + a.offsetPos[0]) / (double)PRECISION, (startPos[1] + a.offsetPos[1]) / (double)PRECISION, (startPos[2] + a.offsetPos[2]) / (double)PRECISION, a.rotation[0] / (float)PRECISION, a.rotation[1] / (float)PRECISION);

                        world.spawnEntityInWorld(a.state.ent);
                        if(a.state.ent instanceof EntityPlayer)
                        {
                            world.playerEntities.remove(a.state.ent);
                            world.updateAllPlayersSleepingFlag();
                        }

                        LimbComponent lastLook = null;
                        LimbComponent lastPos = null;

                        int lastLookInt = -1;
                        for(Map.Entry<Integer, LimbComponent> e : a.lookComponents.entrySet())
                        {
                            if(e.getKey() > lastLookInt && e.getKey() < playTime)
                            {
                                lastLookInt = e.getKey();
                                lastLook = e.getValue();
                            }
                        }
                        int lastPosInt = -1;
                        for(Map.Entry<Integer, LimbComponent> e : a.posComponents.entrySet())
                        {
                            if(e.getKey() > lastPosInt && e.getKey() < playTime)
                            {
                                lastPosInt = e.getKey();
                                lastPos = e.getValue();
                            }
                        }
                        if(lastLook != null)
                        {
                            a.state.ent.rotationYawHead = a.state.ent.rotationYaw = lastLook.actionChange[0] / Scene.PRECISION;
                            a.state.ent.rotationPitch = lastLook.actionChange[1] / Scene.PRECISION;
                        }
                        if(lastPos != null)
                        {
                            a.state.ent.setLocationAndAngles((lastPos.actionChange[0] + (a.offsetPos[0] + startPos[0])) / (double)PRECISION, (lastPos.actionChange[1] + (a.offsetPos[1] + startPos[1])) / (double)PRECISION, (lastPos.actionChange[2] + (a.offsetPos[2] + startPos[2])) / (double)PRECISION, a.state.ent.rotationYaw, a.state.ent.rotationPitch);
                        }
                    }
                }
            }
        }
    }

    public void play()
    {
        playing = true;
    }

    public void stop()
    {
        for(Action a : actions)
        {
            if(a.state != null && a.state.ent != null)
            {
                a.state.ent.setDead();
                for(Entity ent : a.state.additionalEnts)
                {
                    ent.setDead();
                }
            }
        }
        playing = false;
    }

    public void destroy()
    {
        for(Action a : actions)
        {
            if(a.state != null && a.state.ent != null)
            {
                a.state.ent.setDead();
                for(Entity ent : a.state.additionalEnts)
                {
                    ent.setDead();
                }
            }
        }
    }

    public void repair()
    {
        //not needed yet, save files are still first edition.
    }

    public static Scene openScene(File file)
    {
        try
        {
            //            InputStream con = new FileInputStream(file);
            //            String data = new String(ByteStreams.toByteArray(con));
            //            con.close();
            //
            //            Scene scene = (new Gson()).fromJson(data, Scene.class);
            //
            //            scene.saveFile = (file);
            //            scene.saveFileMd5 = IOUtil.getMD5Checksum(file);
            //            scene.repair();
            //
            //            return scene;

            byte[] data = new byte[(int)file.length()];
            FileInputStream stream = new FileInputStream(file);
            stream.read(data);
            stream.close();

            Scene scene = (new Gson()).fromJson(IOUtil.decompress(data), Scene.class);

            scene.saveFile = (file);
            scene.saveFileMd5 = IOUtil.getMD5Checksum(file);
            scene.repair();

            return scene;
        }
        catch(IOException ignored){ignored.printStackTrace();}
        return null;
    }

    public static boolean saveScene(Scene scene, File file)
    {
        try
        {
            //            FileUtils.writeStringToFile(file, (new Gson()).toJson(scene));

            FileOutputStream stream = new FileOutputStream(file);
            stream.write(IOUtil.compress((new Gson()).toJson(scene)));
            stream.close();

            return true;
        }
        catch(IOException ignored)
        {
        }
        return false;
    }

    public static void saveSceneActions(Scene scene)
    {
        ArrayList<String> actNames = new ArrayList<String>();
        for(Action action : scene.actions)
        {
            try
            {
                String name = action.name;
                int append = 0;
                while(actNames.contains(name))
                {
                    if(append != 0)
                    {
                        name = name.substring(0, name.length() - 2);
                    }
                    append++;
                    name = name + "_" + append;
                }
                FileOutputStream stream = new FileOutputStream(new File(ResourceHelper.getActionsDir(), scene.name + "-" + name + ".kga"));
                stream.write(IOUtil.compress((new Gson()).toJson(action)));
                stream.close();
                actNames.add(name);
            }
            catch(IOException ignored)
            {
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void sendSceneToServer(Scene scene)
    {
        File temp = new File(ResourceHelper.getTempDir(), Integer.toString(Math.abs(scene.hashCode())) + "-send.kgs");

        if(Scene.saveScene(scene, temp))
        {
            try
            {
                byte[] data = IOUtils.toByteArray(new FileInputStream(temp));

                final int maxFile = 31000; //smaller packet cause I'm worried about too much info carried over from the bloat vs hat info.

                int fileSize = data.length;

                int packetsToSend = (int)Math.ceil((float)fileSize / (float)maxFile);

                int packetCount = 0;
                int offset = 0;
                while(fileSize > 0)
                {
                    byte[] fileBytes = new byte[fileSize > maxFile ? maxFile : fileSize];
                    int index = 0;
                    while(index < fileBytes.length) //from index 0 to 31999
                    {
                        fileBytes[index] = data[index + offset];
                        index++;
                    }

                    int time = 0;

                    if(Minecraft.getMinecraft().currentScreen instanceof GuiWorkspace)
                    {
                        //open popup
                        GuiWorkspace workspace = (GuiWorkspace)Minecraft.getMinecraft().currentScreen;
                        time = workspace.timeline.timeline.getCurrentPos();
                        if(time > scene.getLength())
                        {
                            time = 0;
                        }
                    }

                    Keygrip.channel.sendToServer(new PacketSceneFragment(time, scene.identifier, packetsToSend, packetCount, fileSize > maxFile ? maxFile : fileSize, fileBytes));

                    packetCount++;
                    fileSize -= maxFile;
                    offset += index;
                }
            }
            catch(IOException ignored){}

            temp.delete();
        }
        else if(Minecraft.getMinecraft().currentScreen instanceof GuiWorkspace)
        {
            //open popup
            GuiWorkspace workspace = (GuiWorkspace)Minecraft.getMinecraft().currentScreen;
            workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "window.playScene.failed").putInMiddleOfScreen());
        }
    }
}
