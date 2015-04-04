package us.ichun.mods.keygrip.client.gui.window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.WindowPopup;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementButtonTextured;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.keygrip.client.gui.GuiWorkspace;
import us.ichun.mods.keygrip.client.gui.window.element.ElementTimeline;
import us.ichun.mods.keygrip.common.Keygrip;
import us.ichun.mods.keygrip.common.packet.PacketStopScene;
import us.ichun.mods.keygrip.common.scene.Scene;
import us.ichun.mods.keygrip.common.scene.action.Action;

import java.util.Collections;

public class WindowTimeline extends Window
{
    public static final int ID_NEW_ACTION = 0;
    public static final int ID_EDIT_ACTION = 1;
    public static final int ID_DEL_ACTION = 2;
    public static final int ID_REC_ACTION = 3;
    public static final int ID_PLAY_SCENE = 4;
    public static final int ID_STOP_SCENE = 5;

    public GuiWorkspace parent;

    public ElementTimeline timeline;

    public WindowTimeline(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.timeline.title", true);
        this.parent = parent;
        timeline = new ElementTimeline(this, 1, 13, width - 2, height - 14, -2);
        elements.add(timeline);

        int button = 0;
        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_NEW_ACTION, true, 0, 1, "window.timeline.newAction", new ResourceLocation("keygrip", "textures/icon/newAction.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_EDIT_ACTION, true, 0, 1, "window.timeline.editAction", new ResourceLocation("keygrip", "textures/icon/editAction.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_DEL_ACTION, true, 0, 1, "window.timeline.delAction", new ResourceLocation("keygrip", "textures/icon/delAction.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_REC_ACTION, true, 0, 1, "window.timeline.recAction", new ResourceLocation("keygrip", "textures/icon/recAction.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_PLAY_SCENE, true, 0, 1, "window.timeline.playScene", new ResourceLocation("keygrip", "textures/icon/playScene.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_STOP_SCENE, true, 0, 1, "window.timeline.stopScene", new ResourceLocation("keygrip", "textures/icon/stopScene.png")));
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(parent.hasOpenScene())
        {
            if(element.id == ID_NEW_ACTION)
            {
                workspace.addWindowOnTop(new WindowNewAction(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 220, 200, 220).putInMiddleOfScreen());
            }
            else if(element.id == ID_EDIT_ACTION)
            {
                if(!parent.timeline.timeline.selectedIdentifier.isEmpty())
                {
                    workspace.addWindowOnTop(new WindowEditAction(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 260, 200, 260).putInMiddleOfScreen());
                }
            }
            else if(element.id == ID_DEL_ACTION)
            {
                if(!parent.timeline.timeline.selectedIdentifier.isEmpty())
                {
                    Scene scene = parent.getOpenScene();
                    for(int i = scene.actions.size() - 1; i >= 0; i--)
                    {
                        Action act = scene.actions.get(i);
                        if(act.identifier.equals(parent.timeline.timeline.selectedIdentifier))
                        {
                            scene.actions.remove(i);
                            parent.timeline.timeline.selectedIdentifier = "";
                            Collections.sort(scene.actions);
                            break;
                        }
                    }
                }
            }
            else if(element.id == ID_REC_ACTION)
            {
                if(parent.timeline.timeline.selectedIdentifier.isEmpty())
                {
                    workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "window.recAction.noAction").putInMiddleOfScreen());
                }
                parent.toggleRecording();
            }
            else if(element.id == ID_PLAY_SCENE)
            {
                if(parent.hasOpenScene() && parent.sceneSendingCooldown <= 0)
                {
                    if(parent.timeline.timeline.getCurrentPos() > parent.getOpenScene().getLength())
                    {
                        parent.timeline.timeline.setCurrentPos(0);
                    }
                    if(GuiScreen.isCtrlKeyDown())
                    {
                        Minecraft.getMinecraft().displayGuiScreen(null);
                        Minecraft.getMinecraft().setIngameFocus();
                    }
                    Scene.sendSceneToServer(parent.getOpenScene());
                }
                parent.sceneSendingCooldown = 10;
            }
            else if(element.id == ID_STOP_SCENE)
            {
                if(Keygrip.proxy.tickHandlerClient.actionToRecord != null)
                {
                    parent.toggleRecording(); //Lets stop recording in case the end user doesn't know it's a record toggle button.
                }
                else if(parent.hasOpenScene())
                {
                    parent.getOpenScene().stop();

                    Keygrip.channel.sendToServer(new PacketStopScene(parent.getOpenScene().identifier));
                }
            }
        }
    }

    @Override
    public boolean canBeDragged()
    {
        return false;
    }

    @Override
    public int clickedOnBorder(int mouseX, int mouseY, int id)//only left clicks
    {
        if(id == 0 && !minimized)
        {
            return ((mouseY <= BORDER_SIZE + 1) ? 1 : 0) + 1; //you can only drag the top
        }
        return 0;
    }

    @Override
    public void setScissor()
    {
        RendererHelper.startGlScissor(posX, posY + 1, getWidth(), getHeight());
    }

    @Override
    public boolean invertMinimizeSymbol()
    {
        return true;
    }

    @Override
    public boolean interactableWhileNoProjects()
    {
        return false;
    }
}
