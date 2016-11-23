package me.ichun.mods.keygrip.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.Theme;
import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.keygrip.client.gui.GuiWorkspace;
import me.ichun.mods.keygrip.common.scene.Scene;
import net.minecraft.util.text.translation.I18n;

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
            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.notSaved.unsaved"), posX + 15, posY + 40, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.notSaved.save"), posX + 15, posY + 52, Theme.getAsHex(workspace.currentTheme.font), false);
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
