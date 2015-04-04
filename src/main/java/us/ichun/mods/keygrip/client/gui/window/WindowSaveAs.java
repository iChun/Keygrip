package us.ichun.mods.keygrip.client.gui.window;

import com.google.gson.Gson;
import net.minecraft.util.StatCollector;
import org.apache.commons.io.FileUtils;
import us.ichun.mods.ichunutil.client.gui.Theme;
import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.WindowPopup;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementTextInputSaveAs;
import us.ichun.mods.ichunutil.common.core.util.IOUtil;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.keygrip.client.core.ResourceHelper;
import us.ichun.mods.keygrip.client.gui.GuiWorkspace;
import us.ichun.mods.keygrip.common.scene.Scene;
import us.ichun.mods.keygrip.common.scene.action.Action;

import java.io.File;
import java.io.IOException;

public class WindowSaveAs extends Window
{
    private static final String[] invalidChars = new String[] { "\\\\", "/", ":", "\\*", "\\?", "\"", "<", ">", "|" };

    public boolean shouldClose;
    public boolean closeProject;

    public WindowSaveAs(IWorkspace parent, int x, int y, int w, int h, int minW, int minH, boolean close)
    {
        super(parent, x, y, w, h, minW, minH, "window.saveAs.title", true);

        Scene project = ((GuiWorkspace)workspace).sceneManager.scenes.get(((GuiWorkspace)workspace).sceneManager.selectedScene);

        elements.add(new ElementTextInputSaveAs(this, 10, 30, width - 20, 12, 1, "window.saveAs.fileName", project.name.replaceAll("[^A-Za-z0-9()\\[\\]]", "")));
        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 3, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));

        closeProject = close;
    }

    @Override
    public void update()
    {
        if(shouldClose)
        {
            workspace.removeWindow(this, true);
            if(closeProject && !((GuiWorkspace)workspace).sceneManager.scenes.isEmpty())
            {
                ((GuiWorkspace)workspace).sceneManager.removeScene(((GuiWorkspace)workspace).sceneManager.scenes.get(((GuiWorkspace)workspace).sceneManager.selectedScene).identifier);
            }
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.saveAs.fileName"), posX + 11, posY + 20, Theme.getAsHex(workspace.currentTheme.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id == 3)
        {
            String projName = "";
            for(int i = 0; i < elements.size(); i++)
            {
                if(elements.get(i) instanceof ElementTextInput)
                {
                    ElementTextInput text = (ElementTextInput)elements.get(i);
                    if(text.id == 1)
                    {
                        projName = text.textField.getText();
                    }
                }
            }
            if(projName.isEmpty())
            {
                return;
            }

            if(!projName.endsWith(".kgs"))
            {
                projName = projName + ".kgs";
            }

            File file = new File(ResourceHelper.getScenesDir(), projName);

            Scene scene = ((GuiWorkspace)workspace).sceneManager.scenes.get(((GuiWorkspace)workspace).sceneManager.selectedScene);

            if(workspace.windowDragged == this)
            {
                workspace.windowDragged = null;
            }
            if(file.exists())
            {
                workspace.addWindowOnTop(new WindowOverwrite(workspace, this, scene, file).putInMiddleOfScreen());
            }
            else
            {
                if(Scene.saveScene(scene, file))
                {
                    scene.saveFile = file;
                    scene.saveFileMd5 = IOUtil.getMD5Checksum(file);

                    Scene.saveSceneActions(scene);

                    workspace.removeWindow(this, true);

                    if(closeProject && !((GuiWorkspace)workspace).sceneManager.scenes.isEmpty())
                    {
                        ((GuiWorkspace)workspace).sceneManager.removeScene(scene.identifier);
                    }
                }
                else
                {
                    workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "window.saveAs.failed").putInMiddleOfScreen());
                }
            }
        }
    }
}
