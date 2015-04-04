package us.ichun.mods.keygrip.client.core;

import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.keygrip.client.gui.GuiWorkspace;
import us.ichun.mods.keygrip.common.Keygrip;
import us.ichun.mods.keygrip.common.scene.Scene;
import us.ichun.mods.keygrip.common.scene.action.Action;
import us.ichun.mods.keygrip.common.scene.action.ActionComponent;
import us.ichun.mods.keygrip.common.scene.action.EntityState;
import us.ichun.mods.keygrip.common.scene.action.LimbComponent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

public class TickHandlerClient
{
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
                        mc.fontRendererObj.drawString(StatCollector.translateToLocal("window.recording"), (pX + 25) / scale, (pY + 2) / scale, 0xffffff, true);
                    }
                }
                GlStateManager.popMatrix();
            }

            for(int i = sleepers.size() - 1 ; i >= 0; i--)
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
                    player.playerLocation = pos;
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
                else if(dimension != mc.theWorld.provider.getDimensionId() || mc.thePlayer.isDead)
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
                    ItemStack is = mc.thePlayer.getCurrentEquippedItem();
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
                        catch(IOException ignored){}
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

                double[] posChange = new double[] { nextState.pos[0] - ((actionToRecord.offsetPos[0] + sceneFrom.startPos[0])  / (double)Scene.PRECISION), nextState.pos[1] - ((actionToRecord.offsetPos[1] + sceneFrom.startPos[1]) / (double)Scene.PRECISION), nextState.pos[2] - ((actionToRecord.offsetPos[2] + sceneFrom.startPos[2]) / (double)Scene.PRECISION) };
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
                    actions.add(new ActionComponent(7, nextState.sleeping && mc.theWorld.getBlockState(pos).getBlock() instanceof BlockBed ? mc.theWorld.getBlockState(pos).getBlock().getBedDirection(mc.theWorld, pos).ordinal() : 0, null));
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

    public final ResourceLocation txRec = new ResourceLocation("keygrip", "textures/icon/recording.png");
    public final ResourceLocation txRecPulse = new ResourceLocation("keygrip", "textures/icon/recordingPulse.png");

    public ArrayList<EntityPlayer> sleepers = new ArrayList<EntityPlayer>();

    public GuiWorkspace workspace;

    public Action actionToRecord;
    public Scene sceneFrom;
    public int recordActionFrom;
    public int startRecordTime;
    public boolean startRecord;

    public int dimension;

    public EntityState prevState;
    public EntityState nextState;
}
