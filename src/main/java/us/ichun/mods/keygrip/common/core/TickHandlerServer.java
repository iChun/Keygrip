package us.ichun.mods.keygrip.common.core;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.io.FileUtils;
import us.ichun.mods.keygrip.client.core.ResourceHelper;
import us.ichun.mods.keygrip.common.Keygrip;
import us.ichun.mods.keygrip.common.packet.PacketSceneStatus;
import us.ichun.mods.keygrip.common.scene.Scene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TickHandlerServer
{
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase.equals(TickEvent.Phase.START))
        {
            for(int i = scenesToPlay.size() - 1; i >= 0; i--)
            {
                Scene scene = scenesToPlay.get(i);
                scene.update();
                if(scene.playTime > scene.getLength() + 10)
                {
                    scene.stop();
                    scene.destroy();
                    scenesToPlay.remove(i);

                    Keygrip.channel.sendToAll(new PacketSceneStatus(scene.playTime, scene.identifier, false));
                }
            }
        }
    }

    public void receiveProjectData(int dimension, int startPoint, String sceneIdent, short packetTotal, short packetNumber, byte[] data)
    {
        ArrayList<byte[]> byteArray = sceneParts.get(sceneIdent);
        if(byteArray == null)
        {
            byteArray = new ArrayList<byte[]>();

            sceneParts.put(sceneIdent, byteArray);

            for(int i = 0; i < packetTotal; i++)
            {
                byteArray.add(new byte[0]);
            }
        }

        byteArray.set(packetNumber, data);

        boolean hasAllInfo = true;

        for(int i = 0; i < byteArray.size(); i++)
        {
            byte[] byteList = byteArray.get(i);
            if(byteList.length == 0)
            {
                hasAllInfo = false;
            }
        }

        if(hasAllInfo)
        {
            int size = 0;

            for(byte[] aByteArray : byteArray)
            {
                size += aByteArray.length;
            }

            byte[] bytes = new byte[size];

            int index = 0;

            for(int i = 0; i < byteArray.size(); i++)
            {
                System.arraycopy(byteArray.get(i), 0, bytes, index, byteArray.get(i).length);
                index += byteArray.get(i).length;
            }

            //At this point, bytes has the full data. Do something with it.

            File temp = new File(ResourceHelper.getTempDir(), Integer.toString(Math.abs(sceneIdent.hashCode())) + "-received.kgs");

            try
            {
                FileUtils.writeByteArrayToFile(temp, bytes);
                if(temp.exists())
                {
                    Scene scene = Scene.openScene(temp);

                    if(scene != null)
                    {
                        for(int i = scenesToPlay.size() - 1; i >= 0; i--)
                        {
                            Scene scener = scenesToPlay.get(i);
                            if(scener.identifier.equals(scene.identifier))
                            {
                                scener.stop();
                                scener.destroy();
                                scenesToPlay.remove(i);
                            }
                        }

                        scene.playing = true;
                        scene.playTime = startPoint;

                        scenesToPlay.add(scene);

                        Keygrip.channel.sendToDimension(new PacketSceneStatus(startPoint, sceneIdent, true), dimension);

                        scene.create(DimensionManager.getWorld(dimension));
                    }

                    temp.delete();
                }
            }
            catch(IOException ignored)
            {
                ignored.printStackTrace();
            }

            sceneParts.remove(sceneIdent);
        }
    }

    public HashMap<String, ArrayList<byte[]>> sceneParts = new HashMap<String, ArrayList<byte[]>>();
    public ArrayList<Scene> scenesToPlay = new ArrayList<Scene>();
}
