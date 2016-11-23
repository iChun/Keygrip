package me.ichun.mods.keygrip.client.core;

import me.ichun.mods.ichunutil.client.keybind.KeyEvent;
import me.ichun.mods.ichunutil.client.render.RendererHelper;
import me.ichun.mods.keygrip.client.gui.GuiWorkspace;
import me.ichun.mods.keygrip.common.Keygrip;
import me.ichun.mods.keygrip.common.packet.PacketStopScene;
import me.ichun.mods.keygrip.common.scene.Scene;
import me.ichun.mods.keygrip.common.scene.action.Action;
import me.ichun.mods.keygrip.common.scene.action.ActionComponent;
import me.ichun.mods.keygrip.common.scene.action.EntityState;
import me.ichun.mods.keygrip.common.scene.action.LimbComponent;
import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class EventHandlerClient
{
    public final ResourceLocation txRec = new ResourceLocation("keygrip", "textures/icon/recording.png");
    public final ResourceLocation txRecPulse = new ResourceLocation("keygrip", "textures/icon/recordingPulse.png");

    public ArrayList<EntityPlayer> sleepers = new ArrayList<>();

    public GuiWorkspace workspace;

    public Action actionToRecord;
    public Scene sceneFrom;
    public int recordActionFrom;
    public int startRecordTime;
    public boolean startRecord;

    public int dimension;

    public EntityState prevState;
    public EntityState nextState;

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
                            if(Keygrip.eventHandlerClient.workspace == null)
                            {
                                Keygrip.eventHandlerClient.workspace = new GuiWorkspace(1);
                            }
                            int oriScale = mc.gameSettings.guiScale;
                            mc.gameSettings.guiScale = mc.gameSettings.guiScale == 1 ? 1 : 2;

                            Keygrip.eventHandlerClient.workspace.oriScale = oriScale;

                            mc.displayGuiScreen(Keygrip.eventHandlerClient.workspace);
                        }
                    }
                    else if(Keygrip.eventHandlerClient.workspace != null)
                    {
                        if(event.keyBind.equals(Keygrip.config.startStopRecord))
                        {
                            Keygrip.eventHandlerClient.workspace.toggleRecording();
                        }
                        else if(event.keyBind.equals(Keygrip.config.toggleScenePlayback) && Keygrip.eventHandlerClient.workspace.hasOpenScene())
                        {
                            if(Keygrip.eventHandlerClient.workspace.getOpenScene().playing)
                            {
                                Keygrip.eventHandlerClient.workspace.getOpenScene().stop();

                                Keygrip.channel.sendToServer(new PacketStopScene(Keygrip.eventHandlerClient.workspace.getOpenScene().identifier));
                            }
                            else
                            {
                                if(Keygrip.eventHandlerClient.workspace.sceneSendingCooldown <= 0)
                                {
                                    if(Keygrip.eventHandlerClient.workspace.timeline.timeline.getCurrentPos() > Keygrip.eventHandlerClient.workspace.getOpenScene().getLength())
                                    {
                                        Keygrip.eventHandlerClient.workspace.timeline.timeline.setCurrentPos(0);
                                    }
                                    if(GuiScreen.isCtrlKeyDown())
                                    {
                                        Minecraft.getMinecraft().displayGuiScreen(null);
                                        Minecraft.getMinecraft().setIngameFocus();
                                    }
                                    Scene.sendSceneToServer(Keygrip.eventHandlerClient.workspace.getOpenScene());
                                }
                                Keygrip.eventHandlerClient.workspace.sceneSendingCooldown = 10;
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if(actionToRecord != null && mc.theWorld != null)
            {
                int pX = 5;
                int pY = 5;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                RendererHelper.drawTextureOnScreen(sceneFrom.playTime < actionToRecord.startKey + startRecordTime || recordActionFrom % 40 < 20 ? txRec : txRecPulse, pX, pY, 20, 20, 100);
                GlStateManager.pushMatrix();
                float scale = 2.0F;
                GlStateManager.scale(scale, scale, scale);
                if(sceneFrom != null)
                {
                    if(sceneFrom.playTime < actionToRecord.startKey + startRecordTime)
                    {
                        mc.fontRendererObj.drawString(Integer.toString((int)(Math.ceil((actionToRecord.startKey - sceneFrom.playTime) / 20D))), (pX + 25) / scale, (pY + 2) / scale, 0xffffff, true);
                    }
                    else
                    {
                        mc.fontRendererObj.drawString(I18n.translateToLocal("window.recording"), (pX + 25) / scale, (pY + 2) / scale, 0xffffff, true);
                    }
                }
                GlStateManager.popMatrix();
            }

            for(int i = sleepers.size() - 1; i >= 0; i--)
            {
                EntityPlayer player = sleepers.get(i);
                if(player.worldObj != mc.theWorld)
                {
                    sleepers.remove(i);
                    continue;
                }
                BlockPos pos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
                if(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockBed)
                {
                    player.bedLocation = pos;
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.ClientTickEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.phase == TickEvent.Phase.START)
        {
            if(actionToRecord != null)
            {
                if(mc.theWorld == null)
                {
                    actionToRecord = null;
                    recordActionFrom = 0;
                }
                else if(dimension != mc.theWorld.provider.getDimension() || mc.thePlayer.isDead)
                {
                    workspace.toggleRecording();
                }
            }
            if(actionToRecord != null && !mc.isGamePaused() && startRecord && (Keygrip.config.playbackSceneWhileRecording == 1 && sceneFrom.playTime >= actionToRecord.startKey + startRecordTime || Keygrip.config.playbackSceneWhileRecording != 1))
            {
                ArrayList<ActionComponent> actions = new ArrayList<ActionComponent>();

                if(!nextState.dropping && Keyboard.isKeyDown(mc.gameSettings.keyBindDrop.getKeyCode()))
                {
                    //trying to drop
                    ItemStack is = mc.thePlayer.getHeldItemMainhand();
                    if(is != null)
                    {
                        byte[] tag = null;
                        NBTTagCompound nbt = new NBTTagCompound();
                        is = is.copy();
                        if(!GuiScreen.isCtrlKeyDown())
                        {
                            is.stackSize = 1;
                        }
                        is.writeToNBT(nbt);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try
                        {
                            CompressedStreamTools.writeCompressed(nbt, baos);
                            tag = baos.toByteArray();
                        }
                        catch(IOException ignored)
                        {
                        }
                        if(tag != null)
                        {
                            actions.add(new ActionComponent(0, 6, tag));
                        }
                    }
                }

                if(!actions.isEmpty())
                {
                    actionToRecord.actionComponents.put(recordActionFrom, actions);
                }
            }
        }
        else
        {
            if(!mc.isGamePaused() && !(mc.currentScreen instanceof GuiWorkspace) && workspace != null)
            {
                for(Scene scene : workspace.sceneManager.scenes)
                {
                    scene.update();
                    if(workspace.getOpenScene() == scene)
                    {
                        workspace.timeline.timeline.setCurrentPos(scene.playTime);
                        workspace.timeline.timeline.focusOnTicker();
                    }
                    workspace.sceneSendingCooldown--;
                }
            }
            if(actionToRecord != null && !mc.isGamePaused() && startRecord && (Keygrip.config.playbackSceneWhileRecording == 1 && sceneFrom.playTime >= actionToRecord.startKey + startRecordTime || Keygrip.config.playbackSceneWhileRecording != 1))
            {
                nextState = new EntityState(mc.thePlayer);

                double[] posChange = new double[] { nextState.pos[0] - ((actionToRecord.offsetPos[0] + sceneFrom.startPos[0]) / (double)Scene.PRECISION), nextState.pos[1] - ((actionToRecord.offsetPos[1] + sceneFrom.startPos[1]) / (double)Scene.PRECISION), nextState.pos[2] - ((actionToRecord.offsetPos[2] + sceneFrom.startPos[2]) / (double)Scene.PRECISION) };
                double[] rotChange = new double[] { nextState.rot[0], nextState.rot[1] };

                ArrayList<ActionComponent> actions = actionToRecord.actionComponents.containsKey(recordActionFrom) ? actionToRecord.actionComponents.get(recordActionFrom) : new ArrayList<ActionComponent>();

                for(int i = 0; i < nextState.inventory.length; i++)
                {
                    if(nextState.inventory[i] != prevState.inventory[i])
                    {
                        byte[] tag = null;
                        if(nextState.inventory[i] != null)
                        {
                            NBTTagCompound nbt = new NBTTagCompound();
                            nextState.inventory[i].writeToNBT(nbt);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            try
                            {
                                CompressedStreamTools.writeCompressed(nbt, baos);
                                tag = baos.toByteArray();
                            }
                            catch(IOException ioexception)
                            {
                            }
                        }
                        actions.add(new ActionComponent(0, i + 1, nextState.inventory[i] == null ? null : tag));
                    }
                }
                if(nextState.swinging != prevState.swinging)
                {
                    actions.add(new ActionComponent(4, 0, null));
                }
                if(nextState.useItem != prevState.useItem)
                {
                    actions.add(new ActionComponent(1, 0, null));
                }
                if(nextState.sprinting != prevState.sprinting)
                {
                    actions.add(new ActionComponent(2, 0, null));
                }
                if(nextState.sneaking != prevState.sneaking)
                {
                    actions.add(new ActionComponent(3, 0, null));
                }
                if(nextState.health != prevState.health)
                {
                    actions.add(new ActionComponent(5, (int)Math.round(nextState.health * Scene.PRECISION), new byte[] { (byte)nextState.hurtTime, (byte)nextState.deathTime }));
                }
                if(nextState.fire != prevState.fire)
                {
                    actions.add(new ActionComponent(6, 0, null));
                }
                if(nextState.sleeping != prevState.sleeping)
                {
                    BlockPos pos = new BlockPos(Math.floor(mc.thePlayer.posX), Math.floor(mc.thePlayer.posY), Math.floor(mc.thePlayer.posZ));
                    actions.add(new ActionComponent(7, nextState.sleeping && mc.theWorld.getBlockState(pos).getBlock() instanceof BlockBed ? mc.theWorld.getBlockState(pos).getBlock().getBedDirection(mc.theWorld.getBlockState(pos), mc.theWorld, pos).ordinal() : 0, null));
                }

                if(!(nextState.rot[0] == prevState.rot[0] && nextState.rot[1] == prevState.rot[1]))
                {
                    actionToRecord.lookComponents.put(recordActionFrom, new LimbComponent(rotChange));
                }

                if(!(nextState.pos[0] == prevState.pos[0] && nextState.pos[1] == prevState.pos[1] && nextState.pos[2] == prevState.pos[2]))
                {
                    actionToRecord.posComponents.put(recordActionFrom, new LimbComponent(posChange));
                }

                if(!actions.isEmpty())
                {
                    actionToRecord.actionComponents.put(recordActionFrom, actions);
                }

                prevState = nextState;

                recordActionFrom++;
            }
        }
    }
}
