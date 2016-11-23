package me.ichun.mods.keygrip.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementListTree;
import me.ichun.mods.ichunutil.common.core.util.IOUtil;
import me.ichun.mods.keygrip.client.core.ResourceHelper;
import me.ichun.mods.keygrip.client.gui.GuiWorkspace;
import me.ichun.mods.keygrip.common.scene.Scene;
import me.ichun.mods.keygrip.common.scene.action.Action;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class WindowImportAction extends Window
{
    public ElementListTree modelList;

    public WindowImportAction(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.importAction.title", true);

        elements.add(new ElementButton(this, width - 140, height - 22, 60, 16, 1, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 22, 60, 16, 0, false, 1, 1, "element.button.cancel"));
        modelList = new ElementListTree(this, BORDER_SIZE + 1, BORDER_SIZE + 1 + 10, width - (BORDER_SIZE * 2 + 2), height - BORDER_SIZE - 22 - 16, 3, false, false);
        elements.add(modelList);

        ArrayList<File> files = new ArrayList<File>();

        File[] textures = ResourceHelper.getActionsDir().listFiles();

        for(File file : textures)
        {
            if(!file.isDirectory() && FilenameUtils.getExtension(file.getName()).equals("kga"))
            {
                files.add(file);
            }
        }

        for(File file : files)
        {
            modelList.createTree(null, file, 26, 0, false, false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if((element.id == 1 || element.id == 3) && ((GuiWorkspace)workspace).hasOpenScene())
        {
            for(int i = 0; i < modelList.trees.size(); i++)
            {
                ElementListTree.Tree tree = modelList.trees.get(i);
                if(tree.selected)
                {
                    if(workspace.windowDragged == this)
                    {
                        workspace.windowDragged = null;
                    }
                    Action action = Action.openAction((File)tree.attachedObject);
                    if(action != null)
                    {
                        Minecraft mc = Minecraft.getMinecraft();
                        Scene scene = ((GuiWorkspace)workspace).getOpenScene();
                        action.identifier = RandomStringUtils.randomAscii(IOUtil.IDENTIFIER_LENGTH);
                        action.startKey = ((GuiWorkspace)workspace).timeline.timeline.getCurrentPos();
                        //TODO remember to doc down that importing refs the player.
                        if(!GuiScreen.isShiftKeyDown())
                        {
                            action.offsetPos = new int[] { (int)Math.round(mc.thePlayer.posX * Scene.PRECISION) - scene.startPos[0], (int)Math.round(mc.thePlayer.posY * Scene.PRECISION) - scene.startPos[1], (int)Math.round(mc.thePlayer.posZ * Scene.PRECISION) - scene.startPos[2] };
                        }
                        scene.actions.add(action);
                        Collections.sort(scene.actions);
                        workspace.removeWindow(this, true);
                    }
                    else
                    {
                        workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "window.importAction.failed").putInMiddleOfScreen());
                    }
                    break;
                }
            }
        }
    }
}
