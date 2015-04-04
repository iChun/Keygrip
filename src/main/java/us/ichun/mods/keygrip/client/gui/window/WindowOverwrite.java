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
import us.ichun.mods.ichunutil.common.core.util.IOUtil;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.keygrip.client.core.ResourceHelper;
import us.ichun.mods.keygrip.common.scene.Scene;
import us.ichun.mods.keygrip.common.scene.action.Action;

import java.io.File;
import java.io.IOException;

public class WindowOverwrite extends Window
{
    public WindowSaveAs parentWindow;
    public Scene scene;
    public File saveFile;

    public WindowOverwrite(IWorkspace parent, WindowSaveAs win, Scene scene, File file)
    {
        super(parent, 0, 0, 300, 120, 300, 120, "window.saveAs.overwrite", true);

        parentWindow = win;
        this.scene = scene;
        saveFile = file;

        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 3, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.saveAs.confirmOverwrite"), posX + 15, posY + 40, Theme.getAsHex(workspace.currentTheme.font), false);
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
            if(workspace.windowDragged == this)
            {
                workspace.windowDragged = null;
            }

            if(Scene.saveScene(scene, saveFile))
            {
                scene.saveFile = saveFile;
                scene.saveFileMd5 = IOUtil.getMD5Checksum(saveFile);

                Scene.saveSceneActions(scene);

                parentWindow.shouldClose = true;
            }
            else
            {
                workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "window.saveAs.failed").putInMiddleOfScreen());
            }
            workspace.removeWindow(this, true);
        }
    }
}
