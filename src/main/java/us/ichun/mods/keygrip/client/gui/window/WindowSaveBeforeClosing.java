package us.ichun.mods.keygrip.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.ichunutil.client.gui.Theme;
import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import us.ichun.mods.keygrip.client.gui.GuiWorkspace;
import us.ichun.mods.keygrip.common.scene.Scene;

public class WindowSaveBeforeClosing extends Window
{
    public Scene project;

    public WindowSaveBeforeClosing(IWorkspace parent, Scene projectInfo)
    {
        super(parent, 0, 0, 300, 120, 300, 120, "window.notSaved.title", true);

        project = projectInfo;

        elements.add(new ElementButton(this, width - 210, height - 30, 60, 16, 3, false, 1, 1, "gui.yes"));
        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 0, false, 1, 1, "gui.no"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, -1, false, 1, 1, "element.button.cancel"));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.notSaved.unsaved"), posX + 15, posY + 40, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.notSaved.save"), posX + 15, posY + 52, Theme.getAsHex(workspace.currentTheme.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id <= 0)
        {
            workspace.removeWindow(this, true);
            if(element.id == 0)
            {
                ((GuiWorkspace)workspace).sceneManager.removeScene(project.identifier);
            }
        }
        if(element.id == 3)
        {
            if(workspace.windowDragged == this)
            {
                workspace.windowDragged = null;
            }
            ((GuiWorkspace)workspace).save(true);
            workspace.removeWindow(this, true);
        }
    }
}
