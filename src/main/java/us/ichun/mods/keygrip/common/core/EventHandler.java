package us.ichun.mods.keygrip.common.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.ichun.mods.ichunutil.client.keybind.KeyEvent;
import us.ichun.mods.keygrip.client.gui.GuiWorkspace;
import us.ichun.mods.keygrip.common.Keygrip;
import us.ichun.mods.keygrip.common.packet.PacketStopScene;
import us.ichun.mods.keygrip.common.scene.Scene;

public class EventHandler
{
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyEvent(KeyEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.keyBind.isPressed())
        {
            if(mc.theWorld != null)
            {
                if((mc.currentScreen == null || mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiWorkspace))
                {
                    if(event.keyBind.equals(Keygrip.config.toggleSceneRecorder))
                    {
                        if(mc.currentScreen instanceof GuiWorkspace)
                        {
                            mc.displayGuiScreen(null);
                            mc.setIngameFocus();
                        }
                        else
                        {
                            if(Keygrip.proxy.tickHandlerClient.workspace == null)
                            {
                                Keygrip.proxy.tickHandlerClient.workspace = new GuiWorkspace(1);
                            }
                            int oriScale = mc.gameSettings.guiScale;
                            mc.gameSettings.guiScale = mc.gameSettings.guiScale == 1 ? 1 : 2;

                            Keygrip.proxy.tickHandlerClient.workspace.oriScale = oriScale;

                            mc.displayGuiScreen(Keygrip.proxy.tickHandlerClient.workspace);
                        }
                    }
                    else if(Keygrip.proxy.tickHandlerClient.workspace != null)
                    {
                        if(event.keyBind.equals(Keygrip.config.startStopRecord))
                        {
                            Keygrip.proxy.tickHandlerClient.workspace.toggleRecording();
                        }
                        else if(event.keyBind.equals(Keygrip.config.toggleScenePlayback) && Keygrip.proxy.tickHandlerClient.workspace.hasOpenScene())
                        {
                            if(Keygrip.proxy.tickHandlerClient.workspace.getOpenScene().playing)
                            {
                                Keygrip.proxy.tickHandlerClient.workspace.getOpenScene().stop();

                                Keygrip.channel.sendToServer(new PacketStopScene(Keygrip.proxy.tickHandlerClient.workspace.getOpenScene().identifier));
                            }
                            else
                            {
                                if(Keygrip.proxy.tickHandlerClient.workspace.sceneSendingCooldown <= 0)
                                {
                                    if(Keygrip.proxy.tickHandlerClient.workspace.timeline.timeline.getCurrentPos() > Keygrip.proxy.tickHandlerClient.workspace.getOpenScene().getLength())
                                    {
                                        Keygrip.proxy.tickHandlerClient.workspace.timeline.timeline.setCurrentPos(0);
                                    }
                                    if(GuiScreen.isCtrlKeyDown())
                                    {
                                        Minecraft.getMinecraft().displayGuiScreen(null);
                                        Minecraft.getMinecraft().setIngameFocus();
                                    }
                                    Scene.sendSceneToServer(Keygrip.proxy.tickHandlerClient.workspace.getOpenScene());
                                }
                                Keygrip.proxy.tickHandlerClient.workspace.sceneSendingCooldown = 10;
                            }
                        }
                    }
                }
            }
        }
    }
}
