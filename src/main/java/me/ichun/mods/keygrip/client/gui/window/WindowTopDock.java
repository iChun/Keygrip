package me.ichun.mods.keygrip.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.WindowTopDockBase;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButtonTextured;
import me.ichun.mods.keygrip.client.gui.GuiWorkspace;
import net.minecraft.util.ResourceLocation;

public class WindowTopDock extends WindowTopDockBase
{
    public static final int ID_NEW = 0;
    public static final int ID_EDIT = 1;
    public static final int ID_OPEN = 2;
    public static final int ID_SAVE = 3;
    public static final int ID_SAVE_AS = 4;
    public static final int ID_IMPORT_ACTION = 5;

    public GuiWorkspace parent;

    public WindowTopDock(GuiWorkspace parent, int w, int h)
    {
        super(parent, w, h);
        this.parent = parent;

        int button = 0;
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_NEW, true, 0, 0, "window.newScene.title", new ResourceLocation("keygrip", "textures/icon/new.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_EDIT, true, 0, 0, "window.editScene.title", new ResourceLocation("keygrip", "textures/icon/edit.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_OPEN, true, 0, 0, "window.open.title", new ResourceLocation("keygrip", "textures/icon/open.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_SAVE, true, 0, 0, "topdock.save", new ResourceLocation("keygrip", "textures/icon/save.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_SAVE_AS, true, 0, 0, "window.saveAs.title", new ResourceLocation("keygrip", "textures/icon/saveAs.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_IMPORT_ACTION, true, 0, 0, "window.importAction.title", new ResourceLocation("keygrip", "textures/icon/importAction.png")));
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == ID_NEW)
        {
            workspace.addWindowOnTop(new WindowNewScene(workspace, 0, 0, 200, 100, 200, 100).putInMiddleOfScreen());
        }
        else if(element.id == ID_EDIT)
        {
            if(parent.hasOpenScene())
            {
                workspace.addWindowOnTop(new WindowEditScene(workspace, 0, 0, 200, 120, 200, 120).putInMiddleOfScreen());
            }
        }
        else if(element.id == ID_OPEN)
        {
            workspace.addWindowOnTop(new WindowOpenScene(workspace, 0, 0, 260, 230, 240, 160).putInMiddleOfScreen());
        }
        else if(element.id == ID_SAVE)
        {
            parent.save(false);
        }
        else if(element.id == ID_SAVE_AS)
        {
            if(parent.hasOpenScene())
            {
                workspace.addWindowOnTop(new WindowSaveAs(workspace, 0, 0, 200, 100, 200, 100, false).putInMiddleOfScreen());
            }
        }
        else if(element.id == ID_IMPORT_ACTION)
        {
            if(parent.hasOpenScene())
            {
                workspace.addWindowOnTop(new WindowImportAction(workspace, 0, 0, 260, 220, 240, 160).putInMiddleOfScreen());
            }
        }
    }
}
