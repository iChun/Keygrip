package us.ichun.mods.keygrip.client.gui.window.element;

import us.ichun.mods.ichunutil.client.gui.Theme;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.ichunutil.common.core.util.IOUtil;
import us.ichun.mods.keygrip.client.core.ResourceHelper;
import us.ichun.mods.keygrip.client.gui.window.WindowSaveBeforeClosing;
import us.ichun.mods.keygrip.client.gui.window.WindowSceneSelection;
import us.ichun.mods.keygrip.common.scene.Scene;

import java.io.File;

public class ElementSceneTab extends Element
{
    public Scene info;
    public boolean changed;

    public ElementSceneTab(Window window, int x, int y, int w, int h, int ID, Scene inf)
    {
        super(window, x, y, w, h, ID, true);
        info = inf;
        changed = false;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        WindowSceneSelection proj = (WindowSceneSelection)parent;
        if(id != proj.scenes.size() - 1)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.tabSideInactive[0], parent.workspace.currentTheme.tabSideInactive[1], parent.workspace.currentTheme.tabSideInactive[2], 255, getPosX() + width - 1, getPosY() + 1, 1, height, 0);
        }
        if(proj.selectedScene == id)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementProjectTabActive[0], parent.workspace.currentTheme.elementProjectTabActive[1], parent.workspace.currentTheme.elementProjectTabActive[2], 255, getPosX(), getPosY(), width - 1, height, 0);
        }
        else if(hover)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementProjectTabHover[0], parent.workspace.currentTheme.elementProjectTabHover[1], parent.workspace.currentTheme.elementProjectTabHover[2], 255, getPosX(), getPosY(), width - 1, height, 0);
        }

        String titleToRender = info.name;
        while(titleToRender.length() > 1 && parent.workspace.getFontRenderer().getStringWidth(titleToRender) > width -  11)
        {
            if(titleToRender.startsWith("... "))
            {
                break;
            }
            if(titleToRender.endsWith("... "))
            {
                titleToRender = titleToRender.substring(0, titleToRender.length() - 5) + "... ";
            }
            else
            {
                titleToRender = titleToRender.substring(0, titleToRender.length() - 1) + "... ";
            }
        }
        parent.workspace.getFontRenderer().drawString(titleToRender, parent.posX + posX + 4, parent.posY + posY + 3, Theme.getAsHex(changed ? parent.workspace.currentTheme.elementProjectTabFontChanges : parent.workspace.currentTheme.elementProjectTabFont), false);
        parent.workspace.getFontRenderer().drawString("X", parent.posX + posX + width - 8, parent.posY + posY + 3, Theme.getAsHex(parent.workspace.currentTheme.elementProjectTabFont), false);
    }

    @Override
    public void resized()
    {
        WindowSceneSelection tab = (WindowSceneSelection)parent;
        int space = tab.getWidth();
        int totalSpace = 0;
        for(Scene tab1 : tab.scenes)
        {
            totalSpace += tab.workspace.getFontRenderer().getStringWidth(" " + tab1.name + " X ");
        }
        if(totalSpace > space)
        {
            posX = (id * (space / tab.scenes.size()));
            posY = 0;
            width = space / tab.scenes.size();
            height = 12;
        }
        else
        {
            posX = 0;
            for(int i = 0; i < id; i++)
            {
                posX += tab.workspace.getFontRenderer().getStringWidth(" " + tab.scenes.get(i).name + " X ");
            }
            posY = 0;
            width = tab.workspace.getFontRenderer().getStringWidth(" " + info.name + " X ");
            height = 12;
        }
    }

    @Override
    public String tooltip()
    {
        String tooltip = info.name;
        if(info.saveFile != null)
        {
            tooltip = tooltip + " - " + info.saveFile.getName();
        }
        return tooltip; //return null for no tooltip. This is localized.
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        if(id == 0 || id == 2)
        {
            ((WindowSceneSelection)parent).changeScene(this.id);
            if((mouseX + parent.posX > getPosX() + width - 9 || id == 2))
            {
                Scene scene = ((WindowSceneSelection)parent).scenes.get(this.id);

                String md5 = null;

                File temp = new File(ResourceHelper.getTempDir(), Integer.toString(Math.abs(scene.hashCode())) + ".kgs");

                if(Scene.saveScene(scene, temp))
                {
                    md5 = IOUtil.getMD5Checksum(temp);
                    temp.delete();
                }

                if(scene.saveFile == null || scene.saveFileMd5 == null || !scene.saveFileMd5.equals(md5))
                {
                    parent.workspace.addWindowOnTop(new WindowSaveBeforeClosing(parent.workspace, scene).putInMiddleOfScreen());
                }
                else
                {
                    ((WindowSceneSelection)parent).removeScene(scene.identifier);
                }
            }
        }
        return false;
    }
}
